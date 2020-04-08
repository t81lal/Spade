package com.krakenrs.spade.commons.collections.graph;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DigraphTest {
    @Test
    void testAddVertex() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);

        assertFalse(g.containsVertex(v0));
        assertTrue(g.addVertex(v0));
        assertFalse(g.addVertex(v0));
        assertTrue(g.containsVertex(v0));
        assertEquals(Set.of(v0), g.getVertices());
    }

    @Test
    void testAddVertexInvalid() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        assertThrows(NullPointerException.class, () -> g.addVertex(null));
    }

    @Test
    void testAddEdge() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);
        final var v1 = new TestVertex(1);
        final var v0v1 = new Edge<>(v0, v1);

        assertFalse(g.containsVertex(v0));
        assertFalse(g.containsVertex(v1));
        assertFalse(g.containsEdge(v0v1));

        g.addEdge(v0v1);

        assertTrue(g.containsVertex(v0));
        assertTrue(g.containsVertex(v1));
        assertTrue(g.containsEdge(v0v1));
        assertEquals(Set.of(v0v1), g.getEdges(v0));
        assertEquals(Set.of(v0v1), g.getReverseEdges(v1));
    }

    @Test
    void testAddEdgeInvalid() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        assertThrows(NullPointerException.class, () -> g.addEdge(null));
    }

    @Test
    void testGetEdgesInvalid() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);
        assertThrows(NullPointerException.class, () -> g.getEdges(null));
        assertThrows(NullPointerException.class, () -> g.getReverseEdges(null));
        assertThrows(IllegalArgumentException.class, () -> g.getEdges(v0));
        assertThrows(IllegalArgumentException.class, () -> g.getReverseEdges(v0));
    }

    @Test
    void testRemoveVertex() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);
        final var v1 = new TestVertex(1);
        final var v0v1 = new Edge<>(v0, v1);

        g.addEdge(v0v1);

        g.removeVertex(v1);

        assertFalse(g.containsVertex(v1));
        assertFalse(g.containsEdge(v0v1));
        assertEquals(Set.of(), g.getEdges(v0));
    }

    @Test
    void testRemoveEdge() {
        final var g = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);
        final var v1 = new TestVertex(1);
        final var v0v1 = new Edge<>(v0, v1);

        g.addEdge(v0v1);

        g.removeEdge(v0v1);

        assertFalse(g.containsEdge(v0v1));
        assertEquals(Set.of(), g.getEdges(v0));
        assertEquals(Set.of(), g.getReverseEdges(v1));
    }

    @Test
    void testEquals() {
        final var g0 = new Digraph<TestVertex, Edge<TestVertex>>();
        final var g1 = new Digraph<TestVertex, Edge<TestVertex>>();
        final var v0 = new TestVertex(0);
        final var v1 = new TestVertex(1);
        final var v0v1 = new Edge<>(v0, v1);

        assertTrue(g0.equals(g1));
        assertTrue(g1.equals(g0));

        g0.addEdge(v0v1);

        assertFalse(g0.equals(g1));
        assertFalse(g1.equals(g0));

        g1.addEdge(v0v1);

        assertTrue(g0.equals(g1));
        assertTrue(g1.equals(g0));
    }
}
