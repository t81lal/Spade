package com.krakenrs.spade.ir.code.vis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.expr.AllocArrayExpr;
import com.krakenrs.spade.ir.code.expr.AllocObjectExpr;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.ArrayLengthExpr;
import com.krakenrs.spade.ir.code.expr.CastExpr;
import com.krakenrs.spade.ir.code.expr.CompareExpr;
import com.krakenrs.spade.ir.code.expr.InstanceOfExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.code.stmt.JumpSwitchStmt;
import com.krakenrs.spade.ir.code.stmt.JumpUncondStmt;
import com.krakenrs.spade.ir.code.stmt.MonitorStmt;
import com.krakenrs.spade.ir.code.stmt.ReturnStmt;
import com.krakenrs.spade.ir.code.stmt.ThrowStmt;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeVisitor;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.UnresolvedClassType;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;

public class CodeVisitorTest {

    class MockCodeVisitor extends AbstractCodeVisitor {
        List<CodeUnit> searchPath = new ArrayList<>();
        List<String> callPath = new ArrayList<>();

        void addToCallPath() {
            StackTraceElement e = new Exception().getStackTrace()[1];
            callPath.add(e.getMethodName());
        }

        @Override
        public void visitAny(CodeUnit u) {
            searchPath.add(u);
        }

        @Override
        public void visitAssignArrayStmt(AssignArrayStmt s) {
            addToCallPath();
            super.visitAssignArrayStmt(s);
        }

        @Override
        public void visitAssignCatchStmt(AssignCatchStmt s) {
            addToCallPath();
            super.visitAssignCatchStmt(s);
        }

        @Override
        public void visitAssignFieldStmt(AssignFieldStmt s) {
            addToCallPath();
            super.visitAssignFieldStmt(s);
        }

        @Override
        public void visitAssignLocalStmt(AssignLocalStmt s) {
            addToCallPath();
            super.visitAssignLocalStmt(s);
        }

        @Override
        public void visitAssignParamStmt(AssignParamStmt s) {
            addToCallPath();
            super.visitAssignParamStmt(s);
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
            addToCallPath();
            super.visitAssignPhiStmt(s);
        }

        @Override
        public void visitConsumeStmt(ConsumeStmt s) {
            addToCallPath();
            super.visitConsumeStmt(s);
        }

        @Override
        public void visitJumpCondStmt(JumpCondStmt s) {
            addToCallPath();
            super.visitJumpCondStmt(s);
        }

        @Override
        public void visitJumpSwitchStmt(JumpSwitchStmt s) {
            addToCallPath();
            super.visitJumpSwitchStmt(s);
        }

        @Override
        public void visitJumpUncondStmt(JumpUncondStmt s) {
            addToCallPath();
            super.visitJumpUncondStmt(s);
        }

        @Override
        public void visitMonitorStmt(MonitorStmt s) {
            addToCallPath();
            super.visitMonitorStmt(s);
        }

        @Override
        public void visitReturnStmt(ReturnStmt s) {
            addToCallPath();
            super.visitReturnStmt(s);
        }

        @Override
        public void visitThrowStmt(ThrowStmt s) {
            addToCallPath();
            super.visitThrowStmt(s);
        }

        @Override
        public void visitValueExpr(ValueExpr<?> e) {
            addToCallPath();
            super.visitValueExpr(e);
        }

        @Override
        public void visitAllocArrayExpr(AllocArrayExpr e) {
            addToCallPath();
            super.visitAllocArrayExpr(e);
        }

        @Override
        public void visitAllocObjectExpr(AllocObjectExpr e) {
            addToCallPath();
            super.visitAllocObjectExpr(e);
        }

        @Override
        public void visitArithmeticExpr(ArithmeticExpr e) {
            addToCallPath();
            super.visitArithmeticExpr(e);
        }

        @Override
        public void visitNegateExpr(NegateExpr e) {
            addToCallPath();
            super.visitNegateExpr(e);
        }

        @Override
        public void visitArrayLengthExpr(ArrayLengthExpr e) {
            addToCallPath();
            super.visitArrayLengthExpr(e);
        }

        @Override
        public void visitCastExpr(CastExpr e) {
            addToCallPath();
            super.visitCastExpr(e);
        }

        @Override
        public void visitCompareExpr(CompareExpr e) {
            addToCallPath();
            super.visitCompareExpr(e);
        }

