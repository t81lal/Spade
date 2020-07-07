package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class ThrowStmt extends Stmt {

    private final LoadLocalExpr var;

    public ThrowStmt(LoadLocalExpr var) {
        super(Opcodes.THROW);
        this.var = var;

        var.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitThrowStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceThrowStmt(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((ThrowStmt) u).var, var);
    }
}
