package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class AssignFieldStmt extends Stmt {

    private final ClassType owner;
    private final String name;
    private final ValueType fieldType;
    private final ValueExpr<?> value;

    public AssignFieldStmt(ClassType owner, String name, ValueType fieldType, ValueExpr<?> value) {
        super(Opcodes.ASSIGN_FIELD);
        this.owner = owner;
        this.name = name;
        this.fieldType = fieldType;
        this.value = value;

        value.setParent(this);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignFieldStmt(this);
    }

    @Override
    public Stmt reduceStmt(CodeReducer reducer) {
        return reducer.reduceAssignFieldStmt(this);
    }

    public ClassType owner() {
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
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            AssignFieldStmt afs = (AssignFieldStmt) u;
            return Objects.equals(owner, afs.owner) && Objects.equals(name, afs.name) && afs.isStatic() == isStatic()
                    && Objects.equals(fieldType, afs.fieldType) && equivalent(value, afs.value);
        } else {
            return false;
        }
    }

    public static class AssignStaticFieldStmt extends AssignFieldStmt {
        @Inject
        public AssignStaticFieldStmt(@Assisted ClassType owner, @Assisted String name, @Assisted ValueType fieldType, @Assisted ValueExpr<?> value) {
            super(owner, name, fieldType, value);
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }

    public static class AssignVirtualFieldStmt extends AssignFieldStmt {
        private final LoadLocalExpr accessor;

        @Inject
        public AssignVirtualFieldStmt(@Assisted ClassType owner, @Assisted String name, @Assisted ValueType fieldType, @Assisted ValueExpr<?> value,
                @Assisted LoadLocalExpr accessor) {
            super(owner, name, fieldType, value);
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
            return super.equivalent(u) && equivalent(((AssignVirtualFieldStmt) u).accessor, accessor);
        }
    }
}
