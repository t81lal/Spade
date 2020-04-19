package com.krakenrs.spade.ir.code;

import java.util.Objects;

public abstract class Stmt extends CodeUnit {

    private CodeBlock block;

    public Stmt(int opcode) {
        super(opcode);
    }

    public CodeBlock getBlock() {
        return block;
    }

    void setBlock(CodeBlock block) {
        this.block = block;
    }

    @Override
    public Stmt stmt() {
        return this;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        Objects.requireNonNull(u);
        return getClass().equals(u.getClass());
    }
}
