package com.htyoudao.youdao.module.commodity.convert;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.constant.CommodityConstant;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.PriceResultDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCondiments;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityFlavor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.CondimentDto;
import com.htyoudao.youdao.module.commodity.dal.dto.FlavorDto;
import com.htyoudao.youdao.module.commodity.dal.dto.GroupDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SingleDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SkuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.enums.CommodityPackageType;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION;

@Slf4j
public class CommodityConvertor {




    public static CommodityStoreSingle convertToStoreSingle(CommodityTemplateGroupSingle single) {
        CommodityStoreSingle storeSingle = new CommodityStoreSingle();
        storeSingle.setSingleSkuId(single.getSingleSkuId());
        storeSingle.setSingleSkuName(single.getSingleSkuName());
        storeSingle.setCommodityId(single.getCommodityId());
        //TODO 待处理
/*
        storeSingle.setCommodityTemplateId(single.getCommodityTemplateId());
        storeSingle.setGroupTemplateId(single.getTemplateGroupId());*/
        storeSingle.setCommodityStoreSingleSort(single.getSort());
        storeSingle.setCommodityStoreSinglePrice(single.getUpPrice());
        storeSingle.setCommodityStoreSingleCopies(single.getCopies());
        storeSingle.setMarkingPrice(single.getMarkingPrice());
        storeSingle.setCommodityName(single.getCommodityName());
        storeSingle.setCommodityUrl(single.getCommodityUrl());
        storeSingle.setDefaultChoose(single.getDefaultChoose());
        storeSingle.setRequiredChoose(single.getRequiredChoose());
        storeSingle.setSpuId(single.getSpuId());
        storeSingle.setWxStatus(single.getWxStatus());
        storeSingle.setStoreStatus(single.getStoreStatus());
        storeSingle.setFlavor(single.getFlavor());
        return storeSingle;
    }


    public static CommodityStoreSingle convertToStoreSingle(CommodityGroupSingle single) {
        CommodityStoreSingle storeSingle = new CommodityStoreSingle();
        storeSingle.setCommodityId(single.getCommodityId());
        storeSingle.setCommodityStoreSingleSort(single.getSort());
        storeSingle.setCommodityStoreSinglePrice(single.getUpPrice());
        storeSingle.setCommodityStoreSingleCopies(single.getCopies());
        storeSingle.setMarkingPrice(single.getMarkingPrice());
        storeSingle.setCommodityName(single.getCommodityName());
        storeSingle.setCommodityUrl(single.getCommodityUrl());
        storeSingle.setDefaultChoose(single.getDefaultChoose());
        storeSingle.setRequiredChoose(single.getRequiredChoose());
        storeSingle.setSpuId(single.getSpuId());
        storeSingle.setSingleSkuId(single.getSingleSkuId());
        storeSingle.setSingleSkuName(single.getSingleSkuName());
        storeSingle.setWxStatus(single.getWxStatus());
        storeSingle.setStoreStatus(single.getStoreStatus());
        storeSingle.setFlavor(single.getFlavor());
        return storeSingle;
    }


