package com.krakenrs.spade.ir.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.krakenrs.spade.ir.code.visitor.AbstractValueVisitor;
import com.krakenrs.spade.ir.code.visitor.CheckSSAVisitor;
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

    public <T> T getValue(AbstractValueVisitor<T> vis) {
    	accept(vis);
    	return vis.get();
    }
    
    public Set<Local> getUses() {
        return getValue(new LocalUsageVisitor());
    }
    
    public boolean isInSSA() {
    	return getValue(new CheckSSAVisitor());
    }

    public abstract Stmt stmt();

    public void accept(CodeVisitor vis) {
        vis.visitAny(this);
    }

    public abstract CodeUnit deepCopy();

    @SuppressWarnings("unchecked")
    public static <T extends CodeUnit> List<T> deepCopy(List<T> units) {
        List<T> copied = new ArrayList<>();
        for(T unit : units) {
            // cast is implicitly supported by the copy contract
            copied.add((T)unit.deepCopy());
        }
        return copied;
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

    /* hashCode and equals should not be overriden, the only way two
     * units can be equal should be because of their id. */
    @Override
    public final int hashCode() {
        return id;
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
