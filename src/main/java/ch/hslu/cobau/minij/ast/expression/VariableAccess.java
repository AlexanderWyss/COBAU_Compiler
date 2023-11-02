/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.Objects;

public class VariableAccess extends MemoryAccess {
    private final String identifier;

    public VariableAccess(String identifier) {
        Objects.requireNonNull(identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
