package com.htyoudao.youdao.module.commodity.service.storeSingle;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityFlavorDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSingleMapper;
import com.htyoudao.youdao.module.commodity.enums.SpuEnum;
import com.htyoudao.youdao.module.commodity.util.TruncateTableUtil;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 门店下商品套餐分组里的单品表 服务实现类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Service
@Slf4j
public class CommodityStoreSingleServiceImpl extends ServiceImpl<CommodityStoreSingleMapper, CommodityStoreSingle> implements ICommodityStoreSingleService {

    @Resource
    private CommodityStoreSingleMapper commodityStoreSingleMapper;

    @Autowired
    private TruncateTableUtil truncateTableUtil;

    @Override
    public void deleteSpuByStoreId(Long storeId) {
        LambdaQueryWrapper<CommodityStoreSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        commodityStoreSingleMapper.delete(queryWrapper);
    }

    @Override
    public void deleteAllStoreSpu() {
        try {

            truncateTableUtil.safeTruncateTable("commodity_store_single");



        } catch (SQLException e) {
            log.error("清空分类表失败", e);
            throw new RuntimeException("全表清理失败", e);
        }


    }

    @Override
    public void deleteBySpuIds(List<Long> spuIds) {
        LambdaQueryWrapper<CommodityStoreSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityStoreSingle::getCommodityStoreSpuId, spuIds);
        commodityStoreSingleMapper.delete(queryWrapper);
    }

    @Override
    public List<CommodityStoreSingle> selectByChooseViewAndSpuId(Long commodityId, Integer chooseView, Long storeId) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();

        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)) {
            singleLambdaQueryWrapper.eq(CommodityStoreSingle::getWxStatus, 1);
        } else {
            singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreStatus, 1);
        }
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        //找到门店下这个商品原始 id 的子品
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    @Override
    public List<CommodityStoreSingle> selectByGroupIdsAndStoreId(List<Long> groupIds, Long storeId) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper1.in(CommodityStoreSingle::getCommodityStoreGroupId, groupIds);

        singleLambdaQueryWrapper1.eq(CommodityStoreSingle::getStoreId, storeId);
        //分组下把所有品查出来
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper1);
    }

    @Override
    public List<Long> updateDownStatusBySpuIdAndChooseView(Long storeId, Long commodityId, Integer chooseView) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId, storeId);

        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)) {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getWxStatus, 0);
        } else {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getStoreStatus, 0);
        }
        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);

        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        singleLambdaQueryWrapper.select(CommodityStoreSingle::getCommodityStoreSpuId);
        List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);
        return commodityStoreSingles.stream().map(CommodityStoreSingle::getCommodityStoreSpuId).toList();


    }

    @Override
    public List<Long> updateUpStatusBySpuId(Long storeId, Long commodityId, Integer chooseView) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        if (chooseView.equals(1)) {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getWxStatus, 1);
        } else {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getStoreStatus, 1);
        }

        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);


        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        singleLambdaQueryWrapper.select(CommodityStoreSingle::getCommodityStoreGroupId);
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper).stream().map(CommodityStoreSingle::getCommodityStoreGroupId).toList();


    }

    @Override
    public CommodityStoreSingle getOne(Long commodityStoreSingleId) {


        return commodityStoreSingleMapper.selectById(commodityStoreSingleId);
    }

    @Override
    public void updateWxStatusBySingleId(Long commodityStoreSingleId, Integer wxStatus) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityStoreSingleId, commodityStoreSingleId);

        singleLambdaUpdateWrapper.set(CommodityStoreSingle::getWxStatus, wxStatus);


        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);
    }

    @Override
    public void updateStoreStatusBySingleId(Long commodityStoreSingleId, Integer storeStatus) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityStoreSingleId, commodityStoreSingleId);

        singleLambdaUpdateWrapper.set(CommodityStoreSingle::getWxStatus, storeStatus);


        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);
    }


    @Override
    public List<CommodityStoreSingle> selectByGroupId(Long commodityStoreGroupId) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper1.eq(CommodityStoreSingle::getCommodityStoreGroupId, commodityStoreGroupId);

        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper1);
    }

    @Override
    public void deleteByLambda(LambdaQueryWrapper<CommodityStoreSingle> in) {
        commodityStoreSingleMapper.delete(in);
    }



    @Override
    public List<CommodityStoreSingle> selectByGroupIds(List<Long> groupIds) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper1.in(CommodityStoreSingle::getCommodityStoreGroupId, groupIds);


        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper1);
    }


    @Override
    public List<CommodityStoreSingle> selectBySpuIds(List<Long> spuIds) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper1.in(CommodityStoreSingle::getSpuId, spuIds);
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper1);
    }

    @Override
    public void deletByGroupIds(List<Long> groups) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper1.in(CommodityStoreSingle::getCommodityStoreGroupId, groups);
        commodityStoreSingleMapper.delete(singleLambdaQueryWrapper1);
    }

    @Override
    public void saveBatch(List<CommodityStoreSingle> singleList) {
        commodityStoreSingleMapper.insertBatch(singleList);
    }

    @DataPermission(enable = false)
    @PermitAll
    @Override
    public List<StoreSingleInfoDTO> selectSingleListForRpc(Set<Long> singleIds) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityStoreSingle::getCommodityStoreSingleId, singleIds);
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getDeleted, 0);
        List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);

        return commodityStoreSingles.stream().map(single -> {
            StoreSingleInfoDTO singleInfoDTO = new StoreSingleInfoDTO();
            singleInfoDTO.setSingleId(single.getCommodityStoreSingleId());
            singleInfoDTO.setSpuId(single.getCommodityStoreSpuId());
            singleInfoDTO.setCommodityId(single.getCommodityId());
            singleInfoDTO.setSpuName(single.getCommodityName());
            singleInfoDTO.setSkuName(single.getSingleSkuName());
            singleInfoDTO.setImageUrl(single.getCommodityUrl());
            singleInfoDTO.setSinglePrice(single.getCommodityStoreSinglePrice());
            singleInfoDTO.setWxStatus(single.getWxStatus());
            singleInfoDTO.setStoreStatus(single.getStoreStatus());
            singleInfoDTO.setOriginalSkuId(single.getSingleSkuId());
            singleInfoDTO.setCommodityFlavors(JSON.parseArray(single.getFlavor(), CommodityFlavorDTO.class));

            return singleInfoDTO;
        }).toList();
    }

    @Override
    public List<Long> updateNameBySingleSpu(CommodityStoreUpdateReqVo commodityStoreSpu) {


        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId,commodityStoreSpu.getStoreId());
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getCommodityId,commodityStoreSpu.getCommodityStorePrimitiveSpuId());
        List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);

        if (ObjectUtil.isNotEmpty(commodityStoreSingles)) {
          /*  LambdaUpdateWrapper<CommodityStoreSingle> storeSingleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSingleLambdaUpdateWrapper.set(CommodityStoreSingle::getCommodityName,commodityStoreSpu.getCommodityStoreSpuName());
            //0806 更换了门店该单品的图片 那么 在这个门店所包含该单品的套餐图片也会同步更换
            storeSingleLambdaUpdateWrapper.set(CommodityStoreSingle::getCommodityUrl,commodityStoreSpu.getImageUrlVO().get(0));

            storeSingleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId,commodityStoreSpu.getStoreId());
            storeSingleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityId,commodityStoreSpu.getCommodityStorePrimitiveSpuId());
            commodityStoreSingleMapper.update(storeSingleLambdaUpdateWrapper);*/
            String commodityStoreSpuName = commodityStoreSpu.getCommodityStoreSpuName();
            for (CommodityStoreSingle commodityStoreSingle : commodityStoreSingles) {
                commodityStoreSingle.setCommodityUrl(commodityStoreSpu.getImageUrlVO().get(0));
                String oldName = commodityStoreSingle.getCommodityName();
                String newName ;
                // 检查是否包含括号
                if (oldName.contains("(") && oldName.contains(")")) {
                    // 提取括号部分（例如"（中）"）
                    String suffix = oldName.substring(oldName.indexOf("("));
                    // 组合新名称：新前缀 + 原有括号部分
                    newName = commodityStoreSpuName + suffix;
                } else {
                    // 不包含括号，直接替换为新名称
                    newName = commodityStoreSpuName;
                }
                commodityStoreSingle.setCommodityName(newName);

            }
            commodityStoreSingleMapper.updateBatch(commodityStoreSingles);



            return commodityStoreSingles.stream().map(CommodityStoreSingle::getCommodityStoreSpuId).toList();
        }else {
            return new ArrayList<>();
        }

    }

    @Override
    public List<CommodityStoreSingle> selectSpuIds(List<Long> commodityId, Long storeId) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();

        singleLambdaQueryWrapper.in(CommodityStoreSingle::getCommodityId, commodityId);
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        //找到门店下这个商品原始 id 的子品
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);
    }

    @Override
    public List<Long> updateUpStatusBySpuIdNew(Long storeId, List<Long> primitiveSpuIds, Integer chooseView,Integer upOrDown) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaUpdateWrapper.in(CommodityStoreSingle::getCommodityId, primitiveSpuIds);
        if (chooseView.equals(SpuEnum.WX.getCode())) {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getWxStatus, upOrDown);
        } else {
            singleLambdaUpdateWrapper.set(CommodityStoreSingle::getStoreStatus, upOrDown);
        }

        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);


        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaQueryWrapper.in(CommodityStoreSingle::getCommodityId, primitiveSpuIds);
        singleLambdaQueryWrapper.select(CommodityStoreSingle::getCommodityStoreSpuId);
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper).stream().map(CommodityStoreSingle::getCommodityStoreSpuId).distinct().toList();
    }

    /**
     * 通过门店下套餐 Ids 获得子品
     * @param packageIDs
     * @return
     */
    @Override
    public List<CommodityStoreSingle> selectByStoreSpuIds(List<Long> packageIDs) {
        LambdaQueryWrapper<CommodityStoreSingle> singleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        singleLambdaQueryWrapper.in(CommodityStoreSingle::getCommodityStoreSpuId, packageIDs);
        return commodityStoreSingleMapper.selectList(singleLambdaQueryWrapper);
    }


    @Override
    public void emitSyncToSubProducts(String flavorJson, Long commodityId, Long storeId) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityId, commodityId);
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaUpdateWrapper.set(CommodityStoreSingle::getFlavor, flavorJson);
        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);
    }

    @Override
    public List<Long> uodateSingleFlavor(String flavorJson, Long commodityStorePrimitiveSpuId, Long storeId) {
        LambdaUpdateWrapper<CommodityStoreSingle> singleLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getCommodityId, commodityStorePrimitiveSpuId);
        singleLambdaUpdateWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        singleLambdaUpdateWrapper.set(CommodityStoreSingle::getFlavor, flavorJson);
        commodityStoreSingleMapper.update(singleLambdaUpdateWrapper);
        // 查询被修改记录的 commodityStoreSpuId
        LambdaQueryWrapper<CommodityStoreSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSingle::getCommodityId, commodityStorePrimitiveSpuId);
        queryWrapper.eq(CommodityStoreSingle::getStoreId, storeId);
        queryWrapper.select(CommodityStoreSingle::getCommodityStoreSpuId);
        List<CommodityStoreSingle> resultList = commodityStoreSingleMapper.selectList(queryWrapper);

        // 提取 commodityStoreSpuId 并返回
        return resultList.stream()
                .map(CommodityStoreSingle::getCommodityStoreSpuId)
                .filter(Objects::nonNull) // 过滤空值，避免空指针
                .toList();
    }
}
