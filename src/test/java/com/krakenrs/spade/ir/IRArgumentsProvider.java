package com.krakenrs.spade.ir;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.gen.AsmGenerationCtx;
import com.krakenrs.spade.ir.gen.AsmGenerator;
import com.krakenrs.spade.ir.type.ResolvingMockTypeManager;

public class IRArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<IRSource> {

    private ControlFlowGraph[] cfgs;

    @Override
    public void accept(IRSource t) {
        Class<?>[] classes = t.classes();
        String[] methods = t.methodNames();
        if (classes.length != methods.length) {
            throw new JUnitException(String.format("%d classes vs %d methods", classes.length, methods.length));
        }
        cfgs = new ControlFlowGraph[classes.length];

        for (int i = 0; i < classes.length; i++) {
            Class<?> c = classes[i];
            String m = methods[i];

            try {
                ClassReader cr = new ClassReader(c.getName());
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                
                List<MethodNode> mns = cn.methods.stream().filter(mn -> mn.name.equals(m)).collect(Collectors.toList());
                if (mns.size() != 1) {
                    throw new JUnitException(String.format("Found %d methods with for %s.%s", mns.size(), cn.name, m));
                }

                AsmGenerationCtx ctx = new AsmGenerationCtx(new ResolvingMockTypeManager(), cn, mns.get(0));
                cfgs[i] = AsmGenerator.run(ctx);
            } catch (IOException e) {
                throw new JUnitException(String.format("Error loading %s.%s", c.getName(), m), e);
            }
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Arrays.stream(cfgs).map(Arguments::of);
    }

}
