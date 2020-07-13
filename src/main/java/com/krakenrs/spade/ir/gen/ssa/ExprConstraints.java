package com.krakenrs.spade.ir.gen.ssa;

import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.LoadArrayExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.NewObjectExpr;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeVisitor;

public class ExprConstraints implements Opcodes {

    public static void collectConstraints(LatestValue value, Expr e) {
        AbstractCodeVisitor visitor = new AbstractCodeVisitor() {
            @Override
            public void visitLoadFieldExpr(LoadFieldExpr e) {
                super.visitLoadFieldExpr(e);
                value.constraints.add(Constraint.makeFieldConstraint(e));
            }

            @Override
            public void visitInvokeExpr(InvokeExpr e) {
                super.visitInvokeExpr(e);
                value.constraints.add(Constraint.makeInvokeConstraint());
            }

            @Override
            public void visitNewObjectExpr(NewObjectExpr e) {
                super.visitNewObjectExpr(e);
                value.constraints.add(Constraint.makeInvokeConstraint());
            }

            @Override
            public void visitLoadArrayExpr(LoadArrayExpr e) {
                super.visitLoadArrayExpr(e);
                value.constraints.add(Constraint.makeArrayConstraint());
            }
        };

        e.accept(visitor);
    }

    static class SideEffectCheckVisitor extends AbstractCodeVisitor {
        boolean hasSideEffects = false;

        @Override
        public void visitAny(CodeUnit u) {
            int opcode = u.opcode();
            switch (opcode) {
                case ALLOCOBJ:
                case ALLOCARR:
                case NEWOBJ:
                case INVOKE:
                    hasSideEffects = true;
            }
        }
    }

    public static boolean hasSideEffects(Expr e) {
        SideEffectCheckVisitor vis = new SideEffectCheckVisitor();
        e.accept(vis);
        return vis.hasSideEffects;
    }

    public static boolean isInvoke(int opcode) {
        return opcode == INVOKE || opcode == NEWOBJ;
    }

    public static boolean isHeapStore(int opcode) {
        return opcode == ASSIGN_FIELD || opcode == ASSIGN_ARRAY;
    }
}
