package com.htyoudao.youdao.module.commodity.service.materialLoss;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.api.VO.MaterialDataVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeMaterialDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLossRecordDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStockFlow;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.FlowAmountDTO;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySkusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySpusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.ScmCommodity.ScmCommodityMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialLossRecordMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialStockFlowMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.invertory.RawMaterialStocktakeMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.scmUnitConversion.ScmUnitConversionMapper;
import com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.commodity.enums.LossType;
import com.htyoudao.youdao.module.commodity.enums.RawMaterialLog;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
@Service
@Slf4j
public class RawMaterialInventoryServiceImpl implements RawMaterialInventoryService {

    @Autowired
    private RawMaterialLossRecordMapper materialLossRecordMapper;

    @Autowired
    private RawMaterialStockFlowMapper rawMaterialStockFlowMapper;

    @Autowired
    private RawMaterialMapper rawMaterialMapper;

    @Autowired
    private CommoditySpusMapper commoditySpusMapper;

    @Autowired
    private CommoditySkusMapper commoditySkusMapper;
    

    @Resource
    private RowMaterialStockFlowService flowService;

    @Autowired
    private RowMaterialStockFlowService materialStockFlowService;

    @Resource
    private RawMaterialStocktakeMapper stocktakeMapper;


    @Autowired
    private RowMaterialStockFlowService rowMaterialStockFlowService;

    @Resource
    private CommodityRecipeMapper commodityRecipeMapper;


    @Resource
    private RawMaterialLogService rawMaterialLogService;

    @Resource
    private ICommoditySpusService commoditySpusService;


    @Resource
    private ScmUnitConversionMapper scmUnitConversionMapper;


    @Resource
    private ScmCommodityMapper scmCommodityMapper;



    @Resource
    private CommodityRecipeMaterialMapper commodityRecipeMaterialMapper;



