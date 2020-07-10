package com.krakenrs.spade.ir.code.stmt;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class ConsumeStmt extends Stmt {
    private final Expr expr;

    @Inject
    public ConsumeStmt(@Assisted Expr expr) {
        super(Opcodes.CONSUME);
        this.expr = expr;

        expr.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitConsumeStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceConsumeStmt(this);
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((ConsumeStmt) u).expr, expr);
    }
}
