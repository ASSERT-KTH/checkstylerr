/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.jdo.spi.persistence.utility;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class implements bucketize hashtable, which subdivide the key
 * collection stored into several hashtables (buckets) of smaller size.
 * This will reduce the contention of hashtable.
 *
 * @author Shing Wai Chan
 */
public class BucketizedHashtable implements Cloneable, Map, Serializable {
    private int bucketSize;
    private Hashtable[] hashtables = null;

    /**
     * Constructs a new, empty BucketizedHashtable with the specified
     * bucket size, initial capacity and load factor.
     * @param bucketSize      the number of buckets used for hashing
     * @param initialCapacity the initial capacity of BucketizedHashtable
     * @param loadFactor      the load factor of hashtable
     */
    public BucketizedHashtable(int bucketSize, int initialCapacity,
            float loadFactor) {
        if (bucketSize <= 0 || initialCapacity < 0) {
            throw new IllegalArgumentException();
        }

        this.bucketSize = bucketSize;

        hashtables = new Hashtable[bucketSize];

        // always round up to the nearest integer so that it has at
        // least the initialCapacity
        int initialHashtableSize =
                (int)Math.ceil((double)initialCapacity / bucketSize);

        for (int i = 0; i < bucketSize; i++) {
            hashtables[i] = new Hashtable(initialHashtableSize, loadFactor);
        }
    }

    /**
     * Constructs a new, empty BucketizedHashtable with the specified
     * bucket size, initial capacity and default load factor 0.75.
     * @param bucketSize      the number of buckets used for hashing
     * @param initialCapacity the initial capacity of hashtable
     */
    public BucketizedHashtable(int bucketSize, int initialCapacity) {
        this(bucketSize, initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty BucketizedHashtable with the specified
     * bucket size, default initial capacity (11 * bucketSize) and
     * default load factor 0.75.
     * @param bucketSize      the number of buckets used for hashing
     */
    public BucketizedHashtable(int bucketSize) {
        this(bucketSize, 11 * bucketSize, 0.75f);
    }

    /**
     * Constructs a new, empty BucketizedHashtable with the default bucket
     * size 11, default initial capacity (11 * bucketSize)
     * and default load factor 0.75.
     */
    public BucketizedHashtable() {
        this(11, 11 * 11, 0.75f);
    }

    //-------- implementing Map --------

    /**
     * @param  key  a key in the hashtable
     * @return the value to which the specified key is mapped.
     */
    public Object get(Object key) {
        return hashtables[getBucketIndex(key)].get(key);
    }

    /**
     * Remove the key and its corresponding value.
     * @param  key  the key that needs to be removed
     * @return the value to which the key had been mapped,
     *         or <code>null</code> if the key did not have a mapping.
     */
    public Object remove(Object key) {
        return hashtables[getBucketIndex(key)].remove(key);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * @param key   the hashtable key
     * @param value the value
     * @return      the previous value of the specified key in hashtables,
     *              or <code>null</code> if it did not have one.
     */
    public Object put(Object key, Object value) {
        return hashtables[getBucketIndex(key)].put(key, value);
    }

    /**
     * @param t  BucketizedHashtable
     *           or a Map with a supported operation entrySet
     */
    public void putAll(Map t) {
        if (t instanceof BucketizedHashtable) {
            BucketizedHashtable bt = (BucketizedHashtable)t;
            for (int i = 0; i < bt.bucketSize; i++) {
                putAllFromMapWithEntrySet(bt.hashtables[i]);
            }
        } else {
            putAllFromMapWithEntrySet(t);
        }
    }

    /**
     * @param  key  possible key
     * @return true if and only if the specified object is a key in one of
     *         of the hashtables
     */
    public boolean containsKey(Object key) {
        return hashtables[getBucketIndex(key)].containsKey(key);
    }

    /**
     * @param  value  possible value
     * @return true if and only if the specified object is a value in one of
     *         of the hashtables
     */
    public boolean containsValue(Object value) {
        for (int i = 0; i < bucketSize; i++) {
            if (hashtables[i].containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the total number of key-value mappings of all buckets
     */
    public int size() {
        int totalSize = 0;
        for (int i = 0; i < bucketSize; i++) {
             totalSize += hashtables[i].size();
        }
        return totalSize;
    }

    /**
     * @return the hash code value for this map
     */
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < bucketSize; i++) {
            h += hashtables[i].hashCode();
        }
        return h;
    }

    /**
     * @return true if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        for (int i = 0; i < bucketSize; i++) {
             if (!hashtables[i].isEmpty()) {
                 return false;
             }
        }
        return true;
    }

    /**
     * Clears this BucketizedHashtable so that it contains no key.
     */
    public void clear() {
        for (int i = 0; i < bucketSize; i++) {
            hashtables[i].clear();
        }
    }

    /**
     * The return set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     * @return a set of Map.Entry when bucketSet equal 1
     * @exception UnsupportedOperationException when bucketSize is greater one
     */
    public Set entrySet() {
        if (bucketSize == 1) {
            return hashtables[0].entrySet();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The return set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     * @return a set of keys when bucketSet equal 1
     * @exception UnsupportedOperationException when bucketSize is greater one
     */
    public Set keySet() {
        if (bucketSize == 1) {
            return hashtables[0].keySet();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The return collection is backed by the map, so changes to the map
     * are reflected in the collection, and vice-versa.
     * @return a collection of values when bucketSet equal 1
     * @exception UnsupportedOperationException when bucketSize is greater one
     */
    public Collection values() {
        if (bucketSize == 1) {
            return hashtables[0].values();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Compares the specified object with this map for equality.
     * @return true if the specified object is a BucketizedHashtable
     *         with hashtables represent the same set of mappings.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof BucketizedHashtable)) {
            return false;
        }
        BucketizedHashtable bt = (BucketizedHashtable)o;
        if (bt.bucketSize != bucketSize || bt.size() != size()) {
            return false;
        }

        for (int i = 0; i < bucketSize; i++) {
             if (!hashtables[i].equals(bt.hashtables[i])) {
                 return false;
             }
        }
        return true;
    }

    //-------- implementing Cloneable --------
    /**
     * Creates and returns a shallow copy of this object.
     * The keys and values are not cloned.
     * @return  a clone of BucketizedHashtable
     */
    public Object clone() {
        try {
            BucketizedHashtable bt = (BucketizedHashtable)super.clone();
            bt.bucketSize = bucketSize;
            bt.hashtables = new Hashtable[bucketSize];
            for (int i = 0; i < bucketSize; i++) {
                bt.hashtables[i] = (Hashtable)hashtables[i].clone();
            }
            return bt;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    //----------------
    /**
     * @return a string representation of this BucketizedHashtable
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("[");  // NOI18N
        //bucketSize always >= 1
        buf.append(hashtables[0].toString());
        for (int i = 1; i < bucketSize; i++) {
            buf.append(", "); // NOI18N
            buf.append(hashtables[i].toString());
        }
        buf.append("]"); // NOI18N
        return buf.toString();
    }

    /**
     * @param t  Map with a supported entrySet operation
     */
    private void putAllFromMapWithEntrySet(Map t) {
        Iterator iter = t.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry)iter.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * @param  key
     * @return the bucket index for the specified key
     */
    private int getBucketIndex(Object key) {
        int index = key.hashCode() % bucketSize;
        return (index >= 0) ? index : index + bucketSize;
    }
}
