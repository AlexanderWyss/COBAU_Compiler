package ch.hslu.cobau.minij;

import ch.hslu.cobau.minij.ast.AstBuilder;
import ch.hslu.cobau.minij.ast.entity.Unit;
import ch.hslu.cobau.minij.generation.CodeGenerator;
import ch.hslu.cobau.minij.symboltable.SymbolTable;
import ch.hslu.cobau.minij.symboltable.SymbolTableBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.PrintStream;

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
        System.exit(isSuccessful ? 0 : 1);
    }

    public boolean run(CharStream in) {
        return run(in, System.out);
    }

    /**
     * Runs the MinijCompiler
     *
     * @param in  the code
     * @param out the generated asm
     * @return true if it was successfully compiled, false otherwise
     */
    public boolean run(CharStream in, PrintStream out) {
        final MiniJLexer miniJLexer = new MiniJLexer(in);
        final CommonTokenStream commonTokenStream = new CommonTokenStream(miniJLexer);
        final MiniJParser miniJParser = new MiniJParser(commonTokenStream);

        final EnhancedConsoleErrorListener errorListener = new EnhancedConsoleErrorListener();
        miniJParser.removeErrorListeners();
        miniJParser.addErrorListener(errorListener);

        // start parsing at outermost level (milestone 2)
        final MiniJParser.UnitContext unitContext = miniJParser.unit();

        final AstBuilder astBuilder = new AstBuilder(errorListener);
        astBuilder.visitUnit(unitContext);
        final Unit unit = astBuilder.getUnit();

        // semantic check (milestone 3)
        if (!errorListener.hasErrors()) {
            final SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(errorListener);
            unit.accept(symbolTableBuilder);
            final SymbolTable symbolTable = symbolTableBuilder.getSymbolTable();

            if (!errorListener.hasErrors()) {
                final TypeChecker typeChecker = new TypeChecker(errorListener, symbolTable);
                unit.accept(typeChecker);
            }
            // code generation (milestone 4)
            if (!errorListener.hasErrors()) {
                final CodeGenerator codeGenerator = new CodeGenerator(symbolTable);
                unit.accept(codeGenerator);
                out.print(codeGenerator.getCode());
            }
        }


        return !errorListener.hasErrors();
    }
}
