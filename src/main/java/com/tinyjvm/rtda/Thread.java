package com.tinyjvm.rtda;

/**
 * Represents a thread of execution in the JVM.
 * Each thread has its own program counter (PC) and a JVM stack.
 */
public class Thread {
    private int pc; // Program Counter - often refers to the pc within the current frame
    private JvmStack stack; // JVM stack for this thread

    public Thread() {
        // Default stack size, can be made configurable later
        // For example, OpenJDK default is 1MB for 64-bit systems.
        // Let's use a smaller, fixed size for our tiny JVM, e.g., 1024 frames.
        this(1024);
    }

    public Thread(int maxStackFrames) {
        this.stack = new JvmStack(maxStackFrames);
        // pc is typically initialized when a frame is pushed with a method to execute.
        // Or it could represent the pc within the current frame.
        // For now, it can be 0, and we'll manage it with frames.
        this.pc = 0;
    }

    public int getPc() {
        // The PC should really be relative to the current method/frame.
        // If the stack is not empty, it should be the pc of the top frame.
        if (!stack.isEmpty()) {
            return stack.top().getNextPc();
        }
        return pc; // Fallback if stack is empty (e.g., before first method)
    }

    public void setPc(int pc) {
        // This should ideally set the pc of the top frame.
        if (!stack.isEmpty()) {
            stack.top().setNextPc(pc);
        } else {
            this.pc = pc; // If stack is empty, update the thread's pc directly
        }
    }

    public JvmStack getStack() {
        return stack;
    }

    public void pushFrame(Frame frame) {
        this.stack.push(frame);
    }

    public Frame popFrame() {
        return this.stack.pop();
    }

    public Frame currentFrame() {
        if (this.stack.isEmpty()) {
            return null;
        }
        return this.stack.top();
    }

    public boolean isStackEmpty() {
        return this.stack.isEmpty();
    }

    /**
     * Creates a new Frame for the given method and pushes it onto this thread's
     * stack.
     * 
     * @param method The method for which to create and push a frame.
     * @return The newly created and pushed frame.
     */
    public Frame newFrame(com.tinyjvm.classfile.MethodInfo method) {
        Frame frame = new Frame(this.stack, method);
        pushFrame(frame);
        return frame;
    }
}