package com.krakenrs.spade.ir.type;

public interface ValueType extends Type {
    /**
     * Get the JVM size of this type.
     *
     * @return Size, in bytes
     */
    int getSize();
}
