package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.commons.collections.graph.invariants.GraphAssertionChecker;
import com.krakenrs.spade.commons.collections.graph.invariants.GraphAssertionChecker.PropTime;
import com.krakenrs.spade.commons.collections.graph.invariants.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DominatorComputerTest {
    static <V extends Vertex> GraphAssertionChecker<V, Edge<V>> createChecker(String fileName,
            Function<Integer, V> vertexCreator, BiFunction<V, V, Edge<V>> edgeCreator) throws Exception {
        Parser<V, Edge<V>> parser = new Parser<>(
                String.join("\n",
                        Files.readAllLines(
                                new File(DominatorComputerTest.class.getResource(fileName).getPath()).toPath()))
                        .toCharArray(),
                vertexCreator, edgeCreator);
        return parser.parse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "dom1", "dom2", "dom3", "ladder" })
    void test(String fileName) throws Exception {
        var expectedDomTree = createChecker("dominator/" + fileName + "_domtree.g", DominatorVertex::new, Edge::new)
                .createGraph();
        var checker = createChecker("dominator/" + fileName + ".g", DominatorVertex::new, DominatorEdge::new);
        var g = checker.createGraph();

        checker.verify(PropTime.PRE, g);

        var computer = new DominatorComputer<>(g, new DominatorVertex(0), true);
        computer.run();

        for (var v : g.getVertices()) {
            v.dominates = computer.getDominates(v).stream().map(TestVertex::getId)
                    .collect(Collectors.toList());
            Collections.sort(v.dominates);

            v.frontier = computer.getDominanceFrontier(v).stream().map(TestVertex::getId)
                    .collect(Collectors.toList());
            Collections.sort(v.frontier);
        }

        checker.verify(PropTime.POST, g);

        var realDomTree = computer.getDominatorTree();
        assertEquals(expectedDomTree.getVertices(), realDomTree.getVertices());
        for (var v : expectedDomTree.getVertices()) {
            assertEquals(expectedDomTree.getEdges(v), realDomTree.getEdges(v));
        }
    }

    static class DominatorVertex extends TestVertex {
        List<Integer> dominates;
        List<Integer> frontier;

        public DominatorVertex(int id) {
            super(id);
        }
    }

    static class DominatorEdge extends Edge<DominatorVertex> {
        public DominatorEdge(DominatorVertex source, DominatorVertex destination) {
            super(source, destination);
        }
    }

    final Supplier<Digraph<TestVertex, Edge<TestVertex>>> graphSupplier = Digraph::new;

    static Edge<TestVertex> edge(int v1, int v2) {
        return new Edge<>(new TestVertex(v1), new TestVertex(v2));
    }
}
