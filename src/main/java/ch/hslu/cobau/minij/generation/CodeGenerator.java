package ch.hslu.cobau.minij.generation;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.StringConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;

import java.util.*;

import static java.lang.String.format;

public class CodeGenerator extends BaseAstVisitor {
    private final StringBuilder code = new StringBuilder();
    private String currentScope = null;
    private final Map<String, Map<String, Integer>> scopes = new HashMap<>();
    private final Set<String> globals = new HashSet<>();
    private final Deque<String> expressions = new LinkedList<>();
    private final Deque<String> statements = new ArrayDeque<>();
    private int ifLabels = 0;
    private int whileLabels = 0;
    private int cmpLabels = 0;

    private static final List<String> PARAM_REGISTERS = List.of("RDI", "RSI", "RDX", "RCX", "R8", "R9");

    private Map<String, Integer> getLocals() {
        return scopes.computeIfAbsent(currentScope, ident -> new HashMap<>());
    }

    private void addVariable(String identifier) {
        if (currentScope != null) {
            final Map<String, Integer> locals = getLocals();
            int position = (locals.size() + 1);
            if (!locals.containsKey(identifier)) {
                locals.put(identifier, position);
            }
        } else {
            globals.add(identifier);
        }
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
        program.visitStructs(this);
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

        int stackSize = scopes.size() * 8;
        stackSize += stackSize % 16; // align to 16 bytes
        code.append(formatIndented("sub rsp, %d", stackSize));

        for (int i = 0; i < procedure.getFormalParameters().size(); i++) {
            if (i < PARAM_REGISTERS.size()) {
                code.append(formatIndented("mov %s, %s", getVariable(procedure.getFormalParameters().get(i).getIdentifier()), PARAM_REGISTERS.get(i)));
            } else {
                code.append(formatIndented("mov rax, [rbp+%d]", (i - PARAM_REGISTERS.size()) * 8 + 16));
                code.append(formatIndented("mov %s, rax", getVariable(procedure.getFormalParameters().get(i).getIdentifier())));
            }
        }
        // TODO init local vars 0

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
        if (!expressions.isEmpty()) {
            addIndented("mov rax, %s", expressions.pop());
        }
        addIndented("jmp _%sEnd", currentScope);
    }


    @Override
    public void visit(final CallExpression callExpression) {
        super.visit(callExpression);
        for (int i = 0; i < callExpression.getParameters().size(); i++) {
            if (i < PARAM_REGISTERS.size()) {
                addIndented("mov %s, %s", PARAM_REGISTERS.get(i), expressions.removeLast());
            } else {
                addIndented("""
                        mov rax, %s
                        push rax
                        """, expressions.removeLast());
            }
        }
        addIndented("call %s", callExpression.getIdentifier());
        for (int i = 0; i < callExpression.getParameters().size() - PARAM_REGISTERS.size(); i++) {
            addIndented("pop rdi");
        }
        expressions.push("rax");
    }

    @Override
    public void visit(CallStatement callStatement) {
        super.visit(callStatement);
        expressions.pop(); // unused return value from CallExpression
    }

    @Override
    public void visit(DeclarationStatement declarationStatement) {
        super.visit(declarationStatement);
        // TODO maybe use this for declarations because of structs
    }


    @Override
    public void visit(final Declaration declaration) {
        addVariable(declaration.getIdentifier());
    }

    @Override
    public void visit(final VariableAccess variable) {
        expressions.push(getVariable(variable.getIdentifier()));
    }

    @Override
    public void visit(final IfStatement ifStatement) {
        ifStatement.visitExpression(this);
        int ifCount = ifLabels++;
        String ifLabel = format("if%d", ifCount);
        String endIfLabel = format("endIf%d", ifCount);
        addIndented("mov rax, %s", expressions.pop());
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
        String whileLabel = format("while%d", whileCount);
        String endWhileLabel = format("endWhile%d", whileCount);
        addIndented("jmp %s", endWhileLabel);
        add("%s:", whileLabel);
        whileStatement.visitBlock(this);
        add("%s:", endWhileLabel);
        whileStatement.visitExpression(this);
        addIndented("mov rax, %s", expressions.pop());
        addIndented("cmp rax, 1");
        addIndented("je %s", whileLabel);
    }

    @Override
    public void visit(final AssignmentStatement assignment) {
        super.visit(assignment);
        String right = expressions.pop();
        String left = expressions.pop();
        addIndented("mov qword %s, %s", left, right);
    }

    @Override
    public void visit(final BinaryExpression binaryExpression) {
        super.visit(binaryExpression);
        String right = expressions.pop();
        String left = expressions.pop();
        addIndented("mov rax, %s", left);
        switch (binaryExpression.getBinaryOperator()) {
            case PLUS -> {
                addIndented("add rax, %s", right);
            }
            case MINUS -> {
                addIndented("sub rax, %s", right);
            }
            case TIMES -> {
                throw new UnsupportedOperationException();
            }
            case DIV -> {
                throw new UnsupportedOperationException();
            }
            case MOD -> {
                throw new UnsupportedOperationException();
            }
            case EQUAL, UNEQUAL, LESSER, LESSER_EQ, GREATER, GREATER_EQ -> {
                add("compare%d:", cmpLabels++);
                addIndented("cmp rax, %s", right);
                switch (binaryExpression.getBinaryOperator()) {
                    case EQUAL -> {
                        addIndented("je ._true");
                    }
                    case UNEQUAL -> {
                        addIndented("jne ._true");
                    }
                    case LESSER -> {
                        addIndented("jl ._true");
                    }
                    case LESSER_EQ -> {
                        addIndented("jle ._true");
                    }
                    case GREATER -> {
                        addIndented("jg ._true");
                    }
                    case GREATER_EQ -> {
                        addIndented("jge ._true");
                    }
                }
                add("""
                            jmp ._false
                        ._true:
                            mov rax, 1
                            jmp ._boolEnd
                        ._false:
                            mov rax, 0
                        ._boolEnd:
                        """);
            }
            case AND -> {
                throw new UnsupportedOperationException();
            }
            case OR -> {
                throw new UnsupportedOperationException();
            }
        }
        expressions.push("rax");
    }

    @Override
    public void visit(final IntegerConstant integerConstant) {
        super.visit(integerConstant);
        expressions.push(format("%d", integerConstant.getValue()));
    }

    @Override
    public void visit(final FalseConstant falseConstant) {
        super.visit(falseConstant);
        expressions.push("0");
    }

    @Override
    public void visit(final TrueConstant trueConstant) {
        super.visit(trueConstant);
        expressions.push("1");
    }

    @Override
    public void visit(final Struct recordStructure) {
        super.visit(recordStructure);
    }

    @Override
    public void visit(final Block block) {
        super.visit(block);
    }

    @Override
    public void visit(final UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
    }

    @Override
    public void visit(final ArrayAccess arrayAccess) {
        super.visit(arrayAccess);
    }

    @Override
    public void visit(final FieldAccess fieldAccess) {
        super.visit(fieldAccess);
    }

    @Override
    public void visit(final StringConstant stringConstant) {
        super.visit(stringConstant);
    }

    public String getCode() {
        return code.toString();
    }
}
