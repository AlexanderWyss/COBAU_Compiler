/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.statement;

import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

public class Block extends Statement {
    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        Objects.requireNonNull(statements);
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        statements.forEach(statement -> statement.accept(astVisitor));
    }
}
