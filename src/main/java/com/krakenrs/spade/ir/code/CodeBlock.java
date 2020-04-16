package com.krakenrs.spade.ir.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.krakenrs.spade.commons.collections.graph.Vertex;

public class CodeBlock implements Vertex {
    private final int id;
    private final List<Stmt> stmts = new ArrayList<>();
    private int orderHint;

    public CodeBlock(int id) {
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
        if(index < 0 || index > stmts.size()-1) {
            throw new IllegalArgumentException();
        }
        Stmt stmt = stmts.remove(index);
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
    }

    public void preprendStmt(Stmt stmt) {
        preAppendStmt(stmt);
        stmts.add(0, stmt);
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
