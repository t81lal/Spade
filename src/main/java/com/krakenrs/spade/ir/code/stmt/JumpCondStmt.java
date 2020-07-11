package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

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

    @Inject
    public JumpCondStmt(@Assisted("left") ValueExpr<?> lhs, @Assisted("right") ValueExpr<?> rhs, @Assisted Mode mode, @Assisted CodeBlock target) {
        super(Opcodes.JUMP_COND);
        this.lhs = lhs;
        this.rhs = rhs;
        this.mode = mode;
        this.target = target;

        lhs.setParent(this);
        rhs.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitJumpCondStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceJumpCondStmt(this);
    }

    public ValueExpr<?> lhs() {
        return lhs;
    }

    public ValueExpr<?> rhs() {
        return rhs;
    }

    public Mode mode() {
        return mode;
    }

    public CodeBlock target() {
        return target;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            JumpCondStmt jcs = (JumpCondStmt) u;
            return Objects.equals(jcs.mode, mode) && Objects.equals(jcs.target, target) && equivalent(jcs.lhs, lhs)
                    && equivalent(jcs.rhs, rhs);
        } else {
            return false;
        }
    }

    @Override
    public JumpCondStmt deepCopy() {
        return new JumpCondStmt(lhs.deepCopy(), rhs.deepCopy(), mode, target);
    }
}
