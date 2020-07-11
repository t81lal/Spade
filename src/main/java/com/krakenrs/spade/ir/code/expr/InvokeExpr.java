package com.krakenrs.spade.ir.code.expr;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;

public abstract class InvokeExpr extends Expr {

    public enum Mode {
        STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC
    }

    protected final ClassType owner;
    protected final String name;
    protected final MethodType methodType;
    protected final List<ValueExpr<?>> arguments;
    protected final Mode mode;

    public InvokeExpr(ClassType owner, String name, MethodType methodType, Mode mode, List<ValueExpr<?>> arguments) {
        super(Opcodes.INVOKE, methodType.getReturnType());
        this.owner = owner;
        this.name = name;
        this.methodType = methodType;
        this.mode = mode;
        this.arguments = Collections.unmodifiableList(arguments);

        for (ValueExpr<?> a : arguments) {
            a.setParent(this);
        }
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitInvokeExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceInvokeExpr(this);
    }

    public ClassType owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public MethodType methodType() {
        return methodType;
    }

    public List<ValueExpr<?>> arguments() {
        return arguments;
    }

    public Mode mode() {
        return mode;
    }

    public abstract InvokeExpr deepCopy();

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            InvokeExpr ie = (InvokeExpr) u;
            return Objects.equals(ie.owner, owner) && Objects.equals(ie.name, name)
                    && Objects.equals(ie.methodType, methodType) && Objects.equals(ie.mode, mode)
                    && equivalent(ie.arguments, arguments);
        } else {
            return false;
        }
    }

    public static class InvokeVirtualExpr extends InvokeExpr {
        private final LoadLocalExpr accessor;

        @Inject
        public InvokeVirtualExpr(@Assisted ClassType owner, @Assisted String name, @Assisted MethodType methodType, @Assisted Mode mode, @Assisted LoadLocalExpr accessor,
                @Assisted List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, mode, arguments);
            if (mode == Mode.STATIC || mode == Mode.DYNAMIC) {
                throw new IllegalArgumentException();
            }
            this.accessor = accessor;

            accessor.setParent(this);
        }

        public LoadLocalExpr accessor() {
            return accessor;
        }

        @Override
        public boolean equivalent(CodeUnit u) {
            return super.equivalent(u) && equivalent(((InvokeVirtualExpr) u).accessor, accessor);
        }

        @Override
        public InvokeVirtualExpr deepCopy() {
            return new InvokeVirtualExpr(owner, name, methodType, mode, accessor.deepCopy(), deepCopy(arguments()));
        }
    }

    public static class InvokeStaticExpr extends InvokeExpr {
        @Inject
        public InvokeStaticExpr(@Assisted ClassType owner, @Assisted String name, @Assisted MethodType methodType, @Assisted List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, Mode.STATIC, arguments);
        }

        @Override
        public InvokeStaticExpr deepCopy() {
            return new InvokeStaticExpr(owner, name, methodType, deepCopy(arguments()));
        }
    }
}
