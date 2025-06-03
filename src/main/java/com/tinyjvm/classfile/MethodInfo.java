package com.tinyjvm.classfile;

/**
 * Represents a method in the class file.
 * method_info {
 * u2 access_flags;
 * u2 name_index;
 * u2 descriptor_index;
 * u2 attributes_count;
 * attribute_info attributes[attributes_count];
 * }
 */
public class MethodInfo {

    private ConstantPool constantPool; // Keep a reference to resolve names/descriptors
    private int accessFlags;
    private int nameIndex; // Index into constant pool for CONSTANT_Utf8_info
    private int descriptorIndex; // Index into constant pool for CONSTANT_Utf8_info
    private AttributeInfo[] attributes;

    /**
     * Parses a method_info structure from the ClassReader.
     * 
     * @param reader ClassReader positioned at the beginning of a method_info
     *               structure.
     * @param cp     ConstantPool for resolving names, descriptors, and for
     *               attributes.
     */
    public MethodInfo(ClassReader reader, ConstantPool cp) {
        this.constantPool = cp;
        this.accessFlags = reader.readU2();
        this.nameIndex = reader.readU2();
        this.descriptorIndex = reader.readU2();

        int attributesCount = reader.readU2();
        this.attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            this.attributes[i] = AttributeInfo.readAttribute(reader, cp);
        }
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public String getName() {
        return constantPool.getUtf8(nameIndex);
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    public String getDescriptor() {
        return constantPool.getUtf8(descriptorIndex);
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }

    /**
     * Gets the Code attribute of this method, if present.
     * 
     * @return The CodeAttribute, or null if this method has no Code attribute
     *         (e.g., if it's an abstract or native method).
     */
    public CodeAttribute getCodeAttribute() {
        for (AttributeInfo attr : attributes) {
            // The name of the attribute is resolved from the constant pool
            // The AttributeInfo base class should provide a method to get its name.
            if ("Code".equals(attr.getName())) { // Assumes attr.getName() resolves name_index via ConstantPool
                if (attr instanceof CodeAttribute) {
                    return (CodeAttribute) attr;
                } else {
                    // This case should ideally not happen if readAttribute correctly instantiates
                    // CodeAttribute.
                    // However, if readAttribute returns a generic AttributeInfo for "Code", this is
                    // a problem.
                    // For now, we will assume readAttribute handles this correctly or throws an
                    // error there.
                    // Or, if CodeAttribute is a stub that doesn't extend our CodeAttribute type but
                    // is named "Code".
                    System.err
                            .println("Warning: Found attribute named 'Code' that is not an instance of CodeAttribute: "
                                    + attr.getClass().getName());
                }
            }
        }
        return null;
    }
}