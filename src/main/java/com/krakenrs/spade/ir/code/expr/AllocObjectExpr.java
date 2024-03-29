package com.krakenrs.spade.ir.code.expr;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ObjectType;

import lombok.NonNull;

public class AllocObjectExpr extends Expr {
    @Inject
    public AllocObjectExpr(@Assisted @NonNull ClassType type) {
        super(Opcodes.ALLOCOBJ, type.asValueType());
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAllocObjectExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceAllocObjectExpr(this);
    }

    @Override
    public AllocObjectExpr deepCopy() {
        ObjectType ourType = (ObjectType) type;
        return new AllocObjectExpr(ourType.getClassType());
    }
}
