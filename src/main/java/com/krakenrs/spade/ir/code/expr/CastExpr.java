package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

public class CastExpr extends Expr {
    private final LoadLocalExpr var;

    public CastExpr(ValueType type, LoadLocalExpr var) {
        super(Opcodes.CAST, type);
        this.var = var;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitCastExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceCastExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((CastExpr) u).var, var);
    }
}
