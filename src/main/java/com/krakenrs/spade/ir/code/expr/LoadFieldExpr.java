package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
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

    public ClassType owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public abstract boolean isStatic();

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, name);
    }

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
        private final LoadLocalExpr accessor;

        public LoadVirtualFieldExpr(ClassType owner, String name, ValueType fieldType, LoadLocalExpr accessor) {
            super(owner, name, fieldType);
            this.accessor = accessor;
        }

        public LoadLocalExpr accessor() {
            return accessor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), accessor);
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
