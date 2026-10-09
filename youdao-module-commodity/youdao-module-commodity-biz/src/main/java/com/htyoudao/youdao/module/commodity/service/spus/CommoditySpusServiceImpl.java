package com.htyoudao.youdao.module.commodity.service.spus;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryBatchHiddenReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.spuTag.VO.CommodityTagReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown.BatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchUp.BatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryDelReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CategoryUpdateReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category.CommodityCateDateRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group.CommodityGroupRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group.CommodityGroupSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.shelfLock.CommoditySpuLockReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.*;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.singleUp.SingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku.CommoditySkuRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku.CommoditySkuSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySpusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.RecipeCommodityMapper;
import com.htyoudao.youdao.module.commodity.enums.SpuEnum;
import com.htyoudao.youdao.module.commodity.service.activity.CommodityActivityService;
import com.htyoudao.youdao.module.commodity.service.afterorder.AfterOrderService;
import com.htyoudao.youdao.module.commodity.service.category.ICommodityCategoryService;
import com.htyoudao.youdao.module.commodity.service.group.ICommoditySetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.inventory.ICommodityRecipeService;
import com.htyoudao.youdao.module.commodity.service.single.ICommodityGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.sku.ICommoditySkusService;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import com.htyoudao.youdao.module.commodity.service.templateSingle.ICommodityTemplateGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.templateSpu.ICommodityTemplateSpusService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.htyoudao.youdao.module.commodity.util.DateTimeSortUtil;
import com.htyoudao.youdao.module.promotion.api.couponCommodity.CouponCommodityApi;
import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import com.htyoudao.youdao.module.promotion.api.seckill.SeckillApi;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;



/**
 * 商品Service业务层处理
 *
 * @author Qizhongann
 * @date 2024-01-16
 */
@RefreshScope
@Service
@Slf4j
public class CommoditySpusServiceImpl extends ServiceImpl<CommoditySpusMapper, CommoditySpus> implements ICommoditySpusService {

    private final static Integer IS_ALL = 2;

    @Resource
    private CommoditySpusMapper commoditySpusMapper;
    @Resource
    private RecipeCommodityMapper recipeCommodityMapper;
    @Resource
    private ICommodityCategoryService commodityCategoryService;

    @Resource
    private ICommoditySkusService commoditySkusService;

    @Resource
    private ICommoditySetmealGroupService commoditySetmealGroupService;

    @Resource
    private ICommodityGroupSingleService commodityGroupSingleService;

    @Resource
    private AfterOrderService commodityAfterOrderService;



    @Resource
    private ICommodityTemplateSpusService commodityTemplateSpusService;



    @Resource
    private ICommodityTemplateGroupSingleService commodityTemplateGroupSingleService;

    @Resource
    private ICommodityTageService commodityTageService;
    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;

    @DubboReference
    private CouponCommodityApi couponCommodityApi;

    @DubboReference
    private SeckillApi seckillApi;

    @DubboReference
    private OrgApi orgApi;

    @Resource
    private CommodityActivityService commodityActivityService;

    @Resource
    private ICommodityRecipeService commodityRecipeService;
    @Resource
    private RedissonClient redissonClient;



    @Override
    public void deleteCategoryV3(CategoryDelReqVo delReqVo) {
        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(CommoditySpus::getCategoryId, delReqVo.getId());

        List<CommoditySpus> list = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            throw exception(BASE_CATE_HAS_PRODUCTS_CANNOT_DELETE);
        } else {
            commodityCategoryService.deleteById(delReqVo.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_CREATE_SUB_TYPE, bizNo = "{{#commoditySpus.commodityId}}", success = COMMODITY_SPU_CREATE_SUCCESS)
    public void createSpuV3(CommoditySpusSaveReqVo saveReqVo) {
        CommoditySpus commoditySpus = new CommoditySpus();
        BeanUtils.copyProperties(saveReqVo, commoditySpus);
        if(ObjectUtil.isNotEmpty(saveReqVo.getStartDate())){
            if (ObjectUtil.isEmpty(saveReqVo.getEndDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
        if (ObjectUtil.isNotEmpty(saveReqVo.getEndDate())){
            if (ObjectUtil.isEmpty(saveReqVo.getStartDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }


        if (ObjectUtil.isNotEmpty(saveReqVo.getImageUrlVO())) {
            commoditySpus.setImageUrl(ConvertUtil.convertListToStringS(saveReqVo.getImageUrlVO()));
        }

        ObjectMapper objectMapper = new ObjectMapper();



        if (ObjectUtil.isNotEmpty(saveReqVo.getTagIdList())){
            commoditySpus.setTagIds(ConvertUtil.convertListToString(saveReqVo.getTagIdList()));
        }


        if (ObjectUtil.isNotEmpty(saveReqVo.getCommodityFlavorList())) {

            try {

                List<CommodityFlavor> commodityFlavorList = saveReqVo.getCommodityFlavorList();
                commodityFlavorList.forEach(commodityFlavor -> {
                    commodityFlavor.setFlavorId(IdWorker.getId());
                });

                // 将 commodityFlavorList 序列化为 JSON 字符串
                String flavorJson = objectMapper.writeValueAsString(saveReqVo.getCommodityFlavorList());
                // 设置到 flavor 属性
                commoditySpus.setFlavor(flavorJson);
            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION);
            }
        }
        if (ObjectUtil.isNotEmpty(saveReqVo.getCommodityCondimentsList())) {

            List<CommodityCondiments> commodityCondimentsList = saveReqVo.getCommodityCondimentsList();
            commodityCondimentsList.forEach(commodityCondiments -> {
                commodityCondiments.setCondimentId(IdWorker.getId());
            });
            try {
                // 将 commodityFlavorList 序列化为 JSON 字符串
                String condiments = objectMapper.writeValueAsString(saveReqVo.getCommodityCondimentsList());
                // 设置到 flavor 属性
                commoditySpus.setCondiments(condiments);
            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION);
            }


        }

        List<CommoditySkuSaveVO> commoditySkusList = saveReqVo.getCommoditySkusList();
        if (ObjectUtil.isEmpty(commoditySkusList)) {
            throw exception(BASE_SKU_PRODUCT_MUST_HAVE_SPECIFICATION);
        }
        if (commoditySkusList.size() > 1) {
            for (CommoditySkuSaveVO commoditySkus : commoditySkusList) {
                if (ObjectUtil.isEmpty(commoditySkus.getSkusName())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_NAMES_REQUIRED);
                }
                if (ObjectUtil.isEmpty(commoditySkus.getSkusValue())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_VALUES_REQUIRED);
                }
            }
        }


        //新建商品默认给下架 2025.0227 新需求 默认上架
        saveReqVo.setStoreStatus(1);
        saveReqVo.setWxStatus(1);
        //处理排序，默认（未填）倒序  填写后，其他商品顺位下移


        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(CommoditySpus::getCategoryId, saveReqVo.getCategoryId());
        spusLambdaQueryWrapper.orderByAsc(CommoditySpus::getSort);
        List<CommoditySpus> list = commoditySpusMapper.selectList(spusLambdaQueryWrapper);

        // 首先判断 commoditySpus 的 sort 是否为空
        if (ObjectUtil.isEmpty(saveReqVo.getSort())) {
            saveReqVo.setSort(1);
        }

        // 再判断 list 是否不为空
        if (ObjectUtil.isNotEmpty(list)) {
            boolean needUpdate = false;
            Integer targetSort = saveReqVo.getSort();
            for (CommoditySpus spus : list) {
                if (spus.getSort().equals(targetSort)) {
                    spus.setSort(spus.getSort() + 1);
                    targetSort++;
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                commoditySpusMapper.updateBatch(list);
            }
        }

        commoditySpusMapper.insert(commoditySpus);

        LogRecordContext.putVariable("commoditySpus", commoditySpus);

        List<CommoditySkus> skusList = coverSkuEntityList(commoditySkusList);

        skusList.forEach(c -> c.setCommodityId(commoditySpus.getCommodityId()));
        skusList.forEach(c -> c.setSkuId(null));
        commoditySkusService.saveBatch(skusList);

        if (commoditySpus.getIsSingle().equals(2)) {
            if (ObjectUtil.isEmpty(commoditySpus.getSetmealType())) {
                throw exception(BASE_SPU_PACKAGE_TYPE_NOT_SELECTED);
            }
            if (ObjectUtil.isNotEmpty(saveReqVo.getCommodityGroupList())) {
                for (CommodityGroupSaveVO commodityGroupSaveVO : saveReqVo.getCommodityGroupList()) {
                    if (saveReqVo.getSetmealType().equals(2) && commodityGroupSaveVO.getAttribute()==1){
                        checkSetmealSingleNum(commodityGroupSaveVO);
                    }
                    CommoditySetmealGroup commoditySetmealGroup = new CommoditySetmealGroup();
                    BeanUtils.copyProperties(commodityGroupSaveVO, commoditySetmealGroup);
                    commoditySetmealGroup.setCommodityId(commoditySpus.getCommodityId());
                    commoditySetmealGroup.setGroupId(null);
                    commoditySetmealGroupService.save(commoditySetmealGroup);
                    if (ObjectUtil.isNotEmpty(commodityGroupSaveVO.getCommoditySingleSaveVOList())) {
                        List<CommoditySingleSaveVO> commoditySingleSaveVOList = commodityGroupSaveVO.getCommoditySingleSaveVOList();
                        if (commoditySpus.getSetmealType().equals(1)) {
                            if (commoditySingleSaveVOList.size() <= 1) {
                                if (commoditySingleSaveVOList.get(0).getCopies()<=1){
                                    throw exception(BASE_SINGLE_PACKAGE_AT_LEAST_TWO_PRODUCTS);
                                }

                            }
                        }
                        /*List<Long> singleSkuIds = new ArrayList<>();
                        for (CommoditySingleSaveVO singleSaveVO : commoditySingleSaveVOList) {
                            if (singleSkuIds.contains(singleSaveVO.getSingleSkuId())) {
                                throw exception(BASE_SINGLE_SAME_GROUP_CANNOT_MULTISELECT_SAME_PRODUCT_SPEC);
                            } else {
                                singleSkuIds.add(singleSaveVO.getSingleSkuId());
                            }

                        }*/
                        List<CommodityGroupSingle> singleList = coverSingleEntityList(commoditySingleSaveVOList);


                        singleList.forEach(s -> {
                            s.setGroupId(commoditySetmealGroup.getGroupId());
                        });
                        singleList.forEach(s -> {
                            s.setSpuId(commoditySpus.getCommodityId());
                        });
                        singleList.forEach(c -> c.setCommodityGroupSingleId(null));

                        createSynchronizationFlavor(singleList);

                        commodityGroupSingleService.saveBatch(singleList);
                    } else {
                        throw exception(BASE_SINGLE_GROUP_NO_PRODUCTS_ADD);
                    }


                }
            } else {
                throw exception(BASE_SINGLE_PLEASE_ADD_PRODUCTS);
            }

        }
    }

    private List<CommodityGroupSingle> coverSingleEntityList(List<CommoditySingleSaveVO> commoditySingleSaveVOList) {
        List<CommodityGroupSingle> commodityGroupSingleList = new ArrayList<>();
        for (CommoditySingleSaveVO singleSaveVO : commoditySingleSaveVOList) {
            CommodityGroupSingle commodityGroupSingle = new CommodityGroupSingle();
            BeanUtils.copyProperties(singleSaveVO, commodityGroupSingle);
            commodityGroupSingleList.add(commodityGroupSingle);
        }
        return commodityGroupSingleList;
    }

    private List<CommoditySkus> coverSkuEntityList(List<CommoditySkuSaveVO> commoditySkusList) {
        List<CommoditySkus> skusList = new ArrayList<>();
        for (CommoditySkuSaveVO commoditySkuSaveVO : commoditySkusList) {
            CommoditySkus commoditySkus = new CommoditySkus();
            BeanUtils.copyProperties(commoditySkuSaveVO, commoditySkus);
            skusList.add(commoditySkus);
        }
        return skusList;

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_UPDATE_SUB_TYPE, bizNo = "{{#commoditySpus.commodityId}}", success = COMMODITY_SPU_UPDATE_SUCCESS)
    public void updateSpuV3(CommoditySpusUpdateReqVo updateReqVo) {

        try {
            RLock lock = redissonClient.getLock("updateSpu:" + updateReqVo.getCommodityId());
            boolean res = lock.tryLock(10, TimeUnit.SECONDS);
            if (!res) {
                log.info("修改商品，获取锁失败，stockLockKey = {}", updateReqVo.getCommodityId());
                throw exception(BASE_SPU_PRODUCT_CHANGE);
            }
            toUpdateSpu(updateReqVo);
        }catch (ServiceException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        } finally{
            RLock lock = redissonClient.getLock("updateSpu:" + updateReqVo.getCommodityId());
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }








    }

    @Override
    public CommodityHiddenFilterRespVO filterHiddenCommodityIds() {
        CommodityHiddenFilterRespVO respVO = new CommodityHiddenFilterRespVO();

        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CommoditySpus::getCommodityId, CommoditySpus::getIsHidden);
        List<CommoditySpus> commoditySpusList = commoditySpusMapper.selectList(queryWrapper);

        List<Long> hiddenIds = commoditySpusList.stream()
                .filter(commoditySpus -> Integer.valueOf(1).equals(commoditySpus.getIsHidden()))
                .map(CommoditySpus::getCommodityId)
                .toList();
        List<Long> visibleIds = commoditySpusList.stream()
                .filter(commoditySpus -> !Integer.valueOf(1).equals(commoditySpus.getIsHidden()))
                .map(CommoditySpus::getCommodityId)
                .toList();

        if (visibleIds.size() > hiddenIds.size()) {
            respVO.setCommodityIds(hiddenIds);
            respVO.setIsHidden(1);
        } else {
            respVO.setCommodityIds(visibleIds);
            respVO.setIsHidden(0);
        }
        return respVO;
    }

    private void toUpdateSpu(CommoditySpusUpdateReqVo updateReqVo) {

        CommoditySpus commoditySpus = new CommoditySpus();
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommoditySpus::getCommodityId, updateReqVo.getCommodityId());
        CommoditySpus commoditySpus1 = commoditySpusMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(updateReqVo, commoditySpus);
        commoditySpus.setIsChange(commoditySpus1.getIsChange());

        CommodityCategory targetCategory = commodityCategoryService.getById(updateReqVo.getCategoryId());
        Integer categoryHidden = targetCategory != null && targetCategory.getIsHidden() != null
                ? targetCategory.getIsHidden()
                : 0;
        commoditySpus.setIsHidden(categoryHidden);

        checkDate(updateReqVo);
        checkSort(updateReqVo);


        buildImage(updateReqVo, commoditySpus);

        ObjectMapper objectMapper = new ObjectMapper();


        if (ObjectUtil.isNotEmpty(updateReqVo.getTagIdList())){
            commoditySpus.setTagIds(ConvertUtil.convertListToString(updateReqVo.getTagIdList()));
        }else {
            commoditySpus.setTagIds(null);
        }

        buildFlavor(updateReqVo, objectMapper, commoditySpus);
        buildCondiment(updateReqVo, objectMapper, commoditySpus);

        //   commoditySkusService.deleteBySpuId(updateReqVo.getCommodityId());


        List<CommoditySkus> skusList =  buildSkuList(updateReqVo, commoditySpus);

        checkSetmeal(updateReqVo, commoditySpus,skusList);


        commoditySpusMapper.updateById(commoditySpus);

        if (Integer.valueOf(1).equals(categoryHidden)) {
            commodityAfterOrderService.deleteByCommodityIds(List.of(updateReqVo.getCommodityId()));
        }

        LogRecordContext.putVariable("commoditySpus", updateReqVo);


        checkNameAndImage(updateReqVo, commoditySpus);

    }

    private List<CommoditySkus>  buildSkuList(CommoditySpusUpdateReqVo updateReqVo, CommoditySpus commoditySpus) {
        List<CommoditySkuSaveVO> commoditySkusList = updateReqVo.getCommoditySkusList();
        if (ObjectUtil.isEmpty(commoditySkusList)) {
            throw exception(BASE_SKU_PRODUCT_MUST_HAVE_SPECIFICATION);
        }
        if (commoditySkusList.size() > 1) {


            List<AfterOrderDO> list = commodityAfterOrderService.selectListBySpuId(updateReqVo.getCommodityId());

            if (ObjectUtil.isNotEmpty(list)) {
                throw exception(BASE_SPU_PRODUCT_HAS_ADD_OPTION_SINGLE_SPEC);
            }
            for (CommoditySkuSaveVO commoditySkus : commoditySkusList) {
                if (ObjectUtil.isEmpty(commoditySkus.getSkusName())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_NAMES_REQUIRED);
                }
                if (ObjectUtil.isEmpty(commoditySkus.getSkusValue())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_VALUES_REQUIRED);
                }
            }
        }

        List<CommoditySkus> skusList = coverSkuEntityList(commoditySkusList);


        skusList.forEach(c -> c.setCommodityId(updateReqVo.getCommodityId()));
        // skusList.forEach(c -> c.setSkuId(null));
        Map<String, Object> result = commoditySkusService.saveBatch(skusList);
        Boolean isChange = (Boolean) result.get("isChange");
        if (!commoditySpus.getIsChange()){
            commoditySpus.setIsChange(isChange);
        }
        Integer skuFlag = (Integer) result.get("skuFlag");
        if (skuFlag == 0){
            commoditySpus.setSkuFlag(skuFlag);
        }


