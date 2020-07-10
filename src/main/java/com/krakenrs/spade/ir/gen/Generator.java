package com.krakenrs.spade.ir.gen;

import java.util.Map;

import com.google.inject.Inject;
import com.krakenrs.spade.ir.code.ControlFlowGraph;

public interface Generator {
    ControlFlowGraph run();

    public interface GeneratorFactory<C extends GenerationCtx> {
        Generator create(C ctx);
    }

    public class AbstractGeneratorFactory {
        private final Map<Class<? extends GenerationCtx>, GeneratorFactory<?>> factories;

        @Inject
        public AbstractGeneratorFactory(Map<Class<? extends GenerationCtx>, GeneratorFactory<?>> factories) {
            this.factories = factories;
        }

        @SuppressWarnings("unchecked")
        <C extends GenerationCtx> GeneratorFactory<C> create(C ctx) {
            Class<? extends GenerationCtx> key = ctx.getClass();
            if (factories.containsKey(key)) {
                return (GeneratorFactory<C>) factories.get(key);
            } else {
                throw new IllegalArgumentException("No factory for " + key);

            }
        }
    }
}
