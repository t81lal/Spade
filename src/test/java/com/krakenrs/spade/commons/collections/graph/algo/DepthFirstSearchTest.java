package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.TestVertexSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DepthFirstSearchTest {
    private final TestVertexSupplier vertexSupplier = new TestVertexSupplier();
    private final Supplier<Digraph<TestVertex, Edge<TestVertex>>> graphSupplier = Digraph::new;

    private static Edge<TestVertex> edge(TestVertex v1, TestVertex v2) {
        return new Edge<>(v1, v2);
    }

    @BeforeEach
    void setup() {
        vertexSupplier.reset();
    }

    @Test
    void testDepthFirstSearch1() {
        var g = graphSupplier.get();
        var v0 = vertexSupplier.get();
        var v1 = vertexSupplier.get();
        var v2 = vertexSupplier.get();
        var v3 = vertexSupplier.get();
        var v4 = vertexSupplier.get();
        g.addEdge(edge(v0, v1));
        g.addEdge(edge(v0, v2));
        g.addEdge(edge(v1, v3));
        g.addEdge(edge(v1, v4));

        DepthFirstSearch<TestVertex> search = new DepthFirstSearch<>(g);
        search.run(v0);

        assertEquals(List.of(v0, v1, v3, v4, v2), search.getPreOrder());
        assertEquals(List.of(v3, v4, v1, v2, v0), search.getPostOrder());
        assertFalse(search.getParent(v0).isPresent());
        assertEquals(v0, search.getParent(v1).orElseThrow());
        assertEquals(v0, search.getParent(v2).orElseThrow());
        assertEquals(v1, search.getParent(v3).orElseThrow());
        assertEquals(v1, search.getParent(v4).orElseThrow());
    }
}
