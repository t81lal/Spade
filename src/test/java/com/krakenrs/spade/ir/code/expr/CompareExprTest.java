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

import com.krakenrs.spade.ir.code.expr.CompareExpr.Operation;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

public class CompareExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new CompareExpr(null, child(), Operation.GT));
        assertThrows(NullPointerException.class, () -> new CompareExpr(child(), null, Operation.GT));
        assertThrows(NullPointerException.class, () -> new CompareExpr(child(), child(), null));
    }

    @Test
    void testGetters() {
        var l = child();
        var r = child();
        var expr = new CompareExpr(l, r, Operation.NONE);

        assertEquals(Operation.NONE, expr.op());
        assertEquals(PrimitiveType.INT, expr.getType());
        assertEquals(l, expr.lhs());
        assertEquals(r, expr.rhs());
    }

    @Test
    void testSetChildParents() {
        var l = child();
        var r = child();
        var expr = new CompareExpr(l, r, Operation.NONE);

        assertEquals(expr, l.getParent());
        assertEquals(expr, r.getParent());
    }

    @Test
    void testCodeVisitor() {
        var expr = new CompareExpr(child(), child(), Operation.NONE);
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitCompareExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new CompareExpr(child(), child(), Operation.NONE);
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceCompareExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new CompareExpr(child(), child(), Operation.NONE);
        var e2 = new CompareExpr(child(), child(), Operation.NONE);

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentOps() {
        var e1 = new CompareExpr(child(), child(), Operation.NONE);
        var e2 = new CompareExpr(child(), child(), Operation.GT);

        assertFalse(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentLhs() {
        var e1 = new CompareExpr(child(1), child(), Operation.GT);
        var e2 = new CompareExpr(child(2), child(), Operation.GT);

        assertFalse(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentRhs() {
        var e1 = new CompareExpr(child(), child(1), Operation.GT);
        var e2 = new CompareExpr(child(), child(2), Operation.GT);

        assertFalse(e1.equivalent(e2));
    }
}
