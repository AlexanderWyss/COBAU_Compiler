/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.entity;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.statement.Block;
import ch.hslu.cobau.minij.ast.statement.Statement;
import ch.hslu.cobau.minij.ast.type.Type;

import java.util.List;
import java.util.Objects;

public class Function extends Block {
    private final String identifier;
	private final Type returnType;
    private final List<Declaration> formalParameters;

    public Function(String identifier, Type returnType, List<Declaration> formalParameters, List<Statement> statements) {
        super(statements);
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(returnType);
        Objects.requireNonNull(formalParameters);

        this.identifier = identifier;
        this.returnType = returnType;
        this.formalParameters = formalParameters;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Declaration> getFormalParameters() {
        return formalParameters;
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        formalParameters.forEach(parameter -> parameter.accept(astVisitor));
        super.visitChildren(astVisitor); // statements
    }
}
