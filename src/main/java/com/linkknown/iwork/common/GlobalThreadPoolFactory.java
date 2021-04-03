package com.linkknown.iwork.common;

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 全局线程池
 */
public class GlobalThreadPoolFactory {

    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(100, 200, 10, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    public static ThreadPoolExecutor getCommonPool() {
        return executor;
    }
}
