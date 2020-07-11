package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class LoadFieldExpr extends Expr {

    private final ClassType owner;
    private final String name;

    public LoadFieldExpr(ClassType owner, String name, ValueType fieldType) {
        super(Opcodes.LOAD_FIELD, fieldType);
        this.owner = owner;
        this.name = name;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitLoadFieldExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceLoadFieldExpr(this);
    }

    public ClassType owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public abstract boolean isStatic();

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            LoadFieldExpr lfe = (LoadFieldExpr) u;
            return Objects.equals(owner, lfe.owner) && Objects.equals(name, lfe.name) && lfe.isStatic() == isStatic();
        } else {
            return false;
        }
    }

    public abstract LoadFieldExpr deepCopy();

    public static class LoadStaticFieldExpr extends LoadFieldExpr {
        @Inject
        public LoadStaticFieldExpr(@Assisted ClassType owner, @Assisted String name, @Assisted ValueType fieldType) {
            super(owner, name, fieldType);
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public LoadStaticFieldExpr deepCopy() {
            return new LoadStaticFieldExpr(owner(), name(), type);
        }
    }

    public static class LoadVirtualFieldExpr extends LoadFieldExpr {
        private final LoadLocalExpr accessor;

        @Inject
        public LoadVirtualFieldExpr(@Assisted ClassType owner, @Assisted String name, @Assisted ValueType fieldType, @Assisted LoadLocalExpr accessor) {
            super(owner, name, fieldType);
            this.accessor = accessor;

            accessor.setParent(this);
        }

        public LoadLocalExpr accessor() {
            return accessor;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public boolean equivalent(CodeUnit u) {
            return super.equivalent(u) && equivalent(((LoadVirtualFieldExpr) u).accessor, accessor);
        }

        @Override
        public LoadVirtualFieldExpr deepCopy() {
            return new LoadVirtualFieldExpr(owner(), name(), type, accessor.deepCopy());
        }
    }
}
