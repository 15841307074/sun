package com.htyoudao.youdao.module.commodity.service.sku;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySkusMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CommoditySkusServiceImpl  implements ICommoditySkusService {

    @Resource
    private CommoditySkusMapper commoditySkusMapper;

    @Override
    public Map<String, Object> saveBatch(List<CommoditySkus> commoditySkusList) {
       /* if (ObjectUtil.isNotEmpty(commoditySkusList)) {
            commoditySkusMapper.insertOrUpdate(commoditySkusList);

        }*/
        boolean isChange = false;
        // 新增商品规格标识 1 规格出现新增场景
        Integer skuFlag = 0;
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<CommoditySkus> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(CommoditySkus::getCommodityId, commoditySkusList.get(0).getCommodityId());
        List<CommoditySkus> commoditySkuses = commoditySkusMapper.selectList(queryWrapper1);

        List<Long> oldIds = commoditySkuses.stream().map(CommoditySkus::getSkuId).toList();

        List<Long> newIds = commoditySkusList.stream().map(CommoditySkus::getSkuId).filter(Objects::nonNull) .toList();

        boolean isEqual = oldIds.size() == newIds.size()
                && new HashSet<>(oldIds).containsAll(newIds)
                && new HashSet<>(newIds).containsAll(oldIds);
        if (!isEqual) {
            isChange = true;
        }
        result.put("skuFlag", commoditySkusList.size() > oldIds.size()? 0 : 1);
        if (ObjectUtil.isNotEmpty(commoditySkusList)) {



            // 分离需要插入和更新的记录
            List<CommoditySkus> insertList = new ArrayList<>();
            List<CommoditySkus> updateList = new ArrayList<>();
            List<Long> currentIds = new ArrayList<>(); // 记录传入列表中的所有ID
            for (CommoditySkus sku : commoditySkusList) {
                if (sku.getSkuId() == null) {
                    // 无主键ID，视为新增
                    insertList.add(sku);
                } else {
                    // 有主键ID，检查记录是否存在
                    currentIds.add(sku.getSkuId());
                    // 替换原 exists 调用逻辑，使用 Wrapper 判断记录是否存在
                    LambdaQueryWrapper<CommoditySkus> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(CommoditySkus::getSkuId, sku.getSkuId());

                    if (commoditySkusMapper.exists(queryWrapper)) {
                        // 记录存在，执行更新
                        updateList.add(sku);
                    } else {
                        // 记录不存在，清除ID后执行新增（避免主键冲突）
                        sku.setSkuId(null);
                        insertList.add(sku);
                    }
                }
            }





            LambdaQueryWrapper<CommoditySkus> queryWrapper = new LambdaQueryWrapper<>();
            if (!currentIds.isEmpty()) {
                queryWrapper.notIn(CommoditySkus::getSkuId, currentIds);
            }

            queryWrapper.eq(CommoditySkus::getCommodityId, commoditySkusList.get(0).getCommodityId());
            commoditySkusMapper.delete(queryWrapper);

            // 执行批量插入
            if (!insertList.isEmpty()) {
                commoditySkusMapper.insertBatch(insertList);
                isChange = true;
            }

            // 执行批量更新
            if (!updateList.isEmpty()) {
                if (!isChange){
                   isChange =  compareSkusLists(commoditySkuses,updateList);
                }
                commoditySkusMapper.updateBatch(updateList);
            }
        }
        result.put("isChange", isChange);
        return result;
    }

    /**
     * 比对两个CommoditySkus列表
     * 在skuId一致的情况下比对指定字段，有任何不匹配则返回true
     *
     * @param list1 第一个商品SKU列表
     * @param list2 第二个商品SKU列表
     * @return 所有匹配skuId的字段都相同返回false，否则返回true
     */
    public static boolean compareSkusLists(List<CommoditySkus> list1, List<CommoditySkus> list2) {
        // 先构建第一个列表的skuId到对象的映射
        Map<Long, CommoditySkus> skuMap = new HashMap<>();
        for (CommoditySkus sku : list1) {
            skuMap.put(sku.getSkuId(), sku);
        }

        // 遍历第二个列表进行比对
        for (CommoditySkus sku : list2) {
            Long skuId = sku.getSkuId();
            // 检查第一个列表中是否存在相同的skuId
            if (skuMap.containsKey(skuId)) {
                CommoditySkus otherSku = skuMap.get(skuId);

                // 比对skusName
                if (!equals(sku.getSkusName(), otherSku.getSkusName())) {
                    return true;
                }

                // 比对skusValue
                if (!equals(sku.getSkusValue(), otherSku.getSkusValue())) {
                    return true;
                }

                if (!(sku.getIllustratePrices().compareTo(otherSku.getIllustratePrices())==0)){
                    return true;
                }
                if (!(sku.getStrikeThroughPrice().compareTo(otherSku.getStrikeThroughPrice())==0)){
                    return true;
                }

            }
        }

        return false;
    }

    /**
     * 安全比较两个对象，处理null情况
     */
    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    @Override
    public void deleteBySpuId(Long commodityId) {
        LambdaQueryWrapper<CommoditySkus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommoditySkus::getCommodityId, commodityId);
        commoditySkusMapper.delete(queryWrapper);
    }

    @Override
    public List<CommoditySkus> selectBySpuIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommoditySkus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommoditySkus::getCommodityId, commodityIds);


        return commoditySkusMapper.selectList(queryWrapper);
    }

    @Override
    public List<CommoditySkus> selectBySpuId(Long id) {

        LambdaQueryWrapper<CommoditySkus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommoditySkus::getCommodityId, id);

        return commoditySkusMapper.selectList(queryWrapper);
    }

    @Override
    public Long selectSkuIdCountByCommodityId(Long commodityId) {
        return commoditySkusMapper.selectCount(CommoditySkus::getCommodityId, commodityId);
    }
}
