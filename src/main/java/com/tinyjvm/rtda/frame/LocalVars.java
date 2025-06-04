package com.tinyjvm.rtda.frame;

/**
 * Local Variables array for a Frame.
 * Values are stored in an array of Slot objects to handle different data types.
 * Note: boolean, byte, short, and char are converted to int before being
 * stored.
 */
public class LocalVars {
    private Slot[] slots;

    public LocalVars(int maxLocals) {
        if (maxLocals > 0) {
            slots = new Slot[maxLocals];
            for (int i = 0; i < maxLocals; i++) {
                slots[i] = new Slot(); // Initialize each slot
            }
        } else {
            slots = new Slot[0]; // Handle case where maxLocals might be 0
        }
    }

    public void setInt(int index, int val) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        slots[index].num = val;
        slots[index].ref = null; // Ensure it's not holding a stale reference
    }

    public int getInt(int index) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        return slots[index].num;
    }

    public void setFloat(int index, float val) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        slots[index].num = Float.floatToIntBits(val);
        slots[index].ref = null;
    }

    public float getFloat(int index) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        return Float.intBitsToFloat(slots[index].num);
    }

    public void setLong(int index, long val) {
        if (index < 0 || index + 1 >= slots.length) { // Long takes two slots
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars (long): " + index);
        }
        // Store high and low 32 bits. Order doesn't strictly matter as long as it's
        // consistent.
        // Simpler approach: just store the long directly if Slot can handle it or use
        // two int slots.
        // For now, let's assume Slot can hold a long or we use two int slots.
        // JVM Spec: long and double values occupy two consecutive local variable slots.
        // We'll store the raw bits in the first slot (index) for simplicity of Slot,
        // and potentially mark the second slot (index+1) as unusable or part of the
        // long.
        // A common way is to store the long in slots[index] and slots[index+1] as two
        // ints (high and low parts)
        // but our Slot class currently only has one `num` field.
        // Let's adapt Slot or make LocalVars more complex.
        // For simplicity, let's assume a Slot can hold a long or a reference.
        // This is a simplification. A real JVM would use two int-sized slots.
        // We'll adjust Slot later if needed or store as two ints.
        // Let's go with two ints for correctness with current Slot definition.
        slots[index].num = (int) (val >> 32); // High bits
        slots[index].ref = null;
        slots[index + 1].num = (int) val; // Low bits
        slots[index + 1].ref = null;
    }

    public long getLong(int index) {
        if (index < 0 || index + 1 >= slots.length) { // Long takes two slots
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars (long): " + index);
        }
        int high = slots[index].num;
        int low = slots[index + 1].num;
        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    public void setDouble(int index, double val) {
        if (index < 0 || index + 1 >= slots.length) { // Double takes two slots
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars (double): " + index);
        }
        setLong(index, Double.doubleToLongBits(val)); // Reuse long logic for storage
    }

    public double getDouble(int index) {
        if (index < 0 || index + 1 >= slots.length) { // Double takes two slots
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars (double): " + index);
        }
        return Double.longBitsToDouble(getLong(index));
    }

    public void setRef(int index, Object ref) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        slots[index].ref = ref;
        // slots[index].num could be set to 0 or a sentinel if needed, but ref takes
        // precedence
    }

    public Object getRef(int index) {
        if (index < 0 || index >= slots.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid index for LocalVars: " + index);
        }
        return slots[index].ref;
    }

    // Helper class for a slot in the local variable array or operand stack
    // It can hold an integer (for int, float bits) or an object reference.
    // For long/double, two slots are used conceptually.
    private static class Slot {
        int num; // For int, boolean, byte, short, char, float (bits)
        Object ref; // For object references
    }
}