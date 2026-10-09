package com.htyoudao.youdao.module.commodity.service.afterorder;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterOrderSimpleDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderDeleteMoreReq;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.AfterOrderSaveReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusByIdRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderAppVO;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.CommodityStoreSpuDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSkuMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSpuMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.afterorder.AfterOrderMapper;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.htyoudao.youdao.module.commodity.util.TimeAfterOrderUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.SAAS_PURCHASE_PRODUCT_SALES;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;

/**
 * 订单生成后加购商品 Service 实现类
 *
 * @author dht
 */
@Service
@Validated
@RefreshScope
public class AfterOrderServiceImpl implements AfterOrderService {

    @Value("${commodity.afterOrder.number}")
    private  Integer afterOrderNum;

    @Resource
    private AfterOrderMapper afterOrderMapper;

    @Resource
    private ICommoditySpusService commoditySupsService;

    @Resource
    private CommodityStoreSpuMapper commodityStoreSpuMapper;

    @Resource
    private CommodityStoreSkuMapper commodityStoreSkuMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @LogRecord(type = COMMODITY_AFTER_TYPE, subType = COMMODITY_AFTER_CREATE_SUB_TYPE, bizNo = "{{#afterOrder.afterId}}", success = COMMODITY_AFTER_CREATE_SUCCESS)
    public Long createAfterOrder(AfterOrderSaveReqVO createReqVO) {

        // 校验总数
        hasTotalNumberOfVerifications();
        // 校验重复
        getAfterOrderDO(createReqVO);
        // 转换对象
        AfterOrderDO afterOrder = getAfterOrderDO(createReqVO);
        afterOrderMapper.insert(afterOrder);

        LogRecordContext.putVariable("afterOrder", afterOrder);
        // 返回
        return afterOrder.getAfterId();
    }

    @Override
    @LogRecord(type = COMMODITY_AFTER_TYPE, subType = COMMODITY_AFTER_UPDATE_SUB_TYPE, bizNo = "{{#updateObj.afterId}}", success = COMMODITY_AFTER_UPDATE_SUCCESS)
    public void updateAfterOrder(AfterOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateAfterOrderExists(updateReqVO.getAfterId());
        // 校验重复
        AfterOrderDO updateObj = getAfterOrderDO(updateReqVO);

        LogRecordContext.putVariable("updateObj", updateObj);

        afterOrderMapper.updateById(updateObj);
    }

    /**
     * 校验总数 上限35个
     */
    private void hasTotalNumberOfVerifications() {
        if (afterOrderMapper.selectCount() >= afterOrderNum) {
            throw exception(AFTER_ORDER_TOTAL_ERROR);
        }
    }

