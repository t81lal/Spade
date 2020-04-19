package com.krakenrs.spade.ir.algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.ir.IRSource;
import com.krakenrs.spade.ir.code.CodeBlock;
import com.krakenrs.spade.ir.code.CodePrinter;
import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.code.FlowEdge;
import com.krakenrs.spade.ir.value.Local;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker.PropTime;

public class SSABlockLivenessAnalyserTest {

    int myMethod(boolean b, int x, int y) {
        int z = 0;
        System.out.println(b);
        if (b) {
            z = x + y;
        } else {
            z = x - y;
        }
        return z;
    }

    @ParameterizedTest
    @IRSource(classes = { SSABlockLivenessAnalyserTest.class }, methodNames = { "myMethod" })
    void test1(ControlFlowGraph cfg) throws Exception {
        System.out.println(CodePrinter.toString(cfg));
        var analyser = new SSABlockLivenessAnalyser(cfg);
        for(var  b: cfg.getVertices()) {
            System.out.println(b.id());
            System.out.println(analyser.getLiveIn(b));
            System.out.println(analyser.getLiveOut(b));
        }
        var g2 = makeLivenessGraph(cfg, analyser);
        var checker = createChecker("liveness/myMethod.g", LivenessBlock::new, Edge::new);
        checker.verify(PropTime.POST, g2, varParser);
    }

    static <V extends Vertex> GraphAssertionChecker<V, Edge<V>> createChecker(String fileName,
            Function<Integer, V> vertexCreator, BiFunction<V, V, Edge<V>> edgeCreator) throws Exception {
        return GraphAssertionChecker.createChecker(SSABlockLivenessAnalyserTest.class, fileName, vertexCreator,
                edgeCreator, true);
    }
    
    static Digraph<LivenessBlock, Edge<LivenessBlock>> makeLivenessGraph(ControlFlowGraph cfg,
            SSABlockLivenessAnalyser analyser) {
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
