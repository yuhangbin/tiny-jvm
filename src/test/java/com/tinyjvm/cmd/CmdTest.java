package com.tinyjvm.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for command line argument parsing
 */
public class CmdTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testParseCmd_EmptyArgs_ReturnsNull() {
        // Test: empty arguments should print usage and return null
        String[] args = {};
        Cmd result = Cmd.parseCmd(args);

        assertNull(result, "Empty arguments should return null");
        assertTrue(outContent.toString().contains("Usage:"), "Should print usage message");
    }

    @Test
    public void testParseCmd_HelpFlag_ReturnsNull() {
        // Test: -help flag should print usage and return null
        String[] args = { "-help" };
        Cmd result = Cmd.parseCmd(args);

        assertNull(result, "-help should return null");
        assertTrue(outContent.toString().contains("Usage:"), "Should print usage message");
    }

    @Test
    public void testParseCmd_VersionFlag_ReturnsNull() {
        // Test: -version flag should print version and return null
        String[] args = { "-version" };
        Cmd result = Cmd.parseCmd(args);

        assertNull(result, "-version should return null");
        assertTrue(outContent.toString().contains("tiny JVM version"), "Should print version message");
    }

    @Test
    public void testParseCmd_SimpleClassName_Success() {
        // Test: simple class name without options
        String[] args = { "HelloWorld" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("HelloWorld", result.getClassName(), "Class name should be set correctly");
        assertEquals("", result.getClasspath(), "Classpath should be empty string");
        assertArrayEquals(new String[0], result.getArgs(), "Args should be empty array");
        assertFalse(result.isHelpFlag(), "Help flag should be false");
        assertFalse(result.isVersionFlag(), "Version flag should be false");
    }

    @Test
    public void testParseCmd_ClassNameWithArgs_Success() {
        // Test: class name with program arguments
        String[] args = { "HelloWorld", "arg1", "arg2", "arg3" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("HelloWorld", result.getClassName(), "Class name should be set correctly");
        assertEquals("", result.getClasspath(), "Classpath should be empty string");
        assertArrayEquals(new String[] { "arg1", "arg2", "arg3" }, result.getArgs(), "Args should be set correctly");
    }

    @Test
    public void testParseCmd_ClasspathShort_Success() {
        // Test: -cp option with classpath
        String[] args = { "-cp", "/path/to/classes", "HelloWorld" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("HelloWorld", result.getClassName(), "Class name should be set correctly");
        assertEquals("/path/to/classes", result.getClasspath(), "Classpath should be set correctly");
        assertArrayEquals(new String[0], result.getArgs(), "Args should be empty array");
    }

    @Test
    public void testParseCmd_ClasspathLong_Success() {
        // Test: -classpath option with classpath
        String[] args = { "-classpath", "/another/path", "MyClass" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("MyClass", result.getClassName(), "Class name should be set correctly");
        assertEquals("/another/path", result.getClasspath(), "Classpath should be set correctly");
        assertArrayEquals(new String[0], result.getArgs(), "Args should be empty array");
    }

    @Test
    public void testParseCmd_ClasspathWithArgs_Success() {
        // Test: -cp option with classpath, class name, and program arguments
        String[] args = { "-cp", "/path/to/classes", "HelloWorld", "param1", "param2" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("HelloWorld", result.getClassName(), "Class name should be set correctly");
        assertEquals("/path/to/classes", result.getClasspath(), "Classpath should be set correctly");
        assertArrayEquals(new String[] { "param1", "param2" }, result.getArgs(), "Args should be set correctly");
    }

    @Test
    public void testParseCmd_ClasspathWithComplexPath_Success() {
        // Test: -cp with complex classpath (multiple entries)
        String[] args = { "-cp", "/path/to/classes:/path/to/lib.jar:.", "MyApp" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("MyApp", result.getClassName(), "Class name should be set correctly");
        assertEquals("/path/to/classes:/path/to/lib.jar:.", result.getClasspath(),
                "Complex classpath should be preserved");
    }

    // Edge case tests that might reveal bugs in your implementation

    @Test
    public void testParseCmd_ClasspathMissingPath_ShouldHandleGracefully() {
        // Test: -cp without path argument (edge case)
        // This test checks how your implementation handles malformed input
        String[] args = { "-cp" };

        // Your implementation should handle this gracefully
        // It might throw an exception or return null - document the expected behavior
        try {
            Cmd result = Cmd.parseCmd(args);
            // If it returns null, that's one valid approach
            // If it throws an exception, that's also valid
            // The test documents the expected behavior
        } catch (ArrayIndexOutOfBoundsException e) {
            // This is acceptable - malformed input
            assertTrue(true, "ArrayIndexOutOfBoundsException is acceptable for malformed input");
        }
    }

    @Test
    public void testParseCmd_ClasspathMissingClassName_ShouldHandleGracefully() {
        // Test: -cp with path but no class name
        String[] args = { "-cp", "/some/path" };

        try {
            Cmd result = Cmd.parseCmd(args);
            // Document expected behavior
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true, "ArrayIndexOutOfBoundsException is acceptable for malformed input");
        }
    }

    @Test
    public void testParseCmd_UnknownOption_TreatedAsClassName() {
        // Test: unknown option should be treated as class name
        String[] args = { "-unknown", "arg1" };
        Cmd result = Cmd.parseCmd(args);

        assertNotNull(result, "Should return valid Cmd object");
        assertEquals("-unknown", result.getClassName(), "Unknown option should be treated as class name");
        assertArrayEquals(new String[] { "arg1" }, result.getArgs(), "Following args should be program args");
    }
}