    public static CommodityStoreSpu convertToStoreSpu(CommodityTemplateSpus commodityTemplateSpus) {
        CommodityStoreSpu storeSpu = new CommodityStoreSpu();
        storeSpu.setImageUrl(commodityTemplateSpus.getImageUrl());
        storeSpu.setCommodityId(commodityTemplateSpus.getCommodityId());
        storeSpu.setCommodityStoreSpuName(commodityTemplateSpus.getCommodityName());
        storeSpu.setCommodityStoreSpuDescription(commodityTemplateSpus.getDescription());
        storeSpu.setCommodityStoreSpuSort(
            commodityTemplateSpus.getSort() == null ? 0 : commodityTemplateSpus.getSort().intValue());
        storeSpu.setCommodityStoreSpuUnit(commodityTemplateSpus.getUnit());
        storeSpu.setDictValue(commodityTemplateSpus.getDictValue());
        storeSpu.setCommodityStoreSpuIsSingle(commodityTemplateSpus.getIsSingle());
        storeSpu.setCommodityStoreSpuDetailUrl(commodityTemplateSpus.getSpuDetailUrl());
        storeSpu.setCommodityStoreSpuMachineStatus(commodityTemplateSpus.getStoreStatus());//1上架，2下架
        storeSpu.setCommodityStoreSpuAppletStatus(commodityTemplateSpus.getWxStatus());//1上架，2下架
        storeSpu.setCommodityStorePrimitiveSpuId(commodityTemplateSpus.getCommodityId());
        storeSpu.setCondiments(commodityTemplateSpus.getCondiments());
        storeSpu.setFlavor(commodityTemplateSpus.getFlavor());
        storeSpu.setCategoryName(commodityTemplateSpus.getCategoryName());
        storeSpu.setSpusTag(commodityTemplateSpus.getSpusTag());
        storeSpu.setTimeSharingTopping(commodityTemplateSpus.getTimeSharingTopping());
        storeSpu.setStartDate(commodityTemplateSpus.getStartDate());
        storeSpu.setEndDate(commodityTemplateSpus.getEndDate());
        storeSpu.setDayNumbers(commodityTemplateSpus.getDayNumbers());
        storeSpu.setWeekNumbers(commodityTemplateSpus.getWeekNumbers());
        storeSpu.setTimeRange(commodityTemplateSpus.getTimeRange());
        storeSpu.setPackageFee(commodityTemplateSpus.getPackageFee());
        storeSpu.setSaleRule(commodityTemplateSpus.getSaleRule());
        storeSpu.setLimitBuyNumber(commodityTemplateSpus.getLimitBuyNumber());
        storeSpu.setLimitDayBuyNumber(commodityTemplateSpus.getLimitDayBuyNumber());
        storeSpu.setLimitOrderBuyNumber(commodityTemplateSpus.getLimitOrderBuyNumber());
        storeSpu.setSetmealType(commodityTemplateSpus.getSetmealType());
        storeSpu.setApplicableNumber(commodityTemplateSpus.getApplicableNumber());
        storeSpu.setIsAllDay(commodityTemplateSpus.getIsAllDay());
        storeSpu.setMaxCondimentNumber(commodityTemplateSpus.getMaxCondimentNumber());
        storeSpu.setCondimentIsMore(commodityTemplateSpus.getCondimentIsMore());
        storeSpu.setManyCopy(commodityTemplateSpus.getManyCopy());
        storeSpu.setCommodityStoreCategoryId(commodityTemplateSpus.getCategoryTemplateId());
        storeSpu.setTagIds(commodityTemplateSpus.getTagIds());
        storeSpu.setGoodsExplain(commodityTemplateSpus.getGoodsExplain());
        return storeSpu;
    }


