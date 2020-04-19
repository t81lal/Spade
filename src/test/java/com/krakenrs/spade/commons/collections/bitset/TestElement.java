package com.krakenrs.spade.commons.collections.bitset;

public class TestElement {
    int index;
    int indexCalls;

    TestElement(int index) {
        this.index = index;
    }

    public int getIndex() {
        indexCalls++;
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TestElement) {
            return ((TestElement) o).index == index;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return index;
    }
}
