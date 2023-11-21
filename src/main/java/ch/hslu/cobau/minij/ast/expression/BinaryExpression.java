/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast.expression;

import ch.hslu.cobau.minij.ast.AstVisitor;
import ch.hslu.cobau.minij.ast.type.BooleanType;
import ch.hslu.cobau.minij.ast.type.IntegerType;
import ch.hslu.cobau.minij.ast.type.StringType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

import java.util.*;

public class BinaryExpression extends Expression {

    private static final Map<BinaryOperator, Set<Type>> VALID_TYPES = new EnumMap<>(BinaryOperator.class);

    {
        VALID_TYPES.put(BinaryOperator.PLUS, Set.of(new IntegerType(), new StringType()));
        Set<Type> mathSet = Set.of(new IntegerType());
        VALID_TYPES.put(BinaryOperator.MINUS, mathSet);
        VALID_TYPES.put(BinaryOperator.TIMES, mathSet);
        VALID_TYPES.put(BinaryOperator.DIV, mathSet);
        VALID_TYPES.put(BinaryOperator.MOD, mathSet);
        Set<Type> equalSet = Set.of(new IntegerType(), new StringType(), new BooleanType());
        VALID_TYPES.put(BinaryOperator.EQUAL, equalSet);
        VALID_TYPES.put(BinaryOperator.UNEQUAL, equalSet);
        Set<Type> compareSet = Set.of(new IntegerType(), new StringType());
        VALID_TYPES.put(BinaryOperator.LESSER, compareSet);
        VALID_TYPES.put(BinaryOperator.LESSER_EQ, compareSet);
        VALID_TYPES.put(BinaryOperator.GREATER, compareSet);
        VALID_TYPES.put(BinaryOperator.GREATER_EQ, compareSet);
        Set<Type> boolSet = Set.of(new BooleanType());
        VALID_TYPES.put(BinaryOperator.AND, boolSet);
        VALID_TYPES.put(BinaryOperator.OR, boolSet);
    }

    private final Expression left;
    private final Expression right;
    private final BinaryOperator binaryOperator;

    public BinaryExpression(Expression left, Expression right, BinaryOperator binaryOperator) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        Objects.requireNonNull(binaryOperator);

        this.left = left;
        this.right = right;
        this.binaryOperator = binaryOperator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinaryOperator getBinaryOperator() {
        return binaryOperator;
    }

    @Override
    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

    @Override
    public void visitChildren(AstVisitor astVisitor) {
        left.accept(astVisitor);
        right.accept(astVisitor);
    }

    @Override
    public Type getResultType(SymbolTable symbolTable, Scope scope) {
        return switch (binaryOperator) {
            case PLUS -> left.getResultType(symbolTable, scope);
            case MINUS, TIMES, DIV, MOD -> new IntegerType();
            case EQUAL, UNEQUAL, LESSER, LESSER_EQ, GREATER, GREATER_EQ, AND, OR -> new BooleanType();
        };
    }

    public Set<Type> getValidExpressionTypes() {
        return VALID_TYPES.get(binaryOperator);
    }
}
