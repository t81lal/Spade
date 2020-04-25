package com.krakenrs.spade.ir.code;

import java.util.Objects;

import com.krakenrs.spade.ir.type.ValueType;

public abstract class Expr extends CodeUnit {

    protected ValueType type;
    protected CodeUnit parent;

    public Expr(int opcode, ValueType type) {
        super(opcode);
        this.type = Objects.requireNonNull(type);
    }

    public ValueType type() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = Objects.requireNonNull(type);
        this.notifyParent();
    }

    public CodeUnit parent() {
        return parent;
    }

    void setParent(CodeUnit parent) {
        this.parent = parent;
    }

    protected void notifyParent() {
        if(parent != null) {
            parent.onChildChange();
        }
    }

    @Override
    public Stmt stmt() {
        return parent != null ? parent.stmt() : null;
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
