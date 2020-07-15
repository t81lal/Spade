package com.krakenrs.spade.ir.code.analysis;

import java.util.Set;

import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;

public class PartialPhiDef extends Def2 {
    public PartialPhiDef(AssignPhiStmt stmt) {
        super(stmt);
    }

    public PartialPhiDef(AssignPhiStmt stmt, Set<Use> uses) {
        super(stmt, uses);
    }

    @Override
    public AssignPhiStmt getStmt() {
        return (AssignPhiStmt) super.getStmt();
    }
}
