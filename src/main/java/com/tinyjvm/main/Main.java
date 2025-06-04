package com.tinyjvm.main;

import com.tinyjvm.cmd.Cmd;
import com.tinyjvm.classpath.Classpath;
import com.tinyjvm.rtda.ClassLoader;
import com.tinyjvm.rtda.MethodArea;
import com.tinyjvm.rtda.RuntimeClass;
import com.tinyjvm.rtda.Thread;
import com.tinyjvm.classfile.MethodInfo;
import com.tinyjvm.interpreter.Interpreter;

/**
 * Main entry point for the Tiny JVM
 * This is where everything starts - like the java command
 */
public class Main {

    public static void main(String[] args) {
        // Parse command line arguments
        Cmd cmd = Cmd.parseCmd(args);

        if (cmd == null) {
            return; // Invalid arguments or help was shown
        }

        // Start the JVM with the parsed command line options
        startJVM(cmd);
    }

    /**
     * Starts the JVM by tying together all the components:
     * 1. Set up the classpath using cmd.getClasspath()
     * 2. Load the main class using cmd.getClassName()
     * 3. Find the main method
     * 4. Create the initial thread and stack frame
     * 5. Start the interpreter loop
     */
    private static void startJVM(Cmd cmd) {
        System.out.printf("classpath: %s, class: %s, args: %s%n",
                cmd.getClasspath(),
                cmd.getClassName(),
                java.util.Arrays.toString(cmd.getArgs()));

        try {
            // 1. Set up the classpath
            Classpath classpath = new Classpath(cmd.getClasspath());

            // 2. Set up method area and class loader
            MethodArea methodArea = new MethodArea();
            ClassLoader classLoader = new ClassLoader(classpath, methodArea);

            // 3. Load the main class
            String className = cmd.getClassName();
            RuntimeClass mainClass = classLoader.loadClass(className);

            // 4. Find the main method
            MethodInfo mainMethod = mainClass.getMainMethod();
            if (mainMethod == null) {
                System.err.println("Error: Main method not found in class " + className);
                return;
            }

            // 5. Create the initial thread
            Thread thread = new Thread();

            // 6. Set up the interpreter
            Interpreter interpreter = new Interpreter(classLoader);

            // 7. Start execution
            System.out.println("Starting JVM execution...");
            interpreter.interpret(thread, mainMethod, true); // verbose = true for debugging
            System.out.println("JVM execution completed.");

        } catch (ClassNotFoundException e) {
            System.err.println("Error: Could not find class " + cmd.getClassName());
            System.err.println("ClassNotFoundException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}