package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyUserReportDO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.PlatformMessageDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.AnomalyUserReportMapper;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.PlatformMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台消息查询服务
 *
 * <p>给 App 3 个接口提供数据：平台消息分页、异常报告分页、标记已读
 */
@Service
@Slf4j
public class PlatformMessageService {

    @Resource
    private PlatformMessageMapper messageMapper;
    @Resource
    private AnomalyUserReportMapper reportMapper;

    /**
     * App 接口 1：平台消息分页（只查自己的，按创建时间倒序）
     */
    public PageResult<PlatformMessageDO> getPlatformMessagePage(Long userId, PageParam pageParam, Integer messageType) {
        return messageMapper.selectPage(pageParam, userId, messageType);
    }

    /**
     * App 接口 2：经营异常报告分页（只查自己的，按周期倒序）
     */
    public PageResult<AnomalyUserReportDO> getAnomalyReportPage(Long userId, PageParam pageParam, Integer anomalyType) {
        return reportMapper.selectPage(pageParam, userId, anomalyType);
    }

    /**
     * App 接口 3：标记已读
     *
     * <p>只能标记自己的消息；已读的保持首次已读时间不变
     *
     * @param userId 当前登录用户ID
     * @param ids    消息ID列表
     */
    public void markRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        int updated = messageMapper.update(null, new LambdaUpdateWrapper<PlatformMessageDO>()
                .eq(PlatformMessageDO::getUserId, userId)
                .in(PlatformMessageDO::getId, ids)
                .eq(PlatformMessageDO::getReadStatus, false)
                .set(PlatformMessageDO::getReadStatus, true)
                .set(PlatformMessageDO::getReadTime, LocalDateTime.now()));
        log.info("[markRead] userId={} ids={} 更新 {} 条", userId, ids, updated);
    }
}
