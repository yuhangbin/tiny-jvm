package com.tinyjvm.classfile;

/**
 * Represents a field in the class file
 */
public class FieldInfo {

    private int accessFlags;
    private int nameIndex; // Index into constant pool
    private int descriptorIndex; // Index into constant pool
    private AttributeInfo[] attributes;

    /**
     * TODO: Implement this constructor
     * Parse field info from class data
     * 
     * @param reader ClassReader positioned at field entry
     */
    public FieldInfo(ClassReader reader) {
        // TODO: Read access flags, name index, descriptor index
        // TODO: Read attributes count and parse attributes
        throw new RuntimeException("TODO: Implement FieldInfo constructor");
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
}