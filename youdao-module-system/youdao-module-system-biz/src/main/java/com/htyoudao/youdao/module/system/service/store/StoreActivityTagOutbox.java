package com.htyoudao.youdao.module.system.service.store;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityStoreTagUpdateDTO;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** 门店事务只保存通知；提交后仍调用原有活动更新接口，失败由定时任务重试。 */
@Service
@DS("master")
@EnableScheduling
@Slf4j
public class StoreActivityTagOutbox {
    /** 每次扫描的待同步通知数，避免一次占用过多数据库连接。 */
    private static final int DISPATCH_BATCH_SIZE = 32;
    /** 提交后立即尝试的队列容量；满时交给定时任务重试。 */
    private static final int IMMEDIATE_QUEUE_CAPACITY = 256;
    /** 失败后等待再次调用营销服务的秒数。 */
    private static final int RETRY_DELAY_SECONDS = 10;
    /** 防止多个实例同时处理同一通知的租约秒数。 */
    private static final int LEASE_SECONDS = 30;
    /** 处理提交后通知的线程数。 */
    private static final int IMMEDIATE_WORKERS = 2;
    /** 定时补偿扫描间隔（毫秒）。 */
    private static final long POLL_INTERVAL_MS = 2000;
    private final ThreadPoolExecutor immediate = new ThreadPoolExecutor(IMMEDIATE_WORKERS, IMMEDIATE_WORKERS, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(IMMEDIATE_QUEUE_CAPACITY), runnable -> {
                Thread thread = new Thread(runnable, "store-activity-tag-sync");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.DiscardPolicy());
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private ObjectMapper json;
    @Resource
    private StoreActivityTagCache cache;
    @DubboReference(timeout=3000,retries=0) private ActivityApi activities;

    /** 与门店标签变更同事务写入通知，避免远程调用失败时丢失变更。 */
    public void enqueue(List<ActivityStoreTagUpdateDTO> changes,CouponStoreTagSqlTypeEnum type) {
        List<Long> ids = new ArrayList<>();
        for(var change:changes)try {
            long id=IdWorker.getId();
            jdbc.update("INSERT INTO system_activity_tag_outbox(id,business_id,store_id,operation,payload) VALUES(?,?,?,?,?)",id,BusinessContextHolder.getRequiredBusinessId(),change.getStoreId(),type.name(),json.writeValueAsString(change));
            ids.add(id);
        } catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalArgumentException(e);}
        Runnable publish=() -> ids.forEach(id -> immediate.execute(() -> dispatchOne(id)));
        if(TransactionSynchronizationManager.isActualTransactionActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { publish.run(); }
            });
        else publish.run();
    }

    /** 扫描处理提交后未成功同步的通知；立即尝试失败也由这里接管。 */
    @Scheduled(fixedDelay=POLL_INTERVAL_MS)
    public void dispatch() {
        try {
            for(Map<String,Object> row:jdbc.queryForList("SELECT id FROM system_activity_tag_outbox WHERE done=0 AND next_at<=NOW(3) ORDER BY id LIMIT ?",DISPATCH_BATCH_SIZE))
                dispatchOne(((Number)row.get("id")).longValue());
        }catch(Exception e){log.warn("门店标签通知扫描异常：{}",e.getClass().getSimpleName());}
    }

    /** 只调用原有活动更新方法，Redis 标签先从已提交的门店关系刷新。 */
    private void dispatchOne(long id) {
        Long prior=BusinessContextHolder.getBusinessId();
        DynamicDataSourceContextHolder.push("master");
        try {
            List<Map<String,Object>> rows=jdbc.queryForList("SELECT business_id,store_id,operation,payload FROM system_activity_tag_outbox WHERE id=? AND done=0 AND next_at<=NOW(3)",id);
            if(rows.isEmpty())return;
            Map<String,Object> row=rows.get(0);
            long b=((Number)row.get("business_id")).longValue(),store=((Number)row.get("store_id")).longValue();
            String token=UUID.randomUUID().toString();
            if(jdbc.update("UPDATE system_activity_tag_outbox SET lease_token=?,next_at=DATE_ADD(NOW(3),INTERVAL ? SECOND) WHERE id=? AND done=0 AND next_at<=NOW(3)",token,LEASE_SECONDS,id)!=1)return;
            try {
                BusinessContextHolder.setBusinessId(b);
                cache.refresh(b,store);
                ActivityStoreTagUpdateDTO update=json.readValue((String)row.get("payload"),ActivityStoreTagUpdateDTO.class);
                activities.updateActivityStoreByTagIdAndStoreId(List.of(update),CouponStoreTagSqlTypeEnum.valueOf((String)row.get("operation")));
                jdbc.update("UPDATE system_activity_tag_outbox SET done=1,lease_token=NULL WHERE id=? AND lease_token=?",id,token);
            } catch(Exception e) {
                jdbc.update("UPDATE system_activity_tag_outbox SET attempts=attempts+1,next_at=DATE_ADD(NOW(3),INTERVAL ? SECOND),lease_token=NULL WHERE id=? AND lease_token=?",RETRY_DELAY_SECONDS,id,token);
                log.warn("门店标签活动同步待重试，通知ID={}",id);
            }
        }catch(Exception e){log.warn("门店标签通知处理异常，通知ID={}",id,e);}
        finally{
            BusinessContextHolder.setBusinessId(prior);
            DynamicDataSourceContextHolder.poll();
        }
    }

    /** 停机时停止立即分发线程，未完成通知仍由数据库保留。 */
    @PreDestroy public void stop() { immediate.shutdown(); }
}
