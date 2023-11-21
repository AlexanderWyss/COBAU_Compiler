/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

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

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        Declaration symbol = scope.getSymbol(identifier);
        return symbol == null ? null : symbol.getType();
    }
}
