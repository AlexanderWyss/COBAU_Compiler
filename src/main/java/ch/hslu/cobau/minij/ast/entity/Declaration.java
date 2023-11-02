/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.entity;

import ch.hslu.cobau.minij.ast.AstElement;
import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.type.Type;

import java.util.Objects;

public class Declaration extends AstElement {
    private final String identifier;
    private Type type;
    private final boolean isReference;

    public Declaration(String identifier, Type type, boolean isReference) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(type);

        this.identifier = identifier;
        this.type = type;
        this.isReference = isReference;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isReference() {
        return isReference;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
