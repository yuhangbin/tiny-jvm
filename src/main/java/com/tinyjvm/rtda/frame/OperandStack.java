package com.tinyjvm.rtda.frame;

/**
 * Operand Stack for a Frame.
 * Values are stored in an array of Slot objects to handle different data types.
 */
public class OperandStack {
    private int top; // Points to the top of the stack, also indicates current size
    private Slot[] slots;

    public OperandStack(int maxStack) {
        if (maxStack > 0) {
            slots = new Slot[maxStack];
            for (int i = 0; i < maxStack; i++) {
                slots[i] = new Slot(); // Initialize each slot
            }
        }
        // If maxStack is 0, slots will be null, which is fine as top will be 0.
        // Operations will throw StackOverflowError or IllegalStateException
        // appropriately.
        this.top = 0;
    }

    public void pushInt(int val) {
        if (top >= slots.length) {
            throw new StackOverflowError("Operand stack overflow");
        }
        slots[top].num = val;
        slots[top].ref = null;
        top++;
    }

    public int popInt() {
        if (top <= 0) {
            throw new IllegalStateException("Operand stack underflow");
        }
        top--;
        // No need to clear slots[top].ref explicitly if type safety is maintained by
        // opcodes
        return slots[top].num;
    }

    public void pushFloat(float val) {
        if (top >= slots.length) {
            throw new StackOverflowError("Operand stack overflow");
        }
        slots[top].num = Float.floatToIntBits(val);
        slots[top].ref = null;
        top++;
    }

    public float popFloat() {
        if (top <= 0) {
            throw new IllegalStateException("Operand stack underflow");
        }
        top--;
        return Float.intBitsToFloat(slots[top].num);
    }

    public void pushLong(long val) {
        if (top + 1 >= slots.length) { // Long takes two slots
            throw new StackOverflowError("Operand stack overflow (long)");
        }
        slots[top].num = (int) val; // Low part
        slots[top].ref = null;
        slots[top + 1].num = (int) (val >> 32); // High part
        slots[top + 1].ref = null;
        top += 2;
    }

    public long popLong() {
        if (top <= 1) { // Long takes two slots
            throw new IllegalStateException("Operand stack underflow (long)");
        }
        top -= 2;
        int low = slots[top].num;
        int high = slots[top + 1].num;
        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    public void pushDouble(double val) {
        long bits = Double.doubleToLongBits(val);
        pushLong(bits); // Reuse long logic
    }

    public double popDouble() {
        return Double.longBitsToDouble(popLong()); // Reuse long logic
    }

    public void pushRef(Object ref) {
        if (top >= slots.length) {
            throw new StackOverflowError("Operand stack overflow (ref)");
        }
        slots[top].ref = ref;
        // slots[top].num can be left as is or zeroed if desired
        top++;
    }

    public Object popRef() {
        if (top <= 0) {
            throw new IllegalStateException("Operand stack underflow (ref)");
        }
        top--;
        Object ref = slots[top].ref;
        slots[top].ref = null; // Help GC
        return ref;
    }

    public int getSize() {
        return top;
    }

    // Helper class for a slot in the local variable array or operand stack
    // It can hold an integer (for int, float bits) or an object reference.
    // For long/double, two slots are used conceptually.
    private static class Slot {
        int num; // For int, boolean, byte, short, char, float (bits)
        Object ref; // For object references
    }
}