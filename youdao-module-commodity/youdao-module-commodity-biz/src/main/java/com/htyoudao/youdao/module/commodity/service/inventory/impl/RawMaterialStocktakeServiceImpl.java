package com.htyoudao.youdao.module.commodity.service.inventory.impl;


import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_GENERATE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_GENERATE_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_REMARK;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_REMARK_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_SAVE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.RAW_MATERIAL_STOCKTAKE_SAVE_SUCCESS;

import cn.hutool.core.lang.Pair;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakeSaveReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakeSaveReq.MaterialInfo;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialDetailVO;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialExportVO;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialTotalExcelVO;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.RawMaterialTotalRespVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStocktake;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.FlowAmountDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO.ChangeInfo;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialStocktakeMapper;
import com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.RawMaterialStocktakeService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 盘点service
 */

@Slf4j
@Service
public class RawMaterialStocktakeServiceImpl extends
    ServiceImpl<RawMaterialStocktakeMapper, RawMaterialStocktake> implements RawMaterialStocktakeService {

    @Resource
    private RawMaterialMapper rawMaterialMapper;

    @Resource
    private IRawMaterialService rawMaterialService;

    @Resource
    private RowMaterialStockFlowService flowService;

    @Resource
    private RawMaterialLossRecordService lossService;

    @Resource
    private RawMaterialStocktakeMapper stocktakeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @DubboReference
    private StoreApi storeApi;


    @Override
    @LogRecord(type = RAW_MATERIAL_STOCKTAKE, subType = RAW_MATERIAL_STOCKTAKE_SAVE, bizNo = "{{#saveReq.storeId}}", success = RAW_MATERIAL_STOCKTAKE_SAVE_SUCCESS)
    public Boolean preserve(MaterialStocktakeSaveReq saveReq) {
        try {
            // 使用storeId作为key的一部分，确保唯一性
            String key = "material:stocktake:" + saveReq.getStoreId();
            // 将对象转换为JSON字符串存储
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(saveReq));

            // 可以设置过期时间，比如24小时（根据需求调整）
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);

            return true;
        } catch (Exception e) {
            log.error("序列化对象失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @LogRecord(type = RAW_MATERIAL_STOCKTAKE, subType = RAW_MATERIAL_STOCKTAKE_GENERATE, bizNo = "{{#saveReq.storeId}}", success = RAW_MATERIAL_STOCKTAKE_GENERATE_SUCCESS)
    public Long generate(MaterialStocktakeSaveReq saveReq) {
        Long storeId = saveReq.getStoreId();

        //1.获取保存的列表
        MaterialStocktakeSaveReq prepareList = getPrepareList(storeId);
        if (prepareList == null) {
            throw new ServiceException(ErrorCodeConstants.INVENTORY_NOT_PREPARE);
        }

        //1.新增盘点记录
        RawMaterialStocktake stocktake = new RawMaterialStocktake();
        stocktake.setStoreId(storeId);
        stocktake.setRemark(saveReq.getRemark());

        //1.5 获取实际出库金额
        BigDecimal flowAmount = selectOutboundAmount(storeId);
        BigDecimal channelOutmount = selectChannelOutmount(storeId);

        stocktake.setOutAmount(flowAmount);
        stocktake.setChannelOutAmount(channelOutmount);

        if (saveReq.getInventoryDifferenceAmount().compareTo(BigDecimal.ZERO) < 0) {
            stocktake.setStocktakeStatus(2);
        } else {
            stocktake.setStocktakeStatus(1);
        }

        BigDecimal attritionRate = lossService.getAttritionRate(storeId);
        this.save(stocktake);

        //2.设置变动流水信息
        setFlowInfo(storeId, stocktake, prepareList);

        //更新所有未盘点信息
        log.info("StockTake:{},设置损耗率:{}", stocktake.getId(), attritionRate);
        //设置损耗率
        stocktake.setAttritionRate(attritionRate);
        lossService.updateUninventoriedLossRecord(stocktake.getId(), storeId, attritionRate);

        this.updateById(stocktake);

        //删除保存的缓存
        stringRedisTemplate.delete("material:stocktake:" + saveReq.getStoreId());

        return stocktake.getId();
    }

    /**
     * 设置变动流水信息
     *
     * @param storeId
     * @param stocktake
     * @param prepareList
     */
    private void setFlowInfo(Long storeId, RawMaterialStocktake stocktake,
        MaterialStocktakeSaveReq prepareList) {
        //2.获取实时库存列表
        List<RawMaterial> rawMaterials = rawMaterialMapper.selectList(RawMaterial::getStoreId, storeId);
        Map<Long, BigDecimal> collect = rawMaterials.stream()
            .collect(Collectors.toMap(RawMaterial::getId, RawMaterial::getStock));

        StockChangeDTO stockChangeDTO = new StockChangeDTO();
        stockChangeDTO.setStoreId(storeId);
        stockChangeDTO.setType(StockChangeEnum.INVENTORY);
        stockChangeDTO.setReferenceId(stocktake.getId().toString());
        List<ChangeInfo> changeInfos = new ArrayList<>();
        for (MaterialInfo materialInfo : prepareList.getMaterialInfos()) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setRawMaterialId(materialInfo.getMaterialId());
            BigDecimal takeCount = materialInfo.getTakeCount();
            BigDecimal oldCount = collect.get(materialInfo.getMaterialId());
            if (oldCount == null) {
                log.error("保存的数据未查询到原有库存:" + materialInfo.getMaterialId());
                continue;
            }
            changeInfo.setQuantity(takeCount);
            changeInfo.setChooseUnit(materialInfo.getChooseUnit());
            changeInfos.add(changeInfo);
        }
        stockChangeDTO.setChangeList(changeInfos);
        flowService.setStock(stockChangeDTO);
    }

    /**
     * 获取实际出库金额
     *
     * @param storeId
     * @return
     */
    @Override
    public BigDecimal selectOutboundAmount(Long storeId) {
        FlowAmountDTO flowAmountDTO = new FlowAmountDTO();
        flowAmountDTO.setChangeTypes(List.of(
            StockChangeEnum.SALE.getType(),//销售
            StockChangeEnum.TRANSFER_OUT.getType(),//调拨出
            StockChangeEnum.LOSS.getType()//报损
        ));
        flowAmountDTO.setStoreId(storeId);
        Date lastTakeTime = stocktakeMapper.selectLastTakeTime(storeId);
        if (lastTakeTime != null) {
            flowAmountDTO.setStartTime(lastTakeTime);
            flowAmountDTO.setEndTime(new Date());
        }
        return flowService.getFlowAmount(flowAmountDTO).abs();
    }

    @Override
    public BigDecimal selectChannelOutmount(Long storeId) {
        FlowAmountDTO flowAmountDTO = new FlowAmountDTO();
        flowAmountDTO.setChangeTypes(List.of(
            StockChangeEnum.SAN_KUAI_OUT.getType(),//美团出
            StockChangeEnum.ELE_ME_OUT.getType(),//饿了么出
            StockChangeEnum.SCHOOL_OUT.getType()//学校小程序出
        ));
        flowAmountDTO.setStoreId(storeId);
        Date lastTakeTime = stocktakeMapper.selectLastTakeTime(storeId);
        if (lastTakeTime != null) {
            flowAmountDTO.setStartTime(lastTakeTime);
            flowAmountDTO.setEndTime(new Date());
        }
        return flowService.getFlowAmount(flowAmountDTO).abs();
    }


    @Override
    public MaterialStocktakeSaveReq getPrepareList(Long storeId) {
        try {
            String key = "material:stocktake:" + storeId;
            String value = stringRedisTemplate.opsForValue().get(key);

            if (value != null) {
                // 将JSON字符串转换回对象
                return JSON.parseObject(value, MaterialStocktakeSaveReq.class);
            }
            return null;
        } catch (Exception e) {
            log.error("反序列化对象失败", e);
            return null;
        }
    }

    @Override
    @LogRecord(type = RAW_MATERIAL_STOCKTAKE, subType = RAW_MATERIAL_STOCKTAKE_REMARK, bizNo = "{{#id}}", success = RAW_MATERIAL_STOCKTAKE_REMARK_SUCCESS)
    public Boolean setRemark(Long id, String remark) {
        LambdaUpdateWrapper<RawMaterialStocktake> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(RawMaterialStocktake::getRemark, remark);
        updateWrapper.eq(RawMaterialStocktake::getId, id);
        return this.update(updateWrapper);
    }

    @Override
    public PageResult<MaterialStocktakePageVO> stocktakeList(MaterialStocktakePageReq pageReq) {
        LambdaQueryWrapperX<RawMaterialStocktake> queryWrapper = new LambdaQueryWrapperX();
        queryWrapper.eqIfPresent(RawMaterialStocktake::getStoreId, pageReq.getStoreId());
        queryWrapper.eqIfPresent(RawMaterialStocktake::getStocktakeStatus, pageReq.getStocktakeStatus());
        queryWrapper.betweenIfPresent(RawMaterialStocktake::getCreateTime, pageReq.getCreateTimeStart(),
            pageReq.getCreateTimeEnd());

        if (pageReq.getOrgId() != null) {
            List<Long> checkedData = storeApi.getStoreIdsByAllOrgId(pageReq.getOrgId()).getCheckedData();
            queryWrapper.inIfPresent(RawMaterialStocktake::getStoreId, checkedData);
        }

        if (pageReq.getWarehouseId() != null) {
            List<Long> checkedData = storeApi.getStoreIdsByWarehouseId(pageReq.getWarehouseId()).getCheckedData();
            queryWrapper.inIfPresent(RawMaterialStocktake::getStoreId, checkedData);
        }

        queryWrapper.orderByDesc(RawMaterialStocktake::getCreateTime);

        PageParam page = new PageParam();
        page.setPageNo(pageReq.getPageNo());
        page.setPageSize(pageReq.getPageSize());

        PageResult<RawMaterialStocktake> pageResult = stocktakeMapper.selectPage(page, queryWrapper);
        List<MaterialStocktakePageVO> pageVOS = BeanCopyUtils.copyBeanList(pageResult.getList(),
            MaterialStocktakePageVO.class);

        //设置门店名称
        List<Long> storeIds = pageVOS.stream().map(MaterialStocktakePageVO::getStoreId).distinct().toList();
        if (!CollectionUtils.isEmpty(storeIds)) {
            Map<Long, String> collect = storeApi.getStoresByStoreIds(storeIds).getCheckedData()
                .stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
            pageVOS.forEach(v -> v.setStoreName(collect.get(v.getStoreId())));
        }

        PageResult<MaterialStocktakePageVO> result = new PageResult();
        result.setList(pageVOS);
        result.setTotal(pageResult.getTotal());
        return result;
    }

    @Override
    public RawMaterialRespVO stocktakeDetail(Long id) {

        RawMaterialStocktake stocktake = this.getById(id);

        RawMaterialRespVO respVO = new RawMaterialRespVO();
        List<RawMaterialStockFlow> flows = flowService.flowByReferenceInfo(id.toString(), StockChangeEnum.INVENTORY);
        if (CollectionUtils.isEmpty(flows)) {
            return respVO;
        }

        List<RawMaterialDetailVO> detailVOS = new ArrayList<>();
        for (RawMaterialStockFlow flow : flows) {
            RawMaterialDetailVO detailVO = new RawMaterialDetailVO();

            String materialSnapshotStr = flow.getMaterialSnapshot();

            if (StringUtils.isNotBlank(materialSnapshotStr)) {
                RawMaterial materialSnapshot = JSON.parseObject(materialSnapshotStr, RawMaterial.class);
                detailVO = BeanCopyUtils.copyBean(materialSnapshot, RawMaterialDetailVO.class);
            }

            detailVO.setId(flow.getRawMaterialId());
            detailVO.setMinUnit(flow.getMinUnit());
            detailVO.setUnitPrice(flow.getUnitPrice());

            //库存数量 为修改前数量 ，盘点数量为修改后数量
            detailVO.setStock(flow.getStockBefore());
            detailVO.setTakeCount(flow.getStockAfter());
            detailVO.setChooseUnit(flow.getChooseUnit());

            BigDecimal rate = detailVO.caleRateByChooseUnit(flow.getChooseUnit());

            // 根据盘点ID 获取 损耗信息
            List<Long> lossIds = lossService.lossIdsByStoreTake(stocktake.getStoreId(), stocktake.getId());
            Map<Long, Pair<BigDecimal, BigDecimal>> lossMap = lossService.getLossMap(lossIds);
            Pair<BigDecimal, BigDecimal> emptyPair = Pair.of(BigDecimal.ZERO, BigDecimal.ZERO);
            Pair<BigDecimal, BigDecimal> lossPair = lossMap.getOrDefault(flow.getRawMaterialId(), emptyPair);
            detailVO.setLossCount(lossPair.getKey().multiply(rate));
            detailVO.setLossAmount(lossPair.getValue());

            //根据换算比例，计算展示金额
            detailVO.caleAmountByRate(rate);

            detailVOS.add(detailVO);
        }

        //total
        RawMaterialTotalRespVO totalRespVO = rawMaterialService.caleTotalVo(detailVOS);
        totalRespVO.setOutAmount(stocktake.getOutAmount());
        totalRespVO.setChannelOutAmount(stocktake.getChannelOutAmount());
        totalRespVO.setAllOutAmount(stocktake.getChannelOutAmount().add(stocktake.getOutAmount()));
        totalRespVO.setAttritionRate(stocktake.getAttritionRate());

        respVO.setTotal(totalRespVO);
        respVO.setDetails(detailVOS);

        return respVO;
    }


    @Override
    public void export(RawMaterialRespVO data, OutputStream outputStream) {

        // 创建合计行数据列表
        List<RawMaterialTotalExcelVO> totalList = new ArrayList<>();
        RawMaterialTotalExcelVO totalExcelVO = new RawMaterialTotalExcelVO(data.getTotal());
        RawMaterialTotalExcelVO attritionRateVo = new RawMaterialTotalExcelVO();
        attritionRateVo.setTotalLossCountWithLabel("损耗率：" + toPercentage(data.getTotal().getAttritionRate()));
        totalList.add(totalExcelVO);
        totalList.add(attritionRateVo);

        // 创建明细数据列表
        List<RawMaterialExportVO> details = data.getDetails().stream()
            .map(RawMaterialExportVO::new)
            .toList();

        // 为明细数据添加序号
        for (int i = 0; i < details.size(); i++) {
            details.get(i).setIndex(i + 1);
        }

        // 创建ExcelWriter
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .build()) {

            // 写入明细数据
            WriteSheet detailSheet = EasyExcel.writerSheet(0, "原材料盘点明细")
                .head(RawMaterialExportVO.class)
                .build();

            excelWriter.write(details, detailSheet);

            // 写入合计行
            WriteSheet totalSheet = EasyExcel.writerSheet(0)
                .head(RawMaterialTotalExcelVO.class)
                .relativeHeadRowIndex(details.size() + 1) // 在明细数据下方一行写入
                .build();

            excelWriter.write(totalList, totalSheet);
        }
    }


    /**
     * 将BigDecimal转换为百分比字符串
     *
     * @param decimal 输入的BigDecimal值
     * @return 百分比格式的字符串，如 "0.22%"
     */
    public static String toPercentage(BigDecimal decimal) {
        if (decimal == null) {
            return "0%";
        }

        // 乘以100转换为百分比
        BigDecimal percentage = decimal.multiply(new BigDecimal("100"));

        // 创建格式化器，保留2位小数
        DecimalFormat df = new DecimalFormat("0.##");

        return df.format(percentage) + "%";
    }


}