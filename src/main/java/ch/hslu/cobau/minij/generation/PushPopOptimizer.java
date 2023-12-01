package ch.hslu.cobau.minij.generation;

public class PushPopOptimizer implements Optimizer {
    private static final String PUSH = "    push ";
    private static final String POP = "    pop ";

    public String optimize(String code) {
        StringBuilder optimized = new StringBuilder();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String nextLine = i + 1 < lines.length ? lines[i + 1] : null;
            if (nextLine != null && line.startsWith(PUSH) & nextLine.startsWith(POP)) {
                String pushRegister = line.substring(PUSH.length());
                String popRegister = nextLine.substring(POP.length());
                if (!pushRegister.equals(popRegister)) {
                    optimized.append("    mov ").append(popRegister).append(", ").append(pushRegister).append("\n");
                }
                i++;
            } else {
                optimized.append(line).append("\n");
            }
        }
        return optimized.toString();
    }
}
