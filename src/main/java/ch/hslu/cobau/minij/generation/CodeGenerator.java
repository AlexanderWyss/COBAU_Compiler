package ch.hslu.cobau.minij.generation;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

import java.util.*;

import static java.lang.String.format;

public class CodeGenerator extends BaseAstVisitor {
    private final StringBuilder code = new StringBuilder();
    private final SymbolTable symbolTable;
    private String currentScope = null;
    private final Map<String, Map<String, Integer>> scopes = new HashMap<>();
    private final Set<String> globals = new HashSet<>();
    private final Stack<Boolean> expressionByReference = new Stack<>();
    private final Deque<String> statements = new ArrayDeque<>();
    private int ifLabels = 0;
    private int whileLabels = 0;
    private int boolLabels = 0;

    private static final List<String> PARAM_REGISTERS = List.of("RDI", "RSI", "RDX", "RCX", "R8", "R9");

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    private Map<String, Integer> getLocals() {
        return scopes.computeIfAbsent(currentScope, ident -> new HashMap<>());
    }

    private String formatIndented(String statement, Object... args) {
        StringBuilder formatted = new StringBuilder();
        final String[] lines = format(statement, args).split("\n");
        for (String line : lines) {
            formatted.append(format("    %s\n", line.strip()));
        }
        return formatted.toString();
    }

    private void addIndented(String statement, Object... args) {
        statements.add(formatIndented(statement, args));
    }

    private String formatUnindented(String statement, Object... args) {
        StringBuilder formatted = new StringBuilder();
        final String[] lines = format(statement, args).split("\n");
        for (String line : lines) {
            formatted.append(format("%s\n", line));
        }
        return formatted.toString();
    }

    private void add(String statement, Object... args) {
        statements.add(formatUnindented(statement, args));
    }

    private void pushReference(String register) {
        addIndented("push %s", register);
        expressionByReference.add(true);
    }

    private void push(String register) {
        addIndented("push %s", register);
        expressionByReference.add(false);
    }

    private void pop(String register) {
        addIndented("pop %s", register);
        if (expressionByReference.pop()) {
            addIndented("mov %s, [%s]", register, register);
        }
    }

    private void popReference(String register) {
        addIndented("pop %s", register);
        if (!expressionByReference.pop()) {
            throw new UnsupportedOperationException("Not a reference");
        }
    }

    @Override
    public void visit(final Unit program) {
        program.visitDeclarations(this);
        code.append(formatUnindented("""
                DEFAULT REL
                extern writeInt
                extern writeChar
                extern _exit
                extern readInt
                extern readChar
                section .data
                    ALIGN 8
                """));
        for (String global : globals) {
            code.append(formatIndented("%s dq 0", global));
        }
        code.append(formatUnindented("""
                section .text
                global _start
                _start:
                    push rbp
                    mov rbp, rsp
                    call main
                    mov rsp, rbp
                    pop rbp
                    mov rdi, rax
                    call _exit
                        """));
        program.visitFunctions(this);
    }

    @Override
    public void visit(final Function procedure) {
        currentScope = procedure.getIdentifier();
        super.visit(procedure);
        code.append(formatUnindented("%s:", currentScope));
        code.append(formatIndented("""
                push rbp
                mov rbp, rsp
                """));

        Map<String, Integer> locals = getLocals();
        int stackSize = locals.size() * 8;
        stackSize += stackSize % 16; // align to 16 bytes
        code.append(formatIndented("sub rsp, %d", stackSize));

        List<Integer> nonStructIndexes = new ArrayList<>();
        List<Declaration> formalParameters = procedure.getFormalParameters();
        int popCount = 0;
        for (int i = 0; i < formalParameters.size(); i++) {
            Declaration formalParam = formalParameters.get(i);
            if (formalParam.getType() instanceof RecordType && !formalParam.isReference()) {
                Struct record = symbolTable.getRecordType(((RecordType) formalParam.getType()).getIdentifier());
                List<Declaration> recordDeclarations = record.getDeclarations();
                for (int declarationIndex = recordDeclarations.size() - 1; declarationIndex >= 0; declarationIndex--) {
                    Declaration recordDeclaration = recordDeclarations.get(declarationIndex);
                    code.append(formatIndented("mov rax, [rbp+%d]", popCount * 8 + 16));
                    code.append(formatIndented("mov [rbp-%d], rax", locals.get(format("%s.%s", formalParam.getIdentifier(), recordDeclaration.getIdentifier())) * 8));
                    popCount++;
                }
            } else {
                nonStructIndexes.add(i);
            }
        }
        for (int i = nonStructIndexes.size() - 1; i >= 0; i--) {
            Integer nonStructIndex = nonStructIndexes.get(i);
            if (i < PARAM_REGISTERS.size()) {
                code.append(formatIndented("mov [rbp-%d], %s", locals.get(formalParameters.get(nonStructIndex).getIdentifier()) * 8, PARAM_REGISTERS.get(i)));
            } else {
                code.append(formatIndented("mov rax, [rbp+%d]", popCount * 8 + 16));
                code.append(formatIndented("mov [rbp-%d], rax", locals.get(formalParameters.get(nonStructIndex).getIdentifier()) * 8));
                popCount++;
            }
        }

        while (!statements.isEmpty()) {
            code.append(statements.poll());
        }

        code.append(formatUnindented("""
                _%sEnd:
                    mov rsp, rbp
                    pop rbp
                    ret
                """, currentScope));
        currentScope = null;
    }

