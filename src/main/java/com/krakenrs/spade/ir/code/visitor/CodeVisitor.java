package com.krakenrs.spade.ir.code.visitor;

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

public interface CodeVisitor {
    void visitAny(CodeUnit u);

    void visitAssignArrayStmt(AssignArrayStmt s);

    void visitAssignCatchStmt(AssignCatchStmt s);

    void visitAssignFieldStmt(AssignFieldStmt s);

    void visitAssignLocalStmt(AssignLocalStmt s);

    void visitAssignParamStmt(AssignParamStmt s);

    void visitAssignPhiStmt(AssignPhiStmt s);

    void visitConsumeStmt(ConsumeStmt s);

    void visitJumpCondStmt(JumpCondStmt s);

    void visitJumpSwitchStmt(JumpSwitchStmt s);

    void visitJumpUncondStmt(JumpUncondStmt s);

    void visitMonitorStmt(MonitorStmt s);

    void visitReturnStmt(ReturnStmt s);

    void visitThrowStmt(ThrowStmt s);

    void visitValueExpr(ValueExpr<?> e);

    void visitAllocArrayExpr(AllocArrayExpr e);

    void visitAllocObjectExpr(AllocObjectExpr e);

    void visitArithmeticExpr(ArithmeticExpr e);

    void visitNegateExpr(NegateExpr e);

    void visitArrayLengthExpr(ArrayLengthExpr e);

    void visitCastExpr(CastExpr e);

    void visitCompareExpr(CompareExpr e);

    void visitInstanceOfExpr(InstanceOfExpr e);

    void visitInvokeExpr(InvokeExpr e);

    void visitLoadArrayExpr(LoadArrayExpr e);

    void visitLoadFieldExpr(LoadFieldExpr e);
}
