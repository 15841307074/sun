package com.htyoudao.youdao.module.analysis.util;

import java.util.concurrent.*;

public class ParallelUtil {
    private static final ExecutorService POOL =
            new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2000));

    public static <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, POOL);
    }
}

