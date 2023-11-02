/**
 * Copyright (c) 2020-2023 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
package ch.hslu.cobau.minij.ast;

import ch.hslu.cobau.minij.*;
import ch.hslu.cobau.minij.ast.constants.*;
import ch.hslu.cobau.minij.ast.entity.*;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.*;
import java.util.*;

/**
 * Builds the abstract syntax tree (AST) for MiniJ using a stack based approach.
 * After building the tree, fetch the generated AST using getUnit().
 */
public class AstBuilder extends MiniJBaseVisitor<Object> {
    private final Stack<Declaration> declarationStack = new Stack<>();
    private final Stack<Function> functionStack = new Stack<>();
    private final Stack<Struct> structStack = new Stack<>();
    private final Stack<Block> blockStack = new Stack<>();
    private final Stack<Statement> statementsStack = new Stack<>();
    private final Stack<Expression> expressionStack = new Stack<>();
    private final Stack<Type> typeStack = new Stack<>();
    private Unit unit;

    /**
     * @return The root of the generated MiniJ AST.
     */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public Object visitUnit(MiniJParser.UnitContext ctx) {
        super.visitChildren(ctx);
        unit = new Unit(getDeclarations(0), getFunctions(), getStructs());
        return null;
    }

    @Override
    public Object visitFunction(MiniJParser.FunctionContext ctx) {
        int declarationsCount = declarationStack.size();
        int statementsCount = statementsStack.size();
        super.visitChildren(ctx);
        Type type = ctx.type() != null ? typeStack.pop() : new VoidType();
        functionStack.add(new Function(ctx.identifier().ID().getText(), type, getDeclarations(declarationsCount), getStatements(statementsCount)));
        return null;
    }

    @Override
    public Object visitParameter(MiniJParser.ParameterContext ctx) {
        super.visitChildren(ctx);
        declarationStack.push(new Declaration(ctx.identifier().ID().getText(), typeStack.pop(), ctx.REF() != null));
        return null;
    }

    @Override
    public Object visitRecord(MiniJParser.RecordContext ctx) {
        int declarationsCount = declarationStack.size();
        super.visitChildren(ctx);
        structStack.push(new Struct(ctx.identifier().ID().getText(), getDeclarations(declarationsCount)));
        return null;
    }

    @Override
    public Object visitDeclarationStatement(MiniJParser.DeclarationStatementContext ctx) {
        super.visitChildren(ctx);
        statementsStack.push(new DeclarationStatement(declarationStack.pop()));
        return null;
    }

    @Override
    public Object visitCallStatement(MiniJParser.CallStatementContext ctx) {
        super.visitChildren(ctx);
        statementsStack.push(new CallStatement((CallExpression) expressionStack.pop()));
        return null;
    }

    @Override
    public Object visitDeclaration(MiniJParser.DeclarationContext ctx) {
        super.visitChildren(ctx);
        declarationStack.push(new Declaration(ctx.identifier().ID().getText(), typeStack.pop(), false));
        return null;
    }

    @Override
    public Object visitWhileStatement(MiniJParser.WhileStatementContext ctx) {
        int statementsCount = statementsStack.size();
        super.visitChildren(ctx);
        statementsStack.push(new WhileStatement(expressionStack.pop(), getStatements(statementsCount)));
        return null;
    }

    @Override
    public Object visitIfStatement(MiniJParser.IfStatementContext ctx) {
        int statementsCount = statementsStack.size();
        int blockCount = blockStack.size();
        super.visitChildren(ctx);

        Block elseBlock = null;
        while (blockStack.size() > blockCount) {
            if (elseBlock == null) {
                elseBlock = blockStack.pop();
            } else {
                IfStatement ifStatement = (IfStatement) blockStack.pop();
                // create new IfStatement to set new parent and elseBlock
                elseBlock = new IfStatement(ifStatement.getExpression(), ifStatement.getStatements(), elseBlock);
            }
        }

        statementsStack.push(new IfStatement(expressionStack.pop(), getStatements(statementsCount), elseBlock));
        return null;
    }

    @Override
    public Object visitElseClause(MiniJParser.ElseClauseContext ctx) {
        int statementsCount = statementsStack.size();
        super.visitChildren(ctx);
        blockStack.push(new Block(getStatements(statementsCount)));
        return null;
    }

    @Override
    public Object visitReturnStatement(MiniJParser.ReturnStatementContext ctx) {
        super.visitChildren(ctx);
        Expression expression = null;
        if (ctx.expression() != null) {
            expression = expressionStack.pop();
        }
        statementsStack.push(new ReturnStatement(expression));
        return null;
    }

    @Override
    public Object visitAssignment(MiniJParser.AssignmentContext ctx) {
        super.visitChildren(ctx);
        Expression rhs = expressionStack.pop();
        statementsStack.push(new AssignmentStatement(expressionStack.pop(), rhs));
        return null;
    }