    public static CommodityStoreSpu convertToStoreSpu(CommoditySpus spu) {
        CommodityStoreSpu commodityStoreSpu = new CommodityStoreSpu();
        commodityStoreSpu.setCommodityStoreCategoryId(spu.getCategoryId());
        commodityStoreSpu.setCommodityStoreSpuDescription(spu.getDescription());
        commodityStoreSpu.setCommodityStorePrimitiveSpuId(spu.getCommodityId());
        commodityStoreSpu.setCommodityStoreSpuSort(spu.getSort());
        commodityStoreSpu.setCommodityStoreSpuUnit(spu.getUnit());
        commodityStoreSpu.setCommodityStoreSpuName(spu.getCommodityName());
        commodityStoreSpu.setCommodityStoreSpuIsSingle(spu.getIsSingle());
        commodityStoreSpu.setCommodityStoreSpuDetailUrl(spu.getSpuDetailUrl());
        commodityStoreSpu.setCommodityStoreSpuAppletStatus(spu.getWxStatus());
        commodityStoreSpu.setCommodityStoreSpuMachineStatus(spu.getStoreStatus());
        commodityStoreSpu.setCommodityStoreSpuLock(true);
        commodityStoreSpu.setSetmealType(spu.getSetmealType());
        commodityStoreSpu.setCommodityId(spu.getCommodityId());
        commodityStoreSpu.setImageUrl(spu.getImageUrl());
        commodityStoreSpu.setManyCopy(spu.getManyCopy());
        commodityStoreSpu.setPackageFee(spu.getPackageFee());
        commodityStoreSpu.setWxForceStatus(spu.getWxStatus());
        commodityStoreSpu.setStoreForceStatus(spu.getStoreStatus());
        commodityStoreSpu.setCondiments(spu.getCondiments());
        commodityStoreSpu.setFlavor(spu.getFlavor());
        commodityStoreSpu.setCategoryName(spu.getCategoryName());
        commodityStoreSpu.setLimitBuyNumber(spu.getLimitBuyNumber());
        commodityStoreSpu.setLimitDayBuyNumber(spu.getLimitDayBuyNumber());
        commodityStoreSpu.setLimitOrderBuyNumber(spu.getLimitOrderBuyNumber());
        commodityStoreSpu.setSaleRule(spu.getSaleRule());
        commodityStoreSpu.setCondimentIsMore(spu.getCondimentIsMore());
        commodityStoreSpu.setMaxCondimentNumber(spu.getMaxCondimentNumber());
        commodityStoreSpu.setApplicableNumber(spu.getApplicableNumber());
        commodityStoreSpu.setTimeSharingTopping(spu.getTimeSharingTopping());
        commodityStoreSpu.setIsAllDay(spu.getIsAllDay());
        commodityStoreSpu.setTimeRange(spu.getTimeRange());
        commodityStoreSpu.setSpusTag(spu.getSpusTag());
        commodityStoreSpu.setStartDate(spu.getStartDate());
        commodityStoreSpu.setEndDate(spu.getEndDate());
        commodityStoreSpu.setDayNumbers(spu.getDayNumbers());
        commodityStoreSpu.setWeekNumbers(spu.getWeekNumbers());
        commodityStoreSpu.setDictValue(spu.getDictValue());
        commodityStoreSpu.setTagIds(spu.getTagIds());
        commodityStoreSpu.setGoodsExplain(spu.getGoodsExplain());
        return commodityStoreSpu;
    }


    public static CommodityStoreCategory convertToCommodityStoreCategory(CommodityCategory commodityCategory) {
        CommodityStoreCategory commodityStoreCategory = new CommodityStoreCategory();
        commodityStoreCategory.setCommodityStorePrimitiveCategoryId(commodityCategory.getId());
        commodityStoreCategory.setCommodityStoreCategoryImage(commodityCategory.getUrl());
        commodityStoreCategory.setCommodityStoreCategoryName(commodityCategory.getName());
        commodityStoreCategory.setCommodityStoreCategoryStatus(commodityCategory.getStatus());
        commodityStoreCategory.setCommodityStoreCategorySort(commodityCategory.getSort());
        commodityStoreCategory.setType(commodityCategory.getType());
        commodityStoreCategory.setTimeSharingTopping(commodityCategory.getTimeSharingTopping());
        commodityStoreCategory.setStartDate(commodityCategory.getStartDate());
        commodityStoreCategory.setEndDate(commodityCategory.getEndDate());
        commodityStoreCategory.setDayNumbers(commodityCategory.getDayNumbers());
        commodityStoreCategory.setWeekNumbers(commodityCategory.getWeekNumbers());
        commodityStoreCategory.setTimeRange(commodityCategory.getTimeRange());
        commodityStoreCategory.setIsAllDay(commodityCategory.getIsAllDay());
        return commodityStoreCategory;
    }

