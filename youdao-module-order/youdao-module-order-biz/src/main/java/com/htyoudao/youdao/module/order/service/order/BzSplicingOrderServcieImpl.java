package com.htyoudao.youdao.module.order.service.order;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.order.client.DTO.SplicingSessionDTO;
import com.htyoudao.youdao.module.order.client.DTO.SyncCommodityDTO;
import com.htyoudao.youdao.module.order.client.SplicingNettyClient;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.SpBzCarGoodsDTO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SplicingOrderMemberAddReqVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzSplicingOrderDO;
import com.htyoudao.youdao.module.order.dal.mysql.BzSplicingOrderMapper;
import com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.SplicingOrderStateEnum;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-12-24
 */
@Service
public class BzSplicingOrderServcieImpl extends ServiceImpl<BzSplicingOrderMapper, BzSplicingOrderDO> implements IBzSplicingOrderServcie {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SplicingNettyClient splicingNettyClient;

    @Override
    public Map<String, Object> mainSelect(String mainId, String openId, Long storeId) {
        //初始化响应体
        HashMap<String, Object> returnMap = new HashMap<>() {
            @Serial
            private static final long serialVersionUID = 8651565126448563690L;

            {
                put("mainId", mainId);
                put("isCaptain", OrderConstants.YES);
                put("status", SplicingOrderStateEnum.OK.getCode());
            }
        };

        //mainId为空，首页进入，判断是否创建拼单主体
        if (ObjectUtils.isEmpty(mainId)) {
            BzSplicingOrderDO objByOpenId = this.getOne(
                    new LambdaQueryWrapper<BzSplicingOrderDO>()
                            .eq(BzSplicingOrderDO::getOpenId, openId)
                            .eq(BzSplicingOrderDO::getStoreId, storeId)
                            .last(" and (status != " + SplicingOrderStateEnum.CANCELED.getCode() + " and status != " + SplicingOrderStateEnum.DONE.getCode() + ")")
            );
            if (ObjectUtils.isEmpty(objByOpenId)) {

                BzSplicingOrderDO oldMain = this.getOne(
                        new LambdaQueryWrapper<BzSplicingOrderDO>()
                                .eq(BzSplicingOrderDO::getOpenId, openId)
                                .last(" and (status != " + SplicingOrderStateEnum.CANCELED.getCode() + " and status != " + SplicingOrderStateEnum.DONE.getCode() + ")")
                );

                this.createNewMain(openId, storeId, returnMap);
                //将旧的拼单主体置为取消
                this.update(
                        new LambdaUpdateWrapper<BzSplicingOrderDO>()
                                .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.CANCELED.getCode())
                                .eq(BzSplicingOrderDO::getOpenId, openId)
                                .ne(BzSplicingOrderDO::getMainId, returnMap.get("mainId"))
                );

                if(!ObjectUtils.isEmpty(oldMain)){
                    //通知netty下发消息
                    splicingNettyClient.cancelOrderNotifyAll(SyncCommodityDTO.builder().mainId(oldMain.getMainId()).openId(openId).build());
                }
            } else {
                returnMap.put("mainId", objByOpenId.getMainId());
                returnMap.put("status", objByOpenId.getStatus());
            }
        }

