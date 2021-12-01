/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.mvstore;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A cursor to iterate over elements in ascending order.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
//深度优先，然后到右边的同节点中的记录，接着过渡到父节点，父节点的右节点...
public final class Cursor<K,V> implements Iterator<K> {
    private final K to;
    private CursorPos<K,V> cursorPos;
    private CursorPos<K,V> keeper;
    private K current;
    private K last;
    private V lastValue;
    private Page<K,V> lastPage;

    public Cursor(Page<K,V> root, K from) {
        this(root, from, null);
    }

    public Cursor(Page<K,V> root, K from, K to) {
        this.cursorPos = traverseDown(root, from);
        this.to = to;
    }

    @Override
    public boolean hasNext() {
        if (cursorPos != null) {
            while (current == null) {
                Page<K,V> page = cursorPos.page;
                int index = cursorPos.index;
                if (index >= (page.isLeaf() ? page.getKeyCount() : page.map.getChildPageCount(page))) {
                    CursorPos<K,V> tmp = cursorPos;
                    cursorPos = cursorPos.parent;
                    tmp.parent = keeper;
                    keeper = tmp;
                    if(cursorPos == null)
                    {
                        return false;
                    }
                } else {
                    while (!page.isLeaf()) {
                        page = page.getChildPage(index);
                        if (keeper == null) {
                            cursorPos = new CursorPos<>(page, 0, cursorPos);
                        } else {
                            CursorPos<K,V> tmp = keeper;
                            keeper = keeper.parent;
                            tmp.parent = cursorPos;
                            tmp.page = page;
                            tmp.index = 0;
                            cursorPos = tmp;
                        }
                        index = 0;
                    }
                    if (index < page.getKeyCount()) {
                        K key = page.getKey(index);
                        if (to != null && page.map.getKeyType().compare(key, to) > 0) {
                            return false;
                        }
                        current = last = key;
                        lastValue = page.getValue(index);
                        lastPage = page;
                    }
                }
                ++cursorPos.index;
            }
        }
        return current != null;
    }

    @Override
    public K next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        current = null;
        return last;
    }

    /**
     * Get the last read key if there was one.
     *
     * @return the key or null
     */
    public K getKey() {
        return last;
    }

    /**
     * Get the last read value if there was one.
     *
     * @return the value or null
     */
    public V getValue() {
        return lastValue;
    }

    /**
     * Get the page where last retrieved key is located.
     *
     * @return the page
     */
    @SuppressWarnings("unused")
    Page<K,V> getPage() {
        return lastPage;
    }

    /**
     * Skip over that many entries. This method is relatively fast (for this map
     * implementation) even if many entries need to be skipped.
     *
     * @param n the number of entries to skip
     */
    public void skip(long n) {
        if (n < 10) {
            while (n-- > 0 && hasNext()) {
                next();
            }
        } else if(hasNext()) {
            assert cursorPos != null;
            CursorPos<K,V> cp = cursorPos;
            CursorPos<K,V> parent;
            while ((parent = cp.parent) != null) cp = parent;
            Page<K,V> root = cp.page;
            MVMap<K,V> map = root.map;
            long index = map.getKeyIndex(next());
            last = map.getKey(index + n);
            this.cursorPos = traverseDown(root, last);
        }
    }

    /**
     * Fetch the next entry that is equal or larger than the given key, starting
     * from the given page. This method retains the stack.
     *
     * @param p the page to start from
     * @param key the key to search, null means search for the first key
     */
//<<<<<<< HEAD
//    private static CursorPos traverseDown(Page p, Object key) {
////<<<<<<< HEAD
////        CursorPos cursorPos = null;
////        while (!p.isLeaf()) {
////            assert p.getKeyCount() > 0;
////            int index = 0;
////            if(key != null) {
////                index = p.binarySearch(key) + 1;
////                if (index < 0) {
////                    index = -index;
////                }
////            }
//////<<<<<<< HEAD
//////            //遍历完当前leaf page后，就转到parent page，然后就到右边的第一个兄弟page，
//////            //所以要x+1
//////            pos = new CursorPos(p, x + 1, pos); 
//////            p = p.getChildPage(x);
//////=======
////            cursorPos = new CursorPos(p, index, cursorPos);
////            p = p.getChildPage(index);
////        }
////        int index = 0;
////        if(key != null) {
////            index = p.binarySearch(key);
////            if (index < 0) {
////                index = -index - 1;
////            }
////=======
//        CursorPos cursorPos = key == null ? p.getPrependCursorPos(null) : CursorPos.traverseDown(p, key);
//=======
    private static <K,V> CursorPos<K,V> traverseDown(Page<K,V> p, K key) {
        CursorPos<K,V> cursorPos = key == null ? p.getPrependCursorPos(null) : CursorPos.traverseDown(p, key);
        if (cursorPos.index < 0) {
            cursorPos.index = -cursorPos.index - 1; 
        }
        return cursorPos;
    }
}
