package com.krakenrs.spade.ir.code.expr;

import java.util.Objects;

import com.krakenrs.spade.app.asm.Klass;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.type.MethodType;

public abstract class InvokeExpr extends Expr {

    public static enum Mode {
        STATIC, VIRTUAL, INTERFACE, SPECIAL, DYNAMIC
    }
    
    private final Klass owner;
    private final String name;
    private final MethodType methodType;
    private final Mode mode;

    public InvokeExpr(Klass owner, String name, MethodType methodType, Mode mode) {
        super(Opcodes.INVOKE, methodType.getReturnType());
        this.owner = owner;
        this.name = name;
        this.methodType = methodType;
        this.mode = mode;
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

    public Mode mode() {
        return mode;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), owner, name, methodType, mode);
    }

    public static class InvokeVirtualExpr extends InvokeExpr {
        private final LoadLocalExpr accessor;

        public InvokeVirtualExpr(LoadLocalExpr accessor, Klass owner, String name, MethodType methodType, Mode mode) {
            super(owner, name, methodType, mode);
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
        public InvokeStaticExpr(Klass owner, String name, MethodType methodType) {
            super(owner, name, methodType, Mode.STATIC);
        }
    }
}
