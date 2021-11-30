//package Multithreading.In /**
// * Created by Chaklader on 1/13/17.
// */
//
//
///*
//*
//* # ==================================
//* # Simple Thread-safe In-memory Cache
//
//High performance scalable web applications often use a distributed in-memory data cache in front of
//or in place of robust persistentstorage for some tasks. In Java Applications it is very common to use
//in Memory Cache for better performance.
//
//Cache: A cache is an area of local memory that holds a copy of frequently accessed data that is otherwise
//expensive to get or compute. Examples of such data include a result of a query to a database, a disk file
//or a report.
//
//
//# Implementation Criterias of the Cache
//# =====================================
//
//1. Items will expire based on a time to live period.
//
//2. Cache will keep most recently used items if you will try to add more items then max specified. (apache
//common collections has a LRUMap,which, removes the least used entries from a fixed sized map)
//
//3. For the expiration of items we can timestamp the last access and in a separate thread remove the items
//when the time to live limit is reached. This is nice for reducing memory pressure for applications that have
//long idle time in between accessing the cached objects.
//* */
//
//
//import org.apache.commons.collections.MapIterator;
//import org.apache.commons.collections.map.LRUMap;
//import java.util.ArrayList;
//
//
//public class SimpleInMemoryCache<K, T> {
//
//    private long timeToLive;
//    private LRUMap cacheMap;
//
//    // made a nested class for the Cache Object
//    protected class cacheObject {
//
//        public T value;
//        public long lastAccessed = System.currentTimeMillis();
//
//        protected cacheObject(T value) {
//            this.value = value;
//        }
//    }
//
//    public SimpleInMemoryCache(long timeToLive, final long timerInterval, int maxItems) {
//
//        this.timeToLive = timeToLive * 1000;
//        cacheMap = new LRUMap(maxItems);
//
//        if (this.timeToLive > 0 && timerInterval > 0) {
//
//            Thread t = new Thread(new Runnable() {
//                public void run() {
//
//                    while (true) {
//
//                        try {
//                            Thread.sleep(timerInterval * 1000);
//                        } catch (InterruptedException ex) {
//                        }
//                        cleanup();
//                    }
//                }
//            });
//
//            t.setDaemon(true);
//            t.start();
//        }
//    }
//
//    public void put(K key, T value) {
//        synchronized (cacheMap) {
//            cacheMap.put(key, new cacheObject(value));
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public T get(K key) {
//        synchronized (cacheMap) {
//            cacheObject c = (cacheObject) cacheMap.get(key);
//
//            if (c == null)
//                return null;
//            else {
//                c.lastAccessed = System.currentTimeMillis();
//                return c.value;
//            }
//        }
//    }
//
//    public void remove(K key) {
//        synchronized (cacheMap) {
//            cacheMap.remove(key);
//        }
//    }
//
//    public int size() {
//        synchronized (cacheMap) {
//            return cacheMap.size();
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public void cleanup() {
//
//        long now = System.currentTimeMillis();
//        ArrayList<K> deleteKey = null;
//
//        synchronized (cacheMap) {
//            MapIterator itr = cacheMap.mapIterator();
//
//            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
//            K key = null;
//            cacheObject c = null;
//
//            while (itr.hasNext()) {
//                key = (K) itr.next();
//                c = (cacheObject) itr.getValue();
//
//                if (c != null && (now > (timeToLive + c.lastAccessed))) {
//                    deleteKey.add(key);
//                }
//            }
//        }
//
//        for (K key : deleteKey) {
//            synchronized (cacheMap) {
//                cacheMap.remove(key);
//            }
//
//            Thread.yield();
//        }
//    }
//}
