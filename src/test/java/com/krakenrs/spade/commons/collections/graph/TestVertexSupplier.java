package com.krakenrs.spade.commons.collections.graph;

import java.util.function.Supplier;

public class TestVertexSupplier implements Supplier<TestVertex> {
    private int counter;

    @Override
    public TestVertex get() {
        return new TestVertex(counter++);
    }

    public void reset() {
        counter = 0;
    }
}
