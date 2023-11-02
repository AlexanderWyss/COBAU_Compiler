/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;

import java.util.List;
import java.util.Objects;

public class CallExpression extends Expression {
    private final String identifier;
    private final List<Expression> actualParameters;

    public CallExpression(String identifier, List<Expression> actualParameters) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(actualParameters);

        this.identifier = identifier;
        this.actualParameters = actualParameters;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Expression> getParameters() {
        return actualParameters;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        actualParameters.forEach(actualParameter -> actualParameter.accept(astVisitor));
    }
}
