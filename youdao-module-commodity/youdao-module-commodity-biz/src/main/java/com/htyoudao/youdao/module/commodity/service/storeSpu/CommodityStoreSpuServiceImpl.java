package com.htyoudao.youdao.module.commodity.service.storeSpu;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.htyoudao.youdao.framework.common.core.KeyValue;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.cache.CacheUtils;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mq.rabbitmq.enums.RabbitMQConstant;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.RecipeCommodityStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.PriceResultDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.TemplatePriceUpdatePriceVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown.StoreBatchDownSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchUp.StoreBatchUpSelectRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeGroup.CommodityStoreGroupSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.CommodityStoreSingleSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSimpleRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.StoreSingleSkuVO;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.CommodityStoreSpuDTO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.convert.CommodityConvertor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySpusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSkuMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSpuMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.afterorder.AfterOrderMapper;
import com.htyoudao.youdao.module.commodity.dal.redis.CommodityStoreRedisDao;
import com.htyoudao.youdao.module.commodity.framework.config.CommodityStoreSpuConfig;
import com.htyoudao.youdao.module.commodity.enums.ClientType;
import com.htyoudao.youdao.module.commodity.enums.CommodityPackageType;
import com.htyoudao.youdao.module.commodity.enums.SpuEnum;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import com.htyoudao.youdao.module.commodity.service.storeCategory.ICommodityStoreCategoryService;
import com.htyoudao.youdao.module.commodity.service.storeGroup.ICommodityStoreGroupService;
import com.htyoudao.youdao.module.commodity.service.storeSingle.ICommodityStoreSingleService;
import com.htyoudao.youdao.module.commodity.service.storeSku.ICommodityStoreSkuService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.strategy.CommodityClientStrategy;
import com.htyoudao.youdao.module.commodity.service.storeSpu.strategy.CommodityStrategyFactory;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncCategoryDTO;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncSetMealDTO;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncSpuDTO;
import com.htyoudao.youdao.module.commodity.service.template.ICommodityTemplateService;
import com.htyoudao.youdao.module.commodity.service.templateCategory.ICommodityTemplateCategoryService;
import com.htyoudao.youdao.module.commodity.service.templateGroup.ICommodityTemplateSetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.templateSingle.ICommodityTemplateGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.templateSku.ICommodityTemplateSkusService;
import com.htyoudao.youdao.module.commodity.service.templateSpu.ICommodityTemplateSpusService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.htyoudao.youdao.module.commodity.util.TimeSharingUtil;
import com.htyoudao.youdao.module.commodity.util.TruncateTableUtil;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzCategoryTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzPlaceOrderTypeEnum;
import com.htyoudao.youdao.module.system.api.permission.PermissionApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.StoreInfoApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@Service
@Slf4j
@ComponentScan
public class CommodityStoreSpuServiceImpl implements ICommodityStoreSpuService {


    @Resource
    private CommodityStoreSpuMapper commodityStoreSpuMapper;

    @Resource
    private ICommodityTemplateSpusService commodityTemplateSpusService;

    @Resource
    private RedissonClient redissonClient;

    @DubboReference
    private ActivityApi activityApi;

    @Resource
    private ThreadPoolExecutor activityTagExecutor;

    @DubboReference
    private StoreInfoApi storeInfoApi;

    @Resource
    private ICommodityTemplateCategoryService commodityTemplateCategoryService;

    @Resource
    private ICommodityTemplateService commodityTemplateService;

    @Resource
    private ICommodityTemplateSkusService commodityTemplateSkusService;

    @Resource
    private ICommodityTemplateGroupSingleService commodityTemplateGroupSingleService;

    @Resource
    private ICommodityTemplateSetmealGroupService commodityTemplateSetmealGroupService;

    @Resource
    private ICommodityStoreCategoryService commodityStoreCategoryService;

    @Resource
    private ICommodityStoreGroupService commodityStoreGroupService;

    @Resource
    private ICommodityStoreSingleService commodityStoreSingleService;

    @Resource
    private ICommodityStoreSkuService commodityStoreSkuService;

    @Resource
    private CommodityStoreSkuMapper commodityStoreSkuMapper;

    @Resource
    private CommodityStoreRedisDao storeRedisDao;

    @Resource
    private ICommodityTageService commodityTageService;

    @Resource
    private AfterOrderMapper afterOrderMapper;

    private String keyTol = "StoreStatus:";

    @Resource
    private RabbitMQService rabbitMQService;

    @Resource
    private CommoditySpusMapper commoditySpusMapper;

    @Resource
    private PermissionApi permissionApi;

    @Autowired
    private TruncateTableUtil truncateTableUtil;

    @DubboReference
    private StoreApi storeApi;

    @Resource
    private CommodityStoreSpuConfig commodityStoreSpuConfig;



