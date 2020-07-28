package com.krakenrs.spade.ir.type.infer;

import com.krakenrs.spade.ir.type.ValueType;

public class BottomType implements ValueType {
    public static final BottomType INST = new BottomType();

    private BottomType() {
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "BTM";
    }
}
