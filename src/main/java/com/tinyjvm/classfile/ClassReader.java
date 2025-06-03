package com.tinyjvm.classfile;

import java.util.HashSet;

/**
 * Utility class for reading binary data from .class files
 * Handles endianness and provides convenient methods for reading different data
 * types
 */
public class ClassReader {

    private byte[] data;
    private int position;

    public ClassReader(byte[] data) {
        this.data = data;
        this.position = 0;
    }

    /**
     * Read an unsigned 8-bit integer (byte)
     */
    public int readU1() {
        return data[position++] & 0xFF;
    }

    /**
     * Read an unsigned 16-bit integer (big-endian)
     */
    public int readU2() {
        return (data[position++] & 0xFF) << 8 | (data[position++] & 0xFF);
    }

    /**
     * Read an unsigned 32-bit integer (big-endian)
     */
    public int readU4() {
        return (data[position++] & 0xFF) << 24 | (data[position++] & 0xFF) << 16 | (data[position++] & 0xFF) << 8 | (data[position++] & 0xFF);
    }

    /**
     * Read multiple bytes
     */
    public byte[] readBytes(int length) { 
        byte[] bytes = new byte[length];
        System.arraycopy(data, position, bytes, 0, length);
        position += length;
        return bytes;
    }

    public int getPosition() {
        return position;
    }

    public int remaining() {
        return data.length - position;
    }
}