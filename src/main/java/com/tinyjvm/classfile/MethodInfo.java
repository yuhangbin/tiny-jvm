package com.tinyjvm.classfile;

// Import AttributeInfo to access its nested CodeAttribute class
import com.tinyjvm.classfile.AttributeInfo;

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

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    /**
     * Gets the Code attribute of this method, if present.
     * 
     * @return The CodeAttribute, or null if this method has no Code attribute
     *         (e.g., if it's an abstract or native method).
     */
    public AttributeInfo.CodeAttribute getCodeAttribute() {
        for (AttributeInfo attr : attributes) {
            if ("Code".equals(attr.getName())) {
                if (attr instanceof AttributeInfo.CodeAttribute) {
                    return (AttributeInfo.CodeAttribute) attr;
                } else {
                    System.err
                            .println(
                                    "Warning: Found attribute named 'Code' that is not an instance of AttributeInfo.CodeAttribute: "
                                            + attr.getClass().getName());
                }
            }
        }
        return null;
    }
}