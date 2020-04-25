package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

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

    private Operation operation;
    private ValueExpr<?> lhs, rhs;

    public ArithmeticExpr(ValueType resultType, Operation operation, ValueExpr<?> lhs, ValueExpr<?> rhs) {
        super(Opcodes.ARITHMETIC, resultType);
        this.operation = Objects.requireNonNull(operation);
        this.lhs = Objects.requireNonNull(lhs);
        this.rhs = Objects.requireNonNull(rhs);

        addChild(lhs);
        addChild(rhs);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitArithmeticExpr(this);
    }

    @Override
    public void setType(ValueType type) {
        // TODO: figure out if this is a good idea
        throw new UnsupportedOperationException();
    }

    public Operation op() {
        return operation;
    }

    public void setOp(Operation operation) {
        this.operation = Objects.requireNonNull(operation);
        notifyParent();
    }

    public ValueExpr<?> lhs() {
        return lhs;
    }

    public void setLhs(ValueExpr<?> lhs) {
        checkArgType(lhs, rhs);
        removeChild(this.lhs);
        this.lhs = lhs;
        addChild(lhs);
        notifyParent();
    }

    public ValueExpr<?> rhs() {
        return rhs;
    }

    public void setRhs(ValueExpr<?> rhs) {
        checkArgType(rhs, lhs);
        removeChild(this.rhs);
        this.rhs = rhs;
        addChild(rhs);
        notifyParent();
    }

    private void checkArgType(ValueExpr<?> newArg, ValueExpr<?> otherArg) {
        Objects.requireNonNull(newArg);
        if (!newArg.type().equals(otherArg.type())) {
            throw new IllegalArgumentException("Expected " + otherArg.type() + " but got " + newArg.type());
        }
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
