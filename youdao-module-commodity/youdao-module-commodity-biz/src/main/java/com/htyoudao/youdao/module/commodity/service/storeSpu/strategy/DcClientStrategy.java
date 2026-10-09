package com.htyoudao.youdao.module.commodity.service.storeSpu.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.htyoudao.youdao.module.commodity.constant.CommodityConstant;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.GroupDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.htyoudao.youdao.module.commodity.enums.CommodityPackageType;
import org.springframework.util.CollectionUtils;

public class DcClientStrategy implements CommodityClientStrategy {

    //
    private static final String ALL_CATEGORY_NAME = "全部";


    @Override
    public List<StoreCategoryDTO> filter(List<StoreCategoryDTO> categoryDTOS) {

        if (CollectionUtils.isEmpty(categoryDTOS)) {
            categoryDTOS = new ArrayList<>();
        }

        //移动下架spu
        for (StoreCategoryDTO categoryDTO : categoryDTOS) {
            categoryDTO.setItems(moveDownItems(categoryDTO.getItems()));
        }

        //过滤掉没有商品的分类
        categoryDTOS.removeIf(categoryDTO -> CollectionUtils.isEmpty(categoryDTO.getItems()));
        //过滤掉下架的分类
        categoryDTOS.removeIf(categoryDTO ->
            Objects.equals(categoryDTO.getCategory().getCommodityStoreCategoryStatus(),0));

        //增加全部分类
        categoryDTOS.add(0,
            new StoreCategoryDTO().setCategory(new CategoryDto().setCategoryName(ALL_CATEGORY_NAME))
        );

        return categoryDTOS;
    }

    /**
     * 移动下架spu
     * @param items
     */
    private List<SpuDto> moveDownItems(List<SpuDto> items) {
        if (CollectionUtils.isEmpty(items)){
            return items;
        }

        //下架 或 启用分时置顶没满足条件
        Predicate<SpuDto> shouldMovePredicate = item ->
            Objects.equals(item.getCommodityStoreSpuMachineStatus(), CommodityConstant.DISABLE)
                || (!item.getIsUp() && Objects.equals(item.getTimeSharingTopping(), CommodityConstant.ENABLE));

        List<SpuDto> itemsToMove = items.stream()
            .filter(shouldMovePredicate)
            .toList();

        List<SpuDto> objects = new ArrayList<>(items);
        objects.removeAll(itemsToMove);
        objects.addAll(itemsToMove);

        // 过滤加价区分组 + 移除无有效商品的套餐
        /*objects.removeIf(item -> {
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
                                && group.getSingleList().stream().anyMatch(single -> single.getStoreStatus() == 1);
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
        });*/
        return objects;

    }


}
