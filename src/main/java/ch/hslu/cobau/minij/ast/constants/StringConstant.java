/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.constants;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.type.StringType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

public class StringConstant extends Constant {
    private final String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        return new StringType();
    }
}
