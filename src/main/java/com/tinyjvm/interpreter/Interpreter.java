package com.tinyjvm.interpreter;

import com.tinyjvm.classfile.ConstantPool;
import com.tinyjvm.classfile.ConstantInfo;
import com.tinyjvm.classfile.MethodInfo;
import com.tinyjvm.rtda.Frame;
import com.tinyjvm.rtda.Thread;
import com.tinyjvm.rtda.ClassLoader;
import com.tinyjvm.interpreter.util.MethodDescriptorParser;
import java.util.List;

/**
 * The main bytecode interpreter.
 */
public class Interpreter {
    private ClassLoader classLoader;

    public Interpreter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Interprets the given method, starting execution in the provided thread.
     *
     * @param thread        The thread in which execution will occur.
     * @param initialMethod The first method to execute (e.g., main).
     * @param verbose       If true, print detailed execution logs.
     */
    public void interpret(Thread thread, MethodInfo initialMethod, boolean verbose) {
        if (this.classLoader == null) {
            System.err.println("Interpreter error: ClassLoader has not been initialized.");
            return;
        }
        if (initialMethod == null) {
            System.err.println("Interpreter error: Initial method cannot be null.");
            return;
        }
        if (initialMethod.getCodeAttribute() == null) {
            System.err.println("Interpreter error: Initial method " + initialMethod.getName()
                    + " has no Code attribute (cannot execute abstract or native methods directly yet).");
            return;
        }

        // Create a frame for the initial method and push it onto the thread's stack
        Frame initialFrame = thread.newFrame(initialMethod);

        if (verbose) {
            System.out.println(
                    "Starting JVM execution with method: " + initialMethod.getName() + initialMethod.getDescriptor());
            System.out
                    .println("Class: [Unable to determine from MethodInfo alone without ClassFile's this_class_index]"); // Placeholder
        }

        // Start the main execution loop
        loop(thread, verbose);

        if (verbose) {
            System.out.println("JVM execution finished.");
        }
    }

