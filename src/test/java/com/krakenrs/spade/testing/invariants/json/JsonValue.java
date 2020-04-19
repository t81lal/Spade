package com.krakenrs.spade.testing.invariants.json;

public interface JsonValue {
    enum Kind {
        OBJECT, ARRAY, VARIABLE, STRING, NUMBER, BOOL, NULL
    }

    Kind kind();

    JsonValue copy();
}
