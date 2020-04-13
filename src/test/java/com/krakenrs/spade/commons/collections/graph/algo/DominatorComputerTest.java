package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.TestVertex;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DominatorComputerTest {
    final Supplier<Digraph<TestVertex, Edge<TestVertex>>> graphSupplier = Digraph::new;

    static Edge<TestVertex> edge(int v1, int v2) {
        return new Edge<>(new TestVertex(v1), new TestVertex(v2));
    }

    @Test
    void testGetDominatorTree1() {
        // https://en.wikipedia.org/wiki/File:Dominator_control_flow_graph.svg
        var g = graphSupplier.get();
        g.addEdge(edge(1, 2));
        g.addEdge(edge(2, 3));
        g.addEdge(edge(2, 4));
        g.addEdge(edge(2, 6));
        g.addEdge(edge(3, 5));
        g.addEdge(edge(4, 5));
        g.addEdge(edge(5, 2));

        // https://en.wikipedia.org/wiki/File:Dominator_tree.svg
        var expected = new DominatorComputer.DominatorTree<>(new TestVertex(1));
        expected.addEdge(edge(1, 2));
        expected.addEdge(edge(2, 3));
        expected.addEdge(edge(2, 4));
        expected.addEdge(edge(2, 5));
        expected.addEdge(edge(2, 6));

        var computer = new DominatorComputer<>(g, new TestVertex(1), false);
        computer.run();
        var actual = computer.getDominatorTree();

        assertEquals(expected, actual);
    }

    @Test
    void testGetDominatorTree3() {
        final int A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9, J = 10, K = 11, L = 12, R = 0;

        var g = graphSupplier.get();

        // LT79 Fig. 1
        g.addEdge(edge(A, D));
        g.addEdge(edge(B, A));
        g.addEdge(edge(B, D));
        g.addEdge(edge(B, E));
        g.addEdge(edge(C, F));
        g.addEdge(edge(C, G));
        g.addEdge(edge(D, L));
        g.addEdge(edge(E, H));
        g.addEdge(edge(F, I));
        g.addEdge(edge(G, I));
        g.addEdge(edge(G, J));
        g.addEdge(edge(H, E));
        g.addEdge(edge(H, K));
        g.addEdge(edge(I, K));
        g.addEdge(edge(J, I));
        g.addEdge(edge(K, I));
        g.addEdge(edge(K, R));
        g.addEdge(edge(L, H));
        g.addEdge(edge(R, A));
        g.addEdge(edge(R, B));
        g.addEdge(edge(R, C));

        // LT79 Fig. 2
        var expected = new DominatorComputer.DominatorTree<>(new TestVertex(R));
        expected.addEdge(edge(R, I));
        expected.addEdge(edge(R, K));
        expected.addEdge(edge(R, C));
        expected.addEdge(edge(R, H));
        expected.addEdge(edge(R, E));
        expected.addEdge(edge(R, A));
        expected.addEdge(edge(R, D));
        expected.addEdge(edge(R, B));
        expected.addEdge(edge(C, F));
        expected.addEdge(edge(C, G));
        expected.addEdge(edge(G, J));
        expected.addEdge(edge(D, L));

        var computer = new DominatorComputer<>(g, new TestVertex(R), false);
        computer.run();
        var actual = computer.getDominatorTree();

        assertEquals(expected, actual);
    }

    @Test
    void testGetDominatorTree2() {
        // https://www.boost.org/doc/libs/1_55_0/libs/graph/doc/lengauer_tarjan_dominator.htm
        var g = graphSupplier.get();
        g.addEdge(edge(0, 1));
        g.addEdge(edge(1, 2));
        g.addEdge(edge(1, 3));
        g.addEdge(edge(2, 7));
        g.addEdge(edge(3, 4));
        g.addEdge(edge(4, 5));
        g.addEdge(edge(4, 6));
        g.addEdge(edge(5, 7));
        g.addEdge(edge(6, 4));

        var expected = new DominatorComputer.DominatorTree<>(new TestVertex(0));
        expected.addEdge(edge(0, 1));
        expected.addEdge(edge(1, 2));
        expected.addEdge(edge(1, 3));
        expected.addEdge(edge(1, 7));
        expected.addEdge(edge(3, 4));
        expected.addEdge(edge(4, 5));
        expected.addEdge(edge(4, 6));

        var computer = new DominatorComputer<>(g, new TestVertex(0), false);
        computer.run();
        var actual = computer.getDominatorTree();

        assertEquals(expected, actual);
    }
}
