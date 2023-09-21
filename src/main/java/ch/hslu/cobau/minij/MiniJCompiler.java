package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.*;

import java.io.IOException;

public class MiniJCompiler {
    private static class EnhancedConsoleErrorListener extends ConsoleErrorListener {
        private boolean hasErrors;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
            hasErrors = true;
        }

        public boolean hasErrors() {
            return hasErrors;
        }
    }

    public static void main(String[] args) throws IOException {    
        // initialize lexer and parser
        CharStream charStream;
        if (args.length > 0) {
            charStream = CharStreams.fromFileName(args[0]);
        } else {
            charStream = CharStreams.fromStream(System.in);
        }
        
        MiniJLexer miniJLexer = new MiniJLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(miniJLexer);
        MiniJParser miniJParser = new MiniJParser(commonTokenStream);
        
        EnhancedConsoleErrorListener errorListener = new EnhancedConsoleErrorListener();
        miniJParser.removeErrorListeners();
        miniJParser.addErrorListener(errorListener);

        // start parsing at outermost level (milestone 2)
        MiniJParser.UnitContext unitContext = miniJParser.unit();

        // semantic check (milestone 3)
        // code generation (milestone 4)
        
        System.exit(errorListener.hasErrors() ? 1 : 0);
    }
}
