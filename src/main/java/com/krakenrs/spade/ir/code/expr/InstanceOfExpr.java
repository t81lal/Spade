package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.Type;
import com.krakenrs.spade.ir.type.ValueType;

public class InstanceOfExpr extends Expr {

    private LoadLocalExpr var;
    private Type checkType;

    public InstanceOfExpr(LoadLocalExpr var, Type checkType) {
        super(Opcodes.INSTANCEOF, PrimitiveType.BOOLEAN);
        this.var = Objects.requireNonNull(var);
        this.checkType = Objects.requireNonNull(checkType);
    }

    @Override
    public void setType(ValueType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitInstanceOfExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public void setVar(LoadLocalExpr var) {
        Objects.requireNonNull(var);
        removeChild(this.var);
        this.var = var;
        addChild(this.var);
        notifyParent();
    }

    public Type checkType() {
        return checkType;
    }

    public void setCheckType(Type checkType) {
        this.checkType = Objects.requireNonNull(checkType);
        notifyParent();
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
