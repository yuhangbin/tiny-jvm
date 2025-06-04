package com.tinyjvm.classfile;

/**
 * Represents a parsed Java .class file
 * 
 * A .class file structure (Java SE 11 Edition of JVM Spec):
 * ClassFile {
 * u4 magic;
 * u2 minor_version;
 * u2 major_version;
 * u2 constant_pool_count;
 * cp_info constant_pool[constant_pool_count-1];
 * u2 access_flags;
 * u2 this_class;
 * u2 super_class;
 * u2 interfaces_count;
 * u2 interfaces[interfaces_count];
 * u2 fields_count;
 * field_info fields[fields_count];
 * u2 methods_count;
 * method_info methods[methods_count];
 * u2 attributes_count;
 * attribute_info attributes[attributes_count];
 * }
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
    private int thisClass; // Index into constant pool for a CONSTANT_Class_info
    private int superClass; // Index into constant pool for a CONSTANT_Class_info, or 0
    private int[] interfaces; // Indices into constant pool for CONSTANT_Class_info

    // Class members (to be parsed later)
    private FieldInfo[] fields;
    private MethodInfo[] methods;
    private AttributeInfo[] attributes; // Top-level attributes of the class file

    /**
     * Parses a .class file from a byte array.
     * This constructor reads the class file structure up to and including
     * interfaces.
     * Fields, methods, and class attributes are parsed in subsequent steps.
     * 
     * @param classData The raw bytes from a .class file.
     * @throws ClassFormatError if the class data is malformed or the magic number
     *                          is incorrect.
     */
    public ClassFile(byte[] classData) {
        ClassReader reader = new ClassReader(classData);

        // 1. Read magic number and verify it's 0xCAFEBABE
        this.magic = reader.readU4();
        if (this.magic != MAGIC) {
            throw new ClassFormatError(
                    "Invalid magic number: 0x" + Integer.toHexString(this.magic) + ", expected 0xCAFEBABE");
        }

        // 2. Read version info
        this.minorVersion = reader.readU2();
        this.majorVersion = reader.readU2();
        // TODO: Add checks for supported major/minor versions if necessary

        // 3. Parse constant pool (constant_pool_count and entries)
        this.constantPool = new ConstantPool(reader);

        // 4. Read access flags
        this.accessFlags = reader.readU2();

        // 5. Read this_class index
        this.thisClass = reader.readU2();

        // 6. Read super_class index
        this.superClass = reader.readU2(); // Can be 0 if this class is java.lang.Object

        // 7. Read interfaces
        int interfacesCount = reader.readU2();
        this.interfaces = new int[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            this.interfaces[i] = reader.readU2();
        }

        // 8. Parse Fields
        int fieldsCount = reader.readU2();
        this.fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            this.fields[i] = new FieldInfo(reader, this.constantPool);
        }

        // 9. Parse Methods
        int methodsCount = reader.readU2();
        this.methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            this.methods[i] = new MethodInfo(reader, this.constantPool);
        }

        // 10. Parse ClassFile attributes
        int classAttributesCount = reader.readU2();
        this.attributes = new AttributeInfo[classAttributesCount];
        for (int i = 0; i < classAttributesCount; i++) {
            this.attributes[i] = AttributeInfo.readAttribute(reader, this.constantPool);
        }
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

    public int getThisClassIndex() { // Renamed for clarity
        return thisClass;
    }

    public int getSuperClassIndex() { // Renamed for clarity
        return superClass;
    }

    public int[] getInterfaceIndices() { // Renamed for clarity
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
     * Gets the name of this class from the constant pool.
     * 
     * @return Fully qualified class name (e.g., "java/lang/Object").
     * @throws ClassFormatError if the constant pool data is inconsistent.
     */
    public String getClassName() {
        if (this.constantPool == null || this.thisClass == 0) {
            throw new ClassFormatError("ClassFile not properly initialized or thisClass index is invalid.");
        }
        return this.constantPool.getClassName(this.thisClass);
    }

    /**
     * Gets the name of the super class from the constant pool.
     * 
     * @return Super class name (e.g., "java/lang/Object") or null if this class is
     *         java.lang.Object (superClass index is 0).
     * @throws ClassFormatError if the constant pool data is inconsistent.
     */
    public String getSuperClassName() {
        if (this.constantPool == null) {
            throw new ClassFormatError("ClassFile not properly initialized.");
        }
        if (this.superClass == 0) {
            return null; // java.lang.Object has no superclass, its superClass index is 0.
        }
        return this.constantPool.getClassName(this.superClass);
    }

    /**
     * Gets the names of all implemented interfaces.
     * 
     * @return An array of fully qualified interface names.
     */
    public String[] getInterfaceNames() {
        if (this.constantPool == null) {
            throw new ClassFormatError("ClassFile not properly initialized.");
        }
        String[] names = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            names[i] = this.constantPool.getClassName(interfaces[i]);
        }
        return names;
    }

    /**
     * Finds a method by name and descriptor
     * 
     * @param name       Method name (e.g., "main")
     * @param descriptor Method descriptor (e.g., "([Ljava/lang/String;)V")
     * @return MethodInfo or null if not found
     */
    public MethodInfo getMethod(String name, String descriptor) {
        for (MethodInfo method : methods) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Gets the SourceFile attribute of this class, if present.
     * 
     * @return The SourceFileAttribute, or null if this class has no SourceFile
     *         attribute.
     */
    public AttributeInfo.SourceFileAttribute getSourceFileAttribute() {
        for (AttributeInfo attr : attributes) {
            if (attr instanceof AttributeInfo.SourceFileAttribute) {
                return (AttributeInfo.SourceFileAttribute) attr;
            }
        }
        return null;
    }
}