    public static CommodityStoreCategory convertToCommodityStoreCategory(CommodityTemplateCategory commodityCategory) {
        CommodityStoreCategory commodityStoreCategory = new CommodityStoreCategory();
        commodityStoreCategory.setCommodityStorePrimitiveCategoryId(commodityCategory.getCategoryId());
        commodityStoreCategory.setCommodityStoreCategoryImage(commodityCategory.getUrl());
        commodityStoreCategory.setCommodityStoreCategoryName(commodityCategory.getName());
        commodityStoreCategory.setCommodityStoreCategoryStatus(commodityCategory.getStatus());
        commodityStoreCategory.setCommodityStoreCategorySort(commodityCategory.getSort());
        commodityStoreCategory.setType(commodityCategory.getType());
        commodityStoreCategory.setTimeSharingTopping(commodityCategory.getTimeSharingTopping());
        commodityStoreCategory.setStartDate(commodityCategory.getStartDate());
        commodityStoreCategory.setEndDate(commodityCategory.getEndDate());
        commodityStoreCategory.setDayNumbers(commodityCategory.getDayNumbers());
        commodityStoreCategory.setWeekNumbers(commodityCategory.getWeekNumbers());
        commodityStoreCategory.setTimeRange(commodityCategory.getTimeRange());
        commodityStoreCategory.setIsAllDay(commodityCategory.getIsAllDay());
        return commodityStoreCategory;
    }

    public static CommodityStoreSku convertToStoreSku(CommoditySkus sku) {
        CommodityStoreSku storeSku = new CommodityStoreSku();
        storeSku.setCommodityId(sku.getCommodityId());
        storeSku.setCommodityStoreSkuName(sku.getSkusName());
        storeSku.setCommodityStoreSkuValue(sku.getSkusValue());
        storeSku.setCommodityStoreSkuPrice(sku.getIllustratePrices());
        storeSku.setCommodityStoreSkuStrikePrice(sku.getStrikeThroughPrice());
        storeSku.setCommodityStoreSkuPrice(sku.getIllustratePrices());
        storeSku.setSkuId(sku.getSkuId());
        storeSku.setCommodityStoreSkuStrikePrice(sku.getStrikeThroughPrice());
        storeSku.setCommodityStoreSkuStatus(sku.getStoreStatus());
        return storeSku;
    }


    public static CommodityStoreSku convertToStoreSku(CommodityTemplateSkus sku) {
        CommodityStoreSku storeSku = new CommodityStoreSku();
        storeSku.setCommodityId(sku.getCommodityId());
        storeSku.setCommodityStoreSkuName(sku.getSkusName());
        storeSku.setCommodityStoreSkuValue(sku.getSkusValue());
        storeSku.setCommodityStoreSkuPrice(sku.getIllustratePrices());
        storeSku.setCommodityStoreSkuStrikePrice(sku.getStrikeThroughPrice());
        storeSku.setCommodityStoreSkuPrice(sku.getIllustratePrices());
        storeSku.setSkuId(sku.getSkuId());
        storeSku.setCommodityStoreSkuStrikePrice(sku.getStrikeThroughPrice());
        storeSku.setCommodityStoreSkuStatus(sku.getStoreStatus());

        return storeSku;
    }

    public static CommodityStoreGroup convertToStoreGroup(CommoditySetmealGroup setMealGroup) {
        CommodityStoreGroup commodityStoreGroup = new CommodityStoreGroup();
        commodityStoreGroup.setCommodityStoreGroupStatus(setMealGroup.getStatus());
        commodityStoreGroup.setCommodityStoreGroupSort(setMealGroup.getSort());
        commodityStoreGroup.setCommodityStoreGroupChoose(
            Objects.isNull(setMealGroup.getChoose()) ? null : Math.toIntExact(setMealGroup.getChoose()));
        commodityStoreGroup.setChooseMany(setMealGroup.getChooseMany());
        commodityStoreGroup.setCommodityStoreGroupAttribute(setMealGroup.getAttribute());
        commodityStoreGroup.setCommodityStoreGroupName(setMealGroup.getCommodityGroupName());
        return commodityStoreGroup;
    }

    public static CommodityStoreGroup convertToStoreGroup(CommodityTemplateSetmealGroup commodityTemplateSetmealGroup) {
        CommodityStoreGroup commodityStoreGroup = new CommodityStoreGroup();
        commodityStoreGroup.setCommodityStoreGroupStatus(commodityTemplateSetmealGroup.getStatus());
        commodityStoreGroup.setCommodityStoreGroupSort(commodityTemplateSetmealGroup.getSort());
        commodityStoreGroup.setCommodityStoreGroupChoose(commodityTemplateSetmealGroup.getChoose());
        commodityStoreGroup.setChooseMany(commodityTemplateSetmealGroup.getChooseMany());
        commodityStoreGroup.setCommodityStoreGroupAttribute(commodityTemplateSetmealGroup.getAttribute());
        commodityStoreGroup.setCommodityStoreGroupName(commodityTemplateSetmealGroup.getCommodityGroupName());
        return commodityStoreGroup;
    }




