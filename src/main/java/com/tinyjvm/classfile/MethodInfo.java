package com.tinyjvm.classfile;

/**
 * Represents a method in the class file
 * Contains method metadata and bytecode
 */
public class MethodInfo {

    private int accessFlags;
    private int nameIndex; // Index into constant pool
    private int descriptorIndex; // Index into constant pool
    private AttributeInfo[] attributes;

    /**
     * TODO: Implement this constructor
     * Parse method info from class data
     * 
     * @param reader ClassReader positioned at method entry
     */
    public MethodInfo(ClassReader reader) {
        // TODO: Read access flags, name index, descriptor index
        // TODO: Read attributes count and parse attributes
        throw new RuntimeException("TODO: Implement MethodInfo constructor");
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }

    /**
     * TODO: Implement this method
     * Get the Code attribute containing bytecode
     * 
     * @return CodeAttribute or null if not found
     */
    public CodeAttribute getCodeAttribute() {
        // TODO: Search through attributes for Code attribute
        throw new RuntimeException("TODO: Implement getCodeAttribute()");
    }

    /**
     * TODO: Implement this method
     * Check if this method matches the given name and descriptor
     * 
     * @param constantPool Constant pool to look up names
     * @param name         Method name
     * @param descriptor   Method descriptor
     * @return true if this method matches
     */
    public boolean matches(ConstantPool constantPool, String name, String descriptor) {
        // TODO: Compare method name and descriptor using constant pool
        throw new RuntimeException("TODO: Implement matches()");
    }
}