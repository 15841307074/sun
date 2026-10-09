package com.htyoudao.youdao.module.promotion.service.exchangelog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogExcel;
import com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo.ExchangeLogPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;

import java.util.List;

/**
 * 活动的兑换记录 Service 接口
 *
 * @author lzw
 */
public interface ExchangeLogService extends IService<ActivityExchangeLogDO> {

    /**
     * 获得活动的兑换记录
     *
     * @param id 编号
     * @return 活动的兑换记录
     */
    ActivityExchangeLogDO getExchangeLog(Long id);

    /**
     * 获得活动的兑换记录分页
     *
     * @param pageReqVO 分页查询
     * @return 活动的兑换记录分页
     */
    PageResult<ActivityExchangeLogDO> getExchangeLogPage(ExchangeLogPageReqVO pageReqVO);

    /**
     * 获得会员数
     * @param pageReqVO
     * @return
     */
    Integer getMemberNum(ExchangeLogPageReqVO pageReqVO);

    /**
     * 插入
     * @param activityExchangeLogDO
     */
    void insert(ActivityExchangeLogDO activityExchangeLogDO);

    /**
     * 导出的查询
     * @param activityExchangeLogDO
     */
    List<ExchangeLogExcel> exportList(ExchangeLogPageReqVO activityExchangeLogDO);

    /**
     * 导出
     * @param activityExchangeLogDO
     */
    void export(ExchangeLogPageReqVO activityExchangeLogDO);
}