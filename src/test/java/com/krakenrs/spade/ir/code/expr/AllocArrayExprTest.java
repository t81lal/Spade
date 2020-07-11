package com.krakenrs.spade.ir.code.expr;

import static com.krakenrs.spade.ir.code.StmtTests.child;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

public class AllocArrayExprTest {
    @Test
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class, () -> new AllocArrayExpr(PrimitiveType.INT, null));
        assertThrows(NullPointerException.class, () -> new AllocArrayExpr(null, List.of(child())));

        // empty bounds
        assertThrows(IllegalArgumentException.class, () -> new AllocArrayExpr(PrimitiveType.INT, List.of()));
    }

    @Test
    void testGetters() {
        List<ValueExpr<?>> bounds = List.of(child(), child());
        var expr = new AllocArrayExpr(PrimitiveType.INT, bounds);
        assertEquals(PrimitiveType.INT, expr.getType());
        assertEquals(bounds, expr.getBounds());
    }

    @Test
    void testSetBoundParent() {
        List<ValueExpr<?>> children = List.of(child(), child());
        var expr = new AllocArrayExpr(PrimitiveType.INT, children);
        for (var c : children) {
            assertEquals(expr, c.getParent());
        }
    }

    @Test
    void testCodeVisitor() {
        var expr = new AllocArrayExpr(PrimitiveType.INT, List.of(child()));
        CodeVisitor vis = mock(CodeVisitor.class);
        expr.accept(vis);

        verify(vis).visitAny(expr);
        verify(vis).visitAllocArrayExpr(expr);
        verifyNoMoreInteractions(vis);
    }

    @Test
    void testCodeReducer() {
        var expr = new AllocArrayExpr(PrimitiveType.INT, List.of(child()));
        CodeReducer red = mock(CodeReducer.class);
        expr.reduceExpr(red);

        verify(red).reduceAllocArrayExpr(expr);
        verifyNoMoreInteractions(red);
    }

    @Test
    void testEquivalent() {
        var e1 = new AllocArrayExpr(PrimitiveType.INT, List.of(child()));
        var e2 = new AllocArrayExpr(PrimitiveType.INT, List.of(child()));

        assertTrue(e1.equivalent(e2));
    }

    @Test
    void testNotEquivalentBounds() {
        var e1 = new AllocArrayExpr(PrimitiveType.INT, List.of(child(), child()));
        var e2 = new AllocArrayExpr(PrimitiveType.INT, List.of(child()));

        assertFalse(e1.equivalent(e2));
    }
}
