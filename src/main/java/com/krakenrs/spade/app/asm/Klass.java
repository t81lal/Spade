package com.krakenrs.spade.app.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class Klass extends ClassNode {

    public Klass() {
        super(Opcodes.ASM8);
    }
}
