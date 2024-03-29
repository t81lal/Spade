package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;

import java.util.*;

public class DepthFirstSearch<V extends Vertex> {
    private final Digraph<V, ? extends Edge<V>> graph;
    private final Map<V, VertexColour> colours;
    private final Map<VertexColour, Set<Edge<V>>> edges;
    private final Map<V, V> parents;
    private final List<V> postOrder;
    private final List<V> preOrder;
    private final boolean reverse;

    public DepthFirstSearch(Digraph<V, ? extends Edge<V>> graph, boolean reverse) {
        this.graph = graph;
        this.colours = new HashMap<>();
        this.edges = Map.of(
                VertexColour.WHITE, new HashSet<>(),
                VertexColour.GREY, new HashSet<>(),
                VertexColour.BLACK, new HashSet<>()
        );
        this.parents = new HashMap<>();
        this.postOrder = new ArrayList<>();
        this.preOrder = new ArrayList<>();
        this.reverse = reverse;
    }

    public DepthFirstSearch(Digraph<V, ? extends Edge<V>> graph) {
        this(graph, false);
    }

    public void run(V vertex) {
        colours.put(vertex, VertexColour.GREY);
        preOrder.add(vertex);

        for (Edge<V> edge : reverse ? graph.getReverseEdges(vertex) : graph.getEdges(vertex)) {
            V destination = reverse ? edge.getSource() : edge.getDestination();

            VertexColour destinationColour = colours.getOrDefault(destination, VertexColour.WHITE);

            edges.get(destinationColour).add(edge);

            if (destinationColour == VertexColour.WHITE) {
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

    /**
     * Get a set of edges by their classification.
     *
     * @param edgeType Edge type to lookup
     * @return A set of edges.
     */
    public Set<Edge<V>> getEdges(EdgeType edgeType) {
        VertexColour colour;

        switch (edgeType) {
            case TREE:
                colour = VertexColour.WHITE;
                break;
            case BACK:
                colour = VertexColour.GREY;
                break;
            case CROSS_AND_FORWARD:
                colour = VertexColour.BLACK;
                break;
            default:
                throw new IllegalArgumentException();
        }

        final Set<Edge<V>> edgeSet = edges.get(colour);

        if (edgeSet == null) {
            throw new IllegalArgumentException();
        }

        return edgeSet;
    }

    /**
     * Get a list of vertices visited in this search in the order that they were <i>last</i> visited by the algorithm.
     *
     * @return An <b>unmodifiable</b> list of vertices
     */
    public List<V> getPostOrder() {
        return Collections.unmodifiableList(postOrder);
    }

    /**
     * Get a list of the vertices visited in this search in the order that they were <i>first</i> visited by the
     * algorithm.
     *
     * @return An <b>unmodifiable</b> list of vertices
     */
    public List<V> getPreOrder() {
        return Collections.unmodifiableList(preOrder);
    }

    /**
     * Get a list of the vertices visited in this search in reverse postorder.
     *
     * @return An <b>unmodifiable</b> list of vertices
     */
    public List<V> getTopOrder() {
        List<V> copy = new ArrayList<>(postOrder);
        Collections.reverse(copy);
        return Collections.unmodifiableList(copy);
    }

    public boolean isReverse() {
        return reverse;
    }

    public enum EdgeType {
        TREE,
        BACK,
        CROSS_AND_FORWARD,
    }

    /**
     * Represents the state of a vertex.
     */
    private enum VertexColour {
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
