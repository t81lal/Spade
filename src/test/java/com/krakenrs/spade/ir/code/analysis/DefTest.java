package com.krakenrs.spade.ir.code.analysis;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.code.MockCodeFactory;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.AssignParamStmt;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.value.Local;

import nl.jqno.equalsverifier.EqualsVerifier;

public class DefTest {

//    @Test
//    void testEquals() {
//        var red = new AssignParamStmt(new Local(0, false, 0));
//        var blue = new AssignParamStmt(new Local(2, false, 1));
//
//        var redU = new ExprUse(new LoadLocalExpr(PrimitiveType.INT, new Local(0, false, 0)));
//        var b = MockCodeFactory.makeBlock(1);
//        var phiStmt = new AssignPhiStmt(new Local(1, false, 0), Map.of(b, new Local(0, false, 0)));
//        var blueU = new PhiUse(b, new Local(0, false, 0), phiStmt);
//
//        EqualsVerifier.forClass(Def.class).withIgnoredFields("uses").withPrefabValues(DeclareLocalStmt.class, red, blue)
//                .withPrefabValues(Use.class, redU, blueU).verify();
//    }
}
