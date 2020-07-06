package com.krakenrs.spade.ir.code.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.UnresolvedClassType;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;

public class SSADefUseMapTest {

    static Local lvar(int index, int version) {
        return new Local(index, false, version);
    }

    @Test
    void testAddStmtNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.addStmt(null));
    }

    @Test
    void testRemoveStmtNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.removeStmt(null));
    }

    @Test
    void testReplaceDefNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.replaceDef(null));
    }

    @Test
    void testGetDefNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.getDef(null));
    }

    @Test
    void testGetUsesNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.getUses(null));
    }

    @Test
    void testIsDefinedNPE() {
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(NullPointerException.class, () -> map.isDefined(null));
    }

    @Test
    void testAddDeclStmtLhsNonSSA() {
        /* @param x (non ssa) */
        SSADefUseMap map = new SSADefUseMap();
        assertThrows(IllegalArgumentException.class, () -> map.addStmt(new AssignParamStmt(new Local(0, false))));
    }

    @Test
    void testAddDeclStmtRhsNonSSA() {
        /* @param y (non ssa)
         * x = y */
        /* This is impossible to setup as y cannot be defined as an unversioned local */
    }

    @Test
    void testAddParamDeclStmt() {
        /* @param x */
        SSADefUseMap map = new SSADefUseMap();
        var x = lvar(0, 0);

        AssignParamStmt paramDef = new AssignParamStmt(x);
        map.addStmt(paramDef);

        assertTrue(map.isDefined(x));
        assertSame(paramDef, map.getDef(x));
        /* param doesn't add uses */
        assertEquals(Set.of(), map.getUses(x));
    }

    @Test
    void testAddExprDeclStmt() {
        /* @param y
         * @param z
         * x = y + z */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(0, 0);
        var y = lvar(1, 0);
        var z = lvar(2, 0);

        map.addStmt(new AssignParamStmt(y));
        map.addStmt(new AssignParamStmt(z));

        assertTrue(map.isDefined(y));
        assertTrue(map.isDefined(z));

        var loadY = new LoadLocalExpr(PrimitiveType.INT, y);
        var loadZ = new LoadLocalExpr(PrimitiveType.INT, z);

        AssignLocalStmt exprDef = new AssignLocalStmt(x,
                new ArithmeticExpr(PrimitiveType.INT, ArithmeticExpr.Operation.ADD, loadY, loadZ));

        map.addStmt(exprDef);

        assertTrue(map.isDefined(x));

        assertEquals(Set.of(), map.getUses(x));
        assertEquals(Set.of(new ExprUse(loadY)), map.getUses(y));
        assertEquals(Set.of(new ExprUse(loadZ)), map.getUses(z));
    }

    @Test
    void testUndefinedLocalUseStmt() {
        /* x = y with y undefined */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(0, 0);
        var y = lvar(1, 0);

        assertFalse(map.isDefined(x));
        assertFalse(map.isDefined(y));

        var loadY = new LoadLocalExpr(PrimitiveType.INT, y);

        AssignLocalStmt copyStmt = new AssignLocalStmt(x, loadY);

        assertThrows(IllegalStateException.class, () -> map.addStmt(copyStmt));
    }

    @Test
    void testRedefineDeclStmt() {
        /* x = 1
         * x = 2 */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(0, 0);

        assertFalse(map.isDefined(x));

        var def1Stmt = new AssignLocalStmt(x, new LoadConstExpr<>(new Constant<Integer>(1, PrimitiveType.INT)));
        var def2Stmt = new AssignLocalStmt(x, new LoadConstExpr<>(new Constant<Integer>(2, PrimitiveType.INT)));

        map.addStmt(def1Stmt);

        assertTrue(map.isDefined(x));
        assertEquals(def1Stmt, map.getDef(x));

        assertThrows(UnsupportedOperationException.class, () -> map.addStmt(def2Stmt));
    }

    @Test
    void testAddPhiDeclStmt() {
        /* L1: x1
         * L2: x2
         * L3: x3 = PHI{ L1: x1, L2: x2 } */
        SSADefUseMap map = new SSADefUseMap();

        var x1 = lvar(0, 1);
        var x2 = lvar(0, 2);
        var x3 = lvar(0, 3);

        var l1 = new CodeBlock(1);
        var a1 = new AssignParamStmt(x1);
        l1.appendStmt(a1);
        map.addStmt(a1);

        var l2 = new CodeBlock(2);
        var a2 = new AssignParamStmt(x2);
        l2.appendStmt(a2);
        map.addStmt(a2);

        var l3 = new CodeBlock(3);
        var phi = new AssignPhiStmt(x3, Map.of(l1, x1, l2, x2));
        l3.appendStmt(phi);
        map.addStmt(phi);

        assertEquals(a1, map.getDef(x1));
        assertEquals(a2, map.getDef(x2));
        assertEquals(phi, map.getDef(x3));

        assertEquals(Set.of(new PhiUse(l1, x1, phi)), map.getUses(x1));
        assertEquals(Set.of(new PhiUse(l2, x2, phi)), map.getUses(x2));
        assertEquals(Set.of(), map.getUses(x3));
    }

    @Test
    void testAddNonDeclStmt() {
        /* @param x
         * @param y
         * consume(TestClass.testMethod(x, y)) */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(0, 0);
        var y = lvar(1, 0);

        assertFalse(map.isDefined(x));
        assertFalse(map.isDefined(y));

        var xDecl = new AssignParamStmt(x);
        var yDecl = new AssignParamStmt(y);

        map.addStmt(xDecl);
        map.addStmt(yDecl);

        assertEquals(xDecl, map.getDef(x));
        assertEquals(yDecl, map.getDef(y));

        var xUse = new LoadLocalExpr(PrimitiveType.INT, x);
        var yUse = new LoadLocalExpr(PrimitiveType.INT, y);

        var testStmt = new ConsumeStmt(new InvokeExpr.InvokeStaticExpr(new UnresolvedClassType("TestClass"),
                "testMethod", new MethodType(List.of(PrimitiveType.INT, PrimitiveType.INT), PrimitiveType.INT),
                List.of(xUse, yUse)));

        map.addStmt(testStmt);

        assertEquals(Set.of(new ExprUse(xUse)), map.getUses(x));
        assertEquals(Set.of(new ExprUse(yUse)), map.getUses(y));
    }

    @Test
    void testRemoveUnusedDeclStmt() {
        /* @param x
         * then remove*/
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(1, 0);
        var declStmt = new AssignParamStmt(x);
        map.addStmt(declStmt);

        assertTrue(map.isDefined(x));
        assertEquals(declStmt, map.getDef(x));
        assertEquals(Set.of(), map.getUses(x));

        map.removeStmt(declStmt);

        assertFalse(map.isDefined(x));
    }

    @Test
    void testRemoveUndeclaredLocalStmt() {
        /* remove @param x without initial decl add */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(1, 0);
        var declStmt = new AssignParamStmt(x);

        assertThrows(IllegalStateException.class, () -> map.removeStmt(declStmt));
    }

    @Test
    void testRemoveStillUsedLocalStmt() {
        /* @param x
         * y = x
         * remove x */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(1, 0);
        var y = lvar(2, 0);

        var declStmt = new AssignParamStmt(x);
        map.addStmt(declStmt);

        var useStmt = new AssignLocalStmt(y, new LoadLocalExpr(PrimitiveType.INT, x));
        map.addStmt(useStmt);

        assertTrue(map.isDefined(x));
        assertTrue(map.getUses(x).size() > 0);

        assertThrows(UnsupportedOperationException.class, () -> map.removeStmt(declStmt));
    }

    @Test
    void testRemoveUnUsedLocalStmt() {
        /* @param x
         * y = x
         * remove y = x */
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(1, 0);
        var y = lvar(2, 0);

        var declStmt = new AssignParamStmt(x);
        map.addStmt(declStmt);

        var useStmt = new AssignLocalStmt(y, new LoadLocalExpr(PrimitiveType.INT, x));
        map.addStmt(useStmt);

        assertTrue(map.isDefined(x));
        assertTrue(map.getUses(x).size() == 1);

        map.removeStmt(useStmt);

        assertTrue(map.isDefined(x));
        assertFalse(map.isDefined(y));
        assertEquals(Set.of(), map.getUses(x));
    }

    @Test
    void testRemovePhiDeclStmt() {
        /* L1: x1
         * L2: x2
         * L3: x3 = PHI{ L1: x1, L2: x2 }
         * then remove x3 = PHI... */
        SSADefUseMap map = new SSADefUseMap();

        var x1 = lvar(0, 1);
        var x2 = lvar(0, 2);
        var x3 = lvar(0, 3);

        var l1 = new CodeBlock(1);
        var a1 = new AssignParamStmt(x1);
        l1.appendStmt(a1);
        map.addStmt(a1);

        var l2 = new CodeBlock(2);
        var a2 = new AssignParamStmt(x2);
        l2.appendStmt(a2);
        map.addStmt(a2);

        var l3 = new CodeBlock(3);
        var phi = new AssignPhiStmt(x3, Map.of(l1, x1, l2, x2));
        l3.appendStmt(phi);
        map.addStmt(phi);

        assertEquals(a1, map.getDef(x1));
        assertEquals(a2, map.getDef(x2));
        assertEquals(phi, map.getDef(x3));

        assertEquals(Set.of(new PhiUse(l1, x1, phi)), map.getUses(x1));
        assertEquals(Set.of(new PhiUse(l2, x2, phi)), map.getUses(x2));
        assertEquals(Set.of(), map.getUses(x3));

        map.removeStmt(phi);

        assertFalse(map.isDefined(x3));
        assertEquals(Set.of(), map.getUses(x1));
        assertEquals(Set.of(), map.getUses(x2));
    }

    @Test
    void testReplaceDeclStmt() {
        /* x = 1
         * y = x
         * replace x = 1 with x = 2*/
        SSADefUseMap map = new SSADefUseMap();

        var x = lvar(1, 0);
        var y = lvar(2, 0);

        var xDecl = new AssignLocalStmt(x, new LoadConstExpr<>(new Constant<Integer>(1, PrimitiveType.INT)));
        map.addStmt(xDecl);

        var xUse = new LoadLocalExpr(PrimitiveType.INT, x);
        var yDecl = new AssignLocalStmt(y, xUse);
        map.addStmt(yDecl);

        assertEquals(xDecl, map.getDef(x));
        assertEquals(yDecl, map.getDef(y));
        assertEquals(Set.of(new ExprUse(xUse)), map.getUses(x));
        assertEquals(Set.of(), map.getUses(y));

        var newXDecl = new AssignLocalStmt(x, new LoadConstExpr<>(new Constant<Integer>(2, PrimitiveType.INT)));
        map.replaceDef(newXDecl);

        assertEquals(newXDecl, map.getDef(x));
        assertEquals(yDecl, map.getDef(y));
        assertEquals(Set.of(new ExprUse(xUse)), map.getUses(x));
        assertEquals(Set.of(), map.getUses(y));
    }
}
