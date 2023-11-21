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

public class UnaryExpression extends Expression {
    private final Expression expression;
    private final UnaryOperator unaryOperator;

    public UnaryExpression(Expression expression, UnaryOperator unaryOperator) {
        Objects.requireNonNull(expression);
        Objects.requireNonNull(unaryOperator);

        this.expression = expression;
        this.unaryOperator = unaryOperator;
    }

    public Expression getExpression() {
        return expression;
    }

    public UnaryOperator getUnaryOperator() {
        return unaryOperator;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        expression.accept(astVisitor);
    }

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        return expression.getResultType(symbolTable, scope);
    }
}
