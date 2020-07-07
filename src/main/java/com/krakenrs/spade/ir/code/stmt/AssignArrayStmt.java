package com.krakenrs.spade.ir.code.stmt;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;

public class AssignArrayStmt extends Stmt {
    private final LoadLocalExpr array;
    private final ValueExpr<?> index, value;

    public AssignArrayStmt(LoadLocalExpr array, ValueExpr<?> index, ValueExpr<?> value) {
        super(Opcodes.ASSIGN_ARRAY);
        this.array = array;
        this.index = index;
        this.value = value;

        array.setParent(this);
        index.setParent(this);
        value.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignArrayStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignArrayStmt(this);
    }

    public LoadLocalExpr array() {
        return array;
    }

    public ValueExpr<?> index() {
        return index;
    }

    public ValueExpr<?> value() {
        return value;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            AssignArrayStmt aas = (AssignArrayStmt) u;
            return equivalent(array, aas.array) && equivalent(index, aas.index) && equivalent(value, aas.value);
        } else {
            return false;
        }
    }
}
