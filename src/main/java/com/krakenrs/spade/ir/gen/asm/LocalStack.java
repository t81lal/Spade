package com.krakenrs.spade.ir.gen.asm;

import java.util.Arrays;
import java.util.Objects;

import com.krakenrs.spade.commons.collections.tuple.Tuple2.ImmutableTuple2;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class LocalStack {

    private TypedLocal[] stack;
    private int size;

    public LocalStack() {
        this(8 * 8);
    }

    public LocalStack(int capacity) {
        capacity = Math.max(capacity, 1);
        stack = new TypedLocal[capacity];
        size = 0;
    }

    private void expand() {
        TypedLocal[] s = new TypedLocal[size * 2];
        System.arraycopy(stack, 0, s, 0, size);
        stack = s;
    }

    public void push(TypedLocal e) {
        int i = size++;
        if (stack.length == size) {
            expand();
        }
        stack[i] = e;
    }

    public TypedLocal peek() {
        int index = size - 1;
        if (index < 0) {
            throw new UnsupportedOperationException("Cannot peek on empty stack");
        }
        return stack[index];
    }

    public TypedLocal peek(int d) {
        if (d < 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        int index = size - d - 1;
        if (index < 0) {
            throw new UnsupportedOperationException("Cannot peek on empty stack");
        }
        return stack[index];
    }

    public TypedLocal pop() {
        if (size == 0) {
            throw new UnsupportedOperationException("Cannot pop from empty stack");
        }
        TypedLocal e = stack[--size];
        stack[size] = null;
        return e;
    }

    public TypedLocal getAt(int i) {
        if(i < 0) {
            throw new IllegalArgumentException("Index must be positive");
        } else if(i >= size) {
            throw new IllegalArgumentException("Index falls off the end of the stack");
        }
        return stack[i];
    }

    public void copyInto(LocalStack other) {
        TypedLocal[] news = new TypedLocal[capacity()];
        System.arraycopy(stack, 0, news, 0, capacity());
        other.stack = news;
        other.size = size;
    }

    public LocalStack copy() {
        LocalStack stack = new LocalStack(size());
        copyInto(stack);
        return stack;
    }

    public void assertHeights(int[] heights) {
        if (heights.length > size()) {
            throw new IllegalArgumentException(String.format("hlen=%d, size=%d", heights.length, size()));
        } else {
            for (int i = 0; i < heights.length; i++) {
                TypedLocal e = peek(i);
                if (e.getB().getSize() != heights[i]) {
                    throw new IllegalStateException(String.format("item at %d, len=%d, expected=%d, expr:%s", i,
                            e.getB().getSize(), heights[i], e));
                }
            }
        }
    }

    public void clear() {
        for (int i = size - 1; i >= 0; i--) {
            stack[i] = null;
        }
        size = 0;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return stack.length;
    }

    public int height() {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            count += peek(i).getB().getSize();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("top->btm[");
        for (int i = size() - 1; i >= 0; i--) {
            TypedLocal n = stack[i];
            if (n != null) {
                sb.append(n.getA());
                sb.append(":").append(n.getB());
                if (i != 0 && stack[i - 1] != null) {
                    sb.append(", ");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalStack) {
            LocalStack other = (LocalStack) o;
            return other.size == size && Arrays.equals(other.stack, stack);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, stack);
    }

    public static class TypedLocal extends ImmutableTuple2<Local, ValueType> {
        public TypedLocal(Local fst, ValueType snd) {
            super(fst, snd);
        }
    }
}
