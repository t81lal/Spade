package com.krakenrs.spade.ir.code;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.krakenrs.spade.commons.collections.graph.Digraph;
import com.krakenrs.spade.ir.type.MethodType;

public class ControlFlowGraph extends Digraph<CodeBlock, FlowEdge> {

    public static interface Factory {
        ControlFlowGraph create(MethodType methodType, boolean isStatic);
    }
    
    private final MethodType methodType;
    private final boolean isStatic;
    private final CodeBlock entryBlock;
    private final Set<ExceptionRange> ranges;
    
    private final CodeBlock.Factory codeBlockFactory;

    @Inject
    public ControlFlowGraph(CodeBlock.Factory codeBlockFactory, @Assisted MethodType methodType, @Assisted boolean isStatic) {
        this.codeBlockFactory = codeBlockFactory;
        this.methodType = methodType;
        this.isStatic = isStatic;
        this.ranges = new HashSet<>();

        this.entryBlock = makeBlock();
        addVertex(entryBlock);
    }

    public CodeBlock getEntryBlock() {
        return entryBlock;
    }

    public CodeBlock makeBlock() {
        return codeBlockFactory.create(size());
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

    public void addExceptionRange(ExceptionRange range) {
        Objects.requireNonNull(range);
        ranges.add(range);
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Set<ExceptionRange> getExceptionRanges() {
        return ranges;
    }
}
