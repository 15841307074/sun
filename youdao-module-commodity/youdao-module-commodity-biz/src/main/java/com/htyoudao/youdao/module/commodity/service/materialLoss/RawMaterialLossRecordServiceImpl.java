package com.htyoudao.youdao.module.commodity.service.materialLoss;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.api.inventory.InventoryApi;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLog.VO.SplitCommodityMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.RawUnitStockVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialDataVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityInfoReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityInfoRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.ScmCommodityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.TriRecipeReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.CommodityConversion;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.api.VO.MaterialDataVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy.ScmCommodity;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.FlowAmountDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.dal.mysql.*;
import com.htyoudao.youdao.module.commodity.dal.mysql.ScmCommodity.ScmCommodityMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.*;
import com.htyoudao.youdao.module.commodity.dal.mysql.scmUnitConversion.ScmUnitConversionMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriCommodityRecipeMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriFormulationMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriRecipeMapper;
import com.htyoudao.youdao.module.commodity.enums.*;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.ImportTaskService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import com.htyoudao.youdao.module.commodity.service.scmCommodity.ScmCommodityService;
import com.htyoudao.youdao.module.commodity.service.scmUnitConversion.ScmUnitConversionService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.service.triInventory.ITriRecipeService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ChannelType.SCHOOL_PROGRAM;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.MATERIAL_RECIPE_NOT_FOUND;

@Slf4j
@Service
public class RawMaterialLossRecordServiceImpl extends ServiceImpl<RawMaterialLossRecordMapper, RawMaterialLossRecordDO> implements RawMaterialLossRecordService {

    @Autowired
    private RawMaterialLossRecordMapper materialLossRecordMapper;

    @Autowired
    private RawMaterialStockFlowMapper rawMaterialStockFlowMapper;

    @Autowired
    private RawMaterialMapper rawMaterialMapper;

    @Autowired
    private IRawMaterialService rawMaterialService;

    @Resource
    private RowMaterialStockFlowService flowService;

    @Autowired
    private RowMaterialStockFlowService materialStockFlowService;

    @Autowired
    private CommoditySkusMapper commoditySkusMapper;

    @Resource
    private RawMaterialStocktakeMapper stocktakeMapper;


    @Autowired
    private RowMaterialStockFlowService rowMaterialStockFlowService;

    @Resource
    private CommodityRecipeMapper commodityRecipeMapper;


    @Resource
    private RawMaterialLogService rawMaterialLogService;

    @Autowired
    private ImportTaskMapper importTaskMapper;


    @Resource
    private ScmUnitConversionMapper scmUnitConversionMapper;


    @Resource
    private ScmCommodityMapper scmCommodityMapper;

    @Resource
    private ScmCommodityService scmCommodityService;

    @Autowired
    private CommoditySpusMapper commoditySpusMapper;

    @Autowired
    private CommodityStoreSkuMapper commodityStoreSkuMapper;

    @Autowired
    private ImportTaskService importTaskService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ITriRecipeService triRecipeService;







    @Resource
    private CommodityRecipeMaterialMapper commodityRecipeMaterialMapper;


    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private InventoryApi inventoryApi;

    @Resource
    private TriRecipeMapper triRecipeMapper;

    @Resource
    private TriFormulationMapper triFormulationMapper;

    @Resource
    private TriCommodityRecipeMaterialMapper triCommodityRecipeMaterialMapper;


    @Resource
    private ImportLogRecordMapper importLogRecordMapper;



    @Override
    public PageResult<MaterialLossRecordPageVO> lossRecordPageList(MaterialLossRecordPageReq pageReq) {
        // 1. 查询基础数据
        List<RawMaterialLossRecordDO> materialLossRecordDOS = getBaseLossRecords(pageReq);


        // 2. 计算损耗率
        calculateAttritionRates(materialLossRecordDOS);

        // 3. 构建分页查询条件
        LambdaQueryWrapperX<RawMaterialLossRecordDO> lambdaQueryWrapper = buildQueryWrapper(pageReq);
        List<RawMaterialLossRecordDO> allRecords = materialLossRecordMapper.selectList(lambdaQueryWrapper);

        if (ObjectUtil.isEmpty(allRecords)) {
            return createEmptyResult();
        }

        // 4. 更新损耗率到查询结果
        updateAttritionRates(allRecords, materialLossRecordDOS);

        // 5. 分页处理
        List<RawMaterialLossRecordDO> pagedRecords = getPagedRecords(allRecords, pageReq);

        // 6. 转换为VO并填充额外信息
        List<MaterialLossRecordPageVO> resultList = convertToVOList(pagedRecords);

        return new PageResult<>(resultList,Long.valueOf(allRecords.size()));
    }

// 辅助方法分解

    /**
     * 获取基础损耗记录
     */
    private List<RawMaterialLossRecordDO> getBaseLossRecords(MaterialLossRecordPageReq pageReq) {
        LambdaQueryWrapperX<RawMaterialLossRecordDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eqIfPresent(RawMaterialLossRecordDO::getStoreId, pageReq.getStoreId());
        queryWrapper.isNull(RawMaterialLossRecordDO::getTakeId);
        return materialLossRecordMapper.selectList(queryWrapper);
    }

    /**
     * 计算损耗率
     */
    private void calculateAttritionRates(List<RawMaterialLossRecordDO> records) {
        Map<Long, List<RawMaterialLossRecordDO>> recordsByStore = records.stream()
                .filter(record -> record.getTakeId() == null)
                .collect(Collectors.groupingBy(RawMaterialLossRecordDO::getStoreId));

        recordsByStore.forEach(this::calculateStoreAttritionRate);
    }

    /**
     * 计算单个门店的损耗率
     */
    private void calculateStoreAttritionRate(Long storeId, List<RawMaterialLossRecordDO> records) {
        BigDecimal totalPrices = calculateTotalPrices(records);
        RawMaterialLossRecordDO latestRecord = getLatestRecord(records);

        // 获取流水金额
        BigDecimal flowAmount = getFlowAmount(storeId);

        if (flowAmount.compareTo(BigDecimal.ZERO) == 0) {
            setAttritionRateForNoFlow(records, latestRecord);
        } else {
            Boolean flag = false;
            BigDecimal attritionRate = calculateAttritionRate(totalPrices, flowAmount);
            if (attritionRate.compareTo(new BigDecimal("0.25")) > 0) {
                flag = true;
            }
            setAttritionRateWithFlow(records, latestRecord, attritionRate,flag);
        }
    }