    public static SpuDto convertDosToSpuDTOQ(CommodityStoreSpu storeSpu,
                                            List<CommodityStoreSku> storeSkuList,
                                            List<CommodityStoreSingle> storeSingleList,
                                            List<CommodityStoreGroup> storeGroupList,
                                             List<CommodityTag> commodityTags) {

        SpuDto spuDto = convertBasicSpuInfo(storeSpu);

        processSkuList(spuDto, storeSkuList);
        processCondimentsQ(spuDto, storeSpu);
        processFlavorsQ(spuDto, storeSpu);
        processTagsQ(spuDto, commodityTags);


        processPackageGroups(spuDto, storeGroupList, storeSingleList);

        // 最终统一处理showBtn
        boolean needShowBtn = CollectionUtil.isNotEmpty(spuDto.getSkuList()) && spuDto.getSkuList().size() > 1 //存在多规格
                || CollectionUtil.isNotEmpty(spuDto.getCommodityCondiments()) // 存在小料
                || CollectionUtil.isNotEmpty(spuDto.getCommodityFlavors()) //存在属性
                || Objects.equals(spuDto.getSetmealType(), CommodityPackageType.GROUP_SELECTABLE.getCode()); // 分组可选套餐
        spuDto.setShowBtn(needShowBtn ? 1 : 0);

        return spuDto;
    }



    private static SpuDto convertBasicSpuInfo(CommodityStoreSpu storeSpu) {
        SpuDto spuDto = new SpuDto();
        spuDto.setSpuId(storeSpu.getCommodityStoreSpuId());
        spuDto.setStoreId(storeSpu.getStoreId());
        spuDto.setCategoryId(storeSpu.getCommodityStoreCategoryId());
        spuDto.setManyCopy(storeSpu.getManyCopy());
        spuDto.setPackageFee(storeSpu.getPackageFee());
        spuDto.setSpuName(storeSpu.getCommodityStoreSpuName());
        spuDto.setSpuDesc(storeSpu.getCommodityStoreSpuDescription());
        spuDto.setCondimentIsMore(storeSpu.getCondimentIsMore());
        spuDto.setMaxCondimentNumber(storeSpu.getMaxCondimentNumber());
        spuDto.setCommodityStoreSpuAppletStatus(storeSpu.getCommodityStoreSpuAppletStatus());
        spuDto.setCommodityStoreSpuMachineStatus(storeSpu.getCommodityStoreSpuMachineStatus());
        spuDto.setLimitBuyNumber(Objects.requireNonNullElse(storeSpu.getLimitBuyNumber(), 1));
        spuDto.setBannerList(ConvertUtil.convertStringToListS(storeSpu.getImageUrl()));
        spuDto.setSetmealType(storeSpu.getSetmealType());
        spuDto.setCommodityId(storeSpu.getCommodityId());

        spuDto.setTimeSharingTopping(storeSpu.getTimeSharingTopping());
        spuDto.setStartDate(storeSpu.getStartDate());
        spuDto.setEndDate(storeSpu.getEndDate());
        spuDto.setDayNumbers(storeSpu.getDayNumbers());
        spuDto.setWeekNumbers(storeSpu.getWeekNumbers());
        spuDto.setTimeRange(storeSpu.getTimeRange());
        spuDto.setIsAllDay(storeSpu.getIsAllDay());
        spuDto.setSaleRule(storeSpu.getSaleRule());
        spuDto.setSort(storeSpu.getCommodityStoreSpuSort());
        spuDto.setGoodsExplain(storeSpu.getGoodsExplain());

        spuDto.setSingleNoDelivery(Objects.equals(storeSpu.getCommodityStoreSpuDetailUrl(), "1"));
        return spuDto;
    }

