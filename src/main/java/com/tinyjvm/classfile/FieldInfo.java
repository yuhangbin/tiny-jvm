package com.tinyjvm.classfile;

/**
 * Represents a field in the class file.
 * field_info {
 * u2 access_flags;
 * u2 name_index;
 * u2 descriptor_index;
 * u2 attributes_count;
 * attribute_info attributes[attributes_count];
 * }
 */
public class FieldInfo {

    private ConstantPool constantPool; // Keep a reference to resolve names/descriptors
    private int accessFlags;
    private int nameIndex; // Index into constant pool for CONSTANT_Utf8_info
    private int descriptorIndex; // Index into constant pool for CONSTANT_Utf8_info
    private AttributeInfo[] attributes;

    /**
     * Parses field_info structure from the ClassReader.
     * 
     * @param reader ClassReader positioned at the beginning of a field_info
     *               structure.
     * @param cp     ConstantPool for resolving names and descriptors.
     */
    public FieldInfo(ClassReader reader, ConstantPool cp) {
        this.constantPool = cp;
        this.accessFlags = reader.readU2();
        this.nameIndex = reader.readU2();
        this.descriptorIndex = reader.readU2();

        // Parse attributes (basic framework, actual attribute parsing is complex)
        int attributesCount = reader.readU2();
        this.attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            // TODO: Full attribute parsing will be handled by AttributeInfo.readAttribute()
            // For now, we can either skip or read them generically if AttributeInfo is set
            // up for it.
            // this.attributes[i] = AttributeInfo.readAttribute(reader, cp);
            // For this step, let's assume AttributeInfo.readAttribute will be implemented
            // later
            // and for now, we might just create placeholder or skip bytes if not handled by
            // AttributeInfo.
            // For simplicity in this step, we'll create new basic AttributeInfo if it's
            // defined or leave them null.
            // This part will be fleshed out when AttributeInfo parsing is implemented.
            // For now, if AttributeInfo.readAttribute is not ready, we can't fully parse.
            // Let's assume for now that attributes are read by a static method in
            // AttributeInfo.
            // If AttributeInfo.readAttribute is not yet implemented to actually consume
            // bytes,
            // this will lead to errors when parsing subsequent fields/methods.
            // For now, we will defer full attribute parsing.
            this.attributes[i] = AttributeInfo.readAttribute(reader, cp); // This line assumes
                                                                          // AttributeInfo.readAttribute exists and
                                                                          // works.
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

    // TODO: Add method to get ConstantValue attribute if present
}