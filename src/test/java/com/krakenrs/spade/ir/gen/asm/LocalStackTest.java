package com.krakenrs.spade.ir.gen.asm;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

import com.krakenrs.spade.ir.gen.asm.LocalStack.TypedLocal;
import com.krakenrs.spade.ir.type.PrimitiveType;
import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Local;

public class LocalStackTest {

    private static TypedLocal item(int size) {
        ValueType type;
        if(size == 2) {
            type = PrimitiveType.DOUBLE;
        } else {
            type = PrimitiveType.INT;
        }
        return new TypedLocal(new Local(1, true), type);
    }

    @Test
    public void testPush() {
        var stack = new LocalStack();

        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());
        stack.push(item(1));
        stack.push(item(1));
        stack.push(item(1));
        assertEquals(3, stack.size());
        assertFalse(stack.isEmpty());
    }

    @Test
    public void testPushDouble() {
        var stack = new LocalStack();
        assertEquals(0, stack.size());
        assertEquals(0, stack.height());
        stack.push(item(1));
        assertEquals(1, stack.size());
        assertEquals(1, stack.height());
        stack.push(item(2));
        assertEquals(2, stack.size());
        assertEquals(3, stack.height());
    }

    @Test
    public void testPushOverflowCapacity() {
        final int initialCapacity = 4;
        final int numItems = initialCapacity + 1;
        
        var stack = new LocalStack(initialCapacity);
        for (int i = 0; i < numItems; i++) {
            stack.push(item(1));
        }
        assertEquals(numItems, stack.size());
        assertTrue(stack.capacity() > numItems);
    }

    @Test
    public void testPeekArguments() {
        var stack = new LocalStack();
        assertThrows(UnsupportedOperationException.class, () -> stack.peek());
        assertThrows(IllegalArgumentException.class, () -> stack.peek(-1));
    }

    @Test
    public void testPeek() {
        var stack = new LocalStack();

        var top = item(1);
        var bottom = item(1);
        stack.push(bottom);
        stack.push(top);

        assertEquals(top, stack.peek());
        assertEquals(top, stack.peek(0));
        assertEquals(bottom, stack.peek(1));
        stack.pop();
        assertEquals(bottom, stack.peek());
        assertEquals(bottom, stack.peek(0));
        assertThrows(UnsupportedOperationException.class, () -> stack.peek(1));
    }

    @Test
    public void testPeekDouble() {
        var stack = new LocalStack();
        var top = item(2);
        var bottom = item(1);
        stack.push(bottom);
        stack.push(top);
        
        assertEquals(top, stack.peek(0));
        assertEquals(bottom, stack.peek(1));
        assertThrows(UnsupportedOperationException.class, () -> stack.peek(2));
    }

    @Test
    public void testPop() {
        var stack = new LocalStack();
        var items = new TypedLocal[5];
        for (int i = 0; i < items.length; i++) {
            items[i] = item(1);
            stack.push(items[i]);
        }
        assertEquals(items.length, stack.size());

        for (int i = 0; i < items.length; i++) {
            assertEquals(items.length - i, stack.size());
            stack.pop();
            assertEquals(items.length - i - 1, stack.size());
        }
    }

    @Test
    public void testPopDouble() {
        var stack = new LocalStack();
        var items = new TypedLocal[5];
        for (int i = 0; i < items.length; i++) {
            items[i] = item(2);
            stack.push(items[i]);
        }
        assertEquals(items.length, stack.size());
        assertEquals(2 * items.length, stack.height());

        for (int i = 0; i < items.length; i++) {
            assertEquals(items.length - i, stack.size());
            assertEquals(2 * (items.length - i), stack.height());
            stack.pop();
            assertEquals(items.length - i - 1, stack.size());
            assertEquals(2 * (items.length - i - 1), stack.height());
        }
    }

    @Test
    public void testPopUnderflow() {
        final int numElements = 3;
        var stack = new LocalStack();

        for (int i = 0; i < numElements; i++) {
            stack.push(item(1));
        }
        assertEquals(numElements, stack.size());
        for (int i = 0; i < numElements; i++) {
            stack.pop();
        }
        assertEquals(0, stack.size());
        assertThrows(UnsupportedOperationException.class, () -> stack.pop());
    }

    @Test
    public void testGetAtArguments() {
        var stack = new LocalStack();
        assertThrows(IllegalArgumentException.class, () -> stack.getAt(-1));
        assertThrows(IllegalArgumentException.class, () -> stack.getAt(0));

        stack.push(item(1));
        assertThrows(IllegalArgumentException.class, () -> stack.getAt(1));
    }

    @Test
    public void testGetAt() {
        var stack = new LocalStack();
        var items = new TypedLocal[5];

        for (int i = 0; i < items.length; i++) {
            items[i] = item(1);
            stack.push(items[i]);
        }

        for (int i = 0; i < items.length; i++) {
            assertSame(items[i], stack.getAt(i));
        }
    }

    @Test
    public void testGetAtDouble() {
        var stack = new LocalStack();
        var items = new TypedLocal[] {
                item(1), item(2), item(2), item(1), item(2)
        };

        for (int i = 0; i < items.length; i++) {
            stack.push(items[i]);
        }

        for (int i = 0; i < items.length; i++) {
            assertSame(items[i], stack.getAt(i));
        }
    }

    @Test
    public void testCopy() {
        var s1 = new LocalStack();
        s1.push(item(1));
        s1.push(item(2));
        s1.push(item(1));
        s1.push(item(1));
        s1.push(item(2));
        var s2 = s1.copy();

        assertEquals(s1, s2);
    }

    @Test
    public void testNotEquals() {
        var s1 = new LocalStack();
        s1.push(item(1));
        s1.push(item(2));
        s1.push(item(1));
        s1.push(item(1));
        s1.push(item(2));
        var s2 = s1.copy();
        s2.pop();

        assertNotEquals(s1, s2);
    }

    @Test
    public void testAssertHeightsArguments() {
        var stack = new LocalStack();
        assertThrows(IllegalArgumentException.class, () -> stack.assertHeights(new int[] { 1 }));
        stack.push(item(1));
        assertThrows(IllegalArgumentException.class, () -> stack.assertHeights(new int[] { 1, 1 }));
    }

    @Test
    public void testAssertHeightsEmpty() {
        var stack = new LocalStack();
        assertDoesNotThrow(() -> stack.assertHeights(new int[0]));
    }
    
    @Test
    public void testAssertHeightsEntireStack() {
        var stack = new LocalStack();
        stack.push(item(2));
        stack.push(item(1));
        stack.push(item(1));
        int[] heights = { 1, 1, 2 };
        assertDoesNotThrow(() -> stack.assertHeights(heights));
    }

    @Test
    public void testAssertHeightsSubStack() {
        var stack = new LocalStack();
        stack.push(item(1));
        stack.push(item(2));
        stack.push(item(1));
        stack.push(item(1));
        int[] heights = { 1, 1, 2 };
        assertDoesNotThrow(() -> stack.assertHeights(heights));
    }

    @Test
    public void testAssertHeightsMismatch() {
        var stack = new LocalStack();
        stack.push(item(1));
        stack.push(item(2));
        stack.push(item(1));
        stack.push(item(2));
        int[] heights = { 1, 1, 2 };
        assertThrows(IllegalStateException.class, () -> stack.assertHeights(heights));
    }

    @Test
    public void testClear() {
        var stack = new LocalStack();
        assertEquals(0, stack.size());
        for (int i = 0; i < 9; i++) {
            stack.push(item(1));
        }
        stack.clear();
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testToStringEmpty() {
        var stack = new LocalStack();
        assertEquals("top->btm[]", stack.toString());
    }

    @Test
    public void testToStringSingle() {
        var stack = new LocalStack();
        stack.push(item(1));
        assertEquals("top->btm[svar1:I]", stack.toString());
    }

    @Test
    public void testToStringMultiple() {
        var stack = new LocalStack();
        stack.push(item(1));
        stack.push(item(1));
        stack.push(item(2));
        stack.push(item(2));
        assertEquals("top->btm[svar1:D, svar1:D, svar1:I, svar1:I]", stack.toString());
    }
}
