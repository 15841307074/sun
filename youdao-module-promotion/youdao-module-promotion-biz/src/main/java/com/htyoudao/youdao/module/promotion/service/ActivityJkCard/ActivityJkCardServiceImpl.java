package com.htyoudao.youdao.module.promotion.service.ActivityJkCard;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkCard.ActivityJkCardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrize.ActivityJkPrizeMapper;
import com.htyoudao.youdao.module.promotion.service.ActivityJkPrize.ActivityJkPrizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class ActivityJkCardServiceImpl extends
        ServiceImpl<ActivityJkCardMapper, ActivityJkCardDO> implements ActivityJkCardService {
}
