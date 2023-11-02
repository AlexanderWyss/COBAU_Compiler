/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.statement;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;

import java.util.Objects;

public class DeclarationStatement extends Statement {
    private final Declaration declaration;

    public DeclarationStatement(Declaration declaration) {
        Objects.requireNonNull(declaration);
        this.declaration = declaration;
    }

    public Declaration getDeclaration() {
        return declaration;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        declaration.accept(astVisitor);
    }
}
