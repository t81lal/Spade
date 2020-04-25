package com.krakenrs.spade.ir.code.expr.value;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Value;

public abstract class ValueExpr<T extends Value> extends Expr {

    private T value;

    public ValueExpr(int opcode, ValueType type, T value) {
        super(opcode, type);
        this.value = Objects.requireNonNull(value);
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.setValue(type(), value);
    }

    public void setValue(ValueType type, T value) {
        // TODO: do real type checking here
        this.value = Objects.requireNonNull(value);
        this.type = Objects.requireNonNull(type);

        notifyParent();
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitValueExpr(this);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            return Objects.equals(((ValueExpr<?>) u).value, value);
        } else {
            return false;
        }
    }
}
