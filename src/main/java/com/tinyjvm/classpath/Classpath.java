package com.tinyjvm.classpath;

import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Classpath manages where to find .class files
 * It can handle:
 * 1. Directories containing .class files
 * 2. JAR files (ZIP files containing .class files)
 * 3. Multiple paths separated by system path separator (: on Unix, ; on
 * Windows)
 */
public class Classpath {
    private ClasspathEntry entry;

    /**
     * Parse the classpath string and create appropriate ClasspathEntry objects
     * 
     * Classpath format examples:
     * "/path/to/classes" -> Directory
     * "/path/to/lib.jar" -> JAR file
     * "/path/to/classes:/path/to/lib.jar" -> Multiple entries
     * "" -> Empty (use current directory)
     * 
     * @param classpath The classpath string
     */
    public Classpath(String classpath) {
        if (classpath == null || classpath.isEmpty()) {
            classpath = "."; // Default to current directory
        }
        if (classpath.equals(".")) {
            this.entry = new DirectoryEntry(classpath);
        } else if (classpath.endsWith(".jar") || classpath.endsWith(".zip")) {
            this.entry = new JarEntry(classpath);
        } else if (classpath.contains(File.pathSeparator)) {
            this.entry = new CompositeEntry(classpath);
        } else {
            this.entry = new DirectoryEntry(classpath);
        }
    }

    /**
     * Read a class file from the classpath
     * 
     * @param className The class name (e.g., "java/lang/Object")
     * @return The class file bytes, or null if not found
     */
    public byte[] readClass(String className) {
        String fileName = className + ".class";

        try {
            return entry.readClass(fileName);
        } catch (IOException e) {
            // Only log errors for unexpected issues, not simple file-not-found cases
            if (!e.getMessage().contains("Class file not found") &&
                    !e.getMessage().contains("TODO")) {
                System.err.println("Error reading class " + className + ": " + e.getMessage());
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return entry.toString();
    }
}

/**
 * Base interface for different types of classpath entries
 */
abstract class ClasspathEntry {
    /**
     * Read a class file from this classpath entry
     * 
     * @param className The relative path to the class file
     * @return The class file bytes
     * @throws IOException If the file cannot be read
     */
    public abstract byte[] readClass(String className) throws IOException;

    @Override
    public abstract String toString();
}

/**
 * TODO: Implement this class
 * Handles classpath entries that are directories
 */
class DirectoryEntry extends ClasspathEntry {
    private String absDir;

    public DirectoryEntry(String path) {
        File dir = new File(path);
        this.absDir = dir.getAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) throws IOException {
        File classFile = new File(absDir, className);
        if (!classFile.exists()) {
            throw new IOException("Class file not found: " + classFile.getPath());
        }
        return Files.readAllBytes(classFile.toPath());
    }

    @Override
    public String toString() {
        return absDir;
    }
}

/**
 * TODO: Implement this class later
 * Handles classpath entries that are JAR/ZIP files
 */
class JarEntry extends ClasspathEntry {
    private String absPath;

    public JarEntry(String path) {
        this.absPath = new File(path).getAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(absPath))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(className)) {
                    return zip.readAllBytes();
                }
            }
        }
        throw new IOException("Class file not found in JAR: " + className);
    }

    @Override
    public String toString() {
        return absPath;
    }
}

/**
 * TODO: Implement this class later
 * Handles multiple classpath entries (separated by path separator)
 */
class CompositeEntry extends ClasspathEntry {
    private ClasspathEntry[] entries;

    public CompositeEntry(String pathList) {
        // TODO: Split pathList by system path separator and create entries
        String[] paths = pathList.split(File.pathSeparator);
        entries = new ClasspathEntry[paths.length];
        for (int i = 0; i < paths.length; i++) {
            entries[i] = new DirectoryEntry(paths[i]);
        }
    }

    @Override
    public byte[] readClass(String className) throws IOException {
        // TODO: Try each entry in order until one succeeds
        for (ClasspathEntry entry : entries) {
            try {
                return entry.readClass(className);
            } catch (IOException e) {
                // Ignore
            }
        }
        throw new IOException("Class file not found in any entry: " + className);
    }

    @Override
    public String toString() {
        // TODO: Return string representation of all entries
        StringBuilder sb = new StringBuilder();
        for (ClasspathEntry entry : entries) {
            sb.append(entry.toString());
            sb.append(File.pathSeparator);
        }
        return sb.toString();
    }
}
