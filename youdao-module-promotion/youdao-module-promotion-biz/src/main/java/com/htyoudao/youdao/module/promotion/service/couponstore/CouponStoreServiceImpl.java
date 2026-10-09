package com.htyoudao.youdao.module.promotion.service.couponstore;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.UpdateCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreAndOrgDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstore.CouponStoreAndOrgMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstore.CouponStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord.UserCouponRecordMapper;
// ...existing imports...
import com.htyoudao.youdao.module.promotion.util.CouponCountUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 优惠券门店关系 Service 实现类
 *
 * @author 13149747939
 */
@Service
@Validated
public class CouponStoreServiceImpl implements CouponStoreService {

    @Resource
    private CouponStoreMapper couponStoreMapper;

    @Resource
    private CouponStoreAndOrgMapper couponStoreAndOrgMapper;

    @Resource
    @Lazy
    private UserCouponRecordMapper userCouponRecordMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void insertBatch(List<CouponStoreDO> couponStores) {
        couponStoreMapper.insertBatchSomeColumn(couponStores);
    }

    @Override
    public void deleteByCouponId(Long couponId) {
        QueryWrapper<CouponStoreDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", couponId);
        couponStoreMapper.delete(queryWrapper);
    }

    @Override
    public List<CouponStoreDO> selectByCouponId(Long id) {
        QueryWrapper<CouponStoreDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", id);
        return couponStoreMapper.selectList(queryWrapper);
    }

    @Override
    public long selectCountByCouponId(Long couponId) {
        QueryWrapper<CouponStoreDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id", couponId);
        return couponStoreMapper.selectCount(queryWrapper);
    }

