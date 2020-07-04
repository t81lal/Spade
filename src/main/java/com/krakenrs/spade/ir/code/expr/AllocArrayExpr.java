package com.krakenrs.spade.ir.code.expr;

import java.util.ArrayList;
import java.util.List;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ValueType;

import lombok.Getter;
import lombok.NonNull;

public class AllocArrayExpr extends Expr {

	@Getter
    private final List<ValueExpr<?>> bounds;

    public AllocArrayExpr(ValueType type, @NonNull List<ValueExpr<?>> bounds) {
        super(Opcodes.ALLOCARR, type);
        this.bounds = new ArrayList<>(bounds);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAllocArrayExpr(this);
    }

    @Override
    public boolean equivalent(@NonNull CodeUnit u) {
        return super.equivalent(u) && equivalent(((AllocArrayExpr) u).bounds, bounds);
    }
}
