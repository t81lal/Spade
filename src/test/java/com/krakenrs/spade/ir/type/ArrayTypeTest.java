package com.krakenrs.spade.ir.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArrayTypeTest {

    @Test
    void testToString() {
        assertEquals("[I", new ArrayType(PrimitiveType.INT).toString());
        assertEquals("[[I", new ArrayType(new ArrayType(PrimitiveType.INT)).toString());
        assertEquals("[Ljava/lang/Object;",
                new ArrayType(new UnresolvedClassType("java/lang/Object").asValueType()).toString());
    }
}
