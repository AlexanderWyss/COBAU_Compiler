/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.entity;

import ch.hslu.cobau.minij.ast.AstElement;
import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

public class Struct extends AstElement {
    private final String identifier;
    private final List<Declaration> declarations;

    public Struct(String identifier, List<Declaration> declarations) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(declarations);

        this.identifier = identifier;
        this.declarations = declarations;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getIndex(String identifier) {
        int index = 0;
        for(Declaration declaration : getDeclarations()) {
            if (declaration.getIdentifier().equals(identifier)) {
                return index;
            }
            ++index;
        }
        throw new RuntimeException("identifier not found");
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        declarations.forEach(declaration -> declaration.accept(astVisitor));
    }

    public int getElementCount() {
        return declarations.size();
    }
}
