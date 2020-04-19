package com.krakenrs.spade.commons.collections.bitset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krakenrs.spade.commons.collections.IdSupplier;

public class CachingBitSetIndexerTest {
    class TestElement {
        int index;
        int indexCalls;

        TestElement(int index) {
            this.index = index;
        }

        public int getIndex() {
            indexCalls++;
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TestElement) {
                return ((TestElement) o).index == index;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return index;
        }
    }

    final IdSupplier<TestElement> elements = new IdSupplier<>(TestElement::new);

    @BeforeEach
    void setup() {
        elements.reset();
    }

    @Test
    void test1() {
        Indexer<TestElement> ixr = new CachingBitSetIndexer<>((e) -> e.getIndex());

        var es = new ArrayList<TestElement>();
        for (int i = 0; i < 5; i++) {
            es.add(elements.get());
        }

        for (var e : es) {
            assertFalse(ixr.isIndexed(e));
        }

        for (var e : es) {
            assertFalse(ixr.isIndexed(e));
            // causes getIndex() to be called
            ixr.getIndex(e);
            assertTrue(ixr.isIndexed(e));
        }

        for (int i = 0; i < 5; i++) {
            assertSame(es.get(i), ixr.get(i));
        }

        for (var e : es) {
            // Shouldn't trigger getIndex() call again as it's cached
            ixr.getIndex(e);
            assertEquals(1, e.indexCalls);
        }
    }
}
