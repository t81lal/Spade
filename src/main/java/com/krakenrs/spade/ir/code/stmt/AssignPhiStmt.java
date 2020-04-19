package com.krakenrs.spade.ir.code.stmt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodeUnit;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.visitor.CodeVisitor;
import com.krakenrs.spade.ir.value.Local;

public class AssignPhiStmt extends DeclareLocalStmt {
    private final Map<CodeBlock, Local> arguments;

    public AssignPhiStmt(Local var, Map<CodeBlock, Local> arguments) {
        super(Opcodes.ASSIGN_PHI, var);
        this.arguments = Collections.unmodifiableMap(arguments);
    }

    @Override
    public void accept(CodeVisitor vis) {
        super.accept(vis);
        vis.visitAssignPhiStmt(this);
    }

    public Map<CodeBlock, Local> arguments() {
        return arguments;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), arguments);
    }

    @Override
    public boolean equivalent(CodeUnit u) {
        return super.equivalent(u) && Objects.equals(((AssignPhiStmt) u).arguments, arguments);
    }
}
