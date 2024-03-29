package com.krakenrs.spade.ir.code.observer;

import com.krakenrs.spade.ir.code.Stmt;

public interface CodeObservationManager {
    void addCodeObserver(CodeObserver codeObserver);

    void removeCodeObserver(CodeObserver codeObserver);

    void notifyStmtAdded(Stmt stmt);

    void notifyStmtRemoved(Stmt stmt);

    void notifyStmtReplaced(Stmt oldStmt, Stmt newStmt);
}
