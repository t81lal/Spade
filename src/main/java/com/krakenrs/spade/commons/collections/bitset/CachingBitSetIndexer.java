package com.krakenrs.spade.commons.collections.bitset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * {@link Indexer} implementation that stores the index mappings for elements and only computes new indices if an
 * element is unmapped. This is done through the provided indexing function.
 * 
 * @author rcx86
 * @author t81lal
 *
 * @param <E>
 */
public class CachingBitSetIndexer<E> implements Indexer<E> {
    private final Map<E, Integer> map;
    private final Map<Integer, E> reverseMap;
    private final Function<E, Integer> indexingFunction;

    /**
     * Creates a new CachingBitSetIndexer.
     * 
     * @param indexingFunction BiFunction that takes in a reference to this indexer and the element to be indexed and
     *     returns a unique index
     */
    public CachingBitSetIndexer(BiFunction<CachingBitSetIndexer<E>, E, Integer> indexingFunction) {
        this.indexingFunction = (e) -> indexingFunction.apply(this, e);
        this.map = new HashMap<>();
        this.reverseMap = new HashMap<>();
    }

    /**
     * Creates a new CachingBitSetIndexer.
     * 
     * @param indexingFunction Function that takes in the element to index and returns a unique index for it
     */
    public CachingBitSetIndexer(Function<E, Integer> indexingFunction) {
        this.indexingFunction = indexingFunction;
        this.map = new HashMap<>();
        this.reverseMap = new HashMap<>();
    }
    
    public Set<E> getKeys() {
        return map.keySet();
    }

    @Override
    public int getIndex(E e) {
        int index = map.computeIfAbsent(e, indexingFunction);
        reverseMap.put(index, e);
        return index;
    }

    @Override
    public E get(int index) {
        if (reverseMap.containsKey(index)) {
            return reverseMap.get(index);
        } else {
            throw new IllegalArgumentException("Index " + index + " is not mapped to an element");
        }
    }

    @Override
    public boolean isIndexed(E e) {
        return map.containsKey(e);
    }
    
    /**
     * Creates a new {@link CachingBitSetIndexer} that generates indexes sequentially and consecutively, i.e. each new
     * index that is generated is one greater than the previous.
     * 
     * @return
     */
    public static <N> CachingBitSetIndexer<N> newSequentialIndexer() {
        return new CachingBitSetIndexer<>((i, e) -> i.map.size() + 1);
    }
}
