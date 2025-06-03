package com.tinyjvm.classfile;

/**
 * Base class for class file attributes
 */
public abstract class AttributeInfo {

    protected int nameIndex;
    protected int length;
    // Keep a reference to the constant pool to resolve the attribute name
    protected ConstantPool constantPool;

    public AttributeInfo(int nameIndex, int length, ConstantPool cp) {
        this.nameIndex = nameIndex;
        this.length = length;
        this.constantPool = cp;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public String getName() {
        return constantPool.getUtf8(nameIndex);
    }

    public int getLength() {
        return length;
    }

    /**
     * Factory method to create appropriate AttributeInfo subclass.
     * For now, it reads known attributes or creates a generic UnparsedAttribute.
     * 
     * @param reader       ClassReader positioned at the start of an attribute_info
     *                     structure.
     * @param constantPool Constant pool for resolving attribute names and for
     *                     nested attributes.
     * @return An instance of a specific AttributeInfo subclass or
     *         UnparsedAttribute.
     */
    public static AttributeInfo readAttribute(ClassReader reader, ConstantPool constantPool) {
        int attributeNameIndex = reader.readU2();
        int attributeLength = reader.readU4();
        String attributeName = constantPool.getUtf8(attributeNameIndex);

        // TODO: Implement specific attribute parsing based on attributeName
        // Known attributes: "ConstantValue", "Code", "Exceptions", "SourceFile",
        // "LineNumberTable", "LocalVariableTable", "Deprecated", "Synthetic", etc.
        switch (attributeName) {
            // case "ConstantValue":
            // return new ConstantValueAttribute(attributeNameIndex, attributeLength,
            // reader, constantPool);
            // case "Code":
            // // CodeAttribute constructor needs different signature or handling here
            // // return new CodeAttribute(attributeNameIndex, attributeLength, reader,
            // constantPool);
            // break;
            // Add other known attributes here
            default:
                // For unknown or unhandled attributes, read and skip their data.
                reader.skipBytes(attributeLength);
                return new UnknownAttribute(attributeNameIndex, attributeLength, constantPool);
        }
    }

    /**
     * Represents an attribute that is not specifically parsed or is unknown.
     * Its content is skipped.
     */
    static class UnknownAttribute extends AttributeInfo {
        private byte[] data; // Optionally store the raw data if needed for debugging

        UnknownAttribute(int nameIndex, int length, ConstantPool cp) {
            super(nameIndex, length, cp);
            // The ClassReader already skipped the bytes in readAttribute's default case
            // If we wanted to store them, we would read them here BEFORE readAttribute
            // skips.
            // For now, data is not stored to save memory, assuming skipping is sufficient.
        }
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
    public CodeAttribute(int nameIndex, int length, ClassReader reader, ConstantPool cp) {
        super(nameIndex, length, cp);
        // TODO: Read max_stack, max_locals
        // TODO: Read code_length and code bytes
        // TODO: Read exception table
        // TODO: Read attributes within Code attribute
        // For now, skip the body of the code attribute to allow ClassFile parsing to
        // proceed
        reader.skipBytes(length); // This is a temporary measure to consume the attribute's declared length
        // throw new RuntimeException("TODO: Implement CodeAttribute constructor");
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