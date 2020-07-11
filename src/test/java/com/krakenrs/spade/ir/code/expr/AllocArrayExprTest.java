package com.krakenrs.spade.ir.code.expr;

import static com.krakenrs.spade.ir.code.StmtTests.child;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.type.PrimitiveType;

public class AllocArrayExprTest {
    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new AllocArrayExpr(PrimitiveType.INT, null));
        assertThrows(NullPointerException.class, () -> new AllocArrayExpr(null, List.of(child())));

        // empty bounds
        assertThrows(IllegalArgumentException.class, () -> new AllocArrayExpr(PrimitiveType.INT, List.of()));
    }

    @Test
    void testSetBoundParent() {
        List<ValueExpr<?>> children = List.of(child(), child());
        var expr = new AllocArrayExpr(PrimitiveType.INT, children);
        for (var c : children) {
            assertEquals(expr, c.getParent());
        }
    }
}
