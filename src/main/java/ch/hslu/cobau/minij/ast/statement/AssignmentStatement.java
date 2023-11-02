/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.statement;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.expression.Expression;

import java.util.Objects;

public class AssignmentStatement extends Statement {
    private final Expression left;
    private final Expression right;

    public AssignmentStatement(Expression left, Expression right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
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
}
