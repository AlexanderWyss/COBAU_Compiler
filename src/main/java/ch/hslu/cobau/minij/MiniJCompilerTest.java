package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.CharStreams;

public class MiniJCompilerTest {

    private static final String CODE = """
            int main() {
                 int i;
                 int result;
                 while(i <= 10) {
                    result = result + i;
                    i = i + 1;
                 }
                 writeInt(result);
                 return 0;
             }
            """;

    public static void main(final String[] args) {
        boolean isSuccessful = new MiniJCompiler().run(CharStreams.fromString(CODE));
        System.out.println("IsSuccessful: " + isSuccessful);
    }
}
