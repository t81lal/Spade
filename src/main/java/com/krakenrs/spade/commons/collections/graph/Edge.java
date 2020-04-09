package com.krakenrs.spade.commons.collections.graph;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents an edge in a directed graph.
 *
 * @param <V> Vertex type
 */
public class Edge<V> {
    private final V source;
    private final V destination;

    /**
     * Create a new edge.
     *
     * @param source      The source vertex. Must not be null
     * @param destination The destination vertex. Must not be null
     */
    public Edge(V source, V destination) {
        this.source = requireNonNull(source);
        this.destination = requireNonNull(destination);
    }

    /**
     * Get the source vertex
     *
     * @return The source vertex. Will never be null
     */
    public V getSource() {
        return source;
    }

    /**
     * Get the destination vertex.
     *
     * @return The destination vertex. Will never be null
     */
    public V getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(source, edge.source) &&
                Objects.equals(destination, edge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
