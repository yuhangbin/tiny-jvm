package com.tinyjvm.testprogram;

/**
 * A simple test program for our tiny JVM.
 * This program uses only basic operations that our JVM should support:
 * - Integer constants and arithmetic
 * - Local variables
 * - Static method calls
 * - Return values
 */
public class SimpleTest {

    public static void main(String[] args) {
        int result = calculate();
        // Note: We don't have System.out.println yet, so we'll just return
    }

    /**
     * A simple method that does basic arithmetic.
     * Uses: iconst, istore, iload, iadd, ireturn
     */
    public static int calculate() {
        int a = 10; // iconst_10, istore_0
        int b = 20; // bipush 20, istore_1
        int c = a + b; // iload_0, iload_1, iadd, istore_2
        return c; // iload_2, ireturn
    }
}