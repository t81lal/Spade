package com.krakenrs.spade.ir.code.analysis;

import java.util.Set;

import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;

public class CopyDef extends Def2 {
    public CopyDef(DeclareLocalStmt stmt) {
        super(stmt);
    }

    public CopyDef(DeclareLocalStmt stmt, Set<Use> uses) {
        super(stmt, uses);
    }
}
