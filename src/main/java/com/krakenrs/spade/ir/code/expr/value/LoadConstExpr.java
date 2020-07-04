package com.krakenrs.spade.ir.code.expr.value;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.value.Constant;

public class LoadConstExpr<T> extends ValueExpr<Constant<T>> {
    public LoadConstExpr(Constant<T> value) {
        super(Opcodes.LOAD_CONST, value.type(), value);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceLoadConstExpr(this);
    }
}
