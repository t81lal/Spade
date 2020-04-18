package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;

public class ConsumeStmt extends Stmt {

    private final Expr expr;

    public ConsumeStmt(Expr expr) {
        super(Opcodes.CONSUME);
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expr);
    }
}