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
import ch.hslu.cobau.minij.ast.expression.ArrayAccess;
import ch.hslu.cobau.minij.ast.expression.BinaryExpression;
import ch.hslu.cobau.minij.ast.expression.CallExpression;
import ch.hslu.cobau.minij.ast.expression.FieldAccess;
import ch.hslu.cobau.minij.ast.expression.UnaryExpression;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.statement.AssignmentStatement;
import ch.hslu.cobau.minij.ast.statement.Block;
import ch.hslu.cobau.minij.ast.statement.IfStatement;
import ch.hslu.cobau.minij.ast.statement.ReturnStatement;
import ch.hslu.cobau.minij.ast.statement.WhileStatement;

import java.util.*;

import static java.lang.String.format;

public class CodeGenerator extends BaseAstVisitor {
    private final StringBuilder code = new StringBuilder();
    private String currentScope = null;
    private final Map<String, Map<String, Integer>> scopes = new HashMap<>();
    private final Set<String> globals = new HashSet<>();
    private final Stack<String> expressions = new Stack<>();
    private final Deque<String> statements = new ArrayDeque<>();
    private int ifLabels = 0;
    private int whileLabels = 0;
    private int cmpLabels = 0;

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

    @Override
    public void visit(final Unit program) {
        program.visitDeclarations(this);
        code.append("""
                DEFAULT REL
                extern writeInt
                extern writeChar
                extern _exit
                extern readInt
                extern readChar
                section .data
                    ALIGN 8
                """);
        for (String global : globals) {
            code.append(format("    %s dq 0\n", global));
        }
        code.append("""
                section .text
                global _start
                        """);
        program.visitFunctions(this);
        program.visitStructs(this);
    }

    @Override
    public void visit(final Function procedure) {
        currentScope = procedure.getIdentifier();
        super.visit(procedure);
        if ("main".equals(procedure.getIdentifier())) {
            code.append("""
                    _start:
                    """);
        }
        code.append("""
                    push rbp ; save rbp of previous subroutine call on stack
                    mov rbp, rsp ; replace current base pointer with stack pointer
                """);

        int stackSize = scopes.size() * 8;
        stackSize += stackSize % 16; // align to 16 bytes
        code.append(format("    sub rsp, %d\n", stackSize));

        while (!statements.isEmpty()) {
            code.append(statements.poll());
        }

        if ("main".equals(procedure.getIdentifier())) {
            code.append("""
                    exit:
                        mov rdi, 0
                        call _exit
                     """);
        }
        code.append("""
                    mov rsp, rbp
                    pop rbp
                """);
    }

    @Override
    public void visit(final CallExpression callExpression) {
        super.visit(callExpression);
        statements.add(format("    mov rdi, %s\n", expressions.pop()));
        statements.add(format("    call %s\n", callExpression.getIdentifier()));
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
        statements.add(format("    mov rax, %s\n", expressions.pop()));
        statements.add("    cmp rax, 0\n");
        statements.add(format("    je %s\n", ifLabel));
        ifStatement.visitBlock(this);
        statements.add(format("    jmp %s\n", endIfLabel));
        statements.add(format("%s:\n", ifLabel));
        ifStatement.visitElse(this);
        statements.add(format("%s:\n", endIfLabel));
    }

    @Override
    public void visit(final WhileStatement whileStatement) {
        int whileCount = whileLabels++;
        String whileLabel = format("while%d", whileCount);
        String endWhileLabel = format("endWhile%d", whileCount);
        statements.add(format("    jmp %s\n", endWhileLabel));
        statements.add(format("%s:\n", whileLabel));
        whileStatement.visitBlock(this);
        statements.add(format("%s:\n", endWhileLabel));
        whileStatement.visitExpression(this);
        statements.add(format("    mov rax, %s\n", expressions.pop()));
        statements.add("    cmp rax, 1\n");
        statements.add(format("    je %s\n", whileLabel));
    }

    @Override
    public void visit(final AssignmentStatement assignment) {
        super.visit(assignment);
        String right = expressions.pop();
        String left = expressions.pop();
        statements.add(format("    mov qword %s, %s\n", left, right));
    }

    @Override
    public void visit(final BinaryExpression binaryExpression) {
        super.visit(binaryExpression);
        String right = expressions.pop();
        String left = expressions.pop();
        statements.add(format("    mov rax, %s\n", left));
        switch (binaryExpression.getBinaryOperator()) {
            case PLUS -> {
                statements.add(format("    add rax, %s\n", right));
            }
            case MINUS -> {
                statements.add(format("    sub rax, %s\n", right));
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
                statements.add(format("compare%d:\n", cmpLabels++));
                statements.add(format("    cmp rax, %s\n", right));
                switch (binaryExpression.getBinaryOperator()) {
                    case EQUAL -> {
                        statements.add("    je ._true\n");
                    }
                    case UNEQUAL -> {
                        statements.add("    jne ._true\n");
                    }
                    case LESSER -> {
                        statements.add("    jl ._true\n");
                    }
                    case LESSER_EQ -> {
                        statements.add("    jle ._true\n");
                    }
                    case GREATER -> {
                        statements.add("    jg ._true\n");
                    }
                    case GREATER_EQ -> {
                        statements.add("    jge ._true\n");
                    }
                }
                statements.add("""
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
    public void visit(final ReturnStatement returnStatement) {
        super.visit(returnStatement);
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
