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
import com.krakenrs.spade.ir.code.expr.InvokeExpr.InvokeVirtualExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr.LoadVirtualFieldExpr;
import com.krakenrs.spade.ir.code.expr.NegateExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignArrayStmt;
import com.krakenrs.spade.ir.code.stmt.AssignCatchStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt;
import com.krakenrs.spade.ir.code.stmt.AssignFieldStmt.AssignVirtualFieldStmt;
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

public class AbstractCodeVisitor implements CodeVisitor {

    @Override
    public void visitAny(CodeUnit u) {
    }

    @Override
    public void visitAssignArrayStmt(AssignArrayStmt s) {
        s.array().accept(this);
        s.index().accept(this);
        s.value().accept(this);
    }

    @Override
    public void visitAssignCatchStmt(AssignCatchStmt s) {
    }

    @Override
    public void visitAssignFieldStmt(AssignFieldStmt s) {
        if (!s.isStatic()) {
            ((AssignVirtualFieldStmt) s).accessor().accept(this);
        }
        s.value().accept(this);
    }

    @Override
    public void visitAssignLocalStmt(AssignLocalStmt s) {
        s.value().accept(this);
    }

    @Override
    public void visitAssignParamStmt(AssignParamStmt s) {
    }

    @Override
    public void visitAssignPhiStmt(AssignPhiStmt s) {
    }

    @Override
    public void visitConsumeStmt(ConsumeStmt s) {
        s.expr().accept(this);
    }

    @Override
    public void visitJumpCondStmt(JumpCondStmt s) {
        s.lhs().accept(this);
        s.rhs().accept(this);
    }

    @Override
    public void visitJumpSwitchStmt(JumpSwitchStmt s) {
        s.expr().accept(this);
    }

    @Override
    public void visitJumpUncondStmt(JumpUncondStmt s) {
    }

    @Override
    public void visitMonitorStmt(MonitorStmt s) {
        s.var().accept(this);
    }

    @Override
    public void visitReturnStmt(ReturnStmt s) {
    	if(s.var() != null) {
            s.var().accept(this);
    	}
    }

    @Override
    public void visitThrowStmt(ThrowStmt s) {
        s.var().accept(this);
    }

    @Override
    public void visitValueExpr(ValueExpr<?> e) {
    }

    @Override
    public void visitAllocArrayExpr(AllocArrayExpr e) {
        for (ValueExpr<?> bE : e.getBounds()) {
            bE.accept(this);
        }
    }

    @Override
    public void visitAllocObjectExpr(AllocObjectExpr e) {
    }

    @Override
    public void visitArithmeticExpr(ArithmeticExpr e) {
        e.lhs().accept(this);
        e.rhs().accept(this);
    }

    @Override
    public void visitNegateExpr(NegateExpr e) {
        e.var().accept(this);
    }

    @Override
    public void visitArrayLengthExpr(ArrayLengthExpr e) {
        e.var().accept(this);
    }

    @Override
    public void visitCastExpr(CastExpr e) {
        e.var().accept(this);
    }

    @Override
    public void visitCompareExpr(CompareExpr e) {
        e.lhs().accept(this);
        e.rhs().accept(this);
    }

    @Override
    public void visitInstanceOfExpr(InstanceOfExpr e) {
        e.var().accept(this);
    }

    @Override
    public void visitInvokeExpr(InvokeExpr e) {
        if (!e.mode().equals(InvokeExpr.Mode.STATIC)) {
            ((InvokeVirtualExpr) e).accessor().accept(this);
        }
        for (ValueExpr<?> aE : e.arguments()) {
            aE.accept(this);
        }
    }

    @Override
    public void visitLoadArrayExpr(LoadArrayExpr e) {
        e.array().accept(this);
        e.index().accept(this);
    }

    @Override
    public void visitLoadFieldExpr(LoadFieldExpr e) {
        if (!e.isStatic()) {
            ((LoadVirtualFieldExpr) e).accessor().accept(this);
        }
    }
}
