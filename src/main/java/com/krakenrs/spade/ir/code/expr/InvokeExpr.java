package com.krakenrs.spade.ir.code.expr;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;

public abstract class InvokeExpr extends Expr {

    public static enum Mode {
        STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC
    }
    
    private final ClassType owner;
    private final String name;
    private final MethodType methodType;
    private final List<ValueExpr<?>> arguments;
    private final Mode mode;

    public InvokeExpr(ClassType owner, String name, MethodType methodType, Mode mode, List<ValueExpr<?>> arguments) {
        super(Opcodes.INVOKE, methodType.getReturnType());
        this.owner = owner;
        this.name = name;
        this.methodType = methodType;
        this.mode = mode;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitInvokeExpr(this);
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
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, name, methodType, mode);
    }

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

        public InvokeVirtualExpr(ClassType owner, String name, MethodType methodType, Mode mode, LoadLocalExpr accessor,
                List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, mode, arguments);
            if (mode == Mode.STATIC || mode == Mode.DYNAMIC) {
                throw new IllegalArgumentException();
            }
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
        public boolean equivalent(CodeUnit u) {
            return super.equivalent(u) && equivalent(((InvokeVirtualExpr) u).accessor, accessor);
        }
    }

    public static class InvokeStaticExpr extends InvokeExpr {
        public InvokeStaticExpr(ClassType owner, String name, MethodType methodType, List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, Mode.STATIC, arguments);
        }
    }
}
