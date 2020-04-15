package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.type.PrimitiveType;

public class CompareExpr extends Expr {

    public static enum Operation {
        LT, GT, NONE
    }

    private final LoadLocalExpr lhs, rhs;
    private final Operation operation;

    public CompareExpr(LoadLocalExpr lhs, LoadLocalExpr rhs, Operation operation) {
        super(Opcodes.COMPARE, PrimitiveType.INT);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
    }

    public LoadLocalExpr lhs() {
        return lhs;
    }

    public LoadLocalExpr rhs() {
        return rhs;
    }

    public Operation operation() {
        return operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lhs, rhs, operation);
    }
}
