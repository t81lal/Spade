package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.value.Local;

public class AssignCatchStmt extends DeclareLocalStmt {
    private final ClassType type;

    public AssignCatchStmt(Local var, ClassType type) {
        super(Opcodes.ASSIGN_CATCH, var);
        this.type = type;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignCatchStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignCatchStmt(this);
    }

    public ClassType type() {
        return type;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((AssignCatchStmt) u).type, type);
    }

    @Override
    public AssignCatchStmt copy(Local newVar) {
        return new AssignCatchStmt(newVar, type);
    }
}
