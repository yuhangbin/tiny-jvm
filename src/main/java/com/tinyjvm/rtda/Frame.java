package com.tinyjvm.rtda;

import com.tinyjvm.rtda.frame.LocalVars;
import com.tinyjvm.rtda.frame.OperandStack;
import com.tinyjvm.classfile.MethodInfo;
import com.tinyjvm.classfile.AttributeInfo;

/**
 * Frame represents a single method invocation.
 * Each frame has its own local variables array, operand stack, and a reference
 * to the method being executed.
 */
public class Frame {
    // private Frame lower; // We are using JvmStack which manages the linked list
    // of frames
    private LocalVars localVariables;
    private OperandStack operandStack;
    private MethodInfo method;
    private int nextPc; // Program Counter for the next instruction to be executed in this method
    private JvmStack threadStack; // Reference to the stack this frame belongs to

    public Frame(JvmStack threadStack, MethodInfo method) {
        this.threadStack = threadStack;
        this.method = method;

        if (method == null) {
            // For testing purposes, use default sizes when method is null
            this.localVariables = new LocalVars(10); // Default size for testing
            this.operandStack = new OperandStack(10); // Default size for testing
        } else {
            AttributeInfo.CodeAttribute codeAttribute = method.getCodeAttribute();
            if (codeAttribute == null) {
                // This might happen for native methods, though we don't support them yet.
                // Or if a method has no Code attribute (e.g., abstract methods - but they
                // wouldn't be invoked directly like this).
                // For now, let's assume a valid CodeAttribute if a frame is being created for
                // execution.
                // A more robust solution would be to handle this based on method type.
                // If it's a native method, it might not have maxLocals/maxStack in the same
                // way.
                // For now, let's throw an error or assign default small sizes if it's null,
                // but ideally this should not happen for a non-native, non-abstract method we
                // try to execute.
                throw new IllegalArgumentException(
                        "Cannot create frame for method without CodeAttribute: " + method.getName());
            }
            this.localVariables = new LocalVars(codeAttribute.getMaxLocals());
            this.operandStack = new OperandStack(codeAttribute.getMaxStack());
        }
        this.nextPc = 0;
    }

    // Accessors
    public LocalVars getLocalVariables() {
        return localVariables;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public int getNextPc() {
        return nextPc;
    }

    public void setNextPc(int nextPc) {
        this.nextPc = nextPc;
    }

    public JvmStack getThreadStack() {
        return threadStack;
    }

    // The Frame.lower field might not be strictly necessary if JvmStack manages the
    // sequence.
    // However, it can be useful for debuggers or certain stack walking operations.
    // For now, JvmStack handles push/pop/top directly.
}