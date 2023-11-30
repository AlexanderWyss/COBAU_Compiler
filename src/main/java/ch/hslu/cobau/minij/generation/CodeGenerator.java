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
import ch.hslu.cobau.minij.ast.statement.CallStatement;
import ch.hslu.cobau.minij.ast.statement.DeclarationStatement;
import ch.hslu.cobau.minij.ast.statement.IfStatement;
import ch.hslu.cobau.minij.ast.statement.ReturnStatement;
import ch.hslu.cobau.minij.ast.statement.WhileStatement;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static java.lang.String.format;

public class CodeGenerator extends BaseAstVisitor {
    private final StringBuilder code = new StringBuilder();
    private final Map<String, Integer> localsMap = new HashMap<>();
    private final Stack<String> expressions = new Stack<>();
    private final Deque<String> statements = new ArrayDeque<>();
    private int ifLabels = 0;

    private void addLocal(String identifier) {
        int position = (localsMap.size() + 1);
        if (!localsMap.containsKey(identifier)) {
            localsMap.put(identifier, position);
        }
    }

    @Override
    public void visit(final Unit program) {
        code.append("""
                            DEFAULT REL
                            extern writeInt
                            extern writeChar
                            extern _exit
                            extern readInt
                            extern readChar
                            section .data
                            section .text
                            global _start
                                    """);
        super.visit(program);
    }

    @Override
    public void visit(final Function procedure) {
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

        int stackSize = localsMap.size() * 8;
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
        super.visit(declaration);
        addLocal(declaration.getIdentifier());
    }

    @Override
    public void visit(final VariableAccess variable) {
        super.visit(variable);
        expressions.push(format("[rbp-%d]", localsMap.get(variable.getIdentifier()) * 8));
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
    public void visit(final AssignmentStatement assignment) {
        super.visit(assignment);
        String right = expressions.pop();
        String left = expressions.pop();
        statements.add(format("    mov qword %s, %s\n", left, right));
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
    public void visit(final DeclarationStatement declarationStatement) {
        super.visit(declarationStatement);
    }

    @Override
    public void visit(final CallStatement callStatement) {
        super.visit(callStatement);
    }

    @Override
    public void visit(final WhileStatement whileStatement) {
        super.visit(whileStatement);
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
    public void visit(final BinaryExpression binaryExpression) {
        super.visit(binaryExpression);
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
