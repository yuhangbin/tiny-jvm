package com.tinyjvm.test;

/**
 * This class is designed to generate a variety of constant pool entries
 * when compiled. It serves as an example to understand how Java language
 * constructs map to the constant pool types in a .class file.
 */
interface MyExampleInterface {
    void anInterfaceMethod();
}

public class AllConstantTypes implements MyExampleInterface {

    // 1. CONSTANT_Integer
    private int intField = 12345;

    // 2. CONSTANT_Float
    private float floatField = 3.14f;

    // 3. CONSTANT_Long
    // Note: long and double constants take two entries in the constant pool.
    private long longField = 9876543210L;

    // 4. CONSTANT_Double
    private double doubleField = 2.718281828459;

    // 5. CONSTANT_String & CONSTANT_Utf8
    // "Hello, Tiny JVM!" is a CONSTANT_String_info, which points to
    // a CONSTANT_Utf8_info holding the actual string.
    private String stringField = "Hello, Tiny JVM!";

    // Static field for demonstration
    public static String staticStringField = "A static field";

    // Constructor
    // "AllConstantTypes" (class name) - CONSTANT_Utf8
    // "<init>" (method name for constructor) - CONSTANT_Utf8
    // "()V" (descriptor for constructor) - CONSTANT_Utf8
    // The constructor itself - CONSTANT_Methodref_info
    // -> class_index: points to CONSTANT_Class_info for "AllConstantTypes"
    // -> name_and_type_index: points to CONSTANT_NameAndType_info for "<init>" and
    // "()V"
    public AllConstantTypes() {
        System.out.println("AllConstantTypes constructor called.");
    }

    // Instance method
    // "instanceMethod" (method name) - CONSTANT_Utf8
    // "(I)Ljava/lang/String;" (descriptor) - CONSTANT_Utf8
    // The method itself - CONSTANT_Methodref_info
    // -> class_index: points to CONSTANT_Class_info for "AllConstantTypes"
    // -> name_and_type_index: points to CONSTANT_NameAndType_info for
    // "instanceMethod" and "(I)Ljava/lang/String;"
    public String instanceMethod(int param) {
        return "Input was: " + param; // Another string constant
    }

    // Static method
    // "staticMethod" (method name) - CONSTANT_Utf8
    // "()V" (descriptor) - CONSTANT_Utf8
    // The method itself - CONSTANT_Methodref_info
    public static void staticMethod() {
        System.out.println(staticStringField); // Accessing a static field
        // Accessing System.out:
        // "System" - CONSTANT_Class_info (for java/lang/System)
        // -> name_index: CONSTANT_Utf8 "java/lang/System"
        // "out" - CONSTANT_Utf8 (field name)
        // "Ljava/io/PrintStream;" - CONSTANT_Utf8 (field descriptor)
        // "out Ljava/io/PrintStream;" - CONSTANT_NameAndType_info
        // Reference to System.out - CONSTANT_Fieldref_info
        // -> class_index: CONSTANT_Class_info for "java/lang/System"
        // -> name_and_type_index: CONSTANT_NameAndType_info for "out" and its
        // descriptor

        // Calling println:
        // "println" - CONSTANT_Utf8 (method name)
        // "(Ljava/lang/String;)V" - CONSTANT_Utf8 (method descriptor)
        // "println (Ljava/lang/String;)V" - CONSTANT_NameAndType_info
        // Reference to PrintStream.println - CONSTANT_Methodref_info
        // -> class_index: CONSTANT_Class_info for "java/io/PrintStream"
        // -> name_and_type_index: CONSTANT_NameAndType_info for "println" and its
        // descriptor
    }

    // Interface method implementation
    // "anInterfaceMethod" (method name) - CONSTANT_Utf8
    // "()V" (descriptor) - CONSTANT_Utf8
    // The method itself - CONSTANT_Methodref_info (for this specific
    // implementation)
    @Override
    public void anInterfaceMethod() {
        System.out.println("Implemented anInterfaceMethod.");
    }

    public static void main(String[] args) {
        // 6. CONSTANT_Class
        // Reference to AllConstantTypes class - CONSTANT_Class_info
        // -> name_index: CONSTANT_Utf8_info for "com/tinyjvm/test/AllConstantTypes"
        AllConstantTypes obj = new AllConstantTypes();

        obj.instanceMethod(obj.intField); // Corrected: Access intField via obj instance
        // but the field itself 'intField' generates a Fieldref if accessed from another
        // class
        // or by reflection. Within the same class, it might be optimized.

        // 7. CONSTANT_Fieldref
        // Reference to 'staticStringField' from 'AllConstantTypes'
        // "staticStringField" - CONSTANT_Utf8
        // "Ljava/lang/String;" - CONSTANT_Utf8 (descriptor for String)
        // NameAndType for staticStringField - CONSTANT_NameAndType_info
        // Field reference - CONSTANT_Fieldref_info
        // -> class_index: CONSTANT_Class_info for "com/tinyjvm/test/AllConstantTypes"
        // -> name_and_type_index: for "staticStringField" and its descriptor
        System.out.println(AllConstantTypes.staticStringField);

        // 8. CONSTANT_Methodref
        // Already shown with constructor, instanceMethod, staticMethod calls.

        // 9. CONSTANT_InterfaceMethodref
        // "MyExampleInterface" - CONSTANT_Class_info (for the interface)
        // -> name_index: CONSTANT_Utf8 "com/tinyjvm/test/MyExampleInterface"
        // "anInterfaceMethod" - CONSTANT_Utf8
        // "()V" - CONSTANT_Utf8
        // NameAndType for anInterfaceMethod - CONSTANT_NameAndType_info
        // Interface method reference - CONSTANT_InterfaceMethodref_info
        // -> class_index: CONSTANT_Class_info for "com/tinyjvm/test/MyExampleInterface"
        // -> name_and_type_index: for "anInterfaceMethod" and "()V"
        MyExampleInterface ifaceObj = obj;
        ifaceObj.anInterfaceMethod();

        // 10. CONSTANT_NameAndType
        // Used by Fieldref, Methodref, InterfaceMethodref as shown above.
        // It pairs a name (Utf8) with a descriptor (Utf8).
        // Example: "staticStringField" (name) and "Ljava/lang/String;" (descriptor)
        // Example: "instanceMethod" (name) and "(I)Ljava/lang/String;" (descriptor)

        // Another class reference for things like instanceof or .class
        Class<?> clazz = AllConstantTypes.class; // Generates CONSTANT_Class for AllConstantTypes
                                                 // and often a CONSTANT_Methodref for Object.getClass() if used
                                                 // internally by .class
        System.out.println(clazz.getName()); // Method call on Class object
    }
}