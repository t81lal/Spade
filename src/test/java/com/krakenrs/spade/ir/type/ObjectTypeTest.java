package com.krakenrs.spade.ir.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ObjectTypeTest {

    @Test
    void testToString() {
        assertEquals("Ljava/lang/Object;", new ObjectType(new UnresolvedClassType("java/lang/Object")).toString());
        assertEquals("Lash/ir/ClassTypeTest;",
                new ObjectType(new UnresolvedClassType("ash/ir/ClassTypeTest")).toString());
    }
}
