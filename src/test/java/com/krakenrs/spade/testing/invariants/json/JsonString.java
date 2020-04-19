package com.krakenrs.spade.testing.invariants.json;

import java.util.Objects;

public class JsonString implements JsonValue {

    private String value;

    public JsonString() {
    }

    public JsonString(String value) {
        this.value = value;
    }

    public JsonString setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Kind kind() {
        return Kind.STRING;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return 47 * Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonString) {
            return Objects.equals(value, ((JsonString) o).value);
        } else {
            return false;
        }
    }

    @Override
    public JsonString copy() {
        return new JsonString(value);
    }
}
