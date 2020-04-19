package com.krakenrs.spade.commons.collections.bitset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krakenrs.spade.commons.collections.IdSupplier;

public class GenericBitSetTest {
    final IdSupplier<TestElement> elements = new IdSupplier<>(TestElement::new);
    final Supplier<Indexer<TestElement>> indexers = () -> new CachingBitSetIndexer<TestElement>((e) -> e.getIndex());

    @BeforeEach
    void setup() {
        elements.reset();
    }

    @Test
    void testCopyEmpty1() {
        var indexer = indexers.get();
        var set1 = new GenericBitSet<>(indexer);
        var set2 = set1.copy();
        assertEquals(set1, set2);
    }

    @Test
    void testCopyEmpty2() {
        var indexer = indexers.get();
        var set1 = new GenericBitSet<>(indexer);
        var e = elements.get();
        set1.add(e);
        set1.remove(e);
        var set2 = set1.copy();
        assertEquals(set1, set2);
    }

    @Test
    void testCopy1() {
        var indexer = indexers.get();
        var set1 = new GenericBitSet<>(indexer);
        for (int i = 0; i < 10; i++) {
            set1.add(elements.get());
        }
        var set2 = set1.copy();
        assertEquals(set1, set2);
    }

    @Test
    void testAdd1() {
        var indexer = indexers.get();
        var set = new GenericBitSet<>(indexer);

        var e = elements.get();
        set.add(e);
        assertTrue(set.contains(e));
    }
}