    private static void processSkuList(SpuDto spuDto, List<CommodityStoreSku> storeSkuList) {
        if (CollectionUtil.isEmpty(storeSkuList)) {
            return;
        }

        List<SkuDto> skuDtoList = storeSkuList.stream()
//            .filter(sku -> CommodityConstant.ENABLE.equals(sku.getCommodityStoreSkuStatus()))
            .map(CommodityConvertor::getSkuDto)
            .toList();

        if (CollectionUtil.isNotEmpty(skuDtoList)) {
            spuDto.setSkuList(skuDtoList);
            PriceResultDTO priceResult = PriceResultDTO.findMinMaxPricesStore(storeSkuList);
            spuDto.setSpuPrice(priceResult.getMinIllustratePrices());
            spuDto.setSpuUnderlinedPrice(priceResult.getMinStrikeThroughPrice());
        }
    }


    private static void processCondimentsQ(SpuDto spuDto, CommodityStoreSpu storeSpu) {
        // 组装小料
        if (ObjectUtil.isNotEmpty(storeSpu.getCondiments())){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(storeSpu.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                });
                List<CondimentDto> list = commodityCondimentsList.stream().map(CommodityConvertor::getCondimentDto)
                .toList();
                spuDto.setCommodityCondiments(list);
            } catch (IOException e) {
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
            }
        }
    }



    private static void processFlavorsQ(SpuDto spuDto, CommodityStoreSpu storeSpu) {
        if (ObjectUtil.isNotEmpty(storeSpu.getFlavor())){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 进行反序列化操作
                List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(storeSpu.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                });
                List<FlavorDto> list = commodityFlavorList.stream().map(CommodityConvertor::getFlavorDto)
                    .toList();
                spuDto.setCommodityFlavors(list);
            } catch (IOException e) {
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
            }
        }
    }

    private static CondimentDto getCondimentDto(CommodityCondiments condiments) {
        CondimentDto condimentDto = new CondimentDto();
        condimentDto.setCondimentId(condiments.getCondimentId());
        condimentDto.setCondimentName(condiments.getCondimentName());
        condimentDto.setImageUrl(condiments.getImageUrl());
        condimentDto.setCondimentPrice(condiments.getPrice());
        condimentDto.setStatus(condiments.getStatus());
        condimentDto.setNumber(condiments.getNumber());
        return condimentDto;
    }

    private static FlavorDto getFlavorDto(CommodityFlavor flavor) {
        FlavorDto flavorDto = new FlavorDto();
        flavorDto.setFlavorId(flavor.getFlavorId());
        flavorDto.setFlavorName(flavor.getFlavorName());
        List<String> flavorValues = flavor.getFlavorValueListMap().stream()
            .flatMap(map -> map.entrySet().stream())
            .filter(entry -> !Objects.equals(entry.getValue(), 0))  // 过滤掉 value 等于 0 的条目
            .map(Map.Entry::getKey)  // 提取键
            .collect(Collectors.toList());
        flavorDto.setFlavorValues(flavorValues);
        flavorDto.setFlavorValueListMap(flavor.getFlavorValueListMap());
        return flavorDto;
    }



    private static void processTagsQ(SpuDto spuDto, List<CommodityTag> commodityTags ){
        if (ObjectUtil.isEmpty(commodityTags)){
            return;
        }

        // 标签组装
        spuDto.setCommodityTags(commodityTags);
    }

    private static void processPackageGroups(SpuDto spuDto,
        List<CommodityStoreGroup> storeGroupList,
        List<CommodityStoreSingle> storeSingleList) {
        if (CollectionUtil.isEmpty(storeGroupList) || CollectionUtil.isEmpty(storeSingleList)) {
            spuDto.setSetmealType(CommodityPackageType.SINGLE_ITEM.getCode());
            return;
        }

        Map<Long, List<CommodityStoreSingle>> singleMap = storeSingleList.stream()
            .collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));

        List<GroupDto> groupDtos = storeGroupList.stream()
            .map(group -> getGroupDto(group, singleMap.getOrDefault(group.getCommodityStoreGroupId(), Collections.emptyList())))
            .filter(group -> CollectionUtil.isNotEmpty(group.getSingleList()))
            .toList();

        spuDto.setGroupList(groupDtos);
    }

    private static GroupDto getGroupDto(CommodityStoreGroup group, List<CommodityStoreSingle> singles) {
        GroupDto groupDto = new GroupDto();
        groupDto.setGroupName(group.getCommodityStoreGroupName());
        groupDto.setGroupId(group.getCommodityStoreGroupId());
        groupDto.setChoose(group.getCommodityStoreGroupChoose() != null ? group.getCommodityStoreGroupChoose() : 0);
        groupDto.setChooseMany(group.getChooseMany());
        groupDto.setCommodityStoreGroupAttribute(group.getCommodityStoreGroupAttribute());

        if (CollectionUtil.isNotEmpty(singles)) {
            List<SingleDto> singleDtos = singles.stream()
                .map(CommodityConvertor::getSingleDto)
                .collect(Collectors.toList());
            groupDto.setSingleList(singleDtos);
        }
        return groupDto;
    }

    private static SkuDto getSkuDto(CommodityStoreSku sku) {
        SkuDto skuDto = new SkuDto();
        skuDto.setSkuId(sku.getCommodityStoreSkuId());
        skuDto.setCommoditySkuId(sku.getSkuId());
        skuDto.setSkuName(sku.getCommodityStoreSkuName());
        skuDto.setSkuValue(sku.getCommodityStoreSkuValue());
        skuDto.setCommodityStoreSkuStrikePrice(sku.getCommodityStoreSkuStrikePrice());
        skuDto.setCommodityStoreSkuStatus(sku.getCommodityStoreSkuStatus());
        skuDto.setSkuPrice(sku.getCommodityStoreSkuPrice());
        return skuDto;
    }

    private static SingleDto getSingleDto(CommodityStoreSingle single) {
        SingleDto singleDto = new SingleDto();
        singleDto.setSingleId(single.getCommodityStoreSingleId());
        singleDto.setSingleName(single.getCommodityName());
        singleDto.setImageUrl(single.getCommodityUrl());
        singleDto.setUpPrice(single.getCommodityStoreSinglePrice());
        singleDto.setMarkingPrice(single.getMarkingPrice());
        singleDto.setWxStatus(single.getWxStatus());
        singleDto.setStoreStatus(single.getStoreStatus());

        singleDto.setCommodityStoreSingleCopies(single.getCommodityStoreSingleCopies());
        singleDto.setDefaultChoose(single.getDefaultChoose());
        singleDto.setSkuName(single.getSingleSkuName() != null ? single.getSingleSkuName() : " ");
        singleDto.setCommodityId(single.getCommodityId());
        singleDto.setRequiredChoose(single.getRequiredChoose());
        singleDto.setSingleSkuId(single.getSingleSkuId());
        singleDto.setSingleSkuId(single.getSingleSkuId());
        singleDto.setCommodityStoreSpuId(single.getCommodityStoreSpuId());


        if (ObjectUtil.isNotEmpty(single.getFlavor())){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 进行反序列化操作
                List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(single.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                });
                List<FlavorDto> list = commodityFlavorList.stream().map(CommodityConvertor::getFlavorDto)
                    .toList();
                singleDto.setCommodityFlavors(list);
            } catch (IOException e) {
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
            }
        }

        return singleDto;
    }



    public static CategoryDto convertDoToCategoryDTO(CommodityStoreCategory storeCategory) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryId(storeCategory.getCommodityStoreCategoryId());
        categoryDto.setCategoryName(storeCategory.getCommodityStoreCategoryName());
        categoryDto.setImageUrl(storeCategory.getCommodityStoreCategoryImage());
        categoryDto.setSort(storeCategory.getCommodityStoreCategorySort());
        categoryDto.setType(storeCategory.getType());
        categoryDto.setCommodityStoreCategoryStatus(storeCategory.getCommodityStoreCategoryStatus());
        BeanUtils.copyProperties(storeCategory, categoryDto);
        return categoryDto;
    }


}
