package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.CharStreams;

public class MiniJCompilerTest {

    private static final String CODE = """
            void myProc1(int a, int b, int c, int d, int e, int f, int g, int h) {
                   int sum;
                   sum = a + b + c + d + e + f + g + h;
                   writeInt(sum);
               }
               int main() {
                   myProc1(1, 1, 1, 1, 1, 1, 1, 1);
                   myProc1(1, 2, 3, 4, 5, 6, 7, 8);
                   return 0;
               }
            """;

    public static void main(final String[] args) {
        boolean isSuccessful = new MiniJCompiler().run(CharStreams.fromString(CODE));
        System.out.println("IsSuccessful: " + isSuccessful);
    }
}
