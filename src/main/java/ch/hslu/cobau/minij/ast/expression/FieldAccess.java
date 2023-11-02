/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.Objects;

public class FieldAccess extends MemoryAccess {
    private final Expression base;
    private final String field;

    public FieldAccess(Expression base, String field) {
        Objects.requireNonNull(base);
        Objects.requireNonNull(field);

        this.base = base;
        this.field = field;
    }

    public Expression getBase() {
        return base;
    }

    public String getField() {
        return field;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        base.accept(astVisitor);
    }
}
