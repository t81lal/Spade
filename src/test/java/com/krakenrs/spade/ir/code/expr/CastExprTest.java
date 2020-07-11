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

public class CastExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new CastExpr(PrimitiveType.INT, null));
        assertThrows(NullPointerException.class, () -> new CastExpr(null, child()));
    }

    @Test
    void testGetters() {
        var var = child();
        var expr = new CastExpr(PrimitiveType.INT, var);

        assertEquals(var, expr.var());
        assertEquals(PrimitiveType.INT, expr.getType());
    }

    @Test
    void testSetChildParent() {
        var var = child();
        var expr = new CastExpr(PrimitiveType.INT, var);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testCodeVisitor() {
        var expr = new CastExpr(PrimitiveType.INT, child());
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitCastExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new CastExpr(PrimitiveType.INT, child());
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceCastExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new CastExpr(PrimitiveType.INT, child());
        var e2 = new CastExpr(PrimitiveType.INT, child());

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentTypes() {
        var e1 = new CastExpr(PrimitiveType.INT, child(1));
        var e2 = new CastExpr(PrimitiveType.INT, child(2));

        assertFalse(e1.equivalent(e2));
    }
}
