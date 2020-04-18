package com.krakenrs.spade.commons.collections.graph.invariants.json;

public class JsonBool implements JsonValue {

    private boolean value;

    public JsonBool() {
    }

    public JsonBool(boolean value) {
        this.value = value;
    }

    public JsonBool setValue(boolean value) {
        this.value = value;
        return this;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Kind kind() {
        return Kind.BOOL;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonBool) {
            return value == ((JsonBool) o).value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 41 * Boolean.hashCode(value);
    }

    @Override
    public JsonBool copy() {
        return new JsonBool(value);
    }
}
