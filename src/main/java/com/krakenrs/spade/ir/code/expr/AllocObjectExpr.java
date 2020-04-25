package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ObjectType;
import com.krakenrs.spade.ir.type.ValueType;

public class AllocObjectExpr extends Expr {
    public AllocObjectExpr(ClassType type) {
        super(Opcodes.ALLOCOBJ, type.asValueType());
    }

    @Override
    public void setType(ValueType type) {
        Objects.requireNonNull(type);

        if (type instanceof ObjectType) {
            super.setType(type); // calls notifyParent
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAllocObjectExpr(this);
    }
}
