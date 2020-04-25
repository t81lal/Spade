package com.krakenrs.spade.ir.code.expr;

import java.util.ArrayList;
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
import com.krakenrs.spade.ir.type.ObjectType;
import com.krakenrs.spade.ir.type.ValueType;

public abstract class InvokeExpr extends Expr {

    public enum Mode {
        STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC
    }
    
    private ClassType owner;
    private String name;
    private MethodType methodType;
    private List<ValueExpr<?>> arguments;
    private Mode mode;

    public InvokeExpr(ClassType owner, String name, MethodType methodType, Mode mode, List<ValueExpr<?>> arguments) {
        super(Opcodes.INVOKE, methodType.getReturnType());
        this.owner = owner;
        this.name = name;
        this.methodType = methodType;
        this.mode = mode;
        this.arguments = new ArrayList<>();

        setMethod(methodType, arguments);
    }

    @Override
    public void setType(ValueType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitInvokeExpr(this);
    }

    public ClassType owner() {
        return owner;
    }

    public void setOwner(ClassType owner) {
        this.owner = owner;
        notifyParent();
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyParent();
    }

    public MethodType methodType() {
        return methodType;
    }

    public List<ValueExpr<?>> arguments() {
        return arguments;
    }

    public void setMethod(MethodType methodType, List<ValueExpr<?>> arguments) {
        Objects.requireNonNull(methodType);
        Objects.requireNonNull(arguments);

        if (methodType.getParamTypes().size() != arguments.size()) {
            throw new IllegalArgumentException("Too few arguments for method: " + methodType + " , " + arguments);
        }

        /*for (int i = 0; i < arguments.size(); i++) {
            ValueExpr<?> arg = arguments.get(i);
            ValueType t = methodType.getParamTypes().get(i);
            // check assignable
        }*/
        this.arguments.forEach(this::removeChild);
        arguments.forEach(this::addChild);

        this.methodType = methodType;
        this.arguments = Collections.unmodifiableList(arguments);

        notifyParent();
    }

    public Mode mode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        notifyParent();
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
        private LoadLocalExpr accessor;

        public InvokeVirtualExpr(ClassType owner, String name, MethodType methodType, Mode mode, LoadLocalExpr accessor,
                List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, mode, arguments);
            if (mode == Mode.STATIC || mode == Mode.DYNAMIC) {
                throw new IllegalArgumentException();
            }
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
        public boolean equivalent(CodeUnit u) {
            return super.equivalent(u) && equivalent(((InvokeVirtualExpr) u).accessor, accessor);
        }

        @Override
        public void setMode(Mode mode) {
            if (mode == Mode.STATIC || mode == Mode.DYNAMIC) {
                throw new IllegalArgumentException(mode.toString());
            }
            super.setMode(mode);
        }
    }

    public static class InvokeStaticExpr extends InvokeExpr {
        public InvokeStaticExpr(ClassType owner, String name, MethodType methodType, List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, Mode.STATIC, arguments);
        }

        @Override
        public void setMode(Mode mode) {
            throw new UnsupportedOperationException();
        }
    }
}
