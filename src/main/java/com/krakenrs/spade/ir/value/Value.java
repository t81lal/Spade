package com.krakenrs.spade.ir.value;

public interface Value {

    public static enum Kind {
        LOCAL, CONST
    }

    Kind kind();

    public static class AbstractValue implements Value {
        private final Kind kind;

        public AbstractValue(Kind kind) {
            this.kind = kind;
        }

        @Override
        public Kind kind() {
            return kind;
        }
    }
}
