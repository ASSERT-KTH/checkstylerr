package com.hlj.util.Z003ThreadLocal;

import java.util.HashMap;
import java.util.Map;


public class ThreadLocalHolderMap {
	

	/* 上下文标识 */
	public static final String CONTEXT_ID = "CONTEXT.ID";

	private Map<String, Object> contextMap;
   /**
     * 为每个线程提供一个默认的值
     */
	private static ThreadLocal<ThreadLocalHolderMap> threadLocal = ThreadLocal.withInitial(() -> new ThreadLocalHolderMap(new HashMap<String, Object>()));
	
    private ThreadLocalHolderMap(Map<String, Object> contextMap) {
        this.contextMap = contextMap;
    }

    public static ThreadLocalHolderMap getWsContext() {
        return threadLocal.get();
    }

	public void setContextId(String contextId) {
		put(CONTEXT_ID, contextId);
	}

    public String getContextId() {
    	return (String) get(CONTEXT_ID);
    }
    

    public Object get(String key) {
        return contextMap.get(key);
    }

    public void put(String key, Object value) {
		contextMap.put(key, value);
    }

    public void remove(String key) {
		contextMap.remove(key);
    }

}
