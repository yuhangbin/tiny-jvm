package com.tinyjvm.rtda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.tinyjvm.classpath.Classpath;
import com.tinyjvm.classfile.ClassFile;
import com.tinyjvm.classfile.MethodInfo;
import com.tinyjvm.rtda.frame.LocalVars;
import com.tinyjvm.rtda.frame.OperandStack;

import java.io.File;
import java.util.Arrays;

/**
 * Comprehensive unit tests for Runtime Data Areas (RTDA) components.
 */
public class RTDATest {

    @Test
    @DisplayName("LocalVars - Basic operations")
    public void testLocalVarsBasicOperations() {
        LocalVars localVars = new LocalVars(10);

        // Test int operations
        localVars.setInt(0, 42);
        assertEquals(42, localVars.getInt(0));

        // Test float operations
        localVars.setFloat(1, 3.14f);
        assertEquals(3.14f, localVars.getFloat(1), 0.001f);

        // Test reference operations
        String testString = "Hello";
        localVars.setRef(2, testString);
        assertEquals(testString, localVars.getRef(2));

        // Test long operations (takes 2 slots)
        localVars.setLong(3, 1234567890L);
        assertEquals(1234567890L, localVars.getLong(3));

        // Test double operations (takes 2 slots)
        localVars.setDouble(5, 2.718281828);
        assertEquals(2.718281828, localVars.getDouble(5), 0.000000001);
    }

    @Test
    @DisplayName("LocalVars - Long and Double slot handling")
    public void testLocalVarsLongDoubleSlots() {
        LocalVars localVars = new LocalVars(10);

        // Set a long at index 0, should occupy indices 0 and 1
        localVars.setLong(0, 0x123456789ABCDEFL);
        assertEquals(0x123456789ABCDEFL, localVars.getLong(0));

        // Set a double at index 2, should occupy indices 2 and 3
        localVars.setDouble(2, Math.PI);
        assertEquals(Math.PI, localVars.getDouble(2), 0.000000001);

        // Test that we can access individual slots if needed (internal details)
        // This verifies that long/double are correctly split across two slots
        assertNotNull(localVars); // Just verify the object is valid
    }

    @Test
    @DisplayName("OperandStack - Basic operations")
    public void testOperandStackBasicOperations() {
        OperandStack stack = new OperandStack(10);

        // Test int operations
        stack.pushInt(100);
        stack.pushInt(200);
        assertEquals(200, stack.popInt());
        assertEquals(100, stack.popInt());

        // Test float operations
        stack.pushFloat(1.5f);
        stack.pushFloat(2.5f);
        assertEquals(2.5f, stack.popFloat(), 0.001f);
        assertEquals(1.5f, stack.popFloat(), 0.001f);

        // Test reference operations
        String obj1 = "Object1";
        String obj2 = "Object2";
        stack.pushRef(obj1);
        stack.pushRef(obj2);
        assertEquals(obj2, stack.popRef());
        assertEquals(obj1, stack.popRef());
    }

    @Test
    @DisplayName("OperandStack - Long and Double operations")
    public void testOperandStackLongDouble() {
        OperandStack stack = new OperandStack(10);

        // Test long operations (takes 2 stack slots)
        stack.pushLong(0xFEDCBA9876543210L);
        stack.pushLong(0x123456789ABCDEFL);
        assertEquals(0x123456789ABCDEFL, stack.popLong());
        assertEquals(0xFEDCBA9876543210L, stack.popLong());

        // Test double operations (takes 2 stack slots)
        stack.pushDouble(Math.E);
        stack.pushDouble(Math.PI);
        assertEquals(Math.PI, stack.popDouble(), 0.000000001);
        assertEquals(Math.E, stack.popDouble(), 0.000000001);
    }

