package com.krakenrs.spade.ir.code.analysis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.type.PrimitiveType;
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
}
