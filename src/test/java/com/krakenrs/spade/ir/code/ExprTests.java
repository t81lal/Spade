package com.krakenrs.spade.ir.code;

import static com.krakenrs.spade.ir.code.StmtTests.child;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeStaticExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeVirtualExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadVirtualFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.NewObjectExpr;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.UnresolvedClassType;

public class ExprTests {

    @Test
    void testParent_AllocArrayExpr() {
        var b1 = child();
        var b2 = child();

        var expr = new AllocArrayExpr(PrimitiveType.INT, List.of(b1, b2));

        assertEquals(expr, b1.getParent());
        assertEquals(expr, b2.getParent());
    }

    @Test
    void testParent_ArithmeticExpr() {
        var lhs = child();
        var rhs = child();

        var expr = new ArithmeticExpr(PrimitiveType.INT, ArithmeticExpr.Operation.ADD, lhs, rhs);

        assertEquals(expr, lhs.getParent());
        assertEquals(expr, rhs.getParent());
    }

    @Test
    void testParent_ArrayLengthExpr() {
        var var = child();

        var expr = new ArrayLengthExpr(var);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testParent_CastExpr() {
        var var = child();

        var expr = new CastExpr(PrimitiveType.INT, var);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testParent_CompareExpr() {
        var lhs = child();
        var rhs = child();

        var expr = new CompareExpr(lhs, rhs, CompareExpr.Operation.NONE);

        assertEquals(expr, lhs.getParent());
        assertEquals(expr, rhs.getParent());
    }

    @Test
    void testParent_InstanceOfExpr() {
        var var = child();

        var expr = new InstanceOfExpr(var, PrimitiveType.INT);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testParent_InvokeVirtualExpr() {
        var accessor = child();
        var a1 = child();
        var a2 = child();

        var expr = new InvokeVirtualExpr(new UnresolvedClassType("TestClass"), "methodM",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT),
                InvokeExpr.Mode.VIRTUAL, accessor, List.of(a1, a2));

        assertEquals(expr, accessor.getParent());
        assertEquals(expr, a1.getParent());
        assertEquals(expr, a2.getParent());
    }

    @Test
    void testParent_InvokeStaticExpr() {
        var a1 = child();
        var a2 = child();

        var expr = new InvokeStaticExpr(new UnresolvedClassType("TestClass"), "methodM",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT), List.of(a1, a2));

        assertEquals(expr, a1.getParent());
        assertEquals(expr, a2.getParent());
    }

    @Test
    void testParent_LoadArrayExpr() {
        var array = child();
        var index = child();

        var expr = new LoadArrayExpr(PrimitiveType.INT, array, index);

        assertEquals(expr, array.getParent());
        assertEquals(expr, index.getParent());
    }

    @Test
    void testParent_LoadVirtualFieldExpr() {
        var accessor = child();

        var expr = new LoadVirtualFieldExpr(new UnresolvedClassType("TestClass"), "fieldF", PrimitiveType.INT,
                accessor);

        assertEquals(expr, accessor.getParent());
    }

    @Test
    void testParent_NegateExpr() {
        var var = child();

        var expr = new NegateExpr(var);

        assertEquals(expr, var.getParent());
    }

    @Test
    void testParent_NewObjectExpr() {
        var a1 = child();

        var expr = new NewObjectExpr(new UnresolvedClassType("TestClass"),
                new MethodType(List.of(PrimitiveType.INT), PrimitiveType.VOID), List.of(a1));

        assertEquals(expr, a1.getParent());
    }
}
