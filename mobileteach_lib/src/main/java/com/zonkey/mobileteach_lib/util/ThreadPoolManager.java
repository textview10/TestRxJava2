package com.zonkey.mobileteach_lib.util;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xu.wang
 * Date on 2016/10/24 10:37
 * 线程池
 */
public class ThreadPoolManager {
    private ThreadPoolExecutor executor;
    private int corePoolSize;//核心线程池，表示能够同时执行的任务数量
    private int maximumPoolSize;//最大线程池，注意，它是包含了核心线程池数量的
    private long keepAliveTime = 1;//存活时间,表示的是最大线程池中等待任务的存活时间
    private TimeUnit unit = TimeUnit.HOURS;//时间单位

    //指定线程数量的线程池
    public ThreadPoolManager(int coolPoolSize) {
        corePoolSize = coolPoolSize;
        maximumPoolSize = corePoolSize;//虽然maximumPoolSize用不到，但是也要赋值，并且赋值要不能比corePoolSize小

        executor = new ThreadPoolExecutor(
                corePoolSize,//
                maximumPoolSize,//,当缓冲队列满之后会放入最大线程池等待,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<Runnable>(),//缓冲队列,核心线程池满之后会往缓冲队列中添加等待的任务
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());//最大线程池满之后的处理策略，默认是拒绝执行

    }

    //默认线程池
    public ThreadPoolManager() {
        //核心线程池数量计算方法：设备可用处理器核心数*2 + 1， 能够让cpu效率得到最大发挥的数量
        corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        maximumPoolSize = corePoolSize;//虽然maximumPoolSize用不到，但是也要赋值，并且赋值要不能比corePoolSize小

        executor = new ThreadPoolExecutor(
                corePoolSize,//
                maximumPoolSize,//,当缓冲队列满之后会放入最大线程池等待,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<Runnable>(),//缓冲队列,核心线程池满之后会往缓冲队列中添加等待的任务
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());//最大线程池满之后的处理策略，默认是拒绝执行

    }

    /**
     * 往线程池中添加任务
     *
     * @param task
     */
    public void execute(Runnable task) {
        if (task == null) return;

        executor.execute(task);
    }

    /**
     * 从线程池中移除任务
     *
     * @param task
     */
    public void remove(Runnable task) {
        if (task == null) return;

        executor.remove(task);
    }
}
