package com.krakenrs.spade.commons.collections.graph.invariants;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.commons.collections.graph.invariants.json.JsonObject;

public class GraphAssertionChecker<V extends Vertex, E extends Edge<V>> {
    public enum PropTime {
        PRE, POST
    }

    private final Map<PropTime, Map<V, JsonObject>> vertexProps;
    private final Map<PropTime, Map<E, JsonObject>> edgeProps;

    private final Set<V> vertices;
    private final Set<E> edges;

    public GraphAssertionChecker(Map<PropTime, Map<V, JsonObject>> vertexProps,
            Map<PropTime, Map<E, JsonObject>> edgeProps) {
        this.vertexProps = vertexProps;
        this.edgeProps = edgeProps;

        this.vertices = getAllVertices();
        this.edges = getAllEdges();
    }

    public GraphAssertionChecker(Map<PropTime, Map<V, JsonObject>> vertexProps,
            Map<PropTime, Map<E, JsonObject>> edgeProps, Set<V> vertices, Set<E> edges) {
        this.vertexProps = vertexProps;
        this.edgeProps = edgeProps;
        this.vertices = vertices;
        this.edges = edges;
    }

    private Set<V> getAllVertices() {
        return vertexProps.values().stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
    }

    private Set<E> getAllEdges() {
        return edgeProps.values().stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
    }

    public Digraph<V, E> createGraph() {
        Digraph<V, E> graph = new Digraph<>();

        for (V v : vertices) {
            graph.addVertex(v);
        }
        
        for (E e : edges) {
            graph.addEdge(e);
        }

        return graph;
    }

    public void verify(PropTime time, Digraph<V, E> graph) {
        verify(time, graph, true);
    }

    public List<String> verify(PropTime time, Digraph<V, E> graph, boolean junitFail) {
        AssertionChecker checker = new AssertionChecker();
        checker.verify(vertexProps.get(time), graph.getVertices());
        checker.verify(edgeProps.get(time), graph.getEdges());

        if (checker.errorMessages.size() > 0) {
            String message = String.join(System.lineSeparator(), checker.errorMessages);
            fail(message);
        }

        return checker.errorMessages;
    }
}
