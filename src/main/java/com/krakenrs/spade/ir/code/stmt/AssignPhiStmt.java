package com.krakenrs.spade.ir.code.stmt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.value.Local;

public class AssignPhiStmt extends DeclareLocalStmt {
    private final Map<CodeBlock, Local> arguments;

    @Inject
    public AssignPhiStmt(@Assisted Local var, @Assisted Map<CodeBlock, Local> arguments) {
        super(Opcodes.ASSIGN_PHI, var);
        this.arguments = Collections.unmodifiableMap(arguments);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignPhiStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignPhiStmt(this);
    }

    public Map<CodeBlock, Local> arguments() {
        return arguments;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((AssignPhiStmt) u).arguments, arguments);
    }

    @Override
    public AssignPhiStmt copy(Local newVar) {
        return new AssignPhiStmt(newVar, arguments);
    }

    @Override
    public AssignPhiStmt deepCopy() {
        return new AssignPhiStmt(var, arguments);
    }
}
