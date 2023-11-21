package ch.hslu.cobau.minij.symboltable;

import ch.hslu.cobau.minij.EnhancedConsoleErrorListener;
import ch.hslu.cobau.minij.ast.BaseAstVisitor;
import ch.hslu.cobau.minij.ast.constants.FalseConstant;
import ch.hslu.cobau.minij.ast.constants.IntegerConstant;
import ch.hslu.cobau.minij.ast.constants.StringConstant;
import ch.hslu.cobau.minij.ast.constants.TrueConstant;
import ch.hslu.cobau.minij.ast.entity.Declaration;
import ch.hslu.cobau.minij.ast.entity.Function;
import ch.hslu.cobau.minij.ast.entity.Struct;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.ast.expression.ArrayAccess;
import ch.hslu.cobau.minij.ast.expression.BinaryExpression;
import ch.hslu.cobau.minij.ast.expression.CallExpression;
import ch.hslu.cobau.minij.ast.expression.FieldAccess;
import ch.hslu.cobau.minij.ast.expression.UnaryExpression;
import ch.hslu.cobau.minij.ast.expression.VariableAccess;
import ch.hslu.cobau.minij.ast.statement.AssignmentStatement;
import ch.hslu.cobau.minij.ast.statement.Block;
import ch.hslu.cobau.minij.ast.statement.CallStatement;
import ch.hslu.cobau.minij.ast.statement.DeclarationStatement;
import ch.hslu.cobau.minij.ast.statement.IfStatement;
import ch.hslu.cobau.minij.ast.statement.ReturnStatement;
import ch.hslu.cobau.minij.ast.statement.WhileStatement;

public class SymbolTableBuilder extends BaseAstVisitor {
    private final EnhancedConsoleErrorListener errorListener;
    private final SymbolTable symbolTable;
    private Scope currentScope;

    public SymbolTableBuilder(final EnhancedConsoleErrorListener errorListener) {
        this.errorListener = errorListener;
        this.symbolTable = new SymbolTable();
        this.currentScope = this.symbolTable.getGlobal();

    }

    @Override
    public void visit(final Unit program) {
        super.visit(program);
    }

    @Override
    public void visit(final Function procedure) {
        if (symbolTable.addFunction(procedure)) {
            currentScope = symbolTable.addScope(procedure.getIdentifier(), this.currentScope);
            super.visit(procedure);
            currentScope = currentScope.getParent();
        } else {
            errorListener.semanticError("Duplicate function '%s'.", procedure.getIdentifier());
        }
    }

    @Override
    public void visit(final Struct recordStructure) {
        if (symbolTable.addRecordType(recordStructure)) {
            currentScope = new Scope(recordStructure.getIdentifier(), currentScope); // Don't add scope to symbol table
            super.visit(recordStructure);
            currentScope = currentScope.getParent();
        } else {
            errorListener.semanticError("Duplicate record '%s'.", recordStructure.getIdentifier());
        }
    }

    @Override
    public void visit(final Declaration declaration) {
        if (currentScope.addSymbol(declaration.getIdentifier(), declaration)) {
            super.visit(declaration);
        } else {
            errorListener.semanticError("Duplicate declaration '%s' in scope '%s'.", declaration.getIdentifier(), currentScope.getIdentifier());
        }
    }

    @Override
    public void visit(final ReturnStatement returnStatement) {
        super.visit(returnStatement);
    }

    @Override
    public void visit(final AssignmentStatement assignment) {
        super.visit(assignment);
    }

    @Override
    public void visit(final DeclarationStatement declarationStatement) {
        super.visit(declarationStatement);
    }

    @Override
    public void visit(final CallStatement callStatement) {
        super.visit(callStatement);
    }

    @Override
    public void visit(final IfStatement ifStatement) {
        super.visit(ifStatement);
    }

    @Override
    public void visit(final WhileStatement whileStatement) {
        super.visit(whileStatement);
    }

    @Override
    public void visit(final Block block) {
        super.visit(block);
    }

    @Override
    public void visit(final UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
    }

    @Override
    public void visit(final BinaryExpression binaryExpression) {
        super.visit(binaryExpression);
    }

    @Override
    public void visit(final CallExpression callExpression) {
        super.visit(callExpression);
    }

    @Override
    public void visit(final VariableAccess variable) {
        super.visit(variable);
    }

    @Override
    public void visit(final ArrayAccess arrayAccess) {
        super.visit(arrayAccess);
    }

    @Override
    public void visit(final FieldAccess fieldAccess) {
        super.visit(fieldAccess);
    }

    @Override
    public void visit(final FalseConstant falseConstant) {
        super.visit(falseConstant);
    }

    @Override
    public void visit(final IntegerConstant integerConstant) {
        super.visit(integerConstant);
    }

    @Override
    public void visit(final StringConstant stringConstant) {
        super.visit(stringConstant);
    }

    @Override
    public void visit(final TrueConstant trueConstant) {
        super.visit(trueConstant);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