    private void loop(Thread thread, boolean verbose) {
        while (!thread.isStackEmpty()) {
            Frame currentFrame = thread.currentFrame();
            if (currentFrame == null) {
                System.err.println("Interpreter loop error: Current frame is null despite non-empty stack.");
                break;
            }

            int pc = currentFrame.getNextPc();
            MethodInfo methodInfo = currentFrame.getMethod();
            ConstantPool cp = methodInfo.getConstantPool(); // Get CP once per iteration for convenience
            byte[] bytecode = methodInfo.getCodeAttribute().getCode();

            if (pc >= bytecode.length) {
                if (verbose) {
                    System.out.println("Reached end of bytecode for method: " + methodInfo.getName()
                            + ", frame will be popped by return instruction or implicitly.");
                }
                thread.popFrame();
                continue;
            }

            int opcode = bytecode[pc] & 0xFF;
            // Default PC advancement for 1-byte opcodes. Multi-byte opcodes MUST call
            // setNextPc explicitly.
            currentFrame.setNextPc(pc + 1);

            if (verbose) {
                System.out.printf("[pc:%04d] [op:0x%02x] Executing opcode... (Frame: %s.%s%s)\n",
                        pc, opcode,
                        "[ClassName]", // Placeholder, requires ClassFile.this_class to resolve properly
                        methodInfo.getName(),
                        methodInfo.getDescriptor());
            }

            switch (opcode) {
                case 0x00: // nop
                    break;
                case 0x01: // aconst_null
                    currentFrame.getOperandStack().pushRef(null);
                    break;
                case 0x02: // iconst_m1
                    currentFrame.getOperandStack().pushInt(-1);
                    break;
                case 0x03: // iconst_0
                    currentFrame.getOperandStack().pushInt(0);
                    break;
                case 0x04: // iconst_1
                    currentFrame.getOperandStack().pushInt(1);
                    break;
                case 0x05: // iconst_2
                    currentFrame.getOperandStack().pushInt(2);
                    break;
                case 0x06: // iconst_3
                    currentFrame.getOperandStack().pushInt(3);
                    break;
                case 0x07: // iconst_4
                    currentFrame.getOperandStack().pushInt(4);
                    break;
                case 0x08: // iconst_5
                    currentFrame.getOperandStack().pushInt(5);
                    break;
                case 0x09: // lconst_0
                    currentFrame.getOperandStack().pushLong(0L);
                    break;
                case 0x0a: // lconst_1
                    currentFrame.getOperandStack().pushLong(1L);
                    break;
                case 0x0b: // fconst_0
                    currentFrame.getOperandStack().pushFloat(0.0f);
                    break;
                case 0x0c: // fconst_1
                    currentFrame.getOperandStack().pushFloat(1.0f);
                    break;
                case 0x0d: // fconst_2
                    currentFrame.getOperandStack().pushFloat(2.0f);
                    break;
                case 0x0e: // dconst_0
                    currentFrame.getOperandStack().pushDouble(0.0);
                    break;
                case 0x0f: // dconst_1
                    currentFrame.getOperandStack().pushDouble(1.0);
                    break;

                case 0x10: { // bipush <byte>
                    byte byteValue = bytecode[pc + 1];
                    currentFrame.getOperandStack().pushInt((int) byteValue);
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x11: { // sipush <byte1> <byte2>
                    byte byte1 = bytecode[pc + 1];
                    byte byte2 = bytecode[pc + 2];
                    short shortValue = (short) ((byte1 << 8) | (byte2 & 0xFF));
                    currentFrame.getOperandStack().pushInt((int) shortValue);
                    currentFrame.setNextPc(pc + 3);
                    break;
                }
                case 0x12: { // ldc <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    Object constant = cp.getConstant(index);
                    if (constant instanceof ConstantInfo.ConstantInteger) {
                        currentFrame.getOperandStack().pushInt(((ConstantInfo.ConstantInteger) constant).getValue());
                    } else if (constant instanceof ConstantInfo.ConstantFloat) {
                        float floatVal = ((ConstantInfo.ConstantFloat) constant).getValue();
                        currentFrame.getOperandStack().pushFloat(floatVal);
                    } else if (constant instanceof ConstantInfo.ConstantString) {
                        String str = cp.getUtf8(((ConstantInfo.ConstantString) constant).getStringIndex());
                        currentFrame.getOperandStack().pushRef(str);
                    } else {
                        System.err.printf("ldc error: Unsupported constant pool type at index %d: %s\n", index,
                                constant.getClass().getName());
                        thread.popFrame();
                        return;
                    }
                    currentFrame.setNextPc(pc + 2);
                    break;
                }

                // Load instructions
                case 0x15: { // iload <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getOperandStack().pushInt(currentFrame.getLocalVariables().getInt(index));
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x16: { // lload <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getOperandStack().pushLong(currentFrame.getLocalVariables().getLong(index));
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x17: { // fload <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getOperandStack().pushFloat(currentFrame.getLocalVariables().getFloat(index));
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x18: { // dload <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getOperandStack().pushDouble(currentFrame.getLocalVariables().getDouble(index));
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x19: { // aload <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getOperandStack().pushRef(currentFrame.getLocalVariables().getRef(index));
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x1a: // iload_0
                    currentFrame.getOperandStack().pushInt(currentFrame.getLocalVariables().getInt(0));
                    break;
                case 0x1b: // iload_1
                    currentFrame.getOperandStack().pushInt(currentFrame.getLocalVariables().getInt(1));
                    break;
                case 0x1c: // iload_2
                    currentFrame.getOperandStack().pushInt(currentFrame.getLocalVariables().getInt(2));
                    break;
                case 0x1d: // iload_3
                    currentFrame.getOperandStack().pushInt(currentFrame.getLocalVariables().getInt(3));
                    break;
                case 0x1e: // lload_0
                    currentFrame.getOperandStack().pushLong(currentFrame.getLocalVariables().getLong(0));
                    break;
                case 0x1f: // lload_1
                    currentFrame.getOperandStack().pushLong(currentFrame.getLocalVariables().getLong(1));
                    break;
                case 0x20: // lload_2
                    currentFrame.getOperandStack().pushLong(currentFrame.getLocalVariables().getLong(2));
                    break;
                case 0x21: // lload_3
                    currentFrame.getOperandStack().pushLong(currentFrame.getLocalVariables().getLong(3));
                    break;
                case 0x22: // fload_0
                    currentFrame.getOperandStack().pushFloat(currentFrame.getLocalVariables().getFloat(0));
                    break;
                case 0x23: // fload_1
                    currentFrame.getOperandStack().pushFloat(currentFrame.getLocalVariables().getFloat(1));
                    break;
                case 0x24: // fload_2
                    currentFrame.getOperandStack().pushFloat(currentFrame.getLocalVariables().getFloat(2));
                    break;
                case 0x25: // fload_3
                    currentFrame.getOperandStack().pushFloat(currentFrame.getLocalVariables().getFloat(3));
                    break;
                case 0x26: // dload_0
                    currentFrame.getOperandStack().pushDouble(currentFrame.getLocalVariables().getDouble(0));
                    break;
                case 0x27: // dload_1
                    currentFrame.getOperandStack().pushDouble(currentFrame.getLocalVariables().getDouble(1));
                    break;
                case 0x28: // dload_2
                    currentFrame.getOperandStack().pushDouble(currentFrame.getLocalVariables().getDouble(2));
                    break;
                case 0x29: // dload_3
                    currentFrame.getOperandStack().pushDouble(currentFrame.getLocalVariables().getDouble(3));
                    break;
                case 0x2a: // aload_0
                    currentFrame.getOperandStack().pushRef(currentFrame.getLocalVariables().getRef(0));
                    break;
                case 0x2b: // aload_1
                    currentFrame.getOperandStack().pushRef(currentFrame.getLocalVariables().getRef(1));
                    break;
                case 0x2c: // aload_2
                    currentFrame.getOperandStack().pushRef(currentFrame.getLocalVariables().getRef(2));
                    break;
                case 0x2d: // aload_3
                    currentFrame.getOperandStack().pushRef(currentFrame.getLocalVariables().getRef(3));
                    break;

                // Store instructions
                case 0x36: { // istore <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getLocalVariables().setInt(index, currentFrame.getOperandStack().popInt());
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x37: { // lstore <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getLocalVariables().setLong(index, currentFrame.getOperandStack().popLong());
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x38: { // fstore <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getLocalVariables().setFloat(index, currentFrame.getOperandStack().popFloat());
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x39: { // dstore <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getLocalVariables().setDouble(index, currentFrame.getOperandStack().popDouble());
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x3a: { // astore <index>
                    int index = bytecode[pc + 1] & 0xFF;
                    currentFrame.getLocalVariables().setRef(index, currentFrame.getOperandStack().popRef());
                    currentFrame.setNextPc(pc + 2);
                    break;
                }
                case 0x3b: // istore_0
                    currentFrame.getLocalVariables().setInt(0, currentFrame.getOperandStack().popInt());
                    break;
                case 0x3c: // istore_1
                    currentFrame.getLocalVariables().setInt(1, currentFrame.getOperandStack().popInt());
                    break;
                case 0x3d: // istore_2
                    currentFrame.getLocalVariables().setInt(2, currentFrame.getOperandStack().popInt());
                    break;
                case 0x3e: // istore_3
                    currentFrame.getLocalVariables().setInt(3, currentFrame.getOperandStack().popInt());
                    break;
                case 0x3f: // lstore_0
                    currentFrame.getLocalVariables().setLong(0, currentFrame.getOperandStack().popLong());
                    break;
                case 0x40: // lstore_1
                    currentFrame.getLocalVariables().setLong(1, currentFrame.getOperandStack().popLong());
                    break;
                case 0x41: // lstore_2
                    currentFrame.getLocalVariables().setLong(2, currentFrame.getOperandStack().popLong());
                    break;
                case 0x42: // lstore_3
                    currentFrame.getLocalVariables().setLong(3, currentFrame.getOperandStack().popLong());
                    break;
                case 0x43: // fstore_0
                    currentFrame.getLocalVariables().setFloat(0, currentFrame.getOperandStack().popFloat());
                    break;
                case 0x44: // fstore_1
                    currentFrame.getLocalVariables().setFloat(1, currentFrame.getOperandStack().popFloat());
                    break;
                case 0x45: // fstore_2
                    currentFrame.getLocalVariables().setFloat(2, currentFrame.getOperandStack().popFloat());
                    break;
                case 0x46: // fstore_3
                    currentFrame.getLocalVariables().setFloat(3, currentFrame.getOperandStack().popFloat());
                    break;
                case 0x47: // dstore_0
                    currentFrame.getLocalVariables().setDouble(0, currentFrame.getOperandStack().popDouble());
                    break;
                case 0x48: // dstore_1
                    currentFrame.getLocalVariables().setDouble(1, currentFrame.getOperandStack().popDouble());
                    break;
                case 0x49: // dstore_2
                    currentFrame.getLocalVariables().setDouble(2, currentFrame.getOperandStack().popDouble());
                    break;
                case 0x4a: // dstore_3
                    currentFrame.getLocalVariables().setDouble(3, currentFrame.getOperandStack().popDouble());
                    break;
                case 0x4b: // astore_0
                    currentFrame.getLocalVariables().setRef(0, currentFrame.getOperandStack().popRef());
                    break;
                case 0x4c: // astore_1
                    currentFrame.getLocalVariables().setRef(1, currentFrame.getOperandStack().popRef());
                    break;
                case 0x4d: // astore_2
                    currentFrame.getLocalVariables().setRef(2, currentFrame.getOperandStack().popRef());
                    break;
                case 0x4e: // astore_3
                    currentFrame.getLocalVariables().setRef(3, currentFrame.getOperandStack().popRef());
                    break;

                // Stack manipulation
                case 0x57: // pop
                    currentFrame.getOperandStack().popInt(); // Pop and discard top value
                    break;
                case 0x58: // pop2 (pop two single-word values or one double-word value)
                    currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().popInt();
                    break;
                case 0x59: // dup
                    int value = currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().pushInt(value);
                    currentFrame.getOperandStack().pushInt(value);
                    break;
                case 0x5c: // dup2 (duplicate top two single-word values)
                    int value2 = currentFrame.getOperandStack().popInt();
                    int value1 = currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().pushInt(value1);
                    currentFrame.getOperandStack().pushInt(value2);
                    currentFrame.getOperandStack().pushInt(value1);
                    currentFrame.getOperandStack().pushInt(value2);
                    break;

                // Arithmetic instructions
                case 0x60: // iadd
                    int b = currentFrame.getOperandStack().popInt();
                    int a = currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().pushInt(a + b);
                    break;
                case 0x61: // ladd
                    long lb = currentFrame.getOperandStack().popLong();
                    long la = currentFrame.getOperandStack().popLong();
                    currentFrame.getOperandStack().pushLong(la + lb);
                    break;
                case 0x62: // fadd
                    float fb = currentFrame.getOperandStack().popFloat();
                    float fa = currentFrame.getOperandStack().popFloat();
                    currentFrame.getOperandStack().pushFloat(fa + fb);
                    break;
                case 0x63: // dadd
                    double db = currentFrame.getOperandStack().popDouble();
                    double da = currentFrame.getOperandStack().popDouble();
                    currentFrame.getOperandStack().pushDouble(da + db);
                    break;
                case 0x64: // isub
                    int sb = currentFrame.getOperandStack().popInt();
                    int sa = currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().pushInt(sa - sb);
                    break;
                case 0x65: // lsub
                    long slb = currentFrame.getOperandStack().popLong();
                    long sla = currentFrame.getOperandStack().popLong();
                    currentFrame.getOperandStack().pushLong(sla - slb);
                    break;
                case 0x66: // fsub
                    float sfb = currentFrame.getOperandStack().popFloat();
                    float sfa = currentFrame.getOperandStack().popFloat();
                    currentFrame.getOperandStack().pushFloat(sfa - sfb);
                    break;
                case 0x67: // dsub
                    double sdb = currentFrame.getOperandStack().popDouble();
                    double sda = currentFrame.getOperandStack().popDouble();
                    currentFrame.getOperandStack().pushDouble(sda - sdb);
                    break;
                case 0x68: // imul
                    int mb = currentFrame.getOperandStack().popInt();
                    int ma = currentFrame.getOperandStack().popInt();
                    currentFrame.getOperandStack().pushInt(ma * mb);
                    break;
                case 0x69: // lmul
                    long mlb = currentFrame.getOperandStack().popLong();
                    long mla = currentFrame.getOperandStack().popLong();
                    currentFrame.getOperandStack().pushLong(mla * mlb);
                    break;
                case 0x6a: // fmul
                    float mfb = currentFrame.getOperandStack().popFloat();
                    float mfa = currentFrame.getOperandStack().popFloat();
                    currentFrame.getOperandStack().pushFloat(mfa * mfb);
                    break;
                case 0x6b: // dmul
                    double mdb = currentFrame.getOperandStack().popDouble();
                    double mda = currentFrame.getOperandStack().popDouble();
                    currentFrame.getOperandStack().pushDouble(mda * mdb);
                    break;
                case 0x6c: // idiv
                    int divb = currentFrame.getOperandStack().popInt();
                    int diva = currentFrame.getOperandStack().popInt();
                    if (divb == 0) {
                        System.err.println("ArithmeticException: Division by zero");
                        return; // TODO: Throw ArithmeticException
                    }
                    currentFrame.getOperandStack().pushInt(diva / divb);
                    break;
                case 0x6d: // ldiv
                    long ldivb = currentFrame.getOperandStack().popLong();
                    long ldiva = currentFrame.getOperandStack().popLong();
                    if (ldivb == 0) {
                        System.err.println("ArithmeticException: Division by zero");
                        return; // TODO: Throw ArithmeticException
                    }
                    currentFrame.getOperandStack().pushLong(ldiva / ldivb);
                    break;
                case 0x6e: // fdiv
                    float fdivb = currentFrame.getOperandStack().popFloat();
                    float fdiva = currentFrame.getOperandStack().popFloat();
                    currentFrame.getOperandStack().pushFloat(fdiva / fdivb);
                    break;
                case 0x6f: // ddiv
                    double ddivb = currentFrame.getOperandStack().popDouble();
                    double ddiva = currentFrame.getOperandStack().popDouble();
                    currentFrame.getOperandStack().pushDouble(ddiva / ddivb);
                    break;
                case 0x70: // irem
                    int remb = currentFrame.getOperandStack().popInt();
                    int rema = currentFrame.getOperandStack().popInt();
                    if (remb == 0) {
                        System.err.println("ArithmeticException: Division by zero");
                        return; // TODO: Throw ArithmeticException
                    }
                    currentFrame.getOperandStack().pushInt(rema % remb);
                    break;
                case 0x74: // ineg
                    currentFrame.getOperandStack().pushInt(-currentFrame.getOperandStack().popInt());
                    break;
                case 0x75: // lneg
                    currentFrame.getOperandStack().pushLong(-currentFrame.getOperandStack().popLong());
                    break;
                case 0x76: // fneg
                    currentFrame.getOperandStack().pushFloat(-currentFrame.getOperandStack().popFloat());
                    break;
                case 0x77: // dneg
                    currentFrame.getOperandStack().pushDouble(-currentFrame.getOperandStack().popDouble());
                    break;

                // Comparison and branching
                case 0x99: { // ifeq <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val == 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9a: { // ifne <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val != 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9b: { // iflt <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val < 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9c: { // ifge <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val >= 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9d: { // ifgt <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val > 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9e: { // ifle <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val = currentFrame.getOperandStack().popInt();
                    if (val <= 0) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0x9f: { // if_icmpeq <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 == val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa0: { // if_icmpne <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 != val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa1: { // if_icmplt <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 < val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa2: { // if_icmpge <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 >= val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa3: { // if_icmpgt <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 > val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa4: { // if_icmple <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    int val2 = currentFrame.getOperandStack().popInt();
                    int val1 = currentFrame.getOperandStack().popInt();
                    if (val1 <= val2) {
                        currentFrame.setNextPc(pc + offset);
                    } else {
                        currentFrame.setNextPc(pc + 3);
                    }
                    break;
                }
                case 0xa7: { // goto <branchbyte1> <branchbyte2>
                    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    if (offset > 32767)
                        offset -= 65536; // Convert to signed
                    currentFrame.setNextPc(pc + offset);
                    break;
                }

                // Return instructions
                case 0xac: // ireturn
                    int retValue = currentFrame.getOperandStack().popInt();
                    thread.popFrame();
                    if (!thread.isStackEmpty()) {
                        thread.currentFrame().getOperandStack().pushInt(retValue);
                    }
                    break;
                case 0xad: // lreturn
                    long lretValue = currentFrame.getOperandStack().popLong();
                    thread.popFrame();
                    if (!thread.isStackEmpty()) {
                        thread.currentFrame().getOperandStack().pushLong(lretValue);
                    }
                    break;
                case 0xae: // freturn
                    float fretValue = currentFrame.getOperandStack().popFloat();
                    thread.popFrame();
                    if (!thread.isStackEmpty()) {
                        thread.currentFrame().getOperandStack().pushFloat(fretValue);
                    }
                    break;
                case 0xaf: // dreturn
                    double dretValue = currentFrame.getOperandStack().popDouble();
                    thread.popFrame();
                    if (!thread.isStackEmpty()) {
                        thread.currentFrame().getOperandStack().pushDouble(dretValue);
                    }
                    break;
                case 0xb0: // areturn
                    Object aretValue = currentFrame.getOperandStack().popRef();
                    thread.popFrame();
                    if (!thread.isStackEmpty()) {
                        thread.currentFrame().getOperandStack().pushRef(aretValue);
                    }
                    break;
                case 0xb1: // return (void)
                    thread.popFrame();
                    break;

                // Method Invocation Instructions
                case 0xb7: { // invokespecial <indexbyte1> <indexbyte2> (constructor calls, private methods,
                             // superclass methods)
                    int methodRefIndex = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    ConstantInfo.ConstantMethodref methodRef = (ConstantInfo.ConstantMethodref) cp
                            .getConstant(methodRefIndex);
                    String className = cp.getClassName(methodRef.getClassIndex());
                    ConstantInfo.ConstantNameAndType nameAndType = (ConstantInfo.ConstantNameAndType) cp
                            .getConstant(methodRef.getNameAndTypeIndex());
                    String methodName = cp.getUtf8(nameAndType.getNameIndex());
                    String methodDescriptor = cp.getUtf8(nameAndType.getDescriptorIndex());

                    if (verbose) {
                        System.out.printf("invokespecial: %s.%s%s\n", className, methodName, methodDescriptor);
                    }

                    // Special case: skip java.lang.Object.<init> since we don't have the standard
                    // library
                    if ("java/lang/Object".equals(className) && "<init>".equals(methodName)) {
                        // Pop the 'this' reference from the stack and just continue
                        currentFrame.getOperandStack().popRef();
                        System.out.println("Note: Skipping java.lang.Object.<init> (not available in tiny JVM)");
                        currentFrame.setNextPc(pc + 3);
                        break;
                    }

                    try {
                        com.tinyjvm.rtda.RuntimeClass targetClass = this.classLoader
                                .loadClass(className.replace('/', '.'));
                        if (targetClass == null) {
                            System.err.println("invokespecial error: Could not load class " + className);
                            return;
                        }
                        MethodInfo targetMethod = targetClass.findMethod(methodName, methodDescriptor);
                        if (targetMethod == null) {
                            System.err.printf("invokespecial error: Method %s%s not found in class %s\n", methodName,
                                    methodDescriptor, className);
                            return;
                        }

                        Frame newFrame = new Frame(thread.getStack(), targetMethod);

                        List<String> paramTypes = MethodDescriptorParser.parseParameterTypes(methodDescriptor);
                        Object[] argsToPass = new Object[paramTypes.size()];

                        // Pop arguments from caller's operand stack in reverse order
                        for (int i = paramTypes.size() - 1; i >= 0; i--) {
                            String paramType = paramTypes.get(i);
                            if (paramType.equals("I") || paramType.equals("S") || paramType.equals("B")
                                    || paramType.equals("C") || paramType.equals("Z")) {
                                argsToPass[i] = currentFrame.getOperandStack().popInt();
                            } else if (paramType.equals("F")) {
                                argsToPass[i] = currentFrame.getOperandStack().popFloat();
                            } else if (paramType.equals("J")) {
                                argsToPass[i] = currentFrame.getOperandStack().popLong();
                            } else if (paramType.equals("D")) {
                                argsToPass[i] = currentFrame.getOperandStack().popDouble();
                            } else if (paramType.startsWith("L") || paramType.startsWith("[")) {
                                argsToPass[i] = currentFrame.getOperandStack().popRef();
                            } else {
                                System.err.println("invokespecial error: Unknown parameter type '" + paramType
                                        + "' in descriptor: " + methodDescriptor);
                                return;
                            }
                        }

                        // Pop the 'this' reference for instance methods (constructor and instance
                        // methods)
                        Object thisRef = currentFrame.getOperandStack().popRef();

                        // Set up local variables in the new frame
                        int currentLocalVarIndex = 0;

                        // For instance methods, 'this' goes in local variable 0
                        newFrame.getLocalVariables().setRef(currentLocalVarIndex++, thisRef);

                        // Then place the method arguments
                        for (int i = 0; i < paramTypes.size(); i++) {
                            String paramType = paramTypes.get(i);
                            Object arg = argsToPass[i];
                            if (paramType.equals("I") || paramType.equals("S") || paramType.equals("B")
                                    || paramType.equals("C") || paramType.equals("Z")) {
                                newFrame.getLocalVariables().setInt(currentLocalVarIndex++, (Integer) arg);
                            } else if (paramType.equals("F")) {
                                newFrame.getLocalVariables().setFloat(currentLocalVarIndex++, (Float) arg);
                            } else if (paramType.equals("J")) {
                                newFrame.getLocalVariables().setLong(currentLocalVarIndex, (Long) arg);
                                currentLocalVarIndex += 2;
                            } else if (paramType.equals("D")) {
                                newFrame.getLocalVariables().setDouble(currentLocalVarIndex, (Double) arg);
                                currentLocalVarIndex += 2;
                            } else if (paramType.startsWith("L") || paramType.startsWith("[")) {
                                newFrame.getLocalVariables().setRef(currentLocalVarIndex++, arg);
                            } else {
                                System.err.println(
                                        "invokespecial error: Internal - Unknown parameter type during local var setting: "
                                                + paramType);
                                return;
                            }
                        }

                        thread.pushFrame(newFrame);

                    } catch (ClassNotFoundException e) {
                        System.err.println(
                                "invokespecial error: ClassNotFoundException for " + className + ": " + e.getMessage());
                        return;
                    } catch (ClassFormatError e) {
                        System.err.println(
                                "invokespecial error: ClassFormatError for " + className + ": " + e.getMessage());
                        return;
                    } catch (Exception e) {
                        System.err.println("invokespecial error: Unexpected exception during method invocation: "
                                + e.getMessage());
                        e.printStackTrace();
                        return;
                    }

                    currentFrame.setNextPc(pc + 3);
                    break;
                }
                case 0xb8: { // invokestatic <indexbyte1> <indexbyte2>
                    int methodRefIndex = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
                    ConstantInfo.ConstantMethodref methodRef = (ConstantInfo.ConstantMethodref) cp
                            .getConstant(methodRefIndex);
                    String className = cp.getClassName(methodRef.getClassIndex());
                    ConstantInfo.ConstantNameAndType nameAndType = (ConstantInfo.ConstantNameAndType) cp
                            .getConstant(methodRef.getNameAndTypeIndex());
                    String methodName = cp.getUtf8(nameAndType.getNameIndex());
                    String methodDescriptor = cp.getUtf8(nameAndType.getDescriptorIndex());

                    if (verbose) {
                        System.out.printf("invokestatic: %s.%s%s\n", className, methodName, methodDescriptor);
                    }

                    try {
                        com.tinyjvm.rtda.RuntimeClass targetClass = this.classLoader
                                .loadClass(className.replace('/', '.'));
                        if (targetClass == null) {
                            System.err.println("invokestatic error: Could not load class " + className);
                            return;
                        }
                        MethodInfo targetMethod = targetClass.findMethod(methodName, methodDescriptor);
                        if (targetMethod == null) {
                            System.err.printf("invokestatic error: Method %s%s not found in class %s\n", methodName,
                                    methodDescriptor, className);
                            return;
                        }
                        if ((targetMethod.getAccessFlags() & 0x0008) == 0) {
                            System.err.printf("invokestatic error: Method %s%s in class %s is not static\n", methodName,
                                    methodDescriptor, className);
                            return;
                        }

                        Frame newFrame = new Frame(thread.getStack(), targetMethod);

                        List<String> paramTypes = MethodDescriptorParser.parseParameterTypes(methodDescriptor);
                        Object[] argsToPass = new Object[paramTypes.size()];

                        for (int i = paramTypes.size() - 1; i >= 0; i--) {
                            String paramType = paramTypes.get(i);
                            if (paramType.equals("I") || paramType.equals("S") || paramType.equals("B")
                                    || paramType.equals("C") || paramType.equals("Z")) {
                                argsToPass[i] = currentFrame.getOperandStack().popInt();
                            } else if (paramType.equals("F")) {
                                argsToPass[i] = currentFrame.getOperandStack().popFloat();
                            } else if (paramType.equals("J")) {
                                argsToPass[i] = currentFrame.getOperandStack().popLong();
                            } else if (paramType.equals("D")) {
                                argsToPass[i] = currentFrame.getOperandStack().popDouble();
                            } else if (paramType.startsWith("L") || paramType.startsWith("[")) {
                                argsToPass[i] = currentFrame.getOperandStack().popRef();
                            } else {
                                System.err.println("invokestatic error: Unknown parameter type '" + paramType
                                        + "' in descriptor: " + methodDescriptor);
                                return;
                            }
                        }

                        int currentLocalVarIndex = 0;
                        for (int i = 0; i < paramTypes.size(); i++) {
                            String paramType = paramTypes.get(i);
                            Object arg = argsToPass[i];
                            if (paramType.equals("I") || paramType.equals("S") || paramType.equals("B")
                                    || paramType.equals("C") || paramType.equals("Z")) {
                                newFrame.getLocalVariables().setInt(currentLocalVarIndex++, (Integer) arg);
                            } else if (paramType.equals("F")) {
                                newFrame.getLocalVariables().setFloat(currentLocalVarIndex++, (Float) arg);
                            } else if (paramType.equals("J")) {
                                newFrame.getLocalVariables().setLong(currentLocalVarIndex, (Long) arg);
                                currentLocalVarIndex += 2;
                            } else if (paramType.equals("D")) {
                                newFrame.getLocalVariables().setDouble(currentLocalVarIndex, (Double) arg);
                                currentLocalVarIndex += 2;
                            } else if (paramType.startsWith("L") || paramType.startsWith("[")) {
                                newFrame.getLocalVariables().setRef(currentLocalVarIndex++, arg);
                            } else {
                                System.err.println(
                                        "invokestatic error: Internal - Unknown parameter type during local var setting: "
                                                + paramType);
                                return;
                            }
                        }

                        thread.pushFrame(newFrame);

                    } catch (ClassNotFoundException e) {
                        System.err.println(
                                "invokestatic error: ClassNotFoundException for " + className + ": " + e.getMessage());
                        return;
                    } catch (ClassFormatError e) {
                        System.err.println(
                                "invokestatic error: ClassFormatError for " + className + ": " + e.getMessage());
                        return;
                    } catch (Exception e) {
                        System.err.println(
                                "invokestatic error: Unexpected exception during method invocation: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }

                    currentFrame.setNextPc(pc + 3);
                    break;
                }

                default:
                    System.err.printf("Unsupported opcode: 0x%02x at pc %d in method %s\n", opcode, pc,
                            methodInfo.getName());
                    System.err.println("Halting due to unsupported opcode.");
                    while (!thread.isStackEmpty())
                        thread.popFrame();
                    return;
            }

            if (thread.isStackEmpty()) {
                if (verbose)
                    System.out.println("Stack is empty, exiting loop.");
                break;
            }
        }
    }
}