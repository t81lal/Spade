package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class ReturnStmt extends Stmt {

    private final LoadLocalExpr var;

    public ReturnStmt(LoadLocalExpr var) {
        super(Opcodes.RETURN);
        this.var = var;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitReturnStmt(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((ReturnStmt) u).var, var);
    }
}
