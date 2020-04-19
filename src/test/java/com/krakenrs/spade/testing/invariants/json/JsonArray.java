package com.krakenrs.spade.testing.invariants.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class JsonArray implements JsonValue, Iterable<JsonValue> {

    private final List<JsonValue> values;

    public JsonArray(List<JsonValue> values) {
        this.values = values;
    }

    public JsonArray() {
        values = new ArrayList<>();
    }

    public JsonArray add(int value) {
        return add(new JsonNumber(value));
    }

    public JsonArray add(float value) {
        return add(new JsonNumber(value));
    }

    public JsonArray add(String value) {
        return add(new JsonString(value));
    }

    public JsonArray add(JsonValue value) {
        this.values.add(value);
        return this;
    }

    public JsonValue get(int i) {
        return values.get(i);
    }

    public int size() {
        return values.size();
    }

    public List<JsonValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Kind kind() {
        return Kind.ARRAY;
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return values.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonArray) {
            return Objects.equals(values, ((JsonArray) o).values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 37 * Objects.hashCode(values);
    }

    @Override
    public JsonArray copy() {
        JsonArray copy = new JsonArray();
        for (JsonValue v : values) {
            copy.values.add(v.copy());
        }
        return copy;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
