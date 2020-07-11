package com.krakenrs.spade.ir.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.ir.code.observer.CodeObservationManager;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

import lombok.NonNull;

public class CodeBlock implements Vertex {
    
    public static interface Factory {
        CodeBlock create(int id);
    }
    
    private final int id;
    private final List<Stmt> stmts = new ArrayList<>();
    private int orderHint;
    
    @NonNull
    private CodeObservationManager codeObservationManager;

    @Inject
    public CodeBlock(CodeObservationManager codeObservationManager, @Assisted int id) {
        this.codeObservationManager = codeObservationManager;
        this.id = id;
        this.orderHint = id;
    }

    public int getOrderHint() {
        return orderHint;
    }

    public void setOrderHint(int orderHint) {
        this.orderHint = orderHint;
    }

    public int id() {
        return id;
    }

    public int indexOf(Stmt stmt) {
        Objects.requireNonNull(stmt);
        if (!stmts.contains(stmt)) {
            throw new IllegalArgumentException("Statement is not in block");
        }
        return stmts.indexOf(stmt);
    }

    public void removeStmt(int index) {
        if (index < 0 || index > stmts.size() - 1) {
            throw new IllegalArgumentException();
        }
        Stmt stmt = stmts.remove(index);
        codeObservationManager.notifyStmtRemoved(stmt);
        stmt.setBlock(null);
    }

    public void removeStmt(Stmt stmt) {
        Objects.requireNonNull(stmt);
        if (!stmts.contains(stmt)) {
            throw new IllegalArgumentException("Statement is not in block");
        }
        removeStmt(stmts.indexOf(stmt));
    }

    public void appendStmt(Stmt stmt) {
        preAppendStmt(stmt);
        stmts.add(stmt);
        codeObservationManager.notifyStmtAdded(stmt);
    }

    public void preprendStmt(Stmt stmt) {
        preAppendStmt(stmt);
        stmts.add(0, stmt);
        codeObservationManager.notifyStmtAdded(stmt);
    }

    private void preAppendStmt(Stmt stmt) {
        Objects.requireNonNull(stmt);
        if (stmt.getBlock() != null) {
            throw new IllegalArgumentException();
        }
        stmt.setBlock(this);
    }

    public void insertBefore(Stmt pos, Stmt stmt) {
        insertAbout(pos, stmt, 0);
    }

    public void insertAfter(Stmt pos, Stmt stmt) {
        insertAbout(pos, stmt, 1);
    }

    private void insertAbout(Stmt pos, Stmt stmt, int offset) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(stmt);

        if (!stmts.contains(pos)) {
            throw new IllegalArgumentException();
        }
        if (stmt.getBlock() != null) {
            throw new IllegalArgumentException();
        }

        int index = stmts.indexOf(pos) + offset;
        stmts.add(index, stmt);
        stmt.setBlock(this);
        codeObservationManager.notifyStmtAdded(stmt);
    }

    public void replaceStmt(@NonNull Stmt oldStmt, @NonNull Stmt newStmt) {
        if (!stmts.contains(oldStmt)) {
            throw new IllegalArgumentException("Old statement is not part of this block");
        }

        if (newStmt.getBlock() != null) {
            throw new IllegalArgumentException("New statement is already in a block");
        }

        oldStmt.setBlock(null);
        int index = stmts.indexOf(oldStmt);
        stmts.set(index, newStmt);
        newStmt.setBlock(this);
        codeObservationManager.notifyStmtReplaced(oldStmt, newStmt);
    }

    public List<Stmt> stmts() {
        return Collections.unmodifiableList(stmts);
    }

    public List<Stmt> modSafeStmts() {
        return new ArrayList<>(stmts);
    }

    public void accept(CodeVisitor vis) {
        for (Stmt stmt : stmts) {
            stmt.accept(vis);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CodeBlock) {
            CodeBlock other = (CodeBlock) o;
            return other.id == id;
        } else {
            return false;
        }
    }
}
