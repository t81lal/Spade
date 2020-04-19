package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.value.Local;

public class AssignLocalStmt extends DeclareLocalStmt {
    private final Expr value;

    public AssignLocalStmt(Local var, Expr value) {
        super(Opcodes.ASSIGN_LOCAL, var);
        this.value = value;
    }

    public Expr value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((AssignLocalStmt) u).value, value);
    }
}
