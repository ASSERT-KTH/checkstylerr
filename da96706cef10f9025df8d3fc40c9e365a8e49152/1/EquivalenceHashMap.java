/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.commons.collections;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A hashmap implementation accepting different notions of equvialence for keys (based on the Guava Equivalence class).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EquivalenceHashMap<K,V> implements Map<K,V> {

    private Equivalence equivalence;

    private HashMap<ElementWrapper, V> delegate;


    private UnwrapFunction unwrapper = new UnwrapFunction();
    private WrapFunction   wrapper   = new WrapFunction();


    public EquivalenceHashMap(Equivalence equivalence) {
        this.equivalence = equivalence;
        this.delegate = new HashMap<>();
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *                                       is not supported by this map
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.  More formally, returns <tt>true</tt> if and only if
     * this map contains a mapping for a key <tt>k</tt> such that
     * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(new ElementWrapper((K)key));
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  More formally, returns <tt>true</tt> if and only if
     * this map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
     * will probably require time linear in the map size for most
     * implementations of the <tt>Map</tt> interface.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     * @throws ClassCastException   if the value is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified value is null and this
     *                              map does not permit null values
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     * <p/>
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     * <p/>
     * <p>If this map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public V get(Object key) {
        return delegate.get(new ElementWrapper((K)key));
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this map
     * @throws NullPointerException          if the specified key or value is null
     *                                       and this map does not permit null keys or values
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this map
     */
    @Override
    public V put(K key, V value) {
        return delegate.put(new ElementWrapper(key), value);
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     * <p/>
     * <p>Returns the value to which this map previously associated the key,
     * or <tt>null</tt> if the map contained no mapping for the key.
     * <p/>
     * <p>If this map permits null values, then a return value of
     * <tt>null</tt> does not <i>necessarily</i> indicate that the map
     * contained no mapping for the key; it's also possible that the map
     * explicitly mapped the key to <tt>null</tt>.
     * <p/>
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the key is of an inappropriate type for
     *                                       this map
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key is null and this
     *                                       map does not permit null keys
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public V remove(Object key) {
        return delegate.remove(new ElementWrapper((K)key));
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(Object, Object) put(k, v)} on this map once
     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
     * specified map.  The behavior of this operation is undefined if the
     * specified map is modified while the operation is in progress.
     *
     * @param m mappings to be stored in this map
     * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the class of a key or value in the
     *                                       specified map prevents it from being stored in this map
     * @throws NullPointerException          if the specified map is null, or if
     *                                       this map does not permit null keys or values, and the
     *                                       specified map contains null keys or values
     * @throws IllegalArgumentException      if some property of a key or value in
     *                                       the specified map prevents it from being stored in this map
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Entry entry : m.entrySet()) {
            put((K)entry.getKey(),(V)entry.getValue());
        }
    }

    /**
     * Returns a {@link java.util.Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    @Override
    public Set<K> keySet() {
        final Set<ElementWrapper> wrapped = delegate.keySet();

        return new Set<K>() {
            @Override
            public int size() {
                return wrapped.size();
            }

            @Override
            public boolean isEmpty() {
                return wrapped.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return wrapped.contains(new ElementWrapper((K)o));
            }

            @Override
            public Iterator<K> iterator() {
                return Iterators.transform(wrapped.iterator(),unwrapper);
            }

            @Override
            public Object[] toArray() {
                return Iterators.toArray(this.iterator(), Object.class);
            }

            @Override
            public <T> T[] toArray(T[] a) {
                T[] result;
                if(a.length < size()) {
                    result = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
                } else {
                    result = a;
                }
                int pos = 0;
                for(ElementWrapper w : wrapped) {
                    try {
                    result[pos++] = (T) w.delegate;
                    } catch (ClassCastException ex) {
                        throw new ArrayStoreException("invalid type for array");
                    }
                }
                if(pos < result.length) {
                    result[pos] = null;
                }
                return result;
            }

            @Override
            public boolean add(K k) {
                return wrapped.add(new ElementWrapper(k));
            }

            @Override
            public boolean remove(Object o) {
                return wrapped.remove(new ElementWrapper((K)o));
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return wrapped.containsAll(Collections2.transform(c, wrapper));
            }

            @Override
            public boolean addAll(Collection<? extends K> c) {
                return wrapped.addAll(Collections2.transform(c, wrapper));
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return wrapped.retainAll(Collections2.transform(c, wrapper));
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return wrapped.removeAll(Collections2.transform(c, wrapper));
            }

            @Override
            public void clear() {
                wrapped.clear();
            }
        };
    }

    /**
     * Returns a {@link java.util.Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    /**
     * Returns a {@link java.util.Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<ElementWrapper,V>> wrapped = delegate.entrySet();
        final EntryUnwrapFunction entryUnwrapFunction = new EntryUnwrapFunction();
        final EntryWrapFunction   entryWrapFunction   = new EntryWrapFunction();

        return new Set<Entry<K, V>>() {
            @Override
            public int size() {
                return wrapped.size();
            }

            @Override
            public boolean isEmpty() {
                return wrapped.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return wrapped.contains(new ElementWrapper((K)o));
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return Iterators.transform(wrapped.iterator(), entryUnwrapFunction);
            }

            @Override
            public Object[] toArray() {
                return Iterators.toArray(this.iterator(), Object.class);
            }

            @Override
            public <T> T[] toArray(T[] a) {
                T[] result;
                if(a.length < size()) {
                    result = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
                } else {
                    result = a;
                }
                int pos = 0;
                for(Entry<ElementWrapper,V> w : wrapped) {
                    try {
                        result[pos++] = (T) entryUnwrapFunction.apply(w);
                    } catch (ClassCastException ex) {
                        throw new ArrayStoreException("invalid type for array");
                    }
                }
                if(pos < result.length) {
                    result[pos] = null;
                }
                return result;
            }

            @Override
            public boolean add(Entry<K, V> k) {
                return wrapped.add(entryWrapFunction.apply(k));
            }

            @Override
            public boolean remove(Object o) {
                return wrapped.remove(entryWrapFunction.apply((Entry<K, V>)o));
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return wrapped.containsAll(Collections2.transform(c, entryWrapFunction));
            }

            @Override
            public boolean addAll(Collection<? extends Entry<K, V>> c) {
                return wrapped.addAll(Collections2.transform(c, entryWrapFunction));
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return wrapped.retainAll(Collections2.transform(c, entryWrapFunction));
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return wrapped.removeAll(Collections2.transform(c, entryWrapFunction));
            }

            @Override
            public void clear() {
                wrapped.clear();
            }
        };
    }

    private class ElementWrapper {

        private K delegate;

        private ElementWrapper(K delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof EquivalenceHashMap<?,?>.ElementWrapper) {
                return equivalence.equivalent(ElementWrapper.this.delegate, ((ElementWrapper)o).delegate);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return equivalence.hash(delegate);
        }
    }

    private class UnwrapFunction implements Function<ElementWrapper, K> {
        @Override
        public K apply(ElementWrapper input) {
            return input.delegate;
        }
    }

    private class WrapFunction implements Function<Object,ElementWrapper> {
        @Override
        public ElementWrapper apply(Object input) {
            return new ElementWrapper((K)input);
        }
    }

    private class EntryUnwrapFunction implements Function<Entry<ElementWrapper,V>, Entry<K, V>> {
        @Override
        public Entry<K, V> apply(final Entry<ElementWrapper, V> input) {
            return new Entry<K, V>() {
                @Override
                public K getKey() {
                    return input.getKey().delegate;
                }

                @Override
                public V getValue() {
                    return input.getValue();
                }

                @Override
                public V setValue(V value) {
                    return input.setValue(value);
                }
            };
        }
    }

    private class EntryWrapFunction implements Function<Object,Entry<ElementWrapper,V>> {
        @Override
        public Entry<ElementWrapper, V> apply(Object o) {
            final Entry<K, V> input = (Entry<K,V>)o;
            return new Entry<ElementWrapper, V>() {
                @Override
                public ElementWrapper getKey() {
                    return new ElementWrapper(input.getKey());
                }

                @Override
                public V getValue() {
                    return input.getValue();
                }

                @Override
                public V setValue(V value) {
                    return input.setValue(value);
                }
            };
        }
    }

}
