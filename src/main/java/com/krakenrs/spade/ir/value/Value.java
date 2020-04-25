package com.krakenrs.spade.ir.value;

public interface Value {

    enum Kind {
        LOCAL, CONST
    }

    Kind kind();

    class AbstractValue implements Value {
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
