package com.krakenrs.spade.ir.code.observer;

import java.util.ArrayList;
import java.util.List;

import com.krakenrs.spade.ir.code.Stmt;

public class DefaultCodeObservationManager implements CodeObservationManager {

    private final List<CodeObserver> codeObservers;

    public DefaultCodeObservationManager() {
        codeObservers = new ArrayList<>();
    }

    public void addCodeObserver(CodeObserver codeObserver) {
        this.codeObservers.add(codeObserver);
    }

    public void removeCodeObserver(CodeObserver codeObserver) {
        this.codeObservers.remove(codeObserver);
    }

    @Override
    public void notifyStmtAdded(Stmt stmt) {
        for (CodeObserver codeObserver : codeObservers) {
            codeObserver.onStmtAdded(stmt);
        }
    }

    @Override
    public void notifyStmtRemoved(Stmt stmt) {
        for (CodeObserver codeObserver : codeObservers) {
            codeObserver.onStmtRemoved(stmt);
        }
    }

    @Override
    public void notifyStmtReplaced(Stmt oldStmt, Stmt newStmt) {
        for (CodeObserver codeObserver : codeObservers) {
            codeObserver.onStmtReplaced(oldStmt, newStmt);
        }
    }
}
