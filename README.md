# Tiny JVM

A minimal Java Virtual Machine implementation built from scratch in Java for educational purposes. This project demonstrates the core concepts of how a JVM works by implementing the essential components needed to load, parse, and execute Java bytecode.

## 🎯 Project Goals

- **Educational**: Learn JVM internals by building one from scratch
- **Minimal**: Implement only the core features needed for basic Java execution  
- **Functional**: Actually run simple Java programs end-to-end
- **Well-tested**: Comprehensive unit test coverage (49 tests currently passing)

## ✅ Current Capabilities

### Phase 1: Infrastructure
- ✅ **Command-line parsing** (`-cp`, `-help`, `-version`)
- ✅ **Classpath management** (directories, JAR files, composite paths)
- ✅ **Class file parsing** (magic numbers, versions, constant pool, methods, fields, attributes)
- ✅ **Constant pool support** (all 18 constant types including modern ones like InvokeDynamic)

### Phase 2: Runtime & Execution  
- ✅ **Runtime Data Areas (RTDA)**:
  - `LocalVars`: Method local variables with proper slot management
  - `OperandStack`: JVM operand stack for all data types
  - `Frame`: Method invocation frames
  - `JvmStack`: Stack of frames for method calls
  - `Thread`: Thread execution context
  - `MethodArea`: Class storage and retrieval
  - `ClassLoader`: Class loading with dependency resolution
- ✅ **Bytecode Interpreter** with 80+ instructions:
  - **Constants**: `iconst`, `lconst`, `fconst`, `dconst`, `bipush`, `sipush`, `ldc`
  - **Load/Store**: Complete set for all data types (`iload`, `lload`, `fload`, `dload`, `aload` + indexed versions)
  - **Stack Manipulation**: `pop`, `pop2`, `dup`, `dup2`
  - **Arithmetic**: Full arithmetic operations (`add`, `sub`, `mul`, `div`, `rem`, `neg`) for all numeric types
  - **Comparison/Branching**: Conditional branches (`ifeq`, `ifne`, `iflt`, etc.) and comparisons (`if_icmpeq`, etc.)
  - **Control Flow**: `goto` for unconditional jumps
  - **Returns**: All return types (`ireturn`, `lreturn`, `freturn`, `dreturn`, `areturn`, `return`)
  - **Method Calls**: `invokestatic`, `invokespecial` (constructors)
