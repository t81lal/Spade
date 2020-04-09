package com.krakenrs.spade.ir.type;

public class AnyClassType extends UnresolvedClassType {
    public static final AnyClassType INSTANCE = new AnyClassType();

    private AnyClassType() {
        super("<any>");
    }
}
