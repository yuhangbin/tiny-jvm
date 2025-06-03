# Phase 1.2: Class File Parsing Roadmap

## 🎯 **Goal**: Parse `.class` files to extract bytecode and metadata

You now have the **framework** for class file parsing. Here's your implementation roadmap:

## 📁 **What You Have (Framework Created)**

```
src/main/java/com/tinyjvm/classfile/
├── ClassFile.java           ⚠️  Main class file representation
├── ClassReader.java         ⚠️  Binary data reader utility  
├── ConstantPool.java        ⚠️  Constant pool management
├── ConstantInfo.java        ⚠️  Base class for constants
├── MethodInfo.java          ⚠️  Method representation
├── FieldInfo.java           ⚠️  Field representation
└── AttributeInfo.java       ⚠️  Attributes (Code, etc.)
```

⚠️ = Framework created, needs implementation

## 🗂️ **Implementation Order (Start Here)**

### **Step 1: ClassReader (Foundation)**
**Priority: 🔥 CRITICAL - Everything depends on this**

**File**: `src/main/java/com/tinyjvm/classfile/ClassReader.java`

**What to implement:**
```java
public int readU1() {
    // Read 1 byte as unsigned integer
    if (position >= data.length) throw new RuntimeException("Unexpected end of data");
    return data[position++] & 0xFF;
}

public int readU2() {
    // Read 2 bytes in big-endian as unsigned integer
    return (readU1() << 8) | readU1();
}

public int readU4() {
    // Read 4 bytes in big-endian as integer
    return (readU2() << 16) | readU2();
}

public byte[] readBytes(int length) {
    // Read multiple bytes
    if (position + length > data.length) throw new RuntimeException("Unexpected end of data");
    byte[] result = new byte[length];
    System.arraycopy(data, position, result, 0, length);
    position += length;
    return result;
}
```

**Test**: Create a simple test to verify byte reading works correctly.

### **Step 2: Basic ConstantInfo Types**
**Priority: 🔥 HIGH - Needed for constant pool**

**File**: `src/main/java/com/tinyjvm/classfile/ConstantInfo.java`

**What to implement:**
1. **CONSTANT_Utf8** - For string constants
2. **CONSTANT_Class** - For class references
3. **CONSTANT_String** - For string literals

**Example for CONSTANT_Utf8:**
```java
class ConstantUtf8Info extends ConstantInfo {
    private String value;
    
    public ConstantUtf8Info(ClassReader reader) {
        super(CONSTANT_Utf8);
        int length = reader.readU2();
        byte[] bytes = reader.readBytes(length);
        this.value = new String(bytes, StandardCharsets.UTF_8);
    }
    
    public String getValue() { return value; }
}
```

### **Step 3: ConstantPool**
**Priority: 🔥 HIGH - Core component**

**File**: `src/main/java/com/tinyjvm/classfile/ConstantPool.java`

**What to implement:**
- Read constant pool count
- Parse each constant entry using `ConstantInfo.readConstant()`
- Implement `getUtf8()` and `getClassName()` methods

### **Step 4: ClassFile Constructor (Basic)**
**Priority: 🔶 MEDIUM - Integration**

**File**: `src/main/java/com/tinyjvm/classfile/ClassFile.java`

**What to implement:**
1. Read and verify magic number (0xCAFEBABE)
2. Read version info
3. Parse constant pool
4. Read basic class info (access flags, this class, super class)
5. Skip interfaces, fields, methods, attributes for now

### **Step 5: MethodInfo and FieldInfo**
**Priority: 🔶 MEDIUM - For finding main method**

**Files**: `MethodInfo.java`, `FieldInfo.java`

**What to implement:**
- Parse method/field access flags, name index, descriptor index
- Skip attributes for now
- Implement `MethodInfo.matches()` to find methods by name

### **Step 6: Basic AttributeInfo**
**Priority: 🔶 LOW - For bytecode extraction**

**File**: `src/main/java/com/tinyjvm/classfile/AttributeInfo.java`

**What to implement:**
- Basic attribute parsing
- Focus on **Code attribute** (contains bytecode)
- Skip exception table and nested attributes initially

## 🧪 **Testing Strategy**

### **Phase 1: Unit Tests**
Create tests for each component:

```java
@Test
public void testClassReader() {
    byte[] data = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
    ClassReader reader = new ClassReader(data);
    assertEquals(0xCAFEBABE, reader.readU4());
}

@Test  
public void testClassFile() {
    // Use SimpleClass.class from your test resources
    byte[] classData = Files.readAllBytes(Paths.get("src/test/resources/testclasses/SimpleClass.class"));
    ClassFile classFile = new ClassFile(classData);
    assertEquals(ClassFile.MAGIC, classFile.getMagic());
    assertEquals("SimpleClass", classFile.getClassName());
}
```

### **Phase 2: Integration Test**
Test with real `.class` file:

```java
@Test
public void testLoadSimpleClass() {
    Classpath cp = new Classpath("src/test/resources/testclasses");
    byte[] classData = cp.readClass("SimpleClass");
    ClassFile classFile = new ClassFile(classData);
    
    // Should find main method
    MethodInfo mainMethod = classFile.getMethod("main", "([Ljava/lang/String;)V");
    assertNotNull(mainMethod);
    
    // Should have bytecode
    CodeAttribute code = mainMethod.getCodeAttribute();
    assertNotNull(code);
    assertTrue(code.getCode().length > 0);
}
```

## 📚 **Resources**

### **JVM Specification References**
- [Class File Format](https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html)
- [Constant Pool](https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.4)
- [Method Descriptors](https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3.3)

### **Useful Commands**
```bash
# Examine .class file structure
javap -v SimpleClass

# Compile test class
javac src/test/resources/testclasses/SimpleClass.java

# Run tests
mvn test -Dtest=ClassFileTest
```

## 🎯 **Success Criteria**

By the end of Phase 1.2, you should be able to:

1. ✅ **Load a `.class` file**: `new ClassFile(classData)`
2. ✅ **Extract basic info**: class name, super class, methods
3. ✅ **Find the main method**: `classFile.getMethod("main", "...")`
4. ✅ **Get bytecode**: `mainMethod.getCodeAttribute().getCode()`

## 🚀 **Phase 1.3 Preview**

Once Phase 1.2 is complete, Phase 1.3 will focus on:
- **Runtime Data Areas** (Stack, Heap, Method Area)
- **Basic interpreter loop**
- **Simple instruction execution**

## 📝 **Quick Start**

1. **Start with ClassReader** - implement the 4 TODO methods
2. **Test immediately** - make sure byte reading works
3. **Move to ConstantInfo** - implement CONSTANT_Utf8 first
4. **Build incrementally** - test each component as you go

Remember: **Start small, test early, build incrementally!** 🔧 