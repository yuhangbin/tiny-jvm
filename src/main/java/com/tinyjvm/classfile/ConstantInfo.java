package com.tinyjvm.classfile;

import java.nio.charset.StandardCharsets;

/**
 * Base class for constant pool entries
 */
public abstract class ConstantInfo {

    // Constant pool tags (from JVM specification)
    public static final int CONSTANT_Utf8 = 1;
    public static final int CONSTANT_Integer = 3;
    public static final int CONSTANT_Float = 4;
    public static final int CONSTANT_Long = 5;
    public static final int CONSTANT_Double = 6;
    public static final int CONSTANT_Class = 7;
    public static final int CONSTANT_String = 8;
    public static final int CONSTANT_Fieldref = 9;
    public static final int CONSTANT_Methodref = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType = 12;

    protected int tag;

    public ConstantInfo(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    /**
     * Factory method to create appropriate ConstantInfo subclass based on tag
     * 
     * @param reader ClassReader positioned at constant entry
     * @return Appropriate ConstantInfo subclass
     */
    public static ConstantInfo readConstant(ClassReader reader) {
        int tag = reader.readU1();
        switch (tag) {
            case CONSTANT_Utf8:
                return new ConstantUtf8(reader);
            case CONSTANT_Class:
                return new ConstantClass(reader);
            case CONSTANT_String:
                return new ConstantString(reader);
            case CONSTANT_Fieldref:
                return new ConstantFieldref(reader);
            case CONSTANT_Methodref:
                return new ConstantMethodref(reader);
            case CONSTANT_InterfaceMethodref:
                return new ConstantInterfaceMethodref(reader);
            case CONSTANT_NameAndType:
                return new ConstantNameAndType(reader);
            case CONSTANT_Integer:
                return new ConstantInteger(reader);
            case CONSTANT_Float:
                return new ConstantFloat(reader);
            case CONSTANT_Long:
                return new ConstantLong(reader);
            case CONSTANT_Double:
                return new ConstantDouble(reader);
            default:
                throw new ClassFormatError("Invalid constant type: " + tag);
        }
    }

    // --- Inner classes for specific constant types ---

    /**
     * CONSTANT_Utf8_info structure holds UTF-8 strings.
     */
    static class ConstantUtf8 extends ConstantInfo {
        private String value;

        ConstantUtf8(ClassReader reader) {
            super(CONSTANT_Utf8);
            int length = reader.readU2();
            byte[] bytes = reader.readBytes(length);
            this.value = new String(bytes, StandardCharsets.UTF_8);
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * CONSTANT_Class_info structure represents a class or interface.
     */
    static class ConstantClass extends ConstantInfo {
        private int nameIndex; // Index into constant pool for the class name (a CONSTANT_Utf8_info)

        ConstantClass(ClassReader reader) {
            super(CONSTANT_Class);
            this.nameIndex = reader.readU2();
        }

        public int getNameIndex() {
            return nameIndex;
        }
    }

    /**
     * CONSTANT_String_info structure represents string constants.
     */
    static class ConstantString extends ConstantInfo {
        private int stringIndex; // Index into constant pool for the string value (a CONSTANT_Utf8_info)

        ConstantString(ClassReader reader) {
            super(CONSTANT_String);
            this.stringIndex = reader.readU2();
        }

        public int getStringIndex() {
            return stringIndex;
        }
    }

    // TODO: Define other ConstantXxx classes here (Fieldref, Methodref, Integer,
    // etc.)
    static class ConstantFieldref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantFieldref(ClassReader reader) {
            super(CONSTANT_Fieldref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }
    }

    static class ConstantMethodref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantMethodref(ClassReader reader) {
            super(CONSTANT_Methodref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }
    }

    static class ConstantInterfaceMethodref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantInterfaceMethodref(ClassReader reader) {
            super(CONSTANT_InterfaceMethodref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }
    }

    static class ConstantNameAndType extends ConstantInfo {
        private int nameIndex;
        private int descriptorIndex;

        ConstantNameAndType(ClassReader reader) {
            super(CONSTANT_NameAndType);
            this.nameIndex = reader.readU2();
            this.descriptorIndex = reader.readU2();
        }
    }

    static class ConstantInteger extends ConstantInfo {
        private int value;

        ConstantInteger(ClassReader reader) {
            super(CONSTANT_Integer);
            this.value = reader.readU4();
        }
    }

    static class ConstantFloat extends ConstantInfo {
        private float value;

        ConstantFloat(ClassReader reader) { 
            super(CONSTANT_Float);
            this.value = reader.readU4();
        }
    }

    static class ConstantLong extends ConstantInfo {
        private long value;

        ConstantLong(ClassReader reader) {
            super(CONSTANT_Long);
            this.value = reader.readU4();
            this.value = (this.value << 32) | reader.readU4();
        }
    }

    static class ConstantDouble extends ConstantInfo {
        private double value;

        ConstantDouble(ClassReader reader) {
            super(CONSTANT_Double);
            long highBytes = reader.readU4();
            long lowBytes = reader.readU4();
            long bits = (highBytes << 32) | lowBytes;
            this.value = Double.longBitsToDouble(bits);
        }
    }

}