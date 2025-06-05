# Development Guide

This guide is for developers who want to understand, modify, or extend the Tiny JVM project.

## 🏛️ Architecture Overview

The Tiny JVM follows a layered architecture similar to real JVM implementations:

```
┌─────────────────────────────────────────────────────────────┐
│                     Main.java                              │
│                   (Entry Point)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   cmd/                                      │
│              Command Line Parsing                          │
│     • Cmd.java - Argument parsing and validation          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 classpath/                                  │
│             Classpath Management                           │
│  • Classpath.java - Main classpath interface              │
│  • ClasspathEntry.java - Abstract entry                   │
│  • DirectoryEntry.java - Directory searching              │
│  • JarEntry.java - JAR file reading                       │
│  • CompositeEntry.java - Multiple path handling           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 classfile/                                  │
│              Class File Parsing                           │
│  • ClassFile.java - Main class file structure             │
│  • ClassReader.java - Binary reading utilities            │
│  • ConstantPool.java - Constant pool management           │
│  • ConstantInfo.java - All 18 constant types              │
│  • MethodInfo.java - Method information                   │
│  • FieldInfo.java - Field information                     │
│  • AttributeInfo.java - Code, SourceFile, etc.           │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    rtda/                                    │
│             Runtime Data Areas                             │
│  • ClassLoader.java - Class loading and linking           │
│  • MethodArea.java - Loaded class storage                 │
│  • RuntimeClass.java - Runtime class representation       │
│  • Thread.java - Thread execution context                 │
│  • Frame.java - Method invocation frame                   │
│  • JvmStack.java - Stack of frames                        │
│  • frame/LocalVars.java - Local variables                 │
│  • frame/OperandStack.java - Operand stack                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│               interpreter/                                  │
│             Bytecode Execution                             │
│  • Interpreter.java - Main execution loop                 │
│  • util/MethodDescriptorParser.java - Method signatures   │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Key Components

### ClassFile Parsing (`classfile/`)
- **Purpose**: Parse Java `.class` files according to JVM specification
- **Key Classes**: 
  - `ClassFile` - Main parser and container
  - `ConstantPool` - Manages the constant pool with 1-based indexing
  - `ConstantInfo` - Factory for all 18 constant pool entry types
- **Extension Points**: Add new attribute types in `AttributeInfo`

### Runtime Data Areas (`rtda/`)
- **Purpose**: Manage JVM runtime memory structures
- **Key Classes**:
  - `ClassLoader` - Loads classes with dependency resolution
  - `Frame` - Represents a method call with local vars and operand stack
  - `LocalVars` - Manages local variables with proper slot handling
  - `OperandStack` - JVM operand stack with type-specific operations
- **Extension Points**: Add new runtime structures or optimize memory usage

### Interpreter (`interpreter/`)
- **Purpose**: Execute bytecode instructions
- **Key Classes**:
  - `Interpreter` - Main fetch-decode-execute loop
  - `MethodDescriptorParser` - Parses method signatures for argument handling
- **Extension Points**: Add new bytecode instructions or optimization

## 🛠️ Adding New Bytecode Instructions

To add a new bytecode instruction (e.g., `0x99` for `ifeq`):

1. **Find the opcode** in the JVM specification
2. **Add the case** in `Interpreter.java`:

```java
case 0x99: { // ifeq <branchbyte1> <branchbyte2>
    int offset = ((bytecode[pc + 1] & 0xFF) << 8) | (bytecode[pc + 2] & 0xFF);
    if (offset > 32767) offset -= 65536; // Convert to signed
    int val = currentFrame.getOperandStack().popInt();
    if (val == 0) {
        currentFrame.setNextPc(pc + offset);
    } else {
        currentFrame.setNextPc(pc + 3);
    }
    break;
}
```

3. **Handle PC advancement** - Multi-byte instructions must set PC explicitly
4. **Add tests** in appropriate test classes

## 🧪 Testing Strategy

Each layer has its own test suite:

- **`CmdTest`** - Command line parsing edge cases
- **`ClasspathTest`** - File reading, JAR handling, composite paths
- **`ClassFileParserTest`** - Class file format compliance
- **`RTDATest`** - Runtime data structure operations

### Writing New Tests

```java
@Test
@DisplayName("Description of what you're testing")
public void testSomething() {
    // Arrange
    SomeClass instance = new SomeClass();
    
    // Act  
    Result result = instance.doSomething();
    
    // Assert
    assertEquals(expected, result);
}
```

## 🐛 Debugging Tips

### Enable Verbose Mode
```bash
mvn exec:java -Dexec.mainClass="com.tinyjvm.main.Main" \
    -Dexec.args="-cp output SomeClass"
```
The interpreter runs with `verbose=true` by default, showing each instruction.

### Common Issues

1. **PC Not Advanced**: Multi-byte instructions must call `setNextPc(pc + instructionLength)`
2. **Stack Imbalance**: Ensure pop/push operations match instruction semantics
3. **Type Mismatches**: Use correct `pushInt/popInt` vs `pushRef/popRef`
4. **Slot Handling**: `long` and `double` take 2 slots in LocalVars

### Using JavaP for Reference
```bash
javap -v SomeClass.class
```
Shows the expected bytecode sequence for comparison.

## 🚀 Performance Considerations

Current implementation prioritizes simplicity over performance:

- **No bytecode optimization** - Each instruction is interpreted directly
- **No JIT compilation** - Pure interpretation only  
- **Simple data structures** - Arrays for stacks, HashMap for method area
- **No garbage collection** - Manual memory management

For better performance, consider:
- **Bytecode caching** and preprocessing
- **Stack allocation optimization**
- **Instruction dispatch optimization** (computed goto, etc.)

## 📝 Code Style

- **JavaDoc** for public APIs
- **Descriptive variable names** (e.g., `methodRefIndex` not `idx`)
- **Early returns** for error cases
- **Package-private** where appropriate
- **Null safety** with explicit null checks

## 🔍 Understanding JVM Specification

Key sections for this project:
- **Chapter 4**: Class File Format
- **Chapter 5**: Loading, Linking, and Initializing  
- **Chapter 6**: The Java Virtual Machine Instruction Set
- **Chapter 2.5**: Runtime Data Areas

## 🎯 Extension Ideas

### Easy Extensions
- Add more arithmetic instructions (`ishl`, `ishr`, `iand`, `ior`, `ixor`)
- Implement more comparison instructions
- Add `tableswitch` and `lookupswitch`

### Medium Extensions  
- Object instantiation (`new`, `invokevirtual`, `getfield`, `putfield`)
- Array support (`newarray`, `anewarray`, `aaload`, `aastore`)
- Exception handling (`athrow`, exception tables)

### Hard Extensions
- Garbage collection
- Threading and synchronization
- JNI (Java Native Interface)
- Reflection support

## 📚 Resources

- [JVM Specification](https://docs.oracle.com/javase/specs/jvms/se11/html/)
- [Bytecode instruction reference](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)
- [Understanding JVM Internals](https://www.cubrid.org/blog/understanding-jvm-internals/)

---

Happy coding! 🎉 