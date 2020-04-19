package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class ConsumeStmt extends Stmt {
    private final Expr expr;

    public ConsumeStmt(Expr expr) {
        super(Opcodes.CONSUME);
        this.expr = expr;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitConsumeStmt(this);
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((ConsumeStmt) u).expr, expr);
    }
}
