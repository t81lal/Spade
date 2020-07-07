package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.Type;

public class InstanceOfExpr extends Expr {

    private final LoadLocalExpr var;
    private final Type checkType;

    public InstanceOfExpr(LoadLocalExpr var, Type checkType) {
        super(Opcodes.INSTANCEOF, PrimitiveType.BOOLEAN);
        this.var = var;
        this.checkType = checkType;

        var.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitInstanceOfExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceInstanceOfExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public Type checkType() {
        return checkType;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            InstanceOfExpr ioe = (InstanceOfExpr) u;
            return equivalent(var, ioe.var) && Objects.equals(checkType, ioe.checkType);
        } else {
            return false;
        }
    }
}
