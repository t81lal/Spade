package com.krakenrs.spade.ir.code.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
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
    private final RemoveStmtVisitor removeStmtVisitor = new RemoveStmtVisitor();

    public SSADefUseMap() {
    }

    public boolean isDefined(@NonNull Local local) {
        return defs.containsKey(assertVersioned(local));
    }

    public void addStmt(@NonNull Stmt addedStmt) {
        addedStmt.accept(addStmtVisitor);
    }

    public void removeStmt(@NonNull Stmt removedStmt) {
        removedStmt.accept(removeStmtVisitor);
    }

    public void replaceDef(@NonNull DeclareLocalStmt newDecl) {
        Local declLocal = newDecl.var();
        assertDefined(declLocal);

        /* Here we are swapping out the old definition by removing the
         * old def and adding the new one, but this may invalidate the
         * map invariants, so we have to do this ourselves so that after
         * the map remains consistent. */

        Def oldDef = defs.get(declLocal);
        try {
            removeStmtVisitor.shouldRemoveDef = false;
            oldDef.getStmt().accept(removeStmtVisitor);
        } finally {
            removeStmtVisitor.shouldRemoveDef = true;
        }

        try {
            addStmtVisitor.shouldAddDef = false;
            newDecl.accept(addStmtVisitor);
        } finally {
            addStmtVisitor.shouldAddDef = true;
        }

        /* copy the uses over to the new Def holder */
        Def newDef = new Def(newDecl, oldDef.getUses());
        defs.put(declLocal, newDef);
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
        boolean shouldAddDef = true;

        @Override
        public void visitAny(CodeUnit u) {
            if (shouldAddDef && u instanceof DeclareLocalStmt) {
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

    class RemoveStmtVisitor extends AbstractCodeVisitor {
        boolean shouldRemoveDef = true;

        @Override
        public void visitAny(CodeUnit u) {
            if (shouldRemoveDef && u instanceof DeclareLocalStmt) {
                DeclareLocalStmt decl = (DeclareLocalStmt) u;
                Local declaredLocal = assertDefined(decl.var());
                Set<Use> uses = defs.get(declaredLocal).getUses();
                if (!uses.isEmpty()) {
                    throw new UnsupportedOperationException(declaredLocal + " still has uses");
                }
                defs.remove(declaredLocal);
            }
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
            for (Entry<CodeBlock, Local> e : s.arguments().entrySet()) {
                Local usedLocal = assertDefined(e.getValue());
                defs.get(usedLocal).removeUse(new PhiUse(e.getKey(), usedLocal, s));
            }
        }

        @Override
        public void visitValueExpr(ValueExpr<?> e) {
            if (e instanceof LoadLocalExpr) {
                LoadLocalExpr usedLocalExpr = (LoadLocalExpr) e;
                Local usedLocal = assertDefined(usedLocalExpr.value());
                defs.get(usedLocal).removeUse(new ExprUse(usedLocalExpr));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Def table:\n");

        Iterator<Entry<Local, Def>> defIt = defs.entrySet().iterator();
        List<Use> uses = new ArrayList<>();
        while (defIt.hasNext()) {
            Entry<Local, Def> e = defIt.next();
            DeclareLocalStmt s = e.getValue().getStmt();
            String blockId = s.getBlock() != null ? String.valueOf(s.getBlock().id()) : "? ";
            sb.append(" ").append(e.getKey()).append(" => L").append(blockId).append(CodePrinter.toString(s));
            uses.addAll(e.getValue().getUses());

            if (defIt.hasNext() || !uses.isEmpty()) {
                sb.append("\n");
            }
        }

        sb.append("Use table:");
        if (!uses.isEmpty()) {
            sb.append("\n");
        }

        Iterator<Use> useIt = uses.iterator();
        while (useIt.hasNext()) {
            Use use = useIt.next();
            sb.append(" ").append(use.getLocal()).append(" => ").append(use);
            if (useIt.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
