package com.tinyjvm.main;

import com.tinyjvm.cmd.Cmd;
import com.tinyjvm.classpath.Classpath;

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

        // TODO: You will implement this method
        // This should start the JVM with the parsed command line options
        startJVM(cmd);
    }

    /**
     * TODO: Implement this method
     * This is where you'll tie together all the JVM components:
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

        // TODO: Implement JVM startup logic here
        // For now, just print what we would do
        System.out.println("TODO: Start JVM with class: " + cmd.getClassName());
    }
}