package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;

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

    private LoadLocalExpr lhs, rhs;
    private Operation operation;

    public CompareExpr(LoadLocalExpr lhs, LoadLocalExpr rhs, Operation operation) {
        super(Opcodes.COMPARE, PrimitiveType.INT);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;

        addChild(lhs);
        addChild(rhs);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitCompareExpr(this);
    }

    public LoadLocalExpr lhs() {
        return lhs;
    }

    public void setLhs(LoadLocalExpr lhs) {
        Objects.requireNonNull(lhs);
        removeChild(this.lhs);
        this.lhs = lhs;
        addChild(lhs);
        notifyParent();
    }

    public LoadLocalExpr rhs() {
        return rhs;
    }

    public void setRhs(LoadLocalExpr rhs) {
        Objects.requireNonNull(rhs);
        removeChild(this.rhs);
        this.rhs = rhs;
        addChild(rhs);
        notifyParent();
    }

    public Operation op() {
        return operation;
    }

    public void setOp(Operation operation) {
        this.operation = Objects.requireNonNull(operation);
        notifyParent();
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
}
