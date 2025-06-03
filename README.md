# tiny-jvm

A minimal Java Virtual Machine implementation for learning purposes.

## Project Status

### ✅ Phase 1.1: Command Line Interface & Classpath (Framework Complete)

**What's Implemented:**
- Basic Maven project structure
- Main entry point (`com.tinyjvm.main.Main`)
- Command line argument parsing framework (`com.tinyjvm.cmd.Cmd`)
- Classpath management framework (`com.tinyjvm.classpath.Classpath`)

**How to Run:**
```bash
mvn compile
mvn exec:java -Dexec.args="HelloWorld"
```

**What You Need to Implement:**

#### 1. Enhanced Command Line Parsing (`Cmd.parseCmd()`)
Currently only handles the simplest case. You need to implement:
- `-cp` or `-classpath` option parsing
- `-help` and `-version` flags
- Proper argument validation

#### 2. File Reading in DirectoryEntry (`DirectoryEntry.readClass()`)
Complete the file reading logic:
```java
// TODO: Read file bytes using FileInputStream or Files.readAllBytes()
```

#### 3. JAR File Support (`JarEntry.readClass()`)
Implement reading from JAR files:
```java
// TODO: Use ZipFile or JarFile to read entries
```

#### 4. Multiple Classpath Support (`CompositeEntry`)
Handle classpath with multiple entries separated by `:` (Unix) or `;` (Windows)

## ClassLoader

**Next Phase (1.2-1.3): Class File Reading & Parsing**
- Parse `.class` file format (magic number, constant pool, methods, etc.)
- Extract bytecode instructions from methods
- Store class metadata for runtime use

## Runtime Data Areas

**Future Phase (2.1-2.3): Memory Management**
- Method Area: Store loaded class information
- JVM Stacks: Method call frames with local variables
- Heap: Object storage and basic garbage collection

## Execution Engine

**Future Phase (3.1-4.2): Bytecode Interpreter**
- Fetch-decode-execute loop
- Individual instruction implementations
- Method invocation and stack management

## Architecture Overview

```
User Input: java -cp /path HelloWorld arg1 arg2
     ↓
┌─────────────────────────────────┐
│         tiny-jvm                │
│  ┌─────────┐  ┌─────────────┐   │
│  │   Cmd   │  │  Classpath  │   │
│  │ Parser  │  │  Manager    │   │
│  └─────────┘  └─────────────┘   │
│       ↓              ↓          │
│  ┌─────────────────────────────┐ │
│  │      Class Loader           │ │ ← You'll implement this next
│  └─────────────────────────────┘ │
│       ↓                         │
│  ┌─────────────────────────────┐ │
│  │   Runtime Data Areas        │ │ ← Then this
│  │  • Method Area              │ │
│  │  • JVM Stacks               │ │
│  │  • Heap                     │ │
│  └─────────────────────────────┘ │
│       ↓                         │
│  ┌─────────────────────────────┐ │
│  │    Execution Engine         │ │ ← Finally this
│  │  • Interpreter Loop         │ │
│  │  • Instruction Set          │ │
│  └─────────────────────────────┘ │
└─────────────────────────────────┘
```

## Next Steps

1. **Complete the TODOs in the current phase**
2. **Test with real classpath**: Create a simple Java class and test loading it
3. **Move to Phase 1.2**: Implement basic class file parsing
4. **Gradually build up the instruction set**

Remember: Each piece builds on the previous one, so master each phase before moving to the next! 