package com.tinyjvm.classfile;

import java.nio.charset.StandardCharsets;

/**
 * Base class for constant pool entries.
 * The constant_pool is a table of structures representing various string
 * constants,
 * class and interface names, field names, and other constants that are referred
 * to
 * within the ClassFile structure and its substructures.
 */
public abstract class ConstantInfo {

    // Constant pool tags (from JVM specification)
    // Each item in the constant_pool table must begin with a 1-byte tag indicating
    // the kind of constant.
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
    // Tags added for Java 7+
    public static final int CONSTANT_MethodHandle = 15;
    public static final int CONSTANT_MethodType = 16;
    public static final int CONSTANT_InvokeDynamic = 18;
    // Java 9+
    public static final int CONSTANT_Dynamic = 17;
    public static final int CONSTANT_Module = 19;
    public static final int CONSTANT_Package = 20;

    protected int tag;
    protected ConstantPool constantPool; // For resolving names within specific constants if needed

    public ConstantInfo(int tag) {
        this.tag = tag;
    }

    // Setter for ConstantPool, to be called after ConstantPool itself is
    // initialized
    // This is needed if a ConstantInfo wants to resolve other constants during its
    // own parsing
    // which is not typical but possible for very complex constants or validation.
    // For now, most ConstantXyz constructors don't need it, but good to have the
    // link.
    void setConstantPool(ConstantPool cp) {
        this.constantPool = cp;
    }

    public int getTag() {
        return tag;
    }

    /**
     * Factory method to create appropriate ConstantInfo subclass based on tag.
     * Reads the 1-byte tag and then dispatches to the appropriate constructor.
     * 
     * @param reader ClassReader positioned at the beginning of a constant pool
     *               entry.
     * @return Appropriate ConstantInfo subclass instance.
     * @throws ClassFormatError if the tag is unrecognized or data is malformed.
     */
    public static ConstantInfo readConstant(ClassReader reader) {
        int tag = reader.readU1();
        ConstantInfo constInfo;
        switch (tag) {
            case CONSTANT_Utf8:
                constInfo = new ConstantUtf8(reader);
                break;
            case CONSTANT_Class:
                constInfo = new ConstantClass(reader);
                break;
            case CONSTANT_String:
                constInfo = new ConstantString(reader);
                break;
            case CONSTANT_Fieldref:
                constInfo = new ConstantFieldref(reader);
                break;
            case CONSTANT_Methodref:
                constInfo = new ConstantMethodref(reader);
                break;
            case CONSTANT_InterfaceMethodref:
                constInfo = new ConstantInterfaceMethodref(reader);
                break;
            case CONSTANT_NameAndType:
                constInfo = new ConstantNameAndType(reader);
                break;
            case CONSTANT_Integer:
                constInfo = new ConstantInteger(reader);
                break;
            case CONSTANT_Float:
                constInfo = new ConstantFloat(reader);
                break;
            case CONSTANT_Long:
                constInfo = new ConstantLong(reader);
                break;
            case CONSTANT_Double:
                constInfo = new ConstantDouble(reader);
                break;
            case CONSTANT_MethodHandle:
                constInfo = new ConstantMethodHandle(reader);
                break;
            case CONSTANT_MethodType:
                constInfo = new ConstantMethodType(reader);
                break;
            case CONSTANT_Dynamic:
                constInfo = new ConstantDynamic(reader);
                break;
            case CONSTANT_InvokeDynamic:
                constInfo = new ConstantInvokeDynamic(reader);
                break;
            default:
                throw new ClassFormatError("Invalid constant pool tag: " + tag);
        }
        return constInfo;
    }

    // --- Inner classes for specific constant types ---

    /**
     * Represents a `CONSTANT_Utf8_info` structure which holds UTF-8 encoded
     * strings.
     * These strings are used for class names, field names, method names,
     * descriptors, etc.
     * <p>
     * Example: A string literal "Hello, World", a class name "java/lang/String", or
     * a method descriptor "()V".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Utf8_info {
     *   u1 tag; // 1
     *   u2 length;
     *   u1 bytes[length];
     * }
     * </pre>
     */
    public static class ConstantUtf8 extends ConstantInfo {
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
     * Represents a `CONSTANT_Class_info` structure which refers to a class or
     * interface.
     * It does not directly contain the class name, but rather an index to a
     * `CONSTANT_Utf8_info`
     * entry in the constant pool that holds the class name.
     * <p>
     * Example: A reference to the class `java.lang.System`.
     * The `nameIndex` would point to a `CONSTANT_Utf8_info` holding the string
     * "java/lang/System".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Class_info {
     *     u1 tag; // 7
     *     u2 name_index; // Index to a CONSTANT_Utf8_info
     * }
     * </pre>
     */
    public static class ConstantClass extends ConstantInfo {
        private int nameIndex;

