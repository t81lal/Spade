package com.krakenrs.spade.ir.code.analysis;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.krakenrs.spade.ir.code.stmt.DeclareLocalStmt;

import lombok.Getter;
import lombok.NonNull;

public final class Def {
    @NonNull
    @Getter
    private final DeclareLocalStmt stmt;
    @NonNull
    @Getter
    private final Set<Use> uses;

    public Def(DeclareLocalStmt stmt) {
        this.stmt = stmt;
        this.uses = new HashSet<>();
    }

    public boolean addUse(Use use) {
        return uses.add(use);
    }

    public boolean removeUse(Use use) {
        return uses.remove(use);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stmt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        Def other = (Def) obj;
        return Objects.equals(other.stmt, stmt);
    }
}