    /**
     * 计算总金额
     */
    private BigDecimal calculateTotalPrices(List<RawMaterialLossRecordDO> records) {
        return records.stream()
                .map(RawMaterialLossRecordDO::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取最新记录
     */
    private RawMaterialLossRecordDO getLatestRecord(List<RawMaterialLossRecordDO> records) {
        return records.stream()
                .max(Comparator.comparing(RawMaterialLossRecordDO::getCreateTime))
                .orElseThrow(() -> new RuntimeException("No records found"));
    }

    /**
     * 获取流水金额
     */
    private BigDecimal getFlowAmount(Long storeId) {
        Date lastTakeTime = stocktakeMapper.selectLastTakeTime(storeId);

        if (lastTakeTime == null) {
            LocalDateTime localDateTime = LocalDateTime.of(2025, 9, 3, 0, 0, 0);
            lastTakeTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        InventoryAggregationRequest aggregationPageRequest = createAggregationRequest(storeId, lastTakeTime);
        CommonResult<BigDecimal> result = inventoryApi.storePage(aggregationPageRequest);

        return result.getData() != null ? result.getData() : BigDecimal.ZERO;
    }

    /**
     * 创建聚合请求
     */
    private InventoryAggregationRequest createAggregationRequest(Long storeId, Date lastTakeTime) {
        InventoryAggregationRequest request = new InventoryAggregationRequest();
        request.setStoreId(storeId);
        LocalDateTime localDateTime = lastTakeTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        request.setCurrentTimeStart(localDateTime);
        request.setCurrentTimeEnd(now);

        return request;
    }

    /**
     * 计算损耗率
     */
    private BigDecimal calculateAttritionRate(BigDecimal totalPrices, BigDecimal flowAmount) {
        BigDecimal positiveAmount = flowAmount.abs();
        if (positiveAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal quotient = totalPrices.divide(positiveAmount, 4, RoundingMode.HALF_UP);
        return quotient.multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 设置无流水时的损耗率
     */
    private void setAttritionRateForNoFlow(List<RawMaterialLossRecordDO> records, RawMaterialLossRecordDO latestRecord) {
        latestRecord.setAttritionRate("-");
        latestRecord.setIsFlag(true);
        String formattedDate = formatDate(latestRecord.getCreateTime());

        records.forEach(record -> {
            if (!record.getId().equals(latestRecord.getId())) {
                record.setAttritionRate("统计结果已合计在" + formattedDate + "损耗单内");
                record.setIsFlag(true);
            }
        });
    }

    /**
     * 设置有流水时的损耗率
     */
    private void setAttritionRateWithFlow(List<RawMaterialLossRecordDO> records,
                                          RawMaterialLossRecordDO latestRecord,
                                          BigDecimal attritionRate,Boolean flag) {
        latestRecord.setAttritionRate(attritionRate.toString() + "%");
        latestRecord.setIsFlag(flag);
        String formattedDate = formatDate(latestRecord.getCreateTime());

        records.forEach(record -> {
            if (!record.getId().equals(latestRecord.getId())) {
                record.setAttritionRate("统计结果已合计在" + formattedDate + "损耗单内");
                record.setIsFlag(flag);
            }
        });
    }

    /**
     * 格式化日期
     */
    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    /**
     * 构建查询包装器
     */
    private LambdaQueryWrapperX<RawMaterialLossRecordDO> buildQueryWrapper(MaterialLossRecordPageReq pageReq) {
        LambdaQueryWrapperX<RawMaterialLossRecordDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eqIfPresent(RawMaterialLossRecordDO::getStoreId, pageReq.getStoreId());
        queryWrapper.eqIfPresent(RawMaterialLossRecordDO::getLossType, pageReq.getLossType());

        if (ObjectUtil.isNotEmpty(pageReq.getStartDate())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(pageReq.getStartDate(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(pageReq.getEndDate(), formatter);
            queryWrapper.geIfPresent(RawMaterialLossRecordDO::getCreateTime, startDate);
            queryWrapper.leIfPresent(RawMaterialLossRecordDO::getCreateTime, endDate);
        }

        queryWrapper.orderByDesc(RawMaterialLossRecordDO::getCreateTime);
        return queryWrapper;
    }

    /**
     * 更新损耗率到查询结果
     */
    private void updateAttritionRates(List<RawMaterialLossRecordDO> targetRecords,
                                      List<RawMaterialLossRecordDO> sourceRecords) {
        Map<Long, RawMaterialLossRecordDO> sourceMap = sourceRecords.stream()
                .filter(record -> record.getTakeId() == null)
                .collect(Collectors.toMap(RawMaterialLossRecordDO::getId, Function.identity()));

        targetRecords.forEach(record -> {
            RawMaterialLossRecordDO sourceRecord = sourceMap.get(record.getId());
            if (sourceRecord != null) {
                record.setAttritionRate(sourceRecord.getAttritionRate());
                record.setIsFlag(sourceRecord.getIsFlag());
            }
        });
    }

    /**
     * 获取分页记录
     */
    private List<RawMaterialLossRecordDO> getPagedRecords(List<RawMaterialLossRecordDO> allRecords,
                                                          MaterialLossRecordPageReq pageReq) {
        // 步骤1：按takeId分组，获取每个分组的最新记录
        Map<Long, RawMaterialLossRecordDO> latestRecords = allRecords.stream()
                .filter(record -> record.getTakeId() != null)
                .collect(Collectors.toMap(
                        RawMaterialLossRecordDO::getTakeId,
                        record -> record,
                        (existing, replacement) ->
                                existing.getCreateTime().isAfter(replacement.getCreateTime()) ? existing : replacement
                ));
        Set<Long> longSet = new HashSet<>();
        // 步骤2：找出需要标记的takeId
        List<Long> flaggedTakeIds = latestRecords.values().stream()
                .filter(record -> {
                    try {
                        String cleanRate = record.getAttritionRate().replace("%", "").trim();
                        BigDecimal rate = new BigDecimal(cleanRate);
                        boolean b = rate.compareTo(new BigDecimal("0.25")) > 0;
                        return b;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .map(RawMaterialLossRecordDO::getTakeId)
                .collect(Collectors.toList());
        longSet.addAll(flaggedTakeIds);

        List<Long> flagT = latestRecords.values().stream()
                .filter(record -> {
                    try {
                        boolean equals = record.getAttritionRate().equals("-");
                        return equals;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .map(RawMaterialLossRecordDO::getTakeId)
                .collect(Collectors.toList());
        longSet.addAll(flagT);

        // 步骤3：更新所有相关记录的isFlag
        allRecords.forEach(record -> {
            if (record.getTakeId() != null && longSet.contains(record.getTakeId())) {
                record.setIsFlag(true);
            }
        });


        int skip = (pageReq.getPageNo() - 1) * pageReq.getPageSize();
        return allRecords.stream()
                .skip(skip)
                .limit(pageReq.getPageSize())
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO列表
     */
    private List<MaterialLossRecordPageVO> convertToVOList(List<RawMaterialLossRecordDO> records) {
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 单个记录转换为VO
     */
    private MaterialLossRecordPageVO convertToVO(RawMaterialLossRecordDO record) {
        MaterialLossRecordPageVO vo = new MaterialLossRecordPageVO();
        BeanUtils.copyProperties(record, vo);

        // 设置门店名称
        setStoreName(vo, record.getStoreId());

        // 设置物料列表
        vo.setMaterialList(getMaterialList(record.getId()));

        return vo;
    }

    /**
     * 设置门店名称
     */
    private void setStoreName(MaterialLossRecordPageVO vo, Long storeId) {
        CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(storeId);
        if (storeResult != null && storeResult.getData() != null) {
            vo.setStoreName(storeResult.getData().getStoreName());
        }
    }

    /**
     * 获取物料列表
     */
    private List<MaterialDataRespVo> getMaterialList(Long recordId) {
        LambdaQueryWrapperX<RawMaterialStockFlow> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(RawMaterialStockFlow::getChangeType, StockChangeEnum.LOSS.getType());
        queryWrapper.eq(RawMaterialStockFlow::getReferenceId, recordId);

        List<RawMaterialStockFlow> flows = rawMaterialStockFlowMapper.selectList(queryWrapper);

        if (ObjectUtil.isEmpty(flows)) {
            return new ArrayList<>();
        }

        return flows.stream()
                .map(this::convertToMaterialData)
                .collect(Collectors.toList());
    }

    /**
     * 转换为物料数据
     */
    private MaterialDataRespVo convertToMaterialData(RawMaterialStockFlow flow) {
        MaterialDataRespVo materialData = new MaterialDataRespVo();
        materialData.setRawMaterialId(flow.getRawMaterialId());
        materialData.setUnit(flow.getChooseUnit());

        RawMaterial rawMaterial = rawMaterialMapper.selectById(flow.getRawMaterialId());
        if (rawMaterial != null) {
            materialData.setCommodityName(flow.getCommodityName());
            calculateMaterialDetails(materialData, flow, rawMaterial);
        } else {
            materialData.setCommodityName(flow.getCommodityName());
            calculateBasicMaterialDetails(materialData, flow);
        }

        return materialData;
    }

    /**
     * 计算物料详情（有规格信息）
     */
    private void calculateMaterialDetails(MaterialDataRespVo materialData,
                                          RawMaterialStockFlow flow,
                                          RawMaterial rawMaterial) {
        BigDecimal changeQuantity = flow.getChangeQuantity().abs();

        if (!flow.getChooseUnit().equals(flow.getMinUnit()) &&
                ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())) {
            try {
                BigDecimal conversionRate = getConversionRate(rawMaterial.getSpecificationsRules(),
                        flow.getChooseUnit());

                if (conversionRate != null) {
                    BigDecimal convertedQuantity = changeQuantity.divide(conversionRate, 4, RoundingMode.HALF_UP);
                    materialData.setQuantity(convertedQuantity);

                    BigDecimal unitPrice = flow.getUnitPrice().multiply(conversionRate)
                            .setScale(4, RoundingMode.HALF_UP);
                    materialData.setPrice(unitPrice);

                    BigDecimal totalPrice = convertedQuantity.multiply(unitPrice)
                            .setScale(4, RoundingMode.HALF_UP);
                    materialData.setTotalPrice(totalPrice);
                    return;
                }
            } catch (Exception e) {
                log.error("解析规格规则失败", e);
            }
        }

        // 默认计算方式
        calculateBasicMaterialDetails(materialData, flow, rawMaterial);
    }

    /**
     * 获取转换率
     */
    private BigDecimal getConversionRate(String specificationsRules, String chooseUnit) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        List<CommodityConversion> conversionList = objectMapper.readValue(
                specificationsRules,
                new TypeReference<List<CommodityConversion>>() {}
        );

        if (ObjectUtil.isNotEmpty(conversionList)) {
            for (CommodityConversion conversion : conversionList) {
                if (chooseUnit.equals(conversion.getBeforeUnit())) {
                    return conversion.getAfterNumber();
                }
            }
        }

        return null;
    }

    /**
     * 基础物料详情计算
     */
    private void calculateBasicMaterialDetails(MaterialDataRespVo materialData,
                                               RawMaterialStockFlow flow,
                                               RawMaterial rawMaterial) {
        BigDecimal changeQuantity = flow.getChangeQuantity().abs();
        materialData.setQuantity(changeQuantity);
        materialData.setPrice(rawMaterial.getUnitPrice());

        BigDecimal totalPrice = changeQuantity.multiply(rawMaterial.getUnitPrice())
                .setScale(4, RoundingMode.HALF_UP);
        materialData.setTotalPrice(totalPrice);
    }

    /**
     * 基础物料详情计算（无原材料信息）
     */
    private void calculateBasicMaterialDetails(MaterialDataRespVo materialData,
                                               RawMaterialStockFlow flow) {
        BigDecimal changeQuantity = flow.getChangeQuantity().abs();
        materialData.setQuantity(changeQuantity);
        materialData.setPrice(flow.getUnitPrice());

        BigDecimal totalPrice = changeQuantity.multiply(flow.getUnitPrice())
                .setScale(4, RoundingMode.HALF_UP);
        materialData.setTotalPrice(totalPrice);
    }

    /**
     * 创建空结果
     */
    private PageResult<MaterialLossRecordPageVO> createEmptyResult() {
        return new PageResult<>(new ArrayList<>(),0L);
    }

    @Override
    public Boolean deleteLossRecord(MaterialLossDeleteReq lossDeleteReq) {
        Long id = lossDeleteReq.getId();
        if(ObjectUtil.isNotEmpty(id)){
            int i = materialLossRecordMapper.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean saveLossRecord(MaterialLossSaveReq saveReq) {
        if(ObjectUtil.isNotEmpty(saveReq)){
            RawMaterialLossRecordDO recordDO = new RawMaterialLossRecordDO();
            BeanUtils.copyProperties(saveReq,recordDO);

            List<StockChangeDTO.ChangeInfo> changeInfoList = new ArrayList<>();
            StockChangeDTO stockChangeDTO = new StockChangeDTO();
            stockChangeDTO.setStoreId(saveReq.getStoreId());

            stockChangeDTO.setType(StockChangeEnum.LOSS);

            BigDecimal totalAmount = BigDecimal.ZERO;

            List<MaterialDataReqVo> materialList = saveReq.getMaterialList();
            if(ObjectUtil.isNotEmpty(materialList)){
                for (MaterialDataReqVo materialDataReqVo : materialList) {
                    if(ObjectUtil.isNotEmpty(materialDataReqVo.getRawMaterialId())){

                        StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                        //选择的单位
                        String unit = materialDataReqVo.getUnit();
                        Long rawMaterialId = materialDataReqVo.getRawMaterialId();
                        LambdaQueryWrapperX<RawMaterial> wrapperX = new LambdaQueryWrapperX<>();
                        wrapperX.eq(RawMaterial::getId,rawMaterialId);
                        RawMaterial rawMaterial = rawMaterialMapper.selectOne(wrapperX);
                        if(rawMaterial!=null){
                            //最小单位
                            if(rawMaterial.getMinUnit().equals(unit)){
                                BigDecimal quantity = materialDataReqVo.getQuantity();
                                BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                ss.setQuantity(negativeBigDecimal);
                                ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                ss.setChooseUnit(materialDataReqVo.getUnit());
                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }else{
                                //选择的不是最小单位，需要转化
                                BigDecimal aa = BigDecimal.ZERO;

                                try {
                                    String specificationsRules = rawMaterial.getSpecificationsRules();
                                    if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                                        ObjectMapper objectMapper = new ObjectMapper();

                                        List<CommodityConversion> conversionList = objectMapper.readValue(
                                                specificationsRules,
                                                new TypeReference<List<CommodityConversion>>() {}
                                        );
                                        for (CommodityConversion commodityConversion : conversionList) {
                                            if(commodityConversion.getBeforeUnit().equals(unit)){
                                                aa = commodityConversion.getAfterNumber();
                                            }
                                        }
                                        BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                        BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                        ss.setQuantity(negativeBigDecimal);
                                        ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                    }else{
                                        ss.setQuantity(materialDataReqVo.getQuantity());
                                        ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ss.setChooseUnit(materialDataReqVo.getUnit());

                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }
                        }
                    }else{
                        StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                        LocalDateTime now = LocalDateTime.now();
                        ScmCommodityInfoReqVO scmCommodityInfoReqVO = new ScmCommodityInfoReqVO();
                        scmCommodityInfoReqVO.setCommodityCode(materialDataReqVo.getMaterialsId());
                        scmCommodityInfoReqVO.setWarehouseId(saveReq.getWarehouseId());
                        scmCommodityInfoReqVO.setStasticsName(materialDataReqVo.getStasticsName());
                        scmCommodityInfoReqVO.setStoreId(saveReq.getStoreId());
                        ScmCommodityInfoRespVO scmCommodityRespVO = scmCommodityService.selectCommodityInfo(scmCommodityInfoReqVO, DeviceType.APP.getValue());
                        if(scmCommodityRespVO!=null){
                            List<SaveMaterialReqVO> saveMaterialReqVOs = getSaveMaterialReqVOS(scmCommodityRespVO, now ,saveReq);
                            rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
                            RawMaterial selectMaterial = rawMaterialMapper.selectMaterial(saveReq.getStoreId(), materialDataReqVo.getMaterialsId());
                            if(selectMaterial!=null){
                                ss.setRawMaterialId(selectMaterial.getId());
                                String unit = materialDataReqVo.getUnit();
                                //最小单位
                                if(selectMaterial.getMinUnit().equals(unit)){
                                    BigDecimal quantity = materialDataReqVo.getQuantity();
                                    BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                    ss.setQuantity(negativeBigDecimal);
                                    ss.setChooseUnit(materialDataReqVo.getUnit());
                                    BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                    BigDecimal price = getSafeBigDecimal(materialDataReqVo.getPrice());
                                    totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                    changeInfoList.add(ss);
                                }else{
                                    //选择的不是最小单位，需要转化
                                    BigDecimal aa = BigDecimal.ZERO;

                                    try {
                                        String specificationsRules = selectMaterial.getSpecificationsRules();
                                        if (ObjectUtil.isNotEmpty(selectMaterial.getSpecificationsRules())&& !selectMaterial.getSpecificationsRules().equals("[]")) {
                                            ObjectMapper objectMapper = new ObjectMapper();

                                            List<CommodityConversion> conversionList = objectMapper.readValue(
                                                    specificationsRules,
                                                    new TypeReference<List<CommodityConversion>>() {}
                                            );
                                            for (CommodityConversion commodityConversion : conversionList) {
                                                if(commodityConversion.getBeforeUnit().equals(unit)){
                                                    aa = commodityConversion.getAfterNumber();
                                                }
                                            }
                                            BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                            BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                            ss.setQuantity(negativeBigDecimal);
                                        }else{
                                            ss.setQuantity(materialDataReqVo.getQuantity());
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ss.setChooseUnit(materialDataReqVo.getUnit());

                                    BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                    BigDecimal price = getSafeBigDecimal(materialDataReqVo.getPrice());
                                    totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                    changeInfoList.add(ss);
                                }
                            }
                        }
                    }
                }
            }else {
                throw exception(11000011,"损耗记录不能为空");
            }
            recordDO.setTotalAmount(totalAmount);
            materialLossRecordMapper.insert(recordDO);
            if(ObjectUtil.isNotEmpty(recordDO.getId())){
                stockChangeDTO.setReferenceId(recordDO.getId().toString());
            }
            stockChangeDTO.setChangeList(changeInfoList);
            Boolean b = rowMaterialStockFlowService.changeStock(stockChangeDTO);
            return true;
        }
        return false;
    }


    private List<SaveMaterialReqVO> getSaveMaterialReqVOS(ScmCommodityInfoRespVO scmCommodityRespVO, LocalDateTime now, MaterialLossSaveReq saveReq) {
        List<SaveMaterialReqVO> saveMaterialReqVO = new ArrayList<>();
        SaveMaterialReqVO materialReqVO = new SaveMaterialReqVO();
        materialReqVO.setBrandName(scmCommodityRespVO.getBrandName());
        materialReqVO.setCommodityCode(scmCommodityRespVO.getCommodityCode());
        materialReqVO.setCommodityName(scmCommodityRespVO.getCommodityName());
        materialReqVO.setInTime(now);
        materialReqVO.setStoreId(saveReq.getStoreId());
        materialReqVO.setBrandId(scmCommodityRespVO.getBrandId());
        materialReqVO.setCategoryId(scmCommodityRespVO.getCategoryId());
        materialReqVO.setCategoryName(scmCommodityRespVO.getCategoryName());
        materialReqVO.setCommodityId(scmCommodityRespVO.getId());
        materialReqVO.setCommodityType(scmCommodityRespVO.getCommodityType());
        materialReqVO.setMinUnit(scmCommodityRespVO.getMinUnit());
        materialReqVO.setOutUnit(scmCommodityRespVO.getOutUnit());
        materialReqVO.setOutPrice(scmCommodityRespVO.getOutPrice());
        if(scmCommodityRespVO.getMinUnit().equals(scmCommodityRespVO.getOutUnit())){
            materialReqVO.setUnitPrice(scmCommodityRespVO.getOutPrice());
        }else{
            BigDecimal multiple = new BigDecimal("1");
            List<ScmUnitConversion> scmUnitConversionList = scmCommodityRespVO.getScmUnitConversionList();
            for (ScmUnitConversion scmUnitConversion : scmUnitConversionList) {
                if(scmUnitConversion.getBeforeUnit().equals(scmCommodityRespVO.getOutUnit())){
                    BigDecimal bigDecimal = new BigDecimal(scmUnitConversion.getAfterNumber());
                    multiple = bigDecimal;
                }

            }
            BigDecimal result = scmCommodityRespVO.getOutPrice().divide(multiple, 4, RoundingMode.HALF_UP);
            materialReqVO.setUnitPrice(result);

        }

        materialReqVO.setSpecifications(scmCommodityRespVO.getSpecifications());
        List<ScmUnitConversion> scmUnitConversionList = scmCommodityRespVO.getScmUnitConversionList();
        if(ObjectUtil.isNotEmpty(scmUnitConversionList)){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(scmUnitConversionList);
                materialReqVO.setSpecificationsRules(json);
            } catch (Exception e) {

            }
        }else{
            materialReqVO.setSpecificationsRules("[]");
        }
//        materialReqVO.setSpecificationsRules(scmCommodityRespVO.getSpecificationsRules());
        materialReqVO.setStasticsCommodityName(scmCommodityRespVO.getStasticsCommodityName());
        saveMaterialReqVO.add(materialReqVO);
        return saveMaterialReqVO;
    }




    @Override
    public List<Long> lossIdsByStoreTake(Long storeId, Long takeId) {

        LambdaQueryWrapperX<RawMaterialLossRecordDO> wrapperX = new LambdaQueryWrapperX<>();
        wrapperX.eqIfPresent(RawMaterialLossRecordDO::getStoreId,storeId);
        wrapperX.select(RawMaterialLossRecordDO::getId);

        if (takeId == null){
            wrapperX.isNull(RawMaterialLossRecordDO::getTakeId);
        }else {
            wrapperX.eq(RawMaterialLossRecordDO::getTakeId, takeId);
        }

        List<RawMaterialLossRecordDO> lossRecordDOS = materialLossRecordMapper.selectList(wrapperX);
        return lossRecordDOS.stream().map(RawMaterialLossRecordDO::getId).toList();
    }


    /**
     * 通过损耗ID 获取损耗的数量 与 金额
     *
     * @param lossIds
     * @return key: 损耗数量:损耗金额
     */
    @Override
    public Map<Long, Pair<BigDecimal, BigDecimal>> getLossMap(List<Long> lossIds) {
        Map<Long, Pair<BigDecimal, BigDecimal>> resultMap = new HashMap<>();

        for (Long lossId : lossIds) {
            List<RawMaterialStockFlow> flows = flowService.flowByReferenceInfo(lossId.toString(), StockChangeEnum.LOSS);
            for (RawMaterialStockFlow flow : flows) {
                Long materialId = flow.getRawMaterialId();
                BigDecimal lossCount = flow.getChangeQuantity();
                BigDecimal lossAmount = lossCount.multiply(flow.getUnitPrice()).setScale(4, RoundingMode.HALF_UP);;

                // 检查是否已有该原材料的记录
                Pair<BigDecimal, BigDecimal> existingPair = resultMap.get(materialId);
                if (existingPair == null) {
                    // 如果没有，创建新记录
                    resultMap.put(materialId, Pair.of(lossCount, lossAmount));
                } else {
                    // 如果有，累加数量和金额
                    BigDecimal totalCount = existingPair.getKey().add(lossCount);
                    BigDecimal totalAmount = existingPair.getValue().add(lossAmount);
                    resultMap.put(materialId, Pair.of(totalCount, totalAmount));
                }
            }
        }
        return resultMap;
    }

    @Override
    public BigDecimal updateUninventoriedLossRecord(Long takeId,Long storeId) {
        BigDecimal mm =BigDecimal.ZERO;
        if (ObjectUtil.isEmpty(takeId)) {
            return BigDecimal.ZERO;
        }
        if (ObjectUtil.isEmpty(storeId)) {
            return BigDecimal.ZERO;
        }
        // 查询需要更新的记录
        LambdaQueryWrapperX<RawMaterialLossRecordDO> wrapperX = new LambdaQueryWrapperX<>();
        wrapperX.isNull(RawMaterialLossRecordDO::getTakeId);
        wrapperX.eqIfPresent(RawMaterialLossRecordDO::getStoreId,storeId);
        List<RawMaterialLossRecordDO> records = materialLossRecordMapper.selectList(wrapperX);

        if (CollectionUtil.isEmpty(records)) {
            return BigDecimal.ZERO;
        }
        for (RawMaterialLossRecordDO record : records) {
            mm = mm.add(record.getTotalAmount());
            
        }
        if (!records.isEmpty()) {
            // 获取流水金额
            BigDecimal flowAmount = getFlowAmount(storeId);
            BigDecimal result = BigDecimal.ZERO;
            if(flowAmount.compareTo(BigDecimal.ZERO) != 0){
                BigDecimal positiveAmount = flowAmount.abs();
                result = mm.divide(positiveAmount, 4, RoundingMode.HALF_UP);
            }


            RawMaterialLossRecordDO latestRecord = Collections.max(
                    records,
                    Comparator.comparing(RawMaterialLossRecordDO::getCreateTime)
            );
            // 批量设置takeId
            records.forEach(record -> {
                if(latestRecord!=null){
                    LocalDateTime createTime = latestRecord.getCreateTime();
                    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String formatted = createTime.format(formatter1);
                    record.setAttritionRate("统计结果已合计在"+formatted+"损耗单内");
                }
                record.setTakeId(takeId);
            });

            boolean success = this.updateBatchById(records);
            if(latestRecord!=null){
                if(flowAmount.compareTo(BigDecimal.ZERO) == 0){
                    latestRecord.setAttritionRate("-");
                }else{
                    BigDecimal oneHundred = new BigDecimal("100");
                    BigDecimal attritionRate= result.multiply(oneHundred).setScale(2, RoundingMode.HALF_UP);;
                    latestRecord.setAttritionRate(attritionRate.toString()+"%");
                }

                this.updateById(latestRecord);
            }
            return result;
            
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAttritionRate(Long storeId) {
        BigDecimal mm =BigDecimal.ZERO;

        if (ObjectUtil.isEmpty(storeId)) {
            return BigDecimal.ZERO;
        }
        // 查询需要更新的记录
        LambdaQueryWrapperX<RawMaterialLossRecordDO> wrapperX = new LambdaQueryWrapperX<>();
        wrapperX.isNull(RawMaterialLossRecordDO::getTakeId);
        wrapperX.eqIfPresent(RawMaterialLossRecordDO::getStoreId,storeId);
        List<RawMaterialLossRecordDO> records = materialLossRecordMapper.selectList(wrapperX);

        if (CollectionUtil.isEmpty(records)) {
            return BigDecimal.ZERO;
        }
        for (RawMaterialLossRecordDO record : records) {
            mm = mm.add(record.getTotalAmount());

        }
        if (!records.isEmpty()) {
            // 获取流水金额
            BigDecimal flowAmount = getFlowAmount(storeId);
            BigDecimal result = BigDecimal.ZERO;
            if(flowAmount.compareTo(BigDecimal.ZERO) != 0){
                BigDecimal positiveAmount = flowAmount.abs();
                result = mm.divide(positiveAmount, 4, RoundingMode.HALF_UP);
            }
            return result;

        }
        return BigDecimal.ZERO;

    }

    @Override
    public BigDecimal updateUninventoriedLossRecord(Long takeId, Long storeId, BigDecimal attritionRate) {
        // 查询需要更新的记录
        LambdaQueryWrapperX<RawMaterialLossRecordDO> wrapperX = new LambdaQueryWrapperX<>();
        wrapperX.eqIfPresent(RawMaterialLossRecordDO::getStoreId,storeId);
        wrapperX.isNull(RawMaterialLossRecordDO::getTakeId);
        List<RawMaterialLossRecordDO> records = materialLossRecordMapper.selectList(wrapperX);
        if(ObjectUtil.isEmpty(records)){
            return BigDecimal.ZERO;
        }

        RawMaterialLossRecordDO latestRecord = Collections.max(
                records,
                Comparator.comparing(RawMaterialLossRecordDO::getCreateTime)
        );
        // 批量设置takeId
        records.forEach(record -> {
            if(latestRecord!=null){
                LocalDateTime createTime = latestRecord.getCreateTime();
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formatted = createTime.format(formatter1);
                record.setAttritionRate("统计结果已合计在"+formatted+"损耗单内");
            }
            record.setTakeId(takeId);
        });

        boolean success = this.updateBatchById(records);
        if(latestRecord!=null){
            if(attritionRate.compareTo(BigDecimal.ZERO) == 0){
                latestRecord.setAttritionRate("-");
            }else{
                BigDecimal oneHundred = new BigDecimal("100");
                BigDecimal bigDecimal= attritionRate.multiply(oneHundred).setScale(2, RoundingMode.HALF_UP);
                latestRecord.setAttritionRate(bigDecimal.toString()+"%");
            }

            this.updateById(latestRecord);
        }
        return attritionRate;
    }

    @Override
    public List<MaterialTypeVo> lossTypeList() {
        List<MaterialTypeVo> list = new ArrayList<>();
        for (LossType type : LossType.values()){
            MaterialTypeVo materialTypeVo = new MaterialTypeVo();
            Integer code = type.getCode();
            String description = type.getDescription();
            materialTypeVo.setCode(code);
            materialTypeVo.setDescription(description);
            list.add(materialTypeVo);
        }

        return list;
    }




    /**
     * 计算总金额
     * @param totalAmount 当前总金额
     * @param quantity 数量
     * @param price 单价
     * @return 累加后的总金额
     */
    private BigDecimal calculateTotalAmount(BigDecimal totalAmount, BigDecimal quantity, BigDecimal price) {
        BigDecimal safeQuantity = getSafeBigDecimal(quantity);
        BigDecimal safePrice = getSafeBigDecimal(price);
        return totalAmount.add(safeQuantity.multiply(safePrice).setScale(4, RoundingMode.HALF_UP));
    }
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }




    @Override
    public List<MaterialListRespVo> modifyCommodity(CommoditySplitMaterialReqVO commoditySplitMaterialReqVO) {

        List<MaterialListRespVo> resultList = new ArrayList<>();
        List<MaterialDataVo> materialDataVos = commoditySplitMaterialReqVO.getMaterialDataVos();

        if (ObjectUtil.isEmpty(materialDataVos)) {
            return resultList;
        }
        //记录原材料列表
        List<RawMaterialIdVO> repertoryList = new ArrayList<>();
        Map<Long, BigDecimal> repertoryMap = new HashMap<>();



        for (MaterialDataVo materialDataVo : materialDataVos) {
            validateMaterialData(materialDataVo);

            try {
                if (materialDataVo.getCommodityType().equals(1)) {
                    processRecipeCommodity(materialDataVo, commoditySplitMaterialReqVO.getStoreId(), resultList,repertoryList,repertoryMap);
                } else if (materialDataVo.getCommodityType().equals(2)) {
                    processRawMaterialCommodity(materialDataVo, commoditySplitMaterialReqVO.getStoreId(), resultList);
                }
            } catch (Exception e) {
//                log.error("处理商品数据失败: commodityId={}, skuId={}",
//                        materialDataVo.getCommodityId(), materialDataVo.getSkuId(), e);
                throw e;
            }
        }
        if(ObjectUtil.isNotEmpty(resultList)){
            List<MaterialListRespVo> respVoArrayList = new ArrayList<>();

            Map<String, List<MaterialListRespVo>> collect = resultList.stream()
                    .collect(Collectors.groupingBy(MaterialListRespVo::getMaterialsId));
            for (String s : collect.keySet()) {
                List<MaterialListRespVo> list = collect.get(s);
                MaterialListRespVo listRespVo = list.get(0);


                int flag = 0;
                for (MaterialListRespVo materialListRespVo : list) {
                    String usedUnit = materialListRespVo.getUsedUnit();
                    if(usedUnit.equals(listRespVo.getUsedUnit())){
                        flag = flag + 1;
                    }

                }
                BigDecimal totalQuantitys = new BigDecimal(0);
                if(list.size() == flag){
                    for (MaterialListRespVo materialListRespVo : list) {

                        BigDecimal voQuantity = materialListRespVo.getQuantity();
                        if(voQuantity!=null){
                            totalQuantitys = totalQuantitys.add(voQuantity)
                                    .setScale(4, RoundingMode.HALF_UP);
                        }


                    }
                    MaterialListRespVo merged = new MaterialListRespVo();
                    // 设置相同的信息
                    merged.setRawMaterialId(listRespVo.getRawMaterialId());
                    merged.setMaterialsId(listRespVo.getMaterialsId());
                    merged.setMaterialsName(listRespVo.getMaterialsName());
                    merged.setUsedUnit(listRespVo.getUsedUnit());
                    merged.setUnitList(listRespVo.getUnitList());
                    merged.setCalUnitList(listRespVo.getCalUnitList());
                    merged.setUnitPrice(listRespVo.getUnitPrice());
                    merged.setStasticsName(listRespVo.getStasticsName());
                    merged.setQuantity(totalQuantitys);
                    respVoArrayList.add(merged);

                }else {
                    BigDecimal totalQuantity = BigDecimal.ZERO;

                    BigDecimal price = BigDecimal.ZERO;
                    String minUnit = new String();
                    List<CommodityConversion> calUnitList = listRespVo.getCalUnitList();
                    if(ObjectUtil.isNotEmpty(calUnitList)){
                        for (CommodityConversion commodityConversion : calUnitList) {
                            minUnit = commodityConversion.getAfterUnit();
                        }
                    }


                    for (MaterialListRespVo materialListRespVo : list) {
                        if(materialListRespVo.getUsedUnit().equals(minUnit)){
                            BigDecimal quantity = materialListRespVo.getQuantity();
                            if (quantity != null) {
                                totalQuantity = totalQuantity.add(quantity)
                                        .setScale(4, RoundingMode.HALF_UP);
                                price = materialListRespVo.getUnitPrice();
                            }
                        }else {
                            BigDecimal mm = new BigDecimal(1);
                            for (CommodityConversion commodityConversion : calUnitList) {
                                if(commodityConversion.getBeforeUnit().equals(materialListRespVo.getUsedUnit())){
                                    mm = mm.multiply(commodityConversion.getAfterNumber()).setScale(4, RoundingMode.HALF_UP);
                                }
                            }
                            BigDecimal quantity = materialListRespVo.getQuantity();
                            if (quantity != null) {
                                totalQuantity = totalQuantity.add(mm.multiply(materialListRespVo.getQuantity()).setScale(4, RoundingMode.HALF_UP))
                                        .setScale(4, RoundingMode.HALF_UP);
                            }

                        }

                    }
                    MaterialListRespVo merged = new MaterialListRespVo();

                    // 设置相同的信息
                    merged.setRawMaterialId(listRespVo.getRawMaterialId());
                    merged.setMaterialsId(listRespVo.getMaterialsId());
                    merged.setMaterialsName(listRespVo.getMaterialsName());
                    merged.setUsedUnit(minUnit);
                    merged.setUnitList(listRespVo.getUnitList());
                    merged.setCalUnitList(listRespVo.getCalUnitList());
                    if(price.compareTo(BigDecimal.ZERO) != 0){
                        merged.setUnitPrice(price);
                    }else {
                        if(ObjectUtil.isNotEmpty(listRespVo.getRawMaterialId())){
                            Long rawMaterialId = listRespVo.getRawMaterialId();
                            RawMaterial rawMaterial = rawMaterialMapper.selectById(rawMaterialId);
                            BigDecimal unitPrice = rawMaterial.getUnitPrice();
                            merged.setUnitPrice(unitPrice);
                        }

                    }

                    merged.setStasticsName(listRespVo.getStasticsName());
                    merged.setQuantity(totalQuantity);
                    respVoArrayList.add(merged);
                }



            }
            return respVoArrayList;
            }




        return resultList;
    }

    @Override
    public List<MaterialListRespVo> splitCommodityMaterial(
        List<SplitCommodityMaterialReqVO> splitCommodityMaterialReqVO) {

        if(ObjectUtil.isNotEmpty(splitCommodityMaterialReqVO)){
            for (SplitCommodityMaterialReqVO commodityMaterialReqVO : splitCommodityMaterialReqVO) {
                List<MaterialListRespVo> resultList = new ArrayList<>();
                //记录原材料列表
                List<RawMaterialIdVO> repertoryList = new ArrayList<>();
                Map<Long, BigDecimal> repertoryMap = new HashMap<>();



                for (ProductQuantity productQuantity : commodityMaterialReqVO.getProductQuantities()) {
                    productQuantityProcessRecipeCommodity(productQuantity, commodityMaterialReqVO.getStoreId(),commodityMaterialReqVO.getChannelType(), resultList,repertoryList,repertoryMap,commodityMaterialReqVO);
                }
                //合并后的数据
                List<MaterialListRespVo> respVoArrayList = new ArrayList<>();
                if(ObjectUtil.isNotEmpty(resultList)){
                    Map<String, List<MaterialListRespVo>> collect = resultList.stream()
                            .collect(Collectors.groupingBy(MaterialListRespVo::getMaterialsId));
                    for (String s : collect.keySet()) {
                        List<MaterialListRespVo> list = collect.get(s);
                        MaterialListRespVo listRespVo = list.get(0);


                        int flag = 0;
                        for (MaterialListRespVo materialListRespVo : list) {
                            String usedUnit = materialListRespVo.getUsedUnit();
                            if(usedUnit.equals(listRespVo.getUsedUnit())){
                                flag = flag + 1;
                            }

                        }
                        BigDecimal totalQuantitys = new BigDecimal(0);
                        if(list.size() == flag){
                            for (MaterialListRespVo materialListRespVo : list) {

                                BigDecimal voQuantity = materialListRespVo.getQuantity();
                                if(voQuantity!=null){
                                    totalQuantitys = totalQuantitys.add(voQuantity)
                                            .setScale(4, RoundingMode.HALF_UP);
                                }


                            }
                            MaterialListRespVo merged = new MaterialListRespVo();
                            // 设置相同的信息
                            merged.setRawMaterialId(listRespVo.getRawMaterialId());
                            merged.setMaterialsId(listRespVo.getMaterialsId());
                            merged.setMaterialsName(listRespVo.getMaterialsName());
                            merged.setUsedUnit(listRespVo.getUsedUnit());
                            merged.setUnitList(listRespVo.getUnitList());
                            merged.setCalUnitList(listRespVo.getCalUnitList());
                            merged.setUnitPrice(listRespVo.getUnitPrice());
                            merged.setStasticsName(listRespVo.getStasticsName());
                            merged.setQuantity(totalQuantitys);
                            respVoArrayList.add(merged);

                        }else {
                            BigDecimal totalQuantity = BigDecimal.ZERO;

                            BigDecimal price = BigDecimal.ZERO;
                            String minUnit = new String();
                            List<CommodityConversion> calUnitList = listRespVo.getCalUnitList();
                            if(ObjectUtil.isNotEmpty(calUnitList)){
                                for (CommodityConversion commodityConversion : calUnitList) {
                                    minUnit = commodityConversion.getAfterUnit();
                                }
                            }


                            for (MaterialListRespVo materialListRespVo : list) {
                                if(materialListRespVo.getUsedUnit().equals(minUnit)){
                                    BigDecimal quantity = materialListRespVo.getQuantity();
                                    if (quantity != null) {
                                        totalQuantity = totalQuantity.add(quantity)
                                                .setScale(4, RoundingMode.HALF_UP);
                                        price = materialListRespVo.getUnitPrice();
                                    }
                                }else {
                                    BigDecimal mm = new BigDecimal(1);
                                    for (CommodityConversion commodityConversion : calUnitList) {
                                        if(commodityConversion.getBeforeUnit().equals(materialListRespVo.getUsedUnit())){
                                            mm = mm.multiply(commodityConversion.getAfterNumber()).setScale(4, RoundingMode.HALF_UP);
                                        }
                                    }
                                    BigDecimal quantity = materialListRespVo.getQuantity();
                                    if (quantity != null) {
                                        totalQuantity = totalQuantity.add(mm.multiply(materialListRespVo.getQuantity()).setScale(4, RoundingMode.HALF_UP))
                                                .setScale(4, RoundingMode.HALF_UP);
                                    }

                                }

                            }
                            MaterialListRespVo merged = new MaterialListRespVo();

                            // 设置相同的信息
                            merged.setRawMaterialId(listRespVo.getRawMaterialId());
                            merged.setMaterialsId(listRespVo.getMaterialsId());
                            merged.setMaterialsName(listRespVo.getMaterialsName());
                            merged.setUsedUnit(minUnit);
                            merged.setUnitList(listRespVo.getUnitList());
                            merged.setCalUnitList(listRespVo.getCalUnitList());
                            if(price.compareTo(BigDecimal.ZERO) != 0){
                                merged.setUnitPrice(price);
                            }else {
                                if(ObjectUtil.isNotEmpty(listRespVo.getRawMaterialId())){
                                    Long rawMaterialId = listRespVo.getRawMaterialId();
                                    RawMaterial rawMaterial = rawMaterialMapper.selectById(rawMaterialId);
                                    BigDecimal unitPrice = rawMaterial.getUnitPrice();
                                    merged.setUnitPrice(unitPrice);
                                }

                            }

                            merged.setStasticsName(listRespVo.getStasticsName());
                            merged.setQuantity(totalQuantity);
                            respVoArrayList.add(merged);
                        }
                    }
                    //门店下新增原材料，并扣减库存
                    saveProductQuantityStock(commodityMaterialReqVO.getStoreId(),commodityMaterialReqVO.getOrderNo(),commodityMaterialReqVO.getChannelType(),respVoArrayList);
                }

            }
        }
        return List.of();
    }
    /**
     * 处理配方商品
     */
    private void productQuantityProcessRecipeCommodity(ProductQuantity productQuantity, Long storeId, ChannelType channelType,
                                                     List<MaterialListRespVo> resultList, List<RawMaterialIdVO> repertoryList, Map<Long,BigDecimal> repertoryMap,SplitCommodityMaterialReqVO splitCommodityMaterialReqVO) {
        //获取当前名称下配方列表
//        List<TriRecipeDO> recipes = findNameRecipesByCommodity(productQuantity,channelType);
        TriRecipeReqVO triRecipeReqVO = new TriRecipeReqVO();
        List<String> stringList = new ArrayList<>();
        stringList.add(productQuantity.getProductName());
        triRecipeReqVO.setTriCommodityName(stringList);
        triRecipeReqVO.setTriType(channelType.getCode());
        List<TriRecipeDO> recipes = triRecipeService.selectByNames(triRecipeReqVO);

        if (ObjectUtil.isNotEmpty(recipes)) {
            for (TriRecipeDO recipe : recipes) {
                if (recipe.getIsAlternative().equals(0L)) {
                    productQuantityModifyProcessNonAlternativeRecipe(recipe, productQuantity, storeId, resultList,splitCommodityMaterialReqVO);
                } else if (recipe.getIsAlternative().equals(1L)) {
                    //可替换原材料逻辑
                    productQuantityProcessAlternativeRecipe(recipe, productQuantity, storeId, resultList,repertoryList,repertoryMap);
                }
            }
        }
//        else {
//            ImportLogRecord importLogRecord = new ImportLogRecord();
//            importLogRecord.setLogType(RawMaterialLog.RECIPE_NOT_FOUND.getType());
//            importLogRecord.setCommodityName(productQuantity.getProductName());
//            importLogRecord.setQuantity(productQuantity.getQuantity());
//            importLogRecord.setStoreId(storeId);
//            importLogRecordMapper.insert(importLogRecord);
//        }

    }

    private void productQuantityModifyProcessNonAlternativeRecipe(TriRecipeDO recipe, ProductQuantity productQuantity,
                                                                Long storeId, List<MaterialListRespVo> resultList,SplitCommodityMaterialReqVO splitCommodityMaterialReqVO) {
        CommodityWhetherVO commodityWhetherVO = productQuantityCalculateConsumptionTwo(recipe.getConsumption(), productQuantity, recipe,storeId);

        RawMaterial rawMaterial = findRawMaterialByCode(recipe.getMaterialsCode(), storeId);

        MaterialListRespVo responseVo = productQuantityCreateMaterialResponseFromRecipe(rawMaterial,recipe,commodityWhetherVO.getTotalNumber(),productQuantity,storeId);
        resultList.add(responseVo);

    }

    /**
     * 计算消耗量
     */
    private CommodityWhetherVO productQuantityCalculateConsumptionTwo(String recipeConsumption, ProductQuantity productQuantity,TriRecipeDO recipe,Long storeId) {
        if(ObjectUtil.isNotEmpty(recipe.getUsedUnit())){

            CommodityWhetherVO commodityWhetherVO = new CommodityWhetherVO();
            BigDecimal consumptionMultiple = new BigDecimal(1);
            Long materialsId = recipe.getMaterialsId();
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,materialsId);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
            BigDecimal qua = new BigDecimal(productQuantity.getQuantity());
            if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                commodityWhetherVO.setScmUnitConversions(scmUnitConversions);
                ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                if(!recipe.getUsedUnit().equals(scmUnitConversion.getAfterUnit())){
                    for (ScmUnitConversion unitConversion : scmUnitConversions) {
                        if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                            consumptionMultiple = BigDecimal.valueOf(unitConversion.getAfterNumber());
                        }

                    }

                }else{
                    commodityWhetherVO.setIsUnit(true);
                }
                BigDecimal consumption = new BigDecimal(recipeConsumption);

                BigDecimal multiply = consumption.multiply(consumptionMultiple).setScale(4, RoundingMode.HALF_UP);
                BigDecimal decimal = multiply.multiply(qua).setScale(4, RoundingMode.HALF_UP);
                commodityWhetherVO.setTotalNumber(decimal);
            }else{
                //说明是单品
                BigDecimal consumption = new BigDecimal(recipeConsumption);
                BigDecimal multiply = consumption.multiply(qua).setScale(4, RoundingMode.HALF_UP);
                commodityWhetherVO.setTotalNumber(multiply);
            }


            return commodityWhetherVO;
        }else {
            ImportLogRecord importLogRecord = new ImportLogRecord();
            importLogRecord.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
            importLogRecord.setCommodityName(productQuantity.getProductName());
            importLogRecord.setQuantity(productQuantity.getQuantity());
            importLogRecord.setStoreId(storeId);
            importLogRecordMapper.insert(importLogRecord);
        }

        return null;


    }

    /**
     * 查询配方
     */
    private List<TriRecipeDO> findNameRecipesByCommodity(ProductQuantity productQuantity,ChannelType channelType) {
        LambdaQueryWrapperX<TriRecipeDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(TriRecipeDO::getTriCommodityName, productQuantity.getProductName());
        wrapper.eqIfPresent(TriRecipeDO::getTriType, channelType.getCode());
        wrapper.isNotNull(TriRecipeDO::getConsumption);
        return triRecipeMapper.selectList(wrapper);
    }










    @Override
    @Transactional
    public Boolean schoolProgramModifyCommodity(SchoolProgramCommoditySplitMaterialReqVO schoolProgramCommoditySplitMaterialReqVO) {
        List<MaterialListRespVo> resultList = new ArrayList<>();
        List<MaterialDataVo> materialDataVos = schoolProgramCommoditySplitMaterialReqVO.getMaterialDataVos();
//        List<ImportTaskService.ErrorRecord> errorRecords = new ArrayList<>();

        if (ObjectUtil.isEmpty(materialDataVos)) {
            return false;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (MaterialDataVo materialDataVo : materialDataVos) {
            if (materialDataVo.getQuantity() != null) {
                total = total.add(materialDataVo.getQuantity());
            }
        }
        //记录原材料列表
        List<RawMaterialIdVO> repertoryList = new ArrayList<>();
        Map<Long, BigDecimal> repertoryMap = new HashMap<>();



        List<String> messageList = new ArrayList<>();
        List<String> codeMessageList = new ArrayList<>();
        List<String> categoryMessageList = new ArrayList<>();
        List<String> categoryNameMessageList = new ArrayList<>();
        for (MaterialDataVo materialDataVo : materialDataVos) {
            if(ObjectUtil.isEmpty(materialDataVo.getSkuId())){
                throw exception(1_003_007_025,"skuId不存在,请联系相关技术人员");
            }
            schoolProgramProcessRecipeCommodity(materialDataVo, schoolProgramCommoditySplitMaterialReqVO.getStoreId(), resultList,repertoryList,repertoryMap,messageList,codeMessageList,categoryMessageList,categoryNameMessageList);
        }
        if(ObjectUtil.isNotEmpty(messageList)){
            String result = messageList.stream()
                    .collect(Collectors.joining(","));
            throw exception(1_003_007_015,"商品名称"+result+"没有设置配方");
        }

//        if(ObjectUtil.isNotEmpty(codeMessageList)){
//            String result = codeMessageList.stream()
//                    .collect(Collectors.joining(","));
//            throw exception(1_003_011_001, "门店下商品编码为" + result + "不存在");
//        }
//
//        if(ObjectUtil.isNotEmpty(categoryMessageList)){
//            String result = categoryMessageList.stream()
//                    .collect(Collectors.joining(","));
//            throw exception(1_003_011_003, "门店下"+categoryNameMessageList+"商品在二级类目"+result+"下没有原材料");
//        }

        //合并后的数据
        List<MaterialListRespVo> respVoArrayList = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(resultList)){
            Map<String, List<MaterialListRespVo>> collect = resultList.stream()
                    .collect(Collectors.groupingBy(MaterialListRespVo::getMaterialsId));
            for (String s : collect.keySet()) {
                List<MaterialListRespVo> list = collect.get(s);
                MaterialListRespVo listRespVo = list.get(0);


                int flag = 0;
                for (MaterialListRespVo materialListRespVo : list) {
                    String usedUnit = materialListRespVo.getUsedUnit();
                    if(usedUnit.equals(listRespVo.getUsedUnit())){
                        flag = flag + 1;
                    }

                }
                BigDecimal totalQuantitys = new BigDecimal(0);
                if(list.size() == flag){
                    for (MaterialListRespVo materialListRespVo : list) {

                        BigDecimal voQuantity = materialListRespVo.getQuantity();
                        if(voQuantity!=null){
                            totalQuantitys = totalQuantitys.add(voQuantity)
                                    .setScale(4, RoundingMode.HALF_UP);
                        }


                    }
                    MaterialListRespVo merged = new MaterialListRespVo();
                    // 设置相同的信息
                    merged.setRawMaterialId(listRespVo.getRawMaterialId());
                    merged.setMaterialsId(listRespVo.getMaterialsId());
                    merged.setMaterialsName(listRespVo.getMaterialsName());
                    merged.setUsedUnit(listRespVo.getUsedUnit());
                    merged.setUnitList(listRespVo.getUnitList());
                    merged.setCalUnitList(listRespVo.getCalUnitList());
                    merged.setUnitPrice(listRespVo.getUnitPrice());
                    merged.setStasticsName(listRespVo.getStasticsName());
                    merged.setQuantity(totalQuantitys);
                    respVoArrayList.add(merged);

                }else {
                    BigDecimal totalQuantity = BigDecimal.ZERO;

                    BigDecimal price = BigDecimal.ZERO;
                    String minUnit = new String();
                    List<CommodityConversion> calUnitList = listRespVo.getCalUnitList();
                    if(ObjectUtil.isNotEmpty(calUnitList)){
                        for (CommodityConversion commodityConversion : calUnitList) {
                            minUnit = commodityConversion.getAfterUnit();
                        }
                    }


                    for (MaterialListRespVo materialListRespVo : list) {
                        if(materialListRespVo.getUsedUnit().equals(minUnit)){
                            BigDecimal quantity = materialListRespVo.getQuantity();
                            if (quantity != null) {
                                totalQuantity = totalQuantity.add(quantity)
                                        .setScale(4, RoundingMode.HALF_UP);
                                price = materialListRespVo.getUnitPrice();
                            }
                        }else {
                            BigDecimal mm = new BigDecimal(1);
                            for (CommodityConversion commodityConversion : calUnitList) {
                                if(commodityConversion.getBeforeUnit().equals(materialListRespVo.getUsedUnit())){
                                    mm = mm.multiply(commodityConversion.getAfterNumber()).setScale(4, RoundingMode.HALF_UP);
                                }
                            }
                            BigDecimal quantity = materialListRespVo.getQuantity();
                            if (quantity != null) {
                                totalQuantity = totalQuantity.add(mm.multiply(materialListRespVo.getQuantity()).setScale(4, RoundingMode.HALF_UP))
                                        .setScale(4, RoundingMode.HALF_UP);
                            }

                        }

                    }
                    MaterialListRespVo merged = new MaterialListRespVo();

                    // 设置相同的信息
                    merged.setRawMaterialId(listRespVo.getRawMaterialId());
                    merged.setMaterialsId(listRespVo.getMaterialsId());
                    merged.setMaterialsName(listRespVo.getMaterialsName());
                    merged.setUsedUnit(minUnit);
                    merged.setUnitList(listRespVo.getUnitList());
                    merged.setCalUnitList(listRespVo.getCalUnitList());
                    if(price.compareTo(BigDecimal.ZERO) != 0){
                        merged.setUnitPrice(price);
                    }else {
                        if(ObjectUtil.isNotEmpty(listRespVo.getRawMaterialId())){
                            Long rawMaterialId = listRespVo.getRawMaterialId();
                            RawMaterial rawMaterial = rawMaterialMapper.selectById(rawMaterialId);
                            BigDecimal unitPrice = rawMaterial.getUnitPrice();
                            merged.setUnitPrice(unitPrice);
                        }

                    }

                    merged.setStasticsName(listRespVo.getStasticsName());
                    merged.setQuantity(totalQuantity);
                    respVoArrayList.add(merged);
                }
            }
        }




        String taskId = UUID.randomUUID().toString().replace("-", "");
        saveSchoolProgramStock(taskId,schoolProgramCommoditySplitMaterialReqVO.getStoreId(),respVoArrayList);

        String jsonString = convertToJson(schoolProgramCommoditySplitMaterialReqVO);

        // 保存错误记录到数据库
        createTask(total,taskId,schoolProgramCommoditySplitMaterialReqVO.getName(), SCHOOL_PROGRAM.getCode(),jsonString);
        return true;
    }

    @Override
    public SchoolProgramCommoditySplitMaterialDataVO selectInfo(Long id) {
        ImportTask importTask = importTaskMapper.selectById(id);
        if(importTask!=null){
            String productParam = importTask.getProductParam();
            if(ObjectUtil.isNotEmpty(productParam)){
                SchoolProgramCommoditySplitMaterialDataVO dataVO = convertJsonToObject(productParam);
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(dataVO.getStoreId());
                if(ObjectUtil.isNotEmpty(storeByStoreId)){
                    StoreDTO data = storeByStoreId.getData();
                    dataVO.setStoreName(data.getStoreName());
                }
                return dataVO;
            }
        }
        return null;
    }

    public SchoolProgramCommoditySplitMaterialDataVO convertJsonToObject(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, SchoolProgramCommoditySplitMaterialDataVO.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }


    public Boolean saveProductQuantityStock(Long storeId,String orderNo,ChannelType channelType, List<MaterialListRespVo> respVoArrayList){

        List<StockChangeDTO.ChangeInfo> changeInfoList = new ArrayList<>();
        StockChangeDTO stockChangeDTO = new StockChangeDTO();
        stockChangeDTO.setStoreId(storeId);

        if(channelType.getCode().equals(ChannelType.ELE_ME.getCode())){
            stockChangeDTO.setType(StockChangeEnum.ELE_ME_OUT);
        }else if(channelType.getCode().equals(ChannelType.SAN_KUAI.getCode())){
            stockChangeDTO.setType(StockChangeEnum.SAN_KUAI_OUT);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        if(ObjectUtil.isNotEmpty(respVoArrayList)){
            for (MaterialListRespVo materialDataReqVo : respVoArrayList) {
                if(ObjectUtil.isNotEmpty(materialDataReqVo.getRawMaterialId())){

                    StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                    //选择的单位
                    String unit = materialDataReqVo.getUsedUnit();
                    Long rawMaterialId = materialDataReqVo.getRawMaterialId();
                    LambdaQueryWrapperX<RawMaterial> wrapperX = new LambdaQueryWrapperX<>();
                    wrapperX.eq(RawMaterial::getId,rawMaterialId);
                    RawMaterial rawMaterial = rawMaterialMapper.selectOne(wrapperX);
                    if(rawMaterial!=null){
                        //最小单位
                        if(rawMaterial.getMinUnit().equals(unit)){
                            BigDecimal quantity = materialDataReqVo.getQuantity();
                            BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                            ss.setQuantity(negativeBigDecimal);
                            ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                            ss.setChooseUnit(materialDataReqVo.getUsedUnit());
                            BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                            BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                            totalAmount = calculateTotalAmount(totalAmount, qu, price);
                            changeInfoList.add(ss);
                        }else{
                            //选择的不是最小单位，需要转化
                            BigDecimal aa = BigDecimal.ZERO;

                            try {
                                String specificationsRules = rawMaterial.getSpecificationsRules();
                                if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                                    ObjectMapper objectMapper = new ObjectMapper();

                                    List<CommodityConversion> conversionList = objectMapper.readValue(
                                            specificationsRules,
                                            new TypeReference<List<CommodityConversion>>() {}
                                    );
                                    for (CommodityConversion commodityConversion : conversionList) {
                                        if(commodityConversion.getBeforeUnit().equals(unit)){
                                            aa = commodityConversion.getAfterNumber();
                                        }
                                    }
                                    BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                    BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                    ss.setQuantity(negativeBigDecimal);
                                    ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                }else{
                                    ss.setQuantity(materialDataReqVo.getQuantity());
                                    ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ss.setChooseUnit(materialDataReqVo.getUsedUnit());

                            BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                            BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                            totalAmount = calculateTotalAmount(totalAmount, qu, price);
                            changeInfoList.add(ss);
                        }
                    }
                }else{
                    StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                    LocalDateTime now = LocalDateTime.now();
                    ScmCommodityInfoReqVO scmCommodityInfoReqVO = new ScmCommodityInfoReqVO();
                    scmCommodityInfoReqVO.setCommodityCode(materialDataReqVo.getMaterialsId());
//                    scmCommodityInfoReqVO.setWarehouseId(saveReq.getWarehouseId());
                    scmCommodityInfoReqVO.setStasticsName(materialDataReqVo.getStasticsName());
                    scmCommodityInfoReqVO.setStoreId(storeId);
                    ScmCommodityInfoRespVO scmCommodityRespVO = scmCommodityService.selectCommodityInfo(scmCommodityInfoReqVO, DeviceType.APP.getValue());
                    if(scmCommodityRespVO!=null){
                        MaterialLossSaveReq saveReq = new MaterialLossSaveReq();
                        saveReq.setStoreId(storeId);
                        List<SaveMaterialReqVO> saveMaterialReqVOs = getSaveMaterialReqVOS(scmCommodityRespVO, now ,saveReq);
                        rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
                        RawMaterial selectMaterial = rawMaterialMapper.selectMaterial(saveReq.getStoreId(), materialDataReqVo.getMaterialsId());
                        if(selectMaterial!=null){
                            ss.setRawMaterialId(selectMaterial.getId());
                            String unit = materialDataReqVo.getUsedUnit();
                            //最小单位
                            if(selectMaterial.getMinUnit().equals(unit)){
                                BigDecimal quantity = materialDataReqVo.getQuantity();
                                BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                ss.setQuantity(negativeBigDecimal);
                                ss.setChooseUnit(materialDataReqVo.getUsedUnit());
                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }else{
                                //选择的不是最小单位，需要转化
                                BigDecimal aa = BigDecimal.ZERO;

                                try {
                                    String specificationsRules = selectMaterial.getSpecificationsRules();
                                    if (ObjectUtil.isNotEmpty(selectMaterial.getSpecificationsRules())&& !selectMaterial.getSpecificationsRules().equals("[]")) {
                                        ObjectMapper objectMapper = new ObjectMapper();

                                        List<CommodityConversion> conversionList = objectMapper.readValue(
                                                specificationsRules,
                                                new TypeReference<List<CommodityConversion>>() {}
                                        );
                                        for (CommodityConversion commodityConversion : conversionList) {
                                            if(commodityConversion.getBeforeUnit().equals(unit)){
                                                aa = commodityConversion.getAfterNumber();
                                            }
                                        }
                                        BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                        BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                        ss.setQuantity(negativeBigDecimal);
                                    }else{
                                        ss.setQuantity(materialDataReqVo.getQuantity());
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ss.setChooseUnit(materialDataReqVo.getUsedUnit());

                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }
                        }
                    }
                }
            }
        }
        stockChangeDTO.setReferenceId(orderNo);
        stockChangeDTO.setChangeList(changeInfoList);
        Boolean b = rowMaterialStockFlowService.changeStock(stockChangeDTO);
        return true;
    }







    public Boolean saveSchoolProgramStock(String taskId,Long storeId, List<MaterialListRespVo> respVoArrayList){

        List<StockChangeDTO.ChangeInfo> changeInfoList = new ArrayList<>();
        StockChangeDTO stockChangeDTO = new StockChangeDTO();
        stockChangeDTO.setStoreId(storeId);

        stockChangeDTO.setType(StockChangeEnum.SCHOOL_OUT);

        BigDecimal totalAmount = BigDecimal.ZERO;

        if(ObjectUtil.isNotEmpty(respVoArrayList)){
            for (MaterialListRespVo materialDataReqVo : respVoArrayList) {
                if(ObjectUtil.isNotEmpty(materialDataReqVo.getRawMaterialId())){

                    StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                    //选择的单位
                    String unit = materialDataReqVo.getUsedUnit();
                    Long rawMaterialId = materialDataReqVo.getRawMaterialId();
                    LambdaQueryWrapperX<RawMaterial> wrapperX = new LambdaQueryWrapperX<>();
                    wrapperX.eq(RawMaterial::getId,rawMaterialId);
                    RawMaterial rawMaterial = rawMaterialMapper.selectOne(wrapperX);
                    if(rawMaterial!=null){
                        //最小单位
                        if(rawMaterial.getMinUnit().equals(unit)){
                            BigDecimal quantity = materialDataReqVo.getQuantity();
                            BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                            ss.setQuantity(negativeBigDecimal);
                            ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                            ss.setChooseUnit(materialDataReqVo.getUsedUnit());
                            BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                            BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                            totalAmount = calculateTotalAmount(totalAmount, qu, price);
                            changeInfoList.add(ss);
                        }else{
                            //选择的不是最小单位，需要转化
                            BigDecimal aa = BigDecimal.ZERO;

                            try {
                                String specificationsRules = rawMaterial.getSpecificationsRules();
                                if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                                    ObjectMapper objectMapper = new ObjectMapper();

                                    List<CommodityConversion> conversionList = objectMapper.readValue(
                                            specificationsRules,
                                            new TypeReference<List<CommodityConversion>>() {}
                                    );
                                    for (CommodityConversion commodityConversion : conversionList) {
                                        if(commodityConversion.getBeforeUnit().equals(unit)){
                                            aa = commodityConversion.getAfterNumber();
                                        }
                                    }
                                    BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                    BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                    ss.setQuantity(negativeBigDecimal);
                                    ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                }else{
                                    ss.setQuantity(materialDataReqVo.getQuantity());
                                    ss.setRawMaterialId(materialDataReqVo.getRawMaterialId());
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ss.setChooseUnit(materialDataReqVo.getUsedUnit());

                            BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                            BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                            totalAmount = calculateTotalAmount(totalAmount, qu, price);
                            changeInfoList.add(ss);
                        }
                    }
                }else{
                    MaterialLossSaveReq saveReq = new MaterialLossSaveReq();
                    saveReq.setStoreId(storeId);
                    StockChangeDTO.ChangeInfo ss = new StockChangeDTO.ChangeInfo();
                    LocalDateTime now = LocalDateTime.now();
                    ScmCommodityInfoReqVO scmCommodityInfoReqVO = new ScmCommodityInfoReqVO();
                    scmCommodityInfoReqVO.setCommodityCode(materialDataReqVo.getMaterialsId());
//                    scmCommodityInfoReqVO.setWarehouseId(saveReq.getWarehouseId());
                    scmCommodityInfoReqVO.setStasticsName(materialDataReqVo.getStasticsName());
                    scmCommodityInfoReqVO.setStoreId(saveReq.getStoreId());
                    ScmCommodityInfoRespVO scmCommodityRespVO = scmCommodityService.selectCommodityInfo(scmCommodityInfoReqVO, DeviceType.APP.getValue());
                    if(scmCommodityRespVO!=null){
                        List<SaveMaterialReqVO> saveMaterialReqVOs = getSaveMaterialReqVOS(scmCommodityRespVO, now ,saveReq);
                        rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
                        RawMaterial selectMaterial = rawMaterialMapper.selectMaterial(saveReq.getStoreId(), materialDataReqVo.getMaterialsId());
                        if(selectMaterial!=null){
                            ss.setRawMaterialId(selectMaterial.getId());
                            String unit = materialDataReqVo.getUsedUnit();
                            //最小单位
                            if(selectMaterial.getMinUnit().equals(unit)){
                                BigDecimal quantity = materialDataReqVo.getQuantity();
                                BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                ss.setQuantity(negativeBigDecimal);
                                ss.setChooseUnit(materialDataReqVo.getUsedUnit());
                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }else{
                                //选择的不是最小单位，需要转化
                                BigDecimal aa = BigDecimal.ZERO;

                                try {
                                    String specificationsRules = selectMaterial.getSpecificationsRules();
                                    if (ObjectUtil.isNotEmpty(selectMaterial.getSpecificationsRules())&& !selectMaterial.getSpecificationsRules().equals("[]")) {
                                        ObjectMapper objectMapper = new ObjectMapper();

                                        List<CommodityConversion> conversionList = objectMapper.readValue(
                                                specificationsRules,
                                                new TypeReference<List<CommodityConversion>>() {}
                                        );
                                        for (CommodityConversion commodityConversion : conversionList) {
                                            if(commodityConversion.getBeforeUnit().equals(unit)){
                                                aa = commodityConversion.getAfterNumber();
                                            }
                                        }
                                        BigDecimal quantity = materialDataReqVo.getQuantity().multiply(aa).setScale(4, RoundingMode.HALF_UP);;
                                        BigDecimal negativeBigDecimal = quantity != null ? quantity.negate() : BigDecimal.ZERO;
                                        ss.setQuantity(negativeBigDecimal);
                                    }else{
                                        ss.setQuantity(materialDataReqVo.getQuantity());
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                ss.setChooseUnit(materialDataReqVo.getUsedUnit());

                                BigDecimal qu = getSafeBigDecimal(materialDataReqVo.getQuantity());
                                BigDecimal price = getSafeBigDecimal(materialDataReqVo.getUnitPrice());
                                totalAmount = calculateTotalAmount(totalAmount, qu, price);
                                changeInfoList.add(ss);
                            }
                        }
                    }
                }
            }
        }
        stockChangeDTO.setReferenceId(taskId);
        stockChangeDTO.setChangeList(changeInfoList);
        Boolean b = rowMaterialStockFlowService.changeStock(stockChangeDTO);
        return true;
    }

    public static String convertToJson(SchoolProgramCommoditySplitMaterialReqVO reqVO) {
        try {
            return objectMapper.writeValueAsString(reqVO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * 创建导入任务
     */
    public String createTask(BigDecimal total,String taskId,String fileName, String channelType,String jsonString) {


        ImportTask task = new ImportTask();
        task.setTotalCount(total != null ? total.intValue() : 0);
        task.setSuccessCount(total != null ? total.intValue() : 0);
        task.setTaskId(taskId);
        task.setFileName(fileName);
        task.setChannelType(channelType);
        task.setStatus(ImportTask.ImportStatus.COMPLETED.name());
        task.setProductParam(jsonString);
        importTaskMapper.insert(task);
        return taskId;
    }

    /**
     * 处理配方商品
     */
    private void schoolProgramProcessRecipeCommodity(MaterialDataVo materialDataVo, Long storeId,
                                        List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap,List<String> messageList,List<String> codeMessageList,List<String> categoryMessageList,List<String> categoryNameMessageList) {
        //获取当前Sku下配方列表
        List<RecipeDO> recipes = findRecipesByCommodity(materialDataVo);

        if (ObjectUtil.isEmpty(recipes)) {
            Long skuId = materialDataVo.getSkuId();
            if(ObjectUtil.isNotEmpty(skuId)){
                CommoditySkus commoditySkus = commoditySkusMapper.selectById(skuId);
                if(commoditySkus!=null){
                    CommoditySpus commoditySpus = commoditySpusMapper.selectById(commoditySkus.getCommodityId());
                    if(commoditySpus!=null){
                        String message = commoditySpus.getCommodityName();
                        messageList.add(message);
                    }
                }
            }
        }else {
            for (RecipeDO recipe : recipes) {
                if (recipe.getIsAlternative().equals(0L)) {
                    schoolProgramModifyProcessNonAlternativeRecipe(recipe, materialDataVo, storeId, resultList,codeMessageList);
                } else if (recipe.getIsAlternative().equals(1L)) {
                    //可替换原材料逻辑
                    schoolProgramProcessAlternativeRecipe(recipe, materialDataVo, storeId, resultList,repertoryList,repertoryMap,categoryMessageList,categoryNameMessageList);
                }
            }
        }

    }


    /**
     * 验证材料数据
     */
    private void validateMaterialData(MaterialDataVo materialDataVo) {
        if (ObjectUtil.isEmpty(materialDataVo.getCommodityType())) {
            throw exception(1_003_011_005,"数据类型不能为空");
        }
    }

    /**
     * 处理配方商品
     */
    private void processRecipeCommodity(MaterialDataVo materialDataVo, Long storeId,
                                        List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //获取当前Sku下配方列表
        List<RecipeDO> recipes = findRecipesByCommodity(materialDataVo);

        if (ObjectUtil.isEmpty(recipes)) {
            //异步生成日志
            asyncData(null,storeId,materialDataVo,RawMaterialLog.RECIPE_NOT_FOUND.getType(),1);
            Long skuId = materialDataVo.getSkuId();
            if(ObjectUtil.isNotEmpty(skuId)){
                CommoditySkus commoditySkus = commoditySkusMapper.selectById(skuId);
                if(commoditySkus!=null){
                    CommoditySpus commoditySpus = commoditySpusMapper.selectById(commoditySkus.getCommodityId());
                    if(commoditySpus!=null){
                        throw exception(1_003_007_015,"商品名称"+commoditySpus.getCommodityName()+"没有配置对应配方");
                    }

                }


            }
            throw exception(1_003_007_015,"没有配置配方");

//            return;
        }
        for (RecipeDO recipe : recipes) {
            if (recipe.getIsAlternative().equals(0L)) {
                processNonAlternativeRecipe(recipe, materialDataVo, storeId, resultList);
            } else if (recipe.getIsAlternative().equals(1L)) {
                //可替换原材料逻辑
                processAlternativeRecipe(recipe, materialDataVo, storeId, resultList,repertoryList,repertoryMap);
            }
        }
    }

    /**
     * 查询配方
     */
    private List<RecipeDO> findRecipesByCommodity(MaterialDataVo materialDataVo) {
        LambdaQueryWrapperX<RecipeDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(RecipeDO::getCommodityId, materialDataVo.getCommodityId());
        wrapper.eqIfPresent(RecipeDO::getSkuId, materialDataVo.getSkuId());
        wrapper.isNotNull(RecipeDO::getConsumption);
        return commodityRecipeMapper.selectList(wrapper);
    }

    private void schoolProgramModifyProcessNonAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                             Long storeId, List<MaterialListRespVo> resultList,List<String> codeMessageList) {
        CommodityWhetherVO commodityWhetherVO = schoolProgramCalculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(),materialDataVo.getSkuId(), recipe);

        RawMaterial rawMaterial = findRawMaterialByCode(recipe.getMaterialsCode(), storeId);
//        if (rawMaterial == null) {
//            String codeMessage = recipe.getMaterialsCode();
//            codeMessageList.add(codeMessage);
//
//        }else {
//            MaterialListRespVo responseVo = schoolProgramCreateMaterialResponseFromRecipe(rawMaterial,recipe,commodityWhetherVO.getTotalNumber(),materialDataVo,storeId);
//            resultList.add(responseVo);
//        }
        MaterialListRespVo responseVo = schoolProgramCreateMaterialResponseFromRecipe(rawMaterial,recipe,commodityWhetherVO.getTotalNumber(),materialDataVo,storeId);
        resultList.add(responseVo);

    }



    /**
     * 处理非替代配方
     */
    private void processNonAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                             Long storeId, List<MaterialListRespVo> resultList) {
        CommodityWhetherVO commodityWhetherVO = calculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(),materialDataVo.getSkuId(), recipe);

        RawMaterial rawMaterial = findRawMaterialByCode(recipe.getMaterialsCode(), storeId);
        if (rawMaterial == null) {
            //异步生成日志
            asyncData(recipe.getMaterialsCode(), storeId, materialDataVo,RawMaterialLog.STORE_MATERIAL_NOT_FOUND.getType(),1);
//            throw exception(1_003_011_001, "门店下商品编码为" + recipe.getMaterialsCode() + "不存在");
        }
        MaterialListRespVo responseVo = createMaterialResponseFromRecipe(rawMaterial,recipe,commodityWhetherVO.getTotalNumber(),materialDataVo,storeId);
        resultList.add(responseVo);
    }

    private void asyncData(String materialCode, Long storeId, MaterialDataVo materialDataVo,Integer type,Integer status) {
        RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
        rawMaterialLogDO.setMaterialCode(materialCode);
        rawMaterialLogDO.setLogType(type);
        rawMaterialLogDO.setStoreId(storeId);
        rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
        rawMaterialLogDO.setStatus(status);
        rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
    }

    /**
     * 处理替代配方
     */
    private void schoolProgramProcessAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                          Long storeId, List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap,List<String> categoryMessageList,List<String> categoryNameMessageList) {
        //计算消耗量与数量乘积
        CommodityWhetherVO commodityWhetherVO = schoolProgramCalculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(),materialDataVo.getSkuId(), recipe);
        //按照时间升序  来获取可替换原材料列表
        List<RawMaterial> alternativeMaterials = findAlternativeMaterials(recipe, storeId);

//        if (ObjectUtil.isEmpty(alternativeMaterials)) {
//
//            String caString = recipe.getMaterialsTypeName();
//            categoryMessageList.add(caString);
//
//            Long skuId = materialDataVo.getSkuId();
//            if(ObjectUtil.isNotEmpty(skuId)){
//                CommoditySkus commoditySkus = commoditySkusMapper.selectById(skuId);
//                if(commoditySkus!=null){
//                    CommoditySpus commoditySpus = commoditySpusMapper.selectById(commoditySkus.getCommodityId());
//                    if(commoditySpus!=null){
//                        String message = commoditySpus.getCommodityName();
//                        categoryNameMessageList.add(message);
//                    }
//                }
//            }
//
//        }
        //整合物料跟单位
//        integrationUnit(commodityWhetherVO,alternativeMaterials);


        schoolProgramDistributeConsumptionAmongAlternatives(recipe, commodityWhetherVO, alternativeMaterials, resultList,storeId,materialDataVo,repertoryList,repertoryMap);
    }

    /**
     * 处理替代配方
     */
    private void productQuantityProcessAlternativeRecipe(TriRecipeDO recipe, ProductQuantity productQuantity,
                                                       Long storeId, List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //计算消耗量与数量乘积
        CommodityWhetherVO commodityWhetherVO = productQuantityCalculateConsumptionTwo(recipe.getConsumption(),productQuantity,recipe,storeId);
        //按照时间升序  来获取可替换原材料列表
        List<RawMaterial> alternativeMaterials = productQuantityFindAlternativeMaterials(recipe, storeId);

//        if (ObjectUtil.isEmpty(alternativeMaterials)) {
//            ImportLogRecord importLogRecord = new ImportLogRecord();
//            importLogRecord.setLogType(RawMaterialLog.CATEGORY_NO_MATERIAL.getType());
//            importLogRecord.setCommodityName(productQuantity.getProductName());
//            importLogRecord.setQuantity(productQuantity.getQuantity());
//            importLogRecord.setStoreId(storeId);
//            importLogRecordMapper.insert(importLogRecord);
//        }


        productQuantityDistributeConsumptionAmongAlternatives(recipe, commodityWhetherVO, alternativeMaterials, resultList,storeId,productQuantity,repertoryList,repertoryMap);
    }



    /**
     * 处理替代配方
     */
    private void processAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                          Long storeId, List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //计算消耗量与数量乘积
        CommodityWhetherVO commodityWhetherVO = calculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(),materialDataVo.getSkuId(), recipe);
        //按照时间升序  来获取可替换原材料列表
        List<RawMaterial> alternativeMaterials = findAlternativeMaterials(recipe, storeId);

        if (ObjectUtil.isEmpty(alternativeMaterials)) {
            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
            rawMaterialLogDO.setCategoryName(recipe.getMaterialsTypeName());
            rawMaterialLogDO.setLogType(RawMaterialLog.CATEGORY_NO_MATERIAL.getType());
            rawMaterialLogDO.setStoreId(storeId);
            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
            rawMaterialLogDO.setStatus(1);
            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);


//            throw exception(1_003_011_003, "当前门店的二级类目下的没有可符合的原材料");
        }
        //整合物料跟单位
//        integrationUnit(commodityWhetherVO,alternativeMaterials);


        distributeConsumptionAmongAlternatives(recipe, commodityWhetherVO, alternativeMaterials, resultList,storeId,materialDataVo,repertoryList,repertoryMap);
    }


    /**
     * 查找替代原材料
     */
    private List<RawMaterial> productQuantityFindAlternativeMaterials(TriRecipeDO recipe, Long storeId) {
        List<String> excludedMaterialCodes = productQuantityGetExcludedMaterialCodes(recipe.getRecipeId());

        LambdaQueryWrapperX<RawMaterial> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(RawMaterial::getStoreId, storeId);
        wrapper.eq(RawMaterial::getStasticsCommodityName, recipe.getMaterialsTypeName());

        if (ObjectUtil.isNotEmpty(excludedMaterialCodes)) {
            wrapper.notIn(RawMaterial::getCommodityCode, excludedMaterialCodes);
        }

        List<RawMaterial> materials = rawMaterialMapper.selectList(wrapper);
        sortByCreateTimeAscending(materials);

        return materials;
    }

    /**
     * 查找替代原材料
     */
    private List<RawMaterial> findAlternativeMaterials(RecipeDO recipe, Long storeId) {
        List<String> excludedMaterialCodes = getExcludedMaterialCodes(recipe.getRecipeId());

        LambdaQueryWrapperX<RawMaterial> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(RawMaterial::getStoreId, storeId);
        wrapper.eq(RawMaterial::getStasticsCommodityName, recipe.getMaterialsTypeName());

        if (ObjectUtil.isNotEmpty(excludedMaterialCodes)) {
            wrapper.notIn(RawMaterial::getCommodityCode, excludedMaterialCodes);
        }

        List<RawMaterial> materials = rawMaterialMapper.selectList(wrapper);
        sortByCreateTimeAscending(materials);

        return materials;
    }

    /**
     * 获取需要排除的材料编码
     */
    private List<String> productQuantityGetExcludedMaterialCodes(Long recipeId) {
        LambdaQueryWrapperX<TriRecipeMaterialDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(TriRecipeMaterialDO::getRecipeId, recipeId);
        List<TriRecipeMaterialDO> recipeMaterials = triCommodityRecipeMaterialMapper.selectList(wrapper);

        if (ObjectUtil.isEmpty(recipeMaterials)) {
            return Collections.emptyList();
        }

        return recipeMaterials.stream()
                .map(TriRecipeMaterialDO::getMaterialsCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取需要排除的材料编码
     */
    private List<String> getExcludedMaterialCodes(Long recipeId) {
        LambdaQueryWrapperX<RecipeMaterialDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(RecipeMaterialDO::getRecipeId, recipeId);
        List<RecipeMaterialDO> recipeMaterials = commodityRecipeMaterialMapper.selectList(wrapper);

        if (ObjectUtil.isEmpty(recipeMaterials)) {
            return Collections.emptyList();
        }

        return recipeMaterials.stream()
                .map(RecipeMaterialDO::getMaterialsCode)
                .collect(Collectors.toList());
    }


    /**
     * 在替代材料之间分配消耗量
     */
    private void productQuantityDistributeConsumptionAmongAlternatives(TriRecipeDO recipe, CommodityWhetherVO commodityWhetherVO,
                                                                     List<RawMaterial> alternatives,
                                                                     List<MaterialListRespVo> resultList,Long storeId,ProductQuantity productQuantity,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //弃用
        BigDecimal remainingConsumption = commodityWhetherVO.getTotalNumber();


        if(ObjectUtil.isNotEmpty(alternatives)){
            //为解决单一配方中 可能有多个原材料一样的  防止扣减库存重复
            for (RawMaterial material : alternatives) {
//            remainingConsumption = getQuantitys(material,recipe,materialDataVo.getQuantity());
                BigDecimal stock = material.getStock();

                if(ObjectUtil.isNotEmpty(repertoryList)){
                    for (RawMaterialIdVO rawMaterialIdVO : repertoryList) {
                        if(rawMaterialIdVO.getRawMaterialId().equals(material.getId())){

                            BigDecimal bigDecimal = repertoryMap.get(material.getId());
                            if(ObjectUtil.isEmpty(bigDecimal)){
                                bigDecimal = repertoryMap.getOrDefault(material.getId(), BigDecimal.ZERO);
                            }
                            stock = stock.subtract(bigDecimal);
                        }

                    }
                }

                if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                if(ObjectUtil.isNotEmpty(repertoryMap.get(material.getId()))){
                    stock = stock.subtract(repertoryMap.get(material.getId()));
                }
                if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal remainingNumber = calculateConsumptionForMaterial(remainingConsumption, stock);
                if (!remainingNumber.equals(BigDecimal.ZERO)) {
                    RawMaterialIdVO rawMaterialIdVO = new RawMaterialIdVO();
                    rawMaterialIdVO.setRawMaterialId(material.getId());
                    rawMaterialIdVO.setInventory(remainingNumber);
//                rawMaterialIdVOList.add(rawMaterialIdVO);
                    repertoryList.add(rawMaterialIdVO);
                    repertoryMap.put(material.getId(),remainingNumber);
                }

                MaterialListRespVo responseVo = productQuantityCreateMaterialResponseFromRecipe(material, recipe,remainingNumber,productQuantity,storeId);
                resultList.add(responseVo);

                remainingConsumption = remainingConsumption.subtract(remainingNumber);
            }

            // 如果还有剩余消耗量，使用第一个替代材料
            if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
                RawMaterial firstMaterial = alternatives.get(0);
                MaterialListRespVo responseVo = productQuantityCreateMaterialResponseFromRecipe(firstMaterial, recipe,remainingConsumption,productQuantity,storeId);
                resultList.add(responseVo);
            }
        }else{
            MaterialListRespVo response = new MaterialListRespVo();
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
//            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            BigDecimal qua = new BigDecimal(productQuantity.getQuantity());
            response.setQuantity(consumptionMultiple.multiply(qua));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);

                    response.setCalUnitList(conversionList);

                    if(scmCommodity.getOutUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                        response.setUsedUnit(recipe.getUsedUnit());
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(scmCommodity.getOutUnit().equals(unitConversion.getBeforeUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                        response.setUsedUnit(scmCommodity.getMinUnit());
//                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice).(4, RoundingMode.HALF_UP);
                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice, 4, RoundingMode.HALF_UP);
                        response.setUnitPrice(bigDecimal);
                    }




                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }
            resultList.add(response);
        }
    }









    /**
     * 在替代材料之间分配消耗量
     */
    private void schoolProgramDistributeConsumptionAmongAlternatives(RecipeDO recipe, CommodityWhetherVO commodityWhetherVO,
                                                        List<RawMaterial> alternatives,
                                                        List<MaterialListRespVo> resultList,Long storeId,MaterialDataVo materialDataVo,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //弃用
        BigDecimal remainingConsumption = commodityWhetherVO.getTotalNumber();


        if(ObjectUtil.isNotEmpty(alternatives)){
            //为解决单一配方中 可能有多个原材料一样的  防止扣减库存重复
            for (RawMaterial material : alternatives) {
//            remainingConsumption = getQuantitys(material,recipe,materialDataVo.getQuantity());
                BigDecimal stock = material.getStock();

                if(ObjectUtil.isNotEmpty(repertoryList)){
                    for (RawMaterialIdVO rawMaterialIdVO : repertoryList) {
                        if(rawMaterialIdVO.getRawMaterialId().equals(material.getId())){

                            BigDecimal bigDecimal = repertoryMap.get(material.getId());
                            if(ObjectUtil.isEmpty(bigDecimal)){
                                bigDecimal = repertoryMap.getOrDefault(material.getId(), BigDecimal.ZERO);
                            }
                            stock = stock.subtract(bigDecimal);
                        }

                    }
                }

                if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                if(ObjectUtil.isNotEmpty(repertoryMap.get(material.getId()))){
                    stock = stock.subtract(repertoryMap.get(material.getId()));
                }
                if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal remainingNumber = calculateConsumptionForMaterial(remainingConsumption, stock);
                if (!remainingNumber.equals(BigDecimal.ZERO)) {
                    RawMaterialIdVO rawMaterialIdVO = new RawMaterialIdVO();
                    rawMaterialIdVO.setRawMaterialId(material.getId());
                    rawMaterialIdVO.setInventory(remainingNumber);
//                rawMaterialIdVOList.add(rawMaterialIdVO);
                    repertoryList.add(rawMaterialIdVO);
                    repertoryMap.put(material.getId(),remainingNumber);
                }

                MaterialListRespVo responseVo = schoolProgramCreateMaterialResponseFromRecipe(material, recipe,remainingNumber,materialDataVo,storeId);
                resultList.add(responseVo);

                remainingConsumption = remainingConsumption.subtract(remainingNumber);
            }

            // 如果还有剩余消耗量，使用第一个替代材料
            if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
                RawMaterial firstMaterial = alternatives.get(0);
                MaterialListRespVo responseVo = schoolProgramCreateMaterialResponseFromRecipe(firstMaterial, recipe,remainingConsumption,materialDataVo,storeId);
                resultList.add(responseVo);
            }
        }else{
            MaterialListRespVo response = new MaterialListRespVo();
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
//            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            response.setQuantity(consumptionMultiple.multiply(materialDataVo.getQuantity()));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);

                    response.setCalUnitList(conversionList);

                    if(scmCommodity.getOutUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                        response.setUsedUnit(recipe.getUsedUnit());
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(scmCommodity.getOutUnit().equals(unitConversion.getBeforeUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                        response.setUsedUnit(scmCommodity.getMinUnit());
//                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice).(4, RoundingMode.HALF_UP);
                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice, 4, RoundingMode.HALF_UP);
                        response.setUnitPrice(bigDecimal);
                    }




                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }
            resultList.add(response);
        }
    }


    /**
     * 在替代材料之间分配消耗量
     */
    private void distributeConsumptionAmongAlternatives(RecipeDO recipe, CommodityWhetherVO commodityWhetherVO,
                                                        List<RawMaterial> alternatives,
                                                        List<MaterialListRespVo> resultList,Long storeId,MaterialDataVo materialDataVo,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap) {
        //弃用
        BigDecimal remainingConsumption = commodityWhetherVO.getTotalNumber();


        if(ObjectUtil.isNotEmpty(alternatives)){
            //为解决单一配方中 可能有多个原材料一样的  防止扣减库存重复
            for (RawMaterial material : alternatives) {
//            remainingConsumption = getQuantitys(material,recipe,materialDataVo.getQuantity());
                BigDecimal stock = material.getStock();

                if(ObjectUtil.isNotEmpty(repertoryList)){
                    for (RawMaterialIdVO rawMaterialIdVO : repertoryList) {
                        if(rawMaterialIdVO.getRawMaterialId().equals(material.getId())){

                            BigDecimal bigDecimal = repertoryMap.get(material.getId());
                            if(ObjectUtil.isEmpty(bigDecimal)){
                                bigDecimal = repertoryMap.getOrDefault(material.getId(), BigDecimal.ZERO);
                            }
                            stock = stock.subtract(bigDecimal);
                        }

                    }
                }

                if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                if(ObjectUtil.isNotEmpty(repertoryMap.get(material.getId()))){
                    stock = stock.subtract(repertoryMap.get(material.getId()));
                }
                if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal remainingNumber = calculateConsumptionForMaterial(remainingConsumption, stock);
                if (!remainingNumber.equals(BigDecimal.ZERO)) {
                    RawMaterialIdVO rawMaterialIdVO = new RawMaterialIdVO();
                    rawMaterialIdVO.setRawMaterialId(material.getId());
                    rawMaterialIdVO.setInventory(remainingNumber);
//                rawMaterialIdVOList.add(rawMaterialIdVO);
                    repertoryList.add(rawMaterialIdVO);
                    repertoryMap.put(material.getId(),remainingNumber);
                }

                MaterialListRespVo responseVo = createMaterialResponseFromRecipe(material, recipe,remainingNumber,materialDataVo,storeId);
                resultList.add(responseVo);

                remainingConsumption = remainingConsumption.subtract(remainingNumber);
            }

            // 如果还有剩余消耗量，使用第一个替代材料
            if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
                RawMaterial firstMaterial = alternatives.get(0);
                MaterialListRespVo responseVo = createMaterialResponseFromRecipe(firstMaterial, recipe,remainingConsumption,materialDataVo,storeId);
                resultList.add(responseVo);
            }
        }else{
            MaterialListRespVo response = new MaterialListRespVo();
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
//            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            response.setQuantity(consumptionMultiple.multiply(materialDataVo.getQuantity()));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);

                    response.setCalUnitList(conversionList);

                    if(scmCommodity.getOutUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                        response.setUsedUnit(recipe.getUsedUnit());
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(scmCommodity.getOutUnit().equals(unitConversion.getBeforeUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                        response.setUsedUnit(scmCommodity.getMinUnit());
//                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice).(4, RoundingMode.HALF_UP);
                        BigDecimal bigDecimal = outPrice.divide(TotalUnitPrice, 4, RoundingMode.HALF_UP);
                        response.setUnitPrice(bigDecimal);
                    }




                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }
            resultList.add(response);
        }
    }


    private BigDecimal getQuantitys(RawMaterial material, RecipeDO recipe, BigDecimal quantity) {
        BigDecimal consumption = new BigDecimal(recipe.getConsumption());
        if(recipe.getUsedUnit().equals(material.getMinUnit())){

            return consumption.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal multiple = new BigDecimal("1");
        //代表这个原料的数量
        String specificationsRules = material.getSpecificationsRules();
        if (ObjectUtil.isNotEmpty(material.getSpecificationsRules())&& !material.getSpecificationsRules().equals("[]")) {
            try {
                ObjectMapper objectMapper = createConfiguredObjectMapper();
                List<CommodityConversion> conversions = objectMapper.readValue(
                        material.getSpecificationsRules(),
                        new TypeReference<List<CommodityConversion>>() {}
                );
                for (CommodityConversion conversion : conversions) {
                    if(conversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                        multiple = conversion.getAfterNumber();
                    }
                }
                BigDecimal multiply = consumption.multiply(quantity);

                return multiply.multiply(multiple).setScale(4, RoundingMode.HALF_UP);

            } catch (Exception e) {
                throw exception(1_003_011_009, "数据有问题，单位换算列表不存在");
            }
        }else {
            throw exception(1_003_011_009, "数据有问题，单位换算列表不存在");
        }

    }

    /**
     * 确定使用的单位
     */
    private String determineUsedUnit(RawMaterial material, String recipeUsedUnit) {
        if (ObjectUtil.isEmpty(recipeUsedUnit)) {
            return null;
        }

        Set<String> availableUnits = getAvailableUnits(material);
        return availableUnits.contains(recipeUsedUnit) ? recipeUsedUnit : null;
    }

    /**
     * 获取可用单位集合
     */
    private Set<String> getAvailableUnits(RawMaterial material) {
        Set<String> units = new HashSet<>();

        if (ObjectUtil.isNotEmpty(material.getSpecificationsRules())&& !material.getSpecificationsRules().equals("[]")) {
            try {
                ObjectMapper objectMapper = createConfiguredObjectMapper();
                List<CommodityConversion> conversions = objectMapper.readValue(
                        material.getSpecificationsRules(),
                        new TypeReference<List<CommodityConversion>>() {}
                );

                conversions.stream()
                        .map(CommodityConversion::getBeforeUnit)
                        .forEach(units::add);
            } catch (Exception e) {
//                log.warn("解析规格规则失败，materialId: {}", material.getId(), e);
            }
        }

        units.add(material.getMinUnit());
        return units;
    }

    /**
     * 处理原材料商品
     */
    private void processRawMaterialCommodity(MaterialDataVo materialDataVo, Long storeId,
                                             List<MaterialListRespVo> resultList) {
        if (ObjectUtil.isEmpty(materialDataVo.getMaterialId())) {
            return;
        }

        RawMaterial rawMaterial = findRawMaterialByCode(materialDataVo.getMaterialId(), storeId);
        if (rawMaterial == null) {
//            //异步生成日志
////            asyncData(materialDataVo.getMaterialId(), storeId, materialDataVo,RawMaterialLog.STORE_MATERIAL_NOT_FOUND.getType());
            throw exception(1_003_011_001, "门店下商品编码为" + materialDataVo.getMaterialId() + "不存在");
        }

        MaterialListRespVo responseVo = createMaterialResponseFromRawMaterial(rawMaterial);
        responseVo.setQuantity(materialDataVo.getQuantity());
//        responseVo.setUsedUnit(rawMaterial.getMinUnit());

        resultList.add(responseVo);
    }

    /**
     * 根据商品编码查找原材料
     */
    private RawMaterial findRawMaterialByCode(String materialCode, Long storeId) {
        LambdaQueryWrapperX<RawMaterial> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(RawMaterial::getCommodityCode, materialCode);
        wrapper.eq(RawMaterial::getStoreId, storeId);

        List<RawMaterial> materials = rawMaterialMapper.selectList(wrapper);
        return ObjectUtil.isNotEmpty(materials) ? materials.get(0) : null;
    }

    /**
     * 从配方创建材料响应对象
     */
    private MaterialListRespVo createMaterialResponseFromRecipe(RawMaterial rawMaterial, RecipeDO recipe,CommodityWhetherVO commodityWhetherVO) {
        List<ScmUnitConversion> scmUnitConversions = commodityWhetherVO.getScmUnitConversions();
        List<CommodityConversion> conversionList = new ArrayList<>();
        for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
            CommodityConversion commodityConversion = new CommodityConversion();
            BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
            commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
            conversionList.add(commodityConversion);
        }
        MaterialListRespVo response = new MaterialListRespVo();
        response.setRawMaterialId(rawMaterial.getId());
        response.setMaterialsId(rawMaterial.getCommodityCode());
        response.setMaterialsName(rawMaterial.getCommodityName());
        if(commodityWhetherVO.getIsUnit()){
            response.setUnitPrice(rawMaterial.getUnitPrice());
        }else{
            BigDecimal conversionFactor = new BigDecimal(1);
            for (CommodityConversion commodityConversion : conversionList) {
                if (commodityConversion.getBeforeUnit().equals(recipe.getUsedUnit())) {
                    conversionFactor = commodityConversion.getAfterNumber();
                }

            }
            BigDecimal multipliedPrice = rawMaterial.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
            response.setUnitPrice(multipliedPrice);

        }

        return response;
    }


    /**
     * 从配方创建材料响应对象
     */
    private MaterialListRespVo schoolProgramCreateMaterialResponseFromRecipe(RawMaterial rawMaterial, RecipeDO recipe,BigDecimal remainingNumber,MaterialDataVo materialDataVo,Long storeId) {
        MaterialListRespVo response = new MaterialListRespVo();
        if(rawMaterial == null){
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);


            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            response.setQuantity(consumptionMultiple.multiply(materialDataVo.getQuantity()));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);
                    response.setUsedUnit(recipe.getUsedUnit());
                    response.setCalUnitList(conversionList);

                    if(scmUnitConversion.getAfterUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                    }
                    BigDecimal bigDecimal = TotalUnitPrice.multiply(outPrice).setScale(4, RoundingMode.HALF_UP);
                    response.setUnitPrice(bigDecimal);



                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }

            return response;
        }else{
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setRawMaterialId(rawMaterial.getId());
            response.setMaterialsId(rawMaterial.getCommodityCode());
            response.setMaterialsName(rawMaterial.getCommodityName());
            if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                try {
                    ObjectMapper objectMapper = createConfiguredObjectMapper();
                    List<CommodityConversion> conversions = objectMapper.readValue(
                            rawMaterial.getSpecificationsRules(),
                            new TypeReference<List<CommodityConversion>>() {}
                    );
                    response.setCalUnitList(conversions);
                    Set<String> unitSet = new HashSet<>();
                    if (ObjectUtil.isNotEmpty(conversions)) {
                        conversions.stream()
                                .map(CommodityConversion::getBeforeUnit)
                                .forEach(unitSet::add);
                    }
                    unitSet.add(conversions.get(0).getAfterUnit());

                    response.setUnitList(new ArrayList<>(unitSet));
                    if (recipe.getUsedUnit().equals(conversions.get(0).getAfterUnit())) {
                        response.setUsedUnit(recipe.getUsedUnit());
                        response.setUnitPrice(rawMaterial.getUnitPrice());
                        response.setQuantity(remainingNumber);
                    }else{
                        Boolean flag = false;
                        BigDecimal conversionFactor = new BigDecimal(1);
                        for (CommodityConversion commodityConversion : conversions) {
                            if (commodityConversion.getBeforeUnit().equals(recipe.getUsedUnit())) {
                                conversionFactor = commodityConversion.getAfterNumber();
                                flag = true;
                            }

                        }
                        //说明没有匹配到单位对应的单位  那就都按最小单位来返回
                        if (!flag) {
//                            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
//                            rawMaterialLogDO.setCategoryName(recipe.getMaterialsTypeName());
//                            rawMaterialLogDO.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
//                            rawMaterialLogDO.setStoreId(storeId);
//                            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
//                            rawMaterialLogDO.setUnitName(recipe.getUsedUnit());
//                            rawMaterialLogDO.setStatus(1);
//                            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
//                            response.setUsedUnit(conversions.get(0).getAfterUnit());
//                            response.setUnitPrice(rawMaterial.getUnitPrice());
//                            response.setQuantity(remainingNumber);
                            throw exception(1_003_011_022, "最小单位匹配不上");
                        }else{
                            BigDecimal multipliedPrice = rawMaterial.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
                            BigDecimal number = remainingNumber.divide(conversionFactor, 4, RoundingMode.HALF_UP);
                            response.setUnitPrice(multipliedPrice);
                            response.setQuantity(number);
                            response.setUsedUnit(recipe.getUsedUnit());
                        }
                    }

                } catch (Exception e) {
                    throw exception(1_003_011_009, "数据有问题，单位换算列表不存在");
                }
            }else{
                response.setCalUnitList(new ArrayList<>());
                List<String> unitList = new ArrayList<>();
                unitList.add(rawMaterial.getMinUnit());
                response.setUnitList(unitList);
                response.setUsedUnit(rawMaterial.getMinUnit());
                response.setUnitPrice(rawMaterial.getUnitPrice());
                response.setQuantity(remainingNumber);
                return response;
            }
        }
        return response;
    }



    /**
     * 从配方创建材料响应对象
     */
    private MaterialListRespVo productQuantityCreateMaterialResponseFromRecipe(RawMaterial rawMaterial, TriRecipeDO recipe,BigDecimal remainingNumber,ProductQuantity productQuantity,Long storeId) {
        MaterialListRespVo response = new MaterialListRespVo();
        if(rawMaterial == null){
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);


            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            BigDecimal qua = new BigDecimal(productQuantity.getQuantity());
            response.setQuantity(consumptionMultiple.multiply(qua));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);
                    response.setUsedUnit(recipe.getUsedUnit());
                    response.setCalUnitList(conversionList);

                    if(scmUnitConversion.getAfterUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                    }
                    BigDecimal bigDecimal = TotalUnitPrice.multiply(outPrice).setScale(4, RoundingMode.HALF_UP);
                    response.setUnitPrice(bigDecimal);



                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }

            return response;
        }else{
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setRawMaterialId(rawMaterial.getId());
            response.setMaterialsId(rawMaterial.getCommodityCode());
            response.setMaterialsName(rawMaterial.getCommodityName());
            if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                try {
                    ObjectMapper objectMapper = createConfiguredObjectMapper();
                    List<CommodityConversion> conversions = objectMapper.readValue(
                            rawMaterial.getSpecificationsRules(),
                            new TypeReference<List<CommodityConversion>>() {}
                    );
                    response.setCalUnitList(conversions);
                    Set<String> unitSet = new HashSet<>();
                    if (ObjectUtil.isNotEmpty(conversions)) {
                        conversions.stream()
                                .map(CommodityConversion::getBeforeUnit)
                                .forEach(unitSet::add);
                    }
                    unitSet.add(conversions.get(0).getAfterUnit());

                    response.setUnitList(new ArrayList<>(unitSet));
                    if (recipe.getUsedUnit().equals(conversions.get(0).getAfterUnit())) {
                        response.setUsedUnit(recipe.getUsedUnit());
                        response.setUnitPrice(rawMaterial.getUnitPrice());
                        response.setQuantity(remainingNumber);
                    }else{
                        Boolean flag = false;
                        BigDecimal conversionFactor = new BigDecimal(1);
                        for (CommodityConversion commodityConversion : conversions) {
                            if (commodityConversion.getBeforeUnit().equals(recipe.getUsedUnit())) {
                                conversionFactor = commodityConversion.getAfterNumber();
                                flag = true;
                            }

                        }
                        //说明没有匹配到单位对应的单位  那就都按最小单位来返回
                        if (!flag) {
                            ImportLogRecord importLogRecord = new ImportLogRecord();
                            importLogRecord.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
                            importLogRecord.setCommodityName(productQuantity.getProductName());
                            importLogRecord.setQuantity(productQuantity.getQuantity());
                            importLogRecord.setStoreId(storeId);
                            importLogRecordMapper.insert(importLogRecord);

                        }else{
                            BigDecimal multipliedPrice = rawMaterial.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
                            BigDecimal number = remainingNumber.divide(conversionFactor, 4, RoundingMode.HALF_UP);
                            response.setUnitPrice(multipliedPrice);
                            response.setQuantity(number);
                            response.setUsedUnit(recipe.getUsedUnit());
                        }
                    }

                } catch (Exception e) {
                    ImportLogRecord importLogRecord = new ImportLogRecord();
                    importLogRecord.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
                    importLogRecord.setCommodityName(productQuantity.getProductName());
                    importLogRecord.setQuantity(productQuantity.getQuantity());
                    importLogRecord.setStoreId(storeId);
                    importLogRecordMapper.insert(importLogRecord);
                }
            }else{
                response.setCalUnitList(new ArrayList<>());
                List<String> unitList = new ArrayList<>();
                unitList.add(rawMaterial.getMinUnit());
                response.setUnitList(unitList);
                response.setUsedUnit(rawMaterial.getMinUnit());
                response.setUnitPrice(rawMaterial.getUnitPrice());
                response.setQuantity(remainingNumber);
                return response;
            }
        }
        return response;
    }




    /**
     * 从配方创建材料响应对象
     */
    private MaterialListRespVo createMaterialResponseFromRecipe(RawMaterial rawMaterial, RecipeDO recipe,BigDecimal remainingNumber,MaterialDataVo materialDataVo,Long storeId) {
        MaterialListRespVo response = new MaterialListRespVo();
        if(rawMaterial == null){
            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            commodityLambdaQueryWrapperX.eqIfPresent(ScmCommodity::getId,recipe.getMaterialsId());
            ScmCommodity scmCommodity = scmCommodityMapper.selectOne(commodityLambdaQueryWrapperX);
            LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,recipe.getMaterialsId());
            scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getIsDelete,0);
            List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);


            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setMaterialsId(recipe.getMaterialsCode());
            response.setMaterialsName(recipe.getMaterialsName());
            BigDecimal consumptionMultiple = new BigDecimal(recipe.getConsumption());
            response.setQuantity(consumptionMultiple.multiply(materialDataVo.getQuantity()));
            BigDecimal TotalUnitPrice = new BigDecimal(1);
            if(scmCommodity!=null){
                BigDecimal outPrice = scmCommodity.getOutPrice();
                if(ObjectUtil.isNotEmpty(scmUnitConversions)){
                    List<String> stringList = new ArrayList<>();
                    List<CommodityConversion> conversionList = new ArrayList<>();
                    for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                        CommodityConversion commodityConversion = new CommodityConversion();
                        BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                        commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                        conversionList.add(commodityConversion);
                        stringList.add(scmUnitConversion.getBeforeUnit());
                    }
                    ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
                    stringList.add(scmUnitConversion.getAfterUnit());
                    response.setUnitList(stringList);
                    response.setUsedUnit(recipe.getUsedUnit());
                    response.setCalUnitList(conversionList);

                    if(scmUnitConversion.getAfterUnit().equals(recipe.getUsedUnit())){
                        response.setUnitPrice(outPrice);
                    }else{
                        for (ScmUnitConversion unitConversion : scmUnitConversions) {
                            if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                                BigDecimal bigDecimal = new BigDecimal(unitConversion.getAfterNumber());
                                TotalUnitPrice = bigDecimal;
                            }

                        }
                    }
                    BigDecimal bigDecimal = TotalUnitPrice.multiply(outPrice).setScale(4, RoundingMode.HALF_UP);
                    response.setUnitPrice(bigDecimal);



                }else {
                    response.setUnitPrice(outPrice);
                    response.setUsedUnit(recipe.getUsedUnit());
                    List<String> stringList = new ArrayList<>();
                    stringList.add(recipe.getUsedUnit());
                    response.setUnitList(stringList);
                    response.setCalUnitList(new ArrayList<>());
                }

            }

            return response;
        }else{
            response.setStasticsName(recipe.getMaterialsTypeName());
            response.setRawMaterialId(rawMaterial.getId());
            response.setMaterialsId(rawMaterial.getCommodityCode());
            response.setMaterialsName(rawMaterial.getCommodityName());
            if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
                try {
                    ObjectMapper objectMapper = createConfiguredObjectMapper();
                    List<CommodityConversion> conversions = objectMapper.readValue(
                            rawMaterial.getSpecificationsRules(),
                            new TypeReference<List<CommodityConversion>>() {}
                    );
                    response.setCalUnitList(conversions);
                    Set<String> unitSet = new HashSet<>();
                    if (ObjectUtil.isNotEmpty(conversions)) {
                        conversions.stream()
                                .map(CommodityConversion::getBeforeUnit)
                                .forEach(unitSet::add);
                    }
                    unitSet.add(conversions.get(0).getAfterUnit());

                    response.setUnitList(new ArrayList<>(unitSet));
                    if (recipe.getUsedUnit().equals(conversions.get(0).getAfterUnit())) {
                        response.setUsedUnit(recipe.getUsedUnit());
                        response.setUnitPrice(rawMaterial.getUnitPrice());
                        response.setQuantity(remainingNumber);
                    }else{
                        Boolean flag = false;
                        BigDecimal conversionFactor = new BigDecimal(1);
                        for (CommodityConversion commodityConversion : conversions) {
                            if (commodityConversion.getBeforeUnit().equals(recipe.getUsedUnit())) {
                                conversionFactor = commodityConversion.getAfterNumber();
                                flag = true;
                            }

                        }
                        //说明没有匹配到单位对应的单位  那就都按最小单位来返回
                        if (!flag) {
                            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
                            rawMaterialLogDO.setCategoryName(recipe.getMaterialsTypeName());
                            rawMaterialLogDO.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
                            rawMaterialLogDO.setStoreId(storeId);
                            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
                            rawMaterialLogDO.setUnitName(recipe.getUsedUnit());
                            rawMaterialLogDO.setStatus(1);
                            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
                            response.setUsedUnit(conversions.get(0).getAfterUnit());
                            response.setUnitPrice(rawMaterial.getUnitPrice());
                            response.setQuantity(remainingNumber);
                        }else{
                            BigDecimal multipliedPrice = rawMaterial.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
                            BigDecimal number = remainingNumber.divide(conversionFactor, 4, RoundingMode.HALF_UP);
                            response.setUnitPrice(multipliedPrice);
                            response.setQuantity(number);
                            response.setUsedUnit(recipe.getUsedUnit());
                        }
                    }

                } catch (Exception e) {
                    throw exception(1_003_011_009, "数据有问题，单位换算列表不存在");
                }
            }else{
                response.setCalUnitList(new ArrayList<>());
                List<String> unitList = new ArrayList<>();
                unitList.add(rawMaterial.getMinUnit());
                response.setUnitList(unitList);
                response.setUsedUnit(rawMaterial.getMinUnit());
                response.setUnitPrice(rawMaterial.getUnitPrice());
                response.setQuantity(remainingNumber);
                return response;
            }
        }






        return response;
    }

    /**
     * 从原材料创建材料响应对象
     */
    private MaterialListRespVo createMaterialResponseFromRawMaterial(RawMaterial rawMaterial) {
        MaterialListRespVo response = new MaterialListRespVo();
        response.setRawMaterialId(rawMaterial.getId());
        response.setMaterialsId(rawMaterial.getCommodityCode());
        response.setMaterialsName(rawMaterial.getCommodityName());
        response.setUnitPrice(rawMaterial.getUnitPrice());
        response.setUsedUnit(rawMaterial.getMinUnit());
        processMaterialSpecifications(response, rawMaterial, null);

        return response;
    }

    /**
     * 处理材料规格信息
     */
    private void processMaterialSpecifications(MaterialListRespVo response,
                                               RawMaterial rawMaterial, RecipeDO recipe) {

        if (ObjectUtil.isNotEmpty(rawMaterial.getSpecificationsRules())&& !rawMaterial.getSpecificationsRules().equals("[]")) {
            try {
                ObjectMapper objectMapper = createConfiguredObjectMapper();
                List<CommodityConversion> conversionList = objectMapper.readValue(
                        rawMaterial.getSpecificationsRules(),
                        new TypeReference<List<CommodityConversion>>() {}
                );

                response.setCalUnitList(conversionList);

                Set<String> unitSet = new HashSet<>();
                if (ObjectUtil.isNotEmpty(conversionList)) {
                    conversionList.stream()
                            .map(CommodityConversion::getBeforeUnit)
                            .forEach(unitSet::add);
                }
                unitSet.add(rawMaterial.getMinUnit());

                response.setUnitList(new ArrayList<>(unitSet));

                if (recipe != null) {
                    calculateUnitPrice(response, rawMaterial, recipe, conversionList);
                }
            } catch (Exception e) {
//            log.error("处理规格规则时发生错误，materialId: {}", rawMaterial.getId(), e);
            }
        }else{
            response.setCalUnitList(new ArrayList<>());
            response.setUnitPrice(rawMaterial.getUnitPrice());
            List<String> stringList = new ArrayList<>();
            stringList.add(rawMaterial.getMinUnit());
            response.setUnitList(stringList);
        }


    }

//    /**
//     * 计算消耗量
//     */
//    private CommodityWhetherVO calculateConsumptionTwo(String recipeConsumption, BigDecimal quantity,RecipeDO recipe) {
//        if(ObjectUtil.isEmpty(recipe.getUsedUnit())){
//            throw exception(1_003_011_009, "数据不正确,对应的配方下单位为空");
//        }
//        CommodityWhetherVO commodityWhetherVO = new CommodityWhetherVO();
//        BigDecimal consumptionMultiple = new BigDecimal(1);
//        Long materialsId = recipe.getMaterialsId();
//        LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
//        scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,materialsId);
//        List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
//        if(ObjectUtil.isNotEmpty(scmUnitConversions)){
//            commodityWhetherVO.setScmUnitConversions(scmUnitConversions);
//            ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
//            if(!recipe.getUsedUnit().equals(scmUnitConversion.getAfterUnit())){
//                for (ScmUnitConversion unitConversion : scmUnitConversions) {
//                    if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
//                        consumptionMultiple = BigDecimal.valueOf(unitConversion.getAfterNumber());
//                    }
//
//                }
//
//            }else{
//                commodityWhetherVO.setIsUnit(true);
//            }
//            BigDecimal consumption = new BigDecimal(recipeConsumption);
//            BigDecimal multiply = consumption.multiply(consumptionMultiple).setScale(4, RoundingMode.HALF_UP);
//            BigDecimal decimal = multiply.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
//            commodityWhetherVO.setTotalNumber(decimal);
//        }else{
//            //说明是单品
//            BigDecimal consumption = new BigDecimal(recipeConsumption);
//            BigDecimal multiply = consumption.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
//            commodityWhetherVO.setTotalNumber(multiply);
//        }
//
//        return commodityWhetherVO;
//    }


    /**
     * 计算消耗量
     */
    private CommodityWhetherVO schoolProgramCalculateConsumptionTwo(String recipeConsumption, BigDecimal quantity,Long skuId,RecipeDO recipe) {
        if(ObjectUtil.isEmpty(recipe.getUsedUnit())){

//            LambdaQueryWrapper<CommodityStoreSku> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(CommodityStoreSku::getCommodityStoreSkuId,skuId);
//            CommodityStoreSku commodityStoreSku = commodityStoreSkuMapper.selectOne(lambdaQueryWrapper);
                CommoditySkus commoditySkus = commoditySkusMapper.selectById(skuId);
            if(commoditySkus!=null){
                CommoditySpus commoditySpus = commoditySpusMapper.selectById(commoditySkus.getCommodityId());
                if(commoditySpus!=null){
                    String message = commoditySpus.getCommodityName();
                    throw exception(1_003_011_009, message+"数据不正确,对应的配方下单位为空");
                }

            }

        }
        CommodityWhetherVO commodityWhetherVO = new CommodityWhetherVO();
        BigDecimal consumptionMultiple = new BigDecimal(1);
        Long materialsId = recipe.getMaterialsId();
        LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
        scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,materialsId);
        List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
        if(ObjectUtil.isNotEmpty(scmUnitConversions)){
            commodityWhetherVO.setScmUnitConversions(scmUnitConversions);
            ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
            if(!recipe.getUsedUnit().equals(scmUnitConversion.getAfterUnit())){
                for (ScmUnitConversion unitConversion : scmUnitConversions) {
                    if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                        consumptionMultiple = BigDecimal.valueOf(unitConversion.getAfterNumber());
                    }

                }

            }else{
                commodityWhetherVO.setIsUnit(true);
            }
            BigDecimal consumption = new BigDecimal(recipeConsumption);
            BigDecimal multiply = consumption.multiply(consumptionMultiple).setScale(4, RoundingMode.HALF_UP);
            BigDecimal decimal = multiply.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
            commodityWhetherVO.setTotalNumber(decimal);
        }else{
            //说明是单品
            BigDecimal consumption = new BigDecimal(recipeConsumption);
            BigDecimal multiply = consumption.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
            commodityWhetherVO.setTotalNumber(multiply);
        }

