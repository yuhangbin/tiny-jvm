package com.tinyjvm.classfile;

/**
 * Represents a parsed Java .class file
 * 
 * A .class file structure:
 * - Magic number (0xCAFEBABE)
 * - Version info (minor, major)
 * - Constant pool (strings, class names, method references, etc.)
 * - Access flags (public, final, etc.)
 * - This class name
 * - Super class name
 * - Interfaces
 * - Fields
 * - Methods (containing bytecode)
 * - Attributes
 */
public class ClassFile {

    // Magic number for Java class files
    public static final int MAGIC = 0xCAFEBABE;

    // Basic file info
    private int magic;
    private int minorVersion;
    private int majorVersion;

    // Constant pool
    private ConstantPool constantPool;

    // Class info
    private int accessFlags;
    private int thisClass; // Index into constant pool
    private int superClass; // Index into constant pool
    private int[] interfaces; // Indices into constant pool

    // Class members
    private FieldInfo[] fields;
    private MethodInfo[] methods;
    private AttributeInfo[] attributes;

    /**
     * TODO: Implement this constructor
     * Parse a .class file from byte array
     * 
     * This is the main parsing logic that reads the binary format
     * and populates all the fields above.
     * 
     * @param classData The raw bytes from a .class file
     */
    public ClassFile(byte[] classData) {
        // TODO: Implement class file parsing
        // 1. Create a ClassReader to read bytes
        // 2. Read magic number and verify it's 0xCAFEBABE
        // 3. Read version info
        // 4. Parse constant pool
        // 5. Read access flags and class references
        // 6. Parse interfaces, fields, methods, attributes

        throw new RuntimeException("TODO: Implement ClassFile constructor");
    }

    // Getters for accessing parsed data

    public int getMagic() {
        return magic;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getThisClass() {
        return thisClass;
    }

    public int getSuperClass() {
        return superClass;
    }

    public int[] getInterfaces() {
        return interfaces;
    }

    public FieldInfo[] getFields() {
        return fields;
    }

    public MethodInfo[] getMethods() {
        return methods;
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }

    /**
     * TODO: Implement this method
     * Get the name of this class from the constant pool
     * 
     * @return Fully qualified class name (e.g., "java/lang/Object")
     */
    public String getClassName() {
        // TODO: Look up thisClass index in constant pool and get the class name
        throw new RuntimeException("TODO: Implement getClassName()");
    }

    /**
     * TODO: Implement this method
     * Get the name of the super class from the constant pool
     * 
     * @return Super class name (e.g., "java/lang/Object") or null if no super class
     */
    public String getSuperClassName() {
        // TODO: Look up superClass index in constant pool and get the class name
        throw new RuntimeException("TODO: Implement getSuperClassName()");
    }

    /**
     * TODO: Implement this method
     * Find a method by name and descriptor
     * 
     * @param name       Method name (e.g., "main")
     * @param descriptor Method descriptor (e.g., "([Ljava/lang/String;)V")
     * @return MethodInfo or null if not found
     */
    public MethodInfo getMethod(String name, String descriptor) {
        // TODO: Search through methods array and match name and descriptor
        throw new RuntimeException("TODO: Implement getMethod()");
    }
}