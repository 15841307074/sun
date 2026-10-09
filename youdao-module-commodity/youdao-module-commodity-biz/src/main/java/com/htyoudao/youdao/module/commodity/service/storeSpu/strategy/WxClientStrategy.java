package com.htyoudao.youdao.module.commodity.service.storeSpu.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.module.commodity.constant.CommodityConstant;
import com.htyoudao.youdao.module.commodity.dal.dto.GroupDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SingleDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SkuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.enums.CommodityPackageType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WxClientStrategy implements CommodityClientStrategy {

    @Override
    public List<StoreCategoryDTO> filter(List<StoreCategoryDTO> categoryDTOS) {
        for (StoreCategoryDTO categoryDTO : categoryDTOS) {
            //小程序过滤掉下架商品
            categoryDTO.setItems(filterDownItem(categoryDTO.getItems()));

            //设置最低价格
            fillMinPrices(categoryDTO.getItems());
        }

        return categoryDTOS.stream()
            //过滤掉没有商品的分类
            .filter(categoryDTO -> CollectionUtils.isNotEmpty(categoryDTO.getItems()))
            //过滤掉下架的分类z
            .filter(c -> Objects.equals(c.getCategory().getCommodityStoreCategoryStatus(), 1)).toList();
    }


    /**
     * 计算并设置每个 SPU 的最低售价和最低划线价
     *
     * @param spuList List<SpuDto>
     */
    private void fillMinPrices(List<SpuDto> spuList) {
        if (spuList == null || spuList.isEmpty()) {
            return;
        }

        for (SpuDto spu : spuList) {
            if (spu == null) {
                continue;
            }

            List<SkuDto> skuList = spu.getSkuList();
            if (skuList == null || skuList.isEmpty()) {
                continue;
            }

            BigDecimal minPrice = null;
            BigDecimal minStrikePrice = null;

            for (SkuDto sku : skuList) {
                if (sku == null) {
                    continue;
                }

                BigDecimal price = sku.getSkuPrice();
                BigDecimal strikePrice = sku.getCommodityStoreSkuStrikePrice();

                if (price != null && (minPrice == null || price.compareTo(minPrice) < 0)) {
                    minPrice = price;
                }

                if (strikePrice != null && (minStrikePrice == null || strikePrice.compareTo(minStrikePrice) < 0)) {
                    minStrikePrice = strikePrice;
                }
            }

            spu.setSpuPrice(minPrice);
            spu.setSpuUnderlinedPrice(minStrikePrice);
        }
    }


    /**
     * 小程序过滤掉下架商品
     *
     * @param items
     */
    private List<SpuDto> filterDownItem(List<SpuDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            return items;
        }
        List<SpuDto> newItems = new ArrayList<>(items);

        //过滤掉直接下架的品
        newItems.removeIf(s -> Objects.equals(s.getCommodityStoreSpuAppletStatus(), CommodityConstant.DISABLE));

        //过滤掉未满足指定时间上下架的品
        newItems.removeIf(s -> !s.getIsUp() && Objects.equals(s.getTimeSharingTopping(), CommodityConstant.ENABLE));

        //过滤掉仅套餐售卖,仅小料售卖的品（仅兑换售卖在小程序侧不隐藏）
        newItems.removeIf(s -> Objects.equals(s.getSaleRule(), 1) || Objects.equals(s.getSaleRule(), 2));

        // 过滤加价区分组 + 移除无有效商品的套餐
        newItems.removeIf(item -> {
            // 1. 非可选套餐 → 不删除
            if (!Objects.equals(item.getSetmealType(), CommodityPackageType.GROUP_SELECTABLE.getCode())) {
                return false;
            }

            List<GroupDto> groupList = item.getGroupList();
            // 无分组 → 直接删除商品
            if (CollectionUtil.isEmpty(groupList)) {
                return true;
            }

            // 过滤空加价区分组
            List<GroupDto> filteredGroups = groupList.stream()
                    .filter(group -> {
                        // 非加价区 → 保留
                        if (group.getCommodityStoreGroupAttribute() != 3) {
                            return true;
                        }
                        // 加价区 → 必须有上架单品才保留
                        return CollectionUtil.isNotEmpty(group.getSingleList())
                                && group.getSingleList().stream().anyMatch(single -> single.getWxStatus() == 1);
                    })
                    .toList();

            // 把过滤后的分组重新设置回去
            item.setGroupList(filteredGroups);

            // 判断是否删除商品
            // 过滤后已经没有任何分组 → 删除
            if (CollectionUtil.isEmpty(filteredGroups)) {
                return true;
            }

            // 所有分组都是加价区 + 没有任何上架单品 → 删除
            boolean allPriceArea = filteredGroups.stream()
                    .allMatch(g -> g.getCommodityStoreGroupAttribute() == 3);

            boolean noOnSaleItems = filteredGroups.stream()
                    .flatMap(g -> g.getSingleList().stream())
                    .noneMatch(s -> s.getWxStatus() == 1);

            return allPriceArea && noOnSaleItems;
        });

        for (SpuDto item : newItems) {
            item.setSkuList(new ArrayList<>(item.getSkuList()));

            //过滤掉下架规格
            item.getSkuList().removeIf(sku -> CommodityConstant.DISABLE.equals(sku.getCommodityStoreSkuStatus()));

            //过滤掉下架属性
            item.setCommodityCondiments(item.getCommodityCondiments().stream()
                .filter(c -> Objects.equals(c.getStatus(), CommodityConstant.ENABLE)).toList());

            //过滤掉下架小料
            item.setCommodityFlavors(
                item.getCommodityFlavors().stream().filter(f -> CollectionUtils.isNotEmpty(f.getFlavorValues()))
                    .toList());

            //过滤掉下架子品
            for (GroupDto groupDto : item.getGroupList()) {
                groupDto.getSingleList().removeIf(g -> Objects.equals(g.getWxStatus(), CommodityConstant.DISABLE));

                for (SingleDto singleDto : groupDto.getSingleList()) {
                    //过滤掉下架小料
                    singleDto.setCommodityFlavors(
                        singleDto.getCommodityFlavors().stream().filter(f -> CollectionUtils.isNotEmpty(f.getFlavorValues()))
                            .toList());
                }
            }
        }

        //过滤掉没有规格的品
        return newItems.stream().filter(p -> CollectionUtils.isNotEmpty(p.getSkuList())).toList();
    }

}
