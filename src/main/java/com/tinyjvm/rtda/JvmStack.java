package com.tinyjvm.rtda;

import java.util.Stack; // Using java.util.Stack for simplicity for now

/**
 * JVM Stack for a single thread. It holds Frames.
 * Each thread has its own JVM Stack.
 */
public class JvmStack {
    private int maxSize;
    private Stack<Frame> frames; // java.util.Stack is synchronized, but we might replace it later

    public JvmStack(int maxSize) {
        this.maxSize = maxSize;
        this.frames = new Stack<>();
    }

    /**
     * Pushes a frame onto the stack.
     * 
     * @param frame The frame to push.
     * @throws StackOverflowError if the stack is full.
     */
    public void push(Frame frame) {
        if (frames.size() >= maxSize) {
            throw new StackOverflowError("JVM stack overflow");
        }
        frames.push(frame);
    }

    /**
     * Pops a frame from the stack.
     * 
     * @return The popped frame.
     * @throws IllegalStateException if the stack is empty.
     */
    public Frame pop() {
        if (frames.isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty JVM stack");
        }
        return frames.pop();
    }

    /**
     * Peeks at the top frame of the stack without removing it.
     * 
     * @return The top frame.
     * @throws IllegalStateException if the stack is empty.
     */
    public Frame top() {
        if (frames.isEmpty()) {
            throw new IllegalStateException("Cannot peek into an empty JVM stack");
        }
        return frames.peek();
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public int getSize() {
        return frames.size();
    }

    // Potentially add a clear() method or other utilities later
}