/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.type.Type;

public abstract class MemoryAccess extends Expression {
    private Type type;

    public void setType(Type type) {
        this.type = type;
    }
    public Type getType() {
        return type;
    }
}