    @Override
    public List<CouponStoreDO> selectByStoreIds(List<Long> storeIds) {
        LambdaQueryWrapper<CouponStoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponStoreDO::getStoreId, storeIds);
        return couponStoreMapper.selectList(queryWrapper);
    }

    @Override
    public List<CouponStoreDO> selectByCouponIds(List<Long> respGoodCouponIds) {
        LambdaQueryWrapper<CouponStoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponStoreDO::getCouponId, respGoodCouponIds);
        return couponStoreMapper.selectList(queryWrapper);
    }

    @Override
    public List<CouponStoreAndOrgDO> getStoresByCouponIdAndName(Long couponId, String storeName) {
        List<CouponStoreAndOrgDO> storesByCouponIdAndName = couponStoreAndOrgMapper.getStoresByCouponIdAndName(couponId, storeName, BusinessContextHolder.getBusinessId());
        List<CouponStoreAndOrgDO> storesByCouponIdAndName2 = userCouponRecordMapper.getStoresByCouponIdAndName(couponId, storeName, BusinessContextHolder.getBusinessId());
        return mergeAndDeduplicateWithStream(storesByCouponIdAndName, storesByCouponIdAndName2);
    }

    private List<CouponStoreAndOrgDO> mergeAndDeduplicateWithStream(List<CouponStoreAndOrgDO> list1, List<CouponStoreAndOrgDO> list2) {
        Stream<CouponStoreAndOrgDO> combinedStream = Stream.concat(
                list1 != null ? list1.stream() : Stream.empty(),
                list2 != null ? list2.stream() : Stream.empty()
        );

        // 使用Collectors.toMap进行合并去重
        return new ArrayList<>(combinedStream
                .collect(Collectors.toMap(
                        item -> item.getStoreId() + "_" + item.getStoreName(),
                        item -> item,
                        (existing, replacement) -> existing
                ))
                .values());
    }

    @Override
    public void updateCouponStore(GoodCouponStoreDTO goodCouponDTO) {
        LambdaUpdateWrapper<CouponStoreDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(CouponStoreDO::getStoreName,goodCouponDTO.getStoreName());
        lambdaUpdateWrapper.eq(CouponStoreDO::getStoreId,goodCouponDTO.getStoreId());
        couponStoreMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateCouponStoreByTagIdAndStoreId(List<UpdateCouponStoreDTO> couponStores, CouponStoreTagSqlTypeEnum sqlType) {
        if (CollectionUtil.isEmpty(couponStores)) {
            return;
        }

        if (sqlType == CouponStoreTagSqlTypeEnum.UPDATE) {
            Set<Long> allTagIds = couponStores.stream()
                    .filter(Objects::nonNull)
                    .flatMap(dto -> dto.getTagIds() == null ? Stream.empty() : dto.getTagIds().stream())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtil.isEmpty(allTagIds)) {
                return;
            }
            LambdaQueryWrapper<CouponStoreDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(CouponStoreDO::getCouponId, CouponStoreDO::getTagId, CouponStoreDO::getTotalNum);
            queryWrapper.in(CouponStoreDO::getTagId, allTagIds);
            List<CouponStoreDO> couponStoreList = couponStoreMapper.selectList(queryWrapper);
            if(CollectionUtil.isEmpty(couponStoreList)){
                return;
            }
            Map<Long, List<CouponStoreDO>> tagCouponIdMap = couponStoreList.stream()
                    .collect(Collectors.groupingBy(CouponStoreDO::getTagId,
                            Collectors.collectingAndThen(
                                    Collectors.toMap(CouponStoreDO::getCouponId, Function.identity(),
                                            (existing, replacement) -> existing, LinkedHashMap::new),
                                    map -> new ArrayList<>(map.values()))));
            List<CouponStoreDO> updateList = new ArrayList<>();

            Map<Long, Integer> redisMap = new HashMap<>();
            for (UpdateCouponStoreDTO couponStore : couponStores) {
                List<Long> tagIds = couponStore.getTagIds() == null ? Collections.emptyList()
                        : couponStore.getTagIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
                if (CollectionUtil.isEmpty(tagIds)) {
                    continue;
                }
                for (Long tagId : tagIds) {
                    List<CouponStoreDO> couponStoreDOS = tagCouponIdMap.get(tagId);
                    if (CollectionUtil.isEmpty(couponStoreDOS)) {
                        continue;
                    }
                    for (CouponStoreDO item : couponStoreDOS) {
                        Integer storeLimitNum = item.getTotalNum();
                        if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setStoreId(couponStore.getStoreId());
                            couponStoreDO.setTagId(tagId);
                            couponStoreDO.setStoreName(couponStore.getStoreName());
                            couponStoreDO.setCouponId(item.getCouponId());
                            couponStoreDO.setTotalNum(storeLimitNum);
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            updateList.add(couponStoreDO);
                            redisMap.put(couponStore.getStoreId(), storeLimitNum);
                            CouponCountUtil.initCouponStoreQuantities(redisTemplate,item.getCouponId(),redisMap);
                        }else {
                            CouponStoreDO couponStoreDO = new CouponStoreDO();
                            couponStoreDO.setStoreId(couponStore.getStoreId());
                            couponStoreDO.setTagId(tagId);
                            couponStoreDO.setStoreName(couponStore.getStoreName());
                            couponStoreDO.setCouponId(item.getCouponId());
                            couponStoreDO.setDeleted(Boolean.FALSE);
                            updateList.add(couponStoreDO);
                        }

                    }
                }
            }

            if (!updateList.isEmpty()) {
                couponStoreMapper.insertBatchSomeColumn(updateList);
            }
        }

        if(sqlType == CouponStoreTagSqlTypeEnum.DELETE){
            List<Long> tagIds = couponStores.stream().map(UpdateCouponStoreDTO::getTagIds).filter(Objects::nonNull).flatMap(Collection::stream).distinct().toList();
            List<Long> storeIds = couponStores.stream().map(UpdateCouponStoreDTO::getStoreId).toList();
            LambdaUpdateWrapper<CouponStoreDO> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.in(CouponStoreDO::getStoreId, storeIds);
            queryWrapper.in(CouponStoreDO::getTagId, tagIds);
            couponStoreMapper.delete(queryWrapper);
        }
    }
}