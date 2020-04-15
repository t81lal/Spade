package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.value.Local;

public class AssignCatchStmt extends DeclareLocalStmt {
    private final ClassType type;

    public AssignCatchStmt(Local var, ClassType type) {
        super(Opcodes.ASSIGN_CATCH, var);
        this.type = type;
    }

    public ClassType type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
