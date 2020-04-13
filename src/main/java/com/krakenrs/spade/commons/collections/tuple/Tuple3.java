package com.krakenrs.spade.commons.collections.tuple;

import java.util.Objects;

public abstract class Tuple3<A, B, C> {

    protected A a;
    protected B b;
    protected C c;

    public Tuple3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
        return Objects.equals(a, other.a) && Objects.equals(b, other.b) && Objects.equals(c, other.c);
    }

    public static class MutableTuple3<A, B, C> extends Tuple3<A, B, C> {
        public MutableTuple3(A a, B b, C c) {
            super(a, b, c);
        }

        public void setA(A a) {
            this.a = a;
        }

        public void setB(B b) {
            this.b = b;
        }

        public void setC(C c) {
            this.c = c;
        }
    }

    public static class ImmutableTuple3<A, B, C> extends Tuple3<A, B, C> {
        public ImmutableTuple3(A a, B b, C c) {
            super(a, b, c);
        }
    }
}
