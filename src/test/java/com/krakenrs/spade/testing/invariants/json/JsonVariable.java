package com.krakenrs.spade.testing.invariants.json;

import java.util.Objects;

public class JsonVariable implements JsonValue {

    private String alias;

    public JsonVariable() {
    }

    public JsonVariable(String alias) {
        checkValid(alias);
        this.alias = alias;
    }

    public JsonVariable setAlias(String alias) {
        checkValid(alias);
        this.alias = alias;
        return this;
    }

    private void checkValid(String alias) {
        for (char c : alias.toCharArray()) {
            if (!Character.isAlphabetic(c)) {
                throw new IllegalArgumentException(alias);
            }
        }
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public Kind kind() {
        return Kind.VARIABLE;
    }

    @Override
    public JsonValue copy() {
        return new JsonVariable(alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonVariable) {
            return Objects.equals(alias, ((JsonVariable) o).alias);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return alias;
    }
}