        ConstantClass(ClassReader reader) {
            super(CONSTANT_Class);
            this.nameIndex = reader.readU2();
        }

        public int getNameIndex() {
            return nameIndex;
        }
    }

    /**
     * Represents a `CONSTANT_String_info` structure which refers to a string
     * literal.
     * It refers to a `CONSTANT_Utf8_info` entry that holds the actual sequence of
     * characters.
     * <p>
     * Example: If your code has `String s = "Test";`, this entry would point to a
     * `CONSTANT_Utf8_info` holding "Test".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_String_info {
     *     u1 tag; // 8
     *     u2 string_index; // Index to a CONSTANT_Utf8_info
     * }
     * </pre>
     */
    public static class ConstantString extends ConstantInfo {
        private int stringIndex;

        ConstantString(ClassReader reader) {
            super(CONSTANT_String);
            this.stringIndex = reader.readU2();
        }

        public int getStringIndex() {
            return stringIndex;
        }
    }

    /**
     * Represents a `CONSTANT_Fieldref_info` structure which refers to a field.
     * It identifies the class or interface that contains the field, and the name
     * and type of the field.
     * <p>
     * Example: A reference to the field `java.lang.System.out`.
     * `classIndex` would point to a `CONSTANT_Class_info` for `java.lang.System`.
     * `nameAndTypeIndex` would point to a `CONSTANT_NameAndType_info` for "out" and
     * its type descriptor.
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Fieldref_info {
     *     u1 tag; // 9
     *     u2 class_index; // Index to a CONSTANT_Class_info
     *     u2 name_and_type_index; // Index to a CONSTANT_NameAndType_info
     * }
     * </pre>
     */
    public static class ConstantFieldref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantFieldref(ClassReader reader) {
            super(CONSTANT_Fieldref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }

