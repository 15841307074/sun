package com.htyoudao.youdao.module.order.service.order;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderLogDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderMapper;
import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_GET_ACTIVITY_FAIL;
import static com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants.*;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-08-22
 */
@Slf4j
@Service
public class ProxyService {

    @Resource
    private BzOrderProductService bzOrderProductService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BzOrderMapper bzOrderMapper;
    @Resource
    private BzOrderLogService bzOrderLogService;

    /**
     * 更新订单信息
     *
     * @param tableFix
     * @param bzOrderDO
     */
    public void updateOrderAsync(String tableFix, BzOrderDO bzOrderDO) {

        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderLogDO.setOrderStateLog(this.getOrderLogState(bzOrderDO));
        bzOrderLogDO.setLogContent(this.getOrderLogContent(bzOrderDO));
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogDO.setLogUserId(bzOrderDO.getMemberId());
        bzOrderLogDO.setCreator(bzOrderDO.getCreator());
        bzOrderLogDO.setBusinessId(bzOrderDO.getBusinessId());
        bzOrderLogService.save(bzOrderLogDO);

        //秒杀单回退库存
        this.rollbackSeckill(bzOrderDO);

        bzOrderMapper.updateByOrderId(tableFix, bzOrderDO);
    }

    private Integer getOrderLogState(BzOrderDO order) {
        return order.getOrderState();
    }

    private String getOrderLogContent(BzOrderDO order) {
        return OrderStateEnum.getMessageByCode(order.getOrderState());
    }

    /**
     * 秒杀单回退库存
     *
     * @param bzOrderDO
     */
    public void rollbackSeckill(BzOrderDO bzOrderDO) {
        if (bzOrderDO.getLockState() == 1 && (OrderStateEnum.PENDING_REFUND.getCode() == bzOrderDO.getOrderState() || OrderStateEnum.CANCELED.getCode() == bzOrderDO.getOrderState())) {

            List<BzOrderProductDO> productDOList = bzOrderProductService.list(new LambdaQueryWrapper<BzOrderProductDO>().eq(BzOrderProductDO::getOrderSn, bzOrderDO.getOrderSn()));
            if(CollectionUtils.isEmpty(productDOList)){
                log.error(">>> 秒杀单取消/退款，查询订单商品信息为空！ orderSn| {}", bzOrderDO.getOrderSn());
                return;
            }

            String seckill = stringRedisTemplate.opsForValue().get(String.format(SECKILL_ACTIVITY_KEY, productDOList.get(0).getActivityId()));
            if (ObjectUtils.isEmpty(seckill)) {
                log.error(">>> 秒杀单取消/退款，查询活动数据缓存失败！ orderSn| {}", bzOrderDO.getOrderSn());
                return;
            }
            JSONObject seckillJsonObject = JSON.parseObject(seckill, JSONObject.class);

            //门店限制
            Object storeLimitCountObj = seckillJsonObject.get("storeLimitCount");
            int storeLimitCount = storeLimitCountObj == null ? 0 :Integer.parseInt(storeLimitCountObj.toString());

            productDOList.forEach(productDO -> {
                JSONObject seckillInfo = JSON.parseObject(productDO.getActivityDiscountDetail(), JSONObject.class);
                String activityId = seckillInfo.get("activityId").toString();
                String sessionId = seckillInfo.get("sessionId").toString();

                if(storeLimitCount > 0){
                    //门店购买订单数
                    String storeBuyCountKey = String.format(SECKILL_STORE_BUY_KEY, activityId, sessionId, bzOrderDO.getStoreId());
                    //门店累计订单数减一
                    stringRedisTemplate.opsForValue().decrement(storeBuyCountKey);
                }

                //库存
                String stockKey = String.format(SECKILL_STOCK_KEY, activityId, bzOrderDO.getStoreId(), productDO.getCommodityId(), sessionId);
                //用户已购买数量
                String userBuyCountKey = String.format(SECKILL_USER_BUY_KEY, activityId, sessionId, productDO.getCommodityId(), bzOrderDO.getMemberId());

                //库存加
                stringRedisTemplate.opsForValue().increment(stockKey, productDO.getGoodsNum());
                //用户已购买数量减
                stringRedisTemplate.opsForValue().decrement(userBuyCountKey, productDO.getGoodsNum());
            });
        }
    }
}
