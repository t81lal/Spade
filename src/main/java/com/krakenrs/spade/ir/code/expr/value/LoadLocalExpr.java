package com.krakenrs.spade.ir.code.expr.value;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class LoadLocalExpr extends ValueExpr<Local> {
    public LoadLocalExpr(ValueType type, Local value) {
        super(Opcodes.LOAD_LOCAL, type, value);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceLoadLocalExpr(this);
    }
}