    private LoadingCache<KeyValue<Long, ClientType>, List<StoreCategoryDTO>> localProductCache =
            CacheUtils.buildCache(
                    Duration.ofMinutes(1L), // 过期时间 1 分钟
                    new CacheLoader<KeyValue<Long, ClientType>, List<StoreCategoryDTO>>() {
                        @Override
                        public List<StoreCategoryDTO> load(KeyValue<Long, ClientType> key) {
                            // 缓存未命中时，调用 DB 查询方法
                            return getAllSpuFromDB(key.getKey(), key.getValue());
                        }
                    });


    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_DELETE_SUB_TYPE, bizNo = "{{#commodityTemplateId}}", success = COMMODITY_TEMP_DELETE_SUCCESS)
    public void deleteCommodityTemplateByCommodityTemplateIdNew(Long commodityTemplateId) {

        List<CommodityTemplateCategory> commodityTemplateCategories = commodityTemplateCategoryService.selectByTemplateId(commodityTemplateId);
        if (ObjectUtil.isNotEmpty(commodityTemplateCategories)) {
            throw exception(TEMP_HAS_PRODUCTS_CANNOT_DELETE);
        }

        commodityTemplateService.deleteByTemplateId(commodityTemplateId);
        LogRecordContext.putVariable("commodityTemplateId", commodityTemplateId);

    }

    @Override
    public List<TemplateSpusRespVO> selectCommodityTemplateListNew(Long templateId, Long templateCategoryId) {


        List<CommodityTemplateSpus> page = commodityTemplateSpusService.selectByTemplateCategoryId(templateCategoryId);

        CommodityTemplate commodityTemplate = commodityTemplateService.selectById(templateId);

        List<TemplateSpusRespVO> templateSpusRespVOS = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(page)) {

            List<Long> templateCommodityIds = page.stream().map(CommodityTemplateSpus::getCommodityTemplateId).toList();
            List<CommodityTemplateSkus> skusList = commodityTemplateSkusService.selectByTemplateSpuIds(templateCommodityIds);

            Map<Long, List<CommodityTemplateSkus>> skuMap = skusList.stream().collect(Collectors.groupingBy(CommodityTemplateSkus::getCommodityTemplateId));

            for (CommodityTemplateSpus commodityTemplateSpus : page) {
                TemplateSpusRespVO templateSpusRespVO = new TemplateSpusRespVO();
                BeanUtils.copyProperties(commodityTemplateSpus, templateSpusRespVO);

                if (ObjectUtil.isNotEmpty(commodityTemplateSpus.getImageUrl())) {
                    templateSpusRespVO.setImageUrlList(ConvertUtil.convertStringToListS(commodityTemplateSpus.getImageUrl()));
                }

                List<CommodityTemplateSkus> skusList1 = skuMap.get(commodityTemplateSpus.getCommodityTemplateId());

                templateSpusRespVO.setCommodityTemplateSkusList(skusList1);
                templateSpusRespVO.setChoosePrice(commodityTemplate.getChoosePrice());
                if (skusList1.size() > 1) {
                    PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPricesTemplate(skusList1);
                    templateSpusRespVO.setLowPrice(minMaxPrices.getMinIllustratePrices());
                    templateSpusRespVO.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                    templateSpusRespVO.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                    templateSpusRespVO.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                    templateSpusRespVO.setIsMoreSku(true);
                } else {
                    templateSpusRespVO.setLowPrice(skusList1.get(0).getIllustratePrices());
                    templateSpusRespVO.setLowStrikePrice(skusList1.get(0).getStrikeThroughPrice());
                    templateSpusRespVO.setIsMoreSku(false);
                }
                templateSpusRespVOS.add(templateSpusRespVO);
            }


        }

        return templateSpusRespVOS;
    }

    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_DELETE_CATE_SUB_TYPE, bizNo = "{{#templateCategoryId}}", success = COMMODITY_TEMP_DELETE_CATE_SUCCESS)
    public void deleteCommodityCategoryByIds(Long templateCategoryId) {

        List<CommodityTemplateSpus> commodityTemplateSpuses = commodityTemplateSpusService.selectByTemplateCategoryId(templateCategoryId);
        if (ObjectUtil.isNotEmpty(commodityTemplateSpuses)) {
            throw exception(TEMP_HAS_SPU_CANNOT_DELETE);
        }

        commodityTemplateCategoryService.deleteById(templateCategoryId);
        LogRecordContext.putVariable("templateCategoryId", templateCategoryId);

    }

    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_SPU_DELETE_SUB_TYPE, bizNo = "{{#comReqVO.commodityTemplateIds}}", success = COMMODITY_TEMP_SPU_DELETE_SUCCESS)
    public void deleteCommodityTemplatePriceByCommodityIds(TemplateDelComReqVO comReqVO) {

        List<Long> commodityTemplateIds = comReqVO.getCommodityTemplateIds();
        commodityTemplateSpusService.deleteByIds(commodityTemplateIds);

        commodityTemplateSkusService.deleteByTemplateSpuIds(commodityTemplateIds);

        commodityTemplateSetmealGroupService.deleteByTemplateSpuIds(commodityTemplateIds);

        commodityTemplateGroupSingleService.deleteByTemplateSpuIds(commodityTemplateIds);

        LogRecordContext.putVariable("commodityTemplateIds", commodityTemplateIds);

    }

    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_SPU_UPDATE_SUB_TYPE, bizNo = "{{#commodityTemplateId}}", success = COMMODITY_TEMP_SPU_UPDATE_SUCCESS)
    public void updateTemplatePriceV3(TemplatePriceUpdatePriceVO templatePriceVO) {
        Long commodityTemplateId = templatePriceVO.getCommodityTemplateSkus().get(0).getCommodityTemplateId();

        commodityTemplateSkusService.updateBatch(templatePriceVO.getCommodityTemplateSkus());
        LogRecordContext.putVariable("commodityTemplateId", commodityTemplateId);
    }


    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_STORE_IN_DELETE_SUB_TYPE, bizNo = "{{#storeId}}", success = COMMODITY_STORE_IN_DELETE_SUCCESS)
    public void deleteSpuByStoreId(Long storeId) {

        //删除门店下所有分类
        commodityStoreCategoryService.deleteSpuByStoreId(storeId);

        //删除门店下所有商品
        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
        commodityStoreSpuMapper.delete(queryWrapper);

        //删除门店下所有sku
        commodityStoreSkuService.deleteSpuByStoreId(storeId);
        //删除门店下所有分组
        commodityStoreGroupService.deleteSpuByStoreId(storeId);

        //删除门店下所有子品
        commodityStoreSingleService.deleteSpuByStoreId(storeId);


        storeRedisDao.clearStoreCache(storeId);

        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");

        LogRecordContext.putVariable("storeId", storeId);
    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_ALL_DELETE_SUB_TYPE, bizNo = "000000", success = COMMODITY_STORE_ALL_DELETE_SUCCESS)
    public void deleteAllStoreSpu() {
        boolean enabled = commodityStoreSpuConfig.isDeleteAllSpuEnabled();
        log.info("[deleteAllStoreSpu] commodity.store.delete-all-spu-enabled={}", enabled);
        if (!enabled) {
            throw new ServiceException(1_005_000_000, "功能已禁用");
        }

        Long businessId = BusinessContextHolder.getBusinessId();

        //删除所有分类
        commodityStoreCategoryService.deleteAllStoreSpu();

        //删除所有商品
        /*LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSpu::getBusinessId, businessId);
        queryWrapper.select(CommodityStoreSpu::getStoreId);
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);
        List<Long> storeIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(commodityStoreSpus)) {
            storeIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getStoreId).distinct().toList();
        }*/

        try {

            truncateTableUtil.safeTruncateTable("commodity_store_spu");

        } catch (SQLException e) {
            log.error("清空分类表失败", e);
            throw new RuntimeException("全表清理失败", e);
        }





        //删除所有 sku
        commodityStoreSkuService.deleteAllStoreSpu();

        //删除所有分组
        commodityStoreGroupService.deleteAllStoreSpu();

        //删除所有子品
        commodityStoreSingleService.deleteAllStoreSpu();

        List<Long> storeIds;

        storeIds =  storeApi.getAllStoreList().getCheckedData().stream().map(StoreInfoDTO::getStoreId).toList();

        if (ObjectUtil.isNotEmpty(storeIds)) {

//            log.info("拿到门店 ID，共{}条", storeIds.size());
//
//
//            storeIds.parallelStream().forEach(storeId -> {
//                try {
//                    storeRedisDao.clearStoreCache(storeId);
//                 //   rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
//                } catch (Exception e) {
//                    log.warn("清理门店缓存或发送 MQ 失败, storeId={}", storeId, e);
//                }
//            });
            this.clearStoreCaches(storeIds);
        }
        log.info("成功批量删除所有门店所有商品数据");

    }

    public void clearStoreCaches(List<Long> storeIds) {
        if (ObjectUtil.isEmpty(storeIds)) {
            return;
        }

        log.info("拿到门店 ID，共{}条", storeIds.size());

        // 你自己定：例如 20/50。建议不要超过 Redis 连接池大小
        int concurrency = 30;

        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r);
            t.setName("clear-store-cache-" + t.getId());
            t.setDaemon(true);
            return t;
        };

        ExecutorService executor = new ThreadPoolExecutor(
                concurrency,
                concurrency,
                0L,
                TimeUnit.MILLISECONDS,
                // 队列别太小：2000个任务放得下；也别太大无限制（这里给个上限）
                new LinkedBlockingQueue<>(10_000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满了就由调用线程执行，起到“反压”
        );

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        long start = System.currentTimeMillis();

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>(storeIds.size());

            for (Long storeId : storeIds) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    try {
                        storeRedisDao.clearStoreCache(storeId);
                        // rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
                        ok.incrementAndGet();
                    } catch (Exception e) {
                        fail.incrementAndGet();
                        log.warn("清理门店缓存或发送 MQ 失败, storeId={}", storeId, e);
                    }
                }, executor);

                futures.add(f);
            }

            // 等待全部完成（也可以加超时）
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
        }

        long cost = System.currentTimeMillis() - start;
        log.info("清理完成：总数={}, 成功={}, 失败={}, 耗时={}ms", storeIds.size(), ok.get(), fail.get(), cost);
    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_CATE_DELETE_SUB_TYPE, bizNo = "{{#commodityStoreCategoryId}}", success = COMMODITY_STORE_CATE_DELETE_SUCCESS)
    public void deleteStoreCategory(Long commodityStoreCategoryId) {
        //校验该分类下是否有商品
        LambdaQueryWrapper<CommodityStoreSpu> spuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreCategoryId, commodityStoreCategoryId);
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(spuLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(commodityStoreSpus)) {
            throw exception(STORE_CATE_HAS_PRODUCTS);
        }
        CommodityStoreCategory commodityStoreCategory = commodityStoreCategoryService.selectById(commodityStoreCategoryId);
        Long storeId = commodityStoreCategory.getStoreId();
        commodityStoreCategoryService.deleteById(commodityStoreCategoryId);

        storeRedisDao.deleteCategory(storeId, commodityStoreCategoryId);
        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
        LogRecordContext.putVariable("commodityStoreCategoryId", commodityStoreCategoryId);

    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_CATE_UPDATE_SUB_TYPE, bizNo = "{{#commodityStoreCategory.commodityStoreCategoryId}}", success = COMMODITY_STORE_CATE_UPDATE_SUCCESS)
    public void updateStoreCategory(StoreCategoryUpdateReqVO updateReqVO) {
        //校验分类 必选分组是否是唯一
        commodityStoreCategoryService.checkUniqueRequiredCategory(updateReqVO);
        CommodityStoreCategoryUpReqVO commodityStoreCategory = new CommodityStoreCategoryUpReqVO();
        BeanUtils.copyProperties(updateReqVO, commodityStoreCategory);
        commodityStoreCategoryService.updateStoreCategory(commodityStoreCategory);
        LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CommodityStoreSpu::getCategoryName, commodityStoreCategory.getCommodityStoreCategoryName());
        updateWrapper.eq(CommodityStoreSpu::getCommodityStoreCategoryId, commodityStoreCategory.getCommodityStoreCategoryId());
        commodityStoreSpuMapper.update(updateWrapper);

        CategoryDto categoryDto = coverStoreCategoryUpdateReqVOToCategoryDto(updateReqVO);

        storeRedisDao.saveOrUpdateCategory(updateReqVO.getStoreId(), updateReqVO.getCommodityStoreCategoryId(), categoryDto);
        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, updateReqVO.getStoreId()), "", "updateCommodity");

        LogRecordContext.putVariable("commodityStoreCategory", commodityStoreCategory);
    }

    private CategoryDto coverStoreCategoryUpdateReqVOToCategoryDto(StoreCategoryUpdateReqVO updateReqVO) {

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryId(updateReqVO.getCommodityStoreCategoryId());
        categoryDto.setCategoryName(updateReqVO.getCommodityStoreCategoryName());
        categoryDto.setImageUrl(updateReqVO.getCommodityStoreCategoryImage());
        categoryDto.setType(updateReqVO.getType());
        categoryDto.setSort(updateReqVO.getCommodityStoreCategorySort());
        categoryDto.setTimeSharingTopping(updateReqVO.getTimeSharingTopping());
        categoryDto.setIsUp(updateReqVO.getIsUp());
        categoryDto.setStartDate(updateReqVO.getStartDate());
        categoryDto.setEndDate(updateReqVO.getEndDate());
        categoryDto.setIsAllDay(updateReqVO.getIsAllDay());
        categoryDto.setTimeRange(updateReqVO.getTimeRange());
        categoryDto.setDayNumbers(updateReqVO.getDayNumbers());
        categoryDto.setWeekNumbers(updateReqVO.getWeekNumbers());
        categoryDto.setCommodityStoreCategoryStatus(updateReqVO.getCommodityStoreCategoryStatus());
        return categoryDto;

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPUS_DELETE_SUB_TYPE, bizNo = "{{#storeId}}", success = COMMODITY_STORE_SPUS_DELETE_SUCCESS)
    public void deleteStoreSpus(List<Long> ids) {


        LambdaQueryWrapper<CommodityStoreSpu> spuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuLambdaQueryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, ids);
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(spuLambdaQueryWrapper);

        Long storeId = commodityStoreSpus.get(0).getStoreId();



        //查询是否有上架商品
        if (ObjectUtil.isNotEmpty(commodityStoreSpus)) {
            for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
                if (storeSpus.getCommodityStoreSpuAppletStatus().equals(1)) {
                    throw exception(STORE_SPU_INCLUDES_PUBLISHED_PRODUCTS);
                }
                if (storeSpus.getCommodityStoreSpuMachineStatus().equals(1)) {
                    throw exception(STORE_SPU_INCLUDES_PUBLISHED_PRODUCTS);
                }
            }

            commodityStoreSpuMapper.delete(spuLambdaQueryWrapper);

            commodityStoreSkuService.deleteBySpuIds(ids);
            commodityStoreGroupService.deletBySpuIds(ids);
            commodityStoreSingleService.deleteBySpuIds(ids);



            for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
                storeRedisDao.deleteProduct(storeSpus.getStoreId(), storeSpus.getCommodityStoreCategoryId(), storeSpus.getCommodityStoreSpuId());
            }
           // Long storeId = commodityStoreSpus.get(0).getStoreId();
            rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");

            LogRecordContext.putVariable("storeId", storeId);

        }


    }


    /**
     * @param
     * @return
     */
    @Override
    public List<CommodityStoreSpuRespVo> storeSingleDownSelectV3(StoreItemDelistReqVo delistReqVo) {

        Long storeId = delistReqVo.getStoreId();

        RLock fairLock = redissonClient.getLock(keyTol + storeId);

        try {
            fairLock.lock();

            Long commodityStoreSpuId = delistReqVo.getCommodityStoreSpuId();
            LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
            CommodityStoreSpu byId = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
            //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
            if (ObjectUtil.isEmpty(byId)){
                throw new ServiceException(SPU_IS_DEL);
            }

            Integer chooseView = delistReqVo.getChooseView();


            // 1) 找到“当前单品”在门店下关联到的套餐关系
            List<CommodityStoreSingle> singleRelations = commodityStoreSingleService
                    .selectByChooseViewAndSpuId(byId.getCommodityStorePrimitiveSpuId(), chooseView, storeId);
            if (ObjectUtil.isEmpty(singleRelations)) {
                return new ArrayList<>();
            }
            // 2) 仅针对关联套餐，复用 getOrDown 统一判定“是否需要联动下架”
            List<Long> relatedPackageIds = singleRelations.stream()
                    .map(CommodityStoreSingle::getCommodityStoreSpuId)
                    .distinct()
                    .toList();
            List<Long> singlePrimitiveIds = Collections.singletonList(byId.getCommodityStorePrimitiveSpuId());
            List<CommodityStoreSpu> packagesNeedDown =
                    findNeedDownPackagesByIds(relatedPackageIds, singlePrimitiveIds, chooseView, storeId);
            // 3) 转为接口返回结构
            return convertToStoreSpuRespVo(packagesNeedDown);
        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }


    }

    private static int calculateDownCount(List<CommodityStoreSingle> singleList, Integer chooseView) {
        int downCount = 0;
        for (CommodityStoreSingle single : singleList) {
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

    private List<CommodityStoreSpu> findNeedDownPackagesByIds(List<Long> packageIds, List<Long> singlePrimitiveIds,
                                                               Integer chooseView, Long storeId) {
        if (ObjectUtil.isEmpty(packageIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<CommodityStoreSpu> packageQuery = new LambdaQueryWrapper<>();
        packageQuery.eq(CommodityStoreSpu::getStoreId, storeId);
        packageQuery.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
        if (chooseView.equals(SpuEnum.WX.getCode())) {
            packageQuery.eq(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, SpuEnum.UP.getCode());
        } else {
            packageQuery.eq(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, SpuEnum.UP.getCode());
        }
        List<CommodityStoreSpu> packageList = commodityStoreSpuMapper.selectList(packageQuery);
        if (ObjectUtil.isEmpty(packageList)) {
            return new ArrayList<>();
        }

        List<Long> activePackageIds = packageList.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
        List<CommodityStoreGroup> storeGroups = commodityStoreGroupService.selectBySpuIds(activePackageIds);
        List<CommodityStoreSingle> storeSingles = commodityStoreSingleService.selectByStoreSpuIds(activePackageIds);
        Map<Long, List<CommodityStoreGroup>> groupMap = storeGroups.stream()
                .collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
        Map<Long, List<CommodityStoreSingle>> singleMap = storeSingles.stream()
                .collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreSpuId));

        List<CommodityStoreSpu> needDown = new ArrayList<>();
        for (CommodityStoreSpu storeSpu : packageList) {
            List<CommodityStoreGroup> groups = groupMap.get(storeSpu.getCommodityStoreSpuId());
            List<CommodityStoreSingle> singles = singleMap.get(storeSpu.getCommodityStoreSpuId());
            if (ObjectUtil.isEmpty(groups) || ObjectUtil.isEmpty(singles)) {
                continue;
            }
            if (getOrDown(storeSpu, groups, singles, chooseView, singlePrimitiveIds, storeId)) {
                needDown.add(storeSpu);
            }
        }
        return needDown;
    }

    private List<CommodityStoreSpuRespVo> convertToStoreSpuRespVo(List<CommodityStoreSpu> storeSpus) {
        if (ObjectUtil.isEmpty(storeSpus)) {
            return new ArrayList<>();
        }
        List<CommodityStoreSpuRespVo> respVos = new ArrayList<>();
        for (CommodityStoreSpu storeSpu : storeSpus) {
            CommodityStoreSpuRespVo respVo = new CommodityStoreSpuRespVo();
            BeanUtils.copyProperties(storeSpu, respVo);
            if (ObjectUtil.isNotEmpty(storeSpu.getImageUrl())) {
                respVo.setImageUrlVO(ConvertUtil.convertStringToListS(storeSpu.getImageUrl()));
            }
            respVos.add(respVo);
        }
        return respVos;
    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_DOWN_SUB_TYPE, bizNo = "{{#commodityStoreSpu.commodityStoreSpuId}}", success = COMMODITY_STORE_SPU_DOWN_SUCCESS)
    public void singleDownV3(StoreItemDelistReqVo delistReqVo) {


        RLock fairLock = redissonClient.getLock(keyTol + delistReqVo.getStoreId());
        Long storeId1 = delistReqVo.getStoreId();
        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId1), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();

        try {
            fairLock.lock();

            Long commodityStoreSpuId = delistReqVo.getCommodityStoreSpuId();
            LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
            CommodityStoreSpu byId = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
            //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
            if (ObjectUtil.isEmpty(byId)){
                throw new ServiceException(SPU_IS_DEL);
            }


            List<CommodityStoreSpuRespVo> storeSpus = delistReqVo.getStoreSpus();
            LambdaUpdateWrapper<CommodityStoreSpu> storeSpuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            List<Long> spuIds = new ArrayList<>();
            spuIds.add(delistReqVo.getCommodityStoreSpuId());
            if (ObjectUtil.isNotEmpty(storeSpus)) {
                List<CommodityStoreSpu> commodityStoreSpus = BeanCopyUtils.copyBeanList(storeSpus, CommodityStoreSpu.class);
                spuIds.addAll(commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList());
            }
            storeSpuLambdaUpdateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, spuIds);
            if (delistReqVo.getChooseView().equals(1)) {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, 0);
            } else {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, 0);
            }

            storeSpuLambdaUpdateWrapper.eq(CommodityStoreSpu::getStoreId, delistReqVo.getStoreId());
            commodityStoreSpuMapper.update(storeSpuLambdaUpdateWrapper);

            CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectById(delistReqVo.getCommodityStoreSpuId());


            List<Long> packageIds = commodityStoreSingleService.updateDownStatusBySpuIdAndChooseView(delistReqVo.getStoreId(), commodityStoreSpu.getCommodityStorePrimitiveSpuId(), delistReqVo.getChooseView());
            if (ObjectUtil.isNotEmpty(packageIds)) {
                spuIds.addAll(packageIds);
            }
            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);

            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }
            if (ObjectUtil.isNotEmpty(storeSpus)){
                List<String> packageNames = new ArrayList<>();
                for (CommodityStoreSpuRespVo storeSpu : storeSpus) {
                    if (ObjectUtil.isNotEmpty(storeSpu.getCommodityStoreSpuName())) {
                        packageNames.add(storeSpu.getCommodityStoreSpuName());
                    }
                }
                LogRecordContext.putVariable("packageNames", packageNames);
            }


            LogRecordContext.putVariable("commodityStoreSpu", commodityStoreSpu);
            LogRecordContext.putVariable("storeName", storeName);
        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!delistReqVo.getIsApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId1), "", "updateCommodity");
            }

        }


    }


    @Override
    public List<StoreCategoryDTO> getAllSpuFromDB(Long storeId, ClientType clientType) {
        List<StoreCategoryDTO> storeCategoryDTOS = new ArrayList<>();
        List<CommodityStoreSpu> storeSpuList = commodityStoreSpuMapper.selectListByStoreId(storeId);
        if (CollectionUtils.isEmpty(storeSpuList)) {
            return Collections.emptyList();
        }
        List<Long> spuIds = storeSpuList.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
        List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);
        List<Long> categoryIds = spuDtos.stream().map(SpuDto::getCategoryId).distinct().toList();
        for (Long categoryId : categoryIds) {
            CommodityStoreCategory storeCategory = commodityStoreCategoryService.getCategoryById(categoryId);
            CategoryDto categoryDto = CommodityConvertor.convertDoToCategoryDTO(storeCategory);

            StoreCategoryDTO storeCategoryDTO = new StoreCategoryDTO();
            storeCategoryDTO.setCategory(categoryDto);
            List<SpuDto> spuDtoList = spuDtos.stream()
                    .filter(spuDto -> spuDto.getCategoryId().equals(categoryId)).toList();
            storeCategoryDTO.setItems(spuDtoList);
            storeCategoryDTOS.add(storeCategoryDTO);
        }
        List<StoreCategoryDTO> result = rebuild(storeId, clientType, storeCategoryDTOS);
        //设置活动标签（N件N折/满减满折/满赠并行执行）
        parallelSetActivityTags(storeId, result);

        return result;
    }

    private List<SpuDto> sortItems(StoreCategoryDTO storeCategoryDTO) {
        List<SpuDto> items = storeCategoryDTO.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        TimeSharingUtil.processTimeBasedList(items);

        List<SpuDto> list = items.stream().sorted(Comparator
                .comparing(SpuDto::getSort, Comparator.nullsFirst(Comparator.naturalOrder()))//降序
                .thenComparing(SpuDto::getSpuId, Comparator.nullsFirst(Comparator.reverseOrder()))//升序
        ).toList();
        return list;
    }

    @Override
    public void resetStoreCache(Long storeId) {
        List<CommodityStoreSpu> storeSpuList = commodityStoreSpuMapper.selectListByStoreId(storeId);
        if (CollectionUtils.isEmpty(storeSpuList)) {
            return;
        }
        List<Long> spuIds = storeSpuList.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
        List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);
        storeRedisDao.clearStoreCache(storeId);
        for (SpuDto spuDto : spuDtos) {
            storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
        }
    }

    @Override
    public List<StoreCategoryDTO> appletGetAllSpu(Long storeId, ClientType clientType) {
        if (storeId == null){
            return List.of();
        }
        return appletGetAllSpu(storeId, clientType, true);
    }

    @Override
    public List<StoreCategoryDTO> appletGetAllSpu(Long storeId, ClientType clientType, Boolean njnz) {
        List<StoreCategoryDTO> storeCategoryDTOS;
        try {
            storeCategoryDTOS = storeRedisDao.getAllByStoreId(storeId);
        } catch (Exception e) {
            log.error("门店{}商品redis缓存获取失败,降级走DB 处理", storeId, e);
            try {
                return localProductCache.get(new KeyValue<>(storeId, clientType));
            } catch (ExecutionException ex) {
                log.error("本地缓存获取失败，直接查询DB", ex);
                return getAllSpuFromDB(storeId, clientType);
            }
        }

        //分类重排
        List<StoreCategoryDTO> result = rebuild(storeId, clientType, storeCategoryDTOS);
        //设置活动标签（N件N折/满减满折/满赠并行执行）
        if (njnz){
            parallelSetActivityTags(storeId, result);
        }

        //点餐机来源 需设置 是否加购品
        if (!Objects.equals(clientType, ClientType.WX)){
            setAfterOrderTag(result);
        }
        return result;
    }

    /**
     * 并行设置活动标签：N件N折 / 满减满折 / 满赠三个打标任务并行执行，等待全部完成后返回
     * <p>
     * 三个方法各自回填 result 中不同字段，无写冲突；任一失败不影响其他标签与商品返回。
     * ThreadLocal 业务上下文（businessId）需在调用线程捕获后显式传入子线程，
     * 否则 Dubbo 消费端过滤器取不到上下文，promotion 侧 getRequiredBusinessId 会抛异常。
     */
    private void parallelSetActivityTags(Long storeId, List<StoreCategoryDTO> result) {
        // 调用线程捕获业务上下文（沿用 SyncTaskServiceImpl 的传递惯例）
        Long businessId = BusinessContextHolder.getBusinessId();
        List<CompletableFuture<Void>> futures = List.of(
                // N件N折标签
                CompletableFuture.runAsync(() -> runWithBusinessContext(businessId, () -> setNjnz(storeId, result)), activityTagExecutor),
                // 满减满折标签
                CompletableFuture.runAsync(() -> runWithBusinessContext(businessId, () -> setMJ(storeId, result)), activityTagExecutor),
                // 满赠标签
                CompletableFuture.runAsync(() -> runWithBusinessContext(businessId, () -> setMz(storeId, result)), activityTagExecutor)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            // 三个打标方法内部已吞异常，此处为兜底保护：失败仅记日志，不影响商品返回
            log.error("并行设置活动标签异常, storeId={}", storeId, e);
        }
    }

    /**
     * 在子线程中执行任务：设置调用线程捕获的业务上下文，执行后 clear 防止线程复用导致上下文泄露
     */
    private void runWithBusinessContext(Long businessId, Runnable task) {
        try {
            BusinessContextHolder.setBusinessId(businessId);
            task.run();
        } finally {
            BusinessContextHolder.clear();
        }
    }

    private void setMJ(Long storeId, List<StoreCategoryDTO> result) {
        Set<Long> commodityIds = new HashSet<>();
        for (List<SpuDto> spuDtos : result.stream().map(StoreCategoryDTO::getItems).toList()) {
            commodityIds.addAll(spuDtos.stream().map(SpuDto::getCommodityId).toList());
        }
        Map<Long, List<ActivityMJDTO>> mjmzData = null;
        try {
            mjmzData = activityApi.selectMJActivity(storeId, commodityIds).getData();
        } catch (Exception e) {
            log.error("MJ(满减满折) error", e);
        }

        if (CollectionUtils.isNotEmpty(mjmzData)) {
            for (StoreCategoryDTO storeCategoryDTO : result) {
                List<SpuDto> items = storeCategoryDTO.getItems();
                if (org.springframework.util.CollectionUtils.isEmpty(items)) {
                    continue;
                }
                for (SpuDto spuDto : items) {
                    spuDto.setMjActivityList(mjmzData.get(spuDto.getCommodityId()));
                }
            }
        }
    }

    private void setMz(Long storeId, List<StoreCategoryDTO> result) {
        Set<Long> commodityIds = new HashSet<>();
        for (List<SpuDto> spuDtos : result.stream().map(StoreCategoryDTO::getItems).toList()) {
            commodityIds.addAll(spuDtos.stream().map(SpuDto::getCommodityId).toList());
        }
        Map<Long, List<ActivityMzDTO>> mzData = null;
        try {
            mzData = activityApi.selectMZActivity(storeId, commodityIds).getData();
        } catch (Exception e) {
            log.error("MZ(满赠) error", e);
        }

        if (CollectionUtils.isNotEmpty(mzData)) {
            for (StoreCategoryDTO storeCategoryDTO : result) {
                List<SpuDto> items = storeCategoryDTO.getItems();
                if (org.springframework.util.CollectionUtils.isEmpty(items)) {
                    continue;
                }
                for (SpuDto spuDto : items) {
                    // 仅兑换售卖（saleRule=3）的商品完全不参与满赠活动
                    if (spuDto.getSaleRule() != null && spuDto.getSaleRule() == 3) {
                        continue;
                    }
                    // 按活动参与品类过滤后再取最新活动
                    List<ActivityMzDTO> matched = filterMzByCategoryType(mzData.get(spuDto.getCommodityId()), spuDto);
                    spuDto.setMzActivityVO(selectLatestMzActivity(matched));
                }
            }
        }
    }

    /**
     * 按活动参与品类（categoryType）过滤命中的满赠活动
     * <p>
     * 1全部/null：保留全部；2仅单品：仅保留单品对当前商品生效的活动；3仅套餐：仅保留套餐生效的活动
     */
    private List<ActivityMzDTO> filterMzByCategoryType(List<ActivityMzDTO> mzList, SpuDto spuDto) {
        if (org.springframework.util.CollectionUtils.isEmpty(mzList)) {
            return mzList;
        }
        boolean setmeal = isSetmeal(spuDto);
        return mzList.stream().filter(mzDTO -> {
            if (mzDTO == null) {
                return false;
            }
            // categoryType 仅在按品类限制时生效；按商品限制的活动已由 Promotion 完成商品范围筛选。
            if (!Objects.equals(mzDTO.getPlaceOrderType(), MzPlaceOrderTypeEnum.BY_CATEGORY.getCode())) {
                return true;
            }
            Integer categoryType = mzDTO.getCategoryType();
            if (categoryType == null || Objects.equals(categoryType, MzCategoryTypeEnum.ALL.getCode())) {
                return true;
            }
            if (Objects.equals(categoryType, MzCategoryTypeEnum.SINGLE.getCode())) {
                // 仅单品
                return !setmeal;
            }
            if (Objects.equals(categoryType, MzCategoryTypeEnum.COMBO.getCode())) {
                // 仅套餐
                return setmeal;
            }
            return true;
        }).toList();
    }

    /**
     * 是否为套餐：setmealType 为 1（固定搭配套餐）或 2（分组可选套餐）视为套餐，其余（3单品/null）视为单品
     */
    private boolean isSetmeal(SpuDto spuDto) {
        Integer setmealType = spuDto.getSetmealType();
        return setmealType != null && (setmealType == 1 || setmealType == 2);
    }

    /**
     * 从命中的满赠活动列表中挑选 createTime 最新的一个
     */
    private ActivityMzDTO selectLatestMzActivity(List<ActivityMzDTO> mzList) {
        if (org.springframework.util.CollectionUtils.isEmpty(mzList)) {
            return null;
        }
        ActivityMzDTO latest = null;
        for (ActivityMzDTO mzDTO : mzList) {
            if (mzDTO == null) {
                continue;
            }
            if (latest == null || (mzDTO.getCreateTime() != null
                    && (latest.getCreateTime() == null || latest.getCreateTime().isBefore(mzDTO.getCreateTime())))) {
                latest = mzDTO;
            }
        }
        return latest;
    }

    private void setAfterOrderTag(List<StoreCategoryDTO> result) {
        Set<Long> commodityIds = new HashSet<>();
        for (StoreCategoryDTO storeCategoryDTO : result) {
            for (SpuDto item : storeCategoryDTO.getItems()) {
                commodityIds.add(item.getCommodityId());
            }
        }

        List<AfterOrderDO> afterOrderDOS = afterOrderMapper.selectList(AfterOrderDO::getCommodityId, commodityIds);
        if (org.springframework.util.CollectionUtils.isEmpty(afterOrderDOS)){
            return;
        }
        List<Long> afterCommodityIds = afterOrderDOS.stream().map(AfterOrderDO::getCommodityId).toList();

        for (StoreCategoryDTO storeCategoryDTO : result) {
            for (SpuDto item : storeCategoryDTO.getItems()) {
                item.setAfterOrder(afterCommodityIds.contains(item.getCommodityId()));
            }
        }
    }

    private List<StoreCategoryDTO> rebuild(Long storeId, ClientType clientType,
        List<StoreCategoryDTO> storeCategoryDTOS) {
        storeCategoryDTOS = sortCategory(storeCategoryDTOS);

        //商品重排
        for (StoreCategoryDTO storeCategoryDTO : storeCategoryDTOS) {
            List<SpuDto> list = sortItems(storeCategoryDTO);
            storeCategoryDTO.setItems(list);
        }

        //应用策略
        CommodityClientStrategy strategy = CommodityStrategyFactory.getStrategy(clientType);
        List<StoreCategoryDTO> result = strategy.filter(storeCategoryDTOS);

        // 最终统一处理showBtn
        for (StoreCategoryDTO storeCategoryDTO : result) {
            for (SpuDto item : storeCategoryDTO.getItems()) {
                boolean needShowBtn = CollectionUtil.isNotEmpty(item.getSkuList()) && item.getSkuList().size() > 1 //存在多规格
                        || CollectionUtil.isNotEmpty(item.getCommodityCondiments()) // 存在小料
                        || CollectionUtil.isNotEmpty(item.getCommodityFlavors()) //存在属性
                        || Objects.equals(item.getSetmealType(), CommodityPackageType.GROUP_SELECTABLE.getCode()); // 分组可选套餐
                item.setShowBtn(needShowBtn ? 1 : 0);
            }
        }


        return result;
    }

    private void setNjnz(Long storeId, List<StoreCategoryDTO> result) {
        Set<Long> commodityIds = new HashSet<>();
        for (List<SpuDto> spuDtos : result.stream().map(StoreCategoryDTO::getItems).toList()) {
            commodityIds.addAll(spuDtos.stream().map(SpuDto::getCommodityId).toList());
        }
        Map<Long, List<ActivityNjnzDTO>> njnzData = null;
        try {
            njnzData = activityApi.selectNjnzActivity(storeId, commodityIds).getData();
        } catch (Exception e) {
            log.error("njnz error", e);
        }

        if (CollectionUtils.isNotEmpty(njnzData)) {
            for (StoreCategoryDTO storeCategoryDTO : result) {
                List<SpuDto> items = storeCategoryDTO.getItems();
                if (org.springframework.util.CollectionUtils.isEmpty(items)) {
                    continue;
                }
                for (SpuDto spuDto : items) {
                    spuDto.setNjnzActivityList(njnzData.get(spuDto.getCommodityId()));
                }
            }
        }
    }

    private List<StoreCategoryDTO> sortCategory(List<StoreCategoryDTO> storeCategoryDTOS) {
        if (CollectionUtil.isEmpty(storeCategoryDTOS)) {
            return Collections.emptyList();
        }

        // 提取所有 CategoryDto
        List<CategoryDto> categoryDtos = storeCategoryDTOS.stream()
                .map(StoreCategoryDTO::getCategory).toList();
        TimeSharingUtil.processTimeBasedList(categoryDtos);

        Comparator<StoreCategoryDTO> comparator = Comparator
                // 1. 按 isUp 降序（true在前）
                .comparing(this::getIsUpSafely, Comparator.reverseOrder())
                // 2. isUp 相同时按 sort 值升序（数值小的在前）
                .thenComparingInt(this::getSortSafely)
                // 3. sort 值相同情况下按 id 降序（id大的在前）
                .thenComparing(
                        storeCategoryDTO -> storeCategoryDTO.getCategory().getCategoryId(),
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
        return storeCategoryDTOS.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    private Boolean getIsUpSafely(StoreCategoryDTO dto) {
        return Optional.ofNullable(dto)
                .map(StoreCategoryDTO::getCategory)
                .map(CategoryDto::getIsUp)
                .orElse(false); // 空值默认视为 false
    }

    private int getSortSafely(StoreCategoryDTO dto) {
        return Optional.ofNullable(dto)
                .map(StoreCategoryDTO::getCategory)
                .map(CategoryDto::getSort)
                .orElse(Integer.MAX_VALUE); // 空值排到最后
    }





    private List<SpuDto> coverCommodityStoreSpuListToSpuDtoList(List<Long> spuIds) {

        List<SpuDto> spuDtos = new ArrayList<>();
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectByIds(spuIds);

        List<CommodityStoreSku> commodityStoreSkuList = Optional.ofNullable(commodityStoreSkuService.selectBySpuIds(spuIds))
                .orElse(Collections.emptyList());

        List<CommodityStoreGroup> storeGroupList = Optional.ofNullable(commodityStoreGroupService.selectBySpuIds(spuIds))
                .orElse(Collections.emptyList());
        List<CommodityStoreSingle> singleList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(storeGroupList)) {
            List<Long> groupIds = storeGroupList.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();
            singleList = commodityStoreSingleService.selectByGroupIds(groupIds);
        }


        Map<Long, List<CommodityStoreSku>> skumap = commodityStoreSkuList.stream().collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));
        Map<Long, List<CommodityStoreGroup>> groupMap = new HashMap<>();
        Map<Long, List<CommodityStoreSingle>> singleMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(storeGroupList)) {
            groupMap = storeGroupList.stream().collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
            singleMap = singleList.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
        }

        for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
            List<CommodityStoreSku> commodityStoreSkuList1 = skumap.get(storeSpus.getCommodityStoreSpuId());
            List<CommodityStoreSingle> commodityStoreSinglesAll = new ArrayList<>();
            List<CommodityStoreGroup> commodityStoreGroups = new ArrayList<>();
            List<CommodityTag> commodityTags = new ArrayList<>();
            if (storeSpus.getCommodityStoreSpuIsSingle() == 2) {
                commodityStoreGroups = groupMap.get(storeSpus.getCommodityStoreSpuId());
                List<Long> groupIds = commodityStoreGroups.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();

                for (Long groupId : groupIds) {
                    List<CommodityStoreSingle> commodityStoreSingles = singleMap.getOrDefault(groupId, Collections.emptyList());
                    commodityStoreSinglesAll.addAll(commodityStoreSingles);
                }
            }
            if (ObjectUtil.isNotEmpty(storeSpus.getTagIds())) {
                List<Long> longs = ConvertUtil.convertStringToListNew(storeSpus.getTagIds());
                commodityTags = commodityTageService.selectListByIds(longs);
            }

            SpuDto spuDto = CommodityConvertor.convertDosToSpuDTOQ(storeSpus, commodityStoreSkuList1, commodityStoreSinglesAll, commodityStoreGroups, commodityTags);
            spuDtos.add(spuDto);
        }
        return spuDtos;
    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_UP_SUB_TYPE, bizNo = "{{#byId.commodityStoreSpuId}}", success = COMMODITY_STORE_SPU_UP_SUCCESS)
    public void singleUpV3(StoreItemListingReqVo listingReqVo) {

        Long storeId = listingReqVo.getStoreId();
        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();


        RLock fairLock = redissonClient.getLock(keyTol + storeId);

        try {
            fairLock.lock();

            Long commodityStoreSpuId = listingReqVo.getCommodityStoreSpuId();

            Integer chooseView = listingReqVo.getChooseView();
            CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);

            List<Long> spuIds = new ArrayList<>();


            List<Long> groupIds = commodityStoreSingleService.updateUpStatusBySpuId(storeId, byId.getCommodityStorePrimitiveSpuId(), chooseView);
            LambdaUpdateWrapper<CommodityStoreSpu> storeSpuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSpuLambdaUpdateWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId, commodityStoreSpuId);
            if (chooseView.equals(1)) {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, 1);
            } else {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, 1);
            }
            commodityStoreSpuMapper.update(storeSpuLambdaUpdateWrapper);
            spuIds.add(commodityStoreSpuId);
            if (ObjectUtil.isNotEmpty(groupIds)) {
                List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectByGroupIdsAndStoreId(groupIds, storeId);
                List<Long> spuIdsForSingle = commodityStoreGroups.stream().map(CommodityStoreGroup::getCommodityStoreSpuId).toList();
                spuIds.addAll(spuIdsForSingle);
            }


            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);

            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }
            LogRecordContext.putVariable("byId", byId);
            LogRecordContext.putVariable("storeName", storeName);
        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!listingReqVo.getIsApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
            }

        }


    }

    @Override
    public List<CommodityStoreSingleRespVO> packageSelectV3(StoreItemListingReqVo listingReqVo) {

        RLock fairLock = redissonClient.getLock(keyTol + listingReqVo.getStoreId());

        Long commodityStoreSpuId = listingReqVo.getCommodityStoreSpuId();

        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
        CommodityStoreSpu byId = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (ObjectUtil.isEmpty(byId)){
            throw new ServiceException(SPU_IS_DEL);
        }



        try {
            fairLock.lock();


            List<CommodityStoreGroup> storeGroups = commodityStoreGroupService.selectBySpuId(listingReqVo.getStoreId(), listingReqVo.getCommodityStoreSpuId());


            Integer chooseView = listingReqVo.getChooseView();
            if (ObjectUtil.isNotEmpty(storeGroups)) {
                List<Long> groupIds = storeGroups.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();

                List<CommodityStoreSingle> list = commodityStoreSingleService.selectByGroupIdsAndStoreId(groupIds, listingReqVo.getStoreId());


                List<CommodityStoreSingle> commodityStoreSinglesRe = new ArrayList<>();
                if (ObjectUtil.isNotEmpty(list)) {
                    Map<Long, List<CommodityStoreSingle>> singleMap = list.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
                    for (CommodityStoreGroup storeGroup : storeGroups) {
                        //0是固定搭配 1是可选 2是固定
                        List<CommodityStoreSingle> commodityStoreSingles = singleMap.get(storeGroup.getCommodityStoreGroupId());
                        if (ObjectUtil.isNotEmpty(commodityStoreSingles)) {
                            if (storeGroup.getCommodityStoreGroupAttribute() == 0) {
                                //固定搭配 必须要全部上架
                                commodityStoreSinglesRe = checkDownSingleV0(commodityStoreSingles, chooseView);
                                if (ObjectUtil.isNotEmpty(commodityStoreSinglesRe)) {
                                    break;
                                }
                            } else if (storeGroup.getCommodityStoreGroupAttribute() == 1) {
                                //可选商品 1个上架就行
                                commodityStoreSinglesRe = checkDownSingleV1(commodityStoreSingles, chooseView, storeGroup);
                                if (ObjectUtil.isNotEmpty(commodityStoreSinglesRe)) {
                                    break;
                                }
                            } else if (storeGroup.getCommodityStoreGroupAttribute() == 2) {
                                //分组固定 全部上架
                                commodityStoreSinglesRe = checkDownSingleV0(commodityStoreSingles, chooseView);
                                if (ObjectUtil.isNotEmpty(commodityStoreSinglesRe)) {
                                    break;
                                }
                            } else if (storeGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
                                // 加价组不参与套餐是否可上架判定
                                continue;
                            }
                        }


                    }

                }

                List<CommodityStoreSingleRespVO> singleRespVOS = BeanCopyUtils.copyBeanList(commodityStoreSinglesRe, CommodityStoreSingleRespVO.class);

                return singleRespVOS;

            } else {
                return new ArrayList<>();
            }
        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }


    }

    private void checkShelfLockPermissions(List<Long> originalIds) {
        if (ObjectUtil.isEmpty(originalIds)){
            return;
        }
        Boolean checkedData = permissionApi.hasAnyPermissions(getLoginUserId(), "commodity:spu:updateShelfLock").getCheckedData();
        if (!checkedData){
            LambdaQueryWrapper<CommoditySpus> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommoditySpus::getCommodityId, originalIds);
            List<CommoditySpus> commoditySpus = commoditySpusMapper.selectList(queryWrapper);
            if (ObjectUtil.isNotEmpty(commoditySpus)){
                List<CommoditySpus> lockSpus = new ArrayList<>();
                commoditySpus.forEach(spu -> {
                    if (SpuEnum.SHELF_LOCK.getCode()==spu.getShelfLock()) {
                        lockSpus.add(spu);
                    }
                });
                if (ObjectUtil.isNotEmpty(lockSpus)){
                    // 提取商品名称，用逗号分隔
                    String spuNames = lockSpus.stream()
                            .map(spu -> ObjectUtil.defaultIfNull(spu.getCommodityName(), "商品ID:" + spu.getCommodityId())) // 名称为空时显示ID
                            .collect(Collectors.joining("、"));


                    String errorMsg = String.format(BASE_SPU_LOCK_NO_PERMISSION.getMsg(), spuNames);
                    // 抛异常（用新增的专属错误码 + 替换后的消息）
                    throw new ServiceException(BASE_SPU_LOCK_NO_PERMISSION.getCode(), errorMsg);

                }

            }
        }



    }

    private List<CommodityStoreSingle> checkDownSingleV1(List<CommodityStoreSingle> commodityStoreSingles, Integer chooseView, CommodityStoreGroup storeGroup) {


        //可选 分组 里 可选商品数量
        Integer commodityStoreGroupChoose = storeGroup.getCommodityStoreGroupChoose();

        Integer chooseMany = storeGroup.getChooseMany();

        int upSize = 0;
        List<CommodityStoreSingle> commodityGroupSingleList = new ArrayList<>();
        for (CommodityStoreSingle commodityGroupSingle : commodityStoreSingles) {
            if (chooseView == 1) {
                if (commodityGroupSingle.getWxStatus() == 1) {
                    upSize++;
                } else {
                    commodityGroupSingleList.add(commodityGroupSingle);
                }
            } else {
                if (commodityGroupSingle.getStoreStatus() == 1) {
                    upSize++;
                } else {
                    commodityGroupSingleList.add(commodityGroupSingle);
                }
            }
        }




        if (chooseMany == 1) {
            //0814新需求，如果套餐里必选品为上架，套餐不允许上架
            if (ObjectUtil.isNotEmpty(commodityGroupSingleList)) {
                for (CommodityStoreSingle commodityStoreSingle : commodityGroupSingleList) {
                    if (commodityStoreSingle.getRequiredChoose()==1){
                        return commodityGroupSingleList;
                    }
                }
            }


            // 同一商品可以多选：只要有一个品可以上架就返回空列表
            return upSize >= 1 ? new ArrayList<>() : commodityGroupSingleList;
        } else {
            if (ObjectUtil.isNotEmpty(commodityGroupSingleList)) {
                for (CommodityStoreSingle commodityStoreSingle : commodityGroupSingleList) {
                    if (commodityStoreSingle.getRequiredChoose()==1){
                        return commodityGroupSingleList;
                    }
                }
            }

            // 同一商品不可多选：如果上架数量大于等于可选数量则返回空列表
            return upSize >= commodityStoreGroupChoose ? new ArrayList<>() : commodityGroupSingleList;
        }


    }

    private List<CommodityStoreSingle> checkDownSingleV0(List<CommodityStoreSingle> commodityStoreSingles, Integer chooseView) {
        //固定搭配 必须要全部上架
        List<CommodityStoreSingle> downSingles = new ArrayList<>();
        for (CommodityStoreSingle commodityStoreSingle : commodityStoreSingles) {
            if (chooseView == 1) {
                if (commodityStoreSingle.getWxStatus() == 0) {
                    downSingles.add(commodityStoreSingle);
                }
            } else {
                if (commodityStoreSingle.getStoreStatus() == 0) {
                    downSingles.add(commodityStoreSingle);
                }
            }
        }
        return downSingles;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SETMEAL_UP_AND_DOWN_SUB_TYPE, bizNo = "{{#commodityStoreSpu.commodityStoreSpuId}}", success = COMMODITY_STORE_SETMEAL_UP_AND_DOWN_SUCCESS)
    public void packageUpAndDownV3(StoreItemUnmountReqVo unmountReqVo) {

        Long commodityStoreSpuId = unmountReqVo.getCommodityStoreSpuId();
        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
        CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (ObjectUtil.isEmpty(commodityStoreSpu)){
            throw new ServiceException(SPU_IS_DEL);
        }
        //校验套餐上架锁权限
        List<Long> originalIds = new ArrayList<>();
        originalIds.add(commodityStoreSpu.getCommodityStorePrimitiveSpuId());
        checkShelfLockPermissions(originalIds);


       // CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectById(unmountReqVo.getCommodityStoreSpuId());

        RLock fairLock = redissonClient.getLock(keyTol + commodityStoreSpu.getStoreId());
        Long storeId = commodityStoreSpu.getStoreId();

        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();

        try {
            fairLock.lock();

            LambdaUpdateWrapper<CommodityStoreSpu> storeSpuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSpuLambdaUpdateWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId, unmountReqVo.getCommodityStoreSpuId());
            if (ObjectUtil.isNotEmpty(unmountReqVo.getCommodityStoreSpuAppletStatus())) {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, unmountReqVo.getCommodityStoreSpuAppletStatus());
            }
            if (ObjectUtil.isNotEmpty(unmountReqVo.getCommodityStoreSpuMachineStatus())) {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, unmountReqVo.getCommodityStoreSpuMachineStatus());
            }
            commodityStoreSpuMapper.update(storeSpuLambdaUpdateWrapper);

            List<Long> spuIds = new ArrayList<>();
            spuIds.add(unmountReqVo.getCommodityStoreSpuId());

            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);
            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }
            LogRecordContext.putVariable("commodityStoreSpu", commodityStoreSpu);

            LogRecordContext.putVariable("storeName", storeName);
        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!unmountReqVo.getIsApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
            }

        }


    }


    public void updateSetmealSingleStatusV3(CommodityStoreSingleStatusReqVO commodityStoreSingleStatusReqVO) {


        CommodityStoreSingle single = commodityStoreSingleService.getOne(commodityStoreSingleStatusReqVO.getCommodityStoreSingleId());


        Long commodityStoreGroupId = single.getCommodityStoreGroupId();


        CommodityStoreGroup group = commodityStoreGroupService.getOne(commodityStoreGroupId);

        Long commodityStoreSpuId = group.getCommodityStoreSpuId();
        LambdaQueryWrapper<CommodityStoreSpu> spuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId, commodityStoreSpuId);
        CommodityStoreSpu one = commodityStoreSpuMapper.selectOne(spuLambdaQueryWrapper);
        Long storeId = one.getStoreId();


        if (ObjectUtil.isNotEmpty(commodityStoreSingleStatusReqVO.getWxStatus())) {
            commodityStoreSingleService.updateWxStatusBySingleId(commodityStoreSingleStatusReqVO.getCommodityStoreSingleId(), commodityStoreSingleStatusReqVO.getWxStatus());
        } else {
            commodityStoreSingleService.updateStoreStatusBySingleId(commodityStoreSingleStatusReqVO.getCommodityStoreSingleId(), commodityStoreSingleStatusReqVO.getStoreStatus());
        }


        Integer isUp = 3;

        // 根据商品选择视图和状态更新 isUp 的值
        if (commodityStoreSingleStatusReqVO.getChooseView() == 1) {
            isUp = commodityStoreSingleStatusReqVO.getWxStatus() == 0 ? 0 : 1;
        } else {
            isUp = commodityStoreSingleStatusReqVO.getStoreStatus() == 0 ? 0 : 1;
        }

        // 如果是下架行为
        if (isUp == 0) {
            if (one.getSetmealType() == 1 || group.getCommodityStoreGroupAttribute() == 2) {
                // 根据选择视图更新商品状态
                if (commodityStoreSingleStatusReqVO.getChooseView() == 1) {
                    one.setCommodityStoreSpuAppletStatus(0);
                } else {
                    one.setCommodityStoreSpuMachineStatus(0);
                }
            } else {
                // 查询商品组下的商品列表


                List<CommodityStoreSingle> list = commodityStoreSingleService.selectByGroupId(group.getCommodityStoreGroupId());

                // 统计下架商品数量
                int down = countDownItems(list, commodityStoreSingleStatusReqVO);
                // 这里可以根据 down 的值做进一步处理，如果有需要的话
                if (down == list.size()) {
                    if (commodityStoreSingleStatusReqVO.getChooseView() == 1) {
                        one.setCommodityStoreSpuAppletStatus(0);
                    } else {
                        one.setCommodityStoreSpuMachineStatus(0);
                    }
                }
            }
            commodityStoreSpuMapper.updateById(one);
        }

        //TODO 删缓存
    }

    private int countDownItems(List<CommodityStoreSingle> list, CommodityStoreSingleStatusReqVO commodityStoreSingleStatusReqVO) {
        int down = 0;
        if (commodityStoreSingleStatusReqVO.getChooseView() == 1) {
            for (CommodityStoreSingle commodityStoreSingle : list) {
                if (commodityStoreSingle.getWxStatus() == 0) {
                    down++;
                }
            }
        } else {
            for (CommodityStoreSingle commodityStoreSingle : list) {
                if (commodityStoreSingle.getStoreStatus() == 0) {
                    down++;
                }
            }
        }
        return down;

    }


    @Override
    public CommodityStoreRespVo getSpusInfoV3(CommodityStoreSpuDetailReqVO detailReqVO) {

        CommodityStoreRespVo commodityStoreRespVo = new CommodityStoreRespVo();

        CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectById(detailReqVO.getCommodityStoreSpuId());
        BeanUtils.copyProperties(commodityStoreSpu, commodityStoreRespVo);

        ObjectMapper objectMapper = new ObjectMapper();


        if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())) {
            commodityStoreRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
        }


        List<CommodityStoreSku> skuList = commodityStoreSkuService.selectBySpuId(detailReqVO.getCommodityStoreSpuId());

        if (ObjectUtil.isNotEmpty(skuList)) {
            List<CommodityStoreSkuRespVo> skuRespVoList = coverSku(skuList);
            commodityStoreRespVo.setCommoditySkusList(skuRespVoList);
        }


        if (ObjectUtil.isNotEmpty(commodityStoreSpu.getTagIds())) {

            List<Long> longs = ConvertUtil.convertStringToListNew(commodityStoreSpu.getTagIds());
            List<CommodityTag> commodityTags = commodityTageService.selectListByIds(longs);
            Map<Long, CommodityTag> tagMap = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, e -> e, (e1, e2) -> e1));
            List<CommodityTag> tagList = new ArrayList<>();
            for (Long aLong : longs) {
                tagList.add(tagMap.get(aLong));
            }
            commodityStoreRespVo.setTagList(tagList);
        }


        if (ObjectUtil.isNotEmpty(commodityStoreSpu.getFlavor())) {
            try {
                // 进行反序列化操作
                List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(commodityStoreSpu.getFlavor(), new TypeReference<>() {
                });

                // 将反序列化后的列表设置回 commoditySpus 对象
                commodityStoreRespVo.setFlavorList(commodityFlavorList);
            } catch (IOException e) {
                // 处理反序列化异常
                System.out.println(e.getMessage());
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
            }
        }

        if (ObjectUtil.isNotEmpty(commodityStoreSpu.getCondiments())) {
            try {
                List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(commodityStoreSpu.getCondiments(), new TypeReference<>() {
                });
                commodityStoreRespVo.setCondimentsList(commodityCondimentsList);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
            }
        }
        if (commodityStoreSpu.getCommodityStoreSpuIsSingle() == 2) {


            List<CommodityStoreGroup> groupList = commodityStoreGroupService.selectBySpuId(commodityStoreSpu.getStoreId(), commodityStoreSpu.getCommodityStoreSpuId());


            List<Long> groupIds = groupList.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();

            List<CommodityStoreSingle> singleList = commodityStoreSingleService.selectByGroupIds(groupIds);


            Map<Long, List<CommodityStoreSingle>> collect = new HashMap<>();
            if (ObjectUtil.isNotEmpty(singleList)) {
                collect = singleList.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
            }


            if (ObjectUtil.isNotEmpty(groupList)) {

                List<CommodityStoreGroupRespVO> commodityStoreGroupRespVOList = coverGroup(groupList);

                for (CommodityStoreGroupRespVO commodityStoreGroupRespVO : commodityStoreGroupRespVOList) {
                    List<CommodityStoreSingle> singleList1 = collect.get(commodityStoreGroupRespVO.getCommodityStoreGroupId());

                    List<CommodityStoreSingleRespVO> commodityStoreSingleRespVOS = coverSingle(singleList1);
                    commodityStoreGroupRespVO.setCommodityStoreSingleRespVOList(commodityStoreSingleRespVOS);


                }
                commodityStoreRespVo.setGroupList(commodityStoreGroupRespVOList);


            }
        }

        return commodityStoreRespVo;
    }

    private List<CommodityStoreGroupRespVO> coverGroup(List<CommodityStoreGroup> groupList) {

        List<CommodityStoreGroupRespVO> commodityStoreGroupRespVOList = new ArrayList<>();
        for (CommodityStoreGroup commodityStoreGroup : groupList) {
            CommodityStoreGroupRespVO commodityStoreGroupRespVO = new CommodityStoreGroupRespVO();
            BeanUtils.copyProperties(commodityStoreGroup, commodityStoreGroupRespVO);
            commodityStoreGroupRespVOList.add(commodityStoreGroupRespVO);
        }

        return commodityStoreGroupRespVOList;
    }

    private List<CommodityStoreSingleRespVO> coverSingle(List<CommodityStoreSingle> singleList1) {
        List<CommodityStoreSingleRespVO> commodityStoreSingleRespVOList = new ArrayList<>();
        for (CommodityStoreSingle commodityStoreSingle : singleList1) {
            CommodityStoreSingleRespVO commodityStoreSingleRespVO = new CommodityStoreSingleRespVO();
            BeanUtils.copyProperties(commodityStoreSingle, commodityStoreSingleRespVO);
            commodityStoreSingleRespVOList.add(commodityStoreSingleRespVO);
        }
        return commodityStoreSingleRespVOList;

    }

    private List<CommodityStoreSkuRespVo> coverSku(List<CommodityStoreSku> skuList) {
        List<CommodityStoreSkuRespVo> skuRespVoList = new ArrayList<>();
        for (CommodityStoreSku commodityStoreSku : skuList) {
            CommodityStoreSkuRespVo commodityStoreSkuRespVo = new CommodityStoreSkuRespVo();
            BeanUtils.copyProperties(commodityStoreSku, commodityStoreSkuRespVo);
            skuRespVoList.add(commodityStoreSkuRespVo);
        }
        return skuRespVoList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_UPDATE_SUB_TYPE, bizNo = "{{#commodityStoreSpu.commodityStoreSpuId}}", success = COMMODITY_STORE_SPU_UPDATE_SUCCESS)
    public void updateCommodityPremiseV3(CommodityStoreUpdateReqVo updateReqVo) {

        Long storeId = updateReqVo.getStoreId();
        CommodityStoreSpu oldSpu = commodityStoreSpuMapper.selectById(updateReqVo.getCommodityStoreSpuId());

        CommodityStoreSpu commodityStoreSpu = new CommodityStoreSpu();
        BeanUtils.copyProperties(updateReqVo, commodityStoreSpu);


        if (ObjectUtil.isNotEmpty(updateReqVo.getImageUrlVO())) {
            commodityStoreSpu.setImageUrl(ConvertUtil.convertListToStringS(updateReqVo.getImageUrlVO()));
        } else {
            commodityStoreSpu.setImageUrl(null);
        }


        ObjectMapper objectMapper = new ObjectMapper();


        if (ObjectUtil.isNotEmpty(updateReqVo.getTagIds())) {
            commodityStoreSpu.setTagIds(ConvertUtil.convertListToString(updateReqVo.getTagIds()));
        } else {
            commodityStoreSpu.setTagIds(null);
        }


        if (ObjectUtil.isNotEmpty(updateReqVo.getFlavorList())) {

            try {
                List<CommodityFlavor> commodityFlavorList = updateReqVo.getFlavorList();
                commodityFlavorList.forEach(commodityFlavor -> {
                    commodityFlavor.setFlavorId(IdWorker.getId());
                });

                // 将 commodityFlavorList 序列化为 JSON 字符串
                String flavorJson = objectMapper.writeValueAsString(updateReqVo.getFlavorList());
                // 设置到 flavor 属性
                commodityStoreSpu.setFlavor(flavorJson);

                commodityStoreSingleService.emitSyncToSubProducts(flavorJson, commodityStoreSpu.getCommodityStorePrimitiveSpuId(),commodityStoreSpu.getStoreId());

            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION);
            }
        } else {
            commodityStoreSpu.setFlavor(null);

            commodityStoreSingleService.emitSyncToSubProducts(null, commodityStoreSpu.getCommodityStorePrimitiveSpuId(),commodityStoreSpu.getStoreId());
        }
        //小料反序列化
        if (ObjectUtil.isNotEmpty(updateReqVo.getCondimentsList())) {
            try {
                List<CommodityCondiments> commodityCondimentsList = updateReqVo.getCondimentsList();
                commodityCondimentsList.forEach(commodityCondiments -> {
                    commodityCondiments.setCondimentId(IdWorker.getId());
                });

                // 将 commodityFlavorList 序列化为 JSON 字符串
                String condiments = objectMapper.writeValueAsString(updateReqVo.getCondimentsList());
                // 设置到 flavor 属性
                commodityStoreSpu.setCondiments(condiments);
            } catch (IOException e) {
                // 处理 JSON 序列化异常
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION);
            }

        } else {
            commodityStoreSpu.setCondiments(null);
        }
        //skuList
        List<CommodityStoreSkuRespVo> commodityStoreSkuRespVoList = updateReqVo.getCommoditySkusList();
        //处理 SKuLISt
        if (ObjectUtil.isEmpty(commodityStoreSkuRespVoList)) {
            throw exception(BASE_SKU_PRODUCT_MUST_HAVE_SPECIFICATION);
        }
        if (commodityStoreSkuRespVoList.size() > 1) {
            commodityStoreSkuRespVoList.forEach(c -> {
                if (StringUtils.isBlank(c.getCommodityStoreSkuName()) || StringUtils.isBlank(c.getCommodityStoreSkuValue())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_NAMES_REQUIRED);
                }
            });
        }
        boolean hasStatusOne = commodityStoreSkuRespVoList.stream()
                .anyMatch(commoditySkus -> commoditySkus.getCommodityStoreSkuStatus() == 1);

        if (!hasStatusOne) {
            throw exception(STORE_SKU_PRODUCT_CANNOT_HAVE_NO_PUBLISHED_SPECIFICATIONS);
        }

        /*LambdaQueryWrapper<CommodityStoreSku> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CommodityStoreSku::getCommodityStoreSpuId, updateReqVo.getCommodityStoreSpuId());
        lambdaQueryWrapper.eq(CommodityStoreSku::getStoreId, updateReqVo.getStoreId());
        commodityStoreSkuMapper.delete(lambdaQueryWrapper);*/
        List<CommodityStoreSku> commodityStoreSkuList = coverSkuEntity(commodityStoreSkuRespVoList);
       // commodityStoreSkuList.forEach(c -> c.setCommodityStoreSkuId(null));
        commodityStoreSkuList.forEach(commodityStoreSku -> {
            commodityStoreSku.setCommodityStoreSpuId(updateReqVo.getCommodityStoreSpuId());
        });

        commodityStoreSkuService.saveBatch(commodityStoreSkuList);


        // 处理套餐
        if (commodityStoreSpu.getCommodityStoreSpuIsSingle() == 2) {
            if (updateReqVo.getGroupList().isEmpty()) {
                throw exception(STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_TWO_ITEMS);
            }

            Integer wxStatus = commodityStoreSpu.getCommodityStoreSpuAppletStatus();
            Integer storeStatus = commodityStoreSpu.getCommodityStoreSpuMachineStatus();


            if (commodityStoreSpu.getSetmealType() == 1) {
                if (ObjectUtil.isNotEmpty(updateReqVo.getGroupList())) {
                    updateReqVo.getGroupList().forEach(item -> {
                        if (ObjectUtil.isNotEmpty(item.getCommodityStoreSingleRespVOList()) && item.getCommodityStoreSingleRespVOList().size() < 2) {
                            if (item.getCommodityStoreSingleRespVOList().size() == 1 && item.getCommodityStoreSingleRespVOList().get(0).getCommodityStoreSingleCopies() < 2) {
                                throw exception(STORE_GROUP_FIXED_COLLOCATION_PACKAGE_REQUIRES_AT_LEAST_TWO_ITEMS);
                            }
                        }
                    });
                }

            } else {
                updateReqVo.getGroupList().forEach(item -> {
                    if (ObjectUtil.isNotEmpty(item.getCommodityStoreSingleRespVOList()) && item.getCommodityStoreSingleRespVOList().size() < 1) {
                        throw exception(STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_ONE_ITEM);
                    }
                });
            }
            // List<Long> groups = commodityStoreGroupMapper.selectList(new LambdaQueryWrapper<CommodityStoreGroup>().eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId())).stream().map(CommodityStoreGroup::getCommodityStoreGroupId).collect(Collectors.toList());

            List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectBySpuId(commodityStoreSpu.getStoreId(), commodityStoreSpu.getCommodityStoreSpuId());
            List<Long> groups = commodityStoreGroups.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();

            // commodityStoreGroupMapper.delete(new LambdaQueryWrapper<CommodityStoreGroup>().eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId()));

            // commodityStoreGroupService.deleteByLambda(new LambdaQueryWrapper<CommodityStoreGroup>().eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId()));

            commodityStoreGroupService.delbySpuId(commodityStoreSpu.getCommodityStoreSpuId());
            if (!groups.isEmpty()) {
                //  commodityStoreSingleMapper.delete(new LambdaQueryWrapper<CommodityStoreSingle>().in(CommodityStoreSingle::getCommodityStoreGroupId, groups));
                //  commodityStoreSingleService.deleteByLambda(new LambdaQueryWrapper<CommodityStoreSingle>().in(CommodityStoreSingle::getCommodityStoreGroupId, groups));

                commodityStoreSingleService.deletByGroupIds(groups);
            }
            List<CommodityStoreGroupSaveVO> groupList = updateReqVo.getGroupList();
            if (ObjectUtil.isNotEmpty(groupList)) {

                if (wxStatus == 1) {
                    wxStatus = checkGroupListStatus(
                            updateReqVo.getGroupList(),
                            updateReqVo.getSetmealType(),
                            this::checkWxStatus
                    );
                }

                if (storeStatus == 1) {
                    storeStatus = checkGroupListStatus(
                            updateReqVo.getGroupList(),
                            updateReqVo.getSetmealType(),
                            this::checkStoreStatus
                    );
                }



                for (CommodityStoreGroupSaveVO commoditySetmealGroup : groupList) {





                    if (updateReqVo.getSetmealType().equals(2) && commoditySetmealGroup.getCommodityStoreGroupAttribute() == 1) {
                        checkSetmealSingleNum(commoditySetmealGroup);
                    }


                    CommodityStoreGroup commodityStoreGroup = new CommodityStoreGroup();
                    BeanUtils.copyProperties(commoditySetmealGroup, commodityStoreGroup);
                    commodityStoreGroup.setCommodityStoreSpuId(commodityStoreSpu.getCommodityStoreSpuId());
                    commodityStoreGroup.setStoreId(commodityStoreSpu.getStoreId());

                    commodityStoreGroup.setCommodityStoreGroupId(null);
                    commodityStoreGroupService.save(commodityStoreGroup);
                    if (ObjectUtil.isNotEmpty(commoditySetmealGroup.getCommodityStoreSingleRespVOList())) {
                        /*Set<Long> skuIdSet = new HashSet<>();
                        commoditySetmealGroup.getCommodityStoreSingleRespVOList().forEach(s -> {
                            Long skuId = s.getSingleSkuId();
                            if (!skuIdSet.add(skuId)) {
                                // 如果 add 方法返回 false，说明 skuId 已经存在于集合中，即重复
                                throw exception(BASE_SINGLE_SAME_GROUP_CANNOT_MULTISELECT_SAME_PRODUCT_SPEC);
                            }
                        });*/


                        List<CommodityStoreSingle> singleList = new ArrayList<>();
                        for (CommodityStoreSingleSaveVO commodityStoreSingleSaveVO : commoditySetmealGroup.getCommodityStoreSingleRespVOList()) {
                            if (commodityStoreSingleSaveVO.getCommodityId()==null){
                                throw exception(STORE_SINGLE_NO_ID);
                            }
                            CommodityStoreSingle commodityStoreSingle = new CommodityStoreSingle();
                            BeanUtils.copyProperties(commodityStoreSingleSaveVO, commodityStoreSingle);
                            commodityStoreSingle.setStoreId(commodityStoreSpu.getStoreId());
                            commodityStoreSingle.setCommodityStoreGroupId(commodityStoreGroup.getCommodityStoreGroupId());
                            commodityStoreSingle.setCommodityStoreSpuId(commodityStoreSpu.getCommodityStoreSpuId());
                            singleList.add(commodityStoreSingle);
                        }
                        createSynchronizationFlavor(singleList);
                        commodityStoreSingleService.saveBatch(singleList);
                    }
                }
            }
            commodityStoreSpu.setCommodityStoreSpuAppletStatus(wxStatus);
            commodityStoreSpu.setCommodityStoreSpuMachineStatus(storeStatus);
        }


        commodityStoreSpuMapper.update(commodityStoreSpu, new LambdaUpdateWrapper<CommodityStoreSpu>().eq(CommodityStoreSpu::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId()));

        List<Long> setmelIds = new ArrayList<>();
        //修改子品名称
        if (updateReqVo.getCommodityStoreSpuIsSingle().equals(1)) {

            setmelIds = commodityStoreSingleService.updateNameBySingleSpu(updateReqVo);
        }
        if (!Objects.equals(oldSpu.getCommodityStoreCategoryId(), commodityStoreSpu.getCommodityStoreCategoryId())) {

            storeRedisDao.deleteProduct(oldSpu.getStoreId(),oldSpu.getCommodityStoreCategoryId(),oldSpu.getCommodityStoreSpuId());
        }




        List<Long> spuIds = new ArrayList<>();
        spuIds.add(commodityStoreSpu.getCommodityStoreSpuId());
        if (ObjectUtil.isNotEmpty(setmelIds)) {
            spuIds.addAll(setmelIds);
        }
        List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);

        for (SpuDto spuDto : spuDtos) {
            storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
        }

        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");

        LogRecordContext.putVariable("commodityStoreSpu", commodityStoreSpu);

    }

    private void createSynchronizationFlavor(List<CommodityStoreSingle> singleList) {
        // 1. 收集所有需要查询的商品ID，过滤空值并去重
        List<Long> commodityIds = singleList.stream()
                .map(CommodityStoreSingle::getCommodityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> commodityFlavorMap = new HashMap<>();
        if (!commodityIds.isEmpty()) {
            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
            // 修正1：查询条件和select字段匹配，同时只查需要的两个字段（ID+口味）
            queryWrapper.in(CommodityStoreSpu::getCommodityStorePrimitiveSpuId, commodityIds)
                    .eq(CommodityStoreSpu::getStoreId, singleList.get(0).getStoreId())
                    .select(CommodityStoreSpu::getCommodityStorePrimitiveSpuId, CommodityStoreSpu::getFlavor);

            List<CommodityStoreSpu> commoditySpusList = commodityStoreSpuMapper.selectList(queryWrapper);

            // 修正2：先过滤null元素，再过滤空口味，最后构建Map（key用查询条件的ID字段）
            commodityFlavorMap = commoditySpusList.stream()
                    .filter(Objects::nonNull) // 过滤null对象，避免空指针
                    .filter(spu -> ObjectUtil.isNotEmpty(spu.getFlavor())) // 过滤空口味
                    .collect(Collectors.toMap(
                            // 关键修正：Map的key要和查询条件的字段一致
                            CommodityStoreSpu::getCommodityStorePrimitiveSpuId,
                            CommodityStoreSpu::getFlavor,
                            (v1, v2) -> v1 // 重复ID时保留第一个值
                    ));
        }

        // 3. 遍历赋值
        for (CommodityStoreSingle commodityStoreSingle : singleList) {
            Long commodityId = commodityStoreSingle.getCommodityId();
            if (commodityId != null && commodityFlavorMap.containsKey(commodityId)) {
                commodityStoreSingle.setFlavor(commodityFlavorMap.get(commodityId));
            }
        }


    }

    // 检查商品组列表的状态，支持短路操作
    private Integer checkGroupListStatus(
            List<CommodityStoreGroupSaveVO>  groupList,
            Integer setmealType,
            BiFunction<CommodityStoreGroupSaveVO, Integer, Integer> statusChecker
    ) {
        int initialStatus = 1;
        for (CommodityStoreGroupSaveVO group : groupList) {
            initialStatus = statusChecker.apply(group, setmealType);
            if (initialStatus == 0) {
                break; // 短路操作：一旦发现状态为0，立即终止循环
            }
        }
        return initialStatus;
    }


    // 检查微信状态（复用之前的优化版本）
    private Integer checkWxStatus(CommodityStoreGroupSaveVO group, Integer setmealType) {
        // 加价组不参与套餐上/下架状态计算，直接视为满足
        if (group.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
            return 1;
        }

        List<CommodityStoreSingleSaveVO> items = group.getCommodityStoreSingleRespVOList();

        // 如果某个子品的requiredChoose是1并且storeStatus是0，那么也要给这个套餐做下架处理
        boolean hasRequiredChooseDown = items.stream()
                .anyMatch(item-> item.getRequiredChoose() == 1
                        && item.getWxStatus() != null
                        && item.getWxStatus() == 0);
        if (hasRequiredChooseDown) {
            return 0;
        }


        if (setmealType == 1 || group.getCommodityStoreGroupAttribute() != 1) {
            return checkAllItemsOnline(items, CommodityStoreSingleSaveVO::getWxStatus) ? 1 : 0;
        }

        long onlineCount = items.stream()
                .filter(item -> item.getWxStatus() == 1)
                .count();

        return group.getChooseMany() == 0
                ? (onlineCount >= group.getCommodityStoreGroupChoose() ? 1 : 0)
                : (onlineCount >= 1 ? 1 : 0);
    }

    // 检查店铺状态（复用之前的优化版本）
    private Integer checkStoreStatus(CommodityStoreGroupSaveVO group, Integer setmealType) {
        // 加价组不参与套餐上/下架状态计算，直接视为满足
        if (group.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
            return 1;
        }

        List<CommodityStoreSingleSaveVO> items = group.getCommodityStoreSingleRespVOList();

        // 如果某个子品的requiredChoose是1并且storeStatus是0，那么也要给这个套餐做下架处理
        boolean hasRequiredChooseDown = items.stream()
                .anyMatch(item-> item.getRequiredChoose() == 1
                        && item.getWxStatus() != null
                        && item.getWxStatus() == 0);
        if (hasRequiredChooseDown) {
            return 0;
        }

        if (setmealType == 1 || group.getCommodityStoreGroupAttribute() != 1) {
            return checkAllItemsOnline(items, CommodityStoreSingleSaveVO::getStoreStatus) ? 1 : 0;
        }

        long onlineCount = items.stream()
                .filter(item -> item.getStoreStatus() == 1)
                .count();

        return group.getChooseMany() == 0
                ? (onlineCount >= group.getCommodityStoreGroupChoose() ? 1 : 0)
                : (onlineCount >= 1 ? 1 : 0);
    }

    // 通用方法：检查所有商品的指定状态是否都为1
    private boolean checkAllItemsOnline(
            List<CommodityStoreSingleSaveVO> items,
            Function<CommodityStoreSingleSaveVO, Integer> statusExtractor
    ) {
        return items.stream().allMatch(item -> statusExtractor.apply(item) == 1);
    }


    private void checkSetmealSingleNum(CommodityStoreGroupSaveVO commoditySetmealGroup) {
        Integer choose = commoditySetmealGroup.getCommodityStoreGroupChoose();

        List<CommodityStoreSingleSaveVO> commodityStoreSingleRespVOList = commoditySetmealGroup.getCommodityStoreSingleRespVOList();


        Integer defaultChooseNum = 0;

        Integer requiredChooseNum = 0;

        for (CommodityStoreSingleSaveVO commodityStoreSingleSaveVO : commodityStoreSingleRespVOList) {
            if (commodityStoreSingleSaveVO.getDefaultChoose() == 1) {
                defaultChooseNum++;
            }
            if (commodityStoreSingleSaveVO.getRequiredChoose() == 1) {
                requiredChooseNum++;
            }
        }


        if (choose < defaultChooseNum | choose < requiredChooseNum) {
            throw exception(BASE_SPU_PRODUCT_NUM_SMAIL);
        }


    }

    private List<CommodityStoreSku> coverSkuEntity(List<CommodityStoreSkuRespVo> commodityStoreSkuRespVoList) {
        List<CommodityStoreSku> commodityStoreSkuList = new ArrayList<>();
        for (CommodityStoreSkuRespVo commodityStoreSkuRespVo : commodityStoreSkuRespVoList) {
            CommodityStoreSku commodityStoreSku = new CommodityStoreSku();
            BeanUtils.copyProperties(commodityStoreSkuRespVo, commodityStoreSku);
            commodityStoreSkuList.add(commodityStoreSku);
        }


        return commodityStoreSkuList;
    }

    @Override
    public List<CommodityStoreRespVo> getAllSpuV3(CommodityStoreAllReqVO allReqVO) {

        List<CommodityStoreRespVo> commodityStoreRespVos = new ArrayList<>();

        LambdaQueryWrapper<CommodityStoreSpu> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreCategoryId, allReqVO.getCommodityStoreCategoryId());


        spusLambdaQueryWrapper.orderByAsc(CommodityStoreSpu::getCommodityStoreSpuSort);
        List<CommodityStoreSpu> list = commodityStoreSpuMapper.selectList(spusLambdaQueryWrapper);


        if (ObjectUtil.isNotEmpty(list)) {

            List<Long> commodityIds = list.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).collect(Collectors.toList());


            List<CommodityStoreSku> commoditySkuses = commodityStoreSkuService.selectBySpuIds(commodityIds);

            if (ObjectUtil.isNotEmpty(commoditySkuses)) {
                Map<Long, List<CommodityStoreSku>> collect = commoditySkuses.stream().collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));

                for (CommodityStoreSpu commodityStoreSpu : list) {
                    CommodityStoreRespVo commodityStoreRespVo = new CommodityStoreRespVo();
                    BeanUtils.copyProperties(commodityStoreSpu, commodityStoreRespVo);
                    List<CommodityStoreSku> commoditySkuses1 = collect.get(commodityStoreSpu.getCommodityStoreSpuId());
                    if (commoditySkuses1 != null && commoditySkuses1.size() > 1) {
                        commodityStoreRespVo.setIsMoreSku(true);
                        PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPricesStore(commoditySkuses1);
                        commodityStoreRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                        commodityStoreRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                        commodityStoreRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                        commodityStoreRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                    } else {
                        commodityStoreRespVo.setIsMoreSku(false);
                        if (commoditySkuses1 != null) {
                            commodityStoreRespVo.setLowPrice(commoditySkuses1.get(0).getCommodityStoreSkuPrice());
                            commodityStoreRespVo.setLowStrikePrice(commoditySkuses1.get(0).getCommodityStoreSkuStrikePrice());
                        }

                    }

                    if (ObjectUtil.isNotEmpty(commoditySkuses1)) {
                        List<CommodityStoreSkuRespVo> commodityStoreSkuRespVoList = coverSku(commoditySkuses1);
                        commodityStoreRespVo.setCommoditySkusList(commodityStoreSkuRespVoList);
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())) {
                        commodityStoreRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getTagIds())) {
                        List<CommodityTag> commodityTags = commodityTageService.selectListByIds(ConvertUtil.convertStringToList(commodityStoreSpu.getTagIds()));
                        Map<Long, CommodityTag> collect1 = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, c -> c));
                        List<CommodityTag> commodityTagsnew = new ArrayList<>();
                        for (Long l : ConvertUtil.convertStringToList(commodityStoreSpu.getTagIds())) {
                            commodityTagsnew.add(collect1.get(l));
                        }

                        commodityStoreRespVo.setTagList(commodityTagsnew);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getFlavor())) {
                        try {
                            // 进行反序列化操作
                            List<CommodityFlavor> commodityTagReqVOList = objectMapper.readValue(commodityStoreSpu.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                            });
                            // 将反序列化后的列表设置回 commoditySpus 对象
                            commodityStoreRespVo.setFlavorList(commodityTagReqVOList);
                        } catch (IOException e) {
                            // 处理反序列化异常
                            System.out.println(e.getMessage());
                            throw exception(BASE_TAG_HANDLE_TAG_DESERIALIZATION_EXCEPTION);
                        }
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getCondiments())) {
                        try {
                            // 进行反序列化操作
                            List<CommodityCondiments> commodityTagReqVOList = objectMapper.readValue(commodityStoreSpu.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                            });
                            // 将反序列化后的列表设置回 commoditySpus 对象
                            commodityStoreRespVo.setCondimentsList(commodityTagReqVOList);
                        } catch (IOException e) {
                            // 处理反序列化异常
                            System.out.println(e.getMessage());
                            throw exception(BASE_TAG_HANDLE_TAG_DESERIALIZATION_EXCEPTION);
                        }
                    }

                    commodityStoreRespVos.add(commodityStoreRespVo);

                }

            }
        }


        return commodityStoreRespVos;
    }

    private List<CommodityStoreRespVo> coverSpuList(List<CommodityStoreSpu> list) {

        List<CommodityStoreRespVo> commodityStoreRespVos = new ArrayList<>();
        for (CommodityStoreSpu commodityStoreSpu : list) {
            CommodityStoreRespVo commodityStoreRespVo = new CommodityStoreRespVo();
            BeanUtils.copyProperties(commodityStoreSpu, commodityStoreRespVo);
            commodityStoreRespVos.add(commodityStoreRespVo);
        }
        return commodityStoreRespVos;
    }

    @Transactional
    @Override
    public void doSync(Long storeId, List<CommoditySyncCategoryDTO> syncData) {

        //不增量 同步 category
        for (CommoditySyncCategoryDTO category : syncData) {
            CommodityStoreCategory storeCategory = category.getCategory();
            storeCategory.setStoreId(storeId);
            Long storeCategoryId = commodityStoreCategoryService.synchronizeSingle(storeCategory);

            List<CommoditySyncSpuDTO> spuList = category.getSpuList();
            for (CommoditySyncSpuDTO syncSpuDTO : spuList) {
                CommodityStoreSpu storeSpu = syncSpuDTO.getSpu();
                storeSpu.setStoreId(storeId);
                storeSpu.setCommodityStoreCategoryId(storeCategoryId);
                this.syncSaveOrUpdate(storeSpu);

                //构建sku 增量
                List<CommodityStoreSku> storeSkuList = syncSpuDTO.getStoreSkuList();
                storeSkuList.forEach(storeSku -> {
                    storeSku.setCommodityStoreSkuId(null);
                    storeSku.setStoreId(storeId);
                    storeSku.setCommodityStoreSpuId(storeSpu.getCommodityStoreSpuId());
                    commodityStoreSkuMapper.insert(storeSku);
                });


                //构建group 增量 套餐
                List<CommoditySyncSetMealDTO> setMealList = syncSpuDTO.getSetMealList();
                for (CommoditySyncSetMealDTO syncSetMealDTO : setMealList) {
                    CommodityStoreGroup storeGroup = syncSetMealDTO.getStoreGroup();
                    storeGroup.setStoreId(storeId);
                    storeGroup.setCommodityStoreGroupId(null);
                    storeGroup.setCommodityStoreSpuId(storeSpu.getCommodityStoreSpuId());
                    commodityStoreGroupService.save(storeGroup);

                    //构建single 增量 单品
                    List<CommodityStoreSingle> storeSingles = syncSetMealDTO.getStoreSingles();
                    for (CommodityStoreSingle storeSingle : storeSingles) {
                        storeSingle.setCommodityStoreSingleId(null);
                        storeSingle.setStoreId(storeId);
                        storeSingle.setCommodityStoreGroupId(storeGroup.getCommodityStoreGroupId());
                        storeSingle.setCommodityStoreSpuId(storeGroup.getCommodityStoreSpuId());
                        commodityStoreSingleService.save(storeSingle);
                    }

                }
            }
        }
    }

    @DataPermission(enable = false) // 不开启数据权限 异步调用场景会出现问题
    private void syncSaveOrUpdate(CommodityStoreSpu storeSpu) {
        List<CommodityStoreSpu> spuList = commodityStoreSpuMapper.listByCommodityId(storeSpu.getStoreId(), storeSpu.getCommodityId());
        if (CollectionUtil.isNotEmpty(spuList)) {

            List<Long> ids = spuList.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
            commodityStoreSkuService.deleteBySpuIds(ids);
            commodityStoreGroupService.deletBySpuIds(ids);
            commodityStoreSingleService.deleteBySpuIds(ids);

            //删除之前脏数据
            if (ids.size() > 1) {
                commodityStoreSpuMapper.deleteByIds(ids.subList(1, ids.size()));
            }
            for (CommodityStoreSpu storeSpus : spuList) {
                storeRedisDao.deleteProduct(storeSpus.getStoreId(), storeSpus.getCommodityStoreCategoryId(), storeSpus.getCommodityStoreSpuId());
            }
            storeSpu.setCommodityStoreSpuId(spuList.get(0).getCommodityStoreSpuId());
            commodityStoreSpuMapper.updateById(storeSpu);
            return;
        }
        storeSpu.setCommodityStoreSpuId(null);
        commodityStoreSpuMapper.insert(storeSpu);
    }

    @Override
    public void sortSpu(List<CommodityStoreSortSpuReqVO> sortReqVOS) {
        for (CommodityStoreSortSpuReqVO sortReqVO : sortReqVOS) {
            LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId, sortReqVO.getId());
            updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuSort, sortReqVO.getSort());
            commodityStoreSpuMapper.update(updateWrapper);
        }
        List<Long> spuIds = sortReqVOS.stream().map(CommodityStoreSortSpuReqVO::getId).toList();
        List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);
        for (SpuDto spuDto : spuDtos) {
            storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
        }
        Long storeId = spuDtos.get(0).getStoreId();
        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");

    }


    @Override
    public Boolean selectIsExistTag(Long id) {
        LambdaQueryWrapper<CommodityStoreSpu> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.apply("FIND_IN_SET({0}, tag_ids) > 0", id);
        long count = commodityStoreSpuMapper.selectCount(spusLambdaQueryWrapper);
        return count > 0;
    }

    /**
     * 点餐机修改商品
     *
     * @param reqVO
     */
    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_UPDATE_SUB_TYPE, bizNo = "{{#commodityStoreSpu.commodityStoreSpuId}}", success = COMMODITY_STORE_SPU_UPDATE_SUCCESS)
    @Transactional(rollbackFor = Exception.class)
    public void appletUpdateSpu(DCSpuUpdateReqVO reqVO) {

        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        reqVO.setLoginUserId(loginUserId);

        CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectById(reqVO.getCommodityStoreSpuId());
        if (!ObjectUtil.isNotEmpty(commodityStoreSpu)){
            throw new ServiceException(SPU_IS_DEL);
        }

        List<Long> spuIds = new ArrayList<>();

        if (reqVO.getCommodityStoreSpuIsSingle().equals(2)) {
            //如果是套餐 那只能修改分组的 是否可以多选商品
            commodityStoreGroupService.updateByDc(reqVO);
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            String flavorJson = null;
            String condiments = null;
            if (ObjectUtil.isNotEmpty(reqVO.getFlavorList())) {

                try {

                    List<CommodityFlavor> commodityFlavorList = reqVO.getFlavorList();
                    commodityFlavorList.forEach(commodityFlavor -> {
                        commodityFlavor.setFlavorId(IdWorker.getId());
                    });

                    // 将 commodityFlavorList 序列化为 JSON 字符串
                    flavorJson = objectMapper.writeValueAsString(reqVO.getFlavorList());


                    //修改套餐下子品的属性
                    List<Long> stemealIds = new ArrayList<>();
                    if (ObjectUtil.isNotEmpty(flavorJson)){
                        stemealIds=   commodityStoreSingleService.uodateSingleFlavor(flavorJson,commodityStoreSpu.getCommodityStorePrimitiveSpuId(),commodityStoreSpu.getStoreId());
                    }

                    if (ObjectUtil.isNotEmpty(stemealIds)){
                        spuIds.addAll(stemealIds);
                    }

                } catch (IOException e) {
                    // 处理 JSON 序列化异常
                    throw exception(BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION);
                }
            }
            if (ObjectUtil.isNotEmpty(reqVO.getCondimentsList())) {

                List<CommodityCondiments> commodityCondimentsList = reqVO.getCondimentsList();
                commodityCondimentsList.forEach(commodityCondiments -> {
                    commodityCondiments.setCondimentId(IdWorker.getId());
                });
                try {
                    // 将 commodityFlavorList 序列化为 JSON 字符串
                    condiments = objectMapper.writeValueAsString(reqVO.getCondimentsList());

                } catch (IOException e) {
                    // 处理 JSON 序列化异常
                    throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION);
                }


            }
            //修改商品小料和属性信息
            LambdaUpdateWrapper<CommodityStoreSpu> storeSpuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSpuLambdaUpdateWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId, reqVO.getCommodityStoreSpuId());
            storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCondiments, condiments);
            storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getFlavor, flavorJson);
            storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getUpdater, reqVO.getLoginUserId());
            storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getUpdateTime,new Date());
            commodityStoreSpuMapper.update(storeSpuLambdaUpdateWrapper);
            //修改商品 sku

            commodityStoreSkuService.updateByDc(reqVO);
        }



        spuIds.add(reqVO.getCommodityStoreSpuId());





        List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);

        for (SpuDto spuDto : spuDtos) {
            storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
        }
        LogRecordContext.putVariable("commodityStoreSpu", commodityStoreSpu);

    }


    @Override
    public ItemDto getCouponIsUsed(Long spuId, Long storeId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemDto itemDto = new ItemDto();
        CommodityStoreSpuDTO commodityStoreSpu = new CommodityStoreSpuDTO();
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(new LambdaQueryWrapper<CommodityStoreSpu>()
                .eq(CommodityStoreSpu::getCommodityId, spuId).eq(CommodityStoreSpu::getStoreId, storeId).eq(CommodityStoreSpu::getDeleted, 0)
                .eq(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, 1).orderByDesc(CommodityStoreSpu::getCreateTime));
        List<CommodityStoreSpuDTO> bean = BeanUtils.toBean(commodityStoreSpus, CommodityStoreSpuDTO.class);
        TimeSharingUtil.processTimeBasedList(bean);
        bean.removeIf(item -> !item.getIsUp());
        if (bean != null && bean.size() > 0) {
            commodityStoreSpu = bean.get(0);
        }
        if (commodityStoreSpu != null && ObjectUtil.isNotEmpty(commodityStoreSpu.getCommodityStoreSpuAppletStatus()) && commodityStoreSpu.getCommodityStoreSpuAppletStatus() == 1) {
            LambdaQueryWrapper<CommodityStoreSku> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommodityStoreSku::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId())
                    .eq(CommodityStoreSku::getDeleted, 0)
                    .eq(CommodityStoreSku::getCommodityStoreSkuStatus, 1);
            List<CommodityStoreSku> skuList = commodityStoreSkuService.list(wrapper);
            skuList.sort(Comparator.comparing(CommodityStoreSku::getCommodityStoreSkuPrice));

            if (skuList != null && skuList.size() > 1) {
                commodityStoreSpu.setIsMoreSku(true);
                PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPricesStore(skuList);
                commodityStoreSpu.setLowPrice(minMaxPrices.getMinIllustratePrices());
                commodityStoreSpu.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                commodityStoreSpu.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                commodityStoreSpu.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
            } else {
                commodityStoreSpu.setIsMoreSku(false);
                commodityStoreSpu.setLowPrice(skuList.get(0).getCommodityStoreSkuPrice());
                commodityStoreSpu.setLowStrikePrice(skuList.get(0).getCommodityStoreSkuStrikePrice());
            }

            commodityStoreSpu.setCommoditySkusList(skuList);
            if (commodityStoreSpu.getCommodityStoreSpuIsSingle() == 2) {
                LambdaQueryWrapper<CommodityStoreGroup> wrapperGroup = new LambdaQueryWrapper<>();
                wrapperGroup.eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpu.getCommodityStoreSpuId())
                        .eq(CommodityStoreGroup::getDeleted, 0);
                List<CommodityStoreGroup> groupList = commodityStoreGroupService.list(wrapperGroup);
                groupList.forEach(group -> {
                    LambdaQueryWrapper<CommodityStoreSingle> wrapperSingle = new LambdaQueryWrapper<>();
                    wrapperSingle.eq(CommodityStoreSingle::getCommodityStoreGroupId, group.getCommodityStoreGroupId())
                            .eq(CommodityStoreSingle::getDeleted, 0);
                    List<CommodityStoreSingle> singleList = commodityStoreSingleService.list(wrapperSingle);
                    group.setCommodityStoreSingleList(singleList);
                });
                commodityStoreSpu.setCommodityStoreGroups(groupList);
            }

            if (!"".equals(commodityStoreSpu.getFlavor()) || ObjectUtil.isNotEmpty(commodityStoreSpu.getFlavor())) {
                try {
                    // 进行反序列化操作
                    List<CommodityFlavor> commodityFlavorList = objectMapper.readValue(commodityStoreSpu.getFlavor(), new TypeReference<>() {
                    });
                    System.out.println(commodityFlavorList.size());
                    // 将反序列化后的列表设置回 commoditySpus 对象
                    commodityStoreSpu.setFlavorList(commodityFlavorList);
                } catch (IOException e) {
                    // 处理反序列化异常
                    System.out.println(e.getMessage());
                    throw exception(COUPON_CAN_NOT_USE);
                }
            }

            if (!"".equals(commodityStoreSpu.getCondiments()) || ObjectUtil.isNotEmpty(commodityStoreSpu.getCondiments())) {
                try {
                    List<CommodityCondiments> commodityCondimentsList = objectMapper.readValue(commodityStoreSpu.getCondiments(), new TypeReference<>() {
                    });
                    commodityStoreSpu.setCondimentsList(commodityCondimentsList);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    throw exception(COUPON_CAN_NOT_USE);
                }
            }
            itemDto = convertToItemDto(commodityStoreSpu, 0);
        } else {
            throw exception(COUPON_CAN_NOT_USE);
        }
        return itemDto;
    }

    @Override
    public List<StoreSpuCountDTO> saleStoreCountByCommodityIds(Set<Long> commodityIds, Set<Long> storeIds) {
        return commodityStoreSpuMapper.saleStoreCountByCommodityIds(commodityIds, storeIds);
    }

    @Override
    public List<CommodityStoreRespVo> getByStoreSpuList(CommodityStoreAllReqVO allReqVO) {
        List<CommodityStoreRespVo> commodityStoreRespVos = new ArrayList<>();

        LambdaQueryWrapper<CommodityStoreSpu> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spusLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreCategoryId, allReqVO.getCommodityStoreCategoryId());
