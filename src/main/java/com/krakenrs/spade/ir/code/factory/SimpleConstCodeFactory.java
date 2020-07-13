package com.krakenrs.spade.ir.code.factory;

import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.value.Constant;

public class SimpleConstCodeFactory implements ConstCodeFactory {
    @Override
    public <T> LoadConstExpr<T> createLoadConstExpr(Constant<T> value) {
        return new LoadConstExpr<>(value);
    }
}
