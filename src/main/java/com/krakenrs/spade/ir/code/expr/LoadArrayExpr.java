package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;

public class LoadArrayExpr extends Expr {

    private LoadLocalExpr array;
    private ValueExpr<?> index;

    public LoadArrayExpr(ValueType componentType, LoadLocalExpr array, ValueExpr<?> index) {
        super(Opcodes.LOAD_ARR, componentType);
        this.array = array;
        this.index = index;

        addChild(array);
        addChild(index);
    }

    @Override
    public void setType(ValueType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitLoadArrayExpr(this);
    }

    public LoadLocalExpr array() {
        return array;
    }

    public void setArray(LoadLocalExpr array) {
        Objects.requireNonNull(array);

        ValueType t = array.type();
        if (!(t instanceof ArrayType)) {
            throw new IllegalArgumentException(array + " must be int type, was: " + t);
        }

        removeChild(this.array);
        this.array = array;
        addChild(array);

        this.type = ((ArrayType) t).componentType();

        notifyParent();
    }

    public ValueExpr<?> index() {
        return index;
    }

    public void setIndex(ValueExpr<?> index) {
        Objects.requireNonNull(index);

        ValueType t = index.type();
        if (!(t instanceof PrimitiveType) || !((PrimitiveType) t).isIntLike()) {
            throw new IllegalArgumentException(index + " must be int type, was: " + t);
        }

        removeChild(this.index);
        this.index = index;
        addChild(index);

        notifyParent();
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            LoadArrayExpr lae = (LoadArrayExpr) u;
            return equivalent(lae.array, array) && equivalent(lae.index, index);
        } else {
            return false;
        }
    }
}
