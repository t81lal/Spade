package com.krakenrs.spade.ir.code.stmt;

import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.value.Local;

public class AssignParamStmt extends DeclareLocalStmt {
    public AssignParamStmt(Local var) {
        super(Opcodes.ASSIGN_PARAM, var);
    }
}