    /**
     * 校验重复
     *
     * @param createReqVO createReqVO
     */
    private void isVerifyDuplicate(AfterOrderSaveReqVO createReqVO) {
        LambdaQueryWrapper<AfterOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AfterOrderDO::getCommodityId, createReqVO.getCommodityId());
        queryWrapper.ne(ObjectUtil.isNotEmpty(createReqVO.getAfterId()), AfterOrderDO::getAfterId, createReqVO.getAfterId());
        if (afterOrderMapper.selectCount(queryWrapper) > 0) {
            throw exception(AFTER_ORDER_DUPLICATE);
        }
    }

    /**
     * 获取更新/新增对象
     *
     * @param updateReqVO updateReqVO
     * @return AfterOrderDO
     */
    private AfterOrderDO getAfterOrderDO(AfterOrderSaveReqVO updateReqVO) {
        // 校验重复
        isVerifyDuplicate(updateReqVO);
        // 判断价格大小
        //hasDetermineThePriceSize(updateReqVO);
        // 更新
        AfterOrderDO updateObj = BeanUtils.toBean(updateReqVO, AfterOrderDO.class);

        // 更新商品名称和图片
        CommoditySpusByIdRespVo spuByIdV3 = commoditySupsService.getSpuByIdV3(updateObj.getCommodityId());
        updateObj.setCommodityName(spuByIdV3.getCommodityName());
        if (ObjectUtil.isNotEmpty(spuByIdV3.getImageUrlVO())) {
            String thumbnailUrl = spuByIdV3.getImageUrlVO().get(0);
            updateObj.setThumbnailUrl(thumbnailUrl);
        } else {
            updateObj.setThumbnailUrl(null);
        }

        return updateObj;
    }

    @Override
    @LogRecord(type = COMMODITY_AFTER_TYPE, subType = COMMODITY_AFTER_DELETE_SUB_TYPE, bizNo = "{{#afterId}}", success = COMMODITY_AFTER_DELETE_SUCCESS)
    public void deleteAfterOrder(Long afterId) {
        // 校验存在
        validateAfterOrderExists(afterId);
        // 删除
        afterOrderMapper.deleteById(afterId);
    }

    private void validateAfterOrderExists(Long afterId) {
        if (afterOrderMapper.selectById(afterId) == null) {
            throw exception(AFTER_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public AfterOrderDO getAfterOrder(Long afterId) {
        return afterOrderMapper.selectById(afterId);
    }

    @Override
    public PageResult<AfterOrderRespVO> getAfterOrderPage(AfterOrderPageReqVO pageReqVO) {
        PageResult<AfterOrderDO> page = afterOrderMapper.selectPage(pageReqVO);
        PageResult<AfterOrderRespVO> bean = new PageResult<>();

        if (ObjectUtil.isNotEmpty(page.getList())) {
            bean = BeanUtils.toBean(page, AfterOrderRespVO.class);
            List<AfterOrderRespVO> list = bean.getList();
            List<Long> commodityIds = list.stream().map(AfterOrderRespVO::getCommodityId).toList();
            List<CommoditySpus> spursByCommodityIds = commoditySupsService.getSpursByCommodityIds(commodityIds);
            Map<Long, CommoditySpus> collect = spursByCommodityIds.stream().collect(Collectors.toMap(CommoditySpus::getCommodityId, c -> c));
            Map<Long, Long> saleMap = getCommoditySalesMap(commodityIds);
            for (AfterOrderRespVO afterOrder : bean.getList()) {
                CommoditySpus commoditySpus = collect.get(afterOrder.getCommodityId());
                if (commoditySpus != null) {
                    afterOrder.setDictValue(commoditySpus.getDictValue());
                }
                afterOrder.setSalesVolumes(saleMap.getOrDefault(afterOrder.getCommodityId(), 0L));
            }
        }
        return bean;
    }

    private Map<Long, Long> getCommoditySalesMap(List<Long> commodityIds) {
        if (commodityIds == null || commodityIds.isEmpty()) {
            return Map.of();
        }
        List<Object> cacheKeys = commodityIds.stream().map(String::valueOf).collect(Collectors.toList());
        List<Object> cacheValues = stringRedisTemplate.opsForHash().multiGet(SAAS_PURCHASE_PRODUCT_SALES, cacheKeys);
        Map<Long, Long> saleMap = new java.util.HashMap<>();
        for (int i = 0; i < commodityIds.size(); i++) {
            Object cacheValue = cacheValues == null ? null : cacheValues.get(i);
            if (cacheValue != null) {
                saleMap.put(commodityIds.get(i), Math.abs(Long.parseLong(cacheValue.toString())));
            }
        }
        return saleMap;
    }

    @Override
    public void updateThumbnailUrlBySpuId(CommoditySpusUpdateReqVo updateReqVo) {
        LambdaUpdateWrapper<AfterOrderDO> orderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderLambdaUpdateWrapper.eq(AfterOrderDO::getCommodityId, updateReqVo.getCommodityId());
        orderLambdaUpdateWrapper.set(AfterOrderDO::getThumbnailUrl, updateReqVo.getImageUrlVO().get(0));
        afterOrderMapper.update(orderLambdaUpdateWrapper);
    }

    @Override
    public void updateEmptyThumbnailUrlBySpuId(CommoditySpusUpdateReqVo updateReqVo) {
        LambdaUpdateWrapper<AfterOrderDO> orderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderLambdaUpdateWrapper.eq(AfterOrderDO::getCommodityId, updateReqVo.getCommodityId());
        orderLambdaUpdateWrapper.set(AfterOrderDO::getThumbnailUrl, "");
        afterOrderMapper.update(orderLambdaUpdateWrapper);
    }

    @Override
    public List<AfterOrderDO> selectListBySpuId(Long commodityId) {
        LambdaQueryWrapper<AfterOrderDO> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.eq(AfterOrderDO::getCommodityId, commodityId);
        return afterOrderMapper.selectList(orderLambdaQueryWrapper);
    }

    @Override
    public List<AfterOrderDO> selectListBySpuIds(List<Long> commodityIds) {
        LambdaQueryWrapper<AfterOrderDO> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.in(AfterOrderDO::getCommodityId, commodityIds);

        return afterOrderMapper.selectList(orderLambdaQueryWrapper);
    }

    @Override
    public void deleteByCommodityIds(List<Long> commodityIds) {
        if (ObjectUtil.isEmpty(commodityIds)) {
            return;
        }
        LambdaQueryWrapper<AfterOrderDO> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.in(AfterOrderDO::getCommodityId, commodityIds);
        afterOrderMapper.delete(orderLambdaQueryWrapper);
    }

    @Override
    public List<AfterOrderAppVO> getAfterOrderList(AfterOrderReqVO reqVO) {
        //查询出所有的加购商品
        LambdaQueryWrapper<AfterOrderDO> commodityAfterOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<AfterOrderDO> commodityAfterOrders = afterOrderMapper.selectList(commodityAfterOrderLambdaQueryWrapper);
        // 下单的门店id
        Long storeId = reqVO.getStoreId();
        // 下单时候的商品ids
        List<Long> commodityIds = reqVO.getCommodityIds();
        //初始化一个返回加购商品列表
        List<AfterOrderAppVO> commodityAfterOrderChatVOList = new ArrayList<>();

        if (ObjectUtil.isEmpty(commodityAfterOrders)) {
            return commodityAfterOrderChatVOList;
        }
        List<Long> commodityIdList = commodityAfterOrders.stream().map(AfterOrderDO::getCommodityId).toList();
        LambdaQueryWrapper<CommodityStoreSpu> spuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, 1);
        if (ObjectUtil.isNotEmpty(commodityIds)) {
            spuLambdaQueryWrapper.notIn(CommodityStoreSpu::getCommodityStorePrimitiveSpuId, commodityIds);
        }
        spuLambdaQueryWrapper.in(CommodityStoreSpu::getCommodityStorePrimitiveSpuId, commodityIdList);
        List<CommodityStoreSpu> commodityStoreSpuList2 = commodityStoreSpuMapper.selectList(spuLambdaQueryWrapper);
        List<CommodityStoreSpuDTO> commodityStoreSpuList = BeanUtils.toBean(commodityStoreSpuList2, CommodityStoreSpuDTO.class);
        if (ObjectUtil.isNotEmpty(commodityStoreSpuList)) {
            List<Long> storeSpuIdList = commodityStoreSpuList.stream().map(CommodityStoreSpuDTO::getCommodityStoreSpuId).collect(Collectors.toList());
            List<Long> prIds = commodityStoreSpuList.stream().map(CommodityStoreSpuDTO::getCommodityStorePrimitiveSpuId).toList();
            LambdaQueryWrapper<CommodityStoreSku> storeSkuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            storeSkuLambdaQueryWrapper.in(CommodityStoreSku::getCommodityStoreSpuId, storeSpuIdList);
            List<CommodityStoreSku> storeSkuList = commodityStoreSkuMapper.selectList(storeSkuLambdaQueryWrapper);
            Map<Long, List<CommodityStoreSku>> skuMap = storeSkuList.stream().collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));
            //TODO 嵌入 SkuLIst
            for (CommodityStoreSpuDTO commodityStoreSpu : commodityStoreSpuList) {
                commodityStoreSpu.setCommodityStoreSkuList(skuMap.get(commodityStoreSpu.getCommodityStoreSpuId()));
            }
            TimeAfterOrderUtil.processTimeBasedList(commodityStoreSpuList);
            Map<Long, CommodityStoreSpuDTO> spuMap = commodityStoreSpuList.stream().collect(Collectors.toMap(CommodityStoreSpuDTO::getCommodityStorePrimitiveSpuId, c -> c, (key1, key2) -> key1));
            if (ObjectUtil.isNotEmpty(commodityAfterOrders)) {
                commodityAfterOrders.removeIf(c -> !prIds.contains(c.getCommodityId()));
            }
            if (ObjectUtil.isNotEmpty(commodityAfterOrders)) {
                for (AfterOrderDO afterOrder : commodityAfterOrders) {
                    CommodityStoreSpuDTO commodityStoreSpu = spuMap.get(afterOrder.getCommodityId());
                    if (!commodityStoreSpu.getIsUp()) {
                        continue;
                    }
                    if (commodityStoreSpu.getCommodityStoreSpuAppletStatus() == 0) {
                        continue;
                    }
                    //TODO 得到 skuLIst
                    List<CommodityStoreSku> commodityStoreSkuList = commodityStoreSpu.getCommodityStoreSkuList();
                    //TODO skuList
                    //初始化一个返回加购商品
                    AfterOrderAppVO chatVO = new AfterOrderAppVO();
                    chatVO.setAfterId(afterOrder.getAfterId());
                    chatVO.setCommodityId(afterOrder.getCommodityId());
                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())) {
                        chatVO.setThumbnailUrl(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()).get(0));
                    }
                    chatVO.setCommodityName(commodityStoreSpu.getCommodityStoreSpuName());
                    if (ObjectUtil.isNotEmpty(commodityStoreSkuList)) {
                        BigDecimal price = commodityStoreSkuList.get(0).getCommodityStoreSkuPrice();
                        BigDecimal subtract = price.subtract(afterOrder.getAfterPrice());
                        chatVO.setAfterPrice(subtract);
                    }
                    if (!org.apache.commons.collections4.CollectionUtils.isEmpty(commodityStoreSkuList) && ObjectUtil.isNotEmpty(commodityStoreSkuList.get(0))) {
                        chatVO.setStrikeThroughPrice(commodityStoreSkuList.get(0).getCommodityStoreSkuStrikePrice());
                        chatVO.setCommodityStatus(commodityStoreSpu.getCommodityStoreSpuAppletStatus());
                        chatVO.getSkuCode().add(String.valueOf(commodityStoreSkuList.get(0).getCommodityStoreSkuId()));
                    }
                    //比对购物车里的商品，如果有就把加购商品删掉
                    if (chatVO.getAfterPrice().compareTo(BigDecimal.valueOf(0)) > 0) {
                        commodityAfterOrderChatVOList.add(chatVO);
                    }
                }
            }
        }
        return commodityAfterOrderChatVOList;
    }

    @DataPermission(enable = false)
    @PermitAll
    @Override
    public List<AfterInfoDTO> selectAfterListForRpc(Set<Long> afterIds, Long storeId) {
        return afterOrderMapper.selectAfterListForRpc(afterIds, storeId);
    }

    @Override
    public List<AfterOrderSimpleDTO> selectAfterOrderSimpleListForRpc() {
        return BeanUtils.toBean(afterOrderMapper.selectList(new LambdaQueryWrapper<AfterOrderDO>()
            .orderByDesc(AfterOrderDO::getAfterId)), AfterOrderSimpleDTO.class);
    }

    @Override
    public void updateAfterOrderName(Long commodityId, String commodityName) {
        LambdaUpdateWrapper<AfterOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(AfterOrderDO::getCommodityId, commodityId);
        lambdaUpdateWrapper.set(AfterOrderDO::getCommodityName, commodityName);
        afterOrderMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateAfterOrderNameAndImage(Long commodityId, String commodityName, String image) {
        LambdaUpdateWrapper<AfterOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(AfterOrderDO::getCommodityId, commodityId);
        lambdaUpdateWrapper.set(AfterOrderDO::getCommodityName, commodityName);
        lambdaUpdateWrapper.set(AfterOrderDO::getThumbnailUrl, image);
        afterOrderMapper.update(lambdaUpdateWrapper);
    }

    @Override
    @LogRecord(type = COMMODITY_AFTER_TYPE, subType = COMMODITY_AFTER_DELETE_SUB_TYPE, bizNo = "{{#afterIds}}", success = COMMODITY_AFTER_DELETEMORE_SUCCESS)
    public void deleteMore(AfterOrderDeleteMoreReq afterOrderDeleteMoreReq) {
        if (ObjectUtil.isNotEmpty(afterOrderDeleteMoreReq.getAfterIds())){
            List<Long> afterIds = afterOrderDeleteMoreReq.getAfterIds();

            LambdaQueryWrapper<AfterOrderDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(AfterOrderDO::getAfterId, afterIds);
            afterOrderMapper.delete(queryWrapper);
            LogRecordContext.putVariable("afterIds", afterIds);
        }
    }
}
