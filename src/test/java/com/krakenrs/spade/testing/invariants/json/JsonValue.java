package com.krakenrs.spade.testing.invariants.json;

public interface JsonValue {
    enum Kind {
        OBJECT, ARRAY, STRING, NUMBER, BOOL, NULL
    }

    Kind kind();

    JsonValue copy();
}
