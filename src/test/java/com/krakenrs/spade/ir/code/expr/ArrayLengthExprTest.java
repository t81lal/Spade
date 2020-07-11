package com.krakenrs.spade.ir.code.expr;

import static com.krakenrs.spade.ir.code.StmtTests.child;
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
import com.krakenrs.spade.ir.type.PrimitiveType;

public class ArrayLengthExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new ArrayLengthExpr(null));
    }

    @Test
    void testGetters() {
        var var = child();
        var expr = new ArrayLengthExpr(var);

        assertEquals(var, expr.var());
    }

    @Test
    void testType() {
        var expr = new ArrayLengthExpr(child());
        assertEquals(PrimitiveType.INT, expr.getType());
    }

    @Test
    void testSetChildParents() {
        var var = child();
        var expr = new ArrayLengthExpr(var);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testCodeVisitor() {
        var expr = new ArrayLengthExpr(child());
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitArrayLengthExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new ArrayLengthExpr(child());
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceArrayLengthExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new ArrayLengthExpr(child());
        var e2 = new ArrayLengthExpr(child());

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentTypes() {
        var e1 = new ArrayLengthExpr(child(1));
        var e2 = new ArrayLengthExpr(child(2));

        assertFalse(e1.equivalent(e2));
    }
}
