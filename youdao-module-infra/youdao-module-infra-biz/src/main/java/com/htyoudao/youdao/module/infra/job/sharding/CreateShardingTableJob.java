package com.htyoudao.youdao.module.infra.job.sharding;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.tenant.core.aop.TenantIgnore;
import com.htyoudao.youdao.module.infra.service.sharding.CreateTableService;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建分片表 job
 *
 * @author liuzhaowang
 */
@Component
@Slf4j
public class CreateShardingTableJob {

    @Resource
    private CreateTableService createTableService;

    @XxlJob("createShardingTableJob")
    @TenantIgnore
    @DataPermission(enable = false)
    public void createShardingTableJob() {
        createTableService.createShardingTable();
        log.info("[createShardingTableJob][定时创建分表成功]");
    }

}
