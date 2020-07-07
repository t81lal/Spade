package com.krakenrs.spade.ir.code;

import java.util.Objects;

import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.type.ValueType;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class Expr extends CodeUnit {

    @Getter
    @Setter
    protected ValueType type;
    @Getter
    protected CodeUnit parent;

    public Expr(int opcode) {
        super(opcode);
    }

    public Expr(int opcode, ValueType type) {
        super(opcode);
        this.type = type;
    }

    public void setParent(CodeUnit parent) {
        if (this.parent != null) {
            throw new IllegalArgumentException("Immutable");
        }
        this.parent = parent;
    }

    @Override
    public Stmt stmt() {
        return parent != null ? parent.stmt() : null;
    }

    @Override
    public boolean equivalent(@NonNull CodeUnit u) {
        // Expr type needs to be the exact same as this
        // TODO: maybe relax this to check the subtype?
        if (!getClass().equals(u.getClass())) {
            return false;
        }

        return Objects.equals(type, ((Expr) u).type);
    }

    public abstract Expr reduceExpr(CodeReducer reducer);
}
