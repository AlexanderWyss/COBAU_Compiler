package ch.hslu.cobau.minij;

import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.StringConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.expression.*;
import ch.hslu.cobau.minij.ast.statement.*;
import ch.hslu.cobau.minij.ast.type.BooleanType;
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.ast.type.VoidType;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

import java.util.Set;

/**
 * Disclaimer:
 * While developing we realised that the expression.getResultType(...) is a bit wrong and would better be solved with putting it on a stack
 * and read the stack in the visitor.
 * <p>
 * Sadly that realization came too late and we are too far gone now. :D
 */
public class TypeChecker extends BaseAstVisitor {
    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private Scope currentScope;

    public TypeChecker(EnhancedConsoleErrorListener errorListener, SymbolTable symbolTable) {
        this.errorListener = errorListener;
        this.symbolTable = symbolTable;
        currentScope = symbolTable.getGlobal();
    }

    @Override
    public void visit(final Function procedure) {
        currentScope = symbolTable.getScope(procedure.getIdentifier());
        super.visit(procedure);
        currentScope = currentScope.getParent();
    }

    @Override
    public void visit(VariableAccess variable) {
        if (currentScope.hasSymbol(variable.getIdentifier())) {
            super.visit(variable);
        } else {
            errorListener.semanticError("Variable '%s' not declared.", variable.getIdentifier());
        }
    }

    @Override
    public void visit(final Declaration declaration) {
        if (declaration.getType() instanceof RecordType) {
            String recordIdentifier = ((RecordType) declaration.getType()).getIdentifier();
            if (symbolTable.hasRecordType(recordIdentifier)) {
                super.visit(declaration);
            } else {
                errorListener.semanticError("Type '%s' does not exist.", recordIdentifier);
            }
        }
    }

    @Override
    public void visit(FieldAccess fieldAccess) {
        Type recordType = fieldAccess.getBase().getResultType(symbolTable, currentScope);
        if (recordType instanceof RecordType) {
            String recordTypeIdentifier = ((RecordType) recordType).getIdentifier();
            if (symbolTable.getRecordType(recordTypeIdentifier).getDeclarations().stream().map(Declaration::getIdentifier)
                    .anyMatch(field -> fieldAccess.getField().equals(field))) {
                super.visit(fieldAccess);
            } else {
                errorListener.semanticError("Type '%s' does not have a field '%s'.", recordTypeIdentifier, fieldAccess.getField());
            }
        } else {
            errorListener.semanticError("'%s' is not a record.", recordType);
        }
    }

    @Override
    public void visit(CallExpression call) {
        if (!symbolTable.hasFunction(call.getIdentifier())) {
            errorListener.semanticError("Function '%s' does not exist.", call.getIdentifier());
            return;
        }
        Function function = symbolTable.getFunction(call.getIdentifier());
        if (function.getFormalParameters().size() != call.getParameters().size()) {
            errorListener.semanticError("Function call %s has an invalid number of parameters. Expected: %d, Actual: %d.", call.getIdentifier(), function.getFormalParameters().size(), call.getParameters().size());
            return;
        }
        for (int i = 0; i < function.getFormalParameters().size(); i++) {
            Declaration parameterDeclaration = function.getFormalParameters().get(i);
            Expression value = call.getParameters().get(i);
            if (!parameterDeclaration.getType().equals(value.getResultType(symbolTable, currentScope))) {
                errorListener.semanticError("Parameter %s of function call %s has an invalid type. Expected: %s, Actual: %s.", parameterDeclaration.getIdentifier(), call.getIdentifier(), parameterDeclaration.getType(), value.getResultType(symbolTable, currentScope));
            }
        }
        super.visit(call);
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        if (currentScope.equals(symbolTable.getGlobal())) {
            errorListener.semanticError("Return outside of function.");
            return;
        }
        Type expectedReturnType = symbolTable.getFunction(currentScope.getIdentifier()).getReturnType();
        Type actualReturnType = returnStatement.getExpression() == null ? new VoidType() : returnStatement.getExpression().getResultType(symbolTable, currentScope);
        if (!expectedReturnType.equals(actualReturnType)) {
            errorListener.semanticError("Function '%s' should return '%s', instead got '%s'", currentScope.getIdentifier(), expectedReturnType, actualReturnType);
            return;
        }
        super.visit(returnStatement);
    }

    @Override
    public void visit(AssignmentStatement assignment) {
        Type leftType = assignment.getLeft().getResultType(symbolTable, currentScope);
        Type rightType = assignment.getRight().getResultType(symbolTable, currentScope);
        if (leftType != null && rightType != null && !leftType.equals(rightType)) {
            errorListener.semanticError("'%s' cannot be assigned to '%s'", rightType, leftType);
            return;
        }
        super.visit(assignment);
    }

    @Override
    public void visit(IfStatement ifStatement) {
        Type type = ifStatement.getExpression().getResultType(symbolTable, currentScope);
        if (type != null && !type.equals(new BooleanType())) {
            errorListener.semanticError("if expression '%s' must be a '%s'.", type, new BooleanType());
            return;
        }
        super.visit(ifStatement);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        Type type = whileStatement.getExpression().getResultType(symbolTable, currentScope);
        if (type != null && !type.equals(new BooleanType())) {
            errorListener.semanticError("while expression '%s' must be a '%s'.", type, new BooleanType());
            return;
        }
        super.visit(whileStatement);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        Type leftResultType = binaryExpression.getLeft().getResultType(symbolTable, currentScope);
        Type rightResultType = binaryExpression.getLeft().getResultType(symbolTable, currentScope);
        if (leftResultType != null && rightResultType != null) {
            if (!leftResultType.equals(rightResultType)) {
                errorListener.semanticError("Left and right side of '%s' expression must be the same. Actual: '%s' and '%s'.",
                        binaryExpression.getBinaryOperator(), leftResultType, rightResultType);
                return;
            }
            Set<Type> validExpressionTypes = binaryExpression.getValidExpressionTypes();
            if (!validExpressionTypes.contains(leftResultType)) {
                errorListener.semanticError("'%s' expression supports '%s'. Actual: '%s'.",
                        binaryExpression.getBinaryOperator(), validExpressionTypes, leftResultType);
                return;
            }
        }
        super.visit(binaryExpression);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        Type resultType = unaryExpression.getExpression().getResultType(symbolTable, currentScope);
        if (resultType != null) {
            Type validExpressionType = unaryExpression.getValidExpressionType();
            if (!resultType.equals(validExpressionType)) {
                errorListener.semanticError("'%s' expression supports '%s'. Actual: '%s'.",
                        unaryExpression.getUnaryOperator(), validExpressionType, resultType);
                return;
            }
        }
        super.visit(unaryExpression);
    }
}
