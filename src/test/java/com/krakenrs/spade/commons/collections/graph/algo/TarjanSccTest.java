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
import java.util.stream.Collectors;

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
    public void testScc0Nodes() {
        var g = graphSupplier.get();
        TarjanScc<TestVertex> computer = new TarjanScc<>(g);
        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        assertEquals(Set.of(), components);
    }

    @Test
    public void testScc0Edges() {
        var g = graphSupplier.get();
        Set<TestVertex> vs = new HashSet<>();
        for(int i=0; i < 10; i++) {
            TestVertex v = vertexSupplier.get();
            vs.add(v);
            g.addVertex(v);
        }
        TarjanScc<TestVertex> computer = new TarjanScc<>(g);
        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        Set<Set<TestVertex>> expected = vs.stream().map((v) -> Set.of(v)).collect(Collectors.toSet());
        assertEquals(expected, components);
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

    @Test
    void testTarjanScc3() {
        // https://upload.wikimedia.org/wikipedia/commons/5/5c/Scc.png
        var graph = graphSupplier.get();
        TestVertex a = vertexSupplier.get(),
            b = vertexSupplier.get(),
            c = vertexSupplier.get(),
            d = vertexSupplier.get(),
            e = vertexSupplier.get(),
            f = vertexSupplier.get(),
            g = vertexSupplier.get(),
            h = vertexSupplier.get();
        graph.addEdge(edge(a, b));
        graph.addEdge(edge(b, e));
        graph.addEdge(edge(b, f));
        graph.addEdge(edge(b, c));
        graph.addEdge(edge(e, a));
        graph.addEdge(edge(e, f));
        graph.addEdge(edge(f, g));
        graph.addEdge(edge(g, f));
        graph.addEdge(edge(c, g));
        graph.addEdge(edge(c, d));
        graph.addEdge(edge(d, c));
        graph.addEdge(edge(d, h));
        graph.addEdge(edge(h, d));
        graph.addEdge(edge(h, g));

        TarjanScc<TestVertex> computer = new TarjanScc<>(graph);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        assertEquals(
            Set.of(
                Set.of(a, b, e),
                Set.of(f, g),
                Set.of(c, d, h)
            ),
            components
        );
    }

    @Test
    void testTarjanScc4() {
        // http://rosalind.info/media/sccexample.png
        var graph = graphSupplier.get();
        TestVertex a = vertexSupplier.get(),
            b = vertexSupplier.get(),
            c = vertexSupplier.get(),
            d = vertexSupplier.get(),
            e = vertexSupplier.get(),
            f = vertexSupplier.get(),
            g = vertexSupplier.get(),
            h = vertexSupplier.get(),
            i = vertexSupplier.get(),
            j = vertexSupplier.get(),
            k = vertexSupplier.get(),
            l = vertexSupplier.get();
        graph.addEdge(edge(a, b));
        graph.addEdge(edge(b, c));
        graph.addEdge(edge(b, d));
        graph.addEdge(edge(b, e));
        graph.addEdge(edge(c, f));
        graph.addEdge(edge(e, b));
        graph.addEdge(edge(e, f));
        graph.addEdge(edge(e, g));
        graph.addEdge(edge(f, c));
        graph.addEdge(edge(f, h));
        graph.addEdge(edge(g, h));
        graph.addEdge(edge(g, j));
        graph.addEdge(edge(h, k));
        graph.addEdge(edge(i, g));
        graph.addEdge(edge(j, i));
        graph.addEdge(edge(k, l));
        graph.addEdge(edge(l, j));

        TarjanScc<TestVertex> computer = new TarjanScc<>(graph);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        assertEquals(
            Set.of(
                Set.of(a),
                Set.of(d),
                Set.of(b, e),
                Set.of(c, f),
                Set.of(g, h, i, j, k, l)
            ),
            components
        );
    }

    @Test
    void testSccCompletelyConnected() {
        // https://media.geeksforgeeks.org/wp-content/cdn-uploads/connectivity3.png
        var graph = graphSupplier.get();
        TestVertex v0 = vertexSupplier.get(),
            v1 = vertexSupplier.get(),
            v2 = vertexSupplier.get(),
            v3 = vertexSupplier.get(),
            v4 = vertexSupplier.get();
        graph.addEdge(edge(v0, v1));
        graph.addEdge(edge(v1, v2));
        graph.addEdge(edge(v2, v3));
        graph.addEdge(edge(v2, v4));
        graph.addEdge(edge(v4, v2));
        graph.addEdge(edge(v3, v0));

        TarjanScc<TestVertex> computer = new TarjanScc<>(graph);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        assertEquals(
            Set.of(
                Set.of(v0, v1, v2, v3, v4)
            ),
            components
        );
    }

    @Test
    void testTarjanScc5() {
        // https://runestone.academy/runestone/books/published/pythonds/_images/scc1.png
        var graph = graphSupplier.get();
        TestVertex a = vertexSupplier.get(),
            b = vertexSupplier.get(),
            c = vertexSupplier.get(),
            d = vertexSupplier.get(),
            e = vertexSupplier.get(),
            f = vertexSupplier.get(),
            g = vertexSupplier.get(),
            h = vertexSupplier.get(),
            i = vertexSupplier.get();
        graph.addEdge(edge(a, b));
        graph.addEdge(edge(b, e));
        graph.addEdge(edge(b, c));
        graph.addEdge(edge(c, c));
        graph.addEdge(edge(c, f));
        graph.addEdge(edge(d, g));
        graph.addEdge(edge(d, b));
        graph.addEdge(edge(e, d));
        graph.addEdge(edge(e, a));
        graph.addEdge(edge(f, h));
        graph.addEdge(edge(g, e));
        graph.addEdge(edge(h, i));
        graph.addEdge(edge(i, f));

        TarjanScc<TestVertex> computer = new TarjanScc<>(graph);

        Set<Set<TestVertex>> components = new HashSet<>(computer.run());
        assertEquals(
            Set.of(
                Set.of(a, b, d, e, g),
                Set.of(c),
                Set.of(f, h, i)
            ),
            components
        );
    }
}