//        spusLambdaQueryWrapper.eq(CommodityStoreSpu::getStoreId, allReqVO.getStoreId());

        spusLambdaQueryWrapper.orderByAsc(CommodityStoreSpu::getCommodityStoreSpuSort);
        List<CommodityStoreSpu> list = commodityStoreSpuMapper.selectList(spusLambdaQueryWrapper);


        if (ObjectUtil.isNotEmpty(list)) {

            List<Long> commodityIds = list.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).collect(Collectors.toList());


            List<CommodityStoreSku> commoditySkuses = commodityStoreSkuService.selectBySpuIds(commodityIds);

            if (ObjectUtil.isNotEmpty(commoditySkuses)) {
                Map<Long, List<CommodityStoreSku>> collect = commoditySkuses.stream().collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));

                for (CommodityStoreSpu commodityStoreSpu : list) {
                    CommodityStoreRespVo commodityStoreRespVo = new CommodityStoreRespVo();
                    BeanUtils.copyProperties(commodityStoreSpu, commodityStoreRespVo);
                    List<CommodityStoreSku> commoditySkuses1 = collect.get(commodityStoreSpu.getCommodityStoreSpuId());
                    if (commoditySkuses1 != null && commoditySkuses1.size() > 1) {
                        commodityStoreRespVo.setIsMoreSku(true);
                        PriceResultDTO minMaxPrices = PriceResultDTO.findMinMaxPricesStore(commoditySkuses1);
                        commodityStoreRespVo.setLowPrice(minMaxPrices.getMinIllustratePrices());
                        commodityStoreRespVo.setHighPrice(minMaxPrices.getMaxIllustratePrices());
                        commodityStoreRespVo.setLowStrikePrice(minMaxPrices.getMinStrikeThroughPrice());
                        commodityStoreRespVo.setHighStrikePrice(minMaxPrices.getMaxStrikeThroughPrice());
                    } else {
                        commodityStoreRespVo.setIsMoreSku(false);
                        if (commoditySkuses1 != null) {
                            commodityStoreRespVo.setLowPrice(commoditySkuses1.get(0).getCommodityStoreSkuPrice());
                            commodityStoreRespVo.setLowStrikePrice(commoditySkuses1.get(0).getCommodityStoreSkuStrikePrice());
                        }

                    }

                    if (ObjectUtil.isNotEmpty(commoditySkuses1)) {
                        List<CommodityStoreSkuRespVo> commodityStoreSkuRespVoList = coverSku(commoditySkuses1);
                        commodityStoreRespVo.setCommoditySkusList(commodityStoreSkuRespVoList);
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())) {
                        commodityStoreRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getTagIds())) {
                        List<CommodityTag> commodityTags = commodityTageService.selectListByIds(ConvertUtil.convertStringToList(commodityStoreSpu.getTagIds()));
                        commodityStoreRespVo.setTagList(commodityTags);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getFlavor())) {
                        try {
                            // 进行反序列化操作
                            List<CommodityFlavor> commodityTagReqVOList = objectMapper.readValue(commodityStoreSpu.getFlavor(), new TypeReference<List<CommodityFlavor>>() {
                            });
                            // 将反序列化后的列表设置回 commoditySpus 对象
                            commodityStoreRespVo.setFlavorList(commodityTagReqVOList);
                        } catch (IOException e) {
                            // 处理反序列化异常
                            System.out.println(e.getMessage());
                            throw exception(BASE_TAG_HANDLE_TAG_DESERIALIZATION_EXCEPTION);
                        }
                    }

                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getCondiments())) {
                        try {
                            // 进行反序列化操作
                            List<CommodityCondiments> commodityTagReqVOList = objectMapper.readValue(commodityStoreSpu.getCondiments(), new TypeReference<List<CommodityCondiments>>() {
                            });
                            // 将反序列化后的列表设置回 commoditySpus 对象
                            commodityStoreRespVo.setCondimentsList(commodityTagReqVOList);
                        } catch (IOException e) {
                            // 处理反序列化异常
                            System.out.println(e.getMessage());
                            throw exception(BASE_TAG_HANDLE_TAG_DESERIALIZATION_EXCEPTION);
                        }
                    }

                    commodityStoreRespVos.add(commodityStoreRespVo);

                }

            }
        }


        return commodityStoreRespVos;
    }


    private ItemDto convertToItemDto(CommodityStoreSpu commodityStoreSpus, int source) {
        ItemDto itemDto = new ItemDto();
        itemDto.setSpuId(commodityStoreSpus.getCommodityStoreSpuId());
        itemDto.setStoreId(commodityStoreSpus.getStoreId());
        itemDto.setShowBtn(0);
        itemDto.setCategoryId(commodityStoreSpus.getCommodityId());
        itemDto.setManyCopy(commodityStoreSpus.getManyCopy());
        itemDto.setPackageFee(commodityStoreSpus.getPackageFee());
        itemDto.setImageUrl(commodityStoreSpus.getImageUrl());
        itemDto.setSpuName(commodityStoreSpus.getCommodityStoreSpuName());
        itemDto.setSpuDesc(commodityStoreSpus.getCommodityStoreSpuDescription());
        itemDto.setSpuPrice(commodityStoreSpus.getLowPrice());
        itemDto.setSpuTag(commodityStoreSpus.getSpusTag());
        itemDto.setCommodityStoreSpuAppletStatus(commodityStoreSpus.getCommodityStoreSpuAppletStatus());
        itemDto.setCommodityStoreSpuMachineStatus(commodityStoreSpus.getCommodityStoreSpuMachineStatus());
        itemDto.setSpuUnderlinedPrice(commodityStoreSpus.getLowStrikePrice());
        itemDto.setLimitBuyNumber(ObjectUtils.isEmpty(commodityStoreSpus.getLimitBuyNumber()) ? 1 : commodityStoreSpus.getLimitBuyNumber());
        if (ObjectUtil.isNotEmpty(commodityStoreSpus.getImageUrl())) {
            itemDto.setBannerList(ConvertUtil.convertStringToListS(commodityStoreSpus.getImageUrl()));
        }
        if (commodityStoreSpus.getSetmealType() != null) {
            itemDto.setSetmealType(commodityStoreSpus.getSetmealType());
        }
        itemDto.setCategoryId(commodityStoreSpus.getCommodityStoreCategoryId());
        itemDto.setListingStatus(commodityStoreSpus.getCommodityStoreSpuMachineStatus());
        itemDto.setCommodityId(commodityStoreSpus.getCommodityId());
        // 组装sku
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCommoditySkusList())) {
            List<CommodityStoreSku> commodityStoreSkusList = commodityStoreSpus.getCommoditySkusList().stream()
                    .filter(sku -> sku.getCommodityStoreSkuStatus() != 0)
                    .collect(Collectors.toList());
            if (commodityStoreSkusList.size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<SkuDto> skuDtoList = commodityStoreSpus.getCommoditySkusList().stream()
                    .map(sku -> {
                        SkuDto skuDto = new SkuDto();
                        skuDto.setSkuId(sku.getCommodityStoreSkuId());
                        skuDto.setSkuName(sku.getCommodityStoreSkuName());
                        skuDto.setSkuValue(sku.getCommodityStoreSkuValue());
                        skuDto.setCommodityStoreSkuStrikePrice(sku.getCommodityStoreSkuStrikePrice());
                        skuDto.setCommodityStoreSkuStatus(sku.getCommodityStoreSkuStatus());
                        skuDto.setSkuPrice(sku.getCommodityStoreSkuPrice());
                        return skuDto;
                    })
                    .collect(Collectors.toList());
            if (source == 0) {
                skuDtoList.removeIf(sku -> sku.getCommodityStoreSkuStatus() == 0);
            }
            itemDto.setSkuList(skuDtoList);
        }

        // 组装小料
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCondimentsList())) {
            if (commodityStoreSpus.getCondimentsList().size() > 0) {
                itemDto.setShowBtn(1);
            }
            List<CondimentDto> condimentDtos = commodityStoreSpus.getCondimentsList().stream()
                    .map(condiment -> {
                        CondimentDto condimentDto = new CondimentDto();
                        condimentDto.setCondimentId(condiment.getCondimentId());
                        condimentDto.setCondimentName(condiment.getCondimentName());
                        condimentDto.setImageUrl(condiment.getImageUrl());
                        condimentDto.setNumber(condiment.getNumber());
                        condimentDto.setStatus(condiment.getStatus());
                        condimentDto.setCondimentPrice(condiment.getPrice());
                        return condimentDto;
                    })
                    .collect(Collectors.toList());
            if (source == 0) {
                condimentDtos.removeIf(condiment -> condiment.getStatus() == 0);
            }
            itemDto.setCondimentsList(condimentDtos);
        }
        // 套餐组装
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCommodityStoreGroups())) {
            if (commodityStoreSpus.getCommodityStoreGroups().size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<GroupDto> groupDtos = commodityStoreSpus.getCommodityStoreGroups().stream()
                    .map(group -> {
                        GroupDto groupDto = new GroupDto();
                        groupDto.setGroupName(group.getCommodityStoreGroupName());
                        groupDto.setGroupId(group.getCommodityStoreGroupId());
                        groupDto.setChoose(group.getCommodityStoreGroupChoose() != null ? group.getCommodityStoreGroupChoose() : 0);
                        groupDto.setChooseMany(group.getChooseMany());
                        groupDto.setCommodityStoreGroupAttribute(group.getCommodityStoreGroupAttribute());
                        groupDto.setCommodityStoreGroupChoose(group.getCommodityStoreGroupChoose());
                        if (CollectionUtil.isNotEmpty(group.getCommodityStoreSingleList())) {
                            if (source == 0) {
                                group.getCommodityStoreSingleList().removeIf(single -> single.getWxStatus() == 0 || single.getCommodityStoreSingleStatus() == 0);
                            }
                            if (group.getCommodityStoreSingleList().size() > 0) {
                                List<SingleDto> singleDtos = group.getCommodityStoreSingleList().stream()
                                        .map(single -> {
                                            SingleDto singleDto = new SingleDto();
                                            singleDto.setSingleId(single.getCommodityStoreSingleId());
                                            singleDto.setSingleName(single.getCommodityName());
                                            singleDto.setImageUrl(single.getCommodityUrl());
                                            singleDto.setUpPrice(single.getCommodityStoreSinglePrice());
                                            singleDto.setWxStatus(single.getWxStatus());
                                            singleDto.setStoreStatus(single.getStoreStatus());
                                            if (source == 0) {
                                                singleDto.setCommodityStoreSingleStatus(single.getWxStatus());
                                            } else {
                                                singleDto.setCommodityStoreSingleStatus(single.getStoreStatus());
                                            }
                                            singleDto.setCommodityStoreSingleCopies(single.getCommodityStoreSingleCopies());
                                            singleDto.setDefaultChoose(single.getDefaultChoose());
                                            singleDto.setSkuName(single.getSingleSkuName() != null ? single.getSingleSkuName() : " ");
                                            singleDto.setCommodityId(single.getCommodityId());
                                            singleDto.setRequiredChoose(single.getRequiredChoose());
                                            return singleDto;
                                        })
                                        .collect(Collectors.toList());
                                groupDto.setSingleList(singleDtos);
                            }
                        }
                        return groupDto;
                    })
                    .collect(Collectors.toList());
            groupDtos.removeIf(group -> group.getSingleList().isEmpty());
            itemDto.setShowBtn(1);
            itemDto.setGroupList(groupDtos);
            itemDto.setSetmealType(commodityStoreSpus.getSetmealType());
        } else {
            itemDto.setSetmealType(3);
        }

        // 属性组装
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getFlavorList())) {
            if (commodityStoreSpus.getFlavorList().size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<FlavorDto> flavorDtos = commodityStoreSpus.getFlavorList().stream()
                    .map(flavor -> {
                        FlavorDto flavorDto = new FlavorDto();
                        flavorDto.setFlavorId(flavor.getFlavorId());
                        flavorDto.setFlavorName(flavor.getFlavorName());
                        List<String> flavorValues = flavor.getFlavorValueListMap().stream()
                                .flatMap(map -> map.entrySet().stream())
                                .filter(entry -> entry.getValue() != 0)  // 过滤掉 value 等于 0 的条目
                                .map(Entry::getKey)  // 提取键
                                .collect(Collectors.toList());
                        if (itemDto.getShowBtn() == 0 && flavorValues != null && flavorValues.size() > 1) {
                            itemDto.setShowBtn(1);
                        }
                        flavorDto.setFlavorValues(flavorValues);
                        flavorDto.setFlavorValueListMap(flavor.getFlavorValueListMap());
                        return flavorDto;
                    })
                    .collect(Collectors.toList());
            itemDto.setFlavorList(flavorDtos);
        }

        return itemDto;
    }


    private ItemDto convertToItemDto(CommodityStoreSpuDTO commodityStoreSpus, int source) {
        ItemDto itemDto = new ItemDto();
        itemDto.setSpuId(commodityStoreSpus.getCommodityStoreSpuId());
        itemDto.setStoreId(commodityStoreSpus.getStoreId());
        itemDto.setShowBtn(0);
        itemDto.setCategoryId(commodityStoreSpus.getCommodityId());
        itemDto.setManyCopy(commodityStoreSpus.getManyCopy());
        itemDto.setPackageFee(commodityStoreSpus.getPackageFee());
        itemDto.setImageUrl(commodityStoreSpus.getImageUrl());
        itemDto.setSpuName(commodityStoreSpus.getCommodityStoreSpuName());
        itemDto.setSpuDesc(commodityStoreSpus.getCommodityStoreSpuDescription());
        itemDto.setSpuPrice(commodityStoreSpus.getLowPrice());
        itemDto.setSpuTag(commodityStoreSpus.getSpusTag());
        itemDto.setCommodityStoreSpuAppletStatus(commodityStoreSpus.getCommodityStoreSpuAppletStatus());
        itemDto.setCommodityStoreSpuMachineStatus(commodityStoreSpus.getCommodityStoreSpuMachineStatus());
        itemDto.setSpuUnderlinedPrice(commodityStoreSpus.getLowStrikePrice());
        itemDto.setLimitBuyNumber(ObjectUtils.isEmpty(commodityStoreSpus.getLimitBuyNumber()) ? 1 : commodityStoreSpus.getLimitBuyNumber());
        if (ObjectUtil.isNotEmpty(commodityStoreSpus.getImageUrl())) {
            itemDto.setBannerList(ConvertUtil.convertStringToListS(commodityStoreSpus.getImageUrl()));
        }
        if (commodityStoreSpus.getSetmealType() != null) {
            itemDto.setSetmealType(commodityStoreSpus.getSetmealType());
        }
        itemDto.setCategoryId(commodityStoreSpus.getCommodityStoreCategoryId());
        itemDto.setListingStatus(commodityStoreSpus.getCommodityStoreSpuMachineStatus());
        itemDto.setCommodityId(commodityStoreSpus.getCommodityId());
        // 组装sku
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCommoditySkusList())) {
            List<CommodityStoreSku> commodityStoreSkusList = commodityStoreSpus.getCommoditySkusList().stream()
                    .filter(sku -> sku.getCommodityStoreSkuStatus() != 0)
                    .collect(Collectors.toList());
            if (commodityStoreSkusList.size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<SkuDto> skuDtoList = commodityStoreSpus.getCommoditySkusList().stream()
                    .map(sku -> {
                        SkuDto skuDto = new SkuDto();
                        skuDto.setSkuId(sku.getCommodityStoreSkuId());
                        skuDto.setSkuName(sku.getCommodityStoreSkuName());
                        skuDto.setSkuValue(sku.getCommodityStoreSkuValue());
                        skuDto.setCommodityStoreSkuStrikePrice(sku.getCommodityStoreSkuStrikePrice());
                        skuDto.setCommodityStoreSkuStatus(sku.getCommodityStoreSkuStatus());
                        skuDto.setSkuPrice(sku.getCommodityStoreSkuPrice());
                        return skuDto;
                    })
                    .collect(Collectors.toList());
            if (source == 0) {
                skuDtoList.removeIf(sku -> sku.getCommodityStoreSkuStatus() == 0);
            }
            itemDto.setSkuList(skuDtoList);
        }

        // 组装小料
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCondimentsList())) {
            if (commodityStoreSpus.getCondimentsList().size() > 0) {
                itemDto.setShowBtn(1);
            }
            List<CondimentDto> condimentDtos = commodityStoreSpus.getCondimentsList().stream()
                    .map(condiment -> {
                        CondimentDto condimentDto = new CondimentDto();
                        condimentDto.setCondimentId(condiment.getCondimentId());
                        condimentDto.setCondimentName(condiment.getCondimentName());
                        condimentDto.setImageUrl(condiment.getImageUrl());
                        condimentDto.setNumber(condiment.getNumber());
                        condimentDto.setStatus(condiment.getStatus());
                        condimentDto.setCondimentPrice(condiment.getPrice());
                        return condimentDto;
                    })
                    .collect(Collectors.toList());
            if (source == 0) {
                condimentDtos.removeIf(condiment -> condiment.getStatus() == 0);
            }
            itemDto.setCondimentsList(condimentDtos);
        }
        // 套餐组装
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getCommodityStoreGroups())) {
            if (commodityStoreSpus.getCommodityStoreGroups().size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<GroupDto> groupDtos = commodityStoreSpus.getCommodityStoreGroups().stream()
                    .map(group -> {
                        GroupDto groupDto = new GroupDto();
                        groupDto.setGroupName(group.getCommodityStoreGroupName());
                        groupDto.setGroupId(group.getCommodityStoreGroupId());
                        groupDto.setChoose(group.getCommodityStoreGroupChoose() != null ? group.getCommodityStoreGroupChoose() : 0);
                        groupDto.setChooseMany(group.getChooseMany());
                        groupDto.setCommodityStoreGroupAttribute(group.getCommodityStoreGroupAttribute());
                        groupDto.setCommodityStoreGroupChoose(group.getCommodityStoreGroupChoose());
                        if (CollectionUtil.isNotEmpty(group.getCommodityStoreSingleList())) {
                            if (source == 0) {
                                group.getCommodityStoreSingleList().removeIf(single -> single.getWxStatus() == 0 || single.getCommodityStoreSingleStatus() == 0);
                            }
                            if (group.getCommodityStoreSingleList().size() > 0) {
                                List<SingleDto> singleDtos = group.getCommodityStoreSingleList().stream()
                                        .map(single -> {
                                            SingleDto singleDto = new SingleDto();
                                            singleDto.setSingleId(single.getCommodityStoreSingleId());
                                            singleDto.setSingleName(single.getCommodityName());
                                            singleDto.setImageUrl(single.getCommodityUrl());
                                            singleDto.setUpPrice(single.getCommodityStoreSinglePrice());
                                            singleDto.setWxStatus(single.getWxStatus());
                                            singleDto.setStoreStatus(single.getStoreStatus());
                                            if (source == 0) {
                                                singleDto.setCommodityStoreSingleStatus(single.getWxStatus());
                                            } else {
                                                singleDto.setCommodityStoreSingleStatus(single.getStoreStatus());
                                            }
                                            singleDto.setCommodityStoreSingleCopies(single.getCommodityStoreSingleCopies());
                                            singleDto.setDefaultChoose(single.getDefaultChoose());
                                            singleDto.setSkuName(single.getSingleSkuName() != null ? single.getSingleSkuName() : " ");
                                            singleDto.setCommodityId(single.getCommodityId());
                                            singleDto.setRequiredChoose(single.getRequiredChoose());
                                            return singleDto;
                                        })
                                        .collect(Collectors.toList());
                                groupDto.setSingleList(singleDtos);
                            }
                        }
                        return groupDto;
                    })
                    .collect(Collectors.toList());
            groupDtos.removeIf(group -> group.getSingleList().isEmpty());
            itemDto.setShowBtn(1);
            itemDto.setGroupList(groupDtos);
            itemDto.setSetmealType(commodityStoreSpus.getSetmealType());
        } else {
            itemDto.setSetmealType(3);
        }

        // 属性组装
        if (CollectionUtil.isNotEmpty(commodityStoreSpus.getFlavorList())) {
            if (commodityStoreSpus.getFlavorList().size() > 1) {
                itemDto.setShowBtn(1);
            }
            List<FlavorDto> flavorDtos = commodityStoreSpus.getFlavorList().stream()
                    .map(flavor -> {
                        FlavorDto flavorDto = new FlavorDto();
                        flavorDto.setFlavorId(flavor.getFlavorId());
                        flavorDto.setFlavorName(flavor.getFlavorName());
                        List<String> flavorValues = flavor.getFlavorValueListMap().stream()
                                .flatMap(map -> map.entrySet().stream())
                                .filter(entry -> entry.getValue() != 0)  // 过滤掉 value 等于 0 的条目
                                .map(Entry::getKey)  // 提取键
                                .collect(Collectors.toList());
                        if (itemDto.getShowBtn() == 0 && flavorValues != null && flavorValues.size() > 1) {
                            itemDto.setShowBtn(1);
                        }
                        flavorDto.setFlavorValues(flavorValues);
                        flavorDto.setFlavorValueListMap(flavor.getFlavorValueListMap());
                        return flavorDto;
                    })
                    .collect(Collectors.toList());
            itemDto.setFlavorList(flavorDtos);
        }

        return itemDto;
    }


    @Override
    public List<StoreSingleSimpleRespVO> getStoreSingle(StoreSingleSimpleReqVO storeSingleSimpleReqVO) {

        List<StoreSingleSimpleRespVO> storeSingleSimpleRespVOS = new ArrayList<>();
        LambdaQueryWrapper<CommodityStoreSpu> spuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getStoreId, storeSingleSimpleReqVO.getStoreId());
        spuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuIsSingle,1);
        if (ObjectUtil.isNotEmpty(storeSingleSimpleReqVO.getCommodityStoreSpuName())){
            spuLambdaQueryWrapper.like(CommodityStoreSpu::getCommodityStoreSpuName, storeSingleSimpleReqVO.getCommodityStoreSpuName());
        }
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(spuLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(commodityStoreSpus)) {
            List<Long> commodityIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
            LambdaQueryWrapper<CommodityStoreSku>  commodityStoreSkuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commodityStoreSkuLambdaQueryWrapper.in(CommodityStoreSku::getCommodityStoreSpuId, commodityIds);
            List<CommodityStoreSku> commodityStoreSkus = commodityStoreSkuMapper.selectList(commodityStoreSkuLambdaQueryWrapper);
            Map<Long, List<CommodityStoreSku>> skuMap = commodityStoreSkus.stream().collect(Collectors.groupingBy(CommodityStoreSku::getCommodityStoreSpuId));
            for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
                StoreSingleSimpleRespVO storeSingleSimpleRespVO = new StoreSingleSimpleRespVO();
                storeSingleSimpleRespVO.setCommodityStoreSpuId(storeSpus.getCommodityStoreSpuId());
                storeSingleSimpleRespVO.setCommodityStoreSpuName(storeSpus.getCommodityStoreSpuName());
                List<StoreSingleSkuVO>  storeSingleSkuVOS = new ArrayList<>();
                List<CommodityStoreSku> commodityStoreSkus1 = skuMap.get(storeSpus.getCommodityStoreSpuId());
                if (ObjectUtil.isNotEmpty(commodityStoreSkus1)) {
                    for (CommodityStoreSku commodityStoreSku : commodityStoreSkus1) {
                        StoreSingleSkuVO storeSingleSkuVO = new StoreSingleSkuVO();
                        storeSingleSkuVO.setCommodityStoreSkuId(commodityStoreSku.getCommodityStoreSkuId());
                        storeSingleSkuVO.setCommodityStoreSkuPrice(commodityStoreSku.getCommodityStoreSkuPrice());
                        storeSingleSkuVO.setCommodityStoreSkuStrikePrice(commodityStoreSku.getCommodityStoreSkuStrikePrice());
                        storeSingleSkuVO.setSkuId(commodityStoreSku.getSkuId());
                        storeSingleSkuVOS.add(storeSingleSkuVO);
                    }
                    storeSingleSimpleRespVO.setStoreSingleSkuVOS(storeSingleSkuVOS);
                }
                storeSingleSimpleRespVOS.add(storeSingleSimpleRespVO);
            }

        }




        return storeSingleSimpleRespVOS;



    }

    @Override
    public PageResult<StoreInfoDTO> getStoreListByCommodityId(RecipeCommodityStoreReqVO recipeCommodityStoreReqVO) {
        LambdaQueryWrapper<CommodityStoreSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommodityStoreSpu::getCommodityId, recipeCommodityStoreReqVO.getCommodityId());

        List<Long> storeIds = new ArrayList<>();
        ResultHandler<Long> resultHandler = new ResultHandler<Long>() {
            @Override
            public void handleResult (ResultContext<? extends Long> resultContext) {
                // 获取当前行的结果（即 memberId）
                Long storeId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(storeId)) {
                    storeIds.add(storeId);
                }
            }
        };
        wrapper.select(CommodityStoreSpu::getStoreId);

        commodityStoreSpuMapper.selectObjs(wrapper, resultHandler);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(recipeCommodityStoreReqVO.getPageNo());
        pageParam.setPageSize(recipeCommodityStoreReqVO.getPageSize());

        //查询门店
        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(storeIds, pageParam);

        if (storeInfoByStoreIds.isSuccess()) {
            return storeInfoByStoreIds.getCheckedData();
        }

        return PageResult.empty();
    }

    @Override
    public StoreSingleUpRespVO singleUpSelect(StoreSingleUpReqVO singleUpReqVo) {
        StoreSingleUpRespVO storeSingleUpRespVO = new StoreSingleUpRespVO();

        Long commodityStoreSpuId = singleUpReqVo.getCommodityStoreSpuId();


        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
        CommodityStoreSpu byId = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (ObjectUtil.isEmpty(byId)){
            throw new ServiceException(SPU_IS_DEL);
        }



        Integer chooseView = singleUpReqVo.getChooseView();
        Long storeId = singleUpReqVo.getStoreId();

        storeSingleUpRespVO.setCommodityStoreSpuId(commodityStoreSpuId);
        storeSingleUpRespVO.setChooseView(chooseView);
        storeSingleUpRespVO.setStoreId(storeId);

        CommodityStoreSpu commodityStoreSpu = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        List<Long> spuIds = new ArrayList<>();
        spuIds.add(commodityStoreSpu.getCommodityStorePrimitiveSpuId());

        List<CommodityStoreSingle> storeSingles = commodityStoreSingleService.selectSpuIds(spuIds,  storeId);

        if (CollectionUtil.isEmpty(storeSingles)) {
            return storeSingleUpRespVO;
        }

        //拿到门店下的套餐 IDs
        List<Long> storePackageIDs = storeSingles.stream().distinct().map(CommodityStoreSingle::getCommodityStoreSpuId).toList();
        //获得套餐
       List<CommodityStoreSpu> storePackages =  getStorePackagesByIds(storePackageIDs);
        //获得分组
        List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectBySpuIds(storePackageIDs);
        List<Long> storeGroupIds = commodityStoreGroups.stream().map(CommodityStoreGroup::getCommodityStoreGroupId).toList();
        //获得子品
        List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleService.selectByGroupIds(storeGroupIds);
        //将分组做成 Map
        Map<Long, List<CommodityStoreGroup>> groupMap = commodityStoreGroups.stream().collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
        //将子品做成 Map
        Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreSpuId));
        List<CommodityStoreSpuRespVo> storeSpu= new ArrayList<>();
        for (CommodityStoreSpu storePackage : storePackages) {
            List<CommodityStoreGroup> commodityStoreGroups1 = groupMap.get(storePackage.getCommodityStoreSpuId());
            List<CommodityStoreSingle> commodityStoreSingles1 = singleMap.get(storePackage.getCommodityStoreSpuId());
            List<Long> primitiveIds = new ArrayList<>();
            primitiveIds.add(commodityStoreSpu.getCommodityStorePrimitiveSpuId());
            // 仅当本次触发上架的单品命中了“非加价组”时，才需要提示套餐可上架。
            if (!triggeredByNonSurchargeStoreGroup(commodityStoreGroups1, commodityStoreSingles1, primitiveIds, storeId)) {
                continue;
            }

            Boolean b = getOrUp(storePackage,commodityStoreGroups1,commodityStoreSingles1,chooseView,primitiveIds,storeId);

            if (Boolean.TRUE.equals(b)) {
                CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
                BeanUtils.copyProperties(storePackage,commodityStoreSpuRespVo);
                if (ObjectUtil.isNotEmpty(storePackage.getImageUrl())){
                    commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(storePackage.getImageUrl()));
                }
                storeSpu.add(commodityStoreSpuRespVo);
            }

        }
        storeSingleUpRespVO.setStoreSpu(storeSpu);



        return storeSingleUpRespVO;
    }

    /**
     * 判断本次触发上架的门店单品，是否命中套餐中的非加价组。
     * 若仅命中加价组，则不触发“可联动上架套餐”的提醒。
     */
    private boolean triggeredByNonSurchargeStoreGroup(List<CommodityStoreGroup> groups,
                                                      List<CommodityStoreSingle> singles,
                                                      List<Long> triggerPrimitiveIds,
                                                      Long storeId) {
        if (CollectionUtils.isEmpty(groups) || CollectionUtils.isEmpty(singles) || CollectionUtils.isEmpty(triggerPrimitiveIds)) {
            return false;
        }
        Set<Long> nonSurchargeGroupIds = groups.stream()
            .filter(group -> !Objects.equals(group.getCommodityStoreGroupAttribute(), SpuEnum.SURCHARGE_GROUP.getCode()))
            .map(CommodityStoreGroup::getCommodityStoreGroupId)
            .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(nonSurchargeGroupIds)) {
            return false;
        }
        return singles.stream().anyMatch(single ->
            Objects.equals(single.getStoreId(), storeId)
                && triggerPrimitiveIds.contains(single.getCommodityId())
                && nonSurchargeGroupIds.contains(single.getCommodityStoreGroupId()));
    }

    private Boolean getOrUp(CommodityStoreSpu storePackage, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> primitiveIds, Long storeId) {
       if (chooseView.equals(SpuEnum.WX.getCode())){
           return getWXStoreBoolean(storePackage,commodityStoreGroups1,commodityStoreSingles1,chooseView,primitiveIds,storeId);
       }else {
           return getStoreStoreBoolean(storePackage,commodityStoreGroups1,commodityStoreSingles1,chooseView,primitiveIds,storeId);
       }
    }

    private Boolean getStoreStoreBoolean(CommodityStoreSpu storePackage, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> primitiveIds, Long storeId) {
        if (storePackage.getCommodityStoreSpuMachineStatus().equals(SpuEnum.UP.getCode())) {
            //如果商品本来就是上架不用管
            return false;
        }else {
            getStoreCommodityGroupSingle(primitiveIds,commodityStoreSingles1,chooseView,SpuEnum.UP.getCode());
            // 1.固定搭配 2.分组可选套餐
            if (storePackage.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityStoreSingle> filteredList = commodityStoreSingles1.stream()
                        .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()) )
                        .filter(item -> storeId.equals(item.getStoreId()))
                        .toList();

                return CollectionUtils.isEmpty(filteredList);
            }else {
                //分组可选
                Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles1.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
                boolean up = true;
                for (CommodityStoreGroup commodityStoreGroup : commodityStoreGroups1) {
                    // 加价组不参与套餐上架判定
                    if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
                        continue;
                    }
                    //找到分组下的所有子品
                    List<CommodityStoreSingle> commodityStoreSingles = singleMap.get(commodityStoreGroup.getCommodityStoreGroupId());
                    if (CollectionUtils.isEmpty(commodityStoreSingles)) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(up)) {
                        //1 可选 2固定
                        if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.FIXED.getCode())) {
                            //分组内固定
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.UP.getCode()) )
                                    .filter(item -> storeId.equals(item.getStoreId()))
                                    .toList();
                            up = filteredList.size() == commodityStoreSingles.size();
                        }else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item ->item.getRequiredChoose()==SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item ->item.getStoreStatus() == SpuEnum.DOWN.getCode())
                                    .toList();
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                                up = false;
                                break;
                            }


                            //同一商品是否可选多份 1 是 0否
                            Integer chooseMany = commodityStoreGroup.getChooseMany();
                            //必选数量
                            Integer choose = commodityStoreGroup.getCommodityStoreGroupChoose();
                            List<CommodityStoreSingle> filteredListSingle = commodityStoreSingles.stream()
                                    .filter(item -> item.getStoreStatus() == SpuEnum.UP.getCode())
                                    .toList();
                            if (chooseMany== SpuEnum.MULTIPLE_SELECTION.getCode()){
                                if (ObjectUtil.isEmpty(filteredListSingle)){
                                    up = false;
                                }
                            }else {
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

    private Boolean getWXStoreBoolean(CommodityStoreSpu storePackage, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> primitiveIds, Long storeId) {


        if (storePackage.getCommodityStoreSpuAppletStatus().equals(SpuEnum.UP.getCode())) {
            //如果商品本来就是上架不用管
            return false;
        }else {
            //模拟子品上架
            getStoreCommodityGroupSingle(primitiveIds,commodityStoreSingles1,chooseView,SpuEnum.UP.getCode());
            // 1.固定搭配 2.分组可选套餐
            if (storePackage.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityStoreSingle> filteredList = commodityStoreSingles1.stream()
                        .filter(item -> item.getWxStatus().equals(SpuEnum.DOWN.getCode()) )
                        .filter(item -> storeId.equals(item.getStoreId()))
                        //.filter(item -> Optional.ofNullable(item.getCommodityId()).map(primitiveIds::contains).orElse(false))
                        .toList();
                return CollectionUtils.isEmpty(filteredList);
            }else {
               //分组可选
                Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles1.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
                boolean up = true;
                for (CommodityStoreGroup commodityStoreGroup : commodityStoreGroups1) {
                    // 加价组不参与套餐上架判定
                    if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
                        continue;
                    }
                    List<CommodityStoreSingle> commodityStoreSingles = singleMap.get(commodityStoreGroup.getCommodityStoreGroupId());
                    if (CollectionUtils.isEmpty(commodityStoreSingles)) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(up)) {
                        //1 可选 2固定
                        if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.FIXED.getCode())) {
                            //分组内固定
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item -> item.getWxStatus().equals(SpuEnum.UP.getCode()) )
                                    .filter(item -> storeId.equals(item.getStoreId()))
                                    //.filter(item -> Optional.ofNullable(item.getCommodityId()).map(primitiveIds::contains).orElse(false))
                                    .toList();
                            //e 判断
                            up = filteredList.size() == commodityStoreSingles.size();
                        }else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item ->item.getRequiredChoose()==SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item ->item.getWxStatus() == SpuEnum.DOWN.getCode())
                                    .toList();
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                                up = false;
                                break;
                            }


                            //同一商品是否可选多份 1 是 0否
                            Integer chooseMany = commodityStoreGroup.getChooseMany();
                            //必选数量
                            Integer choose = commodityStoreGroup.getCommodityStoreGroupChoose();
                            List<CommodityStoreSingle> filteredListSingle = commodityStoreSingles.stream()
                                    .filter(item -> item.getWxStatus() == SpuEnum.UP.getCode())
                                    .toList();
                            if (chooseMany== SpuEnum.MULTIPLE_SELECTION.getCode()){
                                if (ObjectUtil.isEmpty(filteredListSingle)){
                                    up = false;
                                }
                            }else {
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
     * 模拟给子品上架或下架
     * @param primitiveIds
     * @param chooseView
     * @return
     */
    private static void getStoreCommodityGroupSingle(List<Long> primitiveIds,List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView,Integer upOrDown) {

        if (ObjectUtil.isNotEmpty(primitiveIds)) {
            commodityStoreSingles1.stream()
                    .filter(cgs -> cgs.getCommodityId() != null && primitiveIds.contains(cgs.getCommodityId()))
                    .forEach(cgs -> {
                        if (chooseView == SpuEnum.WX.getCode()) {
                            cgs.setWxStatus(upOrDown);
                        } else {
                            cgs.setStoreStatus(upOrDown);
                        }
                    });

        }

    }

    private List<CommodityStoreSpu> getStorePackagesByIds(List<Long> storePackageIDs) {
        LambdaQueryWrapper<CommodityStoreSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, storePackageIDs);
        return commodityStoreSpuMapper.selectList(wrapper);

    }

    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_UP_SUB_TYPE, bizNo = "{{#byId.commodityStoreSpuId}}", success = COMMODITY_STORE_SPU_UP_SUCCESS)
    public void singleUpConfirm(StoreSingleUpConfirmReqVO singleUpConfirmReqVO) {

        Long storeId = singleUpConfirmReqVO.getStoreId();

        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();


        RLock fairLock = redissonClient.getLock(keyTol + storeId);

        try {

            boolean lockAcquired = fairLock.tryLock();


            if (!lockAcquired) {
                throw new ServiceException(STORE_UP_OR_DOWN);
            }


            Long commodityStoreSpuId = singleUpConfirmReqVO.getCommodityStoreSpuId();


            LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commodityStoreSpuLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuId,commodityStoreSpuId);
            CommodityStoreSpu byId = commodityStoreSpuMapper.selectOne(commodityStoreSpuLambdaQueryWrapper);
            //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
            if (ObjectUtil.isEmpty(byId)){
                throw new ServiceException(SPU_IS_DEL);
            }
            Integer chooseView = singleUpConfirmReqVO.getChooseView();


           // CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);

            List<Long> primitiveSpuIds = new ArrayList<>();
            primitiveSpuIds.add(byId.getCommodityStorePrimitiveSpuId());
            List<Long> updatePackageIds = commodityStoreSingleService.updateUpStatusBySpuIdNew(storeId, primitiveSpuIds, chooseView,SpuEnum.UP.getCode());

            List<Long> spuIds = new ArrayList<>();
            spuIds.add(commodityStoreSpuId);
            if (ObjectUtil.isNotEmpty(singleUpConfirmReqVO.getPackageIds())){
                spuIds.addAll(singleUpConfirmReqVO.getPackageIds());
            }
            LambdaUpdateWrapper<CommodityStoreSpu> storeSpuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            storeSpuLambdaUpdateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, spuIds);
            storeSpuLambdaUpdateWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
            if (chooseView.equals(SpuEnum.WX.getCode())) {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, SpuEnum.UP.getCode());
            }else {
                storeSpuLambdaUpdateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, SpuEnum.UP.getCode());
            }
            commodityStoreSpuMapper.update(storeSpuLambdaUpdateWrapper);
            if (ObjectUtil.isNotEmpty(updatePackageIds)) {
                for (Long updatePackageId : updatePackageIds) {
                    if (!spuIds.contains(updatePackageId)) {
                        spuIds.add(updatePackageId);
                    }
                }
            }
            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(spuIds);
            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }

            List<Long> packageIds = singleUpConfirmReqVO.getPackageIds();
            if (ObjectUtil.isNotEmpty(packageIds)){
                LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuWrapper = new LambdaQueryWrapper<>();
                commodityStoreSpuWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
                List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(commodityStoreSpuWrapper);
                List<String> packageNames = new ArrayList<>();
                for (CommodityStoreSpu commodityStoreSpu : commodityStoreSpus) {
                    packageNames.add(commodityStoreSpu.getCommodityStoreSpuName());
                }
                LogRecordContext.putVariable("packageNames", packageNames);

            }





            LogRecordContext.putVariable("byId", byId);
            LogRecordContext.putVariable("storeName", storeName);
        } finally{
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!singleUpConfirmReqVO.getIsApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
            }

        }



    }

    @Override
    public StoreBatchDownSelectRespVO batchDownSelect(StoreBatchDownSelectReqVO storeBatchDownSelectReqVO) {
        if (ObjectUtil.isEmpty(storeBatchDownSelectReqVO.getSingleIds()) && ObjectUtil.isEmpty(storeBatchDownSelectReqVO.getPackageIds())){
            throw new ServiceException(SPU_NOT_VALUE);
        }


        StoreBatchDownSelectRespVO storeBatchDownSelectRespVO = new StoreBatchDownSelectRespVO();
        List<Long> packageIds = storeBatchDownSelectReqVO.getPackageIds();
        Long storeId = storeBatchDownSelectReqVO.getStoreId();
        List<Long> singleIds = storeBatchDownSelectReqVO.getSingleIds();
        Integer chooseView = storeBatchDownSelectReqVO.getChooseView();
        storeBatchDownSelectRespVO.setStoreId(storeId);
        storeBatchDownSelectRespVO.setSingleIds(singleIds);
        storeBatchDownSelectRespVO.setChooseView(chooseView);
        storeBatchDownSelectRespVO.setPackageIds(packageIds);

        List<Long> all = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(singleIds)){
            all.addAll(singleIds);
        }
        if (ObjectUtil.isNotEmpty(packageIds)){
            all.addAll(packageIds);
        }
        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId,all);
        List<CommodityStoreSpu> commodityStoreSpus1 = commodityStoreSpuMapper.selectList(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (commodityStoreSpus1.size() != all.size()){
            throw new ServiceException(SPUS_IS_DEL);
        }




        if (ObjectUtil.isNotEmpty(singleIds)){
            //查询需要下架的套餐
            List<CommodityStoreSpu> commodityStoreSpus = findNeedDownPackage(singleIds, chooseView,storeId);

            if (ObjectUtil.isNotEmpty(commodityStoreSpus)){
                List<CommodityStoreSpuRespVo> storeSpu= commodityStoreSpus.stream()
                        //过滤选择的packageIds
                        .filter(cs ->ObjectUtil.isEmpty(packageIds) || !packageIds.contains(cs.getCommodityStoreSpuId()))
                        .map(this::convertToQueryReqVo)
                        .collect(Collectors.toList());
                storeBatchDownSelectRespVO.setStoreSpu(storeSpu);
            }


        }




        return storeBatchDownSelectRespVO;
    }

    private List<CommodityStoreSpu> findNeedDownPackage(List<Long> singleIds, Integer chooseView,Long storeId) {
        List<CommodityStoreSpu> commodityStoreSpus = new ArrayList<>();
        //去找相关连单品
        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, singleIds);
        queryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
        List<CommodityStoreSpu> singleY = commodityStoreSpuMapper.selectList(queryWrapper);
        //找到单品的原始 Ids
        List<Long> singleYIds = singleY.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).distinct().toList();

        List<CommodityStoreSingle> singles = commodityStoreSingleService.selectSpuIds(singleYIds, storeId);
        if (ObjectUtil.isNotEmpty(singles)){
            //获得门店下的套餐 IDs
            List<Long> packageIDs = singles.stream().map(CommodityStoreSingle::getCommodityStoreSpuId).distinct().toList();
            //获得这些套餐
            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIDs);
            queryWrapper1.eq(CommodityStoreSpu::getStoreId, storeId);
            List<CommodityStoreSpu> packageSpu = commodityStoreSpuMapper.selectList(queryWrapper1);

            //获得所有分组
            List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectBySpuIds(packageIDs);
            //获得所有子品
            List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleService.selectByStoreSpuIds(packageIDs);

            Map<Long, List<CommodityStoreGroup>> groupMap = commodityStoreGroups.stream().collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
            Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreSpuId));

            for (CommodityStoreSpu commodityStoreSpu : packageSpu) {
                List<CommodityStoreGroup> commodityStoreGroups1 = groupMap.get(commodityStoreSpu.getCommodityStoreSpuId());
                List<CommodityStoreSingle> commodityStoreSingles1 = singleMap.get(commodityStoreSpu.getCommodityStoreSpuId());

                boolean b = getOrDown(commodityStoreSpu,commodityStoreGroups1,commodityStoreSingles1,chooseView,singleYIds,storeId);
                if (b){
                    commodityStoreSpus.add(commodityStoreSpu);
                }

            }
        }





        return commodityStoreSpus;
    }

    private boolean getOrDown(CommodityStoreSpu commodityStoreSpu, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> singleYIds, Long storeId) {

        if (chooseView.equals(SpuEnum.WX.getCode())){
            return getWxBooleanDown(commodityStoreSpu,commodityStoreGroups1,commodityStoreSingles1,chooseView,singleYIds,storeId);
        }else {
            return getStoreBooleanDown(commodityStoreSpu,commodityStoreGroups1,commodityStoreSingles1,chooseView,singleYIds,storeId);
        }

    }

    private boolean getStoreBooleanDown(CommodityStoreSpu commodityStoreSpu, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> singleYIds, Long storeId) {

        if (commodityStoreSpu.getCommodityStoreSpuMachineStatus().equals(SpuEnum.DOWN.getCode())) {
            return false;
        }else {
            //模拟子品下架
            getStoreCommodityGroupSingle(singleYIds,commodityStoreSingles1,chooseView,SpuEnum.DOWN.getCode());
            //1.固定搭配 2分组可选套餐
            if (commodityStoreSpu.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityStoreSingle> filteredList = commodityStoreSingles1.stream()
                        .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()))
                        .toList();
                //但凡有下架的，做下架处理
                return com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(filteredList);
            }else {
                //分组可选 只要有一个分组不满足下架 直接 return true
                Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles1.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
                //true 说明需要下架
                boolean b = false;
                for (CommodityStoreGroup commodityStoreGroup : commodityStoreGroups1) {
                    if (!b){
                        // 加价组不参与套餐下架判定
                        if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
                            continue;
                        }
                        List<CommodityStoreSingle> commodityStoreSingles = singleMap.get(commodityStoreGroup.getCommodityStoreGroupId());
                        if (CollectionUtils.isEmpty(commodityStoreSingles)) {
                            continue;
                        }

                        if (commodityStoreGroup.getCommodityStoreGroupAttribute() == SpuEnum.FIXED.getCode()) {
                            //分组固定
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.DOWN.getCode()))
                                    .toList();
                            //如果分组内但凡有下架的 就给套餐下架
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                                b= true;
                                break;
                            }
                        }else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityStoreSingle> list = commodityStoreSingles.stream()
                                    .filter(item -> item.getRequiredChoose() == SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item -> item.getStoreStatus() == SpuEnum.DOWN.getCode())
                                    .toList();

                            if (CollectionUtils.isNotEmpty(list)){
                                //如果筛选出来的必选品有一个是下架的 那么就需要下架
                                b=  true;
                                break;

                            }

                            //同一商品是否可以多选
                            Integer chooseMany = commodityStoreGroup.getChooseMany();
                            //必选数量
                            Integer choose = commodityStoreGroup.getCommodityStoreGroupChoose();
                            List<CommodityStoreSingle> list1 = commodityStoreSingles.stream()
                                    .filter(item -> item.getStoreStatus().equals(SpuEnum.UP.getCode()))
                                    .toList();
                            if (chooseMany == SpuEnum.MULTIPLE_SELECTION.getCode()){
                                b=  ObjectUtil.isEmpty(list1);
                            }else {
                                b=  choose > list1.size();
                            }

                        }
                    }

                }


                return  b;//所有分组都满足售卖条件 所以不予下架
            }
        }
    }

    private boolean getWxBooleanDown(CommodityStoreSpu commodityStoreSpu, List<CommodityStoreGroup> commodityStoreGroups1, List<CommodityStoreSingle> commodityStoreSingles1, Integer chooseView, List<Long> singleYIds, Long storeId) {
        if (commodityStoreSpu.getCommodityStoreSpuAppletStatus().equals(SpuEnum.DOWN.getCode())) {
            return false;
        }else {
            //模拟子品下架
            getStoreCommodityGroupSingle(singleYIds,commodityStoreSingles1,chooseView,SpuEnum.DOWN.getCode());
            //1.固定搭配 2分组可选套餐
            if (commodityStoreSpu.getSetmealType().equals(SpuEnum.COLLOCATION.getCode())){
                List<CommodityStoreSingle> filteredList = commodityStoreSingles1.stream()
                        .filter(item -> item.getWxStatus().equals(SpuEnum.DOWN.getCode()))
                        .toList();
                //但凡有下架的，做下架处理
                return com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(filteredList);
            }else {
                //分组可选 只要有一个分组不满足下架 直接 return true
                Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles1.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreGroupId));
                //true 说明需要下架
                boolean b = false;
                for (CommodityStoreGroup commodityStoreGroup : commodityStoreGroups1) {
                    if (!b){
                        // 加价组不参与套餐下架判定
                        if (commodityStoreGroup.getCommodityStoreGroupAttribute().equals(SpuEnum.SURCHARGE_GROUP.getCode())) {
                            continue;
                        }
                        List<CommodityStoreSingle> commodityStoreSingles = singleMap.get(commodityStoreGroup.getCommodityStoreGroupId());
                        if (CollectionUtils.isEmpty(commodityStoreSingles)) {
                            continue;
                        }

                        if (commodityStoreGroup.getCommodityStoreGroupAttribute() == SpuEnum.FIXED.getCode()) {
                            //分组固定
                            List<CommodityStoreSingle> filteredList = commodityStoreSingles.stream()
                                    .filter(item -> item.getWxStatus().equals(SpuEnum.DOWN.getCode()))
                                    .toList();
                            //如果分组内但凡有下架的 就给套餐下架
                            if (ObjectUtil.isNotEmpty(filteredList)) {
                                b =  true;
                                break;
                            }
                        }else {
                            //分组内可选
                            //筛选出必选品
                            List<CommodityStoreSingle> list = commodityStoreSingles.stream()
                                    .filter(item -> item.getRequiredChoose() == SpuEnum.AFFIRMATIVELY.getCode())
                                    .filter(item -> item.getWxStatus() == SpuEnum.DOWN.getCode())
                                    .toList();

                            if (CollectionUtils.isNotEmpty(list)){
                                //如果筛选出来的必选品有一个是下架的 那么就需要下架
                                b=  true;
                                break;

                            }

                            //同一商品是否可以多选
                            Integer chooseMany = commodityStoreGroup.getChooseMany();
                            //必选数量
                            Integer choose = commodityStoreGroup.getCommodityStoreGroupChoose();
                            List<CommodityStoreSingle> list1 = commodityStoreSingles.stream()
                                    .filter(item -> item.getWxStatus().equals(SpuEnum.UP.getCode()))
                                    .toList();
                            if (chooseMany == SpuEnum.MULTIPLE_SELECTION.getCode()){
                                b=  ObjectUtil.isEmpty(list1);
                            }else {
                                b=  choose > list1.size();
                            }

                        }
                    }

                }


                return  b;//所有分组都满足售卖条件 所以不予下架
            }
        }

    }

    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_BATCH_DOWN, bizNo = "{{#storeBatchDownSelectReqVO.storeId}}", success = COMMODITY_STORE_SPU_BATCH_DOWN)
    public void batchDownConfirm(StoreBatchDownSelectReqVO storeBatchDownSelectReqVO) {
        if (ObjectUtil.isEmpty(storeBatchDownSelectReqVO.getSingleIds()) && ObjectUtil.isEmpty(storeBatchDownSelectReqVO.getPackageIds())){
            throw new ServiceException(SPU_NOT_VALUE);
        }
        List<Long> packageIds = storeBatchDownSelectReqVO.getPackageIds();
        //校验套餐上架锁权限
        if (ObjectUtil.isNotEmpty(packageIds)){
            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
            List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);
            List<Long> originalIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

            checkShelfLockPermissions(originalIds);
        }


        Long storeId = storeBatchDownSelectReqVO.getStoreId();


        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();

        List<Long> singleIds = storeBatchDownSelectReqVO.getSingleIds();
        Integer chooseView = storeBatchDownSelectReqVO.getChooseView();
        List<Long> allSpuIds = new ArrayList<>();
        RLock fairLock = redissonClient.getLock(keyTol + storeId);

        List<Long> all = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(singleIds)){
            all.addAll(singleIds);
        }
        if (ObjectUtil.isNotEmpty(packageIds)){
            all.addAll(packageIds);
        }
        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId,all);
        List<CommodityStoreSpu> commodityStoreSpus1 = commodityStoreSpuMapper.selectList(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (commodityStoreSpus1.size() != all.size()){
            throw new ServiceException(SPUS_IS_DEL);
        }
        try {

            boolean lockAcquired = fairLock.tryLock();
            if (!lockAcquired) {
                throw new ServiceException(STORE_UP_OR_DOWN);
            }
            //相关的套餐要做缓存处理
            List<Long> longs = new ArrayList<>();
            if (ObjectUtil.isNotEmpty(singleIds)){

               LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
               queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, singleIds);
               List<CommodityStoreSpu> list = commodityStoreSpuMapper.selectList(queryWrapper);
                List<Long> primitiveSpuIds = list.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

                longs = commodityStoreSingleService.updateUpStatusBySpuIdNew(storeId, primitiveSpuIds, chooseView, SpuEnum.DOWN.getCode());
                allSpuIds.addAll(singleIds);
            }
            if (ObjectUtil.isNotEmpty(packageIds)){
                allSpuIds.addAll(packageIds);
            }

            if (ObjectUtil.isNotEmpty(allSpuIds)){
                LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, allSpuIds);
                if (chooseView == SpuEnum.WX.getCode()){
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, SpuEnum.DOWN.getCode());
                }else {
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, SpuEnum.DOWN.getCode());
                }
                commodityStoreSpuMapper.update(updateWrapper);

            }
            allSpuIds.addAll(longs);
            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(allSpuIds);
            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }

            LogRecordContext.putVariable("storeBatchDownSelectReqVO", storeBatchDownSelectReqVO);
            LogRecordContext.putVariable("storeName", storeName);


        }finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!storeBatchDownSelectReqVO.isApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
            }
        }
    }

    private CommodityStoreSpuRespVo convertToQueryReqVo(CommodityStoreSpu commodityStoreSpu) {
        CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
        BeanUtils.copyProperties(commodityStoreSpu, commodityStoreSpuRespVo);
        if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())){
            commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
        }
        return commodityStoreSpuRespVo;
    }

    /**
     * 此接口是同步商品时 做单品与套餐状态同步功能，其余功能不得调用
     * @param storeId
     */
    @Override
    public void doSingleDown(Long storeId) {


        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
        queryWrapper.eq(CommodityStoreSpu::getCommodityStoreSpuIsSingle, true);
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);


        Map<Long, List<CommodityStoreSpu>> isSingleMap = commodityStoreSpus.stream().collect(Collectors.groupingBy(CommodityStoreSpu::getCommodityStorePrimitiveSpuId));

        Set<Long> allSpuIds = new HashSet<>();
        if (ObjectUtil.isNotEmpty(commodityStoreSpus)){
            List<CommodityStoreSpu> wxDown = commodityStoreSpus.stream()
                    .filter(item -> item.getCommodityStoreSpuAppletStatus().equals(SpuEnum.DOWN.getCode()))
                    .toList();
            List<Long> priWxDownIds = wxDown.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

            List<CommodityStoreSpu> storeDown = commodityStoreSpus.stream()
                    .filter(item -> item.getCommodityStoreSpuMachineStatus().equals(SpuEnum.DOWN.getCode()))
                    .toList();
            List<Long> priStoreDownIds = storeDown.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

            //拿到门店单品的原始 Id
            List<Long> singleIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();
            //反查子品
            List<CommodityStoreSingle> singles1 = commodityStoreSingleService.selectSpuIds(singleIds, storeId);
            //将子品与单品的状态统一
            for (CommodityStoreSingle single : singles1) {
                List<CommodityStoreSpu> commodityStoreSpus1 = isSingleMap.get(single.getCommodityId());
                if (ObjectUtil.isNotEmpty(commodityStoreSpus1)) {
                    CommodityStoreSpu commodityStoreSpu = commodityStoreSpus1.get(0);
                    single.setWxStatus(commodityStoreSpu.getCommodityStoreSpuAppletStatus());
                    single.setStoreStatus(commodityStoreSpu.getCommodityStoreSpuMachineStatus());
                }

            }
            commodityStoreSingleService.updateBatchById(singles1);
            List<CommodityStoreSingle> singles = commodityStoreSingleService.selectSpuIds(singleIds, storeId);

            if (ObjectUtil.isNotEmpty(singles)){
                //子品反查套餐 Ids
                List<Long> packageIds = singles.stream().map(CommodityStoreSingle::getCommodityStoreSpuId).toList();
                allSpuIds.addAll(packageIds);
                //查套餐
                LambdaQueryWrapper<CommodityStoreSpu> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(CommodityStoreSpu::getStoreId, storeId);
                queryWrapper1.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
                List<CommodityStoreSpu> packages = commodityStoreSpuMapper.selectList(queryWrapper1);
                //查分组
                List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectBySpuIds(packageIds);
                //查子品
                List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleService.selectByStoreSpuIds(packageIds);

                Map<Long, List<CommodityStoreGroup>> groupMap = commodityStoreGroups.stream().collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
                Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreSpuId));
                List<Integer> twoStatus = new ArrayList<>();
                twoStatus.add(SpuEnum.WX.getCode());
                twoStatus.add(SpuEnum.DCJ.getCode());
                List<Long> wxDownIds = new ArrayList<>();
                List<Long> storeDownIds = new ArrayList<>();
                for (CommodityStoreSpu aPackage : packages) {
                    List<CommodityStoreGroup> commodityStoreGroups1 = groupMap.get(aPackage.getCommodityStoreSpuId());
                    List<CommodityStoreSingle> commodityStoreSingles1 = singleMap.get(aPackage.getCommodityStoreSpuId());
                    for (Integer status : twoStatus) {
                        if (status == SpuEnum.WX.getCode()) {
                            boolean orDown = getOrDown(aPackage, commodityStoreGroups1, commodityStoreSingles1, status, priWxDownIds, storeId);
                            if (orDown) {
                                wxDownIds.add(aPackage.getCommodityStoreSpuId());
                            }
                        }else {
                            boolean orDown = getOrDown(aPackage, commodityStoreGroups1, commodityStoreSingles1, status, priStoreDownIds, storeId);
                            if (orDown) {
                                storeDownIds.add(aPackage.getCommodityStoreSpuId());
                            }
                        }

                    }
                }

                //处理子品下架
                //子品微信下架
