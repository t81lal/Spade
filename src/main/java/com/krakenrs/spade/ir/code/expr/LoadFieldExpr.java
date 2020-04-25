package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ObjectType;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class LoadFieldExpr extends Expr {

    private ClassType owner;
    private String name;

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

    public ClassType owner() {
        return owner;
    }

    public void setOwner(ClassType owner) {
        this.owner = owner;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyParent();
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
    
    public static class LoadStaticFieldExpr extends LoadFieldExpr {
        public LoadStaticFieldExpr(ClassType owner, String name, ValueType fieldType) {
            super(owner, name, fieldType);
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }

    public static class LoadVirtualFieldExpr extends LoadFieldExpr {
        private LoadLocalExpr accessor;

        public LoadVirtualFieldExpr(ClassType owner, String name, ValueType fieldType, LoadLocalExpr accessor) {
            super(owner, name, fieldType);
            this.accessor = accessor;
            addChild(accessor);
        }

        public LoadLocalExpr accessor() {
            return accessor;
        }

        public void setAccessor(LoadLocalExpr accessor) {
            Objects.requireNonNull(accessor);

            if (!(accessor.type() instanceof ObjectType)) {
                throw new IllegalArgumentException(accessor + " is not an object: " + accessor.type());
            }

            removeChild(this.accessor);
            this.accessor = accessor;
            addChild(this.accessor);
            notifyParent();
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public boolean equivalent(CodeUnit u) {
            return super.equivalent(u) && equivalent(((LoadVirtualFieldExpr) u).accessor, accessor);
        }
    }
}
