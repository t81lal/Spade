package com.krakenrs.spade.app.asm;

import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class Klass extends ClassNode {

    private final KlassSource source;

    public Klass(KlassSource source) {
        super(Opcodes.ASM8);
        this.source = Objects.requireNonNull(source);
    }
    
    public KlassSource getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 37 + this.source.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Klass) {
            Klass other = (Klass) o;
            return super.equals(other) && Objects.equals(this.source, other.source);
        } else {
            return false;
        }
    }
}
