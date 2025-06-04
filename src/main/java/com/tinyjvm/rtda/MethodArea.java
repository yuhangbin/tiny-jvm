package com.tinyjvm.rtda;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Method Area in the JVM, which stores class-level data.
 * This includes the runtime representation of loaded classes.
 */
public class MethodArea {

    // A simple map to store loaded classes by their fully qualified names
    private final Map<String, RuntimeClass> loadedClasses;

    public MethodArea() {
        this.loadedClasses = new HashMap<>();
    }

    /**
     * Adds a newly loaded class to the method area.
     * 
     * @param runtimeClass The RuntimeClass object representing the loaded class.
     */
    public void addClass(RuntimeClass runtimeClass) {
        if (runtimeClass != null && runtimeClass.getName() != null) {
            this.loadedClasses.put(runtimeClass.getName(), runtimeClass);
        } else {
            // Handle error: null class or name
            System.err.println("Warning: Attempted to add a null class or class with null name to MethodArea.");
        }
    }

    /**
     * Finds a loaded class by its name.
     * 
     * @param className The fully qualified name of the class (e.g.,
     *                  "java/lang/Object").
     * @return The RuntimeClass object if found, otherwise null.
     */
    public RuntimeClass findClass(String className) {
        return this.loadedClasses.get(className);
    }

    // TODO: Add methods for other functionalities, e.g.,
    // - getting all loaded classes
    // - handling initialization of classes (clinit)
    // - managing runtime constant pools if they become separate from ClassFile's
    // pool
}