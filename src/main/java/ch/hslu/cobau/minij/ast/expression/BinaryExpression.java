/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

import java.util.Objects;

public class BinaryExpression extends Expression {
    private final Expression left;
    private final Expression right;
    private final BinaryOperator binaryOperator;

    public BinaryExpression(Expression left, Expression right, BinaryOperator binaryOperator) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        Objects.requireNonNull(binaryOperator);

        this.left = left;
        this.right = right;
        this.binaryOperator = binaryOperator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinaryOperator getBinaryOperator() {
        return binaryOperator;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        left.accept(astVisitor);
        right.accept(astVisitor);
    }

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        return left.getResultType(symbolTable, scope);
    }
}