    @Override
    public MaterialOwnDataRespVo splitCommodity(CommoditySplitMaterialReqVO commoditySplitMaterialReqVO) {
        MaterialOwnDataRespVo materialOwnDataRespVo = new MaterialOwnDataRespVo();
        List<MaterialListRespVo> resultList = new ArrayList<>();
        List<MaterialDataVo> materialDataVos = commoditySplitMaterialReqVO.getMaterialDataVos();

        if (ObjectUtil.isEmpty(materialDataVos)) {
            MaterialOwnDataRespVo ownDataRespVo = new MaterialOwnDataRespVo();
            ownDataRespVo.setRespVoList(new ArrayList<>());
            ownDataRespVo.setErrorVoList(new ArrayList<>());
            return ownDataRespVo;
        }
        List<RawMaterialIdVO> repertoryList = new ArrayList<>();
        Map<Long, BigDecimal> repertoryMap = new HashMap<>();

        List<MaterialErrorVo> materialErrorVos = new ArrayList<>();



        for (MaterialDataVo materialDataVo : materialDataVos) {
//            validateMaterialData(materialDataVo);

            try {

                processRecipeCommodity(materialDataVo, commoditySplitMaterialReqVO.getStoreId(), resultList,repertoryList,repertoryMap,materialErrorVos);

            } catch (Exception e) {
//                log.error("处理商品数据失败: commodityId={}, skuId={}",
//                        materialDataVo.getCommodityId(), materialDataVo.getSkuId(), e);
                throw e;
            }
        }

        materialOwnDataRespVo.setRespVoList(resultList);
        materialOwnDataRespVo.setErrorVoList(materialErrorVos);
        return materialOwnDataRespVo;
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
                                        List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap,List<MaterialErrorVo> materialErrorVos) {
        //获取当前Sku下配方列表
        List<RecipeDO> recipes = findRecipesByCommodity(materialDataVo);

        if (ObjectUtil.isEmpty(recipes)) {
            //没有找到配方
            MaterialErrorVo materialErrorVo = new MaterialErrorVo();
            materialErrorVo.setSkuId(materialDataVo.getSkuId());
            materialErrorVo.setStoreId(storeId);
            materialErrorVo.setErrorName(ErrorCodeConstants.MATERIAL_RECIPE_NOT_FOUND.getMsg());
            materialErrorVo.setErrorCode(ErrorCodeConstants.MATERIAL_RECIPE_NOT_FOUND.getCode());
            materialErrorVos.add(materialErrorVo);
            //异步生成日志
            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
            rawMaterialLogDO.setLogType(RawMaterialLog.RECIPE_NOT_FOUND.getType());
            rawMaterialLogDO.setStoreId(storeId);
            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
            rawMaterialLogDO.setStatus(0);
            rawMaterialLogDO.setCommodityName(materialDataVo.getCommodityName());
            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
//            asyncData(null,storeId,materialDataVo,RawMaterialLog.RECIPE_NOT_FOUND.getType());
            return;
        }




        for (RecipeDO recipe : recipes) {
            if (recipe.getIsAlternative().equals(0L)) {
                processNonAlternativeRecipe(recipe, materialDataVo, storeId, resultList,materialErrorVos);
            } else if (recipe.getIsAlternative().equals(1L)) {
                //可替换原材料逻辑
                processAlternativeRecipe(recipe, materialDataVo, storeId, resultList,repertoryList,repertoryMap,materialErrorVos);
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

    /**
     * 处理非替代配方
     */
    private void processNonAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                             Long storeId, List<MaterialListRespVo> resultList,List<MaterialErrorVo> materialErrorVos) {
        CommodityWhetherVO commodityWhetherVO = calculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(), recipe,storeId,materialErrorVos);
        if(commodityWhetherVO==null){
            return;
        }
        RawMaterial rawMaterial = findRawMaterialByCode(recipe.getMaterialsCode(), storeId);
        if (rawMaterial == null) {
            errorMe(recipe, ErrorCodeConstants.MATERIAL_STORE_NOT_FOUND, storeId, materialErrorVos);
            //异步生成日志
//            asyncData(recipe.getMaterialsCode(), storeId, materialDataVo,RawMaterialLog.STORE_MATERIAL_NOT_FOUND.getType());
            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
            rawMaterialLogDO.setMaterialCode(recipe.getMaterialsCode());
            rawMaterialLogDO.setLogType(RawMaterialLog.STORE_MATERIAL_NOT_FOUND.getType());
            rawMaterialLogDO.setStoreId(storeId);
            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
            rawMaterialLogDO.setStatus(0);
            //是否可替换
            rawMaterialLogDO.setIsAlternative(recipe.getIsAlternative());
            //计算消耗量
            BigDecimal consumption = new BigDecimal(recipe.getConsumption());
            rawMaterialLogDO.setConsumption(consumption.multiply(materialDataVo.getQuantity()).setScale(4, RoundingMode.HALF_UP));
            //原料名称
            rawMaterialLogDO.setMaterialName(recipe.getMaterialsName());
            rawMaterialLogDO.setMaterialCode(recipe.getMaterialsCode());
            rawMaterialLogDO.setCommodityName(materialDataVo.getCommodityName());
            //商品名称
            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
            return;
        }

        MaterialListRespVo responseVo = createMaterialResponseFromRecipe(rawMaterial,recipe,commodityWhetherVO.getTotalNumber(),materialDataVo,storeId,materialErrorVos);
//         createMaterialResponseFromRecipe(rawMaterial, recipe,commodityWhetherVO);
//        BigDecimal consumption = calculateConsumption(recipe.getConsumption(), materialDataVo.getQuantity());
//        responseVo.setQuantity(consumption);
//        responseVo.setUsedUnit(recipe.getUsedUnit());

        resultList.add(responseVo);
    }



    /**
     * 处理替代配方
     */
    private void processAlternativeRecipe(RecipeDO recipe, MaterialDataVo materialDataVo,
                                          Long storeId, List<MaterialListRespVo> resultList,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap,List<MaterialErrorVo> materialErrorVos) {
        //计算消耗量与数量乘积
        CommodityWhetherVO commodityWhetherVO = calculateConsumptionTwo(recipe.getConsumption(), materialDataVo.getQuantity(), recipe,storeId,materialErrorVos);
        if(commodityWhetherVO==null){
            return;
        }
        //按照时间升序  来获取可替换原材料列表
        List<RawMaterial> alternativeMaterials = findAlternativeMaterials(recipe, storeId);

        if (ObjectUtil.isEmpty(alternativeMaterials) || alternativeMaterials.size()==0) {
            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
            rawMaterialLogDO.setCategoryName(recipe.getMaterialsTypeName());
            rawMaterialLogDO.setLogType(RawMaterialLog.CATEGORY_NO_MATERIAL.getType());
            rawMaterialLogDO.setStoreId(storeId);
            rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
            rawMaterialLogDO.setStatus(0);
            //是否可替换
            rawMaterialLogDO.setIsAlternative(recipe.getIsAlternative());
            //计算消耗量
            BigDecimal consumption = new BigDecimal(recipe.getConsumption());
            rawMaterialLogDO.setConsumption(consumption.multiply(materialDataVo.getQuantity()).setScale(4, RoundingMode.HALF_UP));
            //原料名称
            rawMaterialLogDO.setMaterialName(recipe.getMaterialsName());
            rawMaterialLogDO.setMaterialCode(recipe.getMaterialsCode());
            rawMaterialLogDO.setCommodityName(materialDataVo.getCommodityName());
            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);

            errorMe(recipe, ErrorCodeConstants.MATERIAL_STORE_CATEGORY_NOT_FOUND, storeId, materialErrorVos);
            return;

        }


        distributeConsumptionAmongAlternatives(recipe, commodityWhetherVO, alternativeMaterials, resultList,storeId,materialDataVo,repertoryList,repertoryMap,materialErrorVos);
    }

