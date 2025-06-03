package com.tinyjvm.cmd;

/**
 * Command line argument parser
 * Handles arguments like: java -cp /path/to/classes MyClass arg1 arg2
 * Our version: tiny-jvm -cp /path/to/classes MyClass arg1 arg2
 */
public class Cmd {
    // Flag to indicate if help was requested (-help, -h, or -?)
    private boolean helpFlag;
    // Flag to indicate if version info was requested (-version)
    private boolean versionFlag;
    // The classpath specified via -cp or -classpath option
    private String classpath;
    // The name of the class to execute (e.g. HelloWorld)
    private String className;
    // Additional arguments to pass to the main class
    private String[] args;

    // Private constructor - use parseCmd() to create instances
    private Cmd() {
    }

    /**
     * Parse command line arguments and return a Cmd object
     * Expected format: [options] classname [args...]
     * 
     * Options to support:
     * -help or -h or -? : Show help message
     * -version : Show version 
     * -cp or -classpath : Specify classpath
     * 
     * Examples:
     * tiny-jvm HelloWorld                                  -> className="HelloWorld", args=[]
     * tiny-jvm -help                                      -> helpFlag=true
     * tiny-jvm -version                                   -> versionFlag=true
     * tiny-jvm -cp /path/to/classes HelloWorld           -> classpath="/path/to/classes", className="HelloWorld", args=[]
     * tiny-jvm -cp /path/to/classes HelloWorld arg1 arg2 -> classpath="/path/to/classes", className="HelloWorld", args=["arg1", "arg2"]
     * tiny-jvm                                           -> null (prints usage)
     * 
     * @param args Command line arguments
     * @return Cmd object or null if invalid/help was shown
     */
    public static Cmd parseCmd(String[] args) {
        Cmd cmd = new Cmd();

        if (args.length == 0) {
            printUsage();
            return null;
        }

        // Simple parsing for now - just take the first argument as class name
        // You should improve this to handle -cp, -help, etc.
        if (args.length >= 1) {
            // use switch to handle the options
            switch (args[0]) {
                case "-help":
                    cmd.helpFlag = true;
                    printUsage();
                    return null;
                case "-version":
                    cmd.versionFlag = true;
                    printVersion();
                    return null;
                case "-cp":
                case "-classpath":
                    cmd.classpath = args[1];
                    cmd.className = args[2];
                    cmd.args = new String[args.length - 3];
                    System.arraycopy(args, 3, cmd.args, 0, args.length - 3);
                    return cmd;
                default:
                    cmd.className = args[0];
                    cmd.classpath = "";
                    cmd.args = new String[args.length - 1];
                    System.arraycopy(args, 1, cmd.args, 0, args.length - 1);
                    return cmd;
            }
                
        }
        return cmd;
    }

    private static void printVersion() {
        System.out.println("This is a tiny JVM version 0.0.1");
    }

    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("Usage: tiny-jvm [options] class [args...]");
        System.out.println("Options:");
        System.out.println("  -cp <path>        Specify classpath");
        System.out.println("  -classpath <path> Specify classpath");
        System.out.println("  -help             Show this help message");
        System.out.println("  -version          Show version information");
    }

    // Getters
    public boolean isHelpFlag() {
        return helpFlag;
    }

    public boolean isVersionFlag() {
        return versionFlag;
    }

    public String getClasspath() {
        return classpath;
    }

    public String getClassName() {
        return className;
    }

    public String[] getArgs() {
        return args;
    }
}