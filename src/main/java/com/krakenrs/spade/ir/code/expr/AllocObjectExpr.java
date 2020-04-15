package com.krakenrs.spade.ir.code.expr;

import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.type.ClassType;

public class AllocObjectExpr extends Expr {
    public AllocObjectExpr(ClassType type) {
        super(Opcodes.ALLOCOBJ, type.asValueType());
    }
}
