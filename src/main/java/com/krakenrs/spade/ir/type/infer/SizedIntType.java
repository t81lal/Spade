package com.krakenrs.spade.ir.type.infer;

import com.krakenrs.spade.ir.type.ValueType;

public class SizedIntType implements ValueType {
    public static final SizedIntType BOOL = new SizedIntType(0);
    public static final SizedIntType BYTE = new SizedIntType(1);
    public static final SizedIntType SHORT = new SizedIntType(2);

    private final int byteSize;

    private SizedIntType(int byteSize) {
        this.byteSize = byteSize;
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    public int getByteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        switch (byteSize) {
            case 0:
                return "[0..1]";
            case 1:
                return "[-128..127]";
            case 2:
                return "[-32,768..32,761]";
            default:
                throw new IllegalArgumentException();
        }
    }
}
