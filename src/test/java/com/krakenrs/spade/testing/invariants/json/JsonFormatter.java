package com.krakenrs.spade.testing.invariants.json;

import java.util.Iterator;

public class JsonFormatter {

    public static String toString(JsonValue value) {
        return new JsonFormatter(value).format();
    }

    private final JsonValue root;
    private final TabbedStringBuilder sb;

    public JsonFormatter(JsonValue root) {
        this.root = root;
        this.sb = new TabbedStringBuilder();
    }

    public String format() {
        sb.clear();
        emitValue(root);
        return sb.toString();
    }

    private void emitValue(JsonValue value) {
        switch (value.kind()) {
            case OBJECT:
                emit((JsonObject) value);
                break;
            case ARRAY:
                emit((JsonArray) value);
                break;
            case VARIABLE:
                sb.append(((JsonVariable) value).getAlias());
                break;
            case NUMBER:
                sb.append(value.toString());
                break;
            case STRING:
            case BOOL:
                emitString(value.toString());
                break;
            case NULL:
                emitString("null");
                break;
            default:
                throw new UnsupportedOperationException("Don't know how to handle value of type " + value.kind());
        }
    }

    private void emit(JsonArray a) {
        sb.append('[').tab().appendNewLine();

        Iterator<JsonValue> it = a.iterator();
        while (it.hasNext()) {
            JsonValue v = it.next();

            emitValue(v);
            if (!it.hasNext()) {
                sb.untab();
            } else {
                sb.append(',');
            }
            sb.appendNewLine();
        }
        sb.append(']');
    }

    private void emit(JsonObject o) {
        sb.append('{').tab().appendNewLine();

        Iterator<String> keyIterator = o.keys().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            emitString(key);
            sb.append(" : ");
            emitValue(o.get(key));

            /* Untab before appending the new line so the next line is
             * not indented as far as the current one. */
            if (!keyIterator.hasNext()) {
                sb.untab();
            } else {
                sb.append(',');
            }
            sb.appendNewLine();
        }
        sb.append('}');
    }

    private void emitString(String s) {
        sb.append('"').append(escapeString(s)).append('"');
    }

    private String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    private static class TabbedStringBuilder {
        private final StringBuilder sb = new StringBuilder();
        private int tabCount;

        public TabbedStringBuilder clear() {
            sb.setLength(0);
            return this;
        }

        public TabbedStringBuilder append(String s) {
            for (char c : s.toCharArray()) {
                append(c);
            }
            return this;
        }

        public TabbedStringBuilder tab() {
            tabCount++;
            return this;
        }

        public TabbedStringBuilder untab() {
            if (tabCount == 0) {
                throw new UnsupportedOperationException("Cannot untab when tabCount=0");
            }
            tabCount--;
            return this;
        }

        public TabbedStringBuilder appendNewLine() {
            sb.append('\n');
            appendPrefix();
            return this;
        }

        public TabbedStringBuilder append(char c) {
            sb.append(c);
            if (c == '\n') {
                appendPrefix();
            }
            return this;
        }

        private void appendPrefix() {
            for (int i = 0; i < tabCount; i++) {
                sb.append("  ");
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
