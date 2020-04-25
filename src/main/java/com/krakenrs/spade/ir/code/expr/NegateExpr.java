package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;

public class NegateExpr extends Expr {

    private LoadLocalExpr var;

    public NegateExpr(LoadLocalExpr var) {
        super(Opcodes.NEGATE, var.type());
        this.var = validateVar(var);

        addChild(var);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitNegateExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public void setVar(LoadLocalExpr var) {
        validateVar(var);

        removeChild(this.var);
        this.var = var;
        addChild(var);

        notifyParent();
    }

    private LoadLocalExpr validateVar(LoadLocalExpr var) {
        final ValueType t = var.type();
        if (!(t instanceof PrimitiveType) || !((PrimitiveType) t).isIntLike()) {
            throw new IllegalArgumentException(var + " must be int type, was: " + t);
        }
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((NegateExpr) u).var, var);
    }
}
