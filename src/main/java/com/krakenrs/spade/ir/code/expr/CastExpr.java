package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

public class CastExpr extends Expr {

    private LoadLocalExpr var;

    public CastExpr(ValueType type, LoadLocalExpr var) {
        super(Opcodes.CAST, type);
        this.var = var;

        addChild(var);
    }

    @Override
    public void setType(ValueType type) {
        // TODO: check whether var is assignable to this new type
        super.setType(type); // calls notifyParent
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitCastExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public void setVar(LoadLocalExpr var) {
        Objects.requireNonNull(var);

        removeChild(this.var);
        this.var = var;
        addChild(var);

        notifyParent();
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((CastExpr) u).var, var);
    }
}
