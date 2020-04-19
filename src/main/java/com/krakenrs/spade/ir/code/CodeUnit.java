package com.krakenrs.spade.ir.code;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.code.visitor.LocalUsageVisitor;
import com.krakenrs.spade.ir.value.Local;

public abstract class CodeUnit {
    private static int codeUnitIds = 0;

    final int id;
    final int opcode;

    public CodeUnit(int opcode) {
        this.id = codeUnitIds++;
        this.opcode = opcode;
    }

    public int id() {
        return id;
    }

    public int opcode() {
        return opcode;
    }

    public Set<Local> getUses() {
        LocalUsageVisitor vis = new LocalUsageVisitor();
        accept(vis);
        return vis.getUses();
    }

    public abstract Stmt stmt();

    public void accept(CodeVisitor vis) {
        vis.visitAny(this);
    }

    @Override
    public int hashCode() {
        // Position independent hash
        return opcode;
    }

    public abstract boolean equivalent(CodeUnit u);

    public static boolean equivalent(CodeUnit u1, CodeUnit u2) {
        Objects.requireNonNull(u1);
        Objects.requireNonNull(u2);
        return u1.equivalent(u2);
    }

    public static boolean equivalent(List<? extends CodeUnit> l1, List<? extends CodeUnit> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        final int len = l1.size();
        for (int i = 0; i < len; i++) {
            if (!equivalent(l1.get(i), l2.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof CodeUnit) {
            // Check whether this expression is exactly the same expression as the given object
            return ((CodeUnit) o).id() == id;
        } else {
            return false;
        }
    }
}
