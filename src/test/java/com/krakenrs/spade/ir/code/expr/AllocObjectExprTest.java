package com.krakenrs.spade.ir.code.expr;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AllocObjectExprTest {
    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new AllocObjectExpr(null));
    }
}
