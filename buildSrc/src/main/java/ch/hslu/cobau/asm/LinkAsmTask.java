package ch.hslu.cobau.asm;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.util.*;

/**
 * Gradle task that links the specified objects files into the specified target binary.
 */
public class LinkAsmTask extends DefaultTask {
    private FileCollection inputObjects;
    private File target;

    @InputFiles
    public FileCollection getInputObjects() {
        return inputObjects;
    }

    public void setInputObjects(FileCollection inputObjects) {
        this.inputObjects = inputObjects;
    }

    @OutputFile
    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    @TaskAction
    public void execute() {
        Collection<File> sourceObjects = new ArrayList<>();
        inputObjects.forEach(sourceObjects::add);
        boolean result = AsmUtils.linkSources(sourceObjects, target);
        if (!result) {
            throw new GradleException("LinkAsmTask: error while linking (see above error messages)");
        }
    }
}
