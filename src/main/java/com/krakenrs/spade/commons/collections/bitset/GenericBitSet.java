package com.krakenrs.spade.commons.collections.bitset;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

/**
 * A {@link Set} implementation that is maintained by a {@link BitSet} instead of storing the
 * element's themselves explicitly. <br>
 * Each element is mapped to an integer value using the given {@link Indexer} which is used to maintain the backing
 * bitset. <br>
 * <br>
 * This class supports all of the operations defined by the {@link Set} interface but also provides operations that act
 * on two {@link GenericBitSet}s such as {@link #containsAll(GenericBitSet)}, {@link #containsAll(GenericBitSet)},
 * {@link #containsNone(GenericBitSet)} etc. In general for each Set operation, there is a corresponding
 * {@link GenericBitSet} operation that can perform optimised queries or functionality. <br>
 * It should be noted that operations acting on two GenericBitSet's must share the same {@link Indexer} and will throw
 * exceptions when mismatched sets are to be operated on.
 * 
 * @author rcx86
 * @author t81lal
 *
 * @param <E> The type of the elements this set contains
 */
public class GenericBitSet<E> implements Set<E> {
    private final BitSet bitset;
    private final Indexer<E> indexer;

    public GenericBitSet(Indexer<E> indexer) {
        this.indexer = indexer;
        bitset = new BitSet();
    }

    public GenericBitSet(GenericBitSet<E> other) {
        indexer = other.indexer;
        bitset = (BitSet) other.bitset.clone();
    }

    public GenericBitSet<E> copy() {
        return new GenericBitSet<>(this);
    }

    /**
     * Marks whether the given element is present in this set.
     * 
     * @param e The element to mark
     * @param state Whether the element should be in the set or not
     * @return The previous state of the element before this operation
     */
    public boolean set(E e, boolean state) {
        Objects.requireNonNull(e);

        if (!state && !indexer.isIndexed(e)) {
            // Short circuit: this element has no index so it's not mapped
            // to the bitset.
            return false;
        }
        int index = indexer.getIndex(e);
        boolean ret = bitset.get(index);
        bitset.set(index, state);
        return ret;
    }

    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e);
        boolean ret = !contains(e);
        bitset.set(indexer.getIndex(e));
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        if (!contains(o)) {
            return false;
        }
        bitset.set(indexer.getIndex((E) o), false);
        return true;
    }

    /**
     * See {@link #containsAll(Collection)} <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set that will be tested as subset of this set
     * @return True iff the provided set is a subset of this set
     */
    public boolean containsAll(GenericBitSet<E> other) {
        BitSet temp = (BitSet) other.bitset.clone();
        temp.and(bitset);
        return temp.equals(other.bitset);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given sets have no intersecting elements/are disjoint. <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set to check the intersection with
     * @return True iff both sets are disjoint
     */
    public boolean containsNone(GenericBitSet<E> other) {
        BitSet temp = (BitSet) bitset.clone();
        temp.and(other.bitset);
        return temp.isEmpty();
    }

    /**
     * Checks whether the given sets are not disjoint. <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set to check the intersection with
     * @return True iff the sets have a non empty intersection
     */
    public boolean containsAny(GenericBitSet<E> other) {
        return !containsNone(other);
    }

    /**
     * Computes the union of this set with the provided set and stores the result in this set. <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param n The set whose elements will be added to this set
     */
    public void addAll(GenericBitSet<E> other) {
        if (indexer != other.indexer)
            throw new IllegalArgumentException("Fast addAll operands must share the same BitSetIndexer");
        bitset.or(other.bitset);
    }

    /**
     * Computes the union of this set and the provided set without modifying either set. <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The other set to compute the union with
     * @return A {@link GenericBitSet} that represents the union of this set and the provided set
     */
    public GenericBitSet<E> union(GenericBitSet<E> other) {
        GenericBitSet<E> copy = copy();
        copy.addAll(other);
        return copy;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = false;
        for (E o : c)
            ret = add(o) || ret;
        return ret;
    }

    /**
     * Removes all elements that <b>aren't</b> in the provided set, i.e. uses the provided set as a mask.
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set to use a mask
     */
    public void retainAll(GenericBitSet<E> other) {
        bitset.and(other.bitset);
    }

    /**
     * Computes the intersection of this set and the given set without modifying either set. <br>
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set to compute the intersection with
     * @return A set representing the intersection of this set and the provided set
     */
    public GenericBitSet<E> intersect(GenericBitSet<E> other) {
        GenericBitSet<E> copy = copy();
        copy.retainAll(other);
        return copy;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Removes all elements that <b>are</b> in the provided set, i.e. computes the set difference.
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set whose elements will be removed from this set
     */
    public void removeAll(GenericBitSet<E> other) {
        bitset.andNot(other.bitset);
    }

    /**
     * Removes all elements that <b>are</b> in the provided set without modifying this set.
     * The {@link Indexer} for the provided set must be the same as this set's.
     * 
     * @param other The set whose elements will be removed from this set
     * @return A set representing the relative complement of this set and the provided set
     */
    public GenericBitSet<E> relativeComplement(GenericBitSet<E> other) {
        GenericBitSet<E> copy = copy();
        copy.removeAll(other);
        return copy;
    }

    /**
     * Creates a new set containing all of the elements in this set except for the provided element.
     * 
     * @param n The element to exclude
     * @return A set representing all of the elements of this set except the provided element
     */
    public GenericBitSet<E> relativeComplement(E n) {
        GenericBitSet<E> copy = copy();
        copy.remove(n);
        return copy;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = false;
        for (Object o : c)
            ret = remove(o) || ret;
        return ret;
    }

    @Override
    public void clear() {
        bitset.clear();
    }

    @Override
    public int size() {
        return bitset.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return bitset.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (o == null)
            throw new IllegalArgumentException();
        return indexer.isIndexed((E) o) && bitset.get(indexer.getIndex((E) o));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ ");
        for (E n : this)
            sb.append(n).append(" ");
        return sb.append("]").toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (!(o instanceof GenericBitSet))
            return false;
        GenericBitSet<E> gbs = (GenericBitSet<E>) o;
        return indexer == gbs.indexer && bitset.equals(gbs.bitset);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int index = -1;

            @Override
            public boolean hasNext() {
                return bitset.nextSetBit(index + 1) != -1;
            }

            @Override
            public E next() {
                return indexer.get(index = bitset.nextSetBit(index + 1));
            }

            @Override
            public void remove() {
                bitset.set(index, false);
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
}