        @Override
        public void visitInstanceOfExpr(InstanceOfExpr e) {
            addToCallPath();
            super.visitInstanceOfExpr(e);
        }

        @Override
        public void visitInvokeExpr(InvokeExpr e) {
            addToCallPath();
            super.visitInvokeExpr(e);
        }

        @Override
        public void visitLoadArrayExpr(LoadArrayExpr e) {
            addToCallPath();
            super.visitLoadArrayExpr(e);
        }

        @Override
        public void visitLoadFieldExpr(LoadFieldExpr e) {
            addToCallPath();
            super.visitLoadFieldExpr(e);
        }
    }

    LoadConstExpr<Integer> cst(int x) {
        return new LoadConstExpr<>(new Constant<>(x, PrimitiveType.INT));
    }

    LoadLocalExpr local(int index) {
        return new LoadLocalExpr(PrimitiveType.INT, new Local(index, false));
    }

    @Test
    void testLoadConstExpr() {
        var e = cst(5);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e), v.searchPath);
        assertEquals(List.of("visitValueExpr"), v.callPath);
    }

    @Test
    void testLoadLocalExpr() {
        var e = local(1);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e), v.searchPath);
        assertEquals(List.of("visitValueExpr"), v.callPath);
    }

    @Test
    void testAllocArrayExpr() {
        var bounds = List.<ValueExpr<?>>of(cst(5));
        var e = new AllocArrayExpr(new ArrayType(PrimitiveType.INT), bounds);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, bounds.get(0)), v.searchPath);
        assertEquals(List.of("visitAllocArrayExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAllocObjectExpr() {
        var e = new AllocObjectExpr(new UnresolvedClassType("TestClass"));
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e), v.searchPath);
        assertEquals(List.of("visitAllocObjectExpr"), v.callPath);
    }

    @Test
    void testArithmeticExpr() {
        var l = cst(4);
        var r = cst(6);
        var e = new ArithmeticExpr(PrimitiveType.INT, ArithmeticExpr.Operation.ADD, l, r);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, l, r), v.searchPath);
        assertEquals(List.of("visitArithmeticExpr", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testArrayLengthExpr() {
        var x = local(1);
        var e = new ArrayLengthExpr(x);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, x), v.searchPath);
        assertEquals(List.of("visitArrayLengthExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testCastExpr() {
        var x = local(1);
        var e = new CastExpr(PrimitiveType.INT, x);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, x), v.searchPath);
        assertEquals(List.of("visitCastExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testCompareExpr() {
        var l = local(1);
        var r = local(2);
        var e = new CompareExpr(l, r, CompareExpr.Operation.NONE);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, l, r), v.searchPath);
        assertEquals(List.of("visitCompareExpr", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testInstanceOfExpr() {
        var x = local(1);
        var e = new InstanceOfExpr(x, PrimitiveType.INT);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, x), v.searchPath);
        assertEquals(List.of("visitInstanceOfExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testInvokeStaticExpr() {
        var args = List.<ValueExpr<?>>of(cst(4), local(2), cst(1));
        var e = new InvokeExpr.InvokeStaticExpr(new UnresolvedClassType("TestClass"), "testM",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.VOID),
                args);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, args.get(0), args.get(1), args.get(2)), v.searchPath);
        assertEquals(List.of("visitInvokeExpr", "visitValueExpr", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testInvokeVirtualExpr() {
        var acc = local(1);
        var args = List.<ValueExpr<?>>of(cst(4), local(2), cst(1));
        var e = new InvokeExpr.InvokeVirtualExpr(new UnresolvedClassType("TestClass"), "testM",
                new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.VOID),
                InvokeExpr.Mode.VIRTUAL, acc, args);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, acc, args.get(0), args.get(1), args.get(2)), v.searchPath);
        assertEquals(List.of("visitInvokeExpr", "visitValueExpr", "visitValueExpr", "visitValueExpr", "visitValueExpr"),
                v.callPath);
    }

    @Test
    void testLoadArrayExpr() {
        var arr = local(1);
        var val = cst(5);
        var e = new LoadArrayExpr(PrimitiveType.INT, arr, val);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, arr, val), v.searchPath);
        assertEquals(List.of("visitLoadArrayExpr", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testLoadStaticFieldExpr() {
        var e = new LoadFieldExpr.LoadStaticFieldExpr(new UnresolvedClassType("TestClass"), "testF", PrimitiveType.INT);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e), v.searchPath);
        assertEquals(List.of("visitLoadFieldExpr"), v.callPath);
    }

    @Test
    void testLoadVirtualFieldExpr() {
        var acc = local(1);
        var e = new LoadFieldExpr.LoadVirtualFieldExpr(new UnresolvedClassType("TestClass"), "testF",
                PrimitiveType.INT, acc);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, acc), v.searchPath);
        assertEquals(List.of("visitLoadFieldExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testNegateExpr() {
        var l = local(1);
        var e = new NegateExpr(l);
        var v = new MockCodeVisitor();
        e.accept(v);
        assertEquals(List.of(e, l), v.searchPath);
        assertEquals(List.of("visitNegateExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAssignArrayStmt() {
        var arr = local(1);
        var idx = cst(5);
        var val = cst(10);
        var s = new AssignArrayStmt(arr, idx, val);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, arr, idx, val), v.searchPath);
        assertEquals(List.of("visitAssignArrayStmt", "visitValueExpr", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAssignCatchStmt() {
        var s = new AssignCatchStmt(new Local(1, false), new UnresolvedClassType("TestClass"));
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s), v.searchPath);
        assertEquals(List.of("visitAssignCatchStmt"), v.callPath);
    }

    @Test
    void testAssignStaticFieldStmt() {
        var val = cst(5);
        var s = new AssignFieldStmt.AssignStaticFieldStmt(new UnresolvedClassType("TestClass"), "testF",
                PrimitiveType.INT, val);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, val), v.searchPath);
        assertEquals(List.of("visitAssignFieldStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAssignVirtualFieldStmt() {
        var acc = local(1);
        var val = cst(5);
        var s = new AssignFieldStmt.AssignVirtualFieldStmt(new UnresolvedClassType("TestClass"), "testF",
                PrimitiveType.INT, val, acc);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, acc, val), v.searchPath);
        assertEquals(List.of("visitAssignFieldStmt", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAssignLocalStmt() {
        var e = cst(5);
        var s = new AssignLocalStmt(new Local(1, false), e);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, e), v.searchPath);
        assertEquals(List.of("visitAssignLocalStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testAssignParamStmt() {
        var s = new AssignParamStmt(new Local(1, false));
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s), v.searchPath);
        assertEquals(List.of("visitAssignParamStmt"), v.callPath);
    }

    @Test
    void testAssignPhiStmt() {
        var s = new AssignPhiStmt(new Local(1, false), Map.of());
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s), v.searchPath);
        assertEquals(List.of("visitAssignPhiStmt"), v.callPath);
    }

    @Test
    void testConsumeStmt() {
        var e = cst(5);
        var s = new ConsumeStmt(e);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, e), v.searchPath);
        assertEquals(List.of("visitConsumeStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testJumpCondStmt() {
        var l = cst(1);
        var r = cst(2);
        var s = new JumpCondStmt(l, r, JumpCondStmt.Mode.EQ, new CodeBlock(1));
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, l, r), v.searchPath);
        assertEquals(List.of("visitJumpCondStmt", "visitValueExpr", "visitValueExpr"), v.callPath);
    }

    @Test
    void testJumpSwitchStmt() {
        var e = cst(5);
        var s = new JumpSwitchStmt(e, Map.of(), new CodeBlock(1));
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, e), v.searchPath);
        assertEquals(List.of("visitJumpSwitchStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testJumpUncondStmt() {
        var s = new JumpUncondStmt(new CodeBlock(1));
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s), v.searchPath);
        assertEquals(List.of("visitJumpUncondStmt"), v.callPath);
    }

    @Test
    void testMonitorStmt() {
        var l = local(1);
        var s = new MonitorStmt(l, MonitorStmt.Mode.ENTER);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, l), v.searchPath);
        assertEquals(List.of("visitMonitorStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testReturnStmt() {
        var l = local(1);
        var s = new ReturnStmt(l);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, l), v.searchPath);
        assertEquals(List.of("visitReturnStmt", "visitValueExpr"), v.callPath);
    }

    @Test
    void testThrowStmt() {
        var l = local(1);
        var s = new ThrowStmt(l);
        var v = new MockCodeVisitor();
        s.accept(v);
        assertEquals(List.of(s, l), v.searchPath);
        assertEquals(List.of("visitThrowStmt", "visitValueExpr"), v.callPath);
    }
}
