package com.krakenrs.spade.ir.code.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Stmt;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.expr.value.ValueExpr;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeVisitor;
import com.krakenrs.spade.ir.value.Local;

import lombok.NonNull;

public class SSADefUseMap {
    private final Map<Local, Def> defs = new HashMap<>();
    private final AddStmtVisitor addStmtVisitor = new AddStmtVisitor();

    public SSADefUseMap() {
    }

    public boolean isDefined(@NonNull Local local) {
        return defs.containsKey(assertVersioned(local));
    }

    public void addStmt(@NonNull Stmt addedStmt) {
        addedStmt.accept(addStmtVisitor);
    }

    public void removeStmt(@NonNull Stmt removedStmt) {

    }

    public DeclareLocalStmt getDef(@NonNull Local local) {
        return defs.get(assertDefined(local)).getStmt();
    }

    public Set<Use> getUses(@NonNull Local local) {
        return defs.get(assertDefined(local)).getUses();
    }

    private Local assertVersioned(Local local) {
        if (!local.isVersioned()) {
            throw new IllegalArgumentException("Not an SSA local: " + local);
        }
        return local;
    }

    private Local assertDefined(Local local) {
        assertVersioned(local);
        if (!defs.containsKey(local)) {
            throw new IllegalStateException("Undefined local: " + local);
        }
        return local;
    }

    class AddStmtVisitor extends AbstractCodeVisitor {
        @Override
        public void visitAny(CodeUnit u) {
            if (u instanceof DeclareLocalStmt) {
                /* Note that AssignParamStmt doesn't have a child for the local
                 * so it doesn't get added as a use. */
                DeclareLocalStmt decl = (DeclareLocalStmt) u;
                Local declaredLocal = assertVersioned(decl.var());
                if (!defs.containsKey(declaredLocal)) {
                    defs.put(declaredLocal, new Def(decl));
                } else {
                    throw new UnsupportedOperationException(declaredLocal + " is already defined");
                }
            }
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
            for (Entry<CodeBlock, Local> e : s.arguments().entrySet()) {
                Local usedLocal = assertDefined(e.getValue());
                defs.get(usedLocal).addUse(new PhiUse(e.getKey(), usedLocal, s));
            }
        }

        @Override
        public void visitValueExpr(ValueExpr<?> e) {
            if (e instanceof LoadLocalExpr) {
                LoadLocalExpr usedLocalExpr = (LoadLocalExpr) e;
                Local usedLocal = assertDefined(usedLocalExpr.value());
                defs.get(usedLocal).addUse(new ExprUse(usedLocalExpr));
            }
        }
    };
}
