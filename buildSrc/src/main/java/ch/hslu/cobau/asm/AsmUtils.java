package ch.hslu.cobau.asm;

import org.gradle.api.GradleException;
import org.gradle.work.FileChange;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class AsmUtils {
    public static boolean assembleSource(FileChange change, File targetFile) {
        List<String> commandLine = getAsmCommandLine(change.getFile(), targetFile);
        return executeCommandline(commandLine);
    }

    public static boolean linkSources(Collection<File> sourceObjects, File targetBinary) {
        List<String> commandLine = getLinkCommandLine(sourceObjects, targetBinary);
        return executeCommandline(commandLine);
    }

    public static boolean executeCommandline(List<String> commandLine) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(commandLine);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new GradleException("Error while executing commandline " + Arrays.toString(commandLine.toArray()), e);
        }
    }

    private static List<String> getAsmCommandLine(File source, File target) {
        List<String> expandedCommandLine = new LinkedList<>();
        for (String s : AsmProperties.getAsmCommandLine()) {
            if (Objects.equals(s, "{{source}}")) {
                expandedCommandLine.add(source.getAbsolutePath());
            } else if (Objects.equals(s, "{{target}}")) {
                expandedCommandLine.add(target.getAbsolutePath());
            } else {
                expandedCommandLine.add(s);
            }
        }
        return expandedCommandLine;
    }

    private static List<String> getLinkCommandLine(Collection<File> sourceObject, File targetBinary) {
        List<String> expandedCommandLine = new LinkedList<>();
        for (String s : AsmProperties.getLinkerCommandline()) {
            if (Objects.equals(s, "{{objects}}")) {
                sourceObject.forEach(file -> {
                    expandedCommandLine.add(file.getAbsolutePath());
                });
            } else if (Objects.equals(s, "{{target}}")) {
                expandedCommandLine.add(targetBinary.getAbsolutePath());
            } else {
                expandedCommandLine.add(s);
            }
        }
        return expandedCommandLine;
    }
}
