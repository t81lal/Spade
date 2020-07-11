package com.krakenrs.spade.ir.code.expr;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.visitor.CodeReducer;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;

public class NewObjectExpr extends Expr {

    private final ClassType owner;
    private final MethodType methodType;
    private final List<ValueExpr<?>> arguments;

    @Inject
    public NewObjectExpr(@Assisted ClassType owner, @Assisted MethodType methodType, @Assisted List<ValueExpr<?>> arguments) {
        super(Opcodes.NEWOBJ, owner.asValueType());
        this.owner = owner;
        this.methodType = methodType;
        this.arguments = Collections.unmodifiableList(arguments);

        for (ValueExpr<?> a : arguments) {
            a.setParent(this);
        }
    }

    public ClassType owner() {
        return owner;
    }

    public MethodType methodType() {
        return methodType;
    }

    public List<ValueExpr<?>> arguments() {
        return arguments;
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitNewObjectExpr(this);
    }

    @Override
    public Expr reduceExpr(CodeReducer reducer) {
        return reducer.reduceNewObjectExpr(this);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        if (super.equivalent(u)) {
            NewObjectExpr noe = (NewObjectExpr) u;
            return Objects.equals(noe.owner, owner) && Objects.equals(noe.methodType, methodType)
                    && equivalent(noe.arguments, arguments);
        } else {
            return false;
        }
    }

    @Override
    public NewObjectExpr deepCopy() {
        return new NewObjectExpr(owner, methodType, deepCopy(arguments));
    }
}