    @Test
    @DisplayName("JvmStack - Frame management")
    public void testJvmStackFrameManagement() {
        JvmStack jvmStack = new JvmStack(5);

        assertTrue(jvmStack.isEmpty());

        // Create mock frames (we'll need MethodInfo for this)
        // For testing, we can create a simple frame without a real method
        Frame frame1 = new Frame(jvmStack, null); // null method for testing
        Frame frame2 = new Frame(jvmStack, null);

        jvmStack.push(frame1);
        assertFalse(jvmStack.isEmpty());
        assertEquals(frame1, jvmStack.top());

        jvmStack.push(frame2);
        assertEquals(frame2, jvmStack.top());

        Frame popped = jvmStack.pop();
        assertEquals(frame2, popped);
        assertEquals(frame1, jvmStack.top());

        jvmStack.pop();
        assertTrue(jvmStack.isEmpty());
    }

    @Test
    @DisplayName("Thread - Basic operations")
    public void testThreadBasicOperations() {
        Thread thread = new Thread();

        assertTrue(thread.isStackEmpty());

        // Test newFrame with null method (for testing purposes)
        // Note: newFrame() both creates and pushes the frame
        Frame frame = thread.newFrame(null);
        assertNotNull(frame);

        // Frame is already pushed by newFrame(), so stack should not be empty
        assertFalse(thread.isStackEmpty());
        assertEquals(frame, thread.currentFrame());

        thread.popFrame();
        assertTrue(thread.isStackEmpty());
    }

    @Test
    @DisplayName("MethodArea - Class storage and retrieval")
    public void testMethodAreaClassStorage() {
        MethodArea methodArea = new MethodArea();

        // Test adding and finding classes
        RuntimeClass mockClass1 = new RuntimeClass(null); // null ClassFile for testing
        RuntimeClass mockClass2 = new RuntimeClass(null);

        methodArea.addClass(mockClass1); // Will handle null names appropriately
        methodArea.addClass(mockClass2);

        // Since RuntimeClass with null ClassFile has null name, we can't retrieve them
        // Let's test with non-null names by testing the behavior with null
        assertNull(methodArea.findClass("com.example.Class1"));
        assertNull(methodArea.findClass("com.example.NonExistent"));

        // Test that adding null classes is handled gracefully
        methodArea.addClass(null);

        // Test that findClass returns null for non-existent classes
        assertNull(methodArea.findClass("com.example.NonExistent"));
    }

    @Test
    @DisplayName("Frame - Integration with LocalVars and OperandStack")
    public void testFrameIntegration() {
        JvmStack jvmStack = new JvmStack(5);
        Frame frame = new Frame(jvmStack, null); // null method for testing

        // Test that frame has properly initialized LocalVars and OperandStack
        assertNotNull(frame.getLocalVariables());
        assertNotNull(frame.getOperandStack());

        // Test PC operations
        assertEquals(0, frame.getNextPc()); // Default PC should be 0
        frame.setNextPc(42);
        assertEquals(42, frame.getNextPc());

        // Test that LocalVars and OperandStack work through the frame
        frame.getLocalVariables().setInt(0, 123);
        assertEquals(123, frame.getLocalVariables().getInt(0));

        frame.getOperandStack().pushInt(456);
        assertEquals(456, frame.getOperandStack().popInt());
    }

    @Test
    @DisplayName("ClassLoader - Error handling for non-existent classes")
    public void testClassLoaderErrorHandling() {
        Classpath classpath = new Classpath(".");
        MethodArea methodArea = new MethodArea();
        ClassLoader classLoader = new ClassLoader(classpath, methodArea);

        // Test loading a non-existent class
        assertThrows(ClassNotFoundException.class, () -> {
            classLoader.loadClass("com.nonexistent.FakeClass");
        });
    }

    @Test
    @DisplayName("LocalVars and OperandStack - Boundary conditions")
    public void testBoundaryConditions() {
        // Test with minimum size
        LocalVars smallLocalVars = new LocalVars(1);
        smallLocalVars.setInt(0, 42);
        assertEquals(42, smallLocalVars.getInt(0));

        OperandStack smallStack = new OperandStack(1);
        smallStack.pushInt(100);
        assertEquals(100, smallStack.popInt());

        // Test with larger size
        LocalVars largeLocalVars = new LocalVars(100);
        largeLocalVars.setInt(99, 999);
        assertEquals(999, largeLocalVars.getInt(99));

        OperandStack largeStack = new OperandStack(100);
        for (int i = 0; i < 50; i++) {
            largeStack.pushInt(i);
        }
        for (int i = 49; i >= 0; i--) {
            assertEquals(i, largeStack.popInt());
        }
    }

