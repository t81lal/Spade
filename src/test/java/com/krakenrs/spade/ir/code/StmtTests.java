package com.krakenrs.spade.ir.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignStaticFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignVirtualFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignLocalStmt;
import com.krakenrs.spade.ir.code.stmt.ConsumeStmt;
import com.krakenrs.spade.ir.code.stmt.JumpCondStmt;
import com.krakenrs.spade.ir.code.stmt.JumpSwitchStmt;
import com.krakenrs.spade.ir.code.stmt.MonitorStmt;
import com.krakenrs.spade.ir.code.stmt.ReturnStmt;
import com.krakenrs.spade.ir.code.stmt.ThrowStmt;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.UnresolvedClassType;
import com.krakenrs.spade.ir.value.Local;

public class StmtTests {

    public static LoadLocalExpr child() {
        return new LoadLocalExpr(PrimitiveType.INT, new Local(0, false, 0));
    }

    public static LoadLocalExpr child(int index) {
        return new LoadLocalExpr(PrimitiveType.INT, new Local(index, false, 0));
    }

    public static Local var() {
        return new Local(0, false, 0);
    }

    @Test
    void testSetParent_AssignArrayStmt() {
        var array = child();
        var index = child();
        var value = child();

        var stmt = new AssignArrayStmt(array, index, value);

        assertEquals(stmt, array.getParent());
        assertEquals(stmt, index.getParent());
        assertEquals(stmt, value.getParent());
    }

    @Test
    void testParent_AssignStaticFieldStmt() {
        var value = child();

        var stmt = new AssignStaticFieldStmt(new UnresolvedClassType("TestClass"), "fieldF", PrimitiveType.INT, value);

        assertEquals(stmt, value.getParent());
    }

    @Test
    void testParent_AssignVirtualFieldStmt() {
        var value = child();
        var accessor = child();

        var stmt = new AssignVirtualFieldStmt(new UnresolvedClassType("TestClass"), "fieldF", PrimitiveType.INT, value,
                accessor);

        assertEquals(stmt, value.getParent());
        assertEquals(stmt, accessor.getParent());
    }

    @Test
    void testParent_AssignLocalStmt() {
        var value = child();

        var stmt = new AssignLocalStmt(var(), value);

        assertEquals(stmt, value.getParent());
    }

    @Test
    void testParent_ConsumeStmt() {
        var expr = child();

        var stmt = new ConsumeStmt(expr);

        assertEquals(stmt, expr.getParent());
    }

    @Test
    void testParent_JumpCondStmt() {
        var lhs = child();
        var rhs = child();

        var stmt = new JumpCondStmt(lhs, rhs, JumpCondStmt.Mode.EQ, MockCodeFactory.makeBlock(1));

        assertEquals(stmt, lhs.getParent());
        assertEquals(stmt, rhs.getParent());
    }

    @Test
    void testParent_JumpSwitchStmt() {
        var expr = child();

        var stmt = new JumpSwitchStmt(expr, Map.of(), MockCodeFactory.makeBlock(1));

        assertEquals(stmt, expr.getParent());
    }

    @Test
    void testParent_MonitorStmt() {
        var var = child();

        var stmt = new MonitorStmt(var, MonitorStmt.Mode.ENTER);

        assertEquals(stmt, var.getParent());
    }

    @Test
    void testParent_ReturnStmt() {
        var var = child();

        var stmt = new ReturnStmt(var);

        assertEquals(stmt, var.getParent());
    }

    @Test
    void testParent_ThrowStmt() {
        var var = child();

        var stmt = new ThrowStmt(var);

        assertEquals(stmt, var.getParent());
    }
}
