/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast;

import ch.hslu.cobau.minij.ast.constants.*;
import ch.hslu.cobau.minij.ast.entity.*;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;

public interface AstVisitor {
    void visit(Unit program);
    void visit(Function function);
    void visit(Declaration declaration);
    void visit(Struct record);

    void visit(IfStatement ifStatement);
    void visit(WhileStatement whileStatement);
    void visit(ReturnStatement returnStatement);
    void visit(AssignmentStatement assignment);
    void visit(DeclarationStatement declarationStatement);
    void visit(CallStatement callStatement);
    void visit(Block block);

    void visit(UnaryExpression unaryExpression);
    void visit(BinaryExpression binaryExpression);
    void visit(CallExpression callExpression);

    void visit(VariableAccess variable);
    void visit(ArrayAccess arrayAccess);
    void visit(FieldAccess fieldAccess);

    void visit(FalseConstant falseConstant);
    void visit(IntegerConstant integerConstant);
    void visit(StringConstant stringConstant);
    void visit(TrueConstant trueConstant);
}
