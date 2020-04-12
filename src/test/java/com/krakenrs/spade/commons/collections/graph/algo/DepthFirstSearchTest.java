package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.TestVertexSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
        assertEquals(Set.of(), search.getEdges(DepthFirstSearch.VertexColour.BLACK));
        assertEquals(Set.of(), search.getEdges(DepthFirstSearch.VertexColour.GREY));
        assertEquals(Set.of(edge(v0, v1), edge(v1, v3), edge(v0, v2), edge(v1, v4)),
                search.getEdges(DepthFirstSearch.VertexColour.WHITE));
    }

    @Test
    void testDepthFirstSearch2() {
        // https://upload.wikimedia.org/wikipedia/commons/5/57/Tree_edges.svg
        var g = graphSupplier.get();
        var v0 = vertexSupplier.get();
        var v1 = vertexSupplier.get();
        var v2 = vertexSupplier.get();
        var v3 = vertexSupplier.get();
        var v4 = vertexSupplier.get();
        var v5 = vertexSupplier.get();
        var v6 = vertexSupplier.get();
        var v7 = vertexSupplier.get();
        g.addEdge(edge(v0, v1));
        g.addEdge(edge(v0, v4));
        g.addEdge(edge(v0, v7));
        g.addEdge(edge(v1, v2));
        g.addEdge(edge(v2, v3));
        g.addEdge(edge(v3, v1));
        g.addEdge(edge(v4, v5));
        g.addEdge(edge(v5, v2));
        g.addEdge(edge(v5, v6));
        g.addEdge(edge(v5, v7));

        var search = new DepthFirstSearch<>(g);
        search.run(v0);

        // Test parents.
        Map.of(
                v1, v0,
                v2, v1,
                v3, v2,
                v4, v0,
                v5, v4,
                v6, v5,
                v7, v5
        ).forEach((k, v) -> assertEquals(v, search.getParent(k).orElseThrow()));

        // Test vertex ordering.
        assertEquals(List.of(v3, v2, v1, v6, v7, v5, v4, v0), search.getPostOrder());
        assertEquals(List.of(v0, v1, v2, v3, v4, v5, v6, v7), search.getPreOrder());

        // Test edge classification.
        assertEquals(Set.of(edge(v0, v1), edge(v1, v2), edge(v2, v3), edge(v4, v5), edge(v5, v6), edge(v5, v7),
                edge(v0, v4)), search.getEdges(DepthFirstSearch.VertexColour.WHITE));
        assertEquals(Set.of(edge(v0, v7), edge(v5, v2)), search.getEdges(DepthFirstSearch.VertexColour.BLACK));
        assertEquals(Set.of(edge(v3, v1)), search.getEdges(DepthFirstSearch.VertexColour.GREY));
    }
}
