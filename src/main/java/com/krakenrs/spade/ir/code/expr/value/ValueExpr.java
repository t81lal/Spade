package com.krakenrs.spade.ir.code.expr.value;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Value;

public abstract class ValueExpr<T extends Value> extends Expr {

    private final T value;

    public ValueExpr(int opcode, ValueType type, T value) {
        super(opcode, type);
        this.value = value;
    }

    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
