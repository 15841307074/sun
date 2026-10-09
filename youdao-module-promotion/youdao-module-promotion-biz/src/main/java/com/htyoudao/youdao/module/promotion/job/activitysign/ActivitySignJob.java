package com.htyoudao.youdao.module.promotion.job.activitysign;

import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.service.activitySign.ActivitySignService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 签到活动定时任务。 */
@Slf4j
@Component
public class ActivitySignJob {

  @Resource private ActivitySignService activitySignService;

  /** 刷新实物奖品超时未填写地址状态：发放超过48小时仍未填写地址的记录改为 prizeState=9。 */
  @XxlJob("activitySignPhysicalTimeoutJobHandler")
  @DataPermission(enable = false)
  public void activitySignPhysicalTimeoutJobHandler() {
    Integer count = activitySignService.refreshPhysicalAddressTimeout();
    log.info("activitySignPhysicalTimeoutJobHandler finished, updatedCount={}", count);
    XxlJobHelper.log("activitySignPhysicalTimeoutJobHandler finished, updatedCount={}", count);
  }
}
