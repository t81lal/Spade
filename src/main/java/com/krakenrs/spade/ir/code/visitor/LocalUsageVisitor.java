package com.krakenrs.spade.ir.code.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.value.Local;

public class LocalUsageVisitor extends AbstractCodeVisitor {
    private final Set<Local> uses = new HashSet<>();

    @Override
    public void visitValueExpr(ValueExpr<?> e) {
        if (e instanceof LoadLocalExpr) {
            uses.add((Local) e.value());
        }
    }

    public Set<Local> getUses() {
        return Collections.unmodifiableSet(uses);
    }
}
