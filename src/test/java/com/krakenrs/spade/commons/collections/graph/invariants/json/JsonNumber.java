package com.krakenrs.spade.commons.collections.graph.invariants.json;

import java.util.Objects;

public class JsonNumber implements JsonValue {

    private Number number;

    public JsonNumber() {
    }

    public JsonNumber(int n) {
        setValue(n);
    }

    public JsonNumber(float f) {
        setValue(f);
    }

    public JsonNumber setValue(int n) {
        this.number = Integer.valueOf(n);
        return this;
    }

    public JsonNumber setValue(float f) {
        this.number = Float.valueOf(f);
        return this;
    }

    public int asInt() {
        return this.number.intValue();
    }

    public float asFloat() {
        return this.number.floatValue();
    }

    public Number getValue() {
        return number;
    }

    @Override
    public Kind kind() {
        return Kind.NUMBER;
    }

    @Override
    public String toString() {
        return this.number.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonNumber) {
            return Objects.equals(number, ((JsonNumber) o).number);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 43 * Objects.hashCode(number);
    }

    @Override
    public JsonNumber copy() {
        JsonNumber copy = new JsonNumber();
        copy.number = number;
        return copy;
    }
}