- ✅ **Method Descriptor Parser**: Handles method signatures and argument passing
- ✅ **Special handling** for `java.lang.Object` (since we don't include the standard library)

## 🏗️ Project Structure

```
src/
├── main/java/com/tinyjvm/
│   ├── cmd/                    # Command-line argument parsing
│   │   └── Cmd.java
│   ├── classpath/              # Classpath management
│   │   ├── Classpath.java
│   │   ├── ClasspathEntry.java (abstract)
│   │   ├── DirectoryEntry.java
│   │   ├── JarEntry.java
│   │   └── CompositeEntry.java
│   ├── classfile/              # Class file parsing
│   │   ├── ClassFile.java
│   │   ├── ClassReader.java
│   │   ├── ConstantPool.java
│   │   ├── ConstantInfo.java   # All 18 constant types
│   │   ├── MethodInfo.java
│   │   ├── FieldInfo.java
│   │   └── AttributeInfo.java  # Code, SourceFile attributes
│   ├── rtda/                   # Runtime Data Areas
│   │   ├── ClassLoader.java
│   │   ├── MethodArea.java
│   │   ├── RuntimeClass.java
│   │   ├── Thread.java
│   │   ├── Frame.java
│   │   ├── JvmStack.java
│   │   └── frame/
│   │       ├── LocalVars.java
│   │       └── OperandStack.java
│   ├── interpreter/            # Bytecode execution
│   │   ├── Interpreter.java    # Main interpreter loop
│   │   └── util/
│   │       └── MethodDescriptorParser.java
│   └── main/
│       └── Main.java           # Entry point
└── test/java/com/tinyjvm/
    ├── cmd/CmdTest.java                    # 12 tests
    ├── classpath/ClasspathTest.java        # 15 tests  
    ├── classfile/ClassFileParserTest.java  # 7 tests
    ├── rtda/RTDATest.java                  # 15 tests
    └── testprogram/
        └── SimpleTest.java                 # Sample program for testing
```

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Building
```bash
# Clone the repository
git clone <repository-url>
cd tiny-jvm

# Compile the project
mvn compile

# Run all tests (49 tests should pass)
mvn test
```

### Running a Java Program

1. **Create a simple Java program** (or use the provided `SimpleTest.java`):
```java
public class SimpleTest {
    public static void main(String[] args) {
        int result = calculate();
    }
    
    public static int calculate() {
        int a = 10;
        int b = 20; 
        int c = a + b;
        return c;
    }
}
```

2. **Compile the Java program**:
```bash
javac --release 11 -d output/ SimpleTest.java
```

3. **Run it with Tiny JVM**:
```bash
mvn exec:java -Dexec.mainClass="com.tinyjvm.main.Main" \
    -Dexec.args="-cp output SimpleTest"
```

### Example Output
```
classpath: output, class: SimpleTest, args: []
Note: Skipping java.lang.Object (not available in tiny JVM)
Starting JVM execution...
[pc:0000] [op:0xb8] Executing opcode... (Frame: [ClassName].main([Ljava/lang/String;)V)
invokestatic: SimpleTest.calculate()I
[pc:0000] [op:0x10] Executing opcode... (Frame: [ClassName].calculate()I)
...
JVM execution completed.
```

## 🧪 Testing

The project includes comprehensive tests:

```bash
# Run all tests
mvn test

# Run specific test suites  
mvn test -Dtest=RTDATest        # Runtime data areas
mvn test -Dtest=CmdTest         # Command line parsing
mvn test -Dtest=ClasspathTest   # Classpath functionality
mvn test -Dtest=ClassFileParserTest  # Class file parsing
```

## 📋 What Works

Our Tiny JVM can successfully:

1. **Parse command-line arguments** with classpath support
2. **Load and parse .class files** including complex constant pools
3. **Execute basic Java programs** with arithmetic, method calls, and control flow
4. **Handle method invocation** with proper argument passing and return values
5. **Manage runtime data** (local variables, operand stack, method frames)
6. **Support all primitive data types** (int, long, float, double, references)

### Supported Bytecode Instructions (80+)
- **Constants**: iconst_m1 through iconst_5, lconst_0/1, fconst_0/1/2, dconst_0/1, bipush, sipush, ldc
- **Loads**: iload, lload, fload, dload, aload (both indexed and _0 through _3 variants)
- **Stores**: istore, lstore, fstore, dstore, astore (both indexed and _0 through _3 variants)
- **Stack**: pop, pop2, dup, dup2
- **Math**: iadd, ladd, fadd, dadd, isub, lsub, fsub, dsub, imul, lmul, fmul, dmul, idiv, ldiv, fdiv, ddiv, irem, ineg, lneg, fneg, dneg
- **Comparisons**: ifeq, ifne, iflt, ifge, ifgt, ifle, if_icmpeq, if_icmpne, if_icmplt, if_icmpge, if_icmpgt, if_icmple
- **Control**: goto
- **Methods**: invokestatic, invokespecial
- **Returns**: ireturn, lreturn, freturn, dreturn, areturn, return

## 🚧 Current Limitations

- **No object instantiation** (`new` keyword not supported)
- **No instance methods** (`invokevirtual` not implemented)
- **No arrays** (array operations not supported)
- **No standard library** (java.lang.Object is stubbed out)
- **No exception handling** (try/catch not supported)
- **No garbage collection** (memory management is manual)
- **No threading** (single-threaded execution only)
- **No JNI** (native methods not supported)

## 🔮 Future Extensions

The architecture is designed to be extensible. Potential next steps:

1. **Object Support**: 
   - Implement `new` instruction
   - Add `invokevirtual` for instance methods
   - Implement field access (`getfield`, `putfield`)

2. **Arrays**:
   - Array creation and access instructions
   - Multi-dimensional arrays

3. **Exception Handling**:
   - `athrow` instruction
   - Exception table handling
   - Try/catch blocks

4. **Advanced Features**:
   - Garbage collection
   - Threading support
   - JNI interface
   - Reflection

## 🧩 Architecture Highlights

- **Modular Design**: Each component is well-separated and testable
- **Proper Abstraction**: Clean interfaces between parser, loader, and interpreter
- **Comprehensive Testing**: 49 unit tests covering all major components
- **Error Handling**: Graceful handling of malformed class files and runtime errors
- **Extensible**: Easy to add new bytecode instructions and features

## 📚 Learning Resources

This project demonstrates:
- **JVM Specification** implementation
- **Bytecode interpretation** techniques  
- **Runtime data management** patterns
- **Class loading** and linking mechanisms
- **Stack-based virtual machine** architecture

## 🤝 Contributing

This is an educational project. Feel free to:
- Add new bytecode instructions
- Implement missing JVM features
- Improve error handling
- Add more comprehensive tests
- Optimize performance

## 📄 License

MIT License - Feel free to use this for educational purposes.

---

**Note**: This is a educational JVM implementation. It is not intended for production use and does not implement the complete JVM specification. For production Java execution, use OpenJDK or other certified JVM implementations. 