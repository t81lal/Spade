package com.krakenrs.spade.ir.type;

import java.util.Collection;

public interface TypeHierarchy {
    Collection<ValueType> getLeastCommonAncestors(ValueType t1, ValueType t2);

    boolean isAncestorOf(ValueType possibleAncestor, ValueType node);
}
