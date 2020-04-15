package com.krakenrs.spade.ir.code.expr.value;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Constant;

public class LoadConstExpr<T> extends ValueExpr<Constant<T>> {
    public LoadConstExpr(ValueType type, Constant<T> value) {
        super(Opcodes.LOAD_CONST, type, value);
    }
}
