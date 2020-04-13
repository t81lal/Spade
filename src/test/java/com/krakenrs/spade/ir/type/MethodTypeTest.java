package com.krakenrs.spade.ir.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class MethodTypeTest {

    @Test
    void testToString() {
        assertEquals("(II)V",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.VOID).toString());
        assertEquals("(II)I",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT).toString());
        assertEquals("([Ljava/lang/String;)V",
                new MethodType(List.of(new ArrayType(new UnresolvedClassType("java/lang/String").asValueType())),
                        PrimitiveType.VOID).toString());
    }
}
