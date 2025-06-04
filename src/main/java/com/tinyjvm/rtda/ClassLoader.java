package com.tinyjvm.rtda;

import com.tinyjvm.classfile.ClassFile;
import com.tinyjvm.classpath.Classpath;
// No IOException needed here as Classpath.readClass handles it internally by returning null
import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for loading classes into the MethodArea.
 * It reads class files, parses them, and links them by loading superclasses and
 * interfaces.
 */
public class ClassLoader {

    private Classpath classpath;
    private MethodArea methodArea;
    // Used to detect circular dependencies during a single complex load operation
    // (e.g. A needs B, B needs A)
    private Set<String> loadingInProgress;

    public ClassLoader(Classpath classpath, MethodArea methodArea) {
        this.classpath = classpath;
        this.methodArea = methodArea;
        this.loadingInProgress = new HashSet<>();
    }

    /**
     * Loads a class by its fully qualified name (e.g., "java.lang.Object" or
     * "com.example.MyClass").
     * If the class is already loaded, it returns the existing RuntimeClass.
     * Otherwise, it attempts to find, read, parse, and link the class.
     *
     * @param className The fully qualified name of the class to load (e.g.
     *                  "java.lang.String").
     * @return The RuntimeClass object representing the loaded class.
     * @throws ClassNotFoundException if the class file cannot be found or read.
     * @throws ClassFormatError       if the class file is malformed.
     * @throws ClassCircularityError  if a circular dependency is detected during
     *                                loading.
     */
    public RuntimeClass loadClass(String className) throws ClassNotFoundException {
        // Class names in MethodArea and for classpath searching are typically in
        // internal format (java/lang/Object)
        String internalClassName = className.replace('.', '/');

        RuntimeClass loadedClass = methodArea.findClass(internalClassName);
        if (loadedClass != null) {
            return loadedClass; // Already loaded
        }

        if (loadingInProgress.contains(internalClassName)) {
            // This indicates we are already in the process of loading this exact class in
            // the current call stack,
            // which implies a circular dependency in the class hierarchy or interface
            // implementation.
            throw new ClassCircularityError("Circular dependency detected while loading class: " + internalClassName);
        }
        loadingInProgress.add(internalClassName);

        try {
            // classpath.readClass expects internal name format without .class extension
            byte[] classData = classpath.readClass(internalClassName);
            if (classData == null) {
                // classpath.readClass returns null if not found (after handling its own
                // IOExceptions)
                throw new ClassNotFoundException("Class not found: " + className + " (as " + internalClassName + ")");
            }

            ClassFile classFile = new ClassFile(classData); // This can throw ClassFormatError
            RuntimeClass newRuntimeClass = new RuntimeClass(classFile); // Name is set from classFile.getClassName()
                                                                        // here

            // Crucial: Load superclass and interfaces *before* adding the current class to
            // methodArea
            // to ensure that when a class is findable in methodArea, its hierarchy is also
            // findable.
            loadSuperclass(newRuntimeClass);
            loadInterfaces(newRuntimeClass);

            methodArea.addClass(newRuntimeClass);

            // TODO: Class Initialization (triggering <clinit>) would typically happen here
            // or upon first active use.
            // For now, loading just means parsing and placing in MethodArea, making it
            // resolvable.

            return newRuntimeClass;

        } catch (ClassFormatError e) { // Catch ClassFormatError from ClassFile parser
            // Re-throw or wrap, ensuring it's clear which class caused the issue.
            // No need to catch IOException here as classpath.readClass handles it and
            // returns null.
            throw new ClassFormatError("Bad class file format for " + className + ": " + e.getMessage());
        } finally {
            loadingInProgress.remove(internalClassName); // Clean up loading state for this specific attempt
        }
    }

    private void loadSuperclass(RuntimeClass childClass) throws ClassNotFoundException {
        // java.lang.Object has no superclass (super_class_index is 0 in its ClassFile)
        if (childClass.getClassFile().getSuperClassIndex() == 0) {
            return;
        }
        // getSuperClassName() from ClassFile returns the name in internal format (e.g.,
        // java/lang/Object)
        String superClassNameInternal = childClass.getClassFile().getSuperClassName();

        // Special case: skip loading java.lang.Object since we don't have the standard
        // library
        if ("java/lang/Object".equals(superClassNameInternal)) {
            System.out.println("Note: Skipping java.lang.Object (not available in tiny JVM)");
            return;
        }

        if (superClassNameInternal != null && methodArea.findClass(superClassNameInternal) == null) {
            // loadClass expects external format (java.lang.Object), so convert back
            loadClass(superClassNameInternal.replace('/', '.'));
        }
    }

    private void loadInterfaces(RuntimeClass implementorClass) throws ClassNotFoundException {
        // getInterfaceNames() from ClassFile returns names in internal format
        String[] interfaceNamesInternal = implementorClass.getClassFile().getInterfaceNames();
        for (String interfaceNameInternal : interfaceNamesInternal) {
            if (interfaceNameInternal != null && methodArea.findClass(interfaceNameInternal) == null) {
                // loadClass expects external format, so convert back
                loadClass(interfaceNameInternal.replace('/', '.'));
            }
        }
    }
}