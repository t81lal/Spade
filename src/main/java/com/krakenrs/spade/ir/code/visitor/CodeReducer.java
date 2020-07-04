package com.krakenrs.spade.ir.code.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Stmt;
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

import lombok.NonNull;

public interface CodeReducer {

    default LoadLocalExpr reduceExprToLocal(Expr e) {
        return reduceExprTo(e, LoadLocalExpr.class);
    }
    
    default ValueExpr<?> reduceExprToValue(Expr e) {
        return reduceExprTo(e, ValueExpr.class);
    }
    
    default <T extends Expr> T reduceExprTo(@NonNull Expr e, @NonNull Class<T> expected) {
        Expr reduced = e.reduceExpr(this);
        
        if(reduced == null) {
            return null;
        }
        
        if (expected.isAssignableFrom(reduced.getClass())) {
            return expected.cast(reduced);
        } else {
            throw new IllegalArgumentException("Expr cannot be reduced to " + expected.getCanonicalName());
        }
    }
    
    default <E extends Expr> List<E> reduceExprListTo(List<? extends Expr> list, Function<Expr, E> reducer) {
        List<E> bounds = new ArrayList<>();

        for (var oldE : list) {
            var newE = reducer.apply(oldE);

            if (newE == null) {
                return null;
            } else {
                bounds.add(newE);
            }
        }
        
        return bounds;
    }
    
    default Stmt reduceAssignArrayStmt(AssignArrayStmt s) { return s; }

    default Stmt reduceAssignCatchStmt(AssignCatchStmt s) { return s; }

    default Stmt reduceAssignFieldStmt(AssignFieldStmt s) { return s; }

    default Stmt reduceAssignLocalStmt(AssignLocalStmt s) { return s; }

    default Stmt reduceAssignParamStmt(AssignParamStmt s) { return s; }

    default Stmt reduceAssignPhiStmt(AssignPhiStmt s) { return s; }

    default Stmt reduceConsumeStmt(ConsumeStmt s) { return s; }

    default Stmt reduceJumpCondStmt(JumpCondStmt s) { return s; }

    default Stmt reduceJumpSwitchStmt(JumpSwitchStmt s) { return s; }

    default Stmt reduceJumpUncondStmt(JumpUncondStmt s) { return s; }

    default Stmt reduceMonitorStmt(MonitorStmt s) { return s; }

    default Stmt reduceReturnStmt(ReturnStmt s) { return s; }

    default Stmt reduceThrowStmt(ThrowStmt s) { return s; }

    default Expr reduceLoadLocalExpr(LoadLocalExpr e) { return e; }

    default Expr reduceLoadConstExpr(LoadConstExpr<?> e) { return e; }

    default Expr reduceAllocArrayExpr(AllocArrayExpr e) { return e; }

    default Expr reduceAllocObjectExpr(AllocObjectExpr e) { return e; }

    default Expr reduceArithmeticExpr(ArithmeticExpr e) { return e; }

    default Expr reduceNegateExpr(NegateExpr e) { return e; }

    default Expr reduceArrayLengthExpr(ArrayLengthExpr e) { return e; }

    default Expr reduceCastExpr(CastExpr e) { return e; }

    default Expr reduceCompareExpr(CompareExpr e) { return e; }

    default Expr reduceInstanceOfExpr(InstanceOfExpr e) { return e; }

    default Expr reduceInvokeExpr(InvokeExpr e) { return e; }

    default Expr reduceLoadArrayExpr(LoadArrayExpr e) { return e; }

    default Expr reduceLoadFieldExpr(LoadFieldExpr e) { return e; }
}
