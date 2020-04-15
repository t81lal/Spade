package com.krakenrs.spade.ir.code;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.ir.type.MethodType;

public class ControlFlowGraph extends Digraph<CodeBlock, FlowEdge> {

    private final MethodType methodType;
    private final boolean isStatic;
    private final CodeBlock entryBlock;

    public ControlFlowGraph(MethodType methodType, boolean isStatic) {
        this.methodType = methodType;
        this.isStatic = isStatic;

        this.entryBlock = makeBlock();
    }

    public CodeBlock makeBlock() {
        return new CodeBlock(size());
    }

    public CodeBlock getTarget(CodeBlock block, FlowEdge.Kind kind) {
        List<FlowEdge> edges = getEdges(block, kind);
        if (edges.size() != 1) {
            throw new IllegalStateException();
        }
        return edges.get(0).getDestination();
    }

    public List<FlowEdge> getEdges(CodeBlock block, FlowEdge.Kind kind) {
        return getEdgeStream(block, kind).collect(Collectors.toList());
    }

    public Stream<FlowEdge> getEdgeStream(CodeBlock block, FlowEdge.Kind kind) {
        Objects.requireNonNull(block);
        Objects.requireNonNull(kind);

        return getEdges(block).stream().filter(e -> e.kind().equals(kind));
    }

    @Override
    public boolean addVertex(CodeBlock block) {
        boolean added = super.addVertex(block);
        if (added) {

        }
        return added;
    }
}