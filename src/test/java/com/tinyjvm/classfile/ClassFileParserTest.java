package com.tinyjvm.classfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassFileParserTest {

    private static byte[] allConstantTypesClassData;
    private static ClassFile parsedClassFile;

    @BeforeAll
    static void loadClassFile() throws IOException {
        // Relative path to the compiled test class from the workspace root
        Path path = Paths.get("src/test/resources/testclasses/com/tinyjvm/test/AllConstantTypes.class");
        if (!Files.exists(path)) {
            throw new IOException("Test class file not found: " + path.toAbsolutePath());
        }
        allConstantTypesClassData = Files.readAllBytes(path);
        parsedClassFile = new ClassFile(allConstantTypesClassData);
    }

    @Test
    void testMagicNumber() {
        assertEquals(0xCAFEBABE, parsedClassFile.getMagic(), "Magic number should be 0xCAFEBABE");
    }

    @Test
    void testVersionNumbers() {
        // Assuming compilation with Java 11 (major version 55)
        assertEquals(55, parsedClassFile.getMajorVersion(), "Major version should correspond to Java 11 (55)");
        // Minor version can vary, but should be non-negative
        assertTrue(parsedClassFile.getMinorVersion() >= 0, "Minor version should be non-negative");
    }

    @Test
    void testConstantPool() {
        assertNotNull(parsedClassFile.getConstantPool(), "Constant pool should not be null");
        assertTrue(parsedClassFile.getConstantPool().getCount() > 10,
                "Constant pool should have a significant number of entries");
        // Example: Check for a known string (like the class name or a literal)
        // This requires finding its index with javap -v, so for now we check general
        // integrity.
    }

    @Test
    void testClassInfo() {
        assertEquals("com/tinyjvm/test/AllConstantTypes", parsedClassFile.getClassName(), "Class name mismatch");
        assertEquals("java/lang/Object", parsedClassFile.getSuperClassName(), "Superclass name mismatch");
        assertTrue(parsedClassFile.getInterfaceIndices().length > 0, "Should implement at least one interface");
        assertEquals("com/tinyjvm/test/MyExampleInterface",
                parsedClassFile.getConstantPool().getClassName(parsedClassFile.getInterfaceIndices()[0]),
                "Implemented interface name mismatch");
    }

    @Test
    void testSourceFileAttribute() {
        AttributeInfo.SourceFileAttribute sfAttr = parsedClassFile.getSourceFileAttribute();
        assertNotNull(sfAttr, "SourceFile attribute should be present");
        assertEquals("AllConstantTypes.java", sfAttr.getSourceFileName(), "SourceFile name mismatch");
    }

    @Test
    void testFields() {
        assertTrue(parsedClassFile.getFields().length >= 4, "Should have at least 4 declared fields");
        FieldInfo intField = findField(parsedClassFile, "intField");
        assertNotNull(intField, "Field 'intField' should be found");
        assertEquals("I", intField.getDescriptor(), "Descriptor for intField should be 'I'");

        FieldInfo stringField = findField(parsedClassFile, "stringField");
        assertNotNull(stringField, "Field 'stringField' should be found");
        assertEquals("Ljava/lang/String;", stringField.getDescriptor(),
                "Descriptor for stringField should be 'Ljava/lang/String;'");
    }

    @Test
    void testMethods() {
        // Includes constructor, instanceMethod, staticMethod, anInterfaceMethod, main
        assertTrue(parsedClassFile.getMethods().length >= 5, "Should have at least 5 methods");

        MethodInfo constructor = parsedClassFile.getMethod("<init>", "()V");
        assertNotNull(constructor, "Constructor <init>()V should be found");
        assertNotNull(constructor.getCodeAttribute(), "Constructor should have a Code attribute");
        assertTrue(constructor.getCodeAttribute().getCode().length > 0, "Constructor bytecode should not be empty");
        assertTrue(constructor.getCodeAttribute().getMaxStack() >= 1, "Constructor max_stack should be >= 1");
        assertTrue(constructor.getCodeAttribute().getMaxLocals() >= 1, "Constructor max_locals should be >= 1");

        MethodInfo instanceMethod = parsedClassFile.getMethod("instanceMethod", "(I)Ljava/lang/String;");
        assertNotNull(instanceMethod, "Method 'instanceMethod' should be found");
        AttributeInfo.CodeAttribute codeAttr = instanceMethod.getCodeAttribute();
        assertNotNull(codeAttr, "instanceMethod should have a Code attribute");
        assertTrue(codeAttr.getCode().length > 0, "instanceMethod bytecode should not be empty");
        // Exact max_stack/max_locals depend on compiler, check for plausible values
        // As per javap -v, for Java 11 --release 11, invokedynamic makes this 1
        assertEquals(1, codeAttr.getMaxStack(), "instanceMethod max_stack should be 1");
        assertTrue(codeAttr.getMaxLocals() >= 2, "instanceMethod max_locals should be >= 2 (this + param)");
        assertTrue(codeAttr.getExceptionTable().length == 0, "instanceMethod exception table should be empty");
    }

    private FieldInfo findField(ClassFile cf, String name) {
        for (FieldInfo field : cf.getFields()) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }
}