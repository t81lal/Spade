package com.krakenrs.spade.ir.code.expr;

import java.util.List;
import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.type.ArrayType;

public class AllocArrayExpr extends Expr {

    private final List<ValueExpr<?>> bounds;

    public AllocArrayExpr(ArrayType type, List<ValueExpr<?>> bounds) {
        super(Opcodes.ALLOCARR, type);
        this.bounds = bounds;
    }

    public List<ValueExpr<?>> bounds() {
        return bounds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bounds);
    }
}
