package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.CharStreams;

public class MiniJCompilerTest {

    private static final String CODE = """
            int main() {
                Coordinate coordinate;
            }
            """;

    public static void main(final String[] args) {
        boolean isSuccessful = new MiniJCompiler().run(CharStreams.fromString(CODE));
        System.out.println("IsSuccessful: " + isSuccessful);
    }
}
