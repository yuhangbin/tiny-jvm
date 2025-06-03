package com.tinyjvm.classfile;

/**
 * Constant Pool - stores constants used by the class
 * 
 * The constant pool contains:
 * - String literals
 * - Class and interface names
 * - Field names and descriptors
 * - Method names and descriptors
 * - Numeric constants
 * - etc.
 */
public class ConstantPool {

    private ConstantInfo[] constants;

    /**
     * TODO: Implement this constructor
     * Parse the constant pool from class data
     * 
     * @param reader ClassReader positioned at constant pool
     */
    public ConstantPool(ClassReader reader) {
        // TODO: Read constant pool count
        // TODO: Read each constant pool entry
        // TODO: Handle different constant types (CONSTANT_Utf8, CONSTANT_Class, etc.)

        throw new RuntimeException("TODO: Implement ConstantPool constructor");
    }

    /**
     * TODO: Implement this method
     * Get a constant pool entry by index
     * 
     * @param index 1-based index into constant pool
     * @return ConstantInfo entry
     */
    public ConstantInfo getConstant(int index) {
        // TODO: Return constant at index (remember: constant pool is 1-indexed)
        throw new RuntimeException("TODO: Implement getConstant()");
    }

    /**
     * TODO: Implement this method
     * Get a UTF-8 string from the constant pool
     * 
     * @param index Index of CONSTANT_Utf8 entry
     * @return The string value
     */
    public String getUtf8(int index) {
        // TODO: Get constant at index and extract UTF-8 string
        throw new RuntimeException("TODO: Implement getUtf8()");
    }

    /**
     * TODO: Implement this method
     * Get a class name from the constant pool
     * 
     * @param index Index of CONSTANT_Class entry
     * @return The class name
     */
    public String getClassName(int index) {
        // TODO: Get CONSTANT_Class entry, then get its name from UTF-8 constant
        throw new RuntimeException("TODO: Implement getClassName()");
    }

    public int getCount() {
        return constants != null ? constants.length : 0;
    }
}