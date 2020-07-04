package com.krakenrs.spade.ir.code.visitor;

import static com.krakenrs.spade.util.Nulls.anyNull;

import java.util.List;

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

public class AbstractCodeReducer implements CodeReducer {

    @Override
    public Stmt reduceAssignArrayStmt(AssignArrayStmt s) {
        var array = reduceExprToLocal(s.array());
        var index = reduceExprToValue(s.index());
        var value = reduceExprToValue(s.value());

        if (anyNull(array, index, value)) {
            return s;
        } else {
            return new AssignArrayStmt(array, index, value);
        }
    }

    @Override
    public Stmt reduceAssignCatchStmt(AssignCatchStmt s) {
        return s;
    }

    @Override
    public Stmt reduceAssignFieldStmt(AssignFieldStmt s) {
        if (!s.isStatic()) {
            var accessor = reduceExprToLocal(((AssignFieldStmt.AssignVirtualFieldStmt) s).accessor());
            var value = reduceExprToValue(s.value());

            if (anyNull(accessor, value)) {
                return s;
            } else {
                return new AssignFieldStmt.AssignVirtualFieldStmt(s.owner(), s.name(), s.fieldType(), value, accessor);
            }
        } else {
            var value = reduceExprToValue(s.value());

            if (value == null) {
                return s;
            } else {
                return new AssignFieldStmt.AssignStaticFieldStmt(s.owner(), s.name(), s.fieldType(), value);
            }
        }
    }

    @Override
    public Stmt reduceAssignLocalStmt(AssignLocalStmt s) {
        var value = s.value().reduceExpr(this);

        if (value == null) {
            return s;
        } else {
            return new AssignLocalStmt(s.var(), value);
        }
    }

    @Override
    public Stmt reduceAssignParamStmt(AssignParamStmt s) {
        return s;
    }

    @Override
    public Stmt reduceAssignPhiStmt(AssignPhiStmt s) {
        return s;
    }

    @Override
    public Stmt reduceConsumeStmt(ConsumeStmt s) {
        var expr = s.expr().reduceExpr(this);

        if (expr == null) {
            return s;
        } else {
            return new ConsumeStmt(expr);
        }
    }

    @Override
    public Stmt reduceJumpCondStmt(JumpCondStmt s) {
        var lhs = reduceExprToValue(s.lhs());
        var rhs = reduceExprToValue(s.rhs());

        if (anyNull(lhs, rhs)) {
            return s;
        } else {
            return new JumpCondStmt(lhs, rhs, s.mode(), s.target());
        }
    }

    @Override
    public Stmt reduceJumpSwitchStmt(JumpSwitchStmt s) {
        var expr = reduceExprToValue(s.expr());

        if (expr == null) {
            return s;
        } else {
            return new JumpSwitchStmt(expr, s.cases(), s.defaultCase());
        }
    }

    @Override
    public Stmt reduceJumpUncondStmt(JumpUncondStmt s) {
        return s;
    }

    @Override
    public Stmt reduceMonitorStmt(MonitorStmt s) {
        var var = reduceExprToLocal(s.var());

        if (var == null) {
            return s;
        } else {
            return new MonitorStmt(var, s.mode());
        }
    }

    @Override
    public Stmt reduceReturnStmt(ReturnStmt s) {
        if (s.var() == null) {
            // void return
            return s;
        } else {
            var var = reduceExprToLocal(s.var());
            if (var == null) {
                return s;
            } else {
                return new ReturnStmt(var);
            }
        }
    }

    @Override
    public Stmt reduceThrowStmt(ThrowStmt s) {
        var var = reduceExprToLocal(s.var());
        if (var == null) {
            return s;
        } else {
            return new ThrowStmt(var);
        }
    }

    @Override
    public Expr reduceLoadLocalExpr(LoadLocalExpr e) {
        return e;
    }

    @Override
    public Expr reduceLoadConstExpr(LoadConstExpr<?> e) {
        return e;
    }

