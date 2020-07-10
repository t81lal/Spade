package com.krakenrs.spade.ir.code.expr;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

public class LoadArrayExpr extends Expr {

    private LoadLocalExpr array;
    private ValueExpr<?> index;

    @Inject
    public LoadArrayExpr(@Assisted ValueType componentType, @Assisted LoadLocalExpr array, @Assisted ValueExpr<?> index) {
        super(Opcodes.LOAD_ARR, componentType);
        this.array = array;
        this.index = index;

        array.setParent(this);
        index.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitLoadArrayExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceLoadArrayExpr(this);
    }

    public LoadLocalExpr array() {
        return array;
    }

    public ValueExpr<?> index() {
        return index;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            LoadArrayExpr lae = (LoadArrayExpr) u;
            return equivalent(lae.array, array) && equivalent(lae.index, index);
        } else {
            return false;
        }
    }
}
