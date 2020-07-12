package com.krakenrs.spade.ir.code;

import com.krakenrs.spade.ir.code.visitor.CodeReducer;

public class MockStmt extends Stmt {

    public MockStmt() {
        super(0);
    }

    @Override
    public Stmt deepCopy() {
        return new MockStmt();
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        throw new UnsupportedOperationException();
    }
}
