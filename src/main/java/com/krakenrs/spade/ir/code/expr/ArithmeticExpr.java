package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.CodeVisitor;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.type.ValueType;

public class ArithmeticExpr extends Expr {

    public static enum Operation {
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

        private Operation(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final Operation operation;
    private final ValueExpr<?> lhs, rhs;

    public ArithmeticExpr(ValueType resultType, Operation operation, ValueExpr<?> lhs, ValueExpr<?> rhs) {
        super(Opcodes.ARITHMETIC, resultType);
        this.operation = operation;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(CodeVisitor vis) {
        vis.visitArithmeticExpr(this);
    }

    public Operation op() {
        return operation;
    }

    public ValueExpr<?> lhs() {
        return lhs;
    }

    public ValueExpr<?> rhs() {
        return rhs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operation, lhs, rhs);
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
}