        public int getClassIndex() {
            return classIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    /**
     * Represents a `CONSTANT_Methodref_info` structure which refers to a class
     * method (not an interface method).
     * It identifies the class that contains the method, and the name and type
     * descriptor of the method.
     * <p>
     * Example: A reference to `java.lang.Object.toString()`.
     * `classIndex` would point to a `CONSTANT_Class_info` for `java.lang.Object`.
     * `nameAndTypeIndex` would point to a `CONSTANT_NameAndType_info` for
     * "toString" and "()Ljava/lang/String;".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Methodref_info {
     *     u1 tag; // 10
     *     u2 class_index; // Index to a CONSTANT_Class_info
     *     u2 name_and_type_index; // Index to a CONSTANT_NameAndType_info
     * }
     * </pre>
     */
    public static class ConstantMethodref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantMethodref(ClassReader reader) {
            super(CONSTANT_Methodref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }

        public int getClassIndex() {
            return classIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    /**
     * Represents a `CONSTANT_InterfaceMethodref_info` structure which refers to an
     * interface method.
     * It identifies the interface that contains the method, and the name and type
     * descriptor of the method.
     * <p>
     * Example: A reference to `java.util.List.size()`.
     * `classIndex` would point to a `CONSTANT_Class_info` for `java.util.List`
     * (which is an interface).
     * `nameAndTypeIndex` would point to a `CONSTANT_NameAndType_info` for "size"
     * and "()I".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_InterfaceMethodref_info {
     *     u1 tag; // 11
     *     u2 class_index; // Index to a CONSTANT_Class_info (must be an interface)
     *     u2 name_and_type_index; // Index to a CONSTANT_NameAndType_info
     * }
     * </pre>
     */
    public static class ConstantInterfaceMethodref extends ConstantInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        ConstantInterfaceMethodref(ClassReader reader) {
            super(CONSTANT_InterfaceMethodref);
            this.classIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }

        public int getClassIndex() {
            return classIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    /**
     * Represents a `CONSTANT_NameAndType_info` structure which holds a name and a
     * type descriptor.
     * This structure is used by Fieldref, Methodref, and InterfaceMethodref to
     * specify a field or method.
     * <p>
     * Example: For a field `int count;`, this would hold "count" and "I".
     * For a method `void run();`, this would hold "run" and "()V".
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_NameAndType_info {
     *     u1 tag; // 12
     *     u2 name_index; // Index to a CONSTANT_Utf8_info (name)
     *     u2 descriptor_index; // Index to a CONSTANT_Utf8_info (descriptor)
     * }
     * </pre>
     */
    public static class ConstantNameAndType extends ConstantInfo {
        private int nameIndex;
        private int descriptorIndex;

        ConstantNameAndType(ClassReader reader) {
            super(CONSTANT_NameAndType);
            this.nameIndex = reader.readU2();
            this.descriptorIndex = reader.readU2();
        }

        public int getNameIndex() {
            return nameIndex;
        }

        public int getDescriptorIndex() {
            return descriptorIndex;
        }
    }

    /**
     * Represents a `CONSTANT_Integer_info` structure holding a 4-byte integer
     * value.
     * <p>
     * Example: The integer literal `12345`.
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Integer_info {
     *     u1 tag; // 3
     *     u4 bytes; // Big-endian integer value
     * }
     * </pre>
     */
    public static class ConstantInteger extends ConstantInfo {
        private int value;

        ConstantInteger(ClassReader reader) {
            super(CONSTANT_Integer);
            this.value = reader.readU4();
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Represents a `CONSTANT_Float_info` structure holding a 4-byte float value in
     * IEEE 754 format.
     * <p>
     * Example: The float literal `3.14f`.
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Float_info {
     *     u1 tag; // 4
     *     u4 bytes; // Big-endian bits of the float value
     * }
     * </pre>
     */
    public static class ConstantFloat extends ConstantInfo {
        private float value;

        ConstantFloat(ClassReader reader) {
            super(CONSTANT_Float);
            this.value = Float.intBitsToFloat(reader.readU4());
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Represents a `CONSTANT_Long_info` structure holding an 8-byte long integer
     * value.
     * Note: `CONSTANT_Long_info` and `CONSTANT_Double_info` take up two entries in
     * the constant pool.
     * <p>
     * Example: The long literal `1234567890L`.
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Long_info {
     *     u1 tag; // 5
     *     u4 high_bytes;
     *     u4 low_bytes;
     * }
     * </pre>
     */
    public static class ConstantLong extends ConstantInfo {
        private long value;

        ConstantLong(ClassReader reader) {
            super(CONSTANT_Long);
            long highBytes = reader.readU4();
            long lowBytes = reader.readU4();
            this.value = (highBytes << 32) + (lowBytes & 0xFFFFFFFFL);
        }

        public long getValue() {
            return value;
        }
    }

    /**
     * Represents a `CONSTANT_Double_info` structure holding an 8-byte double value
     * in IEEE 754 format.
     * Note: `CONSTANT_Long_info` and `CONSTANT_Double_info` take up two entries in
     * the constant pool.
     * <p>
     * Example: The double literal `3.1415926535`.
     * <p>
     * Structure:
     * 
     * <pre>
     * CONSTANT_Double_info {
     *     u1 tag; // 6
     *     u4 high_bytes;
     *     u4 low_bytes;
     * }
     * </pre>
     */
    public static class ConstantDouble extends ConstantInfo {
        private double value;

        ConstantDouble(ClassReader reader) {
            super(CONSTANT_Double);
            long highBytes = reader.readU4();
            long lowBytes = reader.readU4();
            long bits = (highBytes << 32) + (lowBytes & 0xFFFFFFFFL);
            this.value = Double.longBitsToDouble(bits);
        }

        public double getValue() {
            return value;
        }
    }

    /** CONSTANT_MethodHandle_info (tag 15) */
    public static class ConstantMethodHandle extends ConstantInfo {
        private int referenceKind; // u1
        private int referenceIndex; // u2

        ConstantMethodHandle(ClassReader reader) {
            super(CONSTANT_MethodHandle);
            this.referenceKind = reader.readU1();
            this.referenceIndex = reader.readU2();
        }

        public int getReferenceKind() {
            return referenceKind;
        }

        public int getReferenceIndex() {
            return referenceIndex;
        }
    }

    /** CONSTANT_MethodType_info (tag 16) */
    public static class ConstantMethodType extends ConstantInfo {
        private int descriptorIndex; // u2, index to CONSTANT_Utf8 for method descriptor

        ConstantMethodType(ClassReader reader) {
            super(CONSTANT_MethodType);
            this.descriptorIndex = reader.readU2();
        }

        public int getDescriptorIndex() {
            return descriptorIndex;
        }
    }

    /** CONSTANT_Dynamic_info (tag 17) */
    public static class ConstantDynamic extends ConstantInfo {
        private int bootstrapMethodAttrIndex; // u2
        private int nameAndTypeIndex; // u2, index to CONSTANT_NameAndType

        ConstantDynamic(ClassReader reader) {
            super(CONSTANT_Dynamic);
            this.bootstrapMethodAttrIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }

        public int getBootstrapMethodAttrIndex() {
            return bootstrapMethodAttrIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    /** CONSTANT_InvokeDynamic_info (tag 18) */
    public static class ConstantInvokeDynamic extends ConstantInfo {
        private int bootstrapMethodAttrIndex; // u2
        private int nameAndTypeIndex; // u2, index to CONSTANT_NameAndType

        ConstantInvokeDynamic(ClassReader reader) {
            super(CONSTANT_InvokeDynamic);
            this.bootstrapMethodAttrIndex = reader.readU2();
            this.nameAndTypeIndex = reader.readU2();
        }

        public int getBootstrapMethodAttrIndex() {
            return bootstrapMethodAttrIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }
}