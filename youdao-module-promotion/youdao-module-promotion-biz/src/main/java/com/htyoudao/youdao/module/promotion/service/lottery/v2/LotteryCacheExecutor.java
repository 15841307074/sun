package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

/** 仅提交非阻塞任务；提交失败由持久化任务或过期机制修复。 */
@Component
public class LotteryCacheExecutor {
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64),
            job->{Thread t=new Thread(job,"lottery-cache-publish");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    public void submit(Runnable job){try{worker.execute(job);}catch(RejectedExecutionException ignored){ /* 持久化任务已记录，可由后台重试。 */ }}
    @PreDestroy public void stop(){worker.shutdown();}
}
