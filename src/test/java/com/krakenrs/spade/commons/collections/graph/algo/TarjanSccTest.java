package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.TestVertexSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarjanSccTest {
    private TestVertexSupplier vertexSupplier = new TestVertexSupplier();
    private Supplier<Digraph<TestVertex, Edge<TestVertex>>> graphSupplier = Digraph::new;

    private static Edge<TestVertex> edge(TestVertex v1, TestVertex v2) {
        return new Edge<>(v1, v2);
    }

    @BeforeEach
    void setup() {
        vertexSupplier.reset();
    }

    @Test
    void testTarjanScc1() {
        /*
         * +--------------+  +------+
         * |              |  |      |
         * |  V0----->V1------->V3  |
         * |   ^      /   |  |   |  |
         * |   |     /    |  +---|--+
         * |  V2<----     |      |
         * |              |  +---|--+
         * +--------------+  |   V  |
         *                   |  V4  |
         *                   |      |
         *                   +------+
         */
        var g = graphSupplier.get();
        var v0 = vertexSupplier.get();
        var v1 = vertexSupplier.get();
        var v2 = vertexSupplier.get();
        var v3 = vertexSupplier.get();
        var v4 = vertexSupplier.get();
        g.addEdge(edge(v0, v1));
        g.addEdge(edge(v1, v2));
        g.addEdge(edge(v2, v0));
        g.addEdge(edge(v1, v3));
        g.addEdge(edge(v3, v4));

        TarjanScc<TestVertex> computer = new TarjanScc<>(g);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());

        assertEquals(Set.of(Set.of(v0, v1, v2), Set.of(v3), Set.of(v4)), components);
    }

    @Test
    void testTarjanScc2() {
        /*
         * +--------------+  +------+
         * |              |  |      |
         * |  V0----->V1------->V3  |
         * |   ^      /   |  |   |  |
         * |   |     /    |  +---|--+
         * |  V2<----     |      |
         * |              |  +---|---------+
         * +--------------+  |   V         |
         *                   |  V4<--->V5  |
         *                   |             |
         *                   +-------------+
         */
        var g = graphSupplier.get();
        var v0 = vertexSupplier.get();
        var v1 = vertexSupplier.get();
        var v2 = vertexSupplier.get();
        var v3 = vertexSupplier.get();
        var v4 = vertexSupplier.get();
        var v5 = vertexSupplier.get();
        g.addEdge(edge(v0, v1));
        g.addEdge(edge(v1, v2));
        g.addEdge(edge(v2, v0));
        g.addEdge(edge(v1, v3));
        g.addEdge(edge(v3, v4));
        g.addEdge(edge(v4, v5));
        g.addEdge(edge(v5, v4));

        TarjanScc<TestVertex> computer = new TarjanScc<>(g);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());

        assertEquals(Set.of(Set.of(v0, v1, v2), Set.of(v3), Set.of(v4, v5)), components);
    }
}
