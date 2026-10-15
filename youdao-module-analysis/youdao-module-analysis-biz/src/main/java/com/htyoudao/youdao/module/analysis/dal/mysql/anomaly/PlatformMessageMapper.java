package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.PlatformMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 平台消息 Mapper
 */
@Mapper
public interface PlatformMessageMapper extends BaseMapperX<PlatformMessageDO> {

    /**
     * App 接口「平台消息查询」分页：查自己的消息，按创建时间倒序
     *
     * @param pageParam   分页参数
     * @param userId      当前登录用户ID（只查自己的）
     * @param messageType 消息类型（可选筛选，null = 全部类型）
     */
    default PageResult<PlatformMessageDO> selectPage(PageParam pageParam, Long userId, Integer messageType) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PlatformMessageDO>()
                .eq(PlatformMessageDO::getUserId, userId)
                .eqIfPresent(PlatformMessageDO::getMessageType, messageType)
                .orderByDesc(PlatformMessageDO::getCreateTime));
    }

    /**
     * 推送任务用：按「推送状态 + 消息类型 + 周期」捞记录
     *
     * <p>正常调度捞 push_status=0（待推送）；
     * 补偿重跑时捞 push_status=2（推送失败）。
     *
     * @param pushStatus  推送状态
     * @param messageTypes 消息类型集合
     * @param periodStart 周期开始日
     */
    default List<PlatformMessageDO> selectListByPush(Integer pushStatus,
                                                     Collection<Integer> messageTypes,
                                                     LocalDate periodStart) {
        return selectList(new LambdaQueryWrapperX<PlatformMessageDO>()
                .eq(PlatformMessageDO::getPushStatus, pushStatus)
                .in(PlatformMessageDO::getMessageType, messageTypes)
                .eq(PlatformMessageDO::getPeriodStart, periodStart));
    }
}
