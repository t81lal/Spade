package com.krakenrs.spade.ir.code.analysis;

import java.util.Objects;

import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.stmt.AssignPhiStmt;
import com.krakenrs.spade.ir.value.Local;

import lombok.Getter;
import lombok.NonNull;

public final class PhiUse extends Use {
    @NonNull
    @Getter
    private final AssignPhiStmt stmt;
    @NonNull
    @Getter
    private final CodeBlock predBlock;

    public PhiUse(CodeBlock predBlock, Local local, AssignPhiStmt stmt) {
        super(local);
        this.predBlock = predBlock;
        this.stmt = stmt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, stmt, predBlock);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        PhiUse other = (PhiUse) obj;
        return Objects.equals(local, other.local) && Objects.equals(stmt, other.stmt)
                && Objects.equals(predBlock, other.predBlock);
    }

    @Override
    public String toString() {
        return CodePrinter.toString(stmt) + ", L" + predBlock.id() + "/" + local;
    }
}
