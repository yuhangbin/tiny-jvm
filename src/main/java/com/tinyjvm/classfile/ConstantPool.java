package com.tinyjvm.classfile;

/**
 * Constant Pool - stores constants used by the class
 * 
 * The constant_pool table is an array of `cp_info` structures.
 * The `constant_pool_count` item gives the number of entries in the
 * `constant_pool` table plus one.
 * A `constant_pool` index is considered valid if it is greater than zero and
 * less than `constant_pool_count`,
 * with the exception for constants of type long and double which consume two
 * entries.
 */
public class ConstantPool {

    private ConstantInfo[] constants;

    /**
     * Parses the constant pool from the given ClassReader.
     * The reader should be positioned at the start of the constant_pool_count
     * field.
     * 
     * @param reader ClassReader to read the constant pool data from.
     * @throws ClassFormatError if the constant pool data is malformed.
     */
    public ConstantPool(ClassReader reader) {
        int constantPoolCount = reader.readU2();
        this.constants = new ConstantInfo[constantPoolCount]; // 1-indexed, so size is count

        // The constant_pool table is 1-indexed.
        for (int i = 1; i < constantPoolCount; i++) {
            this.constants[i] = ConstantInfo.readConstant(reader);
            // Long and Double constants take up two entries in the constant pool.
            // If a CONSTANT_Long_info or CONSTANT_Double_info structure is the item at
            // index i in the constant_pool table, then the next usable item in the pool is
            // at index i+2.
            // The constant_pool item at index i+1 must be valid but unusable.
            int tag = this.constants[i].getTag();
            if (tag == ConstantInfo.CONSTANT_Long || tag == ConstantInfo.CONSTANT_Double) {
                i++; // Skip the next entry as it's taken by the Long/Double
            }
        }
    }

    /**
     * Retrieves a constant pool entry by its 1-based index.
     * 
     * @param index 1-based index into the constant pool.
     * @return The ConstantInfo entry at the given index, or null if the index is
     *         invalid or the entry is unused (e.g., the second slot for a
     *         Long/Double).
     * @throws IndexOutOfBoundsException if index is out of bounds (0 or >=
     *                                   constantPoolCount)
     */
    public ConstantInfo getConstant(int index) {
        if (index <= 0 || index >= constants.length) {
            // As per JVM spec, indices are > 0 and < constant_pool_count.
            // Throwing an error is more informative than returning null for out-of-bounds.
            throw new IndexOutOfBoundsException(
                    "Invalid constant pool index: " + index + ", count: " + constants.length);
        }
        ConstantInfo info = this.constants[index];
        if (info == null) {
            // This can happen for the second slot of a Long or Double, or if parsing failed
            // to fill an entry (though readConstant should throw).
            // Or, if a specific index was explicitly set to null due to an error.
            // Depending on strictness, could throw ClassFormatError here.
            // For now, returning null is consistent with some JVM behaviors for unused
            // slots.
            System.err.println("Warning: Constant pool entry at index " + index + " is null.");
        }
        return info;
    }

    /**
     * Retrieves a UTF-8 string from a CONSTANT_Utf8_info entry in the constant
     * pool.
     * 
     * @param index 1-based index of the CONSTANT_Utf8_info entry.
     * @return The String value.
     * @throws ClassFormatError if the entry at the index is not a
     *                          CONSTANT_Utf8_info or is invalid.
     */
    public String getUtf8(int index) {
        ConstantInfo info = getConstant(index);
        if (info instanceof ConstantInfo.ConstantUtf8) {
            return ((ConstantInfo.ConstantUtf8) info).getValue();
        } else {
            throw new ClassFormatError("Expected CONSTANT_Utf8_info at index " + index + " but found "
                    + (info != null ? info.getClass().getSimpleName() : "null"));
        }
    }

    /**
     * TODO: Implement this method
     * Get a class name from the constant pool
     * 
     * @param index Index of CONSTANT_Class entry
     * @return The class name
     */
    public String getClassName(int index) {
        // TODO: Get CONSTANT_Class entry, then get its name_index, then use
        // getUtf8(name_index)
        ConstantInfo.ConstantClass classInfo = (ConstantInfo.ConstantClass) getConstant(index);
        return getUtf8(classInfo.getNameIndex());
    }

    /**
     * Gets the name and type from a CONSTANT_NameAndType_info entry.
     * 
     * @param index 1-based index of the CONSTANT_NameAndType_info entry.
     * @return A String array where [0] is the name and [1] is the type descriptor.
     * @throws ClassFormatError if the entry at the index is not a
     *                          CONSTANT_NameAndType_info.
     */
    public String[] getNameAndType(int index) {
        ConstantInfo info = getConstant(index);
        if (info instanceof ConstantInfo.ConstantNameAndType) {
            ConstantInfo.ConstantNameAndType natInfo = (ConstantInfo.ConstantNameAndType) info;
            String name = getUtf8(natInfo.getNameIndex());
            String descriptor = getUtf8(natInfo.getDescriptorIndex());
            return new String[] { name, descriptor };
        } else {
            throw new ClassFormatError("Expected CONSTANT_NameAndType_info at index " + index + " but found "
                    + (info != null ? info.getClass().getSimpleName() : "null"));
        }
    }

    public int getCount() {
        return constants != null ? constants.length : 0;
    }
}