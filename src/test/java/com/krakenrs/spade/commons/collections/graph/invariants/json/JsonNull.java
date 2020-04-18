package com.krakenrs.spade.commons.collections.graph.invariants.json;

public class JsonNull implements JsonValue {

    @Override
    public Kind kind() {
        return Kind.NULL;
    }

    @Override
    public String toString() {
        return "JsonNull";
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JsonNull;
    }

    @Override
    public JsonNull copy() {
        return this;
    }
}
