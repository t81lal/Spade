package com.krakenrs.spade.ir.code.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ArrayType;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;

public class AllocArrayExpr extends Expr {

    private List<ValueExpr<?>> bounds;

    public AllocArrayExpr(ArrayType type, List<ValueExpr<?>> bounds) {
        super(Opcodes.ALLOCARR, type);
        this.bounds = new ArrayList<>();

        // maybe this should be a different method, note it calls notifyParent and addChild
        // since it modifies the type and bounds of this expr
        setBoundsAndNotify(type, bounds);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAllocArrayExpr(this);
    }

    @Override
    public ArrayType type() {
        return (ArrayType) super.type();
    }

    public List<ValueExpr<?>> bounds() {
        return bounds;
    }

    @Override
    public void setType(ValueType type) {
        Objects.requireNonNull(type);
        if (!(type instanceof ArrayType) || ((ArrayType) type).dimensions() != this.type().dimensions()) {
            throw new IllegalArgumentException(
                    type + " must be an ArrayType with the same dimensions as " + this.type());
        }

        ArrayType newArrType = (ArrayType) type;
        super.setType(newArrType); // calls notifyParent
    }

    public void setBound(int index, ValueExpr<?> e) {
        Objects.requireNonNull(e);
        checkBoundType(e);
        ValueExpr<?> oldE = bounds.set(index, e);
        removeChild(oldE);
        addChild(e);
        notifyParent();
    }

    public void setBounds(ArrayType newType, List<ValueExpr<?>> newBounds) {
        setBoundsAndNotify(newType, newBounds);
    }

    private void setBoundsAndNotify(ArrayType newType, List<ValueExpr<?>> newBounds) {
        Objects.requireNonNull(newType);

        if (newType.dimensions() != newBounds.size()) {
            throw new IllegalArgumentException(newType + " doesn't match bounds for " + newBounds);
        }

        newBounds.forEach(this::checkBoundType);
        this.bounds.forEach(this::removeChild);
        newBounds.forEach(this::addChild);

        this.type = newType;
        this.bounds = Collections.unmodifiableList(newBounds);

        notifyParent();
    }

    private void checkBoundType(ValueExpr<?> e) {
        if (!(e.type() instanceof PrimitiveType) && ((PrimitiveType) e.type()).isIntLike()) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && equivalent(((AllocArrayExpr) u).bounds, bounds);
    }
}
