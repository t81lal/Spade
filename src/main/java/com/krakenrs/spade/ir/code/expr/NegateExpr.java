package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class NegateExpr extends Expr {
    private final LoadLocalExpr var;

    public NegateExpr(LoadLocalExpr var) {
        super(Opcodes.NEGATE, var.type());
        this.var = var;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitNegateExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((NegateExpr) u).var, var);
    }
}
