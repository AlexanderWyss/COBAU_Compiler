/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

import java.util.Objects;
import java.util.Optional;

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

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        Type recordType = base.getResultType(symbolTable, scope);
        if (recordType instanceof RecordType) {
            Optional<Declaration> fieldDeclaration = symbolTable.getRecordType(((RecordType) recordType).getIdentifier()).getDeclarations().stream()
                    .filter(declaration -> declaration.getIdentifier().equals(field)).findFirst();
            if (fieldDeclaration.isPresent()) {
                return fieldDeclaration.get().getType();
            }
        }
        return null;
    }
}
