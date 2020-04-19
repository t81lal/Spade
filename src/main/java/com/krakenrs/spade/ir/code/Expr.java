package com.krakenrs.spade.ir.code;

import java.util.Objects;

import com.krakenrs.spade.ir.type.ValueType;

public abstract class Expr extends CodeUnit {

    protected final ValueType type;
    protected CodeUnit parent;

    public Expr(int opcode, ValueType type) {
        super(opcode);
        this.type = type;
    }

    public ValueType type() {
        return type;
    }

    public CodeUnit parent() {
        return parent;
    }

    void setParent(CodeUnit parent) {
        this.parent = parent;
    }

    @Override
    public Stmt stmt() {
        return parent != null ? parent.stmt() : null;
    }

    @Override
    public int hashCode() {
        // Don't include parent here, just data regarding this expression so that
        // hashCode of two expressions that are matching/equivalent are the same.
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        Objects.requireNonNull(u);

        if (!getClass().equals(u.getClass())) {
            return false;
        }

        return Objects.equals(type, ((Expr) u).type);
    }
}
