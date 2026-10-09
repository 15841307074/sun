package com.htyoudao.youdao.module.commodity.service.inventory.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakeSaveReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakeSaveReq.MaterialInfo;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialMapper;
import com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.RawMaterialStocktakeService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_MODIFY_STATUS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_MODIFY_STATUS_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_REMOVE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_REMOVE_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_SAVE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_SAVE_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_UPDATE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_UPDATE_SUCCESS;

@Slf4j
@Service
@EnableAspectJAutoProxy(exposeProxy=true)
public class RawMaterialServiceImpl implements IRawMaterialService {

    @Resource
    private RawMaterialMapper rawMaterialMapper;

    @Resource
    private RawMaterialLossRecordService lossService;

    @Resource
    private RowMaterialStockFlowService stockService;

    @Resource
    private RawMaterialStocktakeService stocktakeService;

    @Resource
    private CommodityRecipeMapper commodityRecipeMapper;


    @Resource
    private RawMaterialLogService rawMaterialLogService;

    @Resource
    private ICommoditySpusService commoditySpusService;


    @Resource
    private CommodityRecipeMaterialMapper commodityRecipeMaterialMapper;



    @LogRecord(type = RAW_MATERIAL, subType = RAW_MATERIAL_UPDATE, bizNo = "{{#rawMaterial.id}}",
        success = RAW_MATERIAL_UPDATE_SUCCESS)
    public void updateMaterial(SaveMaterialReqVO saveReqVO) {
        RawMaterial oldMaterial = rawMaterialMapper.selectById(saveReqVO.getId());
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldMaterial, SaveMaterialReqVO.class));

        RawMaterial rawMaterial = BeanCopyUtils.copyBean(saveReqVO, RawMaterial.class);
        //不管调拨到 门店 还是采购到门店 之前的下架状态都改成在售
        rawMaterial.setIsEnable(1);
        rawMaterial.setStock(null);
        rawMaterialMapper.updateById(rawMaterial);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("rawMaterial", rawMaterial);
    }

    @Override
    @Transactional
    public void batchSaveOrUpdate(List<SaveMaterialReqVO> saveMaterialReqVOS) {
        IRawMaterialService o =(IRawMaterialService)  AopContext.currentProxy();

        for (SaveMaterialReqVO reqVo : saveMaterialReqVOS) {
            RawMaterial oldMaterial = this.selectMaterial(reqVo.getStoreId(), reqVo.getCommodityCode());
            if (oldMaterial == null){
                o.saveMaterial(reqVo);
            }else {
                reqVo.setId(oldMaterial.getId());
                o.updateMaterial(reqVo);
            }
        }
    }


    @LogRecord(type = RAW_MATERIAL, subType = RAW_MATERIAL_SAVE, bizNo = "{{#rawMaterial.id}}", success = RAW_MATERIAL_SAVE_SUCCESS)
    public void saveMaterial(SaveMaterialReqVO saveReqVO) {

        RawMaterial rawMaterial = BeanCopyUtils.copyBean(saveReqVO, RawMaterial.class);
        //不管调拨到 门店 还是采购到门店 之前的下架状态都改成在售
        rawMaterial.setIsEnable(1);
        rawMaterial.setId(null);
        rawMaterial.setStock(BigDecimal.ZERO);
        rawMaterialMapper.insert(rawMaterial);

        LogRecordContext.putVariable("rawMaterial", rawMaterial);
    }

    @Override
    public RawMaterialRespVO queryMaterialList(SelectMaterialListReqVO reqVO) {
        LambdaQueryWrapperX<RawMaterial> queryWrapper = new LambdaQueryWrapperX();
        queryWrapper.eqIfPresent(RawMaterial::getStoreId, reqVO.getStoreId());
        queryWrapper.likeIfPresent(RawMaterial::getCategoryName, reqVO.getCategoryName());
        queryWrapper.likeIfPresent(RawMaterial::getStasticsCommodityName, reqVO.getStasticsCommodityName());
        queryWrapper.likeIfPresent(RawMaterial::getCommodityName, reqVO.getCommodityName());
        queryWrapper.eqIfPresent(RawMaterial::getIsEnable, reqVO.getIsEnable());

        // 先按 isEnable 降序（1在前，0在后）, 然后根据库存排序
        queryWrapper.orderByDesc(RawMaterial::getIsEnable).orderBy(true, reqVO.getIsAsc(), RawMaterial::getStock);

        List<RawMaterial> rawMaterials = rawMaterialMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(rawMaterials)) {
            return new RawMaterialRespVO();
        }

        List<RawMaterialDetailVO> details = BeanCopyUtils.copyBeanList(rawMaterials, RawMaterialDetailVO.class);

        // 根据门店ID获取未盘点的损耗单ID集合
        List<Long> lossIds = lossService.lossIdsByStoreTake(reqVO.getStoreId(), null);
        Map<Long, Pair<BigDecimal, BigDecimal>> lossMap = lossService.getLossMap(lossIds);

        // 获取保存时填写的信息
        Map<Long, Pair<String, BigDecimal>> prepareMap = getPrepareMap(reqVO.getStoreId());

        for (RawMaterialDetailVO respVO : details) {
            Long id = respVO.getId();

            //获取保存时填写的信息
            Pair<String, BigDecimal> preparePair = prepareMap.getOrDefault(id, Pair.of("", BigDecimal.ZERO));

            //根据 选择单位和换算规则 计算出最小单位需要乘的比例
            BigDecimal rate = respVO.caleRateByChooseUnit(preparePair.getKey());

            //设置保存时填写的信息
            respVO.setChooseUnit(preparePair.getKey());
            respVO.setTakeCount(preparePair.getValue() == null ? BigDecimal.ZERO : preparePair.getValue());

            //设置损耗值
            Pair<BigDecimal, BigDecimal> emptyPair = Pair.of(BigDecimal.ZERO, BigDecimal.ZERO);
            Pair<BigDecimal, BigDecimal> lossPair = lossMap.getOrDefault(id, emptyPair);
            respVO.setLossCount(lossPair.getKey().multiply(rate));
            respVO.setLossAmount(lossPair.getValue());

            //根据换算比例，计算展示金额
            respVO.caleAmountByRate(rate);
        }
        RawMaterialRespVO respVO = new RawMaterialRespVO();
        respVO.setDetails(details);
        RawMaterialTotalRespVO totalRespVO = caleTotalVo(details);

        BigDecimal outAmount = stocktakeService.selectOutboundAmount(reqVO.getStoreId());
        totalRespVO.setOutAmount(outAmount);
        BigDecimal channelOutAmount = stocktakeService.selectChannelOutmount(reqVO.getStoreId());
        totalRespVO.setChannelOutAmount(channelOutAmount);
        totalRespVO.setAllOutAmount(outAmount.add(channelOutAmount));
        respVO.setTotal(totalRespVO);
        return respVO;
    }

    @Override
    public RawMaterialTotalRespVO caleTotalVo(List<RawMaterialDetailVO> details) {

        RawMaterialTotalRespVO respVO = new RawMaterialTotalRespVO();
        respVO.setStock(details.stream().map(RawMaterialDetailVO::getStock).reduce(BigDecimal.ZERO, BigDecimal::add));
        respVO.setTakeCount(details.stream().map(RawMaterialDetailVO::getTakeCount).reduce(BigDecimal.ZERO, BigDecimal::add));
        respVO.setInventoryDifference(respVO.getTakeCount().subtract(respVO.getStock()));

        respVO.setStockAmount(details.stream().map(RawMaterialDetailVO::getStockAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        respVO.setTakeAmount(details.stream().map(RawMaterialDetailVO::getTakeAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        respVO.setInventoryDifferenceAmount(respVO.getTakeAmount().subtract(respVO.getStockAmount()));

        respVO.setLossCount(details.stream().map(RawMaterialDetailVO::getLossCount).reduce(BigDecimal.ZERO, BigDecimal::add));
        respVO.setLossAmount(details.stream().map(RawMaterialDetailVO::getLossAmount).reduce(BigDecimal.ZERO, BigDecimal::add));



        return respVO;
    }

    /**
     * 获取保存时填写的信息
     *
     * @param storeId
     * @return
     */
    private Map<Long, Pair<String, BigDecimal>> getPrepareMap(Long storeId) {

        MaterialStocktakeSaveReq saveReq = stocktakeService.getPrepareList(storeId);

        if (saveReq == null || CollectionUtils.isEmpty(saveReq.getMaterialInfos())) {
            return Map.of();
        }
        Map<Long, Pair<String, BigDecimal>> map = new HashMap<>();
        for (MaterialInfo stocktake : saveReq.getMaterialInfos()) {
            map.put(stocktake.getMaterialId(), Pair.of(stocktake.getChooseUnit(), stocktake.getTakeCount()));
        }
        return map;
    }



    @Override
    @LogRecord(type = RAW_MATERIAL, subType = RAW_MATERIAL_MODIFY_STATUS, bizNo = "{{#id}}", success = RAW_MATERIAL_MODIFY_STATUS_SUCCESS)
    public Boolean modifyStatus(Long id, Integer isEnable) {
        if (isEnable == 0){
            checkRawMaterialStatus(id);
        }

        LambdaUpdateWrapper<RawMaterial> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(RawMaterial::getIsEnable, isEnable);
        updateWrapper.eq(RawMaterial::getId, id);
        rawMaterialMapper.update(updateWrapper);
        return true;
    }

    @Override
    @LogRecord(type = RAW_MATERIAL, subType = RAW_MATERIAL_REMOVE, bizNo = "{{#id}}", success = RAW_MATERIAL_REMOVE_SUCCESS)
    public Boolean removeById(Long id) {
        checkRawMaterialStatus(id);
        rawMaterialMapper.deleteById(id);
        return true;
    }

    @Override
    public RawMaterial selectMaterial(Long storeId, String commodityCode) {
        return rawMaterialMapper.selectMaterial(storeId, commodityCode);
    }

    /**
     * 只有库存为0的时候可以设置停售和删除
     * 负数 和 有库存的时候 不可设为停售
     * @param id
     */
    private void checkRawMaterialStatus(Long id) {
        RawMaterial rawMaterial = rawMaterialMapper.selectById(id);
        if (rawMaterial == null){
            throw exception(ErrorCodeConstants.MATERIAL_STORE_NOT_FOUND);
        }

        if (rawMaterial.getStock().compareTo(BigDecimal.ZERO) != 0) {
            throw exception(ErrorCodeConstants.MATERIAL_NOT_ZERO);
        }
    }

}
