package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

/** 仅提交有界任务；失败保留持久化记录供人工恢复，不做空闲扫描。 */
@Component
@Slf4j
public class LotteryCacheExecutor {
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64),
            job->{Thread t=new Thread(job,"lottery-cache-publish");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    public void submit(Runnable job){
        String source=DynamicDataSourceContextHolder.peek();
        try {worker.execute(()->{
            if(source!=null)DynamicDataSourceContextHolder.push(source);
            try {job.run();}
            finally {if(source!=null)DynamicDataSourceContextHolder.poll();}
        });}catch(RejectedExecutionException ignored){log.warn("Lottery cache executor full/stopped; pending publication record retained");}
    }
    @PreDestroy public void stop(){worker.shutdown();}
}
