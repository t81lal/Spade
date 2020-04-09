package com.krakenrs.spade.commons.collections.graph.algo;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;

import java.util.*;

/**
 * An algorithm to compute strongly connected components.
 */
public class TarjanScc<V extends Vertex> {
    private final Digraph<V, ? extends Edge<V>> graph;
    private final Map<V, Integer> indices;
    private final Map<V, Integer> lowlinks;
    private final Set<V> onStack;
    private final Deque<V> stack;
    private final List<Set<V>> components;
    private int counter;

    public TarjanScc(Digraph<V, ? extends Edge<V>> graph) {
        this.graph = graph;
        indices = new HashMap<>();
        lowlinks = new HashMap<>();
        onStack = new HashSet<>();
        stack = new ArrayDeque<>();
        components = new ArrayList<>();
        counter = 0;
    }

    public List<Set<V>> run() {
        for (V vertex : graph.getVertices()) {
            if (!indices.containsKey(vertex)) {
                run(vertex);
            }
        }

        return components;
    }

    private void run(V vertex) {
        int num = counter++;
        indices.put(vertex, num);
        lowlinks.put(vertex, num);
        stack.push(vertex);
        onStack.add(vertex);

        for (Edge<V> edge : graph.getEdges(vertex)) {
            if (!indices.containsKey(edge.getDestination())) {
                run(edge.getDestination());
                lowlinks.put(edge.getSource(), Math.min(lowlinks.get(edge.getSource()),
                        lowlinks.get(edge.getDestination())));
            } else if (onStack.contains(edge.getDestination())) {
                lowlinks.put(edge.getSource(), Math.min(lowlinks.get(edge.getSource()),
                        indices.get(edge.getDestination())));
            }
        }

        if (lowlinks.get(vertex).equals(indices.get(vertex))) {
            Set<V> component = new HashSet<>();
            V w;
            do {
                w = stack.pop();
                onStack.remove(w);
                component.add(w);
            } while (!vertex.equals(w));
            components.add(component);
        }
    }
}
