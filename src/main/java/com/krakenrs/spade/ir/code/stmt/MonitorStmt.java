package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class MonitorStmt extends Stmt {

    public enum Mode {
        ENTER, EXIT
    }

    private final LoadLocalExpr var;
    private final Mode mode;

    @Inject
    public MonitorStmt(@Assisted LoadLocalExpr var, @Assisted Mode mode) {
        super(Opcodes.MONITOR);
        this.var = var;
        this.mode = mode;

        var.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitMonitorStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceMonitorStmt(this);
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

    @Override
    public MonitorStmt deepCopy() {
        return new MonitorStmt(var.deepCopy(), mode);
    }
}
