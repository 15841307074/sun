package com.htyoudao.youdao.module.commodity.service.job;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.SAAS_PRODUCT_SALES;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.convert.CommodityConvertor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreCategoryMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSkuMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSpuMapper;
import com.htyoudao.youdao.module.commodity.dal.redis.CommodityStoreRedisDao;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.service.storeGroup.ICommodityStoreGroupService;
import com.htyoudao.youdao.module.commodity.service.storeSingle.ICommodityStoreSingleService;
import com.htyoudao.youdao.module.commodity.service.storeSku.ICommodityStoreSkuService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

    @Resource
    private ICommoditySpusService commoditySpusService;

    @Resource
    protected StringRedisTemplate stringRedisTemplate;

    @Resource
    private ICommodityStoreGroupService commodityStoreGroupService;

    @Resource
    private ICommodityStoreSingleService commodityStoreSingleService;

    @Resource
    private ICommodityStoreSkuService commodityStoreSkuService;

    @Resource
    private CommodityStoreSkuMapper commodityStoreSkuMapper;

    @Resource
    private ICommodityTageService commodityTageService;

    @Resource
    private CommodityStoreSpuMapper commodityStoreSpuMapper;

    @Resource
    private CommodityStoreCategoryMapper storeCategoryMapper;

    @Resource
    private CommodityStoreRedisDao storeRedisDao;

    @DubboReference
    private StoreApi storeApi;

    /**
     * 更新商品销量 每天上午10点，下午3点，凌晨1点 避开高峰执行
     */
    @DataPermission(enable = false)
    @Override
    public void updateCommoditySaleTaskHandler() {

        //销量
        Map<Long, Long> commoditySaleMap = this.getAllHashEntriesAsLong(SAAS_PRODUCT_SALES);

        commoditySaleMap.forEach((commodityId, sale) -> commoditySpusService.update(
            new LambdaUpdateWrapper<CommoditySpus>().set(CommoditySpus::getSalesVolumes, sale)
                .eq(CommoditySpus::getCommodityId, commodityId)));

        log.info("==> 更新商品销量完成, 一共更新了{}条记录", commoditySaleMap.size());
    }

    public Map<Long, Long> getAllHashEntriesAsLong(String redisKey) {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(redisKey);
        return raw.entrySet().stream().collect(Collectors.toMap(e -> Long.parseLong(e.getKey().toString()),
            e -> Math.abs(Long.parseLong(e.getValue().toString()))));
    }


    @Override
    @DataPermission(enable = false)
    public void commodityCacheReloadHandler(List<Long> storeIds, Long businessId) {

        BusinessContextHolder.setBusinessId(businessId);

        if (CollectionUtils.isEmpty(storeIds)){
            storeIds =  storeApi.getAllStoreList().getCheckedData().stream().map(StoreInfoDTO::getStoreId).toList();
        }


        log.info("=====commodityCacheReloadStart,门店总数量:{}=====",storeIds.size());


        for (Long storeId : storeIds) {

            List<CommodityStoreSpu> storeSpus = commodityStoreSpuMapper.selectList(CommodityStoreSpu::getStoreId, storeId);
            log.info("=====storeId:{},重置spu缓存开始,总数量:{}=====",storeId, storeSpus.size());

            storeRedisDao.clearStoreCache(storeId);

            if (CollectionUtils.isEmpty(storeSpus)){
                log.info("storeId:{}没有品,不执行缓存覆盖",storeId);
                continue;
            }

            List<Long> spuIds = storeSpus.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds, storeSpus);

            List<CommodityStoreCategory> categories = storeCategoryMapper.selectList(CommodityStoreCategory::getStoreId, storeId);
            if (ObjectUtil.isNull(categories)) {
                log.info("storeId:{}没有分类,不执行缓存覆盖", storeId);
                continue;
            }

            for (CommodityStoreCategory category : categories) {
                CategoryDto categoryDto = CommodityConvertor.convertDoToCategoryDTO(category);
                storeRedisDao.saveOrUpdateCategory(category.getStoreId(),category.getCommodityStoreCategoryId(), categoryDto);
            }

            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }
            log.info("=====storeId:{},重置spu缓存结束=====",storeId);
        }

        log.info("=====commodityCacheReloadEnd=====");
    }




    private List<SpuDto> coverCommodityStoreSpuListToSpuDtoList(List<Long> spuIds,
        List<CommodityStoreSpu> commodityStoreSpus) {

        List<SpuDto> spuDtos = new ArrayList<>();

        List<CommodityStoreSku> commodityStoreSkuList = Optional.ofNullable(
            commodityStoreSkuService.selectBySpuIds(spuIds)).orElse(Collections.emptyList());

        List<CommodityStoreGroup> storeGroupList = Optional.ofNullable(
            commodityStoreGroupService.selectBySpuIds(spuIds)).orElse(Collections.emptyList());
        List<CommodityStoreSingle> singleList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(storeGroupList)) {
            List<Long> groupIds = storeGroupList.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();
            singleList = commodityStoreSingleService.selectByGroupIds(groupIds);
        }

        Map<Long, List<CommodityStoreSku>> skumap = commodityStoreSkuList.stream()
            .collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));
        Map<Long, List<CommodityStoreGroup>> groupMap = new HashMap<>();
        Map<Long, List<CommodityStoreSingle>> singleMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(storeGroupList)) {
            groupMap = storeGroupList.stream()
                .collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
            singleMap = singleList.stream()
                .collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
        }

        for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
            List<CommodityStoreSku> commodityStoreSkuList1 = skumap.get(storeSpus.getCommodityStoreSpuId());
            List<CommodityStoreSingle> commodityStoreSinglesAll = new ArrayList<>();
            List<CommodityStoreGroup> commodityStoreGroups = new ArrayList<>();
            List<CommodityTag> commodityTags = new ArrayList<>();
            if (storeSpus.getCommodityStoreSpuIsSingle() == 2) {
                commodityStoreGroups = groupMap.get(storeSpus.getCommodityStoreSpuId());
                List<Long> groupIds = commodityStoreGroups.stream().map(CommodityStoreGroup::getCommodityStoreGroupId)
                    .toList();

                for (Long groupId : groupIds) {
                    List<CommodityStoreSingle> commodityStoreSingles = singleMap.getOrDefault(groupId,
                        Collections.emptyList());
                    commodityStoreSinglesAll.addAll(commodityStoreSingles);
                }
            }
            if (ObjectUtil.isNotEmpty(storeSpus.getTagIds())) {
                List<Long> longs = ConvertUtil.convertStringToListNew(storeSpus.getTagIds());
                commodityTags = commodityTageService.selectListByIds(longs);
            }

            SpuDto spuDto = CommodityConvertor.convertDosToSpuDTOQ(storeSpus, commodityStoreSkuList1,
                commodityStoreSinglesAll, commodityStoreGroups, commodityTags);
            spuDtos.add(spuDto);
        }
        return spuDtos;
    }

}
