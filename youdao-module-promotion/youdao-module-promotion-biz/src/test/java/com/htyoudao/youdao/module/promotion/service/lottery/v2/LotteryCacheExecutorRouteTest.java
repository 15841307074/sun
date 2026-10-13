package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LotteryCacheExecutorRouteTest {
    @Test void cachePublicationPreservesTheCallerRouteWithoutLeakingItToTheNextJob() throws Exception {
        var executor=new LotteryCacheExecutor();var first=new CompletableFuture<String>();var next=new CompletableFuture<String>();
        try {
            DynamicDataSourceContextHolder.push("uat-business");
            try {executor.submit(()->first.complete(DynamicDataSourceContextHolder.peek()));}
            finally{DynamicDataSourceContextHolder.poll();}
            assertEquals("uat-business",first.get(1,TimeUnit.SECONDS));
            executor.submit(()->next.complete(DynamicDataSourceContextHolder.peek()));assertNull(next.get(1,TimeUnit.SECONDS));
        }finally{executor.stop();}
    }
}
