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

import com.krakenrs.spade.ir.code.expr.ArithmeticExpr.Operation;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

public class ArithmeticExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new ArithmeticExpr(null, Operation.ADD, child(), child()));
        assertThrows(NullPointerException.class, () -> new ArithmeticExpr(PrimitiveType.INT, null, child(), child()));
        assertThrows(NullPointerException.class,
                () -> new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, null, child()));
        assertThrows(NullPointerException.class,
                () -> new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(), null));
    }

    @Test
    void testGetters() {
        var l = child();
        var r = child();
        var expr = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, l, r);

        assertEquals(PrimitiveType.INT, expr.getType());
        assertEquals(Operation.ADD, expr.getOperation());
        assertEquals(l, expr.getLhs());
        assertEquals(r, expr.getRhs());
    }

    @Test
    void testSetChildParents() {
        var l = child();
        var r = child();
        var expr = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, l, r);

        assertEquals(expr, l.getParent());
        assertEquals(expr, r.getParent());
    }

    @Test
    void testCodeVisitor() {
        var expr = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(), child());
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitArithmeticExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(), child());
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceArithmeticExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(), child());
        var e2 = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(), child());

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentTypes() {
        var e1 = new ArithmeticExpr(PrimitiveType.INT, Operation.ADD, child(1), child(2));
        var e2 = new ArithmeticExpr(PrimitiveType.BYTE, Operation.SUB, child(1), child(2));

        assertFalse(e1.equivalent(e2));
    }
}
