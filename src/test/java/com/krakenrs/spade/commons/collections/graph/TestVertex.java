package com.krakenrs.spade.commons.collections.graph;

import java.util.Objects;

public class TestVertex implements Vertex {
    private final int id;

    public TestVertex(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestVertex that = (TestVertex) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