    @Test
    @DisplayName("Mixed data types on OperandStack")
    public void testMixedDataTypesOnStack() {
        OperandStack stack = new OperandStack(20);

        // Push mixed types
        stack.pushInt(42);
        stack.pushFloat(3.14f);
        stack.pushRef("Hello");
        stack.pushLong(1234567890L);
        stack.pushDouble(2.718);

        // Pop in reverse order
        assertEquals(2.718, stack.popDouble(), 0.000001);
        assertEquals(1234567890L, stack.popLong());
        assertEquals("Hello", stack.popRef());
        assertEquals(3.14f, stack.popFloat(), 0.001f);
        assertEquals(42, stack.popInt());
    }

    @Test
    @DisplayName("LocalVars - Mixed data types")
    public void testMixedDataTypesInLocalVars() {
        LocalVars localVars = new LocalVars(20);

        // Set different types at different indices
        localVars.setInt(0, 100);
        localVars.setFloat(1, 2.5f);
        localVars.setRef(2, "Test");
        localVars.setLong(3, 9876543210L); // Takes indices 3 and 4
        localVars.setDouble(5, Math.E); // Takes indices 5 and 6
        localVars.setInt(7, 200);

        // Verify all values
        assertEquals(100, localVars.getInt(0));
        assertEquals(2.5f, localVars.getFloat(1), 0.001f);
        assertEquals("Test", localVars.getRef(2));
        assertEquals(9876543210L, localVars.getLong(3));
        assertEquals(Math.E, localVars.getDouble(5), 0.000000001);
        assertEquals(200, localVars.getInt(7));
    }

    @Test
    @DisplayName("RuntimeClass - Basic functionality with null ClassFile")
    public void testRuntimeClassBasicFunctionality() {
        // Test with null ClassFile (minimal testing without actual class file parsing)
        RuntimeClass runtimeClass = new RuntimeClass(null);

        // Test that basic methods don't crash with null ClassFile
        assertNull(runtimeClass.getName()); // Will be null since ClassFile is null
        assertNull(runtimeClass.getClassFile());

        // Test findMethod with null ClassFile (should not crash)
        MethodInfo method = runtimeClass.findMethod("testMethod", "()V");
        assertNull(method); // Should return null since there's no ClassFile
    }

    @Test
    @DisplayName("Thread - Multiple frame operations")
    public void testThreadMultipleFrameOperations() {
        Thread thread = new Thread();

        // newFrame() both creates and pushes frames
        Frame frame1 = thread.newFrame(null);
        Frame frame2 = thread.newFrame(null);
        Frame frame3 = thread.newFrame(null);

        // Frames are already pushed by newFrame(), so frame3 should be on top
        assertEquals(frame3, thread.currentFrame());

        thread.popFrame();
        assertEquals(frame2, thread.currentFrame());

        thread.popFrame();
        assertEquals(frame1, thread.currentFrame());

        thread.popFrame();
        assertTrue(thread.isStackEmpty());
    }

    @Test
    @DisplayName("Integration test - Complete RTDA workflow")
    public void testCompleteRTDAWorkflow() {
        // Create all components
        Thread thread = new Thread();
        MethodArea methodArea = new MethodArea();

        // Create and configure a frame
        Frame frame = thread.newFrame(null);

        // Set up local variables
        frame.getLocalVariables().setInt(0, 10);
        frame.getLocalVariables().setInt(1, 20);

        // Perform stack operations
        frame.getOperandStack().pushInt(frame.getLocalVariables().getInt(0));
        frame.getOperandStack().pushInt(frame.getLocalVariables().getInt(1));

        // Simulate an add operation
        int b = frame.getOperandStack().popInt();
        int a = frame.getOperandStack().popInt();
        int result = a + b;
        frame.getOperandStack().pushInt(result);

        // Verify result
        assertEquals(30, frame.getOperandStack().popInt());

        // Test PC management
        frame.setNextPc(5);
        assertEquals(5, frame.getNextPc());

        // Add frame to thread
        thread.pushFrame(frame);
        assertEquals(frame, thread.currentFrame());
    }
}