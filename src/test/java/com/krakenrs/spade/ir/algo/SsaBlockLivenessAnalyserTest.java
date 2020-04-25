package com.krakenrs.spade.ir.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.FlowEdge;
import com.krakenrs.spade.ir.code.expr.ArithmeticExpr;
import com.krakenrs.spade.ir.code.expr.InvokeExpr;
import com.krakenrs.spade.ir.code.expr.LoadFieldExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadConstExpr;
import com.krakenrs.spade.ir.code.expr.value.LoadLocalExpr;
import com.krakenrs.spade.ir.code.stmt.*;
import com.krakenrs.spade.ir.type.SimpleTypeManager;
import com.krakenrs.spade.ir.value.Constant;
import com.krakenrs.spade.ir.value.Local;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker.PropTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class SsaBlockLivenessAnalyserTest {
    static ControlFlowGraph getMyMethod() {
        var tm = new SimpleTypeManager();
        var cfg = new ControlFlowGraph(tm.asMethodType("(ZII)I"), true);

        var cb0 = cfg.getEntryBlock();
        var cb1 = new CodeBlock(1);
        var cb2 = new CodeBlock(2);
        var cb3 = new CodeBlock(3);
        var cb4 = new CodeBlock(4);
        var cb5 = new CodeBlock(5);
        var cb6 = new CodeBlock(6);

        {
            cb0.setOrderHint(0);
            cb0.appendStmt(new AssignParamStmt(new Local(0, false)));
            cb0.appendStmt(new AssignParamStmt(new Local(1, false)));
            cb0.appendStmt(new AssignParamStmt(new Local(2, false)));
        }

        {
            cb1.setOrderHint(1);
            cb1.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadConstExpr<>(new Constant<>((byte) 0, tm.asValueType("B")))));
            cb1.appendStmt(new AssignLocalStmt(new Local(3, false),
                    new LoadLocalExpr(tm.asValueType("B"), new Local(0, true))));
            cfg.addVertex(cb1);
        }

        {
            cb2.setOrderHint(2);
            cb2.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadFieldExpr.LoadStaticFieldExpr(tm.asClassType("java/lang/System"), "out",
                            tm.asValueType("Ljava/io/PrintStream;"))));
            cb2.appendStmt(new AssignLocalStmt(new Local(1, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(0, false))));
            cb2.appendStmt(new ConsumeStmt(
                    new InvokeExpr.InvokeVirtualExpr(tm.asClassType("java/io/PrintStream"), "println",
                            tm.asMethodType("(Z)V"), InvokeExpr.Mode.VIRTUAL,
                            new LoadLocalExpr(tm.asValueType("Ljava/io/PrintStream;"), new Local(0, true)),
                            List.of(new LoadLocalExpr(tm.asValueType("I"), new Local(1, true))))));
            cfg.addVertex(cb2);
        }

        {
            cb3.setOrderHint(3);
            cb3.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(0, false))));
            cb3.appendStmt(new JumpCondStmt(new LoadLocalExpr(tm.asValueType("I"), new Local(0, true)),
                    new LoadConstExpr<>(new Constant<>((byte) 0, tm.asValueType("B"))), JumpCondStmt.Mode.EQ, cb4));
            cfg.addVertex(cb3);
        }

        {
            cb5.setOrderHint(4);
            cb5.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(1, false))));
            cb5.appendStmt(new AssignLocalStmt(new Local(1, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(2, false))));
            cb5.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new ArithmeticExpr(tm.asValueType("I"), ArithmeticExpr.Operation.ADD,
                            new LoadLocalExpr(tm.asValueType("I"), new Local(0, true)),
                            new LoadLocalExpr(tm.asValueType("I"), new Local(1, true)))));
            cb5.appendStmt(new AssignLocalStmt(new Local(3, false),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(0, true))));
            cb5.appendStmt(new JumpUncondStmt(cb6));
            cfg.addVertex(cb5);
        }

        {
            cb4.setOrderHint(5);
            cb4.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(1, false))));
            cb4.appendStmt(new AssignLocalStmt(new Local(1, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(2, false))));
            cb4.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new ArithmeticExpr(tm.asValueType("I"), ArithmeticExpr.Operation.SUB,
                            new LoadLocalExpr(tm.asValueType("I"), new Local(0, true)),
                            new LoadLocalExpr(tm.asValueType("I"), new Local(1, true)))));
            cb4.appendStmt(new AssignLocalStmt(new Local(3, false),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(0, true))));
            cfg.addVertex(cb4);
        }

        {
            cb6.setOrderHint(6);
            cb6.appendStmt(new AssignLocalStmt(new Local(0, true),
                    new LoadLocalExpr(tm.asValueType("I"), new Local(3, false))));
            cb6.appendStmt(new ReturnStmt(new LoadLocalExpr(tm.asValueType("I"), new Local(0, true))));
            cfg.addVertex(cb6);
        }

        {
            cfg.addEdge(new FlowEdge.ImmediateEdge(cb3, cb5));
            cfg.addEdge(new FlowEdge.JumpEdge(cb5, cb6, FlowEdge.Kind.UNCONDITIONAL));
            cfg.addEdge(new FlowEdge.ImmediateEdge(cb2, cb3));
            cfg.addEdge(new FlowEdge.ImmediateEdge(cb1, cb2));
            cfg.addEdge(new FlowEdge.ImmediateEdge(cb4, cb6));
            cfg.addEdge(new FlowEdge.JumpEdge(cb3, cb4, FlowEdge.Kind.CONDITIONAL));
            cfg.addEdge(new FlowEdge.ImmediateEdge(cb0, cb1));
        }

        return cfg;
    }

    static Stream<Arguments> tests() {
        return Stream.of(
                Arguments.of(getMyMethod(), "liveness/myMethod.g")
        );
    }

    @ParameterizedTest
    @MethodSource("tests")
    void test1(ControlFlowGraph cfg, String g) throws Exception {
        var analyser = new SsaBlockLivenessAnalyser(cfg);
        var g2 = makeLivenessGraph(cfg, analyser);
        var checker = createChecker(g, LivenessBlock::new, Edge::new);
        checker.verify(PropTime.POST, g2, varParser);
    }

    static <V extends Vertex> GraphAssertionChecker<V, Edge<V>> createChecker(String fileName,
            Function<Integer, V> vertexCreator, BiFunction<V, V, Edge<V>> edgeCreator) throws Exception {
        return GraphAssertionChecker.createChecker(SsaBlockLivenessAnalyserTest.class, fileName, vertexCreator,
                edgeCreator, true);
    }
    
    static Digraph<LivenessBlock, Edge<LivenessBlock>> makeLivenessGraph(ControlFlowGraph cfg,
            SsaBlockLivenessAnalyser analyser) {
        // Assumes that the blocks in the cfg are ordered with ids starting from 0 and that
        // the generator is deterministic in the way it generates the block ids.
        //  in fact, we can use the orderHint which should represent the order of the blocks
        //  in the original code
        var newGraph = new Digraph<LivenessBlock, Edge<LivenessBlock>>();

        Map<CodeBlock, LivenessBlock> blockMapping = new HashMap<>();
        for (CodeBlock block : cfg.getVertices()) {
            LivenessBlock newBlock = new LivenessBlock(block.getOrderHint(), analyser.getLiveIn(block),
                    analyser.getLiveOut(block));
            newGraph.addVertex(newBlock);
            blockMapping.put(block, newBlock);
        }

        for (FlowEdge e : cfg.getEdges()) {
            newGraph.addEdge(new Edge<>(blockMapping.get(e.getSource()), blockMapping.get(e.getDestination())));
        }

        return newGraph;
    }

    // Rules: put svars before lvars and lowest index wins.
    static final Comparator<Local> localComparator = (a, b) -> {
        if (a.isStack() == b.isStack()) {
            // both svar or both lvar
            return a.index() - b.index();
        } else if (a.isStack()) {
            return -1;
        } else {
            return +1;
        }
    };

    static final Function<String, Local> varParser = (alias) -> {
        boolean isStack;
        if (alias.startsWith("svar")) {
            isStack = true;
        } else if (alias.startsWith("lvar")) {
            isStack = false;
        } else {
            throw new IllegalArgumentException(alias);
        }
        String intPart = alias.substring(4);
        int index = Integer.parseInt(intPart);
        return new Local(index, isStack);
    };

    static class LivenessBlock extends TestVertex {
        List<Local> in, out;

        public LivenessBlock(int id) {
            super(id);
        }

        public LivenessBlock(int id, Set<Local> in, Set<Local> out) {
            super(id);

            this.in = new ArrayList<>(in);
            this.in.sort(localComparator);
            this.out = new ArrayList<>(out);
            this.out.sort(localComparator);
        }
    }
}