        //mainId不为空，重分享链接进
        if (!ObjectUtils.isEmpty(mainId)) {
            //mainId不为空判断是否是队长
            BzSplicingOrderDO objByMainIdAndShopId = this.getOne(
                    new LambdaQueryWrapper<BzSplicingOrderDO>()
                            .eq(BzSplicingOrderDO::getMainId, mainId)
                            .eq(BzSplicingOrderDO::getStoreId, storeId)
            );
            //先判定状态
            if (!ObjectUtils.isEmpty(objByMainIdAndShopId)) {
                if (objByMainIdAndShopId.getStatus().equals(SplicingOrderStateEnum.CANCELED.getCode())) {
                    throw exception(ORDER_SPLICING_CANCELED);
                }

                if (objByMainIdAndShopId.getStatus().equals(SplicingOrderStateEnum.DONE.getCode())) {
                    throw exception(ORDER_SPLICING_DONE);
                }

                //成员
                if (!objByMainIdAndShopId.getOpenId().equals(openId)) {
                    returnMap.put("isCaptain", OrderConstants.NO);
                }

                returnMap.put("status", objByMainIdAndShopId.getStatus());
            }
            //切换了门店
            if (ObjectUtils.isEmpty(objByMainIdAndShopId)) {
                //将旧的拼单主体置为取消
                this.update(
                        new LambdaUpdateWrapper<BzSplicingOrderDO>()
                                .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.CANCELED.getCode())
                                .eq(BzSplicingOrderDO::getMainId, mainId)
                );

                //通知netty下发消息
                splicingNettyClient.cancelOrderNotifyAll(SyncCommodityDTO.builder().mainId(mainId).openId(openId).build());

                BzSplicingOrderDO objByOpenId = this.getOne(
                        new LambdaQueryWrapper<BzSplicingOrderDO>()
                                .eq(BzSplicingOrderDO::getOpenId, openId)
                                .last(" and (status != " + SplicingOrderStateEnum.CANCELED.getCode() + " and status != " + SplicingOrderStateEnum.DONE.getCode() + ")")
                );

                if (!ObjectUtils.isEmpty(objByOpenId)) {
                    returnMap.put("mainId", objByOpenId.getMainId());
                    returnMap.put("status", objByOpenId.getStatus());
                } else {
                    //生成新门店的拼单主体
                    this.createNewMain(openId, storeId, returnMap);
                }
            }
        }
        return returnMap;
    }

    @Override
    public void mainContinue(String mainId, String openId, Long storeId) {
        //主动解锁
        this.update(
                new LambdaUpdateWrapper<BzSplicingOrderDO>()
                        .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.OK.getCode())
                        .eq(BzSplicingOrderDO::getMainId, mainId)
        );

        stringRedisTemplate.opsForValue().set(RedisKeyConstants.SPLICING_LOCK_STATUS + mainId, OrderConstants.NO.toString());

        //通知netty下发消息
        splicingNettyClient.unLockOrderNotifyAll(SyncCommodityDTO.builder().mainId(mainId).openId(openId).build());
    }

    @Override
    public String getCommodity(String mainId, String openId, Long storeId) {
        List<SplicingSessionDTO> sessionList = new ArrayList<>();
        Map<Object, Object> allMemberRedisSessionMap = stringRedisTemplate.opsForHash().entries(RedisKeyConstants.SESSION_KEY_PREFIX + mainId);
        Set<Object> allMemberRedisSessionIds = allMemberRedisSessionMap.keySet();
        for (Object memberRedisSessionId : allMemberRedisSessionIds) {
            Object memberSessionObj = allMemberRedisSessionMap.get(memberRedisSessionId);
            SplicingSessionDTO memberRedisSession = JSON.parseObject(memberSessionObj.toString(), SplicingSessionDTO.class);
            sessionList.add(memberRedisSession);
        }

        //排序给用户推列表
        Collections.sort(sessionList, Comparator.comparing(SplicingSessionDTO::getJoinTimestamp));

        return JSON.toJSONString(sessionList);
    }

    private void createNewMain(String openId, Long shopId, HashMap<String, Object> returnMap) {
        String newMainId = UUID.randomUUID().toString().replace("-", "");
        BzSplicingOrderDO newObj = new BzSplicingOrderDO();
        newObj.setMainId(newMainId);
        newObj.setStatus(SplicingOrderStateEnum.OK.getCode());
        newObj.setStoreId(shopId);
        newObj.setOpenId(openId);
        newObj.setCreateTime(LocalDateTime.now());

        this.save(newObj);

        stringRedisTemplate.opsForValue().set(RedisKeyConstants.SPLICING_LOCK_STATUS + newMainId, OrderConstants.NO.toString());
        returnMap.put("mainId", newMainId);
    }

    @Override
    public void mainLock(String mainId, String openId) {
        String mainLockKey = "LOCK_SPLICING#" + mainId;
        RLock mainLock = redissonClient.getLock(mainLockKey);
        try {
            mainLock.lock();

            this.update(
                    new LambdaUpdateWrapper<BzSplicingOrderDO>()
                            .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.LOCK.getCode())
                            .eq(BzSplicingOrderDO::getMainId, mainId)
            );

            stringRedisTemplate.opsForValue().set(RedisKeyConstants.SPLICING_LOCK_STATUS + mainId, OrderConstants.YES.toString());

            //通知netty下发消息
            splicingNettyClient.lockOrderNotifyAll(SyncCommodityDTO.builder().mainId(mainId).openId(openId).build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ObjectUtil.isNotNull(mainLock) && mainLock.isLocked() && mainLock.isHeldByCurrentThread()) {
                mainLock.unlock();
            }
        }
    }

    @Override
    public void mainCancel(String mainId, String openId) {

        this.update(
                new LambdaUpdateWrapper<BzSplicingOrderDO>()
                        .set(BzSplicingOrderDO::getStatus, SplicingOrderStateEnum.CANCELED.getCode())
                        .eq(BzSplicingOrderDO::getMainId, mainId)
        );
        //通知netty下发消息
        splicingNettyClient.cancelOrderNotifyAll(SyncCommodityDTO.builder().mainId(mainId).openId(openId).build());
    }

    @Override
    public void commodityChange(SplicingOrderMemberAddReqVO reqVO) {

        if (!ObjectUtils.isEmpty(reqVO.getCommodityInfo()) && reqVO.getCommodityInfo().contains("cartList")) {
            reqVO.setCommodityInfo(reqVO.getCommodityInfo().replace("cartList", "carList"));
        }

        String mainLockKey = "LOCK_SPLICING#" + reqVO.getMainId();
        RLock mainLock = redissonClient.getLock(mainLockKey);
        try {
            mainLock.lock();

            String status = stringRedisTemplate.opsForValue().get(RedisKeyConstants.SPLICING_LOCK_STATUS + reqVO.getMainId());

            //队长主动取消
            if (ObjectUtils.isEmpty(status)) {
                throw exception(ORDER_SPLICING_OVER);
            }

            //锁定
            if (Objects.equals(status, OrderConstants.YES.toString())) {
                throw exception(ORDER_SPLICING_PAYING);
            }

            String sessionId = reqVO.getMainId() + ":" + reqVO.getOpenId();

            Object obj = stringRedisTemplate.opsForHash().get(RedisKeyConstants.SESSION_KEY_PREFIX + reqVO.getMainId(), sessionId);
            if(ObjectUtils.isEmpty(obj)){
                throw exception(ORDER_SPLICING_NOT_IN_MAIN);
            }

            SplicingSessionDTO memberRedisSession = JSON.parseObject(Objects.requireNonNull(obj).toString(), SplicingSessionDTO.class);

            //用户选择的全量商品信息
            String commodityJson = reqVO.getCommodityInfo();
            memberRedisSession.setCarList(commodityJson);

            //更新当前用户的商品信息
            stringRedisTemplate.opsForHash().put(RedisKeyConstants.SESSION_KEY_PREFIX + reqVO.getMainId(), sessionId, JSON.toJSONString(memberRedisSession));

            //通知netty下发消息
            splicingNettyClient.syncMenuToOtherMember(SyncCommodityDTO.builder().mainId(reqVO.getMainId()).openId(reqVO.getOpenId()).sessionStr(commodityJson).build());
        } catch (ServiceException e) {
            throw e;
        } finally {
            if (ObjectUtil.isNotNull(mainLock) && mainLock.isLocked() && mainLock.isHeldByCurrentThread()) {
                mainLock.unlock();
            }
        }
    }

    public static void main(String[] args) {

        String s = "{\n" +
                "\t\"carList\": [{\n" +
                "\t\t\"commodityGroupIdList\": [1197506761673170945, 1197506763870986240, 1197506766102355968, 1197506768384057344],\n" +
                "\t\t\"commodityGroupSingleIdlist\": [670, 661, 683, 690],\n" +
                "\t\t\"commodityId\": \"1180466912584556544\",\n" +
                "\t\t\"commodityName\": \"双堡随心配\",\n" +
                "\t\t\"condimentIds\": [],\n" +
                "\t\t\"condimentInfos\": [],\n" +
                "\t\t\"copies\": 1,\n" +
                "\t\t\"flavorWithUser\": [],\n" +
                "\t\t\"isPurchase\": \"false\",\n" +
                "\t\t\"isSingle\": 2,\n" +
                "\t\t\"openId\": \"oHVqn6wls5_MWD9aBddpL5R1p0Po\",\n" +
                "\t\t\"price\": 18,\n" +
                "\t\t\"skuCode\": [\"1180466999171768320\"],\n" +
                "\t\t\"spuFlavorCond\": [],\n" +
                "\t\t\"spuGroupFirstSing\": [\"板烧鸡腿堡*1\", \"香辣鸡腿堡*1\", \"黑椒鸡块*1\", \"百事无糖*1\"],\n" +
                "\t\t\"storeId\": \"1241338966192422912\",\n" +
                "\t\t\"thumbnailUrl\": \"https://zjhb0090-local.oss-cn-beijing.aliyuncs.com/2024/OCTOBER/8/21/31/872dd821-8f07-4c5e-9352-bbc2c7f00a5e.jpg\"\n" +
                "\t}],\n" +
                "\t\"count\": 1,\n" +
                "\t\"orderPrice\": 18,\n" +
                "\t\"payPrice\": 18,\n" +
                "\t\"price\": 18,\n" +
                "\t\"storeId\": \"0\"\n" +
                "}";

        SpBzCarGoodsDTO bzCarGoodsDTO = JSON.parseObject(s, SpBzCarGoodsDTO.class);

        ValueFilter filter = new ValueFilter() {
            @Override
            public Object apply(Object object, String name, Object value) {
                if (value instanceof Long) {
                    return String.valueOf(value);
                }
                return value;
            }
        };

        String commodityJson = JSON.toJSONString(bzCarGoodsDTO, filter);
        System.out.println(commodityJson);
    }

    @Override
    public void feignTest() {
        splicingNettyClient.feignTest("feignTest");
    }
}
