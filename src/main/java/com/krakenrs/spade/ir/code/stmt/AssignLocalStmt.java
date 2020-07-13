package com.krakenrs.spade.ir.code.stmt;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.value.Local;

public class AssignLocalStmt extends DeclareLocalStmt {
    private final Expr value;

    @Inject
    public AssignLocalStmt(@Assisted Local var, @Assisted Expr value) {
        super(Opcodes.ASSIGN_LOCAL, var);
        this.value = value;

        value.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignLocalStmt(this);
    }

    public Expr value() {
        return value;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((AssignLocalStmt) u).value, value);
    }

    @Override
    public AssignLocalStmt copy(Local newVar) {
        return new AssignLocalStmt(newVar, value.deepCopy());
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignLocalStmt(this);
    }

    @Override
    public AssignLocalStmt deepCopy() {
        return new AssignLocalStmt(var, value.deepCopy());
    }
}
