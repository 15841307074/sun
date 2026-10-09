package com.htyoudao.youdao.module.system.job.logger;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.tenant.core.aop.TenantIgnore;
import com.htyoudao.youdao.module.system.service.logger.OperateLogService;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物理删除 N 天前的操作日志的 Job
 *
 * @author lqman
 */
@Slf4j
@Component
public class OperaLogCleanJob {

    @Resource
    private OperateLogService operateLogService;

    /**
     * 清理超过（30）天的日志
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 90;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 100;

    @XxlJob("operateLogCleanJob")
    @TenantIgnore
    @DataPermission(enable = false)
    public void execute() {
        Integer count = operateLogService.cleanOperateLog(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理错误日志数量 ({}) 个]", count);
    }

}
