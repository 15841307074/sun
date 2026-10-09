package com.htyoudao.youdao.module.system.job.token;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.tenant.core.aop.TenantIgnore;
import com.htyoudao.youdao.module.system.service.oauth2.OAuth2TokenService;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物理删除过期的token
 *
 * @author lqman
 */
@Slf4j
@Component
public class TokenCleanJob {

    @Resource
    private OAuth2TokenService oAuth2TokenService;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 1000;

    @XxlJob("tokenCleanJob")
    @TenantIgnore
    @DataPermission(enable = false)
    public void execute() {
        Integer count = oAuth2TokenService.cleanToken(DELETE_LIMIT);
        log.info("[execute][定时执行清理token数量 ({}) 个]", count);
    }

}
