package com.krakenrs.spade.ir.code.expr;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.krakenrs.spade.app.asm.Klass;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.type.MethodType;

public abstract class InvokeExpr extends Expr {

    public static enum Mode {
        STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC
    }
    
    private final Klass owner;
    private final String name;
    private final MethodType methodType;
    private final List<ValueExpr<?>> arguments;
    private final Mode mode;

    public InvokeExpr(Klass owner, String name, MethodType methodType, Mode mode, List<ValueExpr<?>> arguments) {
        super(Opcodes.INVOKE, methodType.getReturnType());
        this.owner = owner;
        this.name = name;
        this.methodType = methodType;
        this.mode = mode;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public Klass owner() {
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

    public static class InvokeVirtualExpr extends InvokeExpr {
        private final LoadLocalExpr accessor;

        public InvokeVirtualExpr(Klass owner, String name, MethodType methodType, Mode mode, LoadLocalExpr accessor,
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
    }

    public static class InvokeStaticExpr extends InvokeExpr {
        public InvokeStaticExpr(Klass owner, String name, MethodType methodType, List<ValueExpr<?>> arguments) {
            super(owner, name, methodType, Mode.STATIC, arguments);
        }
    }
}
