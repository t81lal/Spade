package com.krakenrs.spade.ir.code.stmt;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.Opcodes;
import com.krakenrs.spade.ir.code.Stmt;

public class JumpUncondStmt extends Stmt {

    private final CodeBlock target;

    public JumpUncondStmt(CodeBlock target) {
        super(Opcodes.JUMP_UNCOND);
        this.target = target;
    }

    public CodeBlock target() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target);
    }
}
