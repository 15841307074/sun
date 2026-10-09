package com.htyoudao.youdao.module.commodity.service.storeSku;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSkuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreSkuDTO;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSkuMapper;
import com.htyoudao.youdao.module.commodity.util.TimeSharingUtil;
import com.htyoudao.youdao.module.commodity.util.TruncateTableUtil;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.STORE_SKU_NO_HAVE;

/**
 * <p>
 * 门店下商品规格表 服务实现类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Service
@Slf4j
public class CommodityStoreSkuServiceImpl extends ServiceImpl<CommodityStoreSkuMapper, CommodityStoreSku> implements ICommodityStoreSkuService {

    @Resource
    private CommodityStoreSkuMapper commodityStoreSkuMapper;


    @Autowired
    private TruncateTableUtil truncateTableUtil;

    @Override
    public void deleteSpuByStoreId(Long storeId) {
        LambdaQueryWrapper<CommodityStoreSku> skuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        skuLambdaQueryWrapper.eq(CommodityStoreSku::getStoreId, storeId);
        commodityStoreSkuMapper.delete(skuLambdaQueryWrapper);
    }

    @Override
    public void deleteAllStoreSpu() {

        try {

            truncateTableUtil.safeTruncateTable("commodity_store_sku");


        } catch (SQLException e) {
            log.error("清空分类表失败", e);
            throw new RuntimeException("全表清理失败", e);
        }


       // commodityStoreSkuMapper.delete(queryWrapper);
    }

    @Override
    public void deleteBySpuIds(List<Long> spuIds) {
        LambdaQueryWrapper<CommodityStoreSku> skuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        skuLambdaQueryWrapper.in(CommodityStoreSku::getCommodityStoreSpuId, spuIds);
        commodityStoreSkuMapper.delete(skuLambdaQueryWrapper);
    }


    @Override
    public List<CommodityStoreSku> selectBySpuId(Long commodityStoreSpuId) {
        LambdaQueryWrapper<CommodityStoreSku> skusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        skusLambdaQueryWrapper.eq(CommodityStoreSku::getCommodityStoreSpuId, commodityStoreSpuId);
        return commodityStoreSkuMapper.selectList(skusLambdaQueryWrapper);
    }

    @Override
    public void deleteByLambda(LambdaQueryWrapper<CommodityStoreSku> eq) {
        commodityStoreSkuMapper.delete(eq);
    }

    @Override
    public void saveBatch(List<CommodityStoreSku> commoditySkusList) {

        if (ObjectUtil.isNotEmpty(commoditySkusList)) {
            // 分离需要插入和更新的记录
            List<CommodityStoreSku> insertList = new ArrayList<>();
            List<CommodityStoreSku> updateList = new ArrayList<>();
            List<Long> currentIds = new ArrayList<>();
            for (CommodityStoreSku commodityStoreSku : commoditySkusList) {
                if (commodityStoreSku.getCommodityStoreSkuId()==null){
                    insertList.add(commodityStoreSku);
                }else {
                    currentIds.add(commodityStoreSku.getCommodityStoreSkuId());
                    LambdaQueryWrapper<CommodityStoreSku> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(CommodityStoreSku::getCommodityStoreSkuId, commodityStoreSku.getCommodityStoreSkuId());
                    if (commodityStoreSkuMapper.exists(queryWrapper)) {
                        // 记录存在，执行更新
                        updateList.add(commodityStoreSku);
                    }else {
                        // 记录不存在，清除ID后执行新增（避免主键冲突）
                        commodityStoreSku.setCommodityStoreSkuId(null);
                        insertList.add(commodityStoreSku);
                    }
                }
            }


            LambdaQueryWrapper<CommodityStoreSku> queryWrapper = new LambdaQueryWrapper<>();
            if (!currentIds.isEmpty()) {
                queryWrapper.notIn(CommodityStoreSku::getCommodityStoreSkuId, currentIds);
            }
            queryWrapper.eq(CommodityStoreSku::getCommodityStoreSpuId,commoditySkusList.get(0).getCommodityStoreSpuId());
            commodityStoreSkuMapper.delete(queryWrapper);
            if (!insertList.isEmpty()) {
                commodityStoreSkuMapper.insertBatch(insertList);
            }
            if (!updateList.isEmpty()) {
                commodityStoreSkuMapper.updateBatch(updateList);
            }
        }


        //commodityStoreSkuMapper.insertBatch(commoditySkusList);
    }

    @Override
    public List<CommodityStoreSku> selectBySpuIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommodityStoreSku> skusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        skusLambdaQueryWrapper.in(CommodityStoreSku::getCommodityStoreSpuId, commodityIds);

        return commodityStoreSkuMapper.selectList(skusLambdaQueryWrapper);
    }

    @Override
    public void saveOrUpdateBatch(List<CommodityStoreSku> commodityStoreSkuList) {
        commodityStoreSkuMapper.insertOrUpdate(commodityStoreSkuList);
    }

    @Override
    public void updateByDc(DCSpuUpdateReqVO reqVO) {
        if (ObjectUtil.isEmpty(reqVO.getSkuList())) {
            throw exception(STORE_SKU_NO_HAVE);
        }
        for (DCSkuUpdateReqVO dcSkuUpdateReqVO : reqVO.getSkuList()) {
            LambdaUpdateWrapper<CommodityStoreSku> storeSkuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSkuLambdaUpdateWrapper.eq(CommodityStoreSku::getCommodityStoreSkuId, dcSkuUpdateReqVO.getCommodityStoreSkuId());
            storeSkuLambdaUpdateWrapper.set(CommodityStoreSku::getCommodityStoreSkuStatus, dcSkuUpdateReqVO.getCommodityStoreSkuStatus());
            storeSkuLambdaUpdateWrapper.set(CommodityStoreSku::getUpdater,reqVO.getLoginUserId());
            storeSkuLambdaUpdateWrapper.set(CommodityStoreSku::getUpdateTime,new Date());
            commodityStoreSkuMapper.update(storeSkuLambdaUpdateWrapper);
        }
    }

    @DataPermission(enable = false)
    @PermitAll
    @Override
    public List<StoreSkuInfoDTO> selectSkuListForRpc(Set<Long> storeSkuIds) {
        List<StoreSkuDTO> storeSkuDTOS = commodityStoreSkuMapper.selectSkuListForRpc(storeSkuIds);
        TimeSharingUtil.processTimeBasedList(storeSkuDTOS);
        return BeanCopyUtils.copyBeanList(storeSkuDTOS, StoreSkuInfoDTO.class);
    }

    @DataPermission(enable = false)
    @PermitAll
    @Override
    public List<StoreSkuInfoDTO> selectSkuListByCommodityIdsForRpc(Long storeId, Set<Long> commodityIds) {
        if (commodityIds == null || commodityIds.isEmpty()) {
            return List.of();
        }
        List<StoreSkuDTO> storeSkuDTOS = commodityStoreSkuMapper
                .selectSkuListByCommodityIdsForRpc(storeId, commodityIds);
        TimeSharingUtil.processTimeBasedList(storeSkuDTOS);
        return BeanCopyUtils.copyBeanList(storeSkuDTOS, StoreSkuInfoDTO.class);
    }
}
