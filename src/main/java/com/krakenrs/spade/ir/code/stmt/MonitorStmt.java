package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;

public class MonitorStmt extends Stmt {

    public static enum Mode {
        ENTER, EXIT
    }

    private final LoadLocalExpr var;
    private final Mode mode;

    public MonitorStmt(LoadLocalExpr var, Mode mode) {
        super(Opcodes.MONITOR);
        this.var = var;
        this.mode = mode;
    }

    public LoadLocalExpr var() {
        return var;
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var, mode);
    }
}
