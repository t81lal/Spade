package com.krakenrs.spade.ir.code.stmt;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.value.Local;

public class AssignLocalStmt extends DeclareLocalStmt {
    private final Expr value;

    public AssignLocalStmt(Local var, Expr value) {
        super(Opcodes.ASSIGN_LOCAL, var);
        this.value = value;
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
        return new AssignLocalStmt(newVar, value);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignLocalStmt(this);
    }
}
