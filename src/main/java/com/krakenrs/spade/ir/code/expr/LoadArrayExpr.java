package com.krakenrs.spade.ir.code.expr;

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

    public LoadArrayExpr(ValueType componentType, LoadLocalExpr array, ValueExpr<?> index) {
        super(Opcodes.LOAD_ARR, componentType);
        this.array = array;
        this.index = index;
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
