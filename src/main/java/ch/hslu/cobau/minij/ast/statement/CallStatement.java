/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.statement;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.expression.CallExpression;

import java.util.Objects;

public class CallStatement extends Statement {
    private final CallExpression callExpression;

    public CallStatement(CallExpression callExpression) {
        Objects.requireNonNull(callExpression);
        this.callExpression = callExpression;
    }


    public CallExpression getCallExpression() {
        return callExpression;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        callExpression.accept(astVisitor);
    }
}
