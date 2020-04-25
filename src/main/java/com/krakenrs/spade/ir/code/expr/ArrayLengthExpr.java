package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;

public class ArrayLengthExpr extends Expr {

    private LoadLocalExpr var;

    public ArrayLengthExpr(LoadLocalExpr var) {
        super(Opcodes.ARRAYLEN, PrimitiveType.INT);
        this.var = validateVar(var);

        addChild(var);
    }

    @Override
    public void setType(ValueType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitArrayLengthExpr(this);
    }

    public LoadLocalExpr var() {
        return var;
    }

    public void setVar(LoadLocalExpr var) {
        validateVar(var);

        removeChild(this.var);
        this.var = var;
        addChild(var);
        
        notifyParent();
    }

    private LoadLocalExpr validateVar(LoadLocalExpr var) {
        ValueType varType = var.type();
        if (!(varType instanceof ArrayType)) {
            throw new IllegalArgumentException(var + " must be an array, got " + varType);
        }
        return var;
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((ArrayLengthExpr) u).var, var);
    }
}