    @Override
    public void visit(final ReturnStatement returnStatement) {
        super.visit(returnStatement);
        if (returnStatement.getExpression() != null) {
            pop("rax");
        }
        addIndented("jmp _%sEnd", currentScope);
    }


    @Override
    public void visit(final CallExpression callExpression) {
        List<Integer> structIndexes = new ArrayList<>();
        List<Declaration> formalParameters = symbolTable.getFunction(callExpression.getIdentifier()).getFormalParameters();
        int pushCount = 0;
        for (int i = 0; i < callExpression.getParameters().size(); i++) {
            Declaration formalParam = formalParameters.get(i);
            if (formalParam.getType() instanceof RecordType && !formalParam.isReference()) {
                structIndexes.add(i);
            } else {
                callExpression.getParameters().get(i).accept(this);
                int paramCount = i - structIndexes.size();
                if (paramCount < PARAM_REGISTERS.size()) {
                    if (formalParam.isReference()) {
                        popReference(PARAM_REGISTERS.get(paramCount));
                    } else {
                        pop(PARAM_REGISTERS.get(paramCount));
                    }
                } else {
                    if (formalParam.isReference()) {
                        popReference("rax");
                        push("rax");
                    }
                    pushCount++;
                }
            }
        }
        for (Integer structIndex : structIndexes) {
            callExpression.getParameters().get(structIndex).accept(this);
            Struct record = symbolTable.getRecordType(((RecordType) formalParameters.get(structIndex).getType()).getIdentifier());
            popReference("rax");
            for (int i = 0; i < record.getDeclarations().size(); i++) {
                addIndented("mov rbx, [rax-%d]", (i + 1) * 8);
                push("rbx");
                pushCount++;
            }
        }
        addIndented("call %s", callExpression.getIdentifier());
        for (int i = 0; i < pushCount; i++) {
            pop("rdi");
        }
        push("rax");
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);
        pop("rax"); // unused return value from CallExpression
    }

    @Override
    public void visit(final Declaration declaration) {
        String identifier = declaration.getIdentifier();
        if (currentScope != null) {
            final Map<String, Integer> locals = getLocals();
            int position = (locals.size() + 1);
            if (!locals.containsKey(identifier)) {
                locals.put(identifier, position);
                if (declaration.getType() instanceof RecordType) {
                    Struct recordType = symbolTable.getRecordType(((RecordType) declaration.getType()).getIdentifier());
                    for (Declaration recordTypeDeclaration : recordType.getDeclarations()) {
                        locals.put(format("%s.%s", identifier, recordTypeDeclaration.getIdentifier()), ++position);
                    }
                }
            }
        } else {
            globals.add(identifier);
            if (declaration.getType() instanceof RecordType) {
                Struct recordType = symbolTable.getRecordType(((RecordType) declaration.getType()).getIdentifier());
                for (Declaration recordTypeDeclaration : recordType.getDeclarations()) {
                    globals.add(format("%s.%s", identifier, recordTypeDeclaration.getIdentifier()));
                }
            }
        }
    }


    @Override
    public void visit(final VariableAccess variable) {
        String identifier = variable.getIdentifier();
        Optional<Declaration> formalParam = symbolTable.getFunction(currentScope).getFormalParameters().stream().filter(param -> param.getIdentifier().equals(identifier)).findFirst();
        if (formalParam.isPresent() && formalParam.get().isReference()) {
            addIndented("mov rax, %s", getVariable(identifier));
        } else {
            addIndented("lea rax, %s", getVariable(identifier));
        }
        pushReference("rax");
    }

    private String getVariable(String identifier) {
        if (currentScope != null) {
            final Map<String, Integer> locals = getLocals();
            if (locals.containsKey(identifier)) {
                return format("[rbp-%d]", locals.get(identifier) * 8);
            }
        }
        return format("qword[%s]", identifier); // global
    }

    @Override
    public void visit(final FieldAccess fieldAccess) {
        super.visit(fieldAccess);
        Type resultType = fieldAccess.getBase().getResultType(symbolTable, symbolTable.getScope(currentScope));
        assert resultType instanceof RecordType;
        Struct record = symbolTable.getRecordType(((RecordType) resultType).getIdentifier());
        int index = record.getDeclarations().stream().map(Declaration::getIdentifier).toList().indexOf(fieldAccess.getField());
        popReference("rax");
        addIndented("lea rax, [rax-%d]", (index + 1) * 8);
        pushReference("rax");
    }

    @Override
    public void visit(final IfStatement ifStatement) {
        ifStatement.visitExpression(this);
        int ifCount = ifLabels++;
        String ifLabel = format("_else%d", ifCount);
        String endIfLabel = format("_endElse%d", ifCount);
        pop("rax");
        addIndented("cmp rax, 0");
        addIndented("je %s", ifLabel);
        ifStatement.visitBlock(this);
        addIndented("jmp %s", endIfLabel);
        add("%s:", ifLabel);
        ifStatement.visitElse(this);
        add("%s:", endIfLabel);
    }

    @Override
    public void visit(final WhileStatement whileStatement) {
        int whileCount = whileLabels++;
        String whileLabel = format("_while%d", whileCount);
        String endWhileLabel = format("_endWhile%d", whileCount);
        addIndented("jmp %s", endWhileLabel);
        add("%s:", whileLabel);
        whileStatement.visitBlock(this);
        add("%s:", endWhileLabel);
        whileStatement.visitExpression(this);
        pop("rax");
        addIndented("cmp rax, 1");
        addIndented("je %s", whileLabel);
    }

    @Override
    public void visit(final AssignmentStatement assignment) {
        super.visit(assignment);
        Type resultType = assignment.getRight().getResultType(symbolTable, symbolTable.getScope(currentScope));
        if (resultType instanceof RecordType) {
            Struct record = symbolTable.getRecordType(((RecordType) resultType).getIdentifier());
            popReference("rbx");
            popReference("rax");
            List<Declaration> declarations = record.getDeclarations();
            for (int i = 0; i < declarations.size(); i++) {
                addIndented("mov [rax-%d], [rbx-%d]", (i + 1) * 8);
            }
        } else {
            pop("rbx");
            popReference("rax");
            addIndented("mov [rax], rbx");
        }
    }

    @Override
    public void visit(final BinaryExpression binaryExpression) {
        switch (binaryExpression.getBinaryOperator()) {
            case PLUS, MINUS, TIMES, DIV, MOD, EQUAL, UNEQUAL, LESSER, LESSER_EQ, GREATER, GREATER_EQ -> {
                super.visit(binaryExpression);
                pop("rbx");
                pop("rax");
                switch (binaryExpression.getBinaryOperator()) {
                    case PLUS -> addIndented("add rax, rbx");
                    case MINUS -> addIndented("sub rax, rbx");
                    case TIMES -> addIndented("imul rax, rbx");
                    case DIV, MOD -> {
                        addIndented("""
                                mov rdx, 0
                                cqo
                                idiv rbx
                                """);
                        if (BinaryOperator.MOD.equals(binaryExpression.getBinaryOperator())) {
                            addIndented("mov rax, rdx");
                        }
                    }
                    default -> {
                        addIndented("cmp rax, rbx");
                        switch (binaryExpression.getBinaryOperator()) {
                            case EQUAL -> addIndented("sete al");
                            case UNEQUAL -> addIndented("setne al");
                            case LESSER -> addIndented("setl al");
                            case LESSER_EQ -> addIndented("setle al");
                            case GREATER -> addIndented("setg al");
                            case GREATER_EQ -> addIndented("setge al");
                        }
                        addIndented("movsx rax, al");
                    }
                }
            }
            case AND, OR -> {
                String boolLabel = format("_bool%d", boolLabels++);
                binaryExpression.getLeft().accept(this);
                pop("rax");
                switch (binaryExpression.getBinaryOperator()) {
                    case AND -> addIndented("""
                            cmp rax, 0
                            je %s
                            """, boolLabel);
                    case OR -> addIndented("""
                            cmp rax, 1
                            je %s
                            """, boolLabel);
                }
                binaryExpression.getRight().accept(this);
                pop("rax");
                add("%s:", boolLabel);
                addIndented("""
                        cmp rax, 1
                        sete al
                        movsx rax, al
                        """);
            }

        }
        push("rax");
    }

    @Override
    public void visit(final UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
        switch (unaryExpression.getUnaryOperator()) {
            case MINUS -> {
                pop("rax");
                addIndented("neg rax");
                push("rax");
            }
            case NOT -> {
                pop("rax");
                addIndented("xor rax, 1");
                push("rax");
            }
            case PRE_INCREMENT -> {
                popReference("rax");
                addIndented("add qword [rax], 1");
                pushReference("rax");
            }
            case PRE_DECREMENT -> {
                popReference("rax");
                addIndented("sub qword [rax], 1");
                pushReference("rax");
            }
            case POST_INCREMENT -> {
                popReference("rax");
                push("qword [rax]");
                addIndented("add qword [rax], 1");
            }
            case POST_DECREMENT -> {
                popReference("rax");
                push("qword [rax]");
                addIndented("sub qword [rax], 1");
            }
        }
    }

    @Override
    public void visit(final IntegerConstant integerConstant) {
        addIndented("mov rax, %d", integerConstant.getValue());
        push("rax");
    }

    @Override
    public void visit(final FalseConstant falseConstant) {
        addIndented("mov rax, 0");
        push("rax");
    }

    @Override
    public void visit(final TrueConstant trueConstant) {
        addIndented("mov rax, 1");
        push("rax");
    }

    public String getCode() {
        return code.toString();
    }
}
