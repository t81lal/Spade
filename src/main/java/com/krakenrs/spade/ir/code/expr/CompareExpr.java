package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

import lombok.NonNull;

public class CompareExpr extends Expr {

    public enum Operation {
        LT("<"), GT(">"), NONE("==");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final LoadLocalExpr lhs, rhs;
    private final Operation operation;

    @Inject
    public CompareExpr(@Assisted("left") @NonNull LoadLocalExpr lhs, @Assisted("right") @NonNull LoadLocalExpr rhs,
            @Assisted @NonNull Operation operation) {
        super(Opcodes.COMPARE, PrimitiveType.INT);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;

        lhs.setParent(this);
        rhs.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitCompareExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceCompareExpr(this);
    }

    public LoadLocalExpr lhs() {
        return lhs;
    }

    public LoadLocalExpr rhs() {
        return rhs;
    }

    public Operation op() {
        return operation;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            CompareExpr ce = (CompareExpr) u;
            return equivalent(lhs, ce.lhs) && equivalent(rhs, ce.rhs) && Objects.equals(operation, ce.operation);
        } else {
            return false;
        }
    }

    @Override
    public CompareExpr deepCopy() {
        return new CompareExpr(lhs.deepCopy(), rhs.deepCopy(), operation);
    }
}
