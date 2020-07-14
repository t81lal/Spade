package com.krakenrs.spade.ir.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.krakenrs.spade.SpadeContext;
import com.krakenrs.spade.commons.collections.tuple.Tuple2;
import com.krakenrs.spade.guice.ContextScope;
import com.krakenrs.spade.guice.ContextScopedGuicedPipelineExecutionContext;
import com.krakenrs.spade.guice.MethodScoped;
import com.krakenrs.spade.ir.algo.SsaBlockLivenessAnalyser;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.Expr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.factory.CodeFactory;
import com.krakenrs.spade.ir.code.factory.ConstCodeFactory;
import com.krakenrs.spade.ir.code.factory.SimpleConstCodeFactory;
import com.krakenrs.spade.ir.code.observer.CodeObservationManager;
import com.krakenrs.spade.ir.code.observer.DefaultCodeObservationManager;
import com.krakenrs.spade.ir.code.visitor.AbstractCodeReducer;
import com.krakenrs.spade.ir.gen.Generator.AbstractGeneratorFactory;
import com.krakenrs.spade.ir.gen.Generator.GeneratorFactory;
import com.krakenrs.spade.ir.gen.asm.AsmGenerationCtx;
import com.krakenrs.spade.ir.gen.asm.AsmGenerator;
import com.krakenrs.spade.ir.type.MockTypeManager;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;
import com.krakenrs.spade.pipeline.Pipeline;
import com.krakenrs.spade.pipeline.PipelineStep;
import com.krakenrs.spade.pipeline.execution.ExecutionException;
import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;

public class SSAPassTest {

	private static Tuple2<ClassNode, MethodNode> getContext(Class<?> clazz, String methodName) throws IOException {
		ClassReader cr = new ClassReader(clazz.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		
		for(MethodNode mn : cn.methods) {
			if(mn.name.equals(methodName)) {
				return new Tuple2.ImmutableTuple2<ClassNode, MethodNode>(cn, mn);
			}
		}
		
		throw new IllegalArgumentException("Couldn't find " + cn.name + "." + methodName);
	}
	
	public void test1() {
		String place = "kekistan";
		System.out.println("welcome to " + place);
	}
	
	// x = 1, y = 2
	public static int test2(boolean b, int x, int y) {
		int z = 0;
		System.out.println(b);
		if(b) {
			z = x + y;
		} else {
			z = x - y;
		}
		return z;
	}
	
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(level);
	}
	
