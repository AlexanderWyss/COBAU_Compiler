/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.statement;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.expression.Expression;

import java.util.List;
import java.util.Objects;

public class WhileStatement extends Block {
    private final Expression expression;

    public WhileStatement(Expression expression, List<Statement> statements) {
        super(statements);
        Objects.requireNonNull(expression);

        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        expression.accept(astVisitor);
        super.visitChildren(astVisitor);
    }
}
