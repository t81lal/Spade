package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class MonitorStmt extends Stmt {

    public enum Mode {
        ENTER, EXIT
    }

    private final LoadLocalExpr var;
    private final Mode mode;

    public MonitorStmt(LoadLocalExpr var, Mode mode) {
        super(Opcodes.MONITOR);
        this.var = var;
        this.mode = mode;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitMonitorStmt(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            MonitorStmt ms = (MonitorStmt) u;
            return Objects.equals(mode, ms.mode) && equivalent(var, ms.var);
        } else {
            return false;
        }
    }
}
