package ch.hslu.cobau.asm;

import org.apache.tools.ant.taskdefs.condition.Os;

import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulate the assembler and linker command lines for all supported systems.
 */
public class AsmProperties {
    private static final String ASM_PROPERTY_FILENAME = "Asm.properties";
    private static String[] asmCommandLine;
    private static String objectSuffix;
    private static String[] linkerCommandline;
    
    private static void loadProperties() {
        String prefix;
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            prefix = "windows";
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            prefix = "macos";
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            prefix = "linux";
        } else {
            throw new RuntimeException("unsupported OS family");
        }
        Properties properties = new Properties();
        try {
            properties.load(AsmProperties.class.getResourceAsStream(prefix + ASM_PROPERTY_FILENAME));
        } catch (IOException e) {
            throw new RuntimeException("error opening asm properties file", e);
        }
        asmCommandLine    = properties.getProperty("compileAsm.commandLine").split("\\s+");
        objectSuffix      = properties.getProperty("compileAsm.objectSuffix");
        linkerCommandline = properties.getProperty("linkAsm.commandLine").split("\\s+");
    }

    public static String[] getAsmCommandLine() {
        return asmCommandLine;
    }

    public static String getObjectSuffix() {
        return objectSuffix;
    }

    public static String[] getLinkerCommandline() {
        return linkerCommandline;
    }

    static {
        loadProperties();
    }
}
