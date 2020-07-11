package com.krakenrs.spade.ir.code.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.UnresolvedClassType;

public class AllocObjectExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new AllocObjectExpr(null));
    }

    @Test
    void testGetters() {
        var cType = new UnresolvedClassType("TestClass");
        var expr = new AllocObjectExpr(cType);
        assertEquals(cType.asValueType(), expr.getType());
    }

    @Test
    void testCodeVisitor() {
        var expr = new AllocObjectExpr(new UnresolvedClassType("TestClass"));
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitAllocObjectExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new AllocObjectExpr(new UnresolvedClassType("TestClass"));
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceAllocObjectExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new AllocObjectExpr(new UnresolvedClassType("TestClass"));
        var e2 = new AllocObjectExpr(new UnresolvedClassType("TestClass"));

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentBounds() {
        var e1 = new AllocObjectExpr(new UnresolvedClassType("TestClass1"));
        var e2 = new AllocObjectExpr(new UnresolvedClassType("TestClass2"));

        assertFalse(e1.equivalent(e2));
    }
}