/*
                if (ObjectUtil.isNotEmpty(priWxDownIds)){
                    commodityStoreSingleService.updateUpStatusBySpuIdNew(storeId, priWxDownIds, SpuEnum.WX.getCode(), SpuEnum.DOWN.getCode());
                }
                if (ObjectUtil.isNotEmpty(priStoreDownIds)){
                    //子品dcj下架
                    commodityStoreSingleService.updateUpStatusBySpuIdNew(storeId, priStoreDownIds, SpuEnum.DCJ.getCode(), SpuEnum.DOWN.getCode());
                }
*/


                //处理套餐下架
                if (ObjectUtil.isNotEmpty(wxDownIds)){
                    LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
                    updateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, wxDownIds);
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus, SpuEnum.DOWN.getCode());
                    commodityStoreSpuMapper.update(updateWrapper);
                    allSpuIds.addAll(wxDownIds);
                }
                if (ObjectUtil.isNotEmpty(storeDownIds)){
                    LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
                    updateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, storeDownIds);
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus, SpuEnum.DOWN.getCode());
                    commodityStoreSpuMapper.update(updateWrapper);
                    allSpuIds.addAll(storeDownIds);
                }
            }

        }
        if (ObjectUtil.isNotEmpty(allSpuIds)){
            List<Long> allSpuIdList = new ArrayList<>(allSpuIds);
            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(allSpuIdList);
            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }
        }


    }


    @Override
    public StoreBatchUpSelectRespVO batchUpSelect(StoreBatchUpSelectReqVO storeBatchUpSelectReqVO) {
        StoreBatchUpSelectRespVO respVO = new StoreBatchUpSelectRespVO();
        Integer chooseView = storeBatchUpSelectReqVO.getChooseView();
        respVO.setChooseView(chooseView);
        List<Long> singleIds = storeBatchUpSelectReqVO.getSingleIds();
        respVO.setSingleIds(singleIds);
        List<Long> packageIds = storeBatchUpSelectReqVO.getPackageIds();
        Long storeId = storeBatchUpSelectReqVO.getStoreId();

        List<Long> all = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(singleIds)){
            all.addAll(singleIds);
        }
        if (ObjectUtil.isNotEmpty(packageIds)){
            all.addAll(packageIds);
        }
        LambdaQueryWrapper<CommodityStoreSpu> commodityStoreSpuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityStoreSpuLambdaQueryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId,all);
        List<CommodityStoreSpu> commodityStoreSpusDel = commodityStoreSpuMapper.selectList(commodityStoreSpuLambdaQueryWrapper);
        //CommodityStoreSpu byId = commodityStoreSpuMapper.selectById(commodityStoreSpuId);
        if (commodityStoreSpusDel.size() != all.size()){
            throw new ServiceException(SPUS_IS_DEL);
        }



        //拿到单品所指的子品集
        List<CommodityStoreSingle> commodityStoreSingles = new ArrayList<>();

        List<Long> singleIDs =new ArrayList<>();
        if (ObjectUtil.isNotEmpty(singleIds)){
            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, singleIds);
            List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);
            singleIDs = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

            commodityStoreSingles = commodityStoreSingleService.selectSpuIds(singleIDs,storeId);



        }
        List<Long> allPackageIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(commodityStoreSingles)){
            List<Long> singleForPackageIds = new ArrayList<>(commodityStoreSingles.stream().map(CommodityStoreSingle::getCommodityStoreSpuId).toList());
            allPackageIds.addAll(singleForPackageIds);
        }

        if (ObjectUtil.isNotEmpty(packageIds)){
            allPackageIds.addAll(packageIds);
        }
        //找到本次打勾相关的所有套餐
        if (ObjectUtil.isNotEmpty(allPackageIds)){

            LambdaQueryWrapper<CommodityStoreSpu> queryWrapperDown = new LambdaQueryWrapper<>();
            queryWrapperDown.in(CommodityStoreSpu::getCommodityStoreSpuId,allPackageIds);
            if (chooseView.equals(SpuEnum.WX.getCode())){
                queryWrapperDown.eq(CommodityStoreSpu::getCommodityStoreSpuAppletStatus,SpuEnum.DOWN.getCode());
            }else {
                queryWrapperDown.eq(CommodityStoreSpu::getCommodityStoreSpuMachineStatus,SpuEnum.DOWN.getCode());
            }
            List<CommodityStoreSpu> commodityStoreSpuses = commodityStoreSpuMapper.selectList(queryWrapperDown);
            if (ObjectUtil.isNotEmpty(commodityStoreSpuses)){
                List<Long> allDownIds = commodityStoreSpuses.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();

                packageIds = packageIds.stream()
                        .filter(allDownIds::contains)
                        .toList();


                List<CommodityStoreSpu> commodityStoreSpus =  findNeedDownPackageByPackageIds(allDownIds,singleIDs,chooseView,storeId);

                if (ObjectUtil.isEmpty(commodityStoreSpus)){
                    if (ObjectUtil.isNotEmpty(packageIds)){
                        List<CommodityStoreSpuRespVo>  packageDown = new ArrayList<>();
                        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
                        List<CommodityStoreSpu> commodityStoreSpus1 = commodityStoreSpuMapper.selectList(queryWrapper);
                        for (CommodityStoreSpu commodityStoreSpu : commodityStoreSpus1) {
                            CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
                            BeanUtils.copyProperties(commodityStoreSpu, commodityStoreSpuRespVo);
                            if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())){
                                commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                            }
                            packageDown.add(commodityStoreSpuRespVo);
                        }
                        respVO.setPackageDown(packageDown);
                    }
                }else {
                    if (ObjectUtil.isNotEmpty(packageIds)){
                        List<Long> commodityIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStoreSpuId).toList();
                        List<Long> difference = packageIds.stream()
                                .filter(id -> !commodityIds.contains(id)) // 过滤掉在commodityIds中存在的元素
                                .toList(); // 收集结果到新列表
                        //找到不满足上架的套餐 Id
                        if (ObjectUtil.isNotEmpty(difference)){
                            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, difference);
                            List<CommodityStoreSpu> commodityStoreSpus1 = commodityStoreSpuMapper.selectList(queryWrapper);
                            List<CommodityStoreSpuRespVo>  packageDown = new ArrayList<>();
                            for (CommodityStoreSpu commodityStoreSpu : commodityStoreSpus1) {
                                CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
                                BeanUtils.copyProperties(commodityStoreSpu, commodityStoreSpuRespVo);
                                if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())){
                                    commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                                }
                                packageDown.add(commodityStoreSpuRespVo);
                            }
                            respVO.setPackageDown(packageDown);
                        }

                        //找到未勾选但满足上架的套餐
                        List<Long> finalPackageIds = packageIds;
                        List<Long> difference2 = commodityIds.stream()
                                .filter(id -> !finalPackageIds.contains(id)) // 过滤掉在packageIds中存在的元素
                                .toList(); // 收集结果到新列表
                        if (ObjectUtil.isNotEmpty(difference2)){
                            List<CommodityStoreSpuRespVo> packageUp = new ArrayList<>();
                            for (CommodityStoreSpu commodityStoreSpu : commodityStoreSpus) {
                                if (difference2.contains(commodityStoreSpu.getCommodityStoreSpuId())){
                                    CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
                                    BeanUtils.copyProperties(commodityStoreSpu, commodityStoreSpuRespVo);
                                    if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())){
                                        commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                                    }
                                    packageUp.add(commodityStoreSpuRespVo);
                                }
                            }
                            respVO.setPackageUp(packageUp);
                        }

                        //找到勾选了，并且满足上架的 ID
                        List<Long> difference3 = packageIds.stream()
                                .filter(commodityIds::contains) // 过滤出在commodityIds中存在的元素
                                .toList();
                        respVO.setPackageIds(difference3);

                    }else {
                        List<CommodityStoreSpuRespVo> packageUp = new ArrayList<>();
                        for (CommodityStoreSpu commodityStoreSpu : commodityStoreSpus) {
                            CommodityStoreSpuRespVo commodityStoreSpuRespVo = new CommodityStoreSpuRespVo();
                            BeanUtils.copyProperties(commodityStoreSpu, commodityStoreSpuRespVo);
                            if (ObjectUtil.isNotEmpty(commodityStoreSpu.getImageUrl())){
                                commodityStoreSpuRespVo.setImageUrlVO(ConvertUtil.convertStringToListS(commodityStoreSpu.getImageUrl()));
                            }
                            packageUp.add(commodityStoreSpuRespVo);
                        }
                        respVO.setPackageUp(packageUp);
                    }
                }
            }









        }


        return respVO;
    }

    private List<CommodityStoreSpu> findNeedDownPackageByPackageIds(List<Long> allPackageIds, List<Long> singleIDs, Integer chooseView,Long storeId) {
        List<CommodityStoreSpu> commoditySpuListReturn = new ArrayList<>();
        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, allPackageIds);
        List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);

        List<CommodityStoreGroup> commodityStoreGroups = commodityStoreGroupService.selectBySpuIds(allPackageIds);

        List<CommodityStoreSingle> commodityStoreSingles = commodityStoreSingleService.selectByStoreSpuIds(allPackageIds);

        if (ObjectUtil.isNotEmpty(singleIDs)){
            //模拟子品上架
            getStoreCommodityGroupSingle(singleIDs,commodityStoreSingles,chooseView,SpuEnum.UP.getCode());
        }
        Map<Long, List<CommodityStoreGroup>> groupMap = commodityStoreGroups.stream().collect(Collectors.groupingBy(CommodityStoreGroup::getCommodityStoreSpuId));
        Map<Long, List<CommodityStoreSingle>> singleMap = commodityStoreSingles.stream().collect(Collectors.groupingBy(CommodityStoreSingle::getCommodityStoreSpuId));
        for (CommodityStoreSpu storeSpus : commodityStoreSpus) {
            List<CommodityStoreGroup> commodityStoreGroups1 = groupMap.get(storeSpus.getCommodityStoreSpuId());
            List<CommodityStoreSingle> commodityStoreSingles1 = singleMap.get(storeSpus.getCommodityStoreSpuId());
            // 批量上架场景：若本次勾选单品仅命中加价组，则不提示该套餐可上架。
            if (ObjectUtil.isNotEmpty(singleIDs)
                && !triggeredByNonSurchargeStoreGroup(commodityStoreGroups1, commodityStoreSingles1, singleIDs, storeId)) {
                continue;
            }
            Boolean orUp = getOrUp(storeSpus, commodityStoreGroups1, commodityStoreSingles1, chooseView, singleIDs, storeId);
            if (orUp){
                commoditySpuListReturn.add(storeSpus);
            }
        }


        return commoditySpuListReturn;
    }


    @Override
    @LogRecord(type = COMMODITY_STORE_TYPE, subType = COMMODITY_STORE_SPU_BATCH_UP, bizNo = "{{#storeBatchUpSelectReqVO.storeId}}", success = COMMODITY_STORE_SPU_BATCH_UP)
    public void batchUpConfirm(StoreBatchUpSelectReqVO storeBatchUpSelectReqVO) {
        if (ObjectUtil.isEmpty(storeBatchUpSelectReqVO.getSingleIds()) && ObjectUtil.isEmpty(storeBatchUpSelectReqVO.getPackageIds())){
            throw new ServiceException(SPU_NOT_VALUE);
        }
        Long storeId = storeBatchUpSelectReqVO.getStoreId();

        CommonResult<PageResult<StoreInfoDTO>> storeInfoByStoreIds = storeInfoApi.getStoreInfoByStoreIds(List.of(storeId), new PageParam());
        StoreInfoDTO storeInfoDTO = storeInfoByStoreIds.getData().getList().get(0);
        String storeName = storeInfoDTO.getStoreName();



        Integer chooseView = storeBatchUpSelectReqVO.getChooseView();
        List<Long> singleIds = storeBatchUpSelectReqVO.getSingleIds();
        List<Long> packageIds = storeBatchUpSelectReqVO.getPackageIds();
        //先判断是否有上架锁权限
        if (ObjectUtil.isNotEmpty(packageIds)){
            LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, packageIds);
            List<CommodityStoreSpu> commodityStoreSpus = commodityStoreSpuMapper.selectList(queryWrapper);
            List<Long> originalIds = commodityStoreSpus.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();

            checkShelfLockPermissions(originalIds);
        }




        List<Long> allSpuIds = new ArrayList<>();

        RLock fairLock = redissonClient.getLock(keyTol + storeId);


        try {

            boolean lockAcquired = fairLock.tryLock();
            if (!lockAcquired) {
                throw new ServiceException(STORE_UP_OR_DOWN);
            }

            List<Long> longs = new ArrayList<>();

            //先把子品上架
            if (ObjectUtil.isNotEmpty(singleIds)){

                LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, singleIds);
                List<CommodityStoreSpu> list = commodityStoreSpuMapper.selectList(queryWrapper);
                List<Long> primitiveSpuIds = list.stream().map(CommodityStoreSpu::getCommodityStorePrimitiveSpuId).toList();
                longs = commodityStoreSingleService.updateUpStatusBySpuIdNew(storeId, primitiveSpuIds, chooseView, SpuEnum.UP.getCode());
                allSpuIds.addAll(singleIds);
            }
            if (ObjectUtil.isNotEmpty(packageIds)){
                allSpuIds.addAll(packageIds);
            }
            //再把相应商品上架
            if (ObjectUtil.isNotEmpty(allSpuIds)){
                LambdaUpdateWrapper<CommodityStoreSpu> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.in(CommodityStoreSpu::getCommodityStoreSpuId, allSpuIds);
                if (chooseView.equals(SpuEnum.WX.getCode())){
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuAppletStatus,SpuEnum.UP.getCode());
                }else {
                    updateWrapper.set(CommodityStoreSpu::getCommodityStoreSpuMachineStatus,SpuEnum.UP.getCode());
                }
                commodityStoreSpuMapper.update(updateWrapper);
            }
            if (ObjectUtil.isNotEmpty(longs)){
                allSpuIds.addAll(longs);
            }

            List<SpuDto> spuDtos = coverCommodityStoreSpuListToSpuDtoList(allSpuIds);
            for (SpuDto spuDto : spuDtos) {
                storeRedisDao.saveOrUpdateProduct(spuDto.getStoreId(), spuDto.getCategoryId(), spuDto.getSpuId(), spuDto);
            }

            LogRecordContext.putVariable("storeBatchUpSelectReqVO", storeBatchUpSelectReqVO);

            LogRecordContext.putVariable("storeName", storeName);

        } finally {
            if (ObjectUtil.isNotNull(fairLock) && fairLock.isLocked() && fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
            if (!storeBatchUpSelectReqVO.isApp()){
                rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", "updateCommodity");
            }
        }

    }
}
