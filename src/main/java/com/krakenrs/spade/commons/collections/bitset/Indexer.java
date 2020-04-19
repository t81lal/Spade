package com.krakenrs.spade.commons.collections.bitset;

/**
 * Mapping function that provides an integer identity for given object elements. <br>
 * 
 * The following restrictions must be observed for indexing to be valid:
 * <ul>
 * <li>For any two elements A and B, <code>getIndex(A) == getIndex(B)</code> iff <code>A.equals(B)</code></li>
 * <li>The index of an element must not change between different invocations of {@link #getIndex(E)}</li>
 * <li>All index values must be nonnegative</li>
 * </ul>
 * 
 * @author rcx86
 *
 * @param <E>
 */
public interface Indexer<E> {
    /**
     * Get an index value for the given element in accordance with the contract defined in this class's doc.
     * 
     * @param e
     * @return
     */
    int getIndex(E e);

    /**
     * Retrieve an element given it's index value.
     * 
     * @param index
     * @return
     * @throws IllegalArgumentException if there is no element associated with the given index
     */
    E get(int index);

    /**
     * Check's whether the given element has an index value associated with it.
     * 
     * @param e
     * @return True if the element has been indexed and has an index value else false.
     */
    boolean isIndexed(E e);
}
