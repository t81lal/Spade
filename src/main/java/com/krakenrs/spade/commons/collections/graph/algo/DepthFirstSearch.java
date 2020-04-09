package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;

import java.util.*;

public class DepthFirstSearch<V extends Vertex> {
    private final Digraph<V, ? extends Edge<V>> graph;
    private final Map<V, VertexColour> colours;
    private final Map<V, V> parents;
    private final List<V> postOrder;
    private final List<V> preOrder;

    public DepthFirstSearch(Digraph<V, ? extends Edge<V>> graph) {
        this.graph = graph;
        this.colours = new HashMap<>();
        this.parents = new HashMap<>();
        this.postOrder = new ArrayList<>();
        this.preOrder = new ArrayList<>();
    }

    public void run(V vertex) {
        colours.put(vertex, VertexColour.GREY);
        preOrder.add(vertex);

        for (Edge<V> edge : graph.getEdges(vertex)) {
            V destination = edge.getDestination();

            if (colours.getOrDefault(destination, VertexColour.WHITE) == VertexColour.WHITE) {
                parents.put(destination, vertex);
                run(destination);
            }
        }

        postOrder.add(vertex);
        colours.put(vertex, VertexColour.BLACK);
    }

    public Optional<V> getParent(V vertex) {
        return Optional.ofNullable(parents.get(vertex));
    }

    public List<V> getPostOrder() {
        return postOrder;
    }

    public List<V> getPreOrder() {
        return preOrder;
    }

    /**
     * Represents the state of a vertex.
     */
    public enum VertexColour {
        /**
         * The vertex has not yet been visited.
         */
        WHITE,

        /**
         * The vertex has been visited, but all of its successors have not yet been visited.
         */
        GREY,

        /**
         * The vertex and all of its successors have been visited.
         */
        BLACK,
    }
}
