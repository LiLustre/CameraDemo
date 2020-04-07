package com.zmy.camerademo.threadpoll;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 执行单次任务线程池
 *
 * @author lizhen
 */
public class OneShotThreadPool {
    public static final int CORE_POOL_SIZE = 1;
    public static final int MAXI_MUM_POOL_SIZE = 1;
    public static final int KEEP_ALIVE_TIME = 0;
    public static final TimeUnit UNIT = TimeUnit.SECONDS;
    public static final int WORK_QUEUE_COUNT = 3;

    private static final BlockingQueue<Runnable> BLOCKING_QUEUE =
            new ArrayBlockingQueue<Runnable>(WORK_QUEUE_COUNT);

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "oneShot:pool-" + "thread-" + threadNumber.getAndIncrement());
        }
    };

    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, UNIT, BLOCKING_QUEUE, THREAD_FACTORY,
            new ThreadPoolExecutor.DiscardPolicy());

    public static void addTask(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }
}
