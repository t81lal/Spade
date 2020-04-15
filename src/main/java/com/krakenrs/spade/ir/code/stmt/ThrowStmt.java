package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;

public class ThrowStmt extends Stmt {

    private final LoadLocalExpr var;

    public ThrowStmt(LoadLocalExpr var) {
        super(Opcodes.THROW);
        this.var = var;
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var);
    }
}
