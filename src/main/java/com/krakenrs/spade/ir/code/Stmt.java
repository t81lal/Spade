package com.krakenrs.spade.ir.code;

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
}
