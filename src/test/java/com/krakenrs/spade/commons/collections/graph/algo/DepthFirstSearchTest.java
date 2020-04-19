package com.krakenrs.spade.commons.collections.graph.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker.PropTime;

class DepthFirstSearchTest {

    static <V extends Vertex> GraphAssertionChecker<V, Edge<V>> createChecker(String fileName,
            Function<Integer, V> vertexCreator, BiFunction<V, V, Edge<V>> edgeCreator) throws Exception {
        return GraphAssertionChecker.createChecker(DepthFirstSearchTest.class, fileName, vertexCreator, edgeCreator);
    }

    class DfsVertex extends TestVertex {
        int pre, post, topo;
        int parent;
        public DfsVertex(int id) {
            super(id);
        }
    }

    class DfsEdge extends Edge<DfsVertex> {
        List<String> type = new ArrayList<>();
        public DfsEdge(DfsVertex source, DfsVertex destination) {
            super(source, destination);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "dfs1.g", "dfs2.g" })
    void test(String fileName) throws Exception {
        var checker = createChecker("dfs/" + fileName, DfsVertex::new, DfsEdge::new);
        var g = checker.createGraph();

        checker.verify(PropTime.PRE, g);

        var search = new DepthFirstSearch<>(g);
        search.run(new DfsVertex(0));
        
        for (var v : g.getVertices()) {
            v.pre = search.getPreOrder().indexOf(v);
            v.post = search.getPostOrder().indexOf(v);
            v.topo = search.getTopOrder().indexOf(v);
            v.parent = search.getParent(v).map(DfsVertex::getId).orElse(-1);
        }

        for (var et : DepthFirstSearch.EdgeType.values()) {
            for (var e : search.getEdges(et)) {
                ((DfsEdge) e).type.add(et.name().toLowerCase());
            }
        }

        checker.verify(PropTime.POST, g);
    }
}