    public Object visitExpression(MiniJParser.ExpressionContext ctx) {
        super.visitChildren(ctx);
        if (ctx.binaryOp != null) {
            Expression rhs = expressionStack.pop();
            expressionStack.push(new BinaryExpression(expressionStack.pop(), rhs, BinaryOperator.valueOf(MiniJParser.VOCABULARY.getSymbolicName(ctx.binaryOp.getType()))));
        } else if (ctx.INCREMENT() != null) {
            expressionStack.push(new UnaryExpression(expressionStack.pop(), UnaryOperator.POST_INCREMENT));
        } else if (ctx.DECREMENT() != null) {
            expressionStack.push(new UnaryExpression(expressionStack.pop(), UnaryOperator.POST_DECREMENT));
        }
        return null;
    }

    @Override
    public Object visitCall(MiniJParser.CallContext ctx) {
        int experessionCount = expressionStack.size();
        super.visitChildren(ctx);
        expressionStack.push(new CallExpression(ctx.identifier().ID().getText(), getExpressions(experessionCount)));
        return null;
    }

    @Override
    public Object visitUnaryExpression(MiniJParser.UnaryExpressionContext ctx) {
        super.visitChildren(ctx);

        String operator = MiniJParser.VOCABULARY.getSymbolicName(ctx.unaryOp.getType());
        if (operator.equals("INCREMENT") || operator.equals("DECREMENT")) {
            operator = "PRE_" + operator;
        }
        expressionStack.push(new UnaryExpression(expressionStack.pop(), UnaryOperator.valueOf(operator)));
        return null;
    }

    @Override
    public Object visitTrueConstant(MiniJParser.TrueConstantContext ctx) {
        expressionStack.push(new TrueConstant());
        return null;
    }

    @Override
    public Object visitFalseConstant(MiniJParser.FalseConstantContext ctx) {
        expressionStack.push(new FalseConstant());
        return null;
    }

    @Override
    public Object visitIntegerConstant(MiniJParser.IntegerConstantContext ctx) {
        long value;
        String inputValue = ctx.INTEGER().getText();
        value = Long.parseLong(inputValue);
        expressionStack.push(new IntegerConstant(value));

        return null;
    }

    @Override
    public Object visitStringConstant(MiniJParser.StringConstantContext ctx) {
        expressionStack.push(new StringConstant(ctx.STRINGCONSTANT().getText()));
        return null;
    }

    @Override
    public Object visitMemoryAccess(MiniJParser.MemoryAccessContext ctx) {
        super.visitChildren(ctx);

        if (ctx.DOT() != null) {
            expressionStack.push(new FieldAccess(expressionStack.pop(), ctx.ID().getText()));
        } else if (ctx.LBRACKET() != null) {
            Expression index = expressionStack.pop();
            expressionStack.push(new ArrayAccess((MemoryAccess) expressionStack.pop(), index));
        } else {
            expressionStack.push(new VariableAccess(ctx.ID().getText()));
        }
        return null;
    }

    @Override
    public Object visitIntegerType(MiniJParser.IntegerTypeContext ctx) {
        typeStack.push(new IntegerType());
        return null;
    }

    @Override
    public Object visitBooleanType(MiniJParser.BooleanTypeContext ctx) {
        typeStack.push(new BooleanType());
        return null;
    }

    @Override
    public Object visitStringType(MiniJParser.StringTypeContext ctx) {
        typeStack.push(new StringType());
        return null;
    }

    @Override
    public Object visitRecordType(MiniJParser.RecordTypeContext ctx) {
        typeStack.push(new RecordType(ctx.identifier().ID().getText()));
        return null;
    }

    @Override
    public Object visitType(MiniJParser.TypeContext ctx) {
        super.visitChildren(ctx);
        if (ctx.LBRACKET() != null) {
            typeStack.push(new ArrayType(typeStack.pop()));
        }
        return null;
    }

    private LinkedList<Function> getFunctions() {
        LinkedList<Function> procedures = new LinkedList<>();
        while (functionStack.size() > 0) {
            procedures.addFirst(functionStack.pop());
        }
        return procedures;
    }

    private LinkedList<Declaration> getDeclarations(int declarationCount) {
        LinkedList<Declaration> declarations = new LinkedList<>();
        while (declarationStack.size() > declarationCount) {
            declarations.addFirst(declarationStack.pop());
        }
        return declarations;
    }

    private LinkedList<Struct> getStructs() {
        LinkedList<Struct> recordStructures = new LinkedList<>();
        while (structStack.size() > 0) {
            recordStructures.addFirst(structStack.pop());
        }
        return recordStructures;
    }

    private LinkedList<Statement> getStatements(int statementsCount) {
        LinkedList<Statement> statements = new LinkedList<>();
        while (statementsStack.size() > statementsCount) {
            statements.addFirst(statementsStack.pop());
        }
        return statements;
    }

    private LinkedList<Expression> getExpressions(int expressionCount) {
        LinkedList<Expression> statements = new LinkedList<>();
        while (expressionStack.size() > expressionCount) {
            statements.addFirst(expressionStack.pop());
        }
        return statements;
    }
}
