package com.htyoudao.youdao.module.order.core.submit.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.mq.rabbitmq.enums.RabbitMQConstant;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.strategy.AbstractIOrderSubmitStrategy;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderSourceEnum;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants.*;

/**
 * <p>
 * 付款单，小程序付款单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class XSeckillOrderStrategy extends AbstractIOrderSubmitStrategy<SubmitReqVO> {

    @DubboReference(timeout = 5000)
    private CommodityApi commodityApi;
    private DefaultRedisScript<List> seckillScript;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public OrderSourceEnum getSource() {
        return OrderSourceEnum.SECKILL_ORDER;
    }

    @PostConstruct
    public void loadLuaScript() throws Exception {
        String script = cn.hutool.core.io.resource.ResourceUtil.readUtf8Str("lua/seckill.lua");
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setScriptText(script);
        seckillScript.setResultType(List.class);
    }


    @Override
    protected void validateSpecificBefore(SubmitReqVO reqVO) {
    }

    @Override
    protected void validateSpecificAfter(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        super.validateSpecificAfter(reqVO, cacheData);

        //门店未在堂食营业时间段内
        if (ObjectUtils.isEmpty(cacheData.getStoreHours()) || !com.htyoudao.youdao.framework.common.util.date.DateUtils.isBusinessOpen(Arrays.asList(cacheData.getStoreHours().split(",")))) {
            throw exception(ORDER_T_STORE_NOT_WORK);
        }

        //小程序门店状态（0 正常营业 1  闭店）
        if (OrderConstants.YES.equals(cacheData.getMiniproStatus())) {
            throw exception(ORDER_STORE_NOT_MINIPRO);
        }

        CalculateCacheDataV2DTO.SeckillInfo seckillInfo = cacheData.getSeckillInfo();
        List<String> keys = new ArrayList<>(
                Arrays.asList(
                        String.format(SECKILL_CONCURRENT_KEY, seckillInfo.getActivityId(), seckillInfo.getSessionId()),
                        String.format(SECKILL_QUEUE_KEY, seckillInfo.getActivityId(), seckillInfo.getSessionId()),
                        String.format(SECKILL_STORE_BUY_KEY, seckillInfo.getActivityId(), seckillInfo.getSessionId(), reqVO.getStoreId())
                )
        );
        cacheData.getCommodityInfos().forEach(commodityInfo -> {
            keys.add(String.format(SECKILL_STOCK_KEY, seckillInfo.getActivityId(), cacheData.getStoreId(), commodityInfo.getCommodityId(), seckillInfo.getSessionId()));
            keys.add(String.format(SECKILL_USER_BUY_KEY, seckillInfo.getActivityId(), seckillInfo.getSessionId(), commodityInfo.getCommodityId(), cacheData.getMemberId()));
        });

        Object seckill = redisTemplate.opsForValue().get(String.format(SECKILL_ACTIVITY_KEY, seckillInfo.getActivityId()));
        if (ObjectUtils.isEmpty(seckill)) {
            throw exception(ORDER_GET_ACTIVITY_FAIL);
        }

        List<CalculateCacheDataV2DTO.CommodityInfoVO> commoditylist = cacheData.getCommodityInfos().stream().filter(commodityInfo -> commodityInfo.getIsPurchase() == 0).toList();
        List<String> args = Arrays.asList(
                // TTL 到活动结束（秒）
                String.valueOf(DateUtils.getSecondsToMidnight()),
                // 当前时间 H
                String.valueOf(LocalTime.now().getHour()),
                // 场次开始时间 H
                seckillInfo.getSessionId().toString(),
                // TODO 备用
                String.valueOf(2),
                // 最大并发数
                String.valueOf(500),
                // 并发锁过期时间（秒）
                "2",
                // 等待人数阈值
                "10",
                // 当前用户ID
                String.valueOf(cacheData.getMemberId()),
                // 当前门店ID
                String.valueOf(cacheData.getStoreId()),
                // 商品数量 N
                String.valueOf(commoditylist.size()),
                // SKU ID 数组(JSON)
                JSON.toJSONString(commoditylist.stream().map(CalculateCacheDataV2DTO.CommodityInfoVO::getCommodityId).toArray()),
                // 对应购买数量数组(JSON)
                JSON.toJSONString(commoditylist.stream().map(CalculateCacheDataV2DTO.CommodityInfoVO::getCopies).toArray()),
                // 秒杀活动
                seckill.toString(),
                //商品信息
                JSON.toJSONString(commoditylist)
        );

        List<Object> result;
        try {
            result = stringRedisTemplate.execute(seckillScript, keys, args.toArray());
            if (ObjectUtils.isEmpty(result)) {
                throw exception(ORDER_SECKILL_ORDER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw exception(ORDER_SECKILL_ORDER_ERROR);
        }

        int code = Integer.parseInt(result.get(0).toString());
        String msg = result.get(1).toString();
        if (code != 0) {
            throw new ServiceException(code, msg);
        }
    }

    @Override
    protected BzOrderDO buildBzOrderDO(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheData) {
        BzOrderDO bzOrderDO = super.buildBzOrderDO(reqVO, cacheData);
        bzOrderDO.setLockState(OrderConstants.YES);
        if (OrderConstants.NO.equals(cacheData.getStoreWithoutPayment()) || cacheData.getPayAmount().compareTo(BigDecimal.ZERO) == 0) {
            bzOrderDO.setPickUpNum(super.getPickUpCode(reqVO.getStoreId()));
            //现金单直接置为制作中
            bzOrderDO.setOrderState(OrderStateEnum.MAKING.getCode());
        }

        return bzOrderDO;
    }

    public static void main(String[] args) {
        int hour = LocalTime.now().getHour(); // 当前系统时区小时（0~23）
        System.out.println("当前小时：" + hour);
    }

    @Override
    protected String getPickUpCode(Long storeId) {
        return super.getPickUpCode(storeId);
    }

    @Override
    @Transactional
    public SubmitResVO submit(SubmitReqVO reqVO) throws Exception {
        SubmitResVO result = super.submit(reqVO);

        // TODO 尽情发挥...

        return result;
    }

    @Override
    protected void addTask(BzOrderDO bzOrderDO, CalculateCacheDataV2DTO cacheData) {
        bzOrderService.addOrderTimerTask(bzOrderDO, this.getValueByNow(cacheData.getPeakHours()));
        bzOrderService.cancelCouponTimeTask(bzOrderDO, 3);
    }

    @Override
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        super.postProcess(reqVO, cacheDataDTO, bzOrderDO);
        //使用优惠券
        if (!ObjectUtils.isEmpty(cacheDataDTO.getUserCouponId())) {
            super.usedCoupon(cacheDataDTO);
        }
        // 清除缓存
        stringRedisTemplate.delete(reqVO.getCacheKey());

        if (OrderConstants.NO.equals(cacheDataDTO.getStoreWithoutPayment()) || cacheDataDTO.getPayAmount().compareTo(BigDecimal.ZERO) == 0) {
            strongExecutor.submit(() -> {
                OrderDetailDTO detail = super.getOrderDetail(cacheDataDTO, bzOrderDO);

                try {
                    log.info("==> 【秒杀不付款下单】扣减原材料库存 {}", bzOrderDO.getOrderSn());
                    bzOrderService.changeStock(bzOrderDO, detail, StockChangeEnum.SALE);
                } catch (Exception e) {
                    log.error("==> 【秒杀不付款下单】扣减原材料库存 失败 {}", bzOrderDO.getOrderSn(), e);
                }
            });

            weakExecutor.submit(() -> {
                //发MQ
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, cacheDataDTO.getStoreId()), "", cacheDataDTO.getOrderSn());

                try {
                    log.info("==> 【秒杀不付款下单】kafka发消息 {}", bzOrderDO.getOrderSn());
                    bzOrderService.notifyOrder(bzOrderDO.getOrderSn(), "INSERT");
                } catch (Exception e) {
                    log.error("==> 【秒杀不付款下单】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
                }

                try {
                    log.info("==> 【秒杀不付款下单】缓存商品销量+ orderSn {}", cacheDataDTO.getOrderSn());
                    cacheDataDTO.getCommodityInfos().forEach(commodityInfo -> {
                        if (commodityInfo.getCommodityId() == null) {
                            return;
                        }
                        if (OrderConstants.YES.equals(commodityInfo.getIsPurchase())) {
                            bzOrderService._incrementPurchaseSales(commodityInfo.getCommodityId(), false);
                        } else {
                            bzOrderService._incrementProductSales(commodityInfo.getCommodityId(), commodityInfo.getCopies(), false);
                        }
                    });
                } catch (Exception e) {
                    log.error("==> 【秒杀不付款下单】缓存商品销量+ 失败 orderSn {}", cacheDataDTO.getOrderSn(), e);
                }
            });
        }
    }
}
