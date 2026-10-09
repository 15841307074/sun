package com.htyoudao.youdao.module.promotion.service.ActivityJkPrize;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrize.ActivityJkPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkCommodity.ActivityJkCommodityMapper;
import com.htyoudao.youdao.module.promotion.service.activityJkCommodity.ActivityJkCommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class ActivityJkPrizeServiceImpl extends
        ServiceImpl<ActivityJkPrizeMapper, ActivityJkPrizeDO> implements ActivityJkPrizeService {
}
