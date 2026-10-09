package com.htyoudao.youdao.module.commodity.job;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySyncTaskMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物理删除 180 天前的同步记录的 Job
 *
 * @author lqman
 */
@Slf4j
@Component
public class SyncTaskCleanJob {


    @Resource
    private CommoditySyncTaskMapper taskMapper;

    /**
     * 清理超过（180）天的日志
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 180;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 100;

    @XxlJob("syncTaskCleanJob")
    @DataPermission(enable = false)
    public void execute() {
        int count = 0;
        LocalDateTime expireDate = LocalDateTime.now().minusDays(JOB_CLEAN_RETAIN_DAY);
        // 循环删除，直到没有满足条件的数据
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = taskMapper.deleteByCreateTimeLt(expireDate, DELETE_LIMIT);
            count += deleteCount;
            // 达到删除预期条数，说明到底了
            if (deleteCount < DELETE_LIMIT) {
                break;
            }
        }

        log.info("[execute][定时执行清理错误日志数量 ({}) 个]", count);
    }

}
