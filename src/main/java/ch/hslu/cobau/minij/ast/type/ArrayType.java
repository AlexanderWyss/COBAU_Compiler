/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.type;

import java.util.Objects;

public class ArrayType extends Type {
    private final Type type;

    public ArrayType(Type type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ArrayType arrayType = (ArrayType) other;
        return Objects.equals(type, arrayType.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type.toString() + "[]";
    }
}
