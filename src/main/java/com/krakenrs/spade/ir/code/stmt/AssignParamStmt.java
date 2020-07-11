package com.krakenrs.spade.ir.code.stmt;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.value.Local;

public class AssignParamStmt extends DeclareLocalStmt {
    @Inject
    public AssignParamStmt(@Assisted Local var) {
        super(Opcodes.ASSIGN_PARAM, var);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignParamStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignParamStmt(this);
    }

    @Override
    public AssignParamStmt copy(Local newVar) {
        return new AssignParamStmt(newVar);
    }

    @Override
    public AssignParamStmt deepCopy() {
        return new AssignParamStmt(var);
    }
}
