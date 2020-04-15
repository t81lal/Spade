package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.type.ValueType;

public class NegateExpr extends Expr {
    private final LoadLocalExpr var;

    public NegateExpr(ValueType type, LoadLocalExpr var) {
        super(Opcodes.NEGATE, type);
        this.var = var;
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var);
    }
}
