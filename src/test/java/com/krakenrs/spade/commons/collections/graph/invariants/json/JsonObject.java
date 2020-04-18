package com.krakenrs.spade.commons.collections.graph.invariants.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class JsonObject implements JsonValue {

    private final Map<String, JsonValue> members = new LinkedHashMap<>();

    public JsonObject put(String key, String value) {
        return put(key, new JsonString(value));
    }

    public JsonObject put(String key, int value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, float value) {
        return put(key, new JsonNumber(value));
    }

    public JsonObject put(String key, JsonValue val) {
        this.members.put(key, val);
        return this;
    }

    public JsonValue get(String key) {
        return this.members.get(key);
    }

    public JsonValue select(String path) {
        String[] parts = path.split("\\.");
        JsonValue o = this;
        int idx = 0;
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            idx += p.length();
            if (o instanceof JsonObject) {
                o = ((JsonObject) o).get(p);
            } else {
                throw new IllegalStateException(String.format("Expected JsonObject along path at %s but was %s",
                        path.substring(0, idx), o.getClass()));
            }
        }
        return o;
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(this.members.keySet());
    }

    public boolean containsKey(String key) {
        return this.members.containsKey(key);
    }

    @Override
    public Kind kind() {
        return Kind.OBJECT;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonObject) {
            return Objects.equals(members, ((JsonObject) o).members);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * members.hashCode();
    }

    @Override
    public String toString() {
        return JsonFormatter.toString(this);
    }

    @Override
    public JsonObject copy() {
        JsonObject copy = new JsonObject();
        for (Entry<String, JsonValue> e : members.entrySet()) {
            copy.members.put(e.getKey(), e.getValue().copy());
        }
        return copy;
    }
}
