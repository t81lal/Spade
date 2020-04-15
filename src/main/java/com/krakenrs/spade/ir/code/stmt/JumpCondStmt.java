package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;

public class JumpCondStmt extends Stmt {
    public enum Mode {
        EQ("=="), NE("!="), LT("<"), GE(">="), GT(">"), LE("<=");
        private final String symbol;

        private Mode(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final ValueExpr<?> lhs, rhs;
    private final Mode mode;
    private final CodeBlock target;

    public JumpCondStmt(ValueExpr<?> lhs, ValueExpr<?> rhs, Mode mode, CodeBlock target) {
        super(Opcodes.JUMP_COND);
        this.lhs = lhs;
        this.rhs = rhs;
        this.mode = mode;
        this.target = target;
    }

    public ValueExpr<?> getLhs() {
        return lhs;
    }

    public ValueExpr<?> getRhs() {
        return rhs;
    }

    public Mode getMode() {
        return mode;
    }

    public CodeBlock getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lhs, rhs, mode, target);
    }

}