    static class LoggingMethodInterceptor implements MethodInterceptor {
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Object returnValue = null;
            long start = System.currentTimeMillis();
            System.out.println("Method " + invocation.getMethod() + " entered from " + invocation.getThis()
                    + " with arguments " + invocation.getArguments());
            returnValue = invocation.proceed();
            long end = System.currentTimeMillis();
            System.out
                    .println("Method " + invocation.getMethod() + " exited in " + ((end - start) / 1000L) + " seconds");
            return returnValue;
        }
    }

    static class TestModule extends AbstractModule {
        private final TypeManager typeManager;
        private final ContextScope<GenerationCtx> genCtxScope;
        
        public TestModule(TypeManager typeManager) {
            this.genCtxScope = new ContextScope<>();
            this.typeManager = typeManager;
        }

        /**
         * Binds the context in which the calling thread is bound to with the {@link GenerationCtx} type. This is scoped
         * with the {@link MethodScoped} scope, meaning that each GenerationCtx is bound to one ContextScope.
         * <p>
         * In a way this is a "boostrapping" method as it's not possible to construct a Provider for the current
         * context without knowing which context we are operating under, therefore we need to make an explicit call to
         * the context stored outside of the usual {@link Provider} cache in {@link ContextScope}.
         * 
         * @return The current scope, however, this function is used to wire a {@link Provider} so is not meant to be
         * called by user code
         */
        @Provides
        @MethodScoped
        GenerationCtx provideContext() {
            return genCtxScope.getCurrentContext();
        }
        
//        @Provides
//        @Inject
//        @MethodScoped
//        SsaBlockLivenessAnalyser.Factory provideSsaBlockLivenessAnalyserFactory(CodeObservationManager codeObservationManager) {
//        	return new SsaBlockLivenessAnalyser.Factory(codeObservationManager);
//        }

        @Override
        protected void configure() {
            /* Bind our scope annotation with our actual scope/scope manager */
            bindScope(MethodScoped.class, genCtxScope);
            
            bind(TypeManager.class).toInstance(typeManager);
            
            /* Use a different CodeObservationManager for each method scope, i.e. it's like a singleton for
             * every method/context/IR */
            bind(CodeObservationManager.class).to(DefaultCodeObservationManager.class).in(MethodScoped.class);
                        
            install(new FactoryModuleBuilder().build(AsmGenerator.AsmGeneratorFactory.class));
            
            /* These are all created as singletons (the scope doesn't actually matter), but when
             * the create() methods are called, they use dependencies from the context if they can
             * so we don't have to create scoped providers and verbose factories even though that
             * feels like the more correct approach. */
            install(new FactoryModuleBuilder().build(SSAGenerator.Factory.class));
            install(new FactoryModuleBuilder().build(CodeBlock.Factory.class));
            install(new FactoryModuleBuilder().build(ControlFlowGraph.Factory.class));
            
            install(new FactoryModuleBuilder().build(SsaBlockLivenessAnalyser.Factory.class));
            
            install(new FactoryModuleBuilder().build(CodeFactory.class));
            bind(ConstCodeFactory.class).toInstance(new SimpleConstCodeFactory());

            /* Register the different GeneratorFactory's for the GenerationContext's that are supported */
            MapBinder<Class<? extends GenerationCtx>, GeneratorFactory<?>> mapBinder = MapBinder.newMapBinder(binder(),
                    /* type literals here because java */
                    new TypeLiteral<Class<? extends GenerationCtx>>() {},
                    new TypeLiteral<GeneratorFactory<?>>() {});

            mapBinder.addBinding(AsmGenerationCtx.class).to(AsmGenerator.AsmGeneratorFactory.class);
        }
    }
	
	static class MakeGeneratorStep implements PipelineStep<GenerationCtx, Generator> {
	    private final AbstractGeneratorFactory abstractFactory;
	    
	    @Inject
	    public MakeGeneratorStep(AbstractGeneratorFactory abstractFactory) {
	        this.abstractFactory = abstractFactory;
	    }
	    
        @Override
        public Generator apply(GenerationCtx ctx) {
            GeneratorFactory<GenerationCtx> generatorFactory = abstractFactory.create(ctx);
            Generator generator = generatorFactory.create(ctx);
            return generator;
        }
	}
	
	static class ExecuteGeneratorStep implements PipelineStep<Generator, ControlFlowGraph> {
        @Override
        public ControlFlowGraph apply(Generator t) {
            ControlFlowGraph cfg = t.run();
            return cfg;
        }
	}
	
	static class EnterSSAStep implements PipelineStep<ControlFlowGraph, ControlFlowGraph> {
        private final SSAGenerator.Factory generatorFactory;
        
        @Inject
        public EnterSSAStep(GenerationCtx context, SSAGenerator.Factory generatorFactory) {
            this.generatorFactory = generatorFactory;
            
            System.out.println("For ssa: " + context.isStaticMethod());
        }
        
	    @Override
        public ControlFlowGraph apply(ControlFlowGraph cfg) {
	        SSAGenerator generator = generatorFactory.create(cfg);
	        generator.doTransform();
	        return cfg;
        }
	}
	
    private static Function<GenerationCtx, PipelineExecutionContext<GenerationCtx>> getSpadeContextCreator(
            TestModule module) {
        ContextScope<GenerationCtx> scope = module.genCtxScope;
        Injector injector = Guice.createInjector(module);
        return s -> {
            return new ContextScopedGuicedPipelineExecutionContext<GenerationCtx>(s, injector, scope,
                    GenerationCtx.class);
        };
    }

    public static void main(String[] args) throws IOException, ExecutionException {
        var typeManager = new MockTypeManager();
        
        var module = new TestModule(typeManager);

        var pipeline = Pipeline.<GenerationCtx>from()
                .then(MakeGeneratorStep.class)
                .then(ExecuteGeneratorStep.class)
                .then(EnterSSAStep.class)
                .build(getSpadeContextCreator(module));

        {
            var target = getContext(SSAPassTest.class, "test2");
            var ctx = new AsmGenerationCtx(typeManager, target.getA(), target.getB());
            ControlFlowGraph ssaCfg = pipeline.execute(ctx);
            
            System.out.println(CodePrinter.toString(ssaCfg));
        }
        
        
//        {
//            var target = getContext(SSAPassTest.class, "test1");
//            var ctx = new AsmGenerationCtx(typeManager, target.getA(), target.getB());
//            ControlFlowGraph ssaCfg = pipeline.execute(ctx);
//        }

    }

//	public static void main1(String[] args) throws IOException {
//		setLoggingLevel(ch.qos.logback.classic.Level.ALL);
//		
//		var module = new CodeObserverModule();
//		Injector injector = Guice.createInjector(module);
//		var contextScope = module.getContextScope();
//		
//		SpadeContext spadeContext = new SpadeContext() {};
//		
//		contextScope.enter(spadeContext);
//		
//		var typeManager = new MockTypeManager();
//		var target = getContext(SSAPassTest.class, "test2");
//		var ctx = new AsmGenerationCtx(typeManager, target.getA(), target.getB());
//		
//		GeneratorFactory generatorFactory = injector.getInstance(GeneratorFactory.class);
//		Generator generator = generatorFactory.createGenerator(ctx);
//		var cfg = generator.run();
//		
//		AbstractCodeReducer red = new AbstractCodeReducer() {
//		    @Override
//		    public Expr reduceLoadLocalExpr(LoadLocalExpr e) {
//		        
//		        Local var = e.value();
//		        
//		        if(!var.isStack() && var.index() > 0) {
//		            return new LoadConstExpr<>(new Constant<>(var.index(), PrimitiveType.INT));
//		        } else {
//		            return null;
//		        }
//		    }
//		};
//		
//		for(var b : cfg.getVertices()) {
//		    for(var s : new ArrayList<>(b.stmts())) {
//		        var newStmt = s.reduceStmt(red);
//		        
//		        if(newStmt != null && s != newStmt) {
//		            b.replaceStmt(s, newStmt);
//		        }
//		    }
//		}
//		
//		contextScope.exit(spadeContext);
//		
//	}
}
