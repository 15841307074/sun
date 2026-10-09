package com.htyoudao.youdao.module.promotion.service.market;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.market.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.market.SmsMarketDO;

import java.util.List;

/**
 * @author dht
 */
public interface SmsMarketService extends IService<SmsMarketDO> {

    /**
     * 分页查询
     * @param smsMarket smsMarket
     * @return PageResult<SmsMarketRespVO>
     */
    PageResult<SmsMarketRespVO> selectSmsMarketListPage(SmsMarketReqVO smsMarket);

    /**
     * 根据id查询
     * @param id id
     * @return SmsMarketRespVO
     */
    SmsMarketRespVO selectById(Long id);

    /**
     * 获取模版列表
     * @return List<SmsTemplateRespVO>
     */
    List<SmsTemplateRespVO> getTemplateList();

    /**
     * 新增
     * @param smsMarket smsMarket
     * @return Boolean
     */
    Boolean insert(SmsMarketSaveReqVO smsMarket);

    /**
     * 修改
     * @param smsMarket smsMarket
     * @return Boolean
     */
    Boolean edit(SmsMarketSaveReqVO smsMarket);

    /**
     * 复制
     * @param id id
     * @return Boolean
     */
    Boolean copy(Long id);

    /**
     * 发送短信
     * @param sendMessage sendMessage
     * @return Boolean
     */
    Boolean sendMessage(SmsSendMessageReqVO sendMessage);

    /**
     * 定时发送 --  后端用
     * @return Boolean
     */
    Boolean scheduledSend();

    /**
     * 预览发送
     * @param preMessage preMessage
     * @return Boolean
     */
    Boolean preSend(PreMessageReqVO preMessage);

    /**
     * 获取Pv
     * @param id id
     * @return SmsMarketPageViewVO
     */
    SmsMarketPageViewVO getPv(Long id);

    /**
     * 进入页面,增加pv
     * @param reqVO reqVO
     * @return Boolean
     */
    Boolean insertPv(AddPvReqVO reqVO);

    /**
     * 领取优惠券包
     * @param reqVO reqVO
     * @return  Boolean
     */
    Boolean claimCouponPackage(ClaimCouponPackageReqVO reqVO);

    /**
     * 测试发送短信
     * @param preMessage preMessage
     * @return Boolean
     */
    Boolean testSend(PreMessageReqVO preMessage);
}
