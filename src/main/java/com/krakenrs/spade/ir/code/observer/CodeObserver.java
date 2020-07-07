package com.krakenrs.spade.ir.code.observer;

import com.krakenrs.spade.ir.code.Stmt;

public interface CodeObserver {
    void onStmtAdded(Stmt stmt);

    void onStmtRemoved(Stmt stmt);

    void onStmtReplaced(Stmt oldStmt, Stmt newStmt);
}
