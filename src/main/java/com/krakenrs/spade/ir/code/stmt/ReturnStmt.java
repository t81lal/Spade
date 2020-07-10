package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class ReturnStmt extends Stmt {

    private final LoadLocalExpr var;

    @Inject
    public ReturnStmt(@Assisted LoadLocalExpr var) {
        super(Opcodes.RETURN);
        this.var = var;

        if(var != null) {
            var.setParent(this);
        }
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitReturnStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceReturnStmt(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((ReturnStmt) u).var, var);
    }
}
