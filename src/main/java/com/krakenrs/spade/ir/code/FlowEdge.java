package com.krakenrs.spade.ir.code;

import java.util.Objects;

import com.krakenrs.spade.commons.collections.graph.Edge;

public abstract class FlowEdge extends Edge<CodeBlock> {
    public enum Kind {
        CONDITIONAL, UNCONDITIONAL, IMMEDIATE, SWITCHCASE, DEFAULTCASE, EXCEPTION;
    }

    private final Kind kind;

    private FlowEdge(CodeBlock src, CodeBlock dst, Kind kind) {
        super(src, dst);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlowEdge other = (FlowEdge) obj;
        return Objects.equals(kind, other.kind);
    }

    public abstract FlowEdge create(CodeBlock src, CodeBlock dst);

    @Override
    public String toString() {
        return "edge: " + getSource().id() + " -> " + getDestination().id() + " (" + kind().name().toLowerCase()
                + ")";
    }

    public static class ImmediateEdge extends FlowEdge {
        public ImmediateEdge(CodeBlock src, CodeBlock dst) {
            super(src, dst, Kind.IMMEDIATE);
        }

        @Override
        public ImmediateEdge create(CodeBlock src, CodeBlock dst) {
            return new ImmediateEdge(src, dst);
        }
    }

    public static class JumpEdge extends FlowEdge {
        public JumpEdge(CodeBlock src, CodeBlock dst, Kind kind) {
            super(src, dst, kind);
        }

        @Override
        public JumpEdge create(CodeBlock src, CodeBlock dst) {
            return new JumpEdge(src, dst, kind());
        }
    }

    public static class SwitchEdge extends FlowEdge {
        private final int key;

        public SwitchEdge(CodeBlock src, CodeBlock dst, int key) {
            super(src, dst, Kind.SWITCHCASE);
            this.key = key;
        }

        public int key() {
            return key;
        }

        @Override
        public String toString() {
            return super.toString() + " (" + key + ")";
        }

        @Override
        public SwitchEdge create(CodeBlock src, CodeBlock dst) {
            return new SwitchEdge(src, dst, key());
        }
    }

    public static class DefaultEdge extends FlowEdge {
        public DefaultEdge(CodeBlock src, CodeBlock dst) {
            super(src, dst, Kind.DEFAULTCASE);
        }

        @Override
        public DefaultEdge create(CodeBlock src, CodeBlock dst) {
            return new DefaultEdge(src, dst);
        }

        @Override
        public String toString() {
            return super.toString() + " (default)";
        }
    }

    public static class ExceptionEdge extends FlowEdge {
        private final ExceptionRange range;

        public ExceptionEdge(CodeBlock src, CodeBlock dst, ExceptionRange range) {
            super(src, dst, Kind.EXCEPTION);
            this.range = range;
            if (!dst.equals(range.handler())) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public ExceptionEdge create(CodeBlock src, CodeBlock dst) {
            return new ExceptionEdge(src, dst, range());
        }

        public ExceptionRange range() {
            return range;
        }

        @Override
        public String toString() {
            return super.toString() + " (exception=" + range.catchTypes() + ")";
        }
    }
}
