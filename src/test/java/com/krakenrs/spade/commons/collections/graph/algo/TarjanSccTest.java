package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import com.krakenrs.spade.commons.collections.graph.TestVertexSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TarjanSccTest {
    private TestVertexSupplier vertexSupplier = new TestVertexSupplier();
    private Supplier<Digraph<TestVertex, Edge<TestVertex>>> graphSupplier = Digraph::new;

    private static Edge<TestVertex> edge(int v1, int v2) {
        return edge(new TestVertex(v1), new TestVertex(v2));
    }

    private static Set<TestVertex> scc(int... vs) {
        Set<TestVertex> scc = new HashSet<>();
        for (int v : vs) {
            scc.add(new TestVertex(v));
        }
        return scc;
    }

    private static Edge<TestVertex> edge(TestVertex v1, TestVertex v2) {
        return new Edge<>(v1, v2);
    }

    @BeforeEach
    void setup() {
        vertexSupplier.reset();
    }

    static Stream<Arguments> stronglyConnectedComp1nts() {
        return Stream.of(
                Arguments.of(
                        // Empty.
                        Set.of(),
                        Set.of()
                ),
                Arguments.of(
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
                        Set.of(
                                edge(0, 1),
                                edge(1, 2),
                                edge(2, 0),
                                edge(1, 3),
                                edge(3, 4)
                        ),
                        Set.of(
                                scc(0, 1, 2),
                                scc(3),
                                scc(4)
                        )
                ),
                Arguments.of(
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
                        Set.of(
                                edge(0, 1),
                                edge(1, 2),
                                edge(2, 0),
                                edge(1, 3),
                                edge(3, 4),
                                edge(4, 5),
                                edge(5, 4)
                        ),
                        Set.of(
                                scc(0, 1, 2),
                                scc(3),
                                scc(4, 5)
                        )
                ),
                Arguments.of(
                        // https://upload.wikimedia.org/wikipedia/commons/5/5c/Scc.png
                        Set.of(
                                edge(0, 1),
                                edge(1, 4),
                                edge(1, 5),
                                edge(1, 2),
                                edge(4, 0),
                                edge(4, 5),
                                edge(5, 6),
                                edge(6, 5),
                                edge(2, 6),
                                edge(2, 3),
                                edge(3, 2),
                                edge(3, 7),
                                edge(7, 3),
                                edge(7, 6)
                        ),
                        Set.of(
                                scc(0, 1, 4),
                                scc(5, 6),
                                scc(2, 3, 7)
                        )
                ),
                Arguments.of(
                        // http://rosalind.info/media/sccexample.png
                        Set.of(
                                edge(0, 1),
                                edge(1, 2),
                                edge(1, 3),
                                edge(1, 4),
                                edge(2, 5),
                                edge(4, 1),
                                edge(4, 5),
                                edge(4, 6),
                                edge(5, 2),
                                edge(5, 7),
                                edge(6, 7),
                                edge(6, 9),
                                edge(7, 10),
                                edge(8, 6),
                                edge(9, 8),
                                edge(10, 11),
                                edge(11, 9)
                        ),
                        Set.of(
                                scc(0),
                                scc(3),
                                scc(1, 4),
                                scc(2, 5),
                                scc(6, 7, 8, 9, 10, 11)
                        )
                ),
                Arguments.of(
                        // https://media.geeksforgeeks.org/wp-con10t/cdn-uploads/connectivity3.png
                        Set.of(
                                edge(0, 1),
                                edge(1, 2),
                                edge(2, 3),
                                edge(2, 4),
                                edge(4, 2),
                                edge(3, 0)
                        ),
                        Set.of(
                                scc(0, 1, 2, 3, 4)
                        )
                ),
                Arguments.of(
                        // https://runest1.academy/runest1/books/published/pythonds/_images/scc1.png
                        Set.of(
                                edge(0, 1),
                                edge(1, 4),
                                edge(1, 2),
                                edge(2, 2),
                                edge(2, 5),
                                edge(3, 6),
                                edge(3, 1),
                                edge(4, 3),
                                edge(4, 0),
                                edge(5, 7),
                                edge(6, 4),
                                edge(7, 8),
                                edge(8, 5)
                        ),
                        Set.of(
                                scc(0, 1, 3, 4, 6),
                                scc(2),
                                scc(5, 7, 8)
                        )
                )
        );
    }

    @ParameterizedTest(name = "[{index}]")
    @MethodSource("stronglyConnectedComp1nts")
    void testCompute(Set<Edge<TestVertex>> edges, Set<Set<TestVertex>> expected) {
        var g = graphSupplier.get();
        edges.forEach(g::addEdge);

        var computer = new TarjanScc<>(g);
        var actual = new HashSet<>(computer.run());

        assertEquals(expected, actual);
    }

    @Test
    public void testScc0Edges() {
        var g = graphSupplier.get();
        Set<TestVertex> vs = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            var v = vertexSupplier.get();
            vs.add(v);
            g.addVertex(v);
        }
        TarjanScc<TestVertex> computer = new TarjanScc<>(g);
        Set<Set<TestVertex>> comp1nts = new HashSet<>(computer.run());
        Set<Set<TestVertex>> expected = vs.stream().map((v) -> Set.of(v)).collect(Collectors.toSet());
        assertEquals(expected, comp1nts);
    }
}
