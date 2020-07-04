package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.value.Local;

public abstract class DeclareLocalStmt extends Stmt {
    private final Local var;

    public DeclareLocalStmt(int opcode, Local var) {
        super(opcode);
        this.var = var;
    }

    public Local var() {
        return var;
    }
    
    public abstract DeclareLocalStmt copy(Local newVar);

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((DeclareLocalStmt) u).var, var);
    }
}
