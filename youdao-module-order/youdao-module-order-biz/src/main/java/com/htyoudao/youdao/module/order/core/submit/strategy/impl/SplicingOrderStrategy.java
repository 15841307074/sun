package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.client.DTO.SyncCommodityDTO;
import com.htyoudao.youdao.module.order.client.SplicingNettyClient;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.SplicingOrderConfigDTO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzSplicingOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.SplicingOrderStateEnum;
import com.htyoudao.youdao.module.order.service.order.IBzSplicingOrderServcie;
import com.htyoudao.youdao.module.system.api.sysconfig.SysConfigApi;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Component
public class SplicingOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @DubboReference
    private SysConfigApi sysConfigApi;

    @Resource
    private IBzSplicingOrderServcie iBzSplicingOrderServcie;

    @Resource
    private SplicingNettyClient splicingNettyClient;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.SPLICING_ORDER;
    }

    @Override
    protected void validateSpecificBefore(SubmitReqVO reqVO) {
//        super.checkRepeatToken(reqVO);
    }

    @Override
    protected void validateSpecificAfter(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        super.validateSpecificAfter(reqVO, cacheData);

        //门店未在堂食营业时间段内
        if (ObjectUtils.isEmpty(cacheData.getStoreHours()) || !com.htyoudao.youdao.framework.common.util.date.DateUtils.isBusinessOpen(Arrays.asList(cacheData.getStoreHours().split(",")))) {
            throw exception(ORDER_T_STORE_NOT_WORK);
        }

        if (ObjectUtils.isEmpty(cacheData.getMainId())) {
            throw exception(ORDER_SPLICING_MAIN_ID_ERROR);
        }

        if (!ObjectUtils.isEmpty(cacheData.getUserCouponId())) {
            throw exception(ORDER_SPLICING_NOT_ALLOW_COUPON);
        }

        BzSplicingOrderDO mainObj = iBzSplicingOrderServcie.getOne(new LambdaQueryWrapper<BzSplicingOrderDO>().eq(BzSplicingOrderDO::getMainId, cacheData.getMainId()).eq(BzSplicingOrderDO::getOpenId, cacheData.getOpenId()));
        if (mainObj.getStatus() == 0) {
            throw exception(ORDER_SPLICING_ONLY_ONE_ERROR);
        }
        if (mainObj.getStatus() == 2) {
            throw exception(ORDER_SPLICING_CANCELED_ERROR);
        }
        if (mainObj.getStatus() == 3) {
            throw exception(ORDER_SPLICING_DONE_ERROR);
        }
    }

    @Override
    protected String getPickUpCode(Long storeId) {
        return super.getPickUpCode(storeId);
    }

    @Override
    protected BzOrderDO buildBzOrderDO(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);

//        DiscountResultDTO discountResult = AmountUtil.calculateDiscount(bzOrderDO.getOrderAmount(), this.getSplicingOrderDiscount());
//        //满减存储到优惠活动中
//        bzOrderDO.setPromotionDiscountAmount(discountResult.getDiscountAmount());
//        bzOrderDO.setOrderAmount(discountResult.getFinalAmount());
        bzOrderDO.setExpressCode(cacheData.getMainId());
        return bzOrderDO;
    }

    /**
     * 获取拼单优惠规则
     *
     * @return
     */
    private String getSplicingOrderDiscount() {
        CommonResult<Map<String, String>> commonResult = sysConfigApi.getBykeys(Collections.singletonList(OrderConstants.SPLICING_ORDER_CONFIG));
        Map<String, String> configMap = commonResult.getData();
        String configValueJson = configMap.get(OrderConstants.SPLICING_ORDER_CONFIG);
        if (ObjectUtils.isEmpty(configValueJson)) {
            return "0#0";
        }
        return JSON.parseObject(configValueJson, SplicingOrderConfigDTO.class).getDiscount();
    }


    @Override
    @Transactional
    public SubmitResVO submit(SubmitReqVO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO 尽情发挥...

        return result;
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        super.postProcess(reqVO, cacheDataDTO, bzOrderDO);

        //结束拼单
        iBzSplicingOrderServcie.update(
                new LambdaUpdateWrapper<BzSplicingOrderDO>()
                        .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.DONE.getCode())
                        .eq(BzSplicingOrderDO::getMainId, cacheDataDTO.getMainId())
        );
        //通知netty下发消息
        splicingNettyClient.finishOrderNotifyAll(SyncCommodityDTO.builder().mainId(cacheDataDTO.getMainId()).openId(cacheDataDTO.getOpenId()).build());

        // 清除缓存
        stringRedisTemplate.delete(reqVO.getCacheKey());
    }
}
