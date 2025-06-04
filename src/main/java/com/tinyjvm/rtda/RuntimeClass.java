package com.tinyjvm.rtda;

import com.tinyjvm.classfile.ClassFile;
import com.tinyjvm.classfile.MethodInfo;
import com.tinyjvm.classfile.ConstantPool;
// Import other necessary classfile components if needed later

/**
 * Represents a class that has been loaded into the JVM's Method Area.
 * It holds the parsed ClassFile structure and will later include
 * runtime-specific
 * information like the runtime constant pool, static variables, etc.
 */
public class RuntimeClass {

    private ClassFile classFile;
    private String name; // Fully qualified class name (e.g., java/lang/Object)
    // TODO: Add fields for runtime constant pool, static fields, superclass link,
    // interface links etc.

    public RuntimeClass(ClassFile classFile) {
        this.classFile = classFile;
        if (classFile == null) {
            // For testing purposes, when classFile is null
            this.name = null;
        } else {
            this.name = classFile.getClassName();
        }
        // TODO: Initialize runtime constant pool
        // TODO: Initialize static fields
        // TODO: Resolve and link superclass and interfaces
    }

    public String getName() {
        return this.name;
    }

    public ClassFile getClassFile() {
        return this.classFile;
    }

    public ConstantPool getConstantPool() {
        if (classFile == null) {
            return null;
        }
        return classFile.getConstantPool();
    }

    public MethodInfo getMainMethod() {
        if (classFile == null) {
            return null;
        }
        return classFile.getMethod("main", "([Ljava/lang/String;)V");
    }

    public String[] getInterfaceNames() {
        if (classFile == null) {
            return new String[0];
        }
        return classFile.getInterfaceNames();
    }

    // Added method to find a specific method by name and descriptor
    public MethodInfo findMethod(String name, String descriptor) {
        if (classFile == null) {
            return null;
        }
        for (MethodInfo method : classFile.getMethods()) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }
        return null; // Method not found
    }

    // TODO: Add methods to get superclass, interfaces, resolve field/method
    // references etc.

    @Override
    public String toString() {
        return "RuntimeClass{name='" + name + "'}";
    }
}