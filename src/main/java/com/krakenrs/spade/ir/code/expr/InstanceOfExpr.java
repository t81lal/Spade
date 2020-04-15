package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.Type;

public class InstanceOfExpr extends Expr {

    private final LoadLocalExpr var;
    private final Type checkType;

    public InstanceOfExpr(LoadLocalExpr var, Type checkType) {
        super(Opcodes.INSTANCEOF, PrimitiveType.BOOLEAN);
        this.var = var;
        this.checkType = checkType;
    }

    public LoadLocalExpr var() {
        return var;
    }

    public Type checkType() {
        return checkType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), var, checkType);
    }
}
