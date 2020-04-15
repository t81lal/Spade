package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;

public class AssignArrayStmt extends Stmt {
    private final LoadLocalExpr array;
    private final ValueExpr<?> index, value;

    public AssignArrayStmt(LoadLocalExpr array, ValueExpr<?> index, ValueExpr<?> value) {
        super(Opcodes.ASSIGN_ARRAY);
        this.array = array;
        this.index = index;
        this.value = value;
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
    public int hashCode() {
        return Objects.hash(super.hashCode(), array, index, value);
    }
}
