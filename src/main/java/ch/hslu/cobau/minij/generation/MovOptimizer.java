package ch.hslu.cobau.minij.generation;

public class MovOptimizer implements Optimizer {
    private static final String MOV = "    mov ";

    public String optimize(String code) {
        StringBuilder optimized = new StringBuilder();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String nextLine = i + 1 < lines.length ? lines[i + 1] : null;
            if (nextLine != null && line.startsWith(MOV) & nextLine.startsWith(MOV)) {
                String[] registers = line.substring(MOV.length()).split(", ");
                String left = registers[0];
                String right = registers[1];
                String[] nextRegisters = nextLine.substring(MOV.length()).split(", ");
                String nextLeft = nextRegisters[0];
                String nextRight = nextRegisters[1];
                if (left.equals(nextRight) && !nextLeft.contains("[") && !right.contains("[") && !left.contains("[")) {
                    optimized.append(MOV).append(nextLeft).append(", ").append(right).append("\n");
                    i++;
                } else if (String.format("[%s]", left).equals(nextRight) && !right.contains("[")) {
                    optimized.append(MOV).append(nextLeft).append(", [").append(right).append("]\n");
                    i++;
                } else {
                    optimized.append(line).append("\n");
                }
            } else {
                optimized.append(line).append("\n");
            }
        }
        return optimized.toString();
    }
}