    @Override
    public Expr reduceAllocArrayExpr(AllocArrayExpr e) {
        if (e.getBounds().size() == 0) {
            return e;
        }

        List<ValueExpr<?>> bounds = reduceExprListTo(e.getBounds(), this::reduceExprToValue);
        if (bounds == null) {
            return e;
        } else {
            return new AllocArrayExpr(e.getType(), bounds);
        }
    }

    @Override
    public Expr reduceAllocObjectExpr(AllocObjectExpr e) {
        return e;
    }

    @Override
    public Expr reduceArithmeticExpr(ArithmeticExpr e) {
        var lhs = reduceExprToValue(e.lhs());
        var rhs = reduceExprToValue(e.rhs());

        if (anyNull(lhs, rhs)) {
            return e;
        } else {
            return new ArithmeticExpr(e.getType(), e.op(), lhs, rhs);
        }
    }

    @Override
    public Expr reduceNegateExpr(NegateExpr e) {
        var var = reduceExprToLocal(e.var());

        if (var == null) {
            return e;
        } else {
            return new NegateExpr(var);
        }
    }

    @Override
    public Expr reduceArrayLengthExpr(ArrayLengthExpr e) {
        var var = reduceExprToLocal(e.var());

        if (var == null) {
            return e;
        } else {
            return new ArrayLengthExpr(var);
        }
    }

    @Override
    public Expr reduceCastExpr(CastExpr e) {
        var var = reduceExprToLocal(e.var());

        if (var == null) {
            return e;
        } else {
            return new CastExpr(e.getType(), var);
        }
    }

    @Override
    public Expr reduceCompareExpr(CompareExpr e) {
        var lhs = reduceExprToLocal(e.lhs());
        var rhs = reduceExprToLocal(e.rhs());

        if (anyNull(lhs, rhs)) {
            return e;
        } else {
            return new CompareExpr(lhs, rhs, e.op());
        }
    }

    @Override
    public Expr reduceInstanceOfExpr(InstanceOfExpr e) {
        var var = reduceExprToLocal(e.var());

        if (var == null) {
            return e;
        } else {
            return new InstanceOfExpr(var, e.checkType());
        }
    }

    @Override
    public Expr reduceInvokeExpr(InvokeExpr e) {
        if (e instanceof InvokeExpr.InvokeVirtualExpr) {
            var accessor = reduceExprToLocal(((InvokeExpr.InvokeVirtualExpr) e).accessor());
            if (accessor == null) {
                return e;
            }

            List<ValueExpr<?>> args = reduceExprListTo(e.arguments(), this::reduceExprToValue);
            if (args == null) {
                return e;
            } else {
                return new InvokeExpr.InvokeVirtualExpr(e.owner(), e.name(), e.methodType(), e.mode(), accessor, args);
            }
        } else if (e instanceof InvokeExpr.InvokeStaticExpr) {
            List<ValueExpr<?>> args = reduceExprListTo(e.arguments(), this::reduceExprToValue);
            if (args == null) {
                return e;
            } else {
                return new InvokeExpr.InvokeStaticExpr(e.owner(), e.name(), e.methodType(), args);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Expr reduceLoadArrayExpr(LoadArrayExpr e) {
        var array = reduceExprToLocal(e.array());
        var index = reduceExprToValue(e.index());

        if (anyNull(array, index)) {
            return e;
        } else {
            return new LoadArrayExpr(e.getType(), array, index);
        }
    }

    @Override
    public Expr reduceLoadFieldExpr(LoadFieldExpr e) {
        if (e.isStatic()) {
            return e;
        } else {
            var accessor = reduceExprToLocal(((LoadFieldExpr.LoadVirtualFieldExpr) e).accessor());
            if (accessor == null) {
                return e;
            } else {
                return new LoadFieldExpr.LoadVirtualFieldExpr(e.owner(), e.name(), e.getType(), accessor);
            }
        }
    }
}
