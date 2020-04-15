package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.app.asm.Klass;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class AssignFieldStmt extends Stmt {

    private final Klass owner;
    private final String name;
    private final ValueType fieldType;
    private final ValueExpr<?> value;

    public AssignFieldStmt(Klass owner, String name, ValueType fieldType, ValueExpr<?> value) {
        super(Opcodes.ASSIGN_FIELD);
        this.owner = owner;
        this.name = name;
        this.fieldType = fieldType;
        this.value = value;
    }

    public Klass owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public ValueType fieldType() {
        return fieldType;
    }

    public ValueExpr<?> value() {
        return value;
    }

    public abstract boolean isStatic();
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, name, fieldType, value);
    }

    public static class AssignStaticFieldExpr extends AssignFieldStmt {
        public AssignStaticFieldExpr(Klass owner, String name, ValueType fieldType, ValueExpr<?> value) {
            super(owner, name, fieldType, value);
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }

    public static class AssignVirtualFieldStmt extends AssignFieldStmt {
        private final LoadLocalExpr accessor;

        public AssignVirtualFieldStmt(Klass owner, String name, ValueType fieldType, ValueExpr<?> value,
                LoadLocalExpr accessor) {
            super(owner, name, fieldType, value);
            this.accessor = accessor;
        }

        public LoadLocalExpr accessor() {
            return accessor;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), accessor);
        }
    }
}
