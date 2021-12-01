/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.comparator;

import javolution.util.function.Equality;

/**
 * The identity comparator implementation.
 */
public class IdentityComparatorImpl<E> implements Equality<E> {

    private static final long serialVersionUID = 6576306094743751922L;

    @Override
    public boolean areEqual(E e1, E e2) {
        return e1 == e2;
    }

    @Override
    public int compare(E left, E right) {
        if (left == right)
            return 0;
        if (left == null)
            return -1;
        if (right == null)
            return 1;

        // Empirical comparison.
        return (hashCodeOf(left) < hashCodeOf(right)) ? -1 : 1;
    }

    @Override
    public int hashCodeOf(E obj) {
        return System.identityHashCode(obj);
    }
}
