package com.tinyjvm.classfile;

/**
 * Base class for class file attributes
 */
public abstract class AttributeInfo {

    protected int nameIndex;
    protected int length;

    public AttributeInfo(int nameIndex, int length) {
        this.nameIndex = nameIndex;
        this.length = length;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public int getLength() {
        return length;
    }

    /**
     * TODO: Implement this static method
     * Factory method to create appropriate AttributeInfo subclass
     * 
     * @param reader       ClassReader positioned at attribute
     * @param constantPool Constant pool for attribute names
     * @return Appropriate AttributeInfo subclass
     */
    public static AttributeInfo readAttribute(ClassReader reader, ConstantPool constantPool) {
        // TODO: Read attribute name index and length
        // TODO: Determine attribute type and create appropriate subclass
        throw new RuntimeException("TODO: Implement readAttribute()");
    }
}

/**
 * Represents the Code attribute containing method bytecode
 */
class CodeAttribute extends AttributeInfo {

    private int maxStack;
    private int maxLocals;
    private byte[] code;
    private ExceptionTableEntry[] exceptionTable;
    private AttributeInfo[] attributes;

    /**
     * TODO: Implement this constructor
     * Parse Code attribute from class data
     */
    public CodeAttribute(int nameIndex, int length, ClassReader reader) {
        super(nameIndex, length);
        // TODO: Read max_stack, max_locals
        // TODO: Read code_length and code bytes
        // TODO: Read exception table
        // TODO: Read attributes
        throw new RuntimeException("TODO: Implement CodeAttribute constructor");
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    public ExceptionTableEntry[] getExceptionTable() {
        return exceptionTable;
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }
}

/**
 * Represents an exception table entry in the Code attribute
 */
class ExceptionTableEntry {
    private int startPc;
    private int endPc;
    private int handlerPc;
    private int catchType;

    /**
     * TODO: Implement this constructor
     */
    public ExceptionTableEntry(ClassReader reader) {
        // TODO: Read start_pc, end_pc, handler_pc, catch_type
        throw new RuntimeException("TODO: Implement ExceptionTableEntry constructor");
    }

    public int getStartPc() {
        return startPc;
    }

    public int getEndPc() {
        return endPc;
    }

    public int getHandlerPc() {
        return handlerPc;
    }

    public int getCatchType() {
        return catchType;
    }
}