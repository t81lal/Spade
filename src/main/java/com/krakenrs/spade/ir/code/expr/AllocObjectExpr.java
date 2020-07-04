package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;

public class AllocObjectExpr extends Expr {
    public AllocObjectExpr(ClassType type) {
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
}
