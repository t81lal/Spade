package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

import lombok.Getter;
import lombok.NonNull;

public class ArithmeticExpr extends Expr {

    public enum Operation {
        ADD("+"),
        SUB("-"),
        DIV("/"),
        MUL("*"),
        MOD("%"),
        SHL("<<"),
        SHR(">>"),
        USHR(">>>"),
        OR("|"),
        AND("&"),
        XOR("^");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    @Getter
    private final Operation operation;
    @Getter
    private final ValueExpr<?> lhs, rhs;

    @Inject
    public ArithmeticExpr(@Assisted @NonNull ValueType resultType, @Assisted @NonNull Operation operation,
            @Assisted("left") @NonNull ValueExpr<?> lhs, @Assisted("right") @NonNull ValueExpr<?> rhs) {
        super(Opcodes.ARITHMETIC, resultType);
        this.operation = operation;
        this.lhs = lhs;
        this.rhs = rhs;

        lhs.setParent(this);
        rhs.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitArithmeticExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceArithmeticExpr(this);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            ArithmeticExpr ae = (ArithmeticExpr) u;
            return equivalent(lhs, ae.lhs) && equivalent(rhs, ae.rhs) && Objects.equals(operation, ae.operation);
        } else {
            return false;
        }
    }

    @Override
    public ArithmeticExpr deepCopy() {
        return new ArithmeticExpr(type, operation, lhs.deepCopy(), rhs.deepCopy());
    }
}
