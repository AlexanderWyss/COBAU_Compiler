package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.CharStreams;

import java.io.PrintStream;

public class MiniJCompilerTest {

    private static final String CODE = """
            int main() {
                     int a;
                     int res;
                     a = 10;
                     res = ++a;
                     writeInt(res);
                     writeInt(a);
                     return 0;
                 }
            """;

    public static void main(final String[] args) {
        boolean isSuccessful = new MiniJCompiler().run(CharStreams.fromString(CODE), new PrintStreamLineEnumerator(System.out, true));
        System.out.println("IsSuccessful: " + isSuccessful);
    }

    public static class PrintStreamLineEnumerator extends PrintStream {
        private final boolean addLines;

        public PrintStreamLineEnumerator(PrintStream out, boolean addLines) {
            super(out);
            this.addLines = addLines;
        }

        @Override
        public void print(String s) {
            if (addLines) {
                String[] lines = s.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    super.print(String.format("%d. %s\n", i + 1, lines[i]));
                }
            } else {
                super.print(s);
            }
        }
    }
}
