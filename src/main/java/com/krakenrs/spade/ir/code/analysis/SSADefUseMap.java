package com.krakenrs.spade.ir.code.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private final Map<Local, Def2> defs = new HashMap<>();
    private final AddStmtVisitor addStmtVisitor = new AddStmtVisitor();
    private final RemoveStmtVisitor removeStmtVisitor = new RemoveStmtVisitor();
    private boolean enforceSSA = true;

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

    public void replaceStmt(@NonNull Stmt oldStmt, @NonNull Stmt newStmt) {
        /* We only want to replace a def if it is overwriting another def
         * otherwise we are just replacing normal statements that only contain
         * defs. */

        boolean oldDef = oldStmt instanceof DeclareLocalStmt;
        boolean newDef = newStmt instanceof DeclareLocalStmt;

        if (oldDef ^ newDef) {
            throw new IllegalArgumentException("Can't do replace between a decl and a non decl");
        }

        if (oldDef) {
            /* both are defs */
            DeclareLocalStmt oldDecl = (DeclareLocalStmt) oldStmt;
            DeclareLocalStmt newDecl = (DeclareLocalStmt) newStmt;

            if (!oldDecl.var().equals(newDecl.var())) {
                throw new IllegalArgumentException("Can't replace decl of " + oldDecl.var() + " with " + newDecl.var());
            }

            /* do the replace */
            this.replaceDef(newDecl);
        } else {
            /* both are non def stmts */
            this.removeStmt(oldStmt);
            this.addStmt(newStmt);
        }
    }

    public void replaceDef(@NonNull DeclareLocalStmt newDecl) {
        Local declLocal = newDecl.var();
        assertDefined(declLocal);

        /* Here we are swapping out the old definition by removing the
         * old def and adding the new one, but this may invalidate the
         * map invariants, so we have to do this ourselves so that after
         * the map remains consistent. */

        Def2 oldDef = defs.get(declLocal);
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
        Def2 newDef = createDef(newDecl, oldDef.getUses());
        defs.put(declLocal, newDef);
    }

    private Def2 createDef(DeclareLocalStmt decl, Set<Use> uses) {
        if (decl instanceof AssignPhiStmt) {
            AssignPhiStmt phi = (AssignPhiStmt) decl;
            if (!phi.isInSSA()) {
                return new PartialPhiDef(phi, uses);
            }
        }
        return new CopyDef(decl, uses);
    }

    public DeclareLocalStmt getDef(@NonNull Local local) {
        return defs.get(assertDefined(local)).getStmt();
    }

    public Set<Use> getUses(@NonNull Local local) {
        return defs.get(assertDefined(local)).getUses();
    }

    private Local assertVersioned(Local local) {
        if (enforceSSA && !local.isVersioned()) {
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
                /* The lhs has to be an SSA local here but the right might not be
                 * in the case of a partial phi, so isInSSA will return false only
                 * if the rhs is partial */
                if (!defs.containsKey(declaredLocal)) {
                    defs.put(declaredLocal, createDef(decl, new HashSet<>()));
                } else {
                    throw new UnsupportedOperationException(declaredLocal + " is already defined");
                }
            }
        }

        @Override
        public void visitAssignPhiStmt(AssignPhiStmt s) {
            /* In the case of partial phis, we only added what we can */
            boolean isPartial = defs.get(assertDefined(s.var())) instanceof PartialPhiDef;
            for (Entry<CodeBlock, Local> e : s.arguments().entrySet()) {
                Local usedLocal = e.getValue();

                boolean shouldMakeUse = false;
                if (!isPartial) {
                    /* not partial => verify */
                    assertDefined(usedLocal);
                    shouldMakeUse = true;
                } else {
                    shouldMakeUse = defs.containsKey(usedLocal) && usedLocal.isVersioned();
                }

                if (shouldMakeUse) {
                    defs.get(usedLocal).addUse(new PhiUse(e.getKey(), usedLocal, s));
                }
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

        Iterator<Entry<Local, Def2>> defIt = defs.entrySet().iterator();
        List<Use> uses = new ArrayList<>();
        while (defIt.hasNext()) {
            Entry<Local, Def2> e = defIt.next();
            Def2 def = e.getValue();
            DeclareLocalStmt s = def.getStmt();
            String blockId = s.getBlock() != null ? String.valueOf(s.getBlock().id()) : "?";
            sb.append(" ").append(e.getKey());
            if (def instanceof PartialPhiDef) {
                sb.append(" (partial) ");
            }
            sb.append(" @ L").append(blockId).append(" | ").append(CodePrinter.toString(s));
            uses.addAll(def.getUses());

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
            sb.append(" ").append(use.getLocal()).append(" @ ").append(use);
            if (useIt.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
