package com.hlj.threadpool.thread;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolUtils {

    private static AtomicLong atomicLong = new AtomicLong(0);
    private ThreadPoolExecutor threadPoolExecutor;

    private int corePoolSize = 10;
    private int maximumPoolSize = 300;
    private long keepAliveTime = 30000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10);
    private ThreadFactory threadFactory = r -> new Thread(r, "ThreadPoolUtils" + atomicLong.incrementAndGet());
    private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

    public void execute(Runnable runnable){
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        threadPoolExecutor.execute(runnable);
    }

    public ThreadPoolExecutor getThreadPoolExecutor(){
        if (threadPoolExecutor != null) {
            return threadPoolExecutor;
        }
        synchronized (this){
            if (threadPoolExecutor != null){
                return threadPoolExecutor;
            }
            threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,threadFactory,handler);
            return threadPoolExecutor;
        }
    }

    public ThreadPoolUtils(){
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public BlockingQueue<Runnable> getWorkQueue() {
        return workQueue;
    }

    public void setWorkQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public RejectedExecutionHandler getHandler() {
        return handler;
    }

    public void setHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
    }



}
