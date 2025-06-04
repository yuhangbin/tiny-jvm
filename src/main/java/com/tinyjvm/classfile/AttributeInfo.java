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
     * @return An instance of a specific AttributeInfo subclass or UnknownAttribute.
     */
    public static AttributeInfo readAttribute(ClassReader reader, ConstantPool constantPool) {
        int attributeNameIndex = reader.readU2();
        int attributeLength = reader.readU4();
        String attributeName = constantPool.getUtf8(attributeNameIndex);

        switch (attributeName) {
            case "SourceFile":
                return new SourceFileAttribute(attributeNameIndex, attributeLength, reader, constantPool);
            case "Code":
                return new CodeAttribute(attributeNameIndex, attributeLength, reader, constantPool);
            // case "ConstantValue":
            // return new ConstantValueAttribute(attributeNameIndex, attributeLength,
            // reader, constantPool);
            // break;
            // Add other known attributes here
            default:
                reader.skipBytes(attributeLength);
                return new UnknownAttribute(attributeNameIndex, attributeLength, constantPool);
        }
    }

    /**
     * Represents an attribute that is not specifically parsed or is unknown.
     * Its content is skipped.
     */
    static class UnknownAttribute extends AttributeInfo {
        UnknownAttribute(int nameIndex, int length, ConstantPool cp) {
            super(nameIndex, length, cp);
            // Data is skipped by the factory method for unknown attributes.
        }
    }

    /**
     * Represents the "SourceFile" attribute.
     * SourceFile_attribute {
     * u2 attribute_name_index;
     * u4 attribute_length; // Must be 2
     * u2 sourcefile_index; // Index to CONSTANT_Utf8_info
     * }
     */
    public static class SourceFileAttribute extends AttributeInfo {
        private int sourceFileIndex;

        public SourceFileAttribute(int nameIndex, int length, ClassReader reader, ConstantPool cp) {
            super(nameIndex, length, cp);
            if (length != 2) {
                throw new ClassFormatError("SourceFile attribute length must be 2, but was " + length);
            }
            this.sourceFileIndex = reader.readU2();
        }

        public int getSourceFileIndex() {
            return sourceFileIndex;
        }

        public String getSourceFileName() {
            return constantPool.getUtf8(sourceFileIndex);
        }
    }

    /**
     * Represents the Code attribute containing method bytecode
     */
    public static class CodeAttribute extends AttributeInfo {

        private int maxStack;
        private int maxLocals;
        private byte[] code;
        private ExceptionTableEntry[] exceptionTable;
        private AttributeInfo[] attributes; // Attributes of the Code attribute itself

        /**
         * Parses the Code attribute from the ClassReader.
         * The attribute_length is the length of the entire attribute, excluding the
         * initial 6 bytes
         * (attribute_name_index and attribute_length itself).
         */
        public CodeAttribute(int nameIndex, int length, ClassReader reader, ConstantPool cp) {
            super(nameIndex, length, cp); // length is the total length of the attribute data

            this.maxStack = reader.readU2();
            this.maxLocals = reader.readU2();

            int codeLength = reader.readU4();
            this.code = reader.readBytes(codeLength);

            int exceptionTableLength = reader.readU2();
            this.exceptionTable = new ExceptionTableEntry[exceptionTableLength];
            for (int i = 0; i < exceptionTableLength; i++) {
                this.exceptionTable[i] = new ExceptionTableEntry(reader);
            }

            int attributesCount = reader.readU2();
            this.attributes = new AttributeInfo[attributesCount];
            for (int i = 0; i < attributesCount; i++) {
                this.attributes[i] = AttributeInfo.readAttribute(reader, cp); // Recursive call for nested attributes
            }
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

        /**
         * Represents an exception table entry in the Code attribute
         * exception_table {
         * u2 start_pc;
         * u2 end_pc;
         * u2 handler_pc;
         * u2 catch_type; // Index into constant pool (CONSTANT_Class_info) or 0 for
         * finally
         * }
         */
        public static class ExceptionTableEntry {
            private int startPc;
            private int endPc;
            private int handlerPc;
            private int catchType; // If non-zero, an index into the constant pool to a CONSTANT_Class_info

            /**
             * Parses an exception_table_entry from the ClassReader.
             * 
             * @param reader ClassReader positioned at the start of the entry.
             */
            public ExceptionTableEntry(ClassReader reader) {
                this.startPc = reader.readU2();
                this.endPc = reader.readU2();
                this.handlerPc = reader.readU2();
                this.catchType = reader.readU2();
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
    }
}