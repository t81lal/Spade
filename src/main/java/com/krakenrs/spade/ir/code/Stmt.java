package com.krakenrs.spade.ir.code;

import com.krakenrs.spade.ir.code.visitor.CodeReducer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class Stmt extends CodeUnit {

    @Getter
    @Setter
    private CodeBlock block;

    public Stmt(int opcode) {
        super(opcode);
    }

    @Override
    public Stmt stmt() {
        return this;
    }

    @Override
    public boolean equivalent(@NonNull CodeUnit u) {
        // See Expr.equivalent
        return getClass().equals(u.getClass());
    }

    public abstract Stmt reduceStmt(CodeReducer reducer);
}