        return commodityWhetherVO;
    }


    /**
     * 计算消耗量
     */
    private CommodityWhetherVO calculateConsumptionTwo(String recipeConsumption, BigDecimal quantity,Long skuId,RecipeDO recipe) {
        if(ObjectUtil.isEmpty(recipe.getUsedUnit())){
            throw exception(1_003_011_009, "数据不正确,对应的配方下单位为空");
        }
        CommodityWhetherVO commodityWhetherVO = new CommodityWhetherVO();
        BigDecimal consumptionMultiple = new BigDecimal(1);
        Long materialsId = recipe.getMaterialsId();
        LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
        scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,materialsId);
        List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
        if(ObjectUtil.isNotEmpty(scmUnitConversions)){
            commodityWhetherVO.setScmUnitConversions(scmUnitConversions);
            ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);
            if(!recipe.getUsedUnit().equals(scmUnitConversion.getAfterUnit())){
                for (ScmUnitConversion unitConversion : scmUnitConversions) {
                    if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                        consumptionMultiple = BigDecimal.valueOf(unitConversion.getAfterNumber());
                    }

                }

            }else{
                commodityWhetherVO.setIsUnit(true);
            }
            BigDecimal consumption = new BigDecimal(recipeConsumption);
            BigDecimal multiply = consumption.multiply(consumptionMultiple).setScale(4, RoundingMode.HALF_UP);
            BigDecimal decimal = multiply.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
            commodityWhetherVO.setTotalNumber(decimal);
        }else{
            //说明是单品
            BigDecimal consumption = new BigDecimal(recipeConsumption);
            BigDecimal multiply = consumption.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
            commodityWhetherVO.setTotalNumber(multiply);
        }

        return commodityWhetherVO;
    }


    /**
     * 计算消耗量
     */
    private BigDecimal calculateConsumption(String recipeConsumption, BigDecimal quantity) {

        BigDecimal consumption = new BigDecimal(recipeConsumption);
        return consumption.multiply(quantity).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 计算单个材料的消耗量
     */
    private BigDecimal calculateConsumptionForMaterial(BigDecimal needed, BigDecimal available) {
        BigDecimal bigDecimal = needed.compareTo(available) <= 0 ? needed : available;
        return bigDecimal;
    }

    /**
     * 创建配置好的ObjectMapper
     */
    private ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * 计算单价
     */
    private void calculateUnitPrice(MaterialListRespVo materialListRespVo, RawMaterial material,
                                    RecipeDO recipeDO, List<CommodityConversion> conversionList) {
        if (!recipeDO.getUsedUnit().equals(material.getMinUnit())) {
            materialListRespVo.setUnitPrice(material.getUnitPrice());
            return;
        }

        BigDecimal conversionFactor = conversionList.stream()
                .filter(conversion -> conversion.getBeforeUnit().equals(recipeDO.getUsedUnit()))
                .findFirst()
                .map(CommodityConversion::getAfterNumber)
                .orElse(BigDecimal.ZERO);

        BigDecimal multipliedPrice = material.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
        materialListRespVo.setUnitPrice(multipliedPrice);
    }

    /**
     * 按创建时间升序排序
     */
    public static void sortByCreateTimeAscending(List<RawMaterial> materials) {
        materials.sort(Comparator.comparing(RawMaterial::getCreateTime));
    }






}
