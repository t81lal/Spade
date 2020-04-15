package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.app.asm.Klass;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class LoadFieldExpr extends Expr {

    private final Klass owner;
    private final String name;

    public LoadFieldExpr(Klass owner, String name, ValueType fieldType) {
        super(Opcodes.LOAD_FIELD, fieldType);
        this.owner = owner;
        this.name = name;
    }

    public Klass owner() {
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
    
    public static class LoadStaticFieldExpr extends LoadFieldExpr {
        public LoadStaticFieldExpr(Klass owner, String name, ValueType fieldType) {
            super(owner, name, fieldType);
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }

    public static class LoadVirtualFieldExpr extends LoadFieldExpr {
        private final LoadLocalExpr accessor;

        public LoadVirtualFieldExpr(Klass owner, String name, ValueType fieldType, LoadLocalExpr accessor) {
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
    }
}
