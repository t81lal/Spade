package com.krakenrs.spade.ir.code.factory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.value.Constant;

public class ConstCodeFactoryTest {

    @Test
    void testCreateLoadConstExpr() {
        var factory = new SimpleConstCodeFactory();

        Constant<Integer> cst = new Constant<Integer>(1, PrimitiveType.INT);
        LoadConstExpr<Integer> ex = new LoadConstExpr<>(cst);
        LoadConstExpr<Integer> actual = factory.createLoadConstExpr(cst);

        assertTrue(ex.equivalent(actual), () -> {
            return "Expected: " + ex + ", got: " + actual;
        });
    }
}
