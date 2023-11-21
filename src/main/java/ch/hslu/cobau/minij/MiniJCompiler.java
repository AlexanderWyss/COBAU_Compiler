package ch.hslu.cobau.minij;

import ch.hslu.cobau.minij.ast.AstBuilder;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.symboltable.SymbolTable;
import ch.hslu.cobau.minij.symboltable.SymbolTableBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class MiniJCompiler {

    public static void main(String[] args) throws IOException {
        // initialize lexer and parser
        CharStream charStream;
        if (args.length > 0) {
            charStream = CharStreams.fromFileName(args[0]);
        } else {
            charStream = CharStreams.fromStream(System.in);
        }
        boolean isSuccessful = new MiniJCompiler().run(charStream);
        System.exit(isSuccessful ? 1 : 0);
    }

    /**
     * Runs the MinijCompiler
     *
     * @param charStream the code
     * @return true if it was successfully compiled, false otherwise
     */
    public boolean run(CharStream charStream) {
        final MiniJLexer miniJLexer = new MiniJLexer(charStream);
        final CommonTokenStream commonTokenStream = new CommonTokenStream(miniJLexer);
        final MiniJParser miniJParser = new MiniJParser(commonTokenStream);

        final EnhancedConsoleErrorListener errorListener = new EnhancedConsoleErrorListener();
        miniJParser.removeErrorListeners();
        miniJParser.addErrorListener(errorListener);

        // start parsing at outermost level (milestone 2)
        final MiniJParser.UnitContext unitContext = miniJParser.unit();

        final AstBuilder astBuilder = new AstBuilder();
        astBuilder.visitUnit(unitContext);
        final Unit unit = astBuilder.getUnit();

        // semantic check (milestone 3)
        final SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(errorListener);
        unit.accept(symbolTableBuilder);
        final SymbolTable symbolTable = symbolTableBuilder.getSymbolTable();

        if (!errorListener.hasErrors()) {
            final TypeChecker typeChecker = new TypeChecker(errorListener, symbolTable);
            unit.accept(typeChecker);
        }

        // code generation (milestone 4)

        return errorListener.hasErrors();
    }
}
