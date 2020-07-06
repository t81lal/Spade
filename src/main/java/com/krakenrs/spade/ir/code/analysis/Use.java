package com.krakenrs.spade.ir.code.analysis;

import com.krakenrs.spade.ir.value.Local;

import lombok.Getter;
import lombok.NonNull;

public abstract class Use {
    @NonNull
    @Getter
    protected final Local local;

    public Use(Local var) {
        this.local = var;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