        return skusList;

    }

    private static void buildCondiment(CommoditySpusUpdateReqVo updateReqVo, ObjectMapper objectMapper, CommoditySpus commoditySpus) {
        if (ObjectUtil.isNotEmpty(updateReqVo.getCommodityCondimentsList())) {


            try {
                List<CommodityCondiments> commodityCondimentsList = updateReqVo.getCommodityCondimentsList();
                commodityCondimentsList.forEach(commodityCondiments -> {
                    commodityCondiments.setCondimentId(IdWorker.getId());
                });

                // 将 commodityFlavorList 序列化为 JSON 字符串
                String condiments = objectMapper.writeValueAsString(updateReqVo.getCommodityCondimentsList());
                // 设置到 flavor 属性
                commoditySpus.setCondiments(condiments);
            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION);
            }


        } else {
            commoditySpus.setCondiments(null);
        }
    }

    private  void buildFlavor(CommoditySpusUpdateReqVo updateReqVo, ObjectMapper objectMapper, CommoditySpus commoditySpus) {
        if (ObjectUtil.isNotEmpty(updateReqVo.getCommodityFlavorList())) {

            try {
                List<CommodityFlavor> commodityFlavorList = updateReqVo.getCommodityFlavorList();
                commodityFlavorList.forEach(commodityFlavor -> {
                    commodityFlavor.setFlavorId(IdWorker.getId());
                });

                // 将 commodityFlavorList 序列化为 JSON 字符串
                String flavorJson = objectMapper.writeValueAsString(updateReqVo.getCommodityFlavorList());
                // 设置到 flavor 属性
                commoditySpus.setFlavor(flavorJson);

                commodityGroupSingleService.emitSyncToSubProducts(flavorJson, updateReqVo.getCommodityId());


            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION);
            }




        } else {
            commoditySpus.setFlavor(null);
            commodityGroupSingleService.emitSyncEmptyToSubProducts( updateReqVo.getCommodityId());
        }
    }



    private void buildImage(CommoditySpusUpdateReqVo updateReqVo, CommoditySpus commoditySpus) {
        if (ObjectUtil.isNotEmpty(updateReqVo.getImageUrlVO())) {
            commoditySpus.setImageUrl(ConvertUtil.convertListToStringS(updateReqVo.getImageUrlVO()));
            commodityAfterOrderService.updateThumbnailUrlBySpuId(updateReqVo);

        } else {
            commoditySpus.setImageUrl(null);

            commodityAfterOrderService.updateEmptyThumbnailUrlBySpuId(updateReqVo);

        }
    }

    private void checkSort(CommoditySpusUpdateReqVo updateReqVo) {
        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(CommoditySpus::getCategoryId, updateReqVo.getCategoryId());
        spusLambdaQueryWrapper.orderByAsc(CommoditySpus::getSort);
        List<CommoditySpus> list1 = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(list1)) {
            boolean needUpdate = false;
            Integer targetSort = updateReqVo.getSort();
            for (CommoditySpus spus : list1) {
                if (spus.getSort().equals(targetSort)) {
                    spus.setSort(spus.getSort() + 1);
                    targetSort++;
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                commoditySpusMapper.updateBatch(list1);
            }
        }
    }

    private static void checkDate(CommoditySpusUpdateReqVo updateReqVo) {
        if(ObjectUtil.isNotEmpty(updateReqVo.getStartDate())){
            if (ObjectUtil.isEmpty(updateReqVo.getEndDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
        if (ObjectUtil.isNotEmpty(updateReqVo.getEndDate())){
            if (ObjectUtil.isEmpty(updateReqVo.getStartDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
    }

    private void checkNameAndImage(CommoditySpusUpdateReqVo updateReqVo, CommoditySpus commoditySpus) {
        commodityActivityService.updateCommodityNameAndImage(commoditySpus.getCommodityId(), commoditySpus.getCommodityName(), updateReqVo.getImageUrlVO().get(0));

        commodityAfterOrderService.updateAfterOrderNameAndImage(commoditySpus.getCommodityId(), commoditySpus.getCommodityName(), updateReqVo.getImageUrlVO().get(0));

        couponCommodityApi.updateCommodityName(commoditySpus.getCommodityId(), commoditySpus.getCommodityName());


        //修改连锁商品库子品图片
        if (updateReqVo.getIsSingle().equals(1)) {
            //修改连锁商品库子品图片和名称
            commodityGroupSingleService.changeImageAndName(updateReqVo);
            //修改模板里套餐子品图片和名称

            commodityTemplateGroupSingleService.changeName(updateReqVo);
        }


        //修改模板里套餐图片

        commodityTemplateSpusService.changeSpuNameAndImage(updateReqVo.getCommodityId(), updateReqVo.getCommodityName(), updateReqVo.getImageUrlVO());
    }

    private void checkSetmeal(CommoditySpusUpdateReqVo updateReqVo, CommoditySpus commoditySpus,List<CommoditySkus> skusList) {
        if (updateReqVo.getIsSingle().equals(2)) {

            Integer wxStatus = updateReqVo.getWxStatus();
            Integer storeStatus = updateReqVo.getStoreStatus();


            if (ObjectUtil.isEmpty(updateReqVo.getSetmealType())) {
                throw exception(BASE_SPU_PACKAGE_TYPE_NOT_SELECTED);
            }
            //删除多余分组
            commoditySetmealGroupService.deleteBySpuId(updateReqVo.getCommodityId());
            //删除多余子品
            commodityGroupSingleService.deleteBySpuId(updateReqVo.getCommodityId());




            if (ObjectUtil.isNotEmpty(updateReqVo.getCommodityGroupList())) {
                if (wxStatus==1){
                    wxStatus=  checkGroupListStatus(
                            updateReqVo.getCommodityGroupList(),
                            updateReqVo.getSetmealType(),
                            this::checkWxStatus
                    );
                }


                if (storeStatus==1){
                    storeStatus = checkGroupListStatus(
                            updateReqVo.getCommodityGroupList(),
                            updateReqVo.getSetmealType(),
                            this::checkStoreStatus
                    );

                }



                for (CommodityGroupSaveVO commodityGroupSaveVO : updateReqVo.getCommodityGroupList()) {



                    //分组可选套餐
                    if (updateReqVo.getSetmealType().equals(2) && commodityGroupSaveVO.getAttribute()==1){
                        checkSetmealSingleNum(commodityGroupSaveVO);
                    }


                    CommoditySetmealGroup commoditySetmealGroup = new CommoditySetmealGroup();
                    BeanUtils.copyProperties(commodityGroupSaveVO, commoditySetmealGroup);
                    commoditySetmealGroup.setCommodityId(updateReqVo.getCommodityId());
                    commoditySetmealGroup.setGroupId(null);
                    commoditySetmealGroupService.save(commoditySetmealGroup);
                    if (ObjectUtil.isNotEmpty(commodityGroupSaveVO.getCommoditySingleSaveVOList())) {

                        List<CommoditySingleSaveVO> commoditySingleSaveVOList = commodityGroupSaveVO.getCommoditySingleSaveVOList();
                        if (updateReqVo.getSetmealType().equals(1)) {
                            if (commoditySingleSaveVOList.size() <= 1) {
                                if (commoditySingleSaveVOList.get(0).getCopies()<=1){
                                    throw exception(BASE_SINGLE_PACKAGE_AT_LEAST_TWO_PRODUCTS);
                                }

                            }
                        }

                        List<CommodityGroupSingle> commodityGroupSingleList = coverSingleEntityList(commoditySingleSaveVOList);


                        commodityGroupSingleList.forEach(s -> {
                            s.setGroupId(commoditySetmealGroup.getGroupId());
                        });
                        commodityGroupSingleList.forEach(s -> {
                            s.setSpuId(updateReqVo.getCommodityId());
                        });
                        commodityGroupSingleList.forEach(commodityGroupSingle -> {
                            commodityGroupSingle.setCommodityGroupSingleId(null);
                        });

                        createSynchronizationFlavor(commodityGroupSingleList);

                       /* for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingleList) {
                            LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.eq(CommoditySpus::getCommodityId, commodityGroupSingle.getCommodityId());
                            commoditySpus = commoditySpusMapper.selectOne(queryWrapper);
                            if (ObjectUtil.isNotEmpty(commoditySpus.getFlavor())){
                                commodityGroupSingle.setFlavor(commoditySpus.getFlavor());
                            }
                        }*/



                        commodityGroupSingleService.saveBatch(commodityGroupSingleList);



                    } else {
                        throw exception(BASE_SINGLE_GROUP_NO_PRODUCTS_ADD);
                    }


                }
            } else {
                throw exception(BASE_SINGLE_PLEASE_ADD_PRODUCTS);
            }

            commoditySpus.setWxStatus(wxStatus);
            commoditySpus.setStoreStatus(storeStatus);

        }


        commoditySpusMapper.updateById(commoditySpus);

        LogRecordContext.putVariable("commoditySpus", updateReqVo);



        commodityActivityService.updateCommodityNameAndImage(commoditySpus.getCommodityId(),commoditySpus.getCommodityName(),updateReqVo.getImageUrlVO().get(0));

        commodityAfterOrderService.updateAfterOrderNameAndImage(commoditySpus.getCommodityId(),commoditySpus.getCommodityName(),updateReqVo.getImageUrlVO().get(0));

        couponCommodityApi.updateCommodityName(commoditySpus.getCommodityId(),commoditySpus.getCommodityName());


        ActivityCommodityDTO activityCommodityDTO = new ActivityCommodityDTO();
        activityCommodityDTO.setCommodityId(updateReqVo.getCommodityId());
        activityCommodityDTO.setCommodityName(updateReqVo.getCommodityName());

        List<ActivityCommodityDTO.ActivityCommodityPriceDTO> activityCommodityPriceDTOS  = new ArrayList<>();
        for (CommoditySkus commoditySkus : skusList) {
            ActivityCommodityDTO.ActivityCommodityPriceDTO activityCommodityPriceDTO = new ActivityCommodityDTO.ActivityCommodityPriceDTO();
            activityCommodityPriceDTO.setSkuId(commoditySkus.getSkuId());
            activityCommodityPriceDTO.setCommodityPrice(commoditySkus.getIllustratePrices());
            activityCommodityPriceDTOS.add(activityCommodityPriceDTO);
        }
        activityCommodityDTO.setCommodityPriceList(activityCommodityPriceDTOS);

        seckillApi.updateCommodityInfo(activityCommodityDTO);

        //修改连锁商品库子品图片
        if (updateReqVo.getIsSingle().equals(1)) {
            //修改连锁商品库子品图片和名称
            commodityGroupSingleService.changeImageAndName(updateReqVo);
            //修改模板里套餐子品图片和名称

            commodityTemplateGroupSingleService.changeName(updateReqVo);
        }


        //修改模板里套餐图片

        commodityTemplateSpusService.changeSpuNameAndImage(updateReqVo.getCommodityId(),updateReqVo.getCommodityName(),updateReqVo.getImageUrlVO());



    }

    private void createSynchronizationFlavor(List<CommodityGroupSingle> commodityGroupSingleList) {
// 1. 先收集所有需要查询的商品ID，避免空查询
        List<Long> commodityIds = commodityGroupSingleList.stream()
                .map(CommodityGroupSingle::getCommodityId)
                .filter(Objects::nonNull) // 过滤空ID，避免无效查询
                .distinct() // 去重，减少查询量
                .collect(Collectors.toList());

// 2. 批量查询所有商品的口味信息，存入Map（key：商品ID，value：口味）
        Map<Long, String> commodityFlavorMap = new HashMap<>();
        if (!commodityIds.isEmpty()) {
            LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommoditySpus::getCommodityId, commodityIds)
                    .select(CommoditySpus::getCommodityId, CommoditySpus::getFlavor); // 只查需要的字段，减少数据传输
            List<CommoditySpus> commoditySpusList = commoditySpusMapper.selectList(queryWrapper);

            // 转换为Map，方便后续匹配
            // 关键加固：先过滤null对象，再过滤空口味
            commodityFlavorMap = commoditySpusList.stream()
                    .filter(Objects::nonNull) // 新增：过滤null的CommoditySpus对象，杜绝空指针
                    .filter(spu -> ObjectUtil.isNotEmpty(spu.getFlavor())) // 提前过滤无口味的记录
                    .collect(Collectors.toMap(
                            CommoditySpus::getCommodityId,
                            CommoditySpus::getFlavor,
                            (v1, v2) -> v1 // 避免重复ID导致异常，保留第一个值
                    ));
        }

// 3. 遍历赋值，无需再查数据库
        for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingleList) {
            Long commodityId = commodityGroupSingle.getCommodityId();
            // 关键加固：先判断commodityId不为null，再判断是否包含key
            if (commodityId != null && commodityFlavorMap.containsKey(commodityId)) {
                commodityGroupSingle.setFlavor(commodityFlavorMap.get(commodityId));
            }
        }
    }


    // 检查商品组列表的状态，支持短路操作
    private Integer checkGroupListStatus(
            List<CommodityGroupSaveVO> groupList,
            Integer setmealType,
            BiFunction<CommodityGroupSaveVO, Integer, Integer> statusChecker
    ) {
        int initialStatus = 1;
        for (CommodityGroupSaveVO group : groupList) {
            initialStatus = statusChecker.apply(group, setmealType);
            if (initialStatus == 0) {
                break; // 短路操作：一旦发现状态为0，立即终止循环
            }
        }
        return initialStatus;
    }

    // 检查微信状态（复用之前的优化版本）
    private Integer checkWxStatus(CommodityGroupSaveVO group, Integer setmealType) {
        // 加价组不参与套餐上/下架状态计算，直接视为满足
        if (group.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
            return 1;
        }
        List<CommoditySingleSaveVO> items = group.getCommoditySingleSaveVOList();

        // 如果某个子品的requiredChoose是1并且storeStatus是0，那么也要给这个套餐做下架处理
        boolean hasRequiredChooseDown = items.stream()
                .anyMatch(item -> item.getRequiredChoose() != null
                        && item.getDefaultChoose() == 1
                        && item.getWxStatus() != null
                        && item.getWxStatus() == 0);
        if (hasRequiredChooseDown) {
            return 0;
        }

        if (setmealType == 1 || group.getAttribute() != 1) {
            return checkAllItemsOnline(items, CommoditySingleSaveVO::getWxStatus) ? 1 : 0;
        }

        long onlineCount = items.stream()
                .filter(item -> item.getWxStatus() == 1)
                .count();

        return group.getChooseMany() == 0
                ? (onlineCount >= group.getChoose() ? 1 : 0)
                : (onlineCount >= 1 ? 1 : 0);
    }

    // 检查店铺状态（复用之前的优化版本）
    private Integer checkStoreStatus(CommodityGroupSaveVO group, Integer setmealType) {
        // 加价组不参与套餐上/下架状态计算，直接视为满足
        if (group.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
            return 1;
        }
        List<CommoditySingleSaveVO> items = group.getCommoditySingleSaveVOList();

        // 如果某个子品的requiredChoose是1并且storeStatus是0，那么也要给这个套餐做下架处理
        boolean hasRequiredChooseDown = items.stream()
                .anyMatch(item -> item.getRequiredChoose() != null
                        && item.getRequiredChoose() == 1
                        && item.getStoreStatus() != null
                        && item.getStoreStatus() == 0);
        if (hasRequiredChooseDown) {
            return 0;
        }

        if (setmealType == 1 || group.getAttribute() != 1) {
            return checkAllItemsOnline(items, CommoditySingleSaveVO::getStoreStatus) ? 1 : 0;
        }

        long onlineCount = items.stream()
                .filter(item -> item.getStoreStatus() == 1)
                .count();

        return group.getChooseMany() == 0
                ? (onlineCount >= group.getChoose() ? 1 : 0)
                : (onlineCount >= 1 ? 1 : 0);
    }

    // 通用方法：检查所有商品的指定状态是否都为1
    private boolean checkAllItemsOnline(
            List<CommoditySingleSaveVO> items,
            Function<CommoditySingleSaveVO, Integer> statusExtractor
    ) {
        return items.stream().allMatch(item -> statusExtractor.apply(item) == 1);
    }


    private void checkSetmealSingleNum(CommodityGroupSaveVO commodityGroupSaveVO) {
        Long choose = commodityGroupSaveVO.getChoose();

        List<CommoditySingleSaveVO> commoditySingleSaveVOList = commodityGroupSaveVO.getCommoditySingleSaveVOList();


        Integer defaultChooseNum = 0;
        Integer requiredChooseNum = 0;
        for (CommoditySingleSaveVO commoditySingleSaveVO : commoditySingleSaveVOList) {
            if (commoditySingleSaveVO.getDefaultChoose()==1){
                defaultChooseNum++;
            }
            if (commoditySingleSaveVO.getRequiredChoose()==1){
                requiredChooseNum++;
            }
        }

        if (choose<defaultChooseNum | choose<requiredChooseNum) {
            throw exception(BASE_SPU_PRODUCT_NUM_SMAIL);
        }


    }


    @Override
    public List<CategoryRespVo> getCategoryListV3(CommodityTypeReqVo typeReqVo) {
        if (ObjectUtil.isEmpty(typeReqVo.getSelectView())) {
            throw exception(BASE_SPU_QUERY_CONDITION_NOT_PASSED);
        }
        List<CategoryRespVo> categoryList = new ArrayList<>();
        switch (typeReqVo.getSelectView()) {
            case 1: {
                categoryList = getCategoryListS1();
                break;
            }
            case 2, 3, 4: {
                categoryList = selectDownSpuV3(typeReqVo.getSelectView());
                break;
            }


        }
        categoryList.sort(Comparator.comparingInt(category ->
                Integer.valueOf(1).equals(category.getIsHidden()) ? 1 : 0));
        if(!categoryList.isEmpty()){

            return categoryList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<CategoryRespVo>  selectDownSpuV3(Integer isDown) {

        List<CategoryRespVo> categoryRespVoList = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (isDown.equals(3)) {
            spusLambdaQueryWrapper.eq(CommoditySpus::getIsHidden, 0);
            spusLambdaQueryWrapper.eq(CommoditySpus::getWxStatus, 0);
            spusLambdaQueryWrapper.eq(CommoditySpus::getStoreStatus, 0);
        } else if (isDown.equals(2)) {
            spusLambdaQueryWrapper.eq(CommoditySpus::getIsHidden, 0);
            spusLambdaQueryWrapper.and(wrapper -> wrapper
                    .eq(CommoditySpus::getWxStatus, 1)
                    .or()
                    .eq(CommoditySpus::getStoreStatus, 1));
        } else if (isDown.equals(4)) {
            spusLambdaQueryWrapper.eq(CommoditySpus::getIsHidden, 1);
        }

        List<CommoditySpus> spusList = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
        if (ObjectUtil.isEmpty(spusList)) {
            return new ArrayList<>();
        }

        List<Long> spuIds = spusList.stream().map(CommoditySpus::getCommodityId).toList();

        List<Long> categoryIds = spusList.stream().map(CommoditySpus::getCategoryId).distinct().toList();

        List<CommodityCategory> categoryList = commodityCategoryService.selectListByIdsAndAsc(categoryIds);
        if (isDown.equals(2) || isDown.equals(3)) {
            categoryList = categoryList.stream()
                    .filter(category -> !Integer.valueOf(1).equals(category.getIsHidden()))
                    .toList();
        }

        if (ObjectUtil.isEmpty(categoryList)){
            return categoryRespVoList;
        }


        List<CommoditySkus> skusList =  commoditySkusService.selectBySpuIds(spuIds);

        Map<Long, List<CommoditySkus>> skuMap = skusList.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));


        List<CommoditySpusByIdRespVo> spusByIdRespVos = new ArrayList<>();
        for (CommoditySpus commoditySpus : spusList) {
            CommoditySpusByIdRespVo commoditySpusByIdRespVo = new CommoditySpusByIdRespVo();
            BeanUtils.copyProperties(commoditySpus, commoditySpusByIdRespVo);
            List<CommoditySkus> skusList1 = skuMap.get(commoditySpus.getCommodityId());
            List<CommoditySkuRespVO> skuRespVOList = BeanUtils.toBean(skusList1, CommoditySkuRespVO.class);

            commoditySpusByIdRespVo.setCommoditySkuRespVOList(skuRespVOList);

            if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
                commoditySpusByIdRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
            }

            if (skusList1.size() > 1) {
                PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPrices(skusList1);
                commoditySpusByIdRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                commoditySpusByIdRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                commoditySpusByIdRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                commoditySpusByIdRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                commoditySpusByIdRespVo.setIsMoreSku(true);
            } else {
                commoditySpusByIdRespVo.setLowPrice(skusList1.get(0).getIllustratePrices());
                commoditySpusByIdRespVo.setLowStrikePrice(skusList1.get(0).getStrikeThroughPrice());
                commoditySpusByIdRespVo.setIsMoreSku(false);
            }
            spusByIdRespVos.add(commoditySpusByIdRespVo);
        }


        Map<Long, List<CommoditySpusByIdRespVo>> spuMap = spusByIdRespVos.stream().collect(Collectors.groupingBy(CommoditySpusByIdRespVo::getCategoryId));

        for (CommodityCategory commodityCategory : categoryList) {
            CategoryRespVo categoryRespVo = new CategoryRespVo();
            BeanUtils.copyProperties(commodityCategory, categoryRespVo);
            List<CommoditySpusByIdRespVo> spusList1 = spuMap.get(commodityCategory.getId());
            categoryRespVo.setCommoditySpusList(spusList1);
            categoryRespVo.setCommodityNum(spusList1.size());
            categoryRespVoList.add(categoryRespVo);
        }
        if (ObjectUtil.isNotEmpty(categoryRespVoList)) {
            DateTimeSortUtil.sortDateTime(categoryRespVoList);


            List<CategoryRespVo> categoryListUp = new ArrayList<>();
            List<CategoryRespVo> categoryListDown = new ArrayList<>();
            //如果不在分时置顶时间段内，不显示分时置顶图标
            for (CategoryRespVo category : categoryRespVoList) {
                if (!category.getIsUp()) {
                    category.setTimeSharingTopping(2);
                    categoryListDown.add(category);
                } else {
                    categoryListUp.add(category);
                }

            }


            categoryListUp.addAll(categoryListDown);

            categoryRespVoList = categoryListUp;

        }


        return categoryRespVoList;
    }

    private List<CategoryRespVo> getCategoryListS1() {

        List<CategoryRespVo> categoryRespVoList = new ArrayList<>();

        List<CommodityCategory> list = commodityCategoryService.selectAllListByAsc();


        if (ObjectUtil.isNotEmpty(list)) {
            List<Long> categoryIds = list.stream().map(CommodityCategory::getId).collect(Collectors.toList());
            LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spusLambdaQueryWrapper.in(CommoditySpus::getCategoryId, categoryIds);

            List<CommoditySpus> commoditySpusList = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
            if (ObjectUtil.isNotEmpty(commoditySpusList)) {
                Map<Long, List<CommoditySpus>> collect = commoditySpusList.stream().collect(Collectors.groupingBy(CommoditySpus::getCategoryId));
                for (CommodityCategory category : list) {
                    CategoryRespVo categoryRespVo = new CategoryRespVo();
                    BeanUtils.copyProperties(category, categoryRespVo);
                    List<CommoditySpus> spusList = collect.get(category.getId());
                    if (ObjectUtil.isNotEmpty(spusList)) {
                        categoryRespVo.setCommodityNum(spusList.size());
                    } else {
                        categoryRespVo.setCommodityNum(0);
                    }
                    categoryRespVoList.add(categoryRespVo);
                }
            } else {
                for (CommodityCategory category : list) {
                    CategoryRespVo categoryRespVo = new CategoryRespVo();
                    BeanUtils.copyProperties(category, categoryRespVo);
                    categoryRespVo.setCommodityNum(0);
                    categoryRespVoList.add(categoryRespVo);
                }
            }

            DateTimeSortUtil.sortDateTime(categoryRespVoList);


            List<CategoryRespVo> categoryListUp = new ArrayList<>();
            List<CategoryRespVo> categoryListDown = new ArrayList<>();
            //如果不在分时置顶时间段内，不显示分时置顶图标
            for (CategoryRespVo category : categoryRespVoList) {
                if (!category.getIsUp()) {
                    category.setTimeSharingTopping(2);
                    categoryListDown.add(category);
                } else {
                    categoryListUp.add(category);
                }

            }


            categoryListUp.addAll(categoryListDown);


            categoryRespVoList = categoryListUp;
        }
        return categoryRespVoList;
    }


    @Override
    public List<CommodityCateDateRespVo> getSpuListV3(CommoditySpusQueryReqVo queryReqVo) {
        List<CommodityCateDateRespVo> commodityCateDateRespVoList = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(queryReqVo.getCategoryId()),CommoditySpus::getCategoryId, queryReqVo.getCategoryId());

        spusLambdaQueryWrapper.like(StringUtils.isNotBlank(queryReqVo.getCommodityName()),CommoditySpus::getCommodityName,queryReqVo.getCommodityName());



        if (ObjectUtil.isNotEmpty(queryReqVo.getSelectView())) {
            //查询上架的
            if (queryReqVo.getSelectView().equals(2)) {
                spusLambdaQueryWrapper.and(wrapper -> wrapper
                        .eq(CommoditySpus::getWxStatus, 1)
                        .or()
                        .eq(CommoditySpus::getStoreStatus, 1));
            } else if (queryReqVo.getSelectView().equals(3)) {
                spusLambdaQueryWrapper.eq(CommoditySpus::getWxStatus, 0);
                spusLambdaQueryWrapper.eq(CommoditySpus::getStoreStatus, 0);
            }
        }

        //spusLambdaQueryWrapper.orderByAsc(CommoditySpus::getSort);
        List<CommoditySpus> commoditySpuses = commoditySpusMapper.selectList(spusLambdaQueryWrapper);


        List<CommodityTagReqVO> commodityTagReqVOS = commodityTageService.selectList(new CommodityTagReqVO());

        Map<Long, CommodityTagReqVO> tagMap = commodityTagReqVOS.stream().collect(Collectors.toMap(CommodityTagReqVO::getId, c -> c));

        if (ObjectUtil.isNotEmpty(commoditySpuses)) {


            List<Long> commodityIds = commoditySpuses.stream().map(CommoditySpus::getCommodityId).collect(Collectors.toList());

            List<CommoditySkus> commoditySkuses = commoditySkusService.selectBySpuIds(commodityIds);

            if (ObjectUtil.isNotEmpty(commoditySkuses)) {
                Map<Long, List<CommoditySkus>> collect = commoditySkuses.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));
                for (CommoditySpus commoditySpus : commoditySpuses) {
                    CommodityCateDateRespVo cateDateRespVo = new CommodityCateDateRespVo();
                    BeanUtils.copyProperties(commoditySpus, cateDateRespVo);
                    List<CommoditySkus> commoditySkuses1 = collect.get(commoditySpus.getCommodityId());

                    if (ObjectUtil.isNotEmpty(commoditySpus.getTagIds())){
                        List<Long> longs = ConvertUtil.convertStringToList(commoditySpus.getTagIds());
                        List<CommodityTag> commodityTagList = new ArrayList<>();
                        for (Long aLong : longs) {
                            CommodityTag commodityTag = new CommodityTag();
                            CommodityTagReqVO commodityTagReqVO = tagMap.get(aLong);
                            BeanUtils.copyProperties(commodityTagReqVO,commodityTag);
                            commodityTagList.add(commodityTag);
                        }
                        cateDateRespVo.setCommodityTagReqVOList(commodityTagList);
                    }
                    if (commoditySkuses1.size() > 1) {
                        cateDateRespVo.setIsMoreSku(true);

                        PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPrices(commoditySkuses1);
                        cateDateRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                        cateDateRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                        cateDateRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                        cateDateRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());


                    } else {
                        cateDateRespVo.setIsMoreSku(false);
                        cateDateRespVo.setLowPrice(commoditySkuses1.get(0).getIllustratePrices());
                        cateDateRespVo.setLowStrikePrice(commoditySkuses1.get(0).getStrikeThroughPrice());
                    }


                    if (ObjectUtil.isNotEmpty(commoditySkuses1)) {
                        cateDateRespVo.setCommoditySkusList(commoditySkuses1);
                    }




                    if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
                        cateDateRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                    }

                    commodityCateDateRespVoList.add(cateDateRespVo);
                }
            }

        }

        Collections.sort(commodityCateDateRespVoList, new Comparator<CommodityCateDateRespVo>() {
            @Override
            public int compare(CommodityCateDateRespVo o1, CommodityCateDateRespVo o2) {
                return o1.getSort().compareTo(o2.getSort());
            }
        });
        return commodityCateDateRespVoList;

    }

    @Override
    public CommoditySpusByIdRespVo getSpuByIdV3(Long id) {
        CommoditySpus commoditySpus = commoditySpusMapper.selectById(id);
        CommoditySpusByIdRespVo commoditySpusSaveReqVo = new CommoditySpusByIdRespVo();
        BeanUtils.copyProperties(commoditySpus,commoditySpusSaveReqVo);


        ObjectMapper objectMapper = new ObjectMapper();

        if (ObjectUtil.isNotEmpty(commoditySpus.getTagIds())){
            List<Long> tagIds = ConvertUtil.convertStringToList(commoditySpus.getTagIds());
           List<CommodityTag> commodityTags =  commodityTageService.selectListByIds(tagIds);
           Map<Long, CommodityTag> tagMap = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, tag -> tag));
            for (Long tagId : tagIds) {
                CommodityTag commodityTag = tagMap.get(tagId);
                if (ObjectUtil.isNotEmpty(commodityTag)) {
                    commoditySpusSaveReqVo.getCommodityTagList().add(commodityTag);
                }
            }



        }
        if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
            commoditySpusSaveReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
        }


        List<CommoditySkus> list =commoditySkusService.selectBySpuId(id);

        List<CommoditySkuRespVO> skuRespVOList = BeanUtils.toBean(list, CommoditySkuRespVO.class);

        commoditySpusSaveReqVo.setCommoditySkuRespVOList(skuRespVOList);
        if (ObjectUtil.isNotEmpty(commoditySpus.getFlavor())) {
            try {
                // 进行反序列化操作
                List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(commoditySpus.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                });
                // 将反序列化后的列表设置回 commoditySpus 对象
                commoditySpusSaveReqVo.setCommodityFlavorList(commodityFlavorList);
            } catch (IOException e) {
                // 处理反序列化异常
                System.out.println(e.getMessage());
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
            }
        }

        if (ObjectUtil.isNotEmpty(commoditySpus.getCondiments())) {
            try {
                List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(commoditySpus.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                });
                commoditySpusSaveReqVo.setCommodityCondimentsList(commodityCondimentsList);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
            }
        }
        if (commoditySpusSaveReqVo.getIsSingle().equals(2)) {


            List<CommoditySetmealGroup> groupList = commoditySetmealGroupService.selectBySpuId(commoditySpusSaveReqVo.getCommodityId());

            List<CommodityGroupRespVO> groupRespVOS = BeanUtils.toBean(groupList, CommodityGroupRespVO.class);


            List<CommodityGroupSingle> singleList = commodityGroupSingleService.selectBySpuId(commoditySpusSaveReqVo.getCommodityId());

            List<CommoditySingleRespVO> singleRespVOS = BeanUtils.toBean(singleList, CommoditySingleRespVO.class);

            Map<Long, List<CommoditySingleRespVO>> singleMap = singleRespVOS.stream().collect(Collectors.groupingBy(CommoditySingleRespVO::getGroupId));

            if (ObjectUtil.isNotEmpty(groupList)) {

                for (CommodityGroupRespVO group : groupRespVOS) {
                    List<CommoditySingleRespVO> singleRespVOList = singleMap.get(group.getGroupId());
                    if (ObjectUtil.isNotEmpty(singleRespVOList)) {
                        group.setCommoditySingles(singleRespVOList);
                    }
                }
                commoditySpusSaveReqVo.setCommodityGroupRespVOList(groupRespVOS);
            }


        }

        return commoditySpusSaveReqVo;
    }


    @Override
    public void deleteSpuByIdV3(Long id) {

        CommonResult<Boolean> b = couponCommodityApi.selectCouponHaveCommodity(id);
        if (b.getData()){
            throw exception(BASE_SPU_PRODUCT_USED_BY_COUPON_CANNOT_DELETE);
        }


        List<AfterOrderDO> list =  commodityAfterOrderService.selectListBySpuId(id);

        if (ObjectUtil.isNotEmpty(list)) {
            throw exception(BASE_SPU_PRODUCT_ALREADY_ADDED_CANNOT_DELETE);
        }



        List<CommodityGroupSingle> singleList = commodityGroupSingleService.selectBySpuId(id);

        if (ObjectUtil.isNotEmpty(singleList)) {
            throw exception(BASE_SPU_PRODUCT_USED_BY_PACKAGE_CANNOT_DELETE);
        }


        commoditySpusMapper.deleteById(id);

        commoditySetmealGroupService.deleteBySpuId(id);

        commodityGroupSingleService.deleteBySpuId(id);
    }




    @Override
    public List<CategoryRespVo> fuzzySearchCommodityV3(CommoditySpusSearchReqVo searchReqVo) {
        if (ObjectUtil.isEmpty(searchReqVo.getCommodityName())) {
            return fuzzySearchCommodityV3haveNot();
        } else {
            return fuzzySearchCommodityV3have(searchReqVo);
        }
    }

    private List<CategoryRespVo> fuzzySearchCommodityV3have(CommoditySpusSearchReqVo searchReqVo) {
        List<CategoryRespVo> categoryRespVos = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> commoditySpusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commoditySpusLambdaQueryWrapper.like(CommoditySpus::getCommodityName, searchReqVo.getCommodityName());
        commoditySpusLambdaQueryWrapper.eq(CommoditySpus::getIsHidden, 0);
        List<CommoditySpus> spusList = commoditySpusMapper.selectList(commoditySpusLambdaQueryWrapper);
        if (ObjectUtil.isEmpty(spusList)) {
            return new ArrayList<>();
        }
        List<Long> commodityIds = spusList.stream().map(CommoditySpus::getCommodityId).collect(Collectors.toList());


        List<CommoditySkus> skusList =commoditySkusService.selectBySpuIds(commodityIds);

        Map<Long, List<CommoditySkus>> skusGroup = skusList.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));


        List<Long> categoryIdList = spusList.stream().map(CommoditySpus::getCategoryId).distinct().collect(Collectors.toList());
        Map<Long, List<CommoditySpus>> collect = spusList.stream().collect(Collectors.groupingBy(CommoditySpus::getCategoryId));


        List<CommodityCategory> listComm =commodityCategoryService.selectListByIdsAndAsc(categoryIdList);

        List<CommodityTagReqVO> commodityTagReqVOS = commodityTageService.selectList(new CommodityTagReqVO());

        Map<Long, CommodityTagReqVO> tagMap = commodityTagReqVOS.stream().collect(Collectors.toMap(CommodityTagReqVO::getId, c -> c));

        ObjectMapper objectMapper = new ObjectMapper();
        for (CommodityCategory commodityCategory : listComm) {
            CategoryRespVo categoryRespVo = new CategoryRespVo();
            BeanUtils.copyProperties(commodityCategory, categoryRespVo);
            List<CommoditySpus> spusList1 = collect.get(commodityCategory.getId());
            categoryRespVo.setCommodityNum(spusList1.size());
            List<CommoditySpusByIdRespVo> spusByIdRespVos = new ArrayList<>();
             for (CommoditySpus spus : spusList1) {
                CommoditySpusByIdRespVo commoditySpusByIdRespVo = new CommoditySpusByIdRespVo();
                BeanUtils.copyProperties(spus, commoditySpusByIdRespVo);
                List<CommoditySkus> skusList1 = skusGroup.get(spus.getCommodityId());
                List<CommoditySkuRespVO> skuRespVOList = BeanUtils.toBean(skusList1, CommoditySkuRespVO.class);


                commoditySpusByIdRespVo.setCommoditySkuRespVOList(skuRespVOList);


                 if (ObjectUtil.isNotEmpty(spus.getFlavor())) {
                     try {
                         // 进行反序列化操作
                         List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(spus.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                         });
                         // 将反序列化后的列表设置回 commoditySpus 对象
                         commoditySpusByIdRespVo.setCommodityFlavorList(commodityFlavorList);
                     } catch (IOException e) {
                         // 处理反序列化异常
                         System.out.println(e.getMessage());
                         throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
                     }
                 }

                 if (ObjectUtil.isNotEmpty(spus.getCondiments())) {
                     try {
                         List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(spus.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                         });
                         commoditySpusByIdRespVo.setCommodityCondimentsList(commodityCondimentsList);
                     } catch (IOException e) {
                         System.out.println(e.getMessage());
                         throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
                     }
                 }

                 if (ObjectUtil.isNotEmpty(spus.getTagIds())){
                     List<Long> longs = ConvertUtil.convertStringToList(spus.getTagIds());
                     List<CommodityTag> commodityTagList = new ArrayList<>();
                     for (Long aLong : longs) {
                         CommodityTag commodityTag = new CommodityTag();
                         CommodityTagReqVO commodityTagReqVO = tagMap.get(aLong);
                         BeanUtils.copyProperties(commodityTagReqVO,commodityTag);
                         commodityTagList.add(commodityTag);
                     }
                     commoditySpusByIdRespVo.setCommodityTagList(commodityTagList);
                 }



                if (ObjectUtil.isNotEmpty(spus.getImageUrl())) {
                    commoditySpusByIdRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(spus.getImageUrl()));
                }

                if (skusList1.size() > 1) {
                    PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPrices(skusList1);
                    commoditySpusByIdRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                    commoditySpusByIdRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                    commoditySpusByIdRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                    commoditySpusByIdRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                    commoditySpusByIdRespVo.setIsMoreSku(true);

                } else {
                    commoditySpusByIdRespVo.setLowPrice(skusList1.get(0).getIllustratePrices());
                    commoditySpusByIdRespVo.setLowStrikePrice(skusList1.get(0).getStrikeThroughPrice());
                    commoditySpusByIdRespVo.setIsMoreSku(false);

                }
                 spusByIdRespVos.add(commoditySpusByIdRespVo);

            }
            categoryRespVo.setCommoditySpusList(spusByIdRespVos);
            categoryRespVos.add(categoryRespVo);
        }

        return categoryRespVos;
    }

    private List<CategoryRespVo> fuzzySearchCommodityV3haveNot() {

        List<CategoryRespVo> categoryRespVos = new ArrayList<>();

        List<CommodityCategory> categoryList = commodityCategoryService.selectListByAsc();
        if (ObjectUtil.isEmpty(categoryList)) {
            return categoryRespVos;
        }
        List<Long> categoryIds = categoryList.stream().map(CommodityCategory::getId).toList();

        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();

        spusLambdaQueryWrapper.in(CommoditySpus::getCategoryId, categoryIds);
        List<CommoditySpus> spusList = commoditySpusMapper.selectList(spusLambdaQueryWrapper);

        Map<Long, List<CommoditySpusByIdRespVo>> spuMap = new HashMap<>();


        if (ObjectUtil.isNotEmpty(spusList)) {
            List<Long> spusIds = spusList.stream().map(CommoditySpus::getCommodityId).toList();


            List<CommoditySkus> skuList =commoditySkusService.selectBySpuIds(spusIds);

            Map<Long, List<CommoditySkus>> skuMap = skuList.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));

            List<CommoditySpusByIdRespVo> commoditySpusByIdRespVoList = new ArrayList<>();

            List<CommodityTagReqVO> commodityTagReqVOS = commodityTageService.selectList(new CommodityTagReqVO());

            Map<Long, CommodityTagReqVO> tagMap = commodityTagReqVOS.stream().collect(Collectors.toMap(CommodityTagReqVO::getId, c -> c));

            ObjectMapper objectMapper = new ObjectMapper();
            for (CommoditySpus commoditySpus : spusList) {
                CommoditySpusByIdRespVo commoditySpusByIdRespVo = new CommoditySpusByIdRespVo();
                BeanUtils.copyProperties(commoditySpus, commoditySpusByIdRespVo);
                if (ObjectUtil.isNotEmpty(commoditySpus.getFlavor())) {
                    try {
                        // 进行反序列化操作
                        List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(commoditySpus.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                        });
                        // 将反序列化后的列表设置回 commoditySpus 对象
                        commoditySpusByIdRespVo.setCommodityFlavorList(commodityFlavorList);
                    } catch (IOException e) {
                        // 处理反序列化异常
                        System.out.println(e.getMessage());
                        throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
                    }
                }
                if (ObjectUtil.isNotEmpty(commoditySpus.getTagIds())){
                    List<Long> longs = ConvertUtil.convertStringToList(commoditySpus.getTagIds());
                    List<CommodityTag> commodityTagList = new ArrayList<>();
                    for (Long aLong : longs) {
                        CommodityTag commodityTag = new CommodityTag();
                        CommodityTagReqVO commodityTagReqVO = tagMap.get(aLong);
                        BeanUtils.copyProperties(commodityTagReqVO,commodityTag);
                        commodityTagList.add(commodityTag);
                    }
                    commoditySpusByIdRespVo.setCommodityTagList(commodityTagList);
                }


                if (ObjectUtil.isNotEmpty(commoditySpus.getCondiments())) {
                    try {
                        List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(commoditySpus.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                        });
                        commoditySpusByIdRespVo.setCommodityCondimentsList(commodityCondimentsList);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
                    }
                }

                List<CommoditySkus> skusList = skuMap.get(commoditySpus.getCommodityId());
                List<CommoditySkuRespVO> skuRespVOList = BeanUtils.toBean(skusList, CommoditySkuRespVO.class);

                if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
                    commoditySpusByIdRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                }

                if (skusList.size() > 1) {
                    PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPrices(skusList);
                    commoditySpusByIdRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                    commoditySpusByIdRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                    commoditySpusByIdRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                    commoditySpusByIdRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                    commoditySpusByIdRespVo.setIsMoreSku(true);
                    commoditySpusByIdRespVo.setCommoditySkuRespVOList(skuRespVOList);
                } else {
                    commoditySpusByIdRespVo.setLowPrice(skusList.get(0).getIllustratePrices());
                    commoditySpusByIdRespVo.setLowStrikePrice(skusList.get(0).getStrikeThroughPrice());
                    commoditySpusByIdRespVo.setIsMoreSku(false);
                    commoditySpusByIdRespVo.setCommoditySkuRespVOList(skuRespVOList);
                }
                commoditySpusByIdRespVoList.add(commoditySpusByIdRespVo);
            }


            spuMap = commoditySpusByIdRespVoList.stream().collect(Collectors.groupingBy(CommoditySpusByIdRespVo::getCategoryId));











        }

        DateTimeSortUtil.sortDateTime(categoryList);

        List<CategoryRespVo> categoryListUp = new ArrayList<>();
        List<CategoryRespVo> categoryListDown = new ArrayList<>();
        //如果不在分时置顶时间段内，不显示分时置顶图标

        for (CommodityCategory category : categoryList) {
            CategoryRespVo categoryRespVo = new CategoryRespVo();
            BeanUtils.copyProperties(category, categoryRespVo);
            List<CommoditySpusByIdRespVo> commoditySpusByIdRespVoList1 = spuMap.get(category.getId());
            if (ObjectUtil.isNotEmpty(commoditySpusByIdRespVoList1)) {
                categoryRespVo.setCommodityNum(commoditySpusByIdRespVoList1.size());
                categoryRespVo.setCommoditySpusList(commoditySpusByIdRespVoList1);
            } else {
                categoryRespVo.setCommodityNum(0);
            }

            if (!categoryRespVo.getIsUp()) {
                categoryRespVo.setTimeSharingTopping(2);
                categoryListDown.add(categoryRespVo);
            } else {
                categoryListUp.add(categoryRespVo);
            }



        }


        categoryListUp.addAll(categoryListDown);
        categoryRespVos = BeanCopyUtils.copyBeanList(categoryListUp, CategoryRespVo.class);

        return categoryRespVos;

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CommoditySpus> batchModificationV3(CommodityBatchEditReqVo batchEditReqVo) {
        if (ObjectUtil.isEmpty(batchEditReqVo.getSortingRules())) {
            throw exception(BASE_SPU_MODIFY_RULE_NOT_PASSED);
        }
        if (ObjectUtil.isEmpty(batchEditReqVo.getCommodityIds())) {
            throw exception(BASE_SPU_SPU_ID_NOT_PASSED);
        }
        List<CommoditySpus>  spusList = new ArrayList<>();
        switch (batchEditReqVo.getSortingRules()) {
            //批量上架商品
            case 1:
                spusList =  batchUp(batchEditReqVo);

                break;
            //批量下架商品
            case 2:
                batchDown(batchEditReqVo);

                break;
            //批量删除商品
            case 3:
                batchDel(batchEditReqVo);

                break;
            //批量修改分类
            case 4:
                if (ObjectUtil.isEmpty(batchEditReqVo.getCategoryId())) {
                    throw exception(BASE_CATE_CATEGORY_ID_NOT_PASSED);
                }
                if (ObjectUtil.isEmpty(batchEditReqVo.getCategoryName())) {
                    throw exception(BASE_CATE_CATEGORY_NAME_NOT_PASSED);
                }
                batchChangeCategory(batchEditReqVo);
                break;
            //批量修改可售时间
            case 5:
                batchChangeShowTime(batchEditReqVo);
                break;
        }
        return spusList;
    }

    private void batchChangeShowTime(CommodityBatchEditReqVo batchEditReqVo) {
        List<Long> commodityIds = batchEditReqVo.getCommodityIds();
        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        if (ObjectUtil.isEmpty(batchEditReqVo.getIsAllDay())) {
            throw exception(BASE_SPU_IS_ALL_DAY_NOT_PASSED);
        } else {

            spusLambdaUpdateWrapper.set(CommoditySpus::getIsAllDay, batchEditReqVo.getIsAllDay());
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getStartDate())) {
                spusLambdaUpdateWrapper.set(CommoditySpus::getStartDate, batchEditReqVo.getStartDate());
            } else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getStartDate, null);
            }
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getEndDate())) {
                spusLambdaUpdateWrapper.set(CommoditySpus::getEndDate, batchEditReqVo.getEndDate());
            } else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getEndDate, null);
            }
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getDayNumbers())) {
                spusLambdaUpdateWrapper.set(CommoditySpus::getDayNumbers, batchEditReqVo.getDayNumbers());
            } else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getDayNumbers, null);
            }
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getWeekNumbers())) {
                spusLambdaUpdateWrapper.set(CommoditySpus::getWeekNumbers, batchEditReqVo.getWeekNumbers());
            } else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getWeekNumbers, null);
            }
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getTimeRange())) {
                spusLambdaUpdateWrapper.set(CommoditySpus::getTimeRange, batchEditReqVo.getTimeRange());
            } else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getTimeRange, null);
            }
            if (ObjectUtil.isNotEmpty(batchEditReqVo.getTimeSharingTopping())){
                spusLambdaUpdateWrapper.set(CommoditySpus::getTimeSharingTopping, batchEditReqVo.getTimeSharingTopping());
            }else {
                spusLambdaUpdateWrapper.set(CommoditySpus::getTimeSharingTopping, null);
            }
            commoditySpusMapper.update(spusLambdaUpdateWrapper);

        }
    }

    private void batchChangeCategory(CommodityBatchEditReqVo batchEditReqVo) {
        List<Long> commodityIds = batchEditReqVo.getCommodityIds();
        CommodityCategory category = commodityCategoryService.getById(batchEditReqVo.getCategoryId());

        Integer categoryHidden = category != null && category.getIsHidden() != null
                ? category.getIsHidden()
                : 0;

        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        spusLambdaUpdateWrapper.set(CommoditySpus::getCategoryName, batchEditReqVo.getCategoryName());
        spusLambdaUpdateWrapper.set(CommoditySpus::getCategoryId, batchEditReqVo.getCategoryId());
        spusLambdaUpdateWrapper.set(CommoditySpus::getIsHidden, categoryHidden);
        commoditySpusMapper.update(spusLambdaUpdateWrapper);

        if (Integer.valueOf(1).equals(categoryHidden)) {
            commodityAfterOrderService.deleteByCommodityIds(commodityIds);
        }
    }

    private void batchDel(CommodityBatchEditReqVo batchEditReqVo) {
        List<Long> commodityIds = batchEditReqVo.getCommodityIds();

        List<AfterOrderDO> list = commodityAfterOrderService.selectListBySpuIds(commodityIds);
        if (ObjectUtil.isNotEmpty(list)) {
            throw exception(BASE_SPU_PRODUCT_ALREADY_ADDED_CANNOT_DELETE);
        }



        List<CommodityGroupSingle> list1 =commodityGroupSingleService.selectBySpuIds(commodityIds);

        if (ObjectUtil.isNotEmpty(list1)) {
            throw exception(BASE_SPU_PRODUCT_USED_BY_PACKAGE_CANNOT_DELETE);
        }



        LambdaQueryWrapper<CommoditySpus> commoditySpusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commoditySpusLambdaQueryWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        commoditySpusMapper.delete(commoditySpusLambdaQueryWrapper);



        commoditySetmealGroupService.deleteBySpuIds(commodityIds);



        commodityGroupSingleService.deleteBySpuIds(commodityIds);

    }

    private void batchDown(CommodityBatchEditReqVo batchEditReqVo) {
        List<Long> commodityIds = batchEditReqVo.getCommodityIds();
        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus, 0);
        spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus, 0);
        commoditySpusMapper.update(spusLambdaUpdateWrapper);



        commodityGroupSingleService.updateDownBySpuIds(commodityIds);


       List<CommodityGroupSingle> singleList =  commodityGroupSingleService.selectBySpuOriginalIds(commodityIds);

       if (ObjectUtil.isNotEmpty(singleList)) {

           checkSetmealStatusV3(singleList,commodityIds);


       }





    }

    private void checkSetmealStatusV3(List<CommodityGroupSingle> singleList,List<Long> commodityIds) {

        List<Long> setMealIds = singleList.stream().map(CommodityGroupSingle::getSpuId).toList();

        Map<Long, List<CommodityGroupSingle>> singleMap = singleList.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));

        LambdaQueryWrapper<CommoditySpus> commoditySpusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commoditySpusLambdaQueryWrapper.in(CommoditySpus::getCommodityId, setMealIds);
        List<CommoditySpus> commoditySpuses = commoditySpusMapper.selectList(commoditySpusLambdaQueryWrapper);

        List<CommoditySetmealGroup> commoditySetmealGroups = commoditySetmealGroupService.selectBySpuIds(setMealIds);

        Map<Long, List<CommoditySetmealGroup>> groupMap = commoditySetmealGroups.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));


        List<Long>  fixedCollocation = new ArrayList<>();
        List<Long> troubleshoot = new ArrayList<>();
        for (CommoditySpus commoditySpus : commoditySpuses) {
            //1是固定搭配 2是分组可选
            if (commoditySpus.getSetmealType().equals(2)){
                troubleshoot.add(commoditySpus.getCommodityId());
            }else {
                fixedCollocation.add(commoditySpus.getCommodityId());
            }
        }
        //排除下架的套餐 id 集合
        Set<Long> exclusion = new HashSet<>();
        //需要下架的
        Set<Long> exclusionNeed = new HashSet<>();

        for (CommoditySpus commoditySpus : commoditySpuses) {


            List<CommoditySetmealGroup> commoditySetmealGroups1 = groupMap.get(commoditySpus.getCommodityId());

            for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroups1) {
                List<CommodityGroupSingle> singleList1 = singleMap.get(commoditySetmealGroup.getGroupId());
                if (ObjectUtil.isEmpty(singleList1)) {
                    continue;
                }

                // 获取 singleList 中所有商品的 ID 列表
                List<Long> commodityOriginalIds = singleList1.stream()
                        .map(CommodityGroupSingle::getCommodityId)
                        .toList();

                // 检查商品 ID 是否在列表中
                // 假设 commodityIds 是另一个集合
                if (Collections.disjoint(commodityOriginalIds, commodityIds)) {
                    continue; // 没有交集时跳过
                }
                if (commoditySetmealGroup.getAttribute()==1){
                    //如果是可选商品
                    // 计算未下架商品的数量
                    int downCount = calculateDownCount(singleList1);

                    if (commoditySetmealGroup.getChooseMany()==0){
                        //同一商品不可多选
                        //如果任选数量大于上架的单品数量
                        if (commoditySetmealGroup.getChoose()>=(singleList1.size()-downCount) ){
                            exclusion.add(commoditySetmealGroup.getCommodityId());
                            continue;
                        }else {
                            exclusionNeed.add(commoditySetmealGroup.getCommodityId());
                            continue;
                        }
                    }

                    // 如果分组内还有未下架的商品 ，将此套餐排除下架
                    if (singleList1.size()  != downCount) {
                        exclusion.add(commoditySetmealGroup.getCommodityId());
                    }else {
                        exclusionNeed.add(commoditySetmealGroup.getCommodityId());
                    }
                }
            }
        }
        if (ObjectUtil.isNotEmpty(fixedCollocation)){
            exclusionNeed.addAll(fixedCollocation);
        }

        if (ObjectUtil.isNotEmpty(exclusionNeed)) {
            LambdaUpdateWrapper<CommoditySpus> commoditySpusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            commoditySpusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus, 0);
            commoditySpusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus, 0);
            commoditySpusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, exclusionNeed);
            commoditySpusMapper.update(commoditySpusLambdaUpdateWrapper);
        }

    }

    @Override
    public void checkStatusV3() {
        LambdaQueryWrapper<CommoditySpus> commoditySpusLambdaQueryWrapper = new LambdaQueryWrapper<>();

        List<CommoditySpus> list = commoditySpusMapper.selectList(commoditySpusLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(list)){
            List<CommoditySpus> singleSpu = new ArrayList<>();
            List<CommoditySpus> setmealSpu = new ArrayList<>();
            for (CommoditySpus commoditySpus : list) {
                if (commoditySpus.getIsSingle().equals(1)){
                    singleSpu.add(commoditySpus);
                }else {
                    setmealSpu.add(commoditySpus);
                }
            }
            if (ObjectUtil.isNotEmpty(setmealSpu)){
                Map<Long, CommoditySpus> singleMap = singleSpu.stream().collect(Collectors.toMap(CommoditySpus::getCommodityId, c -> c));
                List<Long> singleIds = singleSpu.stream().map(CommoditySpus::getCommodityId).toList();



                List<CommodityGroupSingle> singles =commodityGroupSingleService.selectBySpuIds(singleIds);


                for (CommodityGroupSingle single : singles) {
                    CommoditySpus commoditySpus = singleMap.get(single.getCommodityId());
                    single.setWxStatus(commoditySpus.getWxStatus());
                    single.setStoreStatus(commoditySpus.getStoreStatus());
                }


                commodityGroupSingleService.updateByIds(singles);


                List<Long> setmealIds = setmealSpu.stream().map(CommoditySpus::getCommodityId).toList();


                List<CommoditySetmealGroup> groups =commoditySetmealGroupService.selectBySpuIds(setmealIds);




                List<CommodityGroupSingle> groupSingles =commodityGroupSingleService.selectBySpuIds(setmealIds);

                if (ObjectUtil.isNotEmpty(groupSingles)){
                    Map<Long, List<CommodityGroupSingle>> singlemap = groupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));



                    List<Long> wxDownSpuIds = new ArrayList<>();
                    List<Long> storeSownSpuIds = new ArrayList<>();
                    for (CommoditySetmealGroup commoditySetmealGroup : groups) {
                        List<CommodityGroupSingle> singleList = singlemap.get(commoditySetmealGroup.getGroupId());
                        if (ObjectUtil.isNotEmpty(singleList)){

                            int size = singleList.size();
                            Integer wxDownSize = 0;
                            Integer StoreDownSize = 0;

                            for (CommodityGroupSingle commodityGroupSingle : singleList) {
                                if (commodityGroupSingle.getWxStatus().equals(0)){
                                    wxDownSize++;
                                }
                                if (commodityGroupSingle.getStoreStatus().equals(0)){
                                    StoreDownSize++;
                                }
                            }
                            if (size==wxDownSize){
                                if (!wxDownSpuIds.contains(commoditySetmealGroup.getCommodityId())){
                                    wxDownSpuIds.add(commoditySetmealGroup.getCommodityId());
                                }
                            }
                            if (size==StoreDownSize){
                                if (!storeSownSpuIds.contains(commoditySetmealGroup.getCommodityId())){
                                    storeSownSpuIds.add(commoditySetmealGroup.getCommodityId());
                                }
                            }
                        }
                    }
                    for (CommoditySpus commoditySpus : setmealSpu) {
                        if (wxDownSpuIds.contains(commoditySpus.getCommodityId())){
                            commoditySpus.setWxStatus(0);
                        }
                        if (storeSownSpuIds.contains(commoditySpus.getCommodityId())){
                            commoditySpus.setStoreStatus(0);
                        }
                    }
                    commoditySpusMapper.updateBatch(setmealSpu);



                }


            }


        }


    }

    private List<CommoditySpus> batchUp(CommodityBatchEditReqVo batchEditReqVo) {
        List<Long> commodityIds = batchEditReqVo.getCommodityIds();

        /*LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus, 1);
        spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus, 1);
        this.update(spusLambdaUpdateWrapper);*/




        commodityGroupSingleService.updateUpInSpuIds(commodityIds);


        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        List<CommoditySpus> list = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
        List<Long> setmealIds = new ArrayList<>();
        List<Long> singleSpuIds = new ArrayList<>();
        for (CommoditySpus commoditySpus : list) {
            if (commoditySpus.getIsSingle() == 1) {
                singleSpuIds.add(commoditySpus.getCommodityId());

            } else {
                setmealIds.add(commoditySpus.getCommodityId());
            }
        }
        List<Long> isDownSet = new ArrayList<>();
        Map<Long, CommoditySpus> commoditySpusMap = new HashMap<>();

        if (ObjectUtil.isNotEmpty(setmealIds)){
            commoditySpusMap = list.stream().collect(Collectors.toMap(CommoditySpus::getCommodityId, c -> c));




            List<CommodityGroupSingle> groupSingles = commodityGroupSingleService.selectByCommodityIds(setmealIds);


            Map<Long, List<CommodityGroupSingle>> singleMap = groupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getSpuId));



            List<CommoditySetmealGroup> groupList =commoditySetmealGroupService.selectBySpuIds(setmealIds);




            Map<Long, List<CommoditySetmealGroup>> groupMap = groupList.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));


            for (Long setmealId : setmealIds) {
                CommoditySpus commoditySpus = commoditySpusMap.get(setmealId);
                List<CommodityGroupSingle> singleList = singleMap.get(setmealId);

                //如果是可选商品
                if (commoditySpus.getSetmealType() == 2) {
                    List<CommoditySetmealGroup> groupList1 = groupMap.get(setmealId);
                    Map<Long, List<CommodityGroupSingle>> singleMapForSpu = singleList.stream()
                            .collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));

                    boolean shouldAdd = false;
                    for (CommoditySetmealGroup commoditySetmealGroup : groupList1) {
                        List<CommodityGroupSingle> singleList1 = singleMapForSpu.get(commoditySetmealGroup.getGroupId());
                        if (singleList1 == null) {
                            continue;
                        }
                        if (commoditySetmealGroup.getAttribute() == 2) {
                            if (hasInactiveItem(singleList1)) {
                                shouldAdd = true;
                                break;
                            }
                        } else {
                            if (allItemsInactive(singleList1)) {
                                shouldAdd = true;
                                break;
                            }
                        }
                    }
                    if (shouldAdd) {
                        isDownSet.add(setmealId);
                    }
                } else {
                    //如果是固定搭配
                    if (!allItemsActive(singleList)) {
                        isDownSet.add(setmealId);
                    }
                }
            }
        }

        if (ObjectUtil.isNotEmpty(isDownSet)) {
            commodityIds.removeAll(isDownSet);
        }
        if (ObjectUtil.isNotEmpty(commodityIds)){
            LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
            spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus, 1);
            spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus, 1);
            commoditySpusMapper.update(spusLambdaUpdateWrapper);
        }

        if (ObjectUtil.isNotEmpty(isDownSet)){
            List<CommoditySpus> spusList = new ArrayList<>();
            for (Long l : isDownSet) {
                spusList.add(commoditySpusMap.get(l));
            }
            return spusList;
        }

        return new ArrayList<>();
    }

    private boolean hasInactiveItem(List<CommodityGroupSingle> singleList1) {
        for (CommodityGroupSingle commodityGroupSingle : singleList1) {
            if (commodityGroupSingle.getStoreStatus() == 0 || commodityGroupSingle.getWxStatus() == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean allItemsInactive(List<CommodityGroupSingle> singleList) {
        for (CommodityGroupSingle commodityGroupSingle : singleList) {
            if (commodityGroupSingle.getStoreStatus() != 0 || commodityGroupSingle.getWxStatus() != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean allItemsActive(List<CommodityGroupSingle> singleList) {
        for (CommodityGroupSingle commodityGroupSingle : singleList) {
            if (commodityGroupSingle.getWxStatus() != 1 || commodityGroupSingle.getStoreStatus() != 1) {
                return false;
            }
        }
        return true;
    }


    @Override
    public List<CommodityCountRespVO> getSpuCountV3() {
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();

        List<CommoditySpus> list = commoditySpusMapper.selectList(queryWrapper);

        List<CommodityCountRespVO> countVOList = new ArrayList<>();
        int[] types = {1, 2, 3, 4};
        for (int type : types) {
            CommodityCountRespVO vo = new CommodityCountRespVO();
            vo.setType(type);
            vo.setCount(0);
            countVOList.add(vo);
        }
        if (ObjectUtil.isNotEmpty(list)) {
            // 隐藏商品单独统计，不再计入售卖或已下架
            int hiddenCount = (int) list.stream()
                    .filter(spus -> Integer.valueOf(1).equals(spus.getIsHidden()))
                    .count();
            int downCount = (int) list.stream()
                    .filter(spus -> !Integer.valueOf(1).equals(spus.getIsHidden())
                            && Integer.valueOf(0).equals(spus.getWxStatus())
                            && Integer.valueOf(0).equals(spus.getStoreStatus()))
                    .count();
            int saleCount = list.size() - hiddenCount - downCount;
            for (CommodityCountRespVO commodityCountRespVO : countVOList) {
                if (commodityCountRespVO.getType().equals(1)) {
                    commodityCountRespVO.setCount(list.size());
                } else if (commodityCountRespVO.getType().equals(2)) {
                    commodityCountRespVO.setCount(saleCount);
                } else if (commodityCountRespVO.getType().equals(3)) {
                    commodityCountRespVO.setCount(downCount);
                } else if (commodityCountRespVO.getType().equals(4)) {
                    commodityCountRespVO.setCount(hiddenCount);
                }
            }
        }


        return countVOList;
    }


    @Override
    public void sortSpuV3(List<CommoditySpusSortReqVo> spusSortReqVos) {
        if (ObjectUtil.isNotEmpty(spusSortReqVos)) {
            for (CommoditySpusSortReqVo spusSortReqVo : spusSortReqVos) {
                LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(CommoditySpus::getCommodityId, spusSortReqVo.getCommodityId());
                updateWrapper.set(CommoditySpus::getSort, spusSortReqVo.getSort());
                commoditySpusMapper.update(updateWrapper);
            }
        }
    }


    @Override
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_UPDATE_NAME_SUB_TYPE, bizNo = "{{#uNameReqVo.commodityId}}", success = COMMODITY_SPU_UPDATE_NAME_SUCCESS)
    public void updateNameV3(CommodityUNameReqVo uNameReqVo) {
        if (ObjectUtil.isEmpty(uNameReqVo.getCommodityName())) {
            throw exception(BASE_SPU_PRODUCT_NAME_CANNOT_BE_EMPTY);
        }
        LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CommoditySpus::getCommodityName, uNameReqVo.getCommodityName());
        updateWrapper.eq(CommoditySpus::getCommodityId, uNameReqVo.getCommodityId());
        commoditySpusMapper.update(updateWrapper);




        commodityGroupSingleService.updateSpuNameBySpuId(uNameReqVo);


        commodityAfterOrderService.updateAfterOrderName(uNameReqVo.getCommodityId(),uNameReqVo.getCommodityName());

        commodityActivityService.updateCommodityName(uNameReqVo.getCommodityId(),uNameReqVo.getCommodityName());

        couponCommodityApi.updateCommodityName(uNameReqVo.getCommodityId(),uNameReqVo.getCommodityName());

        LogRecordContext.putVariable("uNameReqVo", uNameReqVo);

        //修改连锁商品库子品图片和名称
        commodityGroupSingleService.changeName(uNameReqVo);
        //修改模板里套餐子品图片和名称
        CommoditySpusUpdateReqVo updateReqVo = new CommoditySpusUpdateReqVo();
        updateReqVo.setCommodityId(uNameReqVo.getCommodityId());
        updateReqVo.setCommodityName(uNameReqVo.getCommodityName());
        commodityTemplateGroupSingleService.changeName(updateReqVo);

        commodityTemplateSpusService.changeSpuName(uNameReqVo.getCommodityId(),uNameReqVo.getCommodityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_CATE_UPDATE_SUB_TYPE, bizNo = "{{#commodityCategory.id}}", success = COMMODITY_CATE_UPDATE_SUCCESS)
    public void updateCategoryV3(CategoryUpdateReqVo updateReqVo) {
        if (ObjectUtil.isNotEmpty(updateReqVo.getStartDate())){
            if (ObjectUtil.isEmpty(updateReqVo.getEndDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
        if (ObjectUtil.isNotEmpty(updateReqVo.getEndDate())){
            if (ObjectUtil.isEmpty(updateReqVo.getStartDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }


        List<CategoryRespVo> list = commodityCategoryService.getCommodityCategoryList();


        List<CategoryRespVo> list2 = list.stream().filter(c -> c.getName().equals(updateReqVo.getName())).toList();
        for (CategoryRespVo category : list2) {
            if (category.getName().equals(updateReqVo.getName()) && !category.getId().equals(updateReqVo.getId())) {
                throw exception(BASE_CATE_EXISTING_NAME_CATEGORY);
            }
        }
        if (updateReqVo.getType().equals(1)) {
            List<CategoryRespVo> list3 = list.stream().filter(c -> c.getType().equals(1)).toList();
            for (CategoryRespVo category : list3) {
                if (!category.getId().equals(updateReqVo.getId()) && category.getType().equals(1)) {
                    throw exception(BASE_CATE_WITH_GROUPING);
                }
            }
        }




        LambdaQueryWrapper<CommoditySpus> categorySpusQueryWrapper = new LambdaQueryWrapper<>();
        categorySpusQueryWrapper.eq(CommoditySpus::getCategoryId, updateReqVo.getId());
        List<CommoditySpus> categorySpusList = commoditySpusMapper.selectList(categorySpusQueryWrapper);
        List<Long> categorySpuIds = categorySpusList.stream()
                .map(CommoditySpus::getCommodityId)
                .toList();

        CommodityCategory commodityCategory = new CommodityCategory();
        BeanUtils.copyProperties(updateReqVo,commodityCategory);
        commodityCategoryService.updateById(commodityCategory);

        LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CommoditySpus::getCategoryName, commodityCategory.getName());
        if (updateReqVo.getIsHidden() != null) {
            updateWrapper.set(CommoditySpus::getIsHidden, updateReqVo.getIsHidden());
        }
        updateWrapper.eq(CommoditySpus::getCategoryId, commodityCategory.getId());
        commoditySpusMapper.update(updateWrapper);

        if (Integer.valueOf(1).equals(updateReqVo.getIsHidden())) {
            commodityAfterOrderService.deleteByCommodityIds(categorySpuIds);
        }

        LogRecordContext.putVariable("commodityCategory", commodityCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCategoryHidden(CategoryBatchHiddenReqVO reqVO) {
        List<Long> categoryIds = reqVO.getCategoryIds();
        Integer isHidden = reqVO.getIsHidden();

        commodityCategoryService.updateHiddenByIds(categoryIds, isHidden);

        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CommoditySpus::getCommodityId);
        queryWrapper.in(CommoditySpus::getCategoryId, categoryIds);
        List<Long> commodityIds = commoditySpusMapper.selectList(queryWrapper).stream()
                .map(CommoditySpus::getCommodityId)
                .toList();

        LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CommoditySpus::getCategoryId, categoryIds);
        updateWrapper.set(CommoditySpus::getIsHidden, isHidden);
        commoditySpusMapper.update(updateWrapper);

        if (Integer.valueOf(1).equals(isHidden)) {
            commodityAfterOrderService.deleteByCommodityIds(commodityIds);
        }
    }

    @Override
    public List<CommoditySpusByIdRespVo> listForCommodityV3(Long commodityId) {
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        List<CommoditySpusByIdRespVo>  commoditySpusByIdRespVoList = new ArrayList<>();
        queryWrapper.eq(CommoditySpus::getIsSingle, 1)
                .and(wrapper -> wrapper.isNull(CommoditySpus::getFlavor).or().eq(CommoditySpus::getFlavor, ""))
                .and(wrapper -> wrapper.isNull(CommoditySpus::getCondiments).or().eq(CommoditySpus::getCondiments, ""))
                .eq(CommoditySpus::getSaleRule, 0)
                .orderByDesc(CommoditySpus::getUpdateTime);
        queryWrapper.eq(CommoditySpus::getIsHidden, 0);
        List<CommoditySpus> list = commoditySpusMapper.selectList(queryWrapper);
        // 前端传了 id 且 list 里没有，按 id 查出来放进 list
        if (commodityId != null && list.stream().noneMatch(spu -> commodityId.equals(spu.getCommodityId()))) {
            List<CommoditySpus> one = commoditySpusMapper.selectList(
                    new LambdaQueryWrapper<CommoditySpus>().eq(CommoditySpus::getCommodityId, commodityId).last("limit 1"));
            if (ObjectUtil.isNotEmpty(one)) {
                list.add(one.get(0));
            }
        }
        if (ObjectUtil.isNotEmpty(list)) {
            List<Long> commodityIds = list.stream().map(CommoditySpus::getCommodityId).collect(Collectors.toList());


            commoditySpusByIdRespVoList = BeanUtils.toBean(list, CommoditySpusByIdRespVo.class);

            List<CommoditySkus> skusList =commoditySkusService.selectBySpuIds(commodityIds);

            Map<Long, List<CommoditySkus>> collect = skusList.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));

            for (CommoditySpusByIdRespVo commoditySpusByIdRespVo : commoditySpusByIdRespVoList) {
                List<CommoditySkus> skusList1 = collect.get(commoditySpusByIdRespVo.getCommodityId());
                if (ObjectUtil.isNotEmpty(skusList1)) {
                    if (skusList1.size() == 1) {
                        List<CommoditySkuRespVO> commoditySkuRespVOS = BeanCopyUtils.copyBeanList(skusList1, CommoditySkuRespVO.class);
                        commoditySpusByIdRespVo.setCommoditySkuRespVOList(commoditySkuRespVOS);

                    }
                }
            }

        }
        return commoditySpusByIdRespVoList;
    }





    @Override
    public List<CommoditySpusByIdRespVo> getOffShelfProductsV3(CommoditySingleDownReqVo singleDownReqVo) {
        Long commodityId = singleDownReqVo.getCommodityId();
        Integer chooseView = singleDownReqVo.getChooseView();
        // 1) 找到“当前单品”关联到的所有套餐关系
        List<CommodityGroupSingle> singleRelations =
                commodityGroupSingleService.selectByChooseViewAndSpuId(commodityId, chooseView);
        if (ObjectUtil.isEmpty(singleRelations)) {
            return new ArrayList<>();
        }

        // 2) 仅针对关联套餐，复用 getOrDown 统一判定“是否需要联动下架”
        List<Long> relatedPackageIds = singleRelations.stream()
                .map(CommodityGroupSingle::getSpuId)
                .distinct()
                .toList();
        List<Long> singleIds = Collections.singletonList(commodityId);
        List<CommoditySpus> packagesNeedDown = findNeedDownPackagesByIds(relatedPackageIds, singleIds, chooseView);
        // 3) 转为接口返回结构
        return convertToSpusByIdRespVos(packagesNeedDown);
    }

    private int calculateDownCount(List<CommodityGroupSingle> singleList1, Integer chooseView) {
        int downCount = 0;
        for (CommodityGroupSingle single : singleList1) {
            boolean isDown;
            if (chooseView.equals(1)) {
                isDown = single.getWxStatus().equals(1);
            } else {
                isDown = single.getStoreStatus().equals(1);
            }
            if (isDown) {
                downCount++;
            }
        }
        return downCount;
    }

    private List<CommoditySpus> findNeedDownPackagesByIds(List<Long> packageIds, List<Long> singleIds, Integer chooseView) {
        if (ObjectUtil.isEmpty(packageIds)) {
            return new ArrayList<>();
        }

        // 只取当前端口下处于上架状态的套餐，和旧逻辑保持一致
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommoditySpus::getCommodityId, packageIds);
        if (chooseView.equals(SpuEnum.WX.getCode())) {
            queryWrapper.eq(CommoditySpus::getWxStatus, SpuEnum.UP.getCode());
        } else {
            queryWrapper.eq(CommoditySpus::getStoreStatus, SpuEnum.UP.getCode());
        }
        List<CommoditySpus> packageList = commoditySpusMapper.selectList(queryWrapper);
        if (ObjectUtil.isEmpty(packageList)) {
            return new ArrayList<>();
        }

        List<Long> activePackageIds = packageList.stream().map(CommoditySpus::getCommodityId).toList();
        List<CommoditySetmealGroup> setmealGroups = commoditySetmealGroupService.selectBySpuIds(activePackageIds);
        List<CommodityGroupSingle> groupSingles = commodityGroupSingleService.selectByCommodityIds(activePackageIds);
        Map<Long, List<CommoditySetmealGroup>> groupMap =
                setmealGroups.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));
        Map<Long, List<CommodityGroupSingle>> singleMap =
                groupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getSpuId));

        List<CommoditySpus> needDown = new ArrayList<>();
        for (CommoditySpus commoditySpus : packageList) {
            List<CommoditySetmealGroup> groups = groupMap.get(commoditySpus.getCommodityId());
            List<CommodityGroupSingle> singles = singleMap.get(commoditySpus.getCommodityId());
            if (ObjectUtil.isEmpty(groups) || ObjectUtil.isEmpty(singles)) {
                continue;
            }
            // 统一复用 getOrDown，避免 singleDownSelect 和 batchDownSelect 判定分叉
            if (getOrDown(commoditySpus, groups, singles, chooseView, singleIds)) {
                needDown.add(commoditySpus);
            }
        }
        return needDown;
    }

    private List<CommoditySpusByIdRespVo> convertToSpusByIdRespVos(List<CommoditySpus> commoditySpuses) {
        if (ObjectUtil.isEmpty(commoditySpuses)) {
            return new ArrayList<>();
        }
        List<CommoditySpusByIdRespVo> respVos = new ArrayList<>();
        for (CommoditySpus commoditySpus : commoditySpuses) {
            CommoditySpusByIdRespVo respVo = new CommoditySpusByIdRespVo();
            BeanUtils.copyProperties(commoditySpus, respVo);
            if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
                respVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
            }
            respVos.add(respVo);
        }
        return respVos;
    }

    private int calculateDownCount(List<CommodityGroupSingle> singleList1) {
        int downCount = 0;
        for (CommodityGroupSingle single : singleList1) {
            boolean isDown;

            if (single.getWxStatus().equals(0) && single.getStoreStatus().equals(0)){
                isDown = false;

            }else {
                isDown= true;
            }


            if (isDown) {
                downCount++;
            }
        }
        return downCount;
    }


    @Override
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_SINGLE_DOWN_SUB_TYPE, bizNo = "{{#singleDownReqVo.commodityId}}", success = COMMODITY_SPU_SINGLE_DOWN_SUCCESS)
    public void singleDownV3(CommoditySingleDownReqVo singleDownReqVo) {
        List<CommoditySpus> offShelfProductsV3 = singleDownReqVo.getSpusList();
        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        List<Long> commodityIds = new ArrayList<>();
        commodityIds.add(singleDownReqVo.getCommodityId());
        if (ObjectUtil.isNotEmpty(offShelfProductsV3)) {
            commodityIds.addAll(offShelfProductsV3.stream().map(CommoditySpus::getCommodityId).toList()) ;
        }
        spusLambdaUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        if (singleDownReqVo.getChooseView().equals(1)){
            spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus,0);
        }else {
            spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus,0);
        }
        commoditySpusMapper.update(spusLambdaUpdateWrapper);



        commodityGroupSingleService.updateDownBySpuId(singleDownReqVo.getChooseView(),singleDownReqVo.getCommodityId());

        LogRecordContext.putVariable("singleDownReqVo", singleDownReqVo);

    }

    @Override
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_SINGLE_UP_SUB_TYPE, bizNo = "{{#singleUpReqVo.commodityId}}", success = COMMODITY_SPU_SINGLE_UP_SUCCESS)
    public void singleUpV3(CommoditySingleUpReqVo singleUpReqVo) {
        Long commodityId = singleUpReqVo.getCommodityId();
        Integer chooseView = singleUpReqVo.getChooseView();


        commodityGroupSingleService.updateUpBySpuId(chooseView,commodityId);

        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.eq(CommoditySpus::getCommodityId, commodityId);
        if (chooseView.equals(1)){
            spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus,1);
        }else {
            spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus,1);
        }
        commoditySpusMapper.update(spusLambdaUpdateWrapper);
        LogRecordContext.putVariable("singleUpReqVo", singleUpReqVo);
    }

    @Override
    public List<CommodityGroupSingleRespVo> packageSelectV3(SetMealSingleUpReqVo singleUpReqVo) {

        List<CommoditySetmealGroup> groupList =commoditySetmealGroupService.selectBySpuId(singleUpReqVo.getCommodityId());

        Integer chooseView = singleUpReqVo.getChooseView();
        if (ObjectUtil.isNotEmpty(groupList)){
            List<Long> groupIds = groupList.stream().map(CommoditySetmealGroup::getGroupId).toList();
            List<CommodityGroupSingle> groupSingles =commodityGroupSingleService.selectByGroupIds(groupIds);

            List<CommodityGroupSingle> singleList = new ArrayList<>();
            if (ObjectUtil.isNotEmpty(groupSingles)){
                Map<Long, List<CommodityGroupSingle>> singleMap = groupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));
                for (CommoditySetmealGroup commoditySetmealGroup : groupList) {
                    List<CommodityGroupSingle> commodityGroupSingles = singleMap.get(commoditySetmealGroup.getGroupId());
                    if (ObjectUtil.isNotEmpty(commodityGroupSingles)){

                        if (commoditySetmealGroup.getAttribute()==0){
                            //固定搭配 必须要全部上架
                            singleList = checkSownSingleV0(commodityGroupSingles, chooseView);
                            if (ObjectUtil.isNotEmpty(singleList)) {
                                break;
                            }
                        }else if (commoditySetmealGroup.getAttribute()==1){
                            //可选分组
                            singleList = checkSownSingleV1(commodityGroupSingles, chooseView,commoditySetmealGroup);
                            if (ObjectUtil.isNotEmpty(singleList)) {
                                break;
                            }
                        }else if (commoditySetmealGroup.getAttribute()==2){
                            singleList = checkSownSingleV0(commodityGroupSingles, chooseView);
                            if (ObjectUtil.isNotEmpty(singleList)) {
                                break;
                            }
                        }else if (commoditySetmealGroup.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()){
                            // 套餐上架查询：加价组无论是否有上架子品，都不阻断套餐上架
                            continue;
                        }
                    }
                }

            }
            List<CommodityGroupSingleRespVo> commodityGroupSingleRespVos = BeanCopyUtils.copyBeanList(singleList, CommodityGroupSingleRespVo.class);
            return  commodityGroupSingleRespVos;

        }else {
            return new ArrayList<>();
        }

    }

    private List<CommodityGroupSingle> checkSownSingleV1(List<CommodityGroupSingle> commodityGroupSingles, Integer chooseView,CommoditySetmealGroup commoditySetmealGroup) {
        //可选 分组 里 可选商品数量
        Long commodityStoreGroupChoose = commoditySetmealGroup.getChoose();
        Integer chooseMany = commoditySetmealGroup.getChooseMany();
        int upSize= 0;
        List<CommodityGroupSingle> commodityGroupSingleList = new ArrayList<>();
        for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingles) {

            if (chooseView==1){
                if (commodityGroupSingle.getWxStatus()==1){
                    upSize++;
                }else {
                    commodityGroupSingleList.add(commodityGroupSingle);
                }

            }else {

                if (commodityGroupSingle.getStoreStatus()==1){
                    upSize++;
                }else {
                    commodityGroupSingleList.add(commodityGroupSingle);
                }
            }
        }



        if (chooseMany == 1) {
            //如果套餐内未上架子品里有必选品 套餐不予上架
            if (ObjectUtil.isNotEmpty(commodityGroupSingleList)) {
                for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingleList) {
                    if (commodityGroupSingle.getRequiredChoose()==1){
                        return   commodityGroupSingleList;
                    }
                }
            }


            // 同一商品可以多选：只要有一个品可以上架就返回空列表
            return upSize >= 1 ? new ArrayList<>() : commodityGroupSingleList;
        } else {
            if (ObjectUtil.isNotEmpty(commodityGroupSingleList)) {
                for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingleList) {
                    if (commodityGroupSingle.getRequiredChoose()==1){
                        return   commodityGroupSingleList;
                    }
                }
            }

            // 同一商品不可多选：如果上架数量大于等于可选数量则返回空列表
            return upSize >= commodityStoreGroupChoose ? new ArrayList<>() : commodityGroupSingleList;
        }





    }

    private List<CommodityGroupSingle> checkSownSingleV0(List<CommodityGroupSingle> commodityGroupSingles, Integer chooseView) {
        List<CommodityGroupSingle> downSingle = new ArrayList<>();
        for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingles) {
            if (chooseView==1){
                if (commodityGroupSingle.getWxStatus()==0){
                    downSingle.add(commodityGroupSingle);
                }
            }else {
                if (commodityGroupSingle.getStoreStatus()==0){
                    downSingle.add(commodityGroupSingle);
                }
            }
        }
        return downSingle;
    }


    @Override
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SPU_SETMEAL_UP_DOWN_SUB_TYPE, bizNo = "{{#upAndDownReqVo.commodityId}}", success = COMMODITY_SPU_SETMEAL_UP_DOWN_SUCCESS)
    public void packageUpAndDownV3(SetMealUpAndDownReqVo upAndDownReqVo) {
        LambdaUpdateWrapper<CommoditySpus> spusLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spusLambdaUpdateWrapper.eq(CommoditySpus::getCommodityId, upAndDownReqVo.getCommodityId());
        if (ObjectUtil.isNotEmpty(upAndDownReqVo.getWxStatus())){
            spusLambdaUpdateWrapper.set(CommoditySpus::getWxStatus,upAndDownReqVo.getWxStatus());
        }
        if (ObjectUtil.isNotEmpty(upAndDownReqVo.getStoreStatus())){
            spusLambdaUpdateWrapper.set(CommoditySpus::getStoreStatus,upAndDownReqVo.getStoreStatus());
        }
        commoditySpusMapper.update(spusLambdaUpdateWrapper);
        LogRecordContext.putVariable("upAndDownReqVo", upAndDownReqVo);
    }

    @Override
    public List<CommoditySpus> getSpursByCommodityIds(List<Long> commodityIds) {
        LambdaQueryWrapper<CommoditySpus> spursWrapper = new LambdaQueryWrapper<>();
        spursWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        return commoditySpusMapper.selectList(spursWrapper);
    }

    @Override
    public Boolean selectIsExistTag(Long id) {

        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.apply("FIND_IN_SET({0}, tag_ids) > 0", id);
        long count = commoditySpusMapper.selectCount(spusLambdaQueryWrapper);


        return count>0;
    }

    @Override
    public List<CommodityDTO> getSpuDTOByCommodityIds(List<Long> commodityIds) {
        List<CommodityDTO> commodityDTOList = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        List<CommoditySpus> commoditySpuses = commoditySpusMapper.selectList(spusLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(commoditySpuses)) {
            for (CommoditySpus commoditySpus : commoditySpuses) {
                CommodityDTO commodityDTO = new CommodityDTO();
                BeanUtils.copyProperties(commoditySpus, commodityDTO);
                commodityDTOList.add(commodityDTO);
            }
        }
        return commodityDTOList;
    }

    @Override
    public PageResult<CommodityCouponSpuVO> couponSpuList(CommodityCouponSpuReqVO queryReqVo) {
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();

        //存在属性
        if (queryReqVo.getHaveFlavor()){
            queryWrapper.and(wrapper ->
                wrapper.isNull(CommoditySpus::getFlavor)
                .or()
                .eq(CommoditySpus::getFlavor, "")
            );
        }
        //存在小料
        if (queryReqVo.getHaveCondiments()){
            queryWrapper.and(wrapper ->
                wrapper.isNull(CommoditySpus::getCondiments)
                .or()
                .eq(CommoditySpus::getFlavor, "")
            );
        }
        if (Objects.nonNull(queryReqVo.getIsSingle())){
            queryWrapper.eq(CommoditySpus::getIsSingle, queryReqVo.getIsSingle());
        }

        if (Objects.nonNull(queryReqVo.getCategoryId())){
            queryWrapper.eq(CommoditySpus::getCategoryId, queryReqVo.getCategoryId());
        }

        if (StringUtils.isNotBlank(queryReqVo.getCommodityName())){
            queryWrapper.like(CommoditySpus::getCommodityName, queryReqVo.getCommodityName());
        }

        PageResult<CommoditySpus> pageResult = commoditySpusMapper.selectPage(queryReqVo, queryWrapper);
        if (CollectionUtils.isEmpty(pageResult.getList())){
            return PageResult.empty();
        }

        List<CommoditySpus> list = pageResult.getList();
        List<Long> categoryIds = list.stream().map(CommoditySpus::getCategoryId).distinct().toList();
        List<CommodityCategory> commodityCategories = commodityCategoryService.selectListByIdsAndAsc(categoryIds);
        Map<Long, String> categoryMap = commodityCategories.stream()
            .collect(Collectors.toMap(CommodityCategory::getId, CommodityCategory::getName));


        List<Long> spuIds = list.stream().map(CommoditySpus::getCommodityId).distinct().toList();

        List<CommoditySkus> skusList =  commoditySkusService.selectBySpuIds(spuIds);
        Map<Long, List<CommoditySkus>> skuMap = skusList.stream().collect(Collectors.groupingBy(CommoditySkus::getCommodityId));



        List<CommodityCouponSpuVO> spuVOS = new ArrayList<>();
        for (CommoditySpus commoditySpus : list) {
            CommodityCouponSpuVO spuVO = new CommodityCouponSpuVO();

            List<CommoditySkus> skusList1 = skuMap.get(commoditySpus.getCommodityId());
            if (skusList1.size() > 1) {
                PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPrices(skusList1);
                spuVO.setLowPrice(minMaxPrices.getMinIllustratePrices());
                spuVO.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                spuVO.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                spuVO.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                spuVO.setIsMoreSku(true);
            } else {
                spuVO.setLowPrice(skusList1.get(0).getIllustratePrices());
                spuVO.setLowStrikePrice(skusList1.get(0).getStrikeThroughPrice());
                spuVO.setIsMoreSku(false);
            }

            spuVO.setCategoryId(commoditySpus.getCategoryId());
            spuVO.setCategoryName(categoryMap.get(commoditySpus.getCategoryId()));
            spuVO.setCommodityId(commoditySpus.getCommodityId());
            spuVO.setCommodityName(commoditySpus.getCommodityName());
            spuVOS.add(spuVO);
        }

        PageResult<CommodityCouponSpuVO> result = new PageResult<>();
        result.setList(spuVOS);
        result.setTotal(pageResult.getTotal());
        return result;
    }

    @Override
    public void updateCommoditySkuFlag(Long commodityId, int skuFlag) {
        LambdaUpdateWrapper<CommoditySpus> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("sku_flag = {0}, is_change = {1}", skuFlag, "0");
        wrapper.eq(CommoditySpus::getCommodityId, commodityId);
        commoditySpusMapper.update(wrapper);
    }

    @Override
    public PageResult<RecipeCommodityRespVO> selectRecipeCommodity(RecipeCommodityReqVO recipeCommodityReqVO) {

        Set<Long> storeId = new HashSet<>();

        if (recipeCommodityReqVO.getOrgId() != null) {
            if (recipeCommodityReqVO.getIsStore() == 0) {
                CommonResult<Set<Long>> allStoreIdListByOrgID = orgApi.getAllStoreIdListByOrgID(recipeCommodityReqVO.getOrgId());
                if (allStoreIdListByOrgID.isSuccess() && CollectionUtils.isNotEmpty(allStoreIdListByOrgID.getCheckedData())) {
                    storeId = allStoreIdListByOrgID.getData();
                }
            }else if (recipeCommodityReqVO.getIsStore() == 1){
                storeId.add(recipeCommodityReqVO.getOrgId());
            }
        }


        PageParam page = new PageParam();
        page.setPageNo(recipeCommodityReqVO.getPageNo());
        page.setPageSize(recipeCommodityReqVO.getPageSize());

        MPJLambdaWrapper<RecipeCommodityDO> wrapper = new MPJLambdaWrapper<>();

        wrapper.distinct();
        // 主表字段选择
        wrapper.select(
                RecipeCommodityDO::getCommodityId,
                RecipeCommodityDO::getCommodityName,
                RecipeCommodityDO::getImageUrl,
                RecipeCommodityDO::getSkuFlag,
                RecipeCommodityDO::getIsChange
        );

        // 关联表：commodity_category（别名为"category"）
        wrapper.leftJoin(CommodityCategory.class, "category",
                CommodityCategory::getId,  // 关联表字段
                RecipeCommodityDO::getCategoryId);  // 主表关联字段

        // 使用selectAs映射：category表的name → DTO的categoryName
        wrapper.selectAs(
                "category",                  // 表别名
                CommodityCategory::getName,  // 关联表的源字段
                CommodityDTO::getCategoryName  // DTO的目标字段
        );

        if (CollectionUtils.isNotEmpty(storeId)) {
            wrapper.innerJoin(CommodityStoreSpu.class, "store",
                    CommodityStoreSpu::getCommodityId,
                    RecipeCommodityDO::getCommodityId);

            wrapper.in(CommodityStoreSpu::getStoreId, storeId);
        }

        wrapper.eq(RecipeCommodityDO::getIsSingle, "1");
        if (StringUtils.isNotBlank(recipeCommodityReqVO.getCommodityName())){
            wrapper.like(RecipeCommodityDO::getCommodityName, recipeCommodityReqVO.getCommodityName());
        }
        if (recipeCommodityReqVO.getSkuFlag() != null){
            wrapper.eq(RecipeCommodityDO::getSkuFlag, recipeCommodityReqVO.getSkuFlag());
        }

        if (ObjectUtil.equals(recipeCommodityReqVO.getIsHidden(), IS_ALL)){
            recipeCommodityReqVO.setIsHidden(null);
        }

        if (ObjectUtil.isNotEmpty(recipeCommodityReqVO.getIsHidden())){
            wrapper.eq(RecipeCommodityDO::getIsHidden, recipeCommodityReqVO.getIsHidden());
        }

        wrapper.orderByDesc("is_change");
        wrapper.orderByAsc("sku_flag");
        wrapper.orderByDesc(RecipeCommodityDO::getCreateTime);
        // 执行查询
        PageResult<RecipeCommodityDO> commodityCategoryPageResult = recipeCommodityMapper.selectJoinPage(page, RecipeCommodityDO.class, wrapper);

        // 2. 转换列表并处理imageUrl
        List<RecipeCommodityDO> respVOList = commodityCategoryPageResult.getList().stream()
                .map(dto -> {
                    RecipeCommodityDO respVO = new RecipeCommodityDO();
                    BeanUtils.copyProperties(dto, respVO);

                    // 处理imageUrl：非空时只取第一个URL
                    String imageUrl = respVO.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // 按逗号分割，取第一个元素
                        String[] urls = imageUrl.split(",");
                        if (urls.length > 0) {
                            respVO.setImageUrl(urls[0].trim()); // 去除可能的空格
                        }
                    }

                    return respVO;
                })
                .collect(Collectors.toList());

        commodityCategoryPageResult.setList(respVOList);
        return BeanUtils.toBean(commodityCategoryPageResult, RecipeCommodityRespVO.class);

    }

    @Override
    public PageResult<StoreInfoDTO> getCommodityStoreList(RecipeCommodityStoreReqVO recipeCommodityStoreReqVO) {
        PageResult<StoreInfoDTO> storeId = commodityStoreSpuService.getStoreListByCommodityId(recipeCommodityStoreReqVO);
        return storeId;
    }

    public PageResult<RecipeCommoditySkuListRespVO> selectCommodityRecipeList(RecipeCommodityReqVO recipeCommodityReqVO) {
        PageParam page = new PageParam();
        page.setPageNo(recipeCommodityReqVO.getPageNo());
        page.setPageSize(recipeCommodityReqVO.getPageSize());

        MPJLambdaWrapper<RecipeCommodityDO> wrapper = new MPJLambdaWrapper<>();

        // 关联表：commodity_category（别名为"category"）
        wrapper.leftJoin(CommodityCategory.class, "category",
                CommodityCategory::getId,  // 关联表字段
                RecipeCommodityDO::getCategoryId);  // 主表关联字段

        // 使用selectAs映射：category表的name → DTO的categoryName
        wrapper.selectAs(
                "category",                  // 表别名
                CommodityCategory::getName,  // 关联表的源字段
                CommodityDTO::getCategoryName  // DTO的目标字段
        );

        // 主表字段选择
        wrapper.select(
                RecipeCommodityDO::getCommodityId,
                RecipeCommodityDO::getCommodityName,
                RecipeCommodityDO::getImageUrl
        );

        wrapper.eq(RecipeCommodityDO::getIsSingle, "1");
        wrapper.eq(RecipeCommodityDO::getSkuFlag, "1");
        if (StringUtils.isNotBlank(recipeCommodityReqVO.getCommodityName())){
            wrapper.like(RecipeCommodityDO::getCommodityName, recipeCommodityReqVO.getCommodityName());
        }
        wrapper.orderByDesc(RecipeCommodityDO::getCreateTime);
        // 执行查询
        PageResult<RecipeCommodityDO> commodityCategoryPageResult = recipeCommodityMapper.selectJoinPage(page, RecipeCommodityDO.class, wrapper);
        if (ObjectUtil.isEmpty(commodityCategoryPageResult) || CollectionUtils.isEmpty(commodityCategoryPageResult.getList())){
            return PageResult.empty();
        }

        // 2. 转换列表并处理imageUrl
        List<RecipeCommodityDO> respVOList = commodityCategoryPageResult.getList().stream()
                .map(dto -> {
                    RecipeCommodityDO respVO = new RecipeCommodityDO();
                    BeanUtils.copyProperties(dto, respVO);

                    // 处理imageUrl：非空时只取第一个URL
                    String imageUrl = respVO.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // 按逗号分割，取第一个元素
                        String[] urls = imageUrl.split(",");
                        if (urls.length > 0) {
                            respVO.setImageUrl(urls[0].trim()); // 去除可能的空格
                        }
                    }

                    return respVO;
                })
                .collect(Collectors.toList());

        commodityCategoryPageResult.setList(respVOList);
        PageResult<RecipeCommoditySkuListRespVO> recipeCommoditySkuListRespVOPageResult = BeanUtils.toBean(commodityCategoryPageResult, RecipeCommoditySkuListRespVO.class);
        if (ObjectUtil.isEmpty(recipeCommoditySkuListRespVOPageResult) || CollectionUtils.isEmpty(recipeCommoditySkuListRespVOPageResult.getList())){
            return PageResult.empty();
        }

        for (RecipeCommoditySkuListRespVO ele : recipeCommoditySkuListRespVOPageResult.getList()){
            Long commodityId = ele.getCommodityId();
            List<RecipeCommoditySkuRespVO> recipeCommoditySkuRespVOS = commodityRecipeService.selectRelatedByCommodityId(commodityId);
            ele.setRecipeCommoditySkuList(recipeCommoditySkuRespVOS);
        }
        return recipeCommoditySkuListRespVOPageResult;
    }

    /**
     * 单品上架查询，需要查出啦联动的套餐 id
     * @param singleUpReqVo
     * @return
     */
    @Override
    public SingleUpRespVO singleUpSelect(CommoditySingleUpReqVo singleUpReqVo) {

        SingleUpRespVO singleUpRespVO = new SingleUpRespVO();

        Long commodityId = singleUpReqVo.getCommodityId();
        //1 小程序  2 点餐机
        Integer chooseView = singleUpReqVo.getChooseView();


        singleUpRespVO.setSingleUpId(commodityId);
        singleUpRespVO.setChooseView(chooseView);

        List<CommodityGroupSingle> singles = commodityGroupSingleService.selectByChooseViewAndSpuIdForDown(commodityId, chooseView);
        if (CollectionUtils.isEmpty(singles)) {
            return singleUpRespVO;
        }
        //获得套餐 id
        List<Long> setmealSpuIds = singles.stream().map(CommodityGroupSingle::getSpuId).distinct().toList();

        //获得商品
        List<CommoditySpus> spus = getSpursByCommodityIds(setmealSpuIds);
        //获得分组
        List<CommoditySetmealGroup> commoditySetmealGroups = commoditySetmealGroupService.selectBySpuIds(setmealSpuIds);
        //获得子品
        List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleService.selectByCommodityIds(setmealSpuIds);
        //将分组做成 Map
        Map<Long, List<CommoditySetmealGroup>> groupMap = commoditySetmealGroups.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));
        //将子品做成 Map
        Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getSpuId));
        List<CommoditySpusByIdRespVo> commoditySpusByIdRespVos = new ArrayList<>();
        for (CommoditySpus commoditySpus : spus) {
                List<CommoditySetmealGroup> commoditySetmealGroups1 = groupMap.get(commoditySpus.getCommodityId());
                List<CommodityGroupSingle> commodityGroupSingles1 = singleMap.get(commoditySpus.getCommodityId());
                List<Long> commodityIDs = new ArrayList<>();
                commodityIDs.add(commodityId);
                // 仅当本次触发上架的单品命中了“非加价组”时，才需要提示套餐可上架。
                if (!triggeredByNonSurchargeGroup(commoditySetmealGroups1, commodityGroupSingles1, commodityIDs)) {
                    continue;
                }
                // 单品上架查询：加价组不会影响套餐是否可上架
                Boolean b = getOrUp(commoditySpus,commoditySetmealGroups1,commodityGroupSingles1,chooseView,commodityIDs);

            if (Boolean.TRUE.equals(b)){
                CommoditySpusByIdRespVo commoditySpusByIdRespVo = new CommoditySpusByIdRespVo();
                BeanUtils.copyProperties(commoditySpus, commoditySpusByIdRespVo);
                if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())){
                    commoditySpusByIdRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                }
                commoditySpusByIdRespVos.add(commoditySpusByIdRespVo);
            }


        }
        singleUpRespVO.setCommoditySpusList(commoditySpusByIdRespVos);

        return singleUpRespVO;
    }

    /**
     * 判断本次触发上架的单品，是否命中套餐中的非加价组。
     * 若仅命中加价组，则不触发“可联动上架套餐”的提醒。
     */
    private boolean triggeredByNonSurchargeGroup(List<CommoditySetmealGroup> groups,
                                                 List<CommodityGroupSingle> singles,
                                                 List<Long> triggerCommodityIds) {
        if (CollectionUtils.isEmpty(groups) || CollectionUtils.isEmpty(singles) || CollectionUtils.isEmpty(triggerCommodityIds)) {
            return false;
        }
        Set<Long> nonSurchargeGroupIds = groups.stream()
            .filter(group -> group.getAttribute() != SpuEnum.SURCHARGE_GROUP.getCode())
            .map(CommoditySetmealGroup::getGroupId)
            .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(nonSurchargeGroupIds)) {
            return false;
        }
        return singles.stream().anyMatch(single ->
            triggerCommodityIds.contains(single.getCommodityId()) && nonSurchargeGroupIds.contains(single.getGroupId()));
    }

    private Boolean getOrUp(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView, List<Long> commodityIDs) {
        //1 wx 2 dcj
        if (chooseView.equals(SpuEnum.WX.getCode())){
            return getWXBoolean(commoditySpus, commoditySetmealGroups1, commodityGroupSingles1, chooseView, commodityIDs);
        }else {
            return getStoreBoolean(commoditySpus, commoditySetmealGroups1, commodityGroupSingles1, chooseView, commodityIDs);
        }


    }

    private static Boolean getStoreBoolean(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView, List<Long> commodityIDs) {
        if (commoditySpus.getStoreStatus().equals(SpuEnum.UP.getCode())) {
            //如果商品本来就上架不用管
            return false;
        }else {
            //模拟给子品上架
            simulatedShelfRemoval(commodityGroupSingles1,chooseView,commodityIDs,SpuEnum.UP.getCode());
            //1.固定搭配套餐，2.分组可选套餐
            if (commoditySpus.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityGroupSingle> filteredList = commodityGroupSingles1.stream()
                        .filter(item ->  item.getStoreStatus() == SpuEnum.DOWN.getCode())
                        .collect(Collectors.toList());
                //如果是空就说明需要上架
                return CollectionUtils.isEmpty(filteredList);
            }else {
                //分组可选
                Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles1.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));
                boolean up = true;
                for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroups1) {
                    // 加价组不参与套餐上架判定
                    if (commoditySetmealGroup.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
                        continue;
                    }


                    List<CommodityGroupSingle> commodityGroupSingles2 = singleMap.get(commoditySetmealGroup.getGroupId());
                    if (CollectionUtils.isEmpty(commodityGroupSingles2)) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(up)){
                        //1 可选 2 固定
                        if (ObjectUtil.equals(commoditySetmealGroup.getAttribute(),SpuEnum.FIXED.getCode())  ) {
                            //固定

                            List<CommodityGroupSingle> filteredList = commodityGroupSingles2.stream()
                                    .filter(item ->  item.getStoreStatus().equals(SpuEnum.UP.getCode()) )
                                    .toList();
                            //如果上架的数量是分组内所有品的数量，说明该分组满足上线条件
                            up = filteredList.size() == commodityGroupSingles2.size();


                        } else {
                             // 分组内可选
                            // 筛选出必选品
                            List<CommodityGroupSingle> filteredList = commodityGroupSingles2.stream()
                                    .filter(item ->  item.getRequiredChoose().equals(SpuEnum.AFFIRMATIVELY.getCode()))
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()) )
                                    .toList();
                            if (ObjectUtil.isNotEmpty(filteredList)){
                                //如果必选品有下架的，套餐不允许上架
                                up = false;
                                break;
                            }



                            //同一商品是否可选多份 1 是 0否
                            Integer chooseMany = commoditySetmealGroup.getChooseMany();
                            //必选数量
                            Long choose = commoditySetmealGroup.getChoose();
                            List<CommodityGroupSingle> filteredListSingle = commodityGroupSingles2.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.UP.getCode()))
                                    .toList();

                            if (chooseMany.equals(SpuEnum.MULTIPLE_SELECTION.getCode())){
                                //如果商品可以重复选择
                                if (ObjectUtil.isEmpty(filteredListSingle)){
                                    //如果分组里所有品都下架了
                                    up = false;

                                }
                            }else {
                                //如果商品不可以重复选择，并且上架的商品不等于必选的数量
                                if (choose > filteredListSingle.size()){
                                    up = false;

                                }
                            }


                        }
                    }else {
                        break;
                    }

                }
                return up;
            }
        }
    }

    private static Boolean getWXBoolean(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView, List<Long> commodityIDs) {
        if (commoditySpus.getWxStatus().equals(SpuEnum.UP.getCode())){
            //如果商品本来就上架不用管
            return false;
        }else {
            //模拟给子品上架
            simulatedShelfRemoval(commodityGroupSingles1,chooseView,commodityIDs,SpuEnum.UP.getCode());
            //1.固定搭配套餐，2.分组可选套餐
            if (commoditySpus.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityGroupSingle> filteredList = commodityGroupSingles1.stream()
                        .filter(item ->   item.getWxStatus() == SpuEnum.DOWN.getCode())
                        .collect(Collectors.toList());
                //如果是空就说明需要上架
                return CollectionUtils.isEmpty(filteredList);
            }else {
                //分组可选
                Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles1.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));
                boolean up = true;
                for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroups1) {
                    // 加价组不参与套餐上架判定
                    if (commoditySetmealGroup.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
                        continue;
                    }

                    List<CommodityGroupSingle> commodityGroupSingles2 = singleMap.get(commoditySetmealGroup.getGroupId());
                    if (CollectionUtils.isEmpty(commodityGroupSingles2)) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(up)){
                        //1 可选 2 固定
                        if (commoditySetmealGroup.getAttribute() == SpuEnum.FIXED.getCode()) {
                            //分组内固定

                            List<CommodityGroupSingle> filteredList = commodityGroupSingles2.stream()
                                    .filter(item ->  item.getWxStatus() == SpuEnum.UP.getCode())
                                    .toList();
                            //如果上架的数量是分组内所有品的数量，说明该分组满足上线条件
                            up = filteredList.size() == commodityGroupSingles2.size();


                        } else {
                            //分组内可选
                            // 筛选出必选品
                            List<CommodityGroupSingle> filteredList = commodityGroupSingles2.stream()
                                    .filter(item ->  item.getRequiredChoose() == SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item -> item.getWxStatus() == SpuEnum.DOWN.getCode())
                                    .toList();
                            if (ObjectUtil.isNotEmpty(filteredList)){
                                //如果必选品有下架的，套餐不允许上架
                                up = false;
                                break;
                            }



                            //同一商品是否可选多份 1 是 0否
                            Integer chooseMany = commoditySetmealGroup.getChooseMany();
                            //必选数量
                            Long choose = commoditySetmealGroup.getChoose();
                            List<CommodityGroupSingle> filteredListSingle = commodityGroupSingles2.stream()
                                    .filter(item -> item.getWxStatus() == SpuEnum.UP.getCode())
                                    .toList();

                            if (chooseMany==SpuEnum.MULTIPLE_SELECTION.getCode()){
                                //如果商品可以重复选择
                                if (ObjectUtil.isEmpty(filteredListSingle)){
                                    //如果分组里所有品都下架了
                                    up = false;

                                }
                            }else {
                                //如果商品不可以重复选择，并且上架的商品小于必选的数量
                                if (choose > filteredListSingle.size()){
                                    up = false;

                                }
                            }


                        }
                    }else {
                        break;
                    }

                }
                return up;

            }
        }
    }

    /**
     * 单品上架联动套餐上架
     * @param singleUpReqVo
     */
    @Override
    @LogRecord(type = COMMODITY_TYPE, subType = COMMODITY_SINGLE_UP, bizNo = "{{#singleUpReqVo.singleUpId}}", success = COMMODITY_SINGLE_UP_SUCCESS)
    public void singleUpConfirm(SingleUpConfirmReqVO singleUpReqVo) {
        Long singleUpId = singleUpReqVo.getSingleUpId();
        Integer chooseView = singleUpReqVo.getChooseView();
        //操作单品上架
        //需要上架的集合
        List<Long> upIDs = new ArrayList<>();
        upIDs.add(singleUpId);
        //操作子品上架
        commodityGroupSingleService.updateUpOrDownBySpuId(chooseView, upIDs,SpuEnum.UP.getCode());
        if (ObjectUtil.isNotEmpty(singleUpReqVo.getPackageIds())){
            upIDs.addAll(singleUpReqVo.getPackageIds());
        }
        //操作套餐和单品上架
        SpusUpOrDown(upIDs, chooseView,SpuEnum.UP.getCode());





        LogRecordContext.putVariable("singleUpReqVo", singleUpReqVo);
    }

    private void SpusUpOrDown(List<Long> commodityIds, Integer chooseView ,Integer upOrDown) {
        LambdaUpdateWrapper<CommoditySpus> spuUpdateWrapper = new LambdaUpdateWrapper<>();
        spuUpdateWrapper.in(CommoditySpus::getCommodityId, commodityIds);
        if (chooseView.equals(SpuEnum.WX.getCode())){
            spuUpdateWrapper.set(CommoditySpus::getWxStatus, upOrDown);
        }else {
            spuUpdateWrapper.set(CommoditySpus::getStoreStatus, upOrDown);
        }
        commoditySpusMapper.update(spuUpdateWrapper);
    }



    /**
     * 批量下架
     * @param batchDownSelectReqVO
     * @return
     */
    @Override
    public BatchDownSelectRespVO  batchDownSelect(BatchDownSelectReqVO batchDownSelectReqVO) {
        if (ObjectUtil.isEmpty(batchDownSelectReqVO.getSingleIds()) && ObjectUtil.isEmpty(batchDownSelectReqVO.getPackageIds())){
            throw new ServiceException(SPU_NOT_VALUE);
        }

        BatchDownSelectRespVO batchDownSelectRespVO = new BatchDownSelectRespVO();

        Integer chooseView = batchDownSelectReqVO.getChooseView();
        List<Long> singleIds = batchDownSelectReqVO.getSingleIds();
        List<Long> packageIds = batchDownSelectReqVO.getPackageIds();
        //1.找到单品下架相关套餐也会下架的

        batchDownSelectRespVO.setChooseView(chooseView);
        batchDownSelectRespVO.setSingleIds(singleIds);
        batchDownSelectRespVO.setPackageIds(packageIds);

        if (ObjectUtil.isNotEmpty(singleIds)) {
            // 2. 查询需要下架的套餐
            List<CommoditySpus> commoditySpusList = findNeedDownPackage(singleIds, chooseView);

            // 3. 非空判断后，直接转换并过滤（合并嵌套逻辑）
            if (ObjectUtil.isNotEmpty(commoditySpusList)) {
                List<CommoditySpusQueryReqVo> queryReqVos = commoditySpusList.stream()
                        // 3. 按需过滤：packageIds 非空时排除包含的商品ID，空时直接保留所有
                        .filter(cs -> ObjectUtil.isEmpty(packageIds) || !packageIds.contains(cs.getCommodityId()))
                        // 4. Stream 映射：替代手动 for 循环 + BeanUtils 复制
                        .map(this::convertToQueryReqVo)
                        .collect(Collectors.toList());

                // 5. 设置结果（无需重复写赋值逻辑）
                batchDownSelectRespVO.setCommoditySpusQueryReqVos(queryReqVos);
            }
        }




        return batchDownSelectRespVO;
    }

    /**
     * 找到正在上架，但本次操作需要下架的品
     * @param singleIds
     * @param chooseView
     * @return
     */
    private List<CommoditySpus>  findNeedDownPackage(List<Long> singleIds, Integer chooseView) {

        List<CommoditySpus> commoditySpusList = new ArrayList<>();

        //去找相关连子品
        List<CommodityGroupSingle> singles = commodityGroupSingleService.selectBySpuIds(singleIds);
        if (ObjectUtil.isNotEmpty(singles)) {
            //获得套餐 IDs
            List<Long> packageIds = singles.stream().map(CommodityGroupSingle::getSpuId).distinct().toList();

            //获得商品
            LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommoditySpus::getCommodityId, packageIds);
            List<CommoditySpus> packageList = commoditySpusMapper.selectList(queryWrapper);

            //获得分组
            List<CommoditySetmealGroup> commoditySetmealGroups = commoditySetmealGroupService.selectBySpuIds(packageIds);
            //获得子品
            List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleService.selectByCommodityIds(packageIds);

            Map<Long, List<CommoditySetmealGroup>> groupMap = commoditySetmealGroups.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));

            Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getSpuId));

            for (CommoditySpus commoditySpus : packageList) {
                List<CommoditySetmealGroup> commoditySetmealGroups1 = groupMap.get(commoditySpus.getCommodityId());

                List<CommodityGroupSingle> commodityGroupSingles1 = singleMap.get(commoditySpus.getCommodityId());


                boolean b = getOrDown(commoditySpus,commoditySetmealGroups1,commodityGroupSingles1,chooseView,singleIds);
                if (b){
                    commoditySpusList.add(commoditySpus);
                }

            }



        }


        return commoditySpusList;
    }

    private boolean getOrDown(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView, List<Long> singleIds) {


        if (chooseView.equals(SpuEnum.WX.getCode())) {
            return getWxBooleanDown(commoditySpus,commoditySetmealGroups1,commodityGroupSingles1,chooseView,singleIds);
        }else {
            return getStoreBooleanDown(commoditySpus,commoditySetmealGroups1,commodityGroupSingles1,chooseView,singleIds);
        }

    }

    /**
     * 找到适配下架的套餐
     * @param commoditySpus
     * @param commoditySetmealGroups1
     * @param commodityGroupSingles1
     * @param chooseView
     * @param singleIds
     * @return
     */
    private boolean getWxBooleanDown(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView , List<Long> singleIds) {
        if (commoditySpus.getWxStatus().equals(SpuEnum.DOWN.getCode())) {
            //如果就是下架状态不用管
            return  false;
        }else {
            //模拟子品下架下架
            simulatedShelfRemoval(commodityGroupSingles1,chooseView,singleIds,SpuEnum.DOWN.getCode());
            // 1.固定搭配 2.分组可选套餐
            if (commoditySpus.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())) {
                List<CommodityGroupSingle> filteredList = commodityGroupSingles1.stream()
                        .filter(item -> item.getWxStatus().equals(SpuEnum.DOWN.getCode()))
                        .toList();
                //但凡有下架的，做下架处理
                return CollectionUtils.isNotEmpty(filteredList);
            } else {
                //分组可选 只要有一个分组不满足下架 直接 return true
                Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles1.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));
                boolean b = false;
                //true 说明需要下架
                for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroups1) {
                    if (!b){
                        // 加价组不参与套餐下架判定
                        if (commoditySetmealGroup.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
                            continue;
                        }
                        List<CommodityGroupSingle> commodityGroupSingles = singleMap.get(commoditySetmealGroup.getGroupId());
                        if (CollectionUtils.isEmpty(commodityGroupSingles)) {
                            continue;
                        }
                        //1 可选 2固定

                        if (commoditySetmealGroup.getAttribute() == SpuEnum.FIXED.getCode()) {
                            //分组固定
                            List<CommodityGroupSingle> filteredList = commodityGroupSingles.stream()
                                    .filter(item -> item.getWxStatus().equals(SpuEnum.DOWN.getCode()))
                                    .toList();
                            //如果分组内但凡有下架的 就给套餐下架
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                               b = true;
                               break;

                            }
                        } else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityGroupSingle> list = commodityGroupSingles.stream()
                                    .filter(item -> item.getRequiredChoose() == SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item -> item.getWxStatus() == SpuEnum.DOWN.getCode())
                                    .toList();
                            if (CollectionUtils.isNotEmpty(list)){
                                //如果筛选出来的必选品有一个是下架的 那么就需要下架
                                b= true;
                                break;

                            }


                            //同一商品是否可以多选
                            Integer chooseMany = commoditySetmealGroup.getChooseMany();
                            //必选数量
                            Long choose = commoditySetmealGroup.getChoose();
                            List<CommodityGroupSingle> list1 = commodityGroupSingles.stream()
                                    .filter(item -> item.getWxStatus().equals(SpuEnum.UP.getCode()))
                                    .toList();
                            if (chooseMany == SpuEnum.MULTIPLE_SELECTION.getCode()){
                                b =  ObjectUtil.isEmpty(list1);
                            }else {
                                b = choose > list1.size();
                            }
                        }
                    }

                }
                return  b;//所有分组都满足售卖条件 所以不予下架
            }
        }
    }

    /**
     * 模拟子品上下架
     * @param commodityGroupSingles1
     * @param chooseView
     * @param singleIds
     * @param code
     */
    private static void simulatedShelfRemoval(List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView, List<Long> singleIds, int code) {
        if (ObjectUtil.isNotEmpty(singleIds)){
            commodityGroupSingles1.stream()
                    .filter(cgs ->  singleIds.contains(cgs.getCommodityId()))
                    .forEach(cgs -> {
                        if (chooseView == SpuEnum.WX.getCode()) {
                            cgs.setWxStatus(code);
                        } else {
                            cgs.setStoreStatus(code);
                        }
                    });
        }




    }

    private boolean getStoreBooleanDown(CommoditySpus commoditySpus, List<CommoditySetmealGroup> commoditySetmealGroups1, List<CommodityGroupSingle> commodityGroupSingles1, Integer chooseView , List<Long> singleIds) {
        if (commoditySpus.getStoreStatus().equals(SpuEnum.DOWN.getCode())) {
            //如果就是下架状态不用管
            return  false;
        }else {
            //模拟子品下架下架
            simulatedShelfRemoval(commodityGroupSingles1,chooseView,singleIds,SpuEnum.DOWN.getCode());
            // 1.固定搭配 2.分组可选套餐
            if (commoditySpus.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())) {
                List<CommodityGroupSingle> filteredList = commodityGroupSingles1.stream()
                        .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()))
                        .toList();
                //但凡有下架的，做下架处理
                return CollectionUtils.isNotEmpty(filteredList);
            } else {
                //分组可选 只要有一个分组不满足下架 直接 return true
                Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles1.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getGroupId));
                //true 说明需要下架
                boolean b = false;
                for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroups1) {
                    if (!b){
                        // 加价组不参与套餐下架判定
                        if (commoditySetmealGroup.getAttribute() == SpuEnum.SURCHARGE_GROUP.getCode()) {
                            continue;
                        }
                        List<CommodityGroupSingle> commodityGroupSingles = singleMap.get(commoditySetmealGroup.getGroupId());
                        if (CollectionUtils.isEmpty(commodityGroupSingles)) {
                            continue;
                        }
                        //1 可选 2固定

                        if (commoditySetmealGroup.getAttribute() == SpuEnum.FIXED.getCode()) {
                            //分组固定
                            List<CommodityGroupSingle> filteredList = commodityGroupSingles.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()))
                                    .toList();
                            //如果分组内但凡有下架的 就给套餐下架
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                                b= true;
                                break;
                            }
                        } else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityGroupSingle> list = commodityGroupSingles.stream()
                                    .filter(item -> item.getRequiredChoose() == SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item -> item.getStoreStatus() == SpuEnum.DOWN.getCode())
                                    .toList();
                            if (CollectionUtils.isNotEmpty(list)){
                                //如果筛选出来的必选品有一个是下架的 那么就需要下架
                                b=  true;
                                break;

                            }

                            //同一商品是否可以多选
                            Integer chooseMany = commoditySetmealGroup.getChooseMany();
                            //必选数量
                            Long choose = commoditySetmealGroup.getChoose();
                            List<CommodityGroupSingle> list1 = commodityGroupSingles.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.UP.getCode()))
                                    .toList();
                            if (chooseMany == SpuEnum.MULTIPLE_SELECTION.getCode()){
                                b= ObjectUtil.isEmpty(list1);
                            }else {
                                b= choose > list1.size();
                            }
                        }
                    }

                }
                return  b;//所有分组都满足售卖条件 所以不予下架
            }
        }
    }



    // 6. 提取 DTO 转换公共方法（消除重复代码）
    private CommoditySpusQueryReqVo convertToQueryReqVo(CommoditySpus commoditySpus) {
        CommoditySpusQueryReqVo queryReqVo = new CommoditySpusQueryReqVo();
        BeanUtils.copyProperties(commoditySpus, queryReqVo);
        if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())) {
            queryReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
        }
        return queryReqVo;
    }

    /**
     * 批量下架确认操作
     * @param batchDownSelectReqVO
     */
    @Override
    public void batchDownConfirm(BatchDownSelectReqVO batchDownSelectReqVO) {
        if (ObjectUtil.isEmpty(batchDownSelectReqVO.getSingleIds()) && ObjectUtil.isEmpty(batchDownSelectReqVO.getPackageIds())){
            throw new ServiceException(SPU_NOT_VALUE);
        }
        List<Long> packageIds = batchDownSelectReqVO.getPackageIds();
        List<Long> singleIds = batchDownSelectReqVO.getSingleIds();
        Integer chooseView = batchDownSelectReqVO.getChooseView();
        List<Long> allSpuIds = new ArrayList<>();

        if(ObjectUtil.isNotEmpty(singleIds)){
            //操作子品下架
            commodityGroupSingleService.updateUpOrDownBySpuId(chooseView,singleIds,SpuEnum.DOWN.getCode());
            allSpuIds.addAll(singleIds);
        }
        if (ObjectUtil.isNotEmpty(packageIds)){
            allSpuIds.addAll(packageIds);
        }


        if (ObjectUtil.isNotEmpty(allSpuIds)){
            LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(CommoditySpus::getCommodityId, allSpuIds);
            if (chooseView.equals(SpuEnum.WX.getCode()) ) {
                updateWrapper.set(CommoditySpus::getWxStatus,SpuEnum.DOWN.getCode());
            }else {
                updateWrapper.set(CommoditySpus::getStoreStatus,SpuEnum.DOWN.getCode());
            }
            commoditySpusMapper.update(updateWrapper);
        }



    }

    /**
     * 批量上架
     * @param batchUpSelectReqVO
     * @return
     */
    @Override
    public BatchUpSelectRespVO batchUpSelect(BatchUpSelectReqVO batchUpSelectReqVO) {
        BatchUpSelectRespVO batchUpSelectRespVO = new BatchUpSelectRespVO();
        Integer chooseView = batchUpSelectReqVO.getChooseView();
        batchUpSelectRespVO.setChooseView(chooseView);
        List<Long> singleIds = batchUpSelectReqVO.getSingleIds();
        batchUpSelectRespVO.setSingleIds(singleIds);
        List<Long> packageIds = batchUpSelectReqVO.getPackageIds();
        //拿到单品所指的子品集

        List<CommodityGroupSingle> commodityGroupSingles = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(singleIds)){
            commodityGroupSingles = commodityGroupSingleService.selectBySpuIds(singleIds);
        }
        List<Long> allPackageIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(commodityGroupSingles)){
            List<Long> singleForPackageIds = new ArrayList<>(commodityGroupSingles.stream().map(CommodityGroupSingle::getSpuId).toList());
            allPackageIds.addAll(singleForPackageIds);
        }



        if (ObjectUtil.isNotEmpty(packageIds)){
            allPackageIds.addAll(packageIds);
        }

        if (ObjectUtil.isNotEmpty(allPackageIds)){

            LambdaQueryWrapper<CommoditySpus> queryWrapperDown = new LambdaQueryWrapper<>();
            queryWrapperDown.in(CommoditySpus::getCommodityId, allPackageIds);
            if (chooseView.equals(SpuEnum.WX.getCode())){
                queryWrapperDown.eq(CommoditySpus::getWxStatus,SpuEnum.DOWN.getCode());
            }else {
                queryWrapperDown.eq(CommoditySpus::getStoreStatus,SpuEnum.DOWN.getCode());
            }
            List<CommoditySpus> commoditySpuses = commoditySpusMapper.selectList(queryWrapperDown);
            if (ObjectUtil.isNotEmpty(commoditySpuses)){

                List<Long> allDownIds = commoditySpuses.stream().map(CommoditySpus::getCommodityId).toList();

                packageIds = packageIds.stream()
                        .filter(allDownIds::contains)
                        .toList();


                List<CommoditySpus> commoditySpusList = findNeedDownPackageByPackageIds(allDownIds,singleIds,chooseView);

                if (ObjectUtil.isEmpty(commoditySpusList)){
                    if (ObjectUtil.isNotEmpty(packageIds)){
                        List<CommoditySpusQueryReqVo> commoditySpusQueryReqVos = new ArrayList<>();
                        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(CommoditySpus::getCommodityId, packageIds);
                        List<CommoditySpus> commoditySpusList1 = commoditySpusMapper.selectList(queryWrapper);
                        for (CommoditySpus commoditySpus : commoditySpusList1) {
                            CommoditySpusQueryReqVo queryReqVo = new CommoditySpusQueryReqVo();
                            BeanUtils.copyProperties(commoditySpus, queryReqVo);
                            if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())){
                                queryReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                            }
                            commoditySpusQueryReqVos.add(queryReqVo);
                        }
                        batchUpSelectRespVO.setPackageDown(commoditySpusQueryReqVos);
                    }
                }else {
                    if (ObjectUtil.isNotEmpty(packageIds)){
                        List<Long> commodityIds = commoditySpusList.stream().map(CommoditySpus::getCommodityId).toList();

                        List<Long> difference = packageIds.stream()
                                .filter(id -> !commodityIds.contains(id)) // 过滤掉在commodityIds中存在的元素
                                .toList(); // 收集结果到新列表
                        //找到不满足上架的套餐 id
                        if (ObjectUtil.isNotEmpty(difference)){
                            LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.in(CommoditySpus::getCommodityId, difference);
                            List<CommoditySpus> commoditySpusList1 = commoditySpusMapper.selectList(queryWrapper);
                            List<CommoditySpusQueryReqVo> commoditySpusQueryReqVos = new ArrayList<>();
                            for (CommoditySpus commoditySpus : commoditySpusList1) {
                                CommoditySpusQueryReqVo commoditySpusQueryReqVo = new CommoditySpusQueryReqVo();
                                BeanUtils.copyProperties(commoditySpus, commoditySpusQueryReqVo);
                                if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())){
                                    commoditySpusQueryReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                                }
                                commoditySpusQueryReqVos.add(commoditySpusQueryReqVo);
                            }

                            batchUpSelectRespVO.setPackageDown(commoditySpusQueryReqVos);
                        }

                        //找到未勾选但满足上架的套餐
                        List<Long> finalPackageIds = packageIds;
                        List<Long> difference2 = commodityIds.stream()
                                .filter(id -> !finalPackageIds.contains(id)) // 过滤掉在packageIds中存在的元素
                                .toList(); // 收集结果到新列表
                        if (ObjectUtil.isNotEmpty(difference2)){
                            List<CommoditySpusQueryReqVo> commoditySpusQueryReqVos = new ArrayList<>();
                            for (CommoditySpus commoditySpus : commoditySpusList) {
                                if (difference2.contains(commoditySpus.getCommodityId())){
                                    CommoditySpusQueryReqVo commoditySpusQueryReqVo = new CommoditySpusQueryReqVo();
                                    BeanUtils.copyProperties(commoditySpus, commoditySpusQueryReqVo);
                                    if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())){
                                        commoditySpusQueryReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                                    }
                                    commoditySpusQueryReqVos.add(commoditySpusQueryReqVo);
                                }
                            }
                            batchUpSelectRespVO.setPackageUp(commoditySpusQueryReqVos);
                        }
                        //找到勾选了，并且满足上架的 ID
                        List<Long> difference3 = packageIds.stream()
                                .filter(commodityIds::contains) // 过滤出在commodityIds中存在的元素
                                .toList();
                        batchUpSelectRespVO.setPackageIds(difference3);
                    }else {
                        List<CommoditySpusQueryReqVo> commoditySpusQueryReqVos = new ArrayList<>();
                        for (CommoditySpus commoditySpus : commoditySpusList) {
                            CommoditySpusQueryReqVo commoditySpusQueryReqVo = new CommoditySpusQueryReqVo();
                            BeanUtils.copyProperties(commoditySpus, commoditySpusQueryReqVo);
                            if (ObjectUtil.isNotEmpty(commoditySpus.getImageUrl())){
                                commoditySpusQueryReqVo.setImageUrlVO(ConvertUtil.convertStringToListS(commoditySpus.getImageUrl()));
                            }
                            commoditySpusQueryReqVos.add(commoditySpusQueryReqVo);
                        }
                        batchUpSelectRespVO.setPackageUp(commoditySpusQueryReqVos);
                    }
                }
            }










        }




        return batchUpSelectRespVO;
    }

    private List<CommoditySpus> findNeedDownPackageByPackageIds(List<Long> allPackageIds, List<Long> singleIds, Integer chooseView) {
        List<CommoditySpus> commoditySpusListReturn = new ArrayList<>();
        LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommoditySpus::getCommodityId, allPackageIds);
        List<CommoditySpus> commoditySpusList = commoditySpusMapper.selectList(queryWrapper);

        List<CommoditySetmealGroup> commoditySetmealGroups = commoditySetmealGroupService.selectBySpuIds(allPackageIds);

        List<CommodityGroupSingle> commodityGroupSingles = commodityGroupSingleService.selectByCommodityIds(allPackageIds);

        if (ObjectUtil.isNotEmpty(singleIds)){
            //模拟子品上架
            simulatedShelfRemoval(commodityGroupSingles,chooseView,singleIds,SpuEnum.UP.getCode());
        }
        Map<Long, List<CommoditySetmealGroup>> groupMap = commoditySetmealGroups.stream().collect(Collectors.groupingBy(CommoditySetmealGroup::getCommodityId));
        Map<Long, List<CommodityGroupSingle>> singleMap = commodityGroupSingles.stream().collect(Collectors.groupingBy(CommodityGroupSingle::getSpuId));

        for (CommoditySpus commoditySpus : commoditySpusList) {
            List<CommoditySetmealGroup> commoditySetmealGroups1 = groupMap.get(commoditySpus.getCommodityId());
            List<CommodityGroupSingle> commodityGroupSingles1 = singleMap.get(commoditySpus.getCommodityId());
            // 批量上架场景：若本次勾选单品仅命中加价组，则不提示该套餐可上架。
            if (ObjectUtil.isNotEmpty(singleIds)
                && !triggeredByNonSurchargeGroup(commoditySetmealGroups1, commodityGroupSingles1, singleIds)) {
                continue;
            }

            Boolean orUp = getOrUp(commoditySpus, commoditySetmealGroups1, commodityGroupSingles1, chooseView, singleIds);
            if (orUp){
                commoditySpusListReturn.add(commoditySpus);
            }
        }



        return commoditySpusListReturn;
    }

    @Override
    public void batchUpConfirm(BatchUpSelectReqVO batchUpSelectReqVO) {
        Integer chooseView = batchUpSelectReqVO.getChooseView();
        List<Long> packageIds = batchUpSelectReqVO.getPackageIds();
        List<Long> singleIds = batchUpSelectReqVO.getSingleIds();

        List<Long> allSpuIds = new ArrayList<>();

        //先把子品上架
        if(ObjectUtil.isNotEmpty(singleIds)){

            commodityGroupSingleService.updateUpOrDownBySpuId(chooseView,singleIds,SpuEnum.UP.getCode());
            allSpuIds.addAll(singleIds);
        }
        if (ObjectUtil.isNotEmpty(packageIds)){
            allSpuIds.addAll(packageIds);
        }
        //再把相应商品上架
        if (ObjectUtil.isNotEmpty(allSpuIds)){
            LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(CommoditySpus::getCommodityId, allSpuIds);
            if (chooseView.equals(SpuEnum.WX.getCode())){
                updateWrapper.set(CommoditySpus::getWxStatus,SpuEnum.UP.getCode());
            }else {
                updateWrapper.set(CommoditySpus::getStoreStatus,SpuEnum.UP.getCode());
            }
            commoditySpusMapper.update(updateWrapper);
        }




    }


    /**
     * 上架锁定商品
     * @param commoditySpuLockReqVO
     */
    @Override
    public void updateShelfLock(CommoditySpuLockReqVO commoditySpuLockReqVO) {
        LambdaUpdateWrapper<CommoditySpus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommoditySpus::getCommodityId, commoditySpuLockReqVO.getCommodityId());
        updateWrapper.set(CommoditySpus::getShelfLock, commoditySpuLockReqVO.getShelfLock());
        commoditySpusMapper.update(updateWrapper);

    }
}
