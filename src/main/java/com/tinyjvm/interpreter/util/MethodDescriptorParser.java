package com.tinyjvm.interpreter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for parsing JVM method descriptors.
 * Method descriptors are strings that describe the parameters and return type
 * of a method.
 * Example: "(ILjava/lang/String;)V" describes a method that takes an int and a
 * String, and returns void.
 */
public class MethodDescriptorParser {

    private String descriptor;
    private int offset;

    private MethodDescriptorParser(String descriptor) {
        this.descriptor = descriptor;
        this.offset = 0;
    }

    /**
     * Parses the parameter types from a method descriptor string.
     *
     * @param descriptor The method descriptor (e.g., "(IDLjava/lang/String;)V").
     * @return A list of strings, where each string is a field type descriptor for a
     *         parameter.
     * @throws IllegalArgumentException if the descriptor is malformed.
     */
    public static List<String> parseParameterTypes(String descriptor) {
        MethodDescriptorParser parser = new MethodDescriptorParser(descriptor);
        return parser.parseInternalParameterTypes();
    }

    /**
     * Parses the return type from a method descriptor string.
     * 
     * @param descriptor The method descriptor (e.g., "(ID)Ljava/lang/String;").
     * @return A string representing the field type descriptor for the return value.
     * @throws IllegalArgumentException if the descriptor is malformed.
     */
    public static String parseReturnType(String descriptor) {
        MethodDescriptorParser parser = new MethodDescriptorParser(descriptor);
        return parser.parseInternalReturnType();
    }

    private List<String> parseInternalParameterTypes() {
        if (descriptor.charAt(offset) != '(') {
            throw new IllegalArgumentException("Method descriptor must start with '(': " + descriptor);
        }
        offset++; // Skip '('

        List<String> paramTypes = new ArrayList<>();
        while (offset < descriptor.length() && descriptor.charAt(offset) != ')') {
            paramTypes.add(parseFieldType());
        }
        if (offset >= descriptor.length() || descriptor.charAt(offset) != ')') {
            throw new IllegalArgumentException("Method descriptor must end with ')' for parameters: " + descriptor);
        }
        offset++; // Skip ')'
        return paramTypes;
    }

    private String parseInternalReturnType() {
        // First, parse and skip parameters to correctly position offset
        if (descriptor.charAt(offset) != '(') {
            throw new IllegalArgumentException("Method descriptor must start with '(': " + descriptor);
        }
        offset++; // Skip '('
        while (offset < descriptor.length() && descriptor.charAt(offset) != ')') {
            parseFieldType(); // Advances offset through each parameter type
        }
        if (offset >= descriptor.length() || descriptor.charAt(offset) != ')') {
            throw new IllegalArgumentException(
                    "Method descriptor must end with ')' for parameters part before return type: " + descriptor);
        }
        offset++; // Skip ')'

        // What remains is the return type
        if (offset >= descriptor.length()) {
            throw new IllegalArgumentException("Method descriptor is missing return type: " + descriptor);
        }
        return parseFieldType();
    }

    /**
     * Parses a single field type descriptor from the current offset.
     * Advances the offset past the parsed type.
     * FieldType:
     * BaseType
     * ObjectType
     * ArrayType
     * BaseType: B C D F I J S Z V (V only for return)
     * ObjectType: L ClassName ;
     * ArrayType: [ FieldType
     */
    private String parseFieldType() {
        if (offset >= descriptor.length()) {
            throw new IllegalArgumentException("Unexpected end of descriptor while parsing field type: " + descriptor);
        }
        char firstChar = descriptor.charAt(offset);
        int startOffset = offset;

        switch (firstChar) {
            case 'B': // byte
            case 'C': // char
            case 'D': // double
            case 'F': // float
            case 'I': // int
            case 'J': // long
            case 'S': // short
            case 'Z': // boolean
            case 'V': // void (only for return types, but parser handles it as a single char type)
                offset++;
                return String.valueOf(firstChar);
            case 'L': // Object type: L<classname>;
                int semicolonIndex = descriptor.indexOf(';', offset);
                if (semicolonIndex == -1) {
                    throw new IllegalArgumentException(
                            "Malformed object type (missing ';') in descriptor: " + descriptor.substring(startOffset));
                }
                offset = semicolonIndex + 1;
                return descriptor.substring(startOffset, offset);
            case '[': // Array type: [<componenttype>
                offset++; // Skip '['
                parseFieldType(); // Recursively parse (and advance offset past) the component type
                return descriptor.substring(startOffset, offset); // The full array type string includes the component
                                                                  // type
            default:
                throw new IllegalArgumentException("Invalid field type character: " + firstChar + " in descriptor: "
                        + descriptor + " at offset " + startOffset);
        }
    }

    /**
     * Calculates the number of local variable slots occupied by the given parameter
     * types.
     * Long (J) and Double (D) types occupy two slots; all others occupy one.
     * 
     * @param parameterTypes A list of parameter type descriptors (e.g., ["I", "D",
     *                       "Ljava/lang/String;"])
     * @return The total number of local variable slots required.
     */
    public static int calculateArgumentSlots(List<String> parameterTypes) {
        int slots = 0;
        if (parameterTypes == null)
            return 0;
        for (String type : parameterTypes) {
            if (type.equals("J") || type.equals("D")) {
                slots += 2;
            } else {
                slots += 1;
            }
        }
        return slots;
    }
}