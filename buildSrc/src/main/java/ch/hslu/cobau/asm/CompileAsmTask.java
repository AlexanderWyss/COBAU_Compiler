package ch.hslu.cobau.asm;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.api.file.FileType;
import org.gradle.work.ChangeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Gradle task that assembles all source files contained in the input directory,
 * and produces the corresponding object file in the output directory (same name by different suffix).
 */
public class CompileAsmTask extends DefaultTask {
    private FileCollection inputDir;
    private File outputDir;

    @Incremental
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public FileCollection getInputDir() {
        return inputDir;
    }

    public void setInputDir(FileCollection inputDir) {
        this.inputDir = inputDir;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @TaskAction
    void execute(InputChanges inputChanges) {
        boolean failed = false;
        for(FileChange change : inputChanges.getFileChanges(inputDir)) {
            if (change.getFileType() == FileType.DIRECTORY) {

                // create new directories
                if (!change.getNormalizedPath().trim().equals("")) {
                    new File(outputDir, change.getNormalizedPath()).mkdir();
                }

            } else {
                boolean isAssemblerSource = change.getNormalizedPath().toLowerCase().endsWith(".asm");
                File sourceFile = change.getFile();
                File targetFile;

                if (isAssemblerSource) {
                    String outputPath = change.getNormalizedPath();
                    outputPath = outputPath.substring(0, outputPath.lastIndexOf('.')) + "." + AsmProperties.getObjectSuffix();
                    targetFile = new File(outputDir, outputPath);

                } else {
                    targetFile = new File(outputDir, change.getNormalizedPath());
                }

                if (change.getChangeType() == ChangeType.REMOVED) {
                    if (!targetFile.delete()) {
                        System.err.println("Warning: cannot delete file: " + targetFile.getAbsolutePath());
                    }
                } else {
                    if (isAssemblerSource) {
                        failed |= !AsmUtils.assembleSource(change, targetFile);
                    } else {
                        copy(sourceFile, targetFile);
                    }
                }
            }
        }

        // mark task as failed if any assembler source had a problem
        if (failed) {
            throw new GradleException("error while processing assembler sources");
        }
    }

    /**
     * Performs a file copy and wraps any IOException within a GradleException.
     *
     * @param sourceFile The source file.
     * @param targetFile The targer file.
     */
    private void copy(File sourceFile, File targetFile) {
        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        } catch (IOException e) {
            throw new GradleException("Error while copy from '" + sourceFile + "' to '" + targetFile + "'", e);
        }
    }
}