    private void errorMe(RecipeDO recipe, ErrorCode materialStoreCategoryNotFound, Long storeId, List<MaterialErrorVo> materialErrorVos) {
        MaterialErrorVo materialErrorVo = new MaterialErrorVo();
        materialErrorVo.setSkuId(recipe.getSkuId());
        materialErrorVo.setCommodityCode(recipe.getMaterialsCode());
        materialErrorVo.setErrorName(materialStoreCategoryNotFound.getMsg());
        materialErrorVo.setErrorCode(materialStoreCategoryNotFound.getCode());
        materialErrorVo.setStoreId(storeId);
        materialErrorVos.add(materialErrorVo);
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
        long startTime = System.nanoTime();
        List<RawMaterial> materials = rawMaterialMapper.selectList(wrapper);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        log.info(">>> 获取符合条件的替换商品列表接口调用耗时: {} ms", duration);


        sortByCreateTimeAscending(materials);

        return materials;
    }

    /**
     * 获取需要排除的材料编码
     */
    private List<String> getExcludedMaterialCodes(Long recipeId) {
        long startTime = System.nanoTime();
        LambdaQueryWrapperX<RecipeMaterialDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(RecipeMaterialDO::getRecipeId, recipeId);
        List<RecipeMaterialDO> recipeMaterials = commodityRecipeMaterialMapper.selectList(wrapper);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        log.info(">>> 获取未勾选的替换商品列表接口调用耗时: {} ms", duration);


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
    private void distributeConsumptionAmongAlternatives(RecipeDO recipe, CommodityWhetherVO commodityWhetherVO,
                                                        List<RawMaterial> alternatives,
                                                        List<MaterialListRespVo> resultList,Long storeId,MaterialDataVo materialDataVo,List<RawMaterialIdVO> repertoryList,Map<Long,BigDecimal> repertoryMap,List<MaterialErrorVo> materialErrorVos) {
        BigDecimal remainingConsumption = commodityWhetherVO.getTotalNumber();

//        List<RawMaterialIdVO> rawMaterialIdVOList = new ArrayList<>();

        //为解决单一配方中 可能有多个原材料一样的  防止扣减库存重复
//        BigDecimal inventory = BigDecimal.ZERO;
        for (RawMaterial material : alternatives) {
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
                repertoryList.add(rawMaterialIdVO);
                repertoryMap.put(material.getId(),remainingNumber);
            }

            MaterialListRespVo responseVo = createMaterialResponseFromRecipe(material, recipe,remainingNumber,materialDataVo,storeId,materialErrorVos);
            resultList.add(responseVo);

            remainingConsumption = remainingConsumption.subtract(remainingNumber);
        }

        // 如果还有剩余消耗量，使用第一个替代材料
        if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
            RawMaterial firstMaterial = alternatives.get(0);
            MaterialListRespVo responseVo = createMaterialResponseFromRecipe(firstMaterial, recipe,remainingConsumption,materialDataVo,storeId,materialErrorVos);
            resultList.add(responseVo);
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

//    /**
//     * 处理原材料商品
//     */
//    private void processRawMaterialCommodity(MaterialDataVo materialDataVo, Long storeId,
//                                             List<MaterialListRespVo> resultList) {
//        if (ObjectUtil.isEmpty(materialDataVo.getMaterialId())) {
//            return;
//        }
//
//        RawMaterial rawMaterial = findRawMaterialByCode(materialDataVo.getMaterialId(), storeId);
//        if (rawMaterial == null) {
//            //异步生成日志
//            asyncData(materialDataVo.getMaterialId(), storeId, materialDataVo,RawMaterialLog.STORE_MATERIAL_NOT_FOUND.getType());
//            throw exception(1_003_011_001, "门店下商品编码为" + materialDataVo.getMaterialId() + "不存在");
//        }
//
//        MaterialListRespVo responseVo = createMaterialResponseFromRawMaterial(rawMaterial);
//        responseVo.setQuantity(materialDataVo.getQuantity());
//        responseVo.setUsedUnit(rawMaterial.getMinUnit());
//
//        resultList.add(responseVo);
//    }

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
        if(ObjectUtil.isNotEmpty(scmUnitConversions)){
            for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
                CommodityConversion commodityConversion = new CommodityConversion();
                BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
                commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
                conversionList.add(commodityConversion);
            }
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


    private MaterialListRespVo createMaterialResponseFromRecipe(RawMaterial rawMaterial, RecipeDO recipe,BigDecimal remainingNumber,MaterialDataVo materialDataVo,Long storeId,List<MaterialErrorVo> materialErrorVos) {
        MaterialListRespVo response = new MaterialListRespVo();
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
                        rawMaterialLogDO.setStatus(0);
                        rawMaterialLogDO.setUnitError(rawMaterial.getMinUnit());
                        rawMaterialLogDO.setConsumption(remainingNumber);
                        rawMaterialLogDO.setCommodityName(materialDataVo.getCommodityName());
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
                errorMethod(recipe, ErrorCodeConstants.MATERIAL_UNIT_CONVERSION_NOT_FOUND, storeId, materialErrorVos);
                return null;
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


        return response;
    }


//    /**
//     * 从配方创建材料响应对象
//     */
//    private MaterialListRespVo createMaterialResponseFromRecipe(RawMaterial rawMaterial, RecipeDO recipe,CommodityWhetherVO commodityWhetherVO,BigDecimal remainingNumber,MaterialDataVo materialDataVo,Long storeId) {
//        MaterialListRespVo response = new MaterialListRespVo();
//        response.setRawMaterialId(rawMaterial.getId());
//        response.setMaterialsId(rawMaterial.getCommodityCode());
//        response.setMaterialsName(rawMaterial.getCommodityName());
//        response.setUnitPrice(rawMaterial.getUnitPrice());
//        List<ScmUnitConversion> scmUnitConversions = commodityWhetherVO.getScmUnitConversions();
//        List<CommodityConversion> conversionList = new ArrayList<>();
//        for (ScmUnitConversion scmUnitConversion : scmUnitConversions) {
//            CommodityConversion commodityConversion = new CommodityConversion();
//            BeanUtils.copyProperties(scmUnitConversion, commodityConversion);
//            commodityConversion.setAfterNumber(BigDecimal.valueOf(scmUnitConversion.getAfterNumber()));
//            conversionList.add(commodityConversion);
//        }
//        response.setCalUnitList(conversionList);
//
//        Set<String> unitSet = new HashSet<>();
//        if (ObjectUtil.isNotEmpty(scmUnitConversions)) {
//            scmUnitConversions.stream()
//                    .map(ScmUnitConversion::getBeforeUnit)
//                    .forEach(unitSet::add);
//        }
//        unitSet.add(scmUnitConversions.get(0).getAfterUnit());
//
//        response.setUnitList(new ArrayList<>(unitSet));
//        if (commodityWhetherVO.getIsUnit()) {
//            response.setUsedUnit(scmUnitConversions.get(0).getAfterUnit());
//            response.setUnitPrice(rawMaterial.getUnitPrice());
//            response.setQuantity(remainingNumber);
//        }else{
//            Boolean flag = false;
//            BigDecimal conversionFactor = new BigDecimal(1);
//            for (CommodityConversion commodityConversion : conversionList) {
//                if (commodityConversion.getBeforeUnit().equals(recipe.getUsedUnit())) {
//                    conversionFactor = commodityConversion.getAfterNumber();
//                    flag = true;
//                }
//
//            }
//            if (!flag) {
//                RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
//                rawMaterialLogDO.setCategoryName(recipe.getMaterialsTypeName());
//                rawMaterialLogDO.setLogType(RawMaterialLog.UNIT_MISMATCH.getType());
//                rawMaterialLogDO.setStoreId(storeId);
//                rawMaterialLogDO.setSkuId(materialDataVo.getSkuId());
//                rawMaterialLogDO.setUnitName(recipe.getUsedUnit());
//                rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);
//                response.setUsedUnit(null);
//                BigDecimal number = remainingNumber.divide(conversionFactor, 4, RoundingMode.HALF_UP);
//                response.setQuantity(number);
//            }else{
//                BigDecimal multipliedPrice = rawMaterial.getUnitPrice().multiply(conversionFactor).setScale(4, RoundingMode.HALF_UP);
//                BigDecimal number = remainingNumber.divide(conversionFactor, 4, RoundingMode.HALF_UP);
//                response.setUnitPrice(multipliedPrice);
//                response.setQuantity(number);
//                response.setUsedUnit(recipe.getUsedUnit());
//            }
//
//        }
//
//        return response;
//    }

    /**
     * 从原材料创建材料响应对象
     */
    private MaterialListRespVo createMaterialResponseFromRawMaterial(RawMaterial rawMaterial) {
        MaterialListRespVo response = new MaterialListRespVo();
        response.setRawMaterialId(rawMaterial.getId());
        response.setMaterialsId(rawMaterial.getCommodityCode());
        response.setMaterialsName(rawMaterial.getCommodityName());
        response.setUnitPrice(rawMaterial.getUnitPrice());

        processMaterialSpecifications(response, rawMaterial, null);

        return response;
    }

    /**
     * 处理材料规格信息
     */
    private void processMaterialSpecifications(MaterialListRespVo response,
                                               RawMaterial rawMaterial, RecipeDO recipe) {
        if (ObjectUtil.isEmpty(rawMaterial.getSpecificationsRules())) {
            return;
        }

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
    }

    /**
     * 计算消耗量
     */
    private CommodityWhetherVO calculateConsumptionTwo(String recipeConsumption, BigDecimal quantity,RecipeDO recipe,Long storeId, List<MaterialErrorVo> materialErrorVos) {
        if(ObjectUtil.isEmpty(recipe.getUsedUnit())){
            errorMethod(recipe, ErrorCodeConstants.MATERIAL_RECIPE_UNIT_NOT_FOUND, storeId, materialErrorVos);
            return null;
        }
        CommodityWhetherVO commodityWhetherVO = new CommodityWhetherVO();
        BigDecimal consumptionMultiple = new BigDecimal(1);
        Long materialsId = recipe.getMaterialsId();
        LambdaQueryWrapperX<ScmUnitConversion> scmUnitConversionLambdaQueryWrapperX = new LambdaQueryWrapperX<>();
        scmUnitConversionLambdaQueryWrapperX.eq(ScmUnitConversion::getCommodityId,materialsId);
        long startTime = System.nanoTime();
        List<ScmUnitConversion> scmUnitConversions = scmUnitConversionMapper.selectList(scmUnitConversionLambdaQueryWrapperX);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        log.info(">>> 获取单位转换列表接口调用耗时: {} ms", duration);
        if(ObjectUtil.isNotEmpty(scmUnitConversions)){
            commodityWhetherVO.setScmUnitConversions(scmUnitConversions);
            ScmUnitConversion scmUnitConversion = scmUnitConversions.get(0);

            if(!recipe.getUsedUnit().equals(scmUnitConversion.getAfterUnit())){
                for (ScmUnitConversion unitConversion : scmUnitConversions) {
                    if(unitConversion.getBeforeUnit().equals(recipe.getUsedUnit())){
                        consumptionMultiple = BigDecimal.valueOf(unitConversion.getAfterNumber());
                    }

                }
                if (consumptionMultiple.compareTo(BigDecimal.ONE) == 0){

                    errorMethod(recipe, ErrorCodeConstants.MATERIAL_UNIT_CORRESPOND_NOT_FOUND, storeId, materialErrorVos);
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

    private void errorMethod(RecipeDO recipe, ErrorCode materialRecipeUnitNotFound, Long storeId, List<MaterialErrorVo> materialErrorVos) {
        MaterialErrorVo materialErrorVo = new MaterialErrorVo();
        materialErrorVo.setSkuId(recipe.getSkuId());
        materialErrorVo.setCommodityCode(recipe.getMaterialsCode());
        materialErrorVo.setErrorName(materialRecipeUnitNotFound.getMsg());
        materialErrorVo.setErrorCode(materialRecipeUnitNotFound.getCode());
        materialErrorVo.setStoreId(storeId);
        materialErrorVo.setRecipeId(recipe.getRecipeId());
        materialErrorVos.add(materialErrorVo);
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
