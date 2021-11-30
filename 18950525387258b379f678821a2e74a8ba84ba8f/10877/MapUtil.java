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

package org.glassfish.admin.amx.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class MapUtil
{
    private MapUtil()
    {
        // disallow instantiation
    }

    public static <K, V> V getWithDefault(final Map<K, V> m, final K key, V defaultValue)
    {
        return m.containsKey(key) ? m.get(key) : defaultValue;
    }

    public static Object[] getKeyObjects(final Map<?, ?> m)
    {
        final Object[] keys = new Object[m.keySet().size()];
        m.keySet().toArray(keys);
        return keys;
    }

    public static String[] getKeyStrings(final Map<?, ?> m)
    {
        return (SetUtil.toSortedStringArray(m.keySet()));
    }

    /** assignable to any map type, keeps things short on the rhs */
    public static <K, V> Map<K, V> newMap()
    {
        return new HashMap<K, V>();
    }

    /**
    Create a new Map consisting of a single key/value pair.
     */
    public static <V> Map<String, V> newMap(
            final String key,
            final V value)
    {
        final Map<String, V> m = new HashMap<String, V>();

        m.put(key, value);

        return (m);
    }

    /**
    Create a new Map consisting of a single key/value pair.
     */
    public static <K, V> Map<K, V> newMap(
            final Map<K, V> m1,
            final Map<K, V> m2)
    {
        final Map<K, V> m = new HashMap<K, V>();

        if (m1 != null)
        {
            m.putAll(m1);
        }
        if (m2 != null)
        {
            m.putAll(m2);
        }

        return (m);
    }

    public static <K, V> Map<K, V> toMap(final Object[] params, final Class<K> keyClass, final Class<V> valueClass)
    {
        final Map<K, V> m = new HashMap<K, V>();

        for (int i = 0; i < params.length; i += 2)
        {
            final Object key = params[i];
            if (key == null)
            {
                throw new IllegalArgumentException();
            }

            if (!keyClass.isAssignableFrom(key.getClass()))
            {
                throw new IllegalArgumentException("Key of class " + key.getClass().getName() + " not assignable to " + keyClass.getName());
            }

            final Object value = params[i + 1];
            if (value != null && !valueClass.isAssignableFrom(value.getClass()))
            {
                throw new IllegalArgumentException("Value of class " + value.getClass().getName() + " not assignable to " + valueClass.getName());
            }

            m.put(keyClass.cast(key), valueClass.cast(value));
        }
        return m;
    }

    /**
    Create a new Map and insert the specified mappings as found in 'mappings'.
    The even-numbered entries are the keys, and the odd-numbered entries are
    the values.
     */
    public static <T> Map<T, T> newMap(final T... mappings)
    {
        if ((mappings.length % 2) != 0)
        {
            throw new IllegalArgumentException("mappings must have even length");
        }

        final Map<T, T> m = new HashMap<T, T>();

        for (int i = 0; i < mappings.length; i += 2)
        {
            m.put(mappings[i], mappings[i + 1]);
        }

        return (m);
    }

    /**
    Create a new Map and insert the specified mappings as found in 'mappings'.
    The even-numbered entries are the keys, and the odd-numbered entries are
    the values.
     */
    public static Map<String, String> newMap(final String... mappings)
    {
        if ((mappings.length % 2) != 0)
        {
            throw new IllegalArgumentException("mappings must have even length");
        }

        final Map<String, String> m = new HashMap<String, String>();

        for (int i = 0; i < mappings.length; i += 2)
        {
            m.put(mappings[i], mappings[i + 1]);
        }

        return (m);
    }

    /**
    Remove all entries keyed by 'keys'
     */
    public static <T> void removeAll(
            final Map<T, ?> m,
            final T[] keys)
    {
        for (int i = 0; i < keys.length; ++i)
        {
            m.remove(keys[i]);
        }
    }

    public static boolean mapsEqual(
            final Map<?, ?> m1,
            final Map<?, ?> m2)
    {
        if (m1 == m2)
        {
            return (true);
        }

        boolean equal = false;

        if (m1.size() == m2.size() &&
            m1.keySet().equals(m2.keySet()))
        {
            equal = true;

            for (final Map.Entry<?,?> me : m1.entrySet())
            {
                final Object key = me.getKey();
                final Object value1 = me.getValue();
                final Object value2 = m2.get(key);

                if (!CompareUtil.objectsEqual(value1, value2))
                {
                    equal = false;
                    break;
                }
            }
        }

        return (equal);
    }

    public static <K, V> Map<K, V> newMapNoNullValues(final Map<K, V> m)
    {
        final Map<K, V> result = new HashMap<K, V>();

        for (final Map.Entry<K, V> me : m.entrySet())
        {
            final V value = me.getValue();

            if (value != null)
            {
                result.put(me.getKey(), value);
            }
        }

        return (result);
    }

    public static String toString(final Map<?, ?> m)
    {
        return (toString(m, ","));
    }

    public static String toString(final Map<?, ?> m, final String separator)
    {
        if (m == null)
        {
            return ("null");
        }

        final StringBuffer buf = new StringBuffer();

        final String[] keyStrings = getKeyStrings(m);
        for (final String key : keyStrings)
        {
            final Object value = m.get(key);

            buf.append(key);
            buf.append("=");
            buf.append(StringUtil.toString(value));
            buf.append(separator);
        }
        if (buf.length() != 0)
        {
            // strip trailing separator
            buf.setLength(buf.length() - separator.length());
        }

        return (buf.toString());
    }

    public static <K> Set<K> getNullValueKeys(final Map<K, ?> m)
    {
        final Set<K> s = new HashSet<K>();

        for (final Map.Entry<K, ?> me : m.entrySet())
        {
            if (me.getValue() == null)
            {
                s.add(me.getKey());
            }
        }
        return (s);
    }

    public static <K, V> Map<K, V> toMap(final Properties props, final Class<K> kClass, final Class<V> vClass)
    {
        return TypeCast.checkMap(props, kClass, vClass);
    }

    public static <K> void removeAll(final Map<K, ?> m, final Set<K> s)
    {
        for (final K key : s)
        {
            m.remove(key);
        }
    }

    /**
    @return true if non-null Map and all keys and values are of type java.lang.String
     */
    public static boolean isAllStrings(final Map<?, ?> m)
    {
        return m != null && CollectionUtil.isAllStrings(m.keySet()) &&
               CollectionUtil.isAllStrings(m.values());
    }

    /**
    Convert an arbitrary Map to one whose keys and values
    are both of type String.
     */
    public static Map<String, String> toStringStringMap(final Map<?, ?> m)
    {
        if (m == null)
        {
            return null;
        }

        Map<String, String> result;

        if (isAllStrings(m))
        {
            result = TypeCast.asMap(m);
        }
        else
        {
            result = new HashMap<String, String>();

            for (final Map.Entry<?, ?> me : m.entrySet())
            {
                final Object key = me.getKey();
                final Object value = me.getValue();

                if ((key instanceof String) && (value instanceof String))
                {
                    result.put((String) key, (String) value);
                }
                else
                {
                    result.put("" + key, "" + value);
                }
            }
        }

        return result;
    }

}











