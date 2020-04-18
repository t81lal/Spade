package com.krakenrs.spade.commons.collections.graph;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Represents a directed graph.
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class Digraph<V extends Vertex, E extends Edge<V>> {
    private final Map<V, Set<E>> edges;
    private final Map<V, Set<E>> reverseEdges;

    /**
     * Create a new, empty, digraph.
     */
    public Digraph() {
        edges = new HashMap<>();
        reverseEdges = new HashMap<>();
    }

    public int size() {
        return edges.size();
    }

    /**
     * Get all of the vertices contained in this graph.
     *
     * @return An <b>unmodifiable</b> set of vertices
     */
    public Set<V> getVertices() {
        return Collections.unmodifiableSet(edges.keySet());
    }

    /**
     * Get all of the real edges contained in this graph (i.e. not including reverse edges).
     *
     * @return An <b>unmodifiable</b> set of edges
     */
    public Set<E> getEdges() {
        return edges.values().stream().flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Add a new vertex to the graph.
     *
     * @param vertex The vertex to add. Must not be null
     * @return True if the vertex was added to the graph. False if the vertex was already contained in the graph
     */
    public boolean addVertex(V vertex) {
        requireNonNull(vertex);

        if (containsVertex(vertex)) {
            return false;
        }

        edges.put(vertex, new HashSet<>());
        reverseEdges.put(vertex, new HashSet<>());
        return true;
    }

    /**
     * Remove a vertex from the graph.
     *
     * @param vertex The vertex to remove. Must not be null
     */
    public void removeVertex(V vertex) {
        requireNonNull(vertex);
        edges.remove(vertex).forEach(e -> reverseEdges.get(e.getDestination()).remove(e));
        reverseEdges.remove(vertex).forEach(e -> edges.get(e.getSource()).remove(e));
    }

    /**
     * Check if a vertex is contained in the graph.
     *
     * @param vertex Vertex to check. Must not be null
     * @return True if the vertex is contained in the graph. False otherwise
     */
    public boolean containsVertex(V vertex) {
        return edges.containsKey(requireNonNull(vertex));
    }

    /**
     * Get all of the edges leaving a vertex.
     *
     * @param vertex The vertex to lookup. Must not be null
     * @return An <b>unmodifiable</b> set of edges
     * @throws IllegalArgumentException When the vertex is not contained in the graph
     */
    public Set<E> getEdges(V vertex) {
        final Set<E> edgeSet = edges.get(requireNonNull(vertex));

        if (edgeSet == null) {
            throw new IllegalArgumentException("Vertex not contained in graph");
        }

        return Collections.unmodifiableSet(edgeSet);
    }

    /**
     * Get all of the edges entering a vertex.
     *
     * @param vertex The vertex to lookup. Must not be null
     * @return An <b>unmodifiable</b> set of edges
     * @throws IllegalArgumentException When the vertex is not contained in the graph
     */
    public Set<E> getReverseEdges(V vertex) {
        final Set<E> edgeSet = reverseEdges.get(requireNonNull(vertex));

        if (edgeSet == null) {
            throw new IllegalArgumentException("Vertex not contained in graph");
        }

        return Collections.unmodifiableSet(edgeSet);
    }

    /**
     * Add an edge to the graph.
     * <p>
     * If the vertices connected by this edge are not already contained in the graph, they will be added.
     *
     * @param edge The edge to add. Must not be null
     * @return True if the edge was added to the graph. False if the edge was already contained in the graph
     */
    public boolean addEdge(E edge) {
        requireNonNull(edge);

        final V source = edge.getSource();
        final V destination = edge.getDestination();

        addVertex(source);
        addVertex(destination);

        final boolean added = edges.get(source).add(edge);
        reverseEdges.get(destination).add(edge);

        return added;
    }

    /**
     * Remove an edge from the graph.
     *
     * @param edge The edge to remove. Must not be null
     */
    public void removeEdge(E edge) {
        requireNonNull(edge);

        final V source = edge.getSource();
        final V destination = edge.getDestination();

        if (containsVertex(source)) {
            edges.get(source).remove(edge);
        }

        if (containsVertex(destination)) {
            reverseEdges.get(destination).remove(edge);
        }
    }

    /**
     * Check if an edge is contained in the graph.
     *
     * @param edge Edge to check. Must not be null
     * @return True if the edge is contained in the graph. False otherwise
     */
    public boolean containsEdge(E edge) {
        requireNonNull(edge);
        return containsVertex(edge.getSource()) && edges.get(edge.getSource()).contains(edge);
    }

    /**
     * Check if a reverse edge is contained in the graph.
     *
     * @param edge Edge to check. Must not be null
     * @return True if the reverse edge is contained in the graph. False otherwise
     */
    public boolean containsReverseEdge(E edge) {
        requireNonNull(edge);
        return containsVertex(edge.getDestination()) && reverseEdges.get(edge.getDestination()).contains(edge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Digraph<?, ?> digraph = (Digraph<?, ?>) o;
        return Objects.equals(edges, digraph.edges) &&
                Objects.equals(reverseEdges, digraph.reverseEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges, reverseEdges);
    }
}
