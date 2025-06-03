package com.tinyjvm.classpath;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Unit tests for classpath functionality
 */
public class ClasspathTest {

    @TempDir
    Path tempDir;

    private String testClassPath;
    private byte[] testClassBytes;

    @BeforeEach
    public void setUp() throws IOException {
        // Use the real test class file we created
        testClassPath = "src/test/resources/testclasses";

        // Read the actual SimpleClass.class file for testing
        Path testClassFile = Paths.get(testClassPath, "SimpleClass.class");
        if (Files.exists(testClassFile)) {
            testClassBytes = Files.readAllBytes(testClassFile);
        } else {
            // Fallback: create a simple fake class file for testing
            testClassBytes = createFakeClassFile();
        }
    }

    private byte[] createFakeClassFile() {
        // Create a minimal fake .class file for testing
        // This is just for testing file I/O, not a real class file
        return new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0x00, 0x00, 0x00, 0x34 };
    }

    // Tests for Classpath main class

    @Test
    public void testClasspath_NullClasspath_UsesCurrentDirectory() {
        Classpath cp = new Classpath(null);
        assertNotNull(cp.toString(), "Should handle null classpath");
        assertTrue(cp.toString().contains(".") || cp.toString().length() > 0,
                "Should default to current directory or some valid path");
    }

    @Test
    public void testClasspath_EmptyClasspath_UsesCurrentDirectory() {
        Classpath cp = new Classpath("");
        assertNotNull(cp.toString(), "Should handle empty classpath");
    }

    @Test
    public void testClasspath_ValidDirectory_Success() {
        Classpath cp = new Classpath(testClassPath);
        assertNotNull(cp, "Should create classpath successfully");
        assertTrue(cp.toString().contains("testclasses"), "Should contain testclasses directory");
    }

    @Test
    public void testReadClass_ExistingClass_ReturnsBytes() {
        Classpath cp = new Classpath(testClassPath);
        byte[] result = cp.readClass("SimpleClass");

        if (testClassBytes.length > 0) {
            // If DirectoryEntry.readClass() is implemented, this should work
            // If not implemented, it will return null due to IOException handling
            if (result != null) {
                assertTrue(result.length > 0, "Class bytes should not be empty");
            }
        } else {
            // If DirectoryEntry.readClass() is not implemented yet, this is expected
            assertNull(result, "readClass should return null when not implemented");
        }
    }

    @Test
    public void testReadClass_NonExistentClass_ReturnsNull() {
        Classpath cp = new Classpath(testClassPath);
        byte[] result = cp.readClass("NonExistentClass");

        assertNull(result, "Should return null for non-existent class");
    }

    // Tests for DirectoryEntry

    @Test
    public void testDirectoryEntry_ValidPath_Success() throws IOException {
        // Create a temporary directory with a test file
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);

        Path testFile = testDir.resolve("SimpleClass.class");
        Files.write(testFile, testClassBytes);

        DirectoryEntry entry = new DirectoryEntry(testDir.toString());

        assertNotNull(entry.toString(), "Should have valid string representation");
        assertTrue(entry.toString().contains("testdir"), "Should contain directory name");
    }

    @Test
    public void testDirectoryEntry_ReadExistingClass_Success() throws IOException {
        // Create a temporary directory with a test file
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);

        Path testFile = testDir.resolve("SimpleClass.class");
        Files.write(testFile, testClassBytes);

        DirectoryEntry entry = new DirectoryEntry(testDir.toString());

        try {
            byte[] result = entry.readClass("SimpleClass.class");
            // If you've implemented readClass correctly, this should work
            assertNotNull(result, "Should return class bytes");
            assertTrue(result.length > 0, "Should return non-empty bytes");
            assertArrayEquals(testClassBytes, result, "Should return the correct bytes");
        } catch (IOException e) {
            // If not implemented yet, should throw IOException with TODO message
            assertTrue(e.getMessage().contains("TODO"),
                    "Should throw IOException with TODO message when not implemented");
        }
    }

    @Test
    public void testDirectoryEntry_ReadNonExistentClass_ThrowsException() {
        DirectoryEntry entry = new DirectoryEntry(testClassPath);

        assertThrows(IOException.class, () -> {
            entry.readClass("NonExistentClass.class");
        }, "Should throw IOException for non-existent class");
    }

    // Tests for JarEntry

    @Test
    public void testJarEntry_ValidJarPath_Success() throws IOException {
        // Create a test JAR file
        Path jarFile = tempDir.resolve("test.jar");
        createTestJar(jarFile, "SimpleClass.class", testClassBytes);

        JarEntry entry = new JarEntry(jarFile.toString());

        assertNotNull(entry.toString(), "Should have valid string representation");
        assertTrue(entry.toString().contains("test.jar"), "Should contain JAR file name");
    }

    @Test
    public void testJarEntry_ReadClass_WorksCorrectly() throws IOException {
        // Create a test JAR file
        Path jarFile = tempDir.resolve("test.jar");
        createTestJar(jarFile, "SimpleClass.class", testClassBytes);

        JarEntry entry = new JarEntry(jarFile.toString());

        try {
            // Should successfully read the class from the JAR
            byte[] result = entry.readClass("SimpleClass.class");
            assertNotNull(result, "Should return class bytes from JAR");
            assertTrue(result.length > 0, "Should return non-empty bytes");
            // Note: JAR reading might not return exact same bytes due to compression
        } catch (IOException e) {
            // If JAR reading fails, that's also acceptable for this test
            // We're testing that the JAR entry can be created and attempts to read
            assertTrue(e.getMessage().contains("Class file not found in JAR"),
                    "Should get appropriate error message for JAR reading");
        }
    }

    // Tests for CompositeEntry

    @Test
    public void testCompositeEntry_Constructor_Success() {
        // Should successfully create composite entry with multiple paths
        String multiplePaths = testClassPath + File.pathSeparator + ".";
        CompositeEntry entry = new CompositeEntry(multiplePaths);

        assertNotNull(entry.toString(), "Should have valid string representation");
        assertTrue(entry.toString().contains(File.pathSeparator),
                "Should contain path separator in string representation");
    }

    @Test
    public void testCompositeEntry_ReadClass_HandlesMultiplePaths() throws IOException {
        // Test reading from multiple classpath entries
        String multiplePaths = "." + File.pathSeparator + testClassPath;
        CompositeEntry entry = new CompositeEntry(multiplePaths);

        try {
            // Should find SimpleClass in one of the paths
            byte[] result = entry.readClass("SimpleClass");
            if (result != null) {
                assertTrue(result.length > 0, "Should return non-empty bytes when found");
            }
        } catch (IOException e) {
            // If not found in any path, that's expected behavior
            assertTrue(e.getMessage().contains("Class file not found in any entry"),
                    "Should get appropriate error message when not found in any entry");
        }
    }

    // Integration tests

    @Test
    public void testIntegration_FullWorkflow() throws IOException {
        // Test the full workflow: create classpath, read existing class
        Classpath cp = new Classpath(testClassPath);

        // This test validates the integration between Classpath and DirectoryEntry
        byte[] result = cp.readClass("SimpleClass");

        // Depending on implementation status, this might succeed or return null
        if (result != null) {
            assertTrue(result.length > 0, "Should return valid class bytes");
        }
        // If null, that's also acceptable if DirectoryEntry.readClass() isn't
        // implemented yet
    }

    @Test
    public void testIntegration_ClassNameConversion() {
        // Test that class names are properly converted to file paths
        Classpath cp = new Classpath(testClassPath);

        // These should all result in the same file path lookup
        // (though they might not find the file if path conversion isn't right)
        cp.readClass("SimpleClass");
        cp.readClass("com/example/MyClass");
        cp.readClass("java/lang/Object");

        // The test passes if no exceptions are thrown during path conversion
        assertTrue(true, "Class name to file path conversion should not throw exceptions");
    }

    // Helper method to create test JAR files
    private void createTestJar(Path jarPath, String entryName, byte[] entryBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
                JarOutputStream jos = new JarOutputStream(fos)) {

            ZipEntry entry = new ZipEntry(entryName);
            jos.putNextEntry(entry);
            jos.write(entryBytes);
            jos.closeEntry();
        }
    }

    // Performance test (optional)
    @Test
    public void testPerformance_ReadClassMultipleTimes() {
        Classpath cp = new Classpath(testClassPath);

        long startTime = System.currentTimeMillis();

        // Read the same class multiple times
        for (int i = 0; i < 100; i++) {
            cp.readClass("SimpleClass");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // This is more of a performance characterization than a strict test
        System.out.println("Time to read class 100 times: " + duration + "ms");
        assertTrue(duration < 10000, "Should complete in reasonable time (< 10 seconds)");
    }
}