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
import ch.hslu.cobau.minij.ast.type.RecordType;
import ch.hslu.cobau.minij.ast.type.Type;
import ch.hslu.cobau.minij.symboltable.Scope;
import ch.hslu.cobau.minij.symboltable.SymbolTable;

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
            errorListener.semanticError("Undeclared Variable '%s' accessed.", variable.getIdentifier());
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
        if (symbolTable.hasFunction(call.getIdentifier())) {
            super.visit(call);
        } else {
            errorListener.semanticError("Function '%s' does not exist.", call.getIdentifier());
        }
    }
}
