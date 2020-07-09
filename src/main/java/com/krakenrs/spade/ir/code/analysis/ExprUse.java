package com.krakenrs.spade.ir.code.analysis;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;

import lombok.Getter;
import lombok.NonNull;

public final class ExprUse extends Use {

    @NonNull
    @Getter
    private final LoadLocalExpr expr;

    public ExprUse(LoadLocalExpr expr) {
        super(expr.value());
        this.expr = expr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        ExprUse other = (ExprUse) obj;
        return Objects.equals(local, other.local) && Objects.equals(expr, other.expr);
    }

    @Override
    public String toString() {
        return CodePrinter.toString(expr.stmt());
    }
}
