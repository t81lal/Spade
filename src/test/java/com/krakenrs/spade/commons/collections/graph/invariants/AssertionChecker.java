package com.krakenrs.spade.commons.collections.graph.invariants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.krakenrs.spade.commons.collections.graph.invariants.json.*;

public class AssertionChecker {
    abstract class AccessSelector {
        abstract Optional<Object> getValue(Object obj);
    }

    class FieldSelector extends AccessSelector {
        String fieldName;

        FieldSelector(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        Optional<Object> getValue(Object obj) {
            Field field = null;
            Class<?> clazz = obj.getClass();
            Exception reason = null;
            while (clazz != null && field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    // No problem, try going up the class hiearchy
                    reason = e;
                } catch (SecurityException e) {
                    field = null;
                    reason = e;
                    break;
                }
                clazz = clazz.getSuperclass();
            }

            if (field == null) {
                runtimeError(reason != null ? reason.getClass().getSimpleName() : "Unknown error");
                return null;
            }


            try {
                return Optional.ofNullable(field.get(obj));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                runtimeError("Couldn't get value");
                return null;
            }
        }

        @Override
        public String toString() {
            return fieldName;
        }
    }

    class ListSelector extends AccessSelector {
        int index;

        ListSelector(int index) {
            this.index = index;
        }

        @Override
        Optional<Object> getValue(Object obj) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (index < list.size()) {
                    return Optional.ofNullable(list.get(index));
                } else {

                    return null;
                }
            } else {
                runtimeError("Expected list");
                return null;
            }
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    final List<String> errorMessages = new ArrayList<>();
    final Deque<Object> objectPath = new LinkedList<>();
    final Deque<AccessSelector> accessPath = new LinkedList<>();

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    public <T> void verify(Map<T, JsonObject> expectedProps, Set<T> domain) {
        // No props for this entire class of object
        if (expectedProps == null)
            return;

        for (T obj : domain) {

            objectPath.clear();
            accessPath.clear();

            if (expectedProps.containsKey(obj)) {
                verifyFromSpec(obj, expectedProps.get(obj));
            }
            // Else: no props for this object
        }
    }

    private <T> void verifyFromSpec(T obj, JsonValue propSpec) {
        objectPath.push(obj);

        if (propSpec.kind().equals(JsonValue.Kind.OBJECT)) {
            JsonObject props = (JsonObject) propSpec;
            for (String fieldName : props.keys()) {
                accessPath.push(new FieldSelector(fieldName));
                verifyProp(props.get(fieldName));
                accessPath.pop();
            }
        } else if (propSpec.kind().equals(JsonValue.Kind.ARRAY)) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                JsonArray arr = (JsonArray) propSpec;

                if (list.size() != arr.size()) {
                    runtimeError(String.format("List size error: expected %d element(s) but got %d", arr.size(),
                            list.size()));
                } else {
                    for (int i = 0; i < arr.size(); i++) {
                        accessPath.push(new ListSelector(i));
                        verifyProp(arr.get(i));
                        accessPath.pop();
                    }
                }
            } else {
                runtimeError("Expected list");
            }
        } else {
            throw new IllegalArgumentException("Illegal spec: " + propSpec);
        }

        objectPath.pop();
    }

    <T> void verifyProp(JsonValue props) {
        final Object obj = objectPath.peek();
        Optional<Object> value = accessPath.peek().getValue(obj);
        if (value != null) {
            Object val = value.isPresent() ? value.get() : null;
            verifyProp(props, val);
        }
    }

    void verifyProp(JsonValue props, Object value) {
        switch (props.kind()) {
            case OBJECT:
                assertObjectDeepEquals((JsonObject) props, value);
                break;
            case ARRAY:
                verifyFromSpec(value, props);
                break;
            case STRING:
                assertEquals(((JsonString) props).getValue(), value);
                break;
            case NUMBER:
                assertEquals(((JsonNumber) props).getValue(), value);
                break;
            case BOOL:
                assertEquals(((JsonBool) props).getValue(), value);
                break;
            case NULL:
                assertEquals(null, value);
                break;
        }
    }

    void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            invariantError(expected, actual);
        }
    }

    void assertObjectDeepEquals(JsonObject expected, Object actual) {
        verifyFromSpec(actual, expected);
    }

    String accessPath() {
        StringBuilder sb = new StringBuilder();

        boolean donePrev = false;

        Iterator<AccessSelector> it = accessPath.descendingIterator();
        while (it.hasNext()) {
            AccessSelector sel = it.next();
            if (sel.getClass().equals(FieldSelector.class)) {
                if (donePrev) {
                    sb.append(".");
                }
            } else if (sel.getClass().equals(ListSelector.class)) {

            } else {
                throw new UnsupportedOperationException();
            }
            sb.append(sel.toString());
            donePrev = true;
        }

        return sb.toString();
    }

    Object currentObj() {
        return objectPath.getLast();
    }

    void runtimeError(String type) {
        String msg = String.format("%s when trying to access [%s].%s", type, currentObj(), accessPath());
        errorMessages.add(msg);
    }

    void invariantError(Object expected, Object actual) {
        String msg = String.format("Expected [%s].%s to be '%s' but was '%s'", currentObj(), accessPath(), expected,
                actual);
        errorMessages.add(msg);
    }
}
