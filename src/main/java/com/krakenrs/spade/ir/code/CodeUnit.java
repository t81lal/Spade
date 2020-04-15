package com.krakenrs.spade.ir.code;

public abstract class CodeUnit {
    private static int codeUnitIds = 0;

    final int id;
    final int opcode;

    public CodeUnit(int opcode) {
        this.id = codeUnitIds++;
        this.opcode = opcode;
    }

    public abstract Stmt stmt();

    public int id() {
        return id;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int hashCode() {
        // Position independent hash
        return opcode;
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
