package com.krakenrs.spade.commons.collections.tuple;

import java.util.Objects;

public abstract class Tuple2<A, B> {
    protected A a;
    protected B b;

    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tuple2<?, ?> Tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(a, Tuple2.a) && Objects.equals(b, Tuple2.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    public static class MutableTuple2<A, B> extends Tuple2<A, B> {
        public MutableTuple2() {
            super(null, null);
        }

        public MutableTuple2(A a, B b) {
            super(a, b);
        }

        public void setA(A a) {
            this.a = a;
        }

        public void setB(B b) {
            this.b = b;
        }
    }

    public static class ImmutableTuple2<A, B> extends Tuple2<A, B> {
        public ImmutableTuple2(A a, B b) {
            super(a, b);
        }
    }
}
