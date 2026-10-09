package com.htyoudao.youdao.module.promotion.service.activityAnswer;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRecordDetailMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRewardLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 有奖问答异步导出查询服务，确保异步线程使用分库数据源。
 */
@Service
@DS(DsNameConstants.SHARDING)
public class ActivityAnswerExportQueryService {

    @Resource
    private ActivityAnswerRecordMapper recordMapper;

    @Resource
    private ActivityAnswerRecordDetailMapper recordDetailMapper;

    @Resource
    private ActivityAnswerRewardLogMapper rewardLogMapper;

    /**
     * 分页查询答题记录。
     */
    public Page<ActivityAnswerRecordDO> selectRecordPage(Page<ActivityAnswerRecordDO> page,
                                                         LambdaQueryWrapperX<ActivityAnswerRecordDO> wrapper) {
        return recordMapper.selectPage(page, wrapper);
    }

    /**
     * 根据答题记录 ID 查询题目明细。
     */
    public List<ActivityAnswerRecordDetailDO> selectRecordDetails(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyList();
        }
        return recordDetailMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRecordDetailDO>()
                .in(ActivityAnswerRecordDetailDO::getRecordId, recordIds)
                .orderByAsc(ActivityAnswerRecordDetailDO::getCreateTime)
                .orderByAsc(ActivityAnswerRecordDetailDO::getId));
    }

    /**
     * 分页查询奖励发放记录。
     */
    public Page<ActivityAnswerRewardLogDO> selectRewardLogPage(Page<ActivityAnswerRewardLogDO> page,
                                                               LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper) {
        return rewardLogMapper.selectPage(page, wrapper);
    }

    /**
     * 根据 ID 查询答题记录，用于补充会员昵称。
     */
    public List<ActivityAnswerRecordDO> selectRecordsByIds(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyList();
        }
        return recordMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .in(ActivityAnswerRecordDO::getId, recordIds));
    }
}
