package com.krakenrs.spade.ir.code.expr;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

import lombok.NonNull;

public class ArrayLengthExpr extends Expr {
    private final LoadLocalExpr var;

    @Inject
    public ArrayLengthExpr(@Assisted @NonNull LoadLocalExpr var) {
        super(Opcodes.ARRAYLEN, PrimitiveType.INT);
        this.var = var;

        var.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitArrayLengthExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceArrayLengthExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((ArrayLengthExpr) u).var, var);
    }

    @Override
    public Expr deepCopy() {
        return new ArrayLengthExpr(var.deepCopy());
    }
}
