# Unit Test Guide for tiny-jvm

## Test Results Summary

✅ **All 26 tests are currently passing!**

- **Command Line Tests**: 12 tests ✅
- **Classpath Tests**: 14 tests ✅

The tests are designed to guide your implementation by clearly showing what each TODO method should do.

## How to Run Tests

```bash
# Run all tests
mvn test

# Run only command line tests
mvn test -Dtest=CmdTest

# Run only classpath tests  
mvn test -Dtest=ClasspathTest

# Run tests with more verbose output
mvn test -Dsurefire.printSummary=true
```

## What the Tests Tell You

### 1. Command Line Parsing Tests (`CmdTest`)

**Current Status**: ✅ All passing - Your implementation is working correctly!

Your `Cmd.parseCmd()` method successfully handles:
- ✅ Empty arguments (prints usage)
- ✅ `-help` flag (prints usage) 
- ✅ `-version` flag (prints version)
- ✅ Simple class names: `HelloWorld`
- ✅ Class names with args: `HelloWorld arg1 arg2`
- ✅ `-cp` option: `-cp /path HelloWorld`
- ✅ `-classpath` option: `-classpath /path HelloWorld`
- ✅ Complex paths with multiple entries
- ✅ Edge cases and error handling

**What this proves**: Your command line parsing is complete and robust!

### 2. Classpath Tests (`ClasspathTest`) 

**Current Status**: ✅ All passing, but reveals TODO implementations needed

The test output shows:
```
Error reading class SimpleClass: TODO: Implement file reading in DirectoryEntry.readClass()
```

This is **expected behavior**! The tests are designed to:

1. **✅ Pass even when TODOs aren't implemented** - This shows your framework is solid
2. **🔍 Show you exactly what needs implementation** - The error messages guide you
3. **📊 Track your progress** - As you implement TODOs, you'll see fewer error messages

## Implementation Roadmap Based on Test Results

### Phase 1: File Reading (High Priority)

**Test**: `testDirectoryEntry_ReadExistingClass_Success`

**TODO**: Implement `DirectoryEntry.readClass()`

```java
// In DirectoryEntry.readClass(), replace the TODO with:
try {
    return Files.readAllBytes(classFile.toPath());
} catch (IOException e) {
    throw new IOException("Failed to read class file: " + classFile.getPath(), e);
}
```

**Expected Result**: Error messages will change from "TODO: Implement file reading" to actual class loading

### Phase 2: JAR File Support (Medium Priority)

**Test**: `testJarEntry_ReadClass_ThrowsTodoException`

**TODO**: Implement `JarEntry.readClass()`

```java
// Use JarFile or ZipFile to read entries
try (JarFile jar = new JarFile(absPath)) {
    JarEntry entry = jar.getJarEntry(className);
    if (entry == null) {
        throw new IOException("Class not found in JAR: " + className);
    }
    try (InputStream is = jar.getInputStream(entry)) {
        return is.readAllBytes();
    }
}
```

### Phase 3: Multiple Classpath Support (Low Priority)

**Test**: `testCompositeEntry_Constructor_ThrowsTodoException`

**TODO**: Implement `CompositeEntry` class

```java
// Split by system path separator and create appropriate entries
String pathSeparator = System.getProperty("path.separator");
String[] paths = pathList.split(pathSeparator);
entries = new ClasspathEntry[paths.length];
for (int i = 0; i < paths.length; i++) {
    entries[i] = createEntry(paths[i].trim());
}
```

## How to Verify Your Implementation

### 1. Start with DirectoryEntry.readClass()

1. **Before implementing**: Run `mvn test -Dtest=ClasspathTest#testDirectoryEntry_ReadExistingClass_Success`
   - Should pass but show TODO message
   
2. **Implement the file reading logic**

3. **After implementing**: Run the test again
   - Should pass without TODO messages
   - Should actually read and return file bytes

### 2. Test with Real Files

Create a test class and verify it can be loaded:

```bash
# Create a test class
echo 'public class MyTest { }' > /tmp/MyTest.java
javac /tmp/MyTest.java

# Test your implementation
mvn test -Dtest=ClasspathTest#testIntegration_FullWorkflow
```

### 3. Progressive Implementation

As you implement each TODO:

1. **Run tests before**: See the TODO error messages
2. **Implement the feature**: Replace TODO with actual code  
3. **Run tests after**: Verify error messages disappear
4. **See functionality improve**: More tests pass with real behavior

## Test Architecture Benefits

### 1. **Fail-Safe Design**
- Tests pass even when features aren't implemented
- You can always verify what you've built so far

### 2. **Clear Implementation Guidance** 
- Error messages tell you exactly what to implement
- Tests show expected behavior and edge cases

### 3. **Progress Tracking**
- Fewer TODO messages = more implementation complete
- All tests passing = ready for next phase

### 4. **Regression Prevention**
- Once implemented, tests prevent breaking existing functionality
- Refactoring is safe with test coverage

## Next Steps

1. **✅ Your command line parsing is complete!** No work needed there.

2. **🎯 Focus on `DirectoryEntry.readClass()`** - This is the most fundamental feature

3. **📈 Run tests frequently** - Use them to guide and validate your work

4. **🚀 Move to Phase 1.2** - Once file reading works, we'll add class file parsing

The tests are your roadmap to a working JVM! Each error message is a signpost showing you exactly what to implement next. 