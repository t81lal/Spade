package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class NegateExpr extends Expr {
    private final LoadLocalExpr var;

    public NegateExpr(LoadLocalExpr var) {
        super(Opcodes.NEGATE, var.getType());
        this.var = var;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitNegateExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceNegateExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((NegateExpr) u).var, var);
    }
}
