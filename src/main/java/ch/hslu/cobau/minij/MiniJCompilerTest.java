package ch.hslu.cobau.minij;

import org.antlr.v4.runtime.CharStreams;

import java.io.PrintStream;

public class MiniJCompilerTest {

    private static final String CODE = """
             	int a0;
                  int a1;
                  int a2;
                  int a3;
                  int a4;
                  int retval;
                  void set(int index, int value) {
                     if (index == 0) { a0 = value; }
                     else if (index == 1) { a1 = value; }
                     else if (index == 2) { a2 = value; }
                     else if (index == 3) { a3 = value; }
                     else { a4 = value; }
                  }
                  void get(int index) {
                     if (index == 0) { retval = a0; }
                     else if (index == 1) { retval = a1; }
                     else if (index == 2) { retval = a2; }
                     else if (index == 3) { retval = a3; }
                     else { retval = a4; }
                  }
                  void print() {
                      writeInt(a0); writeChar(44);
                      writeInt(a1); writeChar(44);
                      writeInt(a2); writeChar(44);
                      writeInt(a3); writeChar(44);
                      writeInt(a4); writeChar(10);
                  }
                  void sort() {
                      int i;
                      int n;
                      bool swapped;
                      int prev;
                      n = 5;
                      swapped = true;
                      while(swapped) {
                          i = 0;
                          swapped = false;
                          while (i < n - 1) {
                              get(i);
                              prev = retval;
                              get(i + 1);
                              if (prev > retval) {
                                  get(i);
                                  prev = retval;
                                  get(i + 1);
                                  set(i, retval);
                                  set(i + 1, prev);
                                  swapped = true;
                              }
                              i = i + 1;
                          }
                          n = n - 1;
                     }
                  }
                  int main() {
                      a0 = 3; a1 = 1; a2 = 4; a3 = 23; a4= -3;
                      print();
                      sort();
                      print();
                      return 0;
                  }
            """;

    public static void main(final String[] args) {
        boolean isSuccessful = new MiniJCompiler().run(CharStreams.fromString(CODE), new PrintStreamLineEnumerator(System.out, true), true);
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
                    super.print(String.format("%d. %s\n", i + 1, lines[i])); // not redundant, silly intellij
                }
            } else {
                super.print(s);
            }
        }
    }
}
