package com.htyoudao.youdao.module.commodity.service.sync;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.COMMODITY_NO_PRODUCT_SELECTED;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TEMP_CATE_LIST_CANNOT_BE_EMPTY;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TEMP_SKU_LIST_CANNOT_BE_EMPTY;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.TEMP_SPU_LIST_CANNOT_BE_EMPTY;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.util.MyBatisUtils;
import com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.commodity.enums.SyncStatus;
import com.htyoudao.youdao.module.commodity.constant.CommodityConstant;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityBaseToStoreSyncReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncPageReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncStoreReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommodityTemplateToStoreSyncReqVO;
import com.htyoudao.youdao.module.commodity.convert.CommodityConvertor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySyncTask;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplate;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityCategoryMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySpusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySyncTaskMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateMapper;
import com.htyoudao.youdao.module.commodity.dal.redis.CommodityStoreRedisDao;
import com.htyoudao.youdao.module.commodity.service.group.ICommoditySetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.single.ICommodityGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.sku.ICommoditySkusService;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncCategoryDTO;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncSetMealDTO;
import com.htyoudao.youdao.module.commodity.service.sync.dto.CommoditySyncSpuDTO;
import com.htyoudao.youdao.module.commodity.service.templateCategory.ICommodityTemplateCategoryService;
import com.htyoudao.youdao.module.commodity.service.templateGroup.ICommodityTemplateSetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.templateSingle.ICommodityTemplateGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.templateSku.ICommodityTemplateSkusService;
import com.htyoudao.youdao.module.commodity.service.templateSpu.ICommodityTemplateSpusService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Slf4j
@Service
public class SyncTaskServiceImpl implements CommoditySyncTaskService {

    @Resource
    private CommoditySyncTaskMapper taskMapper;
    @Resource
    private ThreadPoolExecutor syncExecutor;
    @Resource
    private CommoditySpusMapper spuMapper;
    @Resource
    private ICommoditySkusService skusService;
    @Resource
    private CommodityCategoryMapper categoryMapper;
    @Resource
    private ICommoditySetmealGroupService groupService;
    @Autowired
    private ICommodityGroupSingleService singleService;
    @Resource
    private CommodityTemplateMapper commodityTemplateMapper;
    @Resource
    private ICommodityTemplateCategoryService templateCategoryService;
    @Resource
    private ICommodityTemplateSpusService templateSpusService;
    @Resource
    private ICommodityTemplateSkusService templateSkusService;
    @Resource
    private ICommodityTemplateSetmealGroupService templateSetmealGroupService;
    @Resource
    private ICommodityTemplateGroupSingleService templateGroupSingleService;
    @Resource
    private ICommodityStoreSpuService storeSpuService;
    @Resource
    private CommodityStoreRedisDao storeRedisDao;
    @Resource
    private ICommodityTageService commodityTageService;


    /**
     * 同步连锁商品到门店接口
     *
     * @param syncVO
     * @return
     */
    @Override
    public String baseToStore(CommodityBaseToStoreSyncReqVO syncVO) {

        List<CommoditySyncCategoryDTO> syncData = buildSyncData(syncVO);

        String params = syncData.stream()
            .flatMap(category -> category.getSpuList().stream())
            .map(spuDTO -> spuDTO.getSpu().getCommodityStoreSpuName())
            .filter(name -> !name.isEmpty())
            .collect(Collectors.joining(","));

        return createBatchTasks(syncVO.getStoreIds(), params, syncData);
    }


    /**
     * 同步模板商品到门店
     *
     * @param syncVO
     * @return
     */
    @Override
    public String templateToStore(CommodityTemplateToStoreSyncReqVO syncVO) {
        Long templateId = syncVO.getCommodityTemplateId();
        CommodityTemplate commodityTemplate = commodityTemplateMapper.selectById(templateId);
        List<CommoditySyncCategoryDTO> syncData = buildSyncData(commodityTemplate);

        String params = commodityTemplate.getCommodityTemplateName();

        return createBatchTasks(syncVO.getStoreIds(), params, syncData);
    }

    @Override
    public PageResult<CommoditySyncListRespVO> syncPageList(CommoditySyncPageReqVO reqVO) {
        IPage<T> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<CommoditySyncTask> queryWrapper = new LambdaQueryWrapperX<CommoditySyncTask>()
            .betweenIfPresent(CommoditySyncTask::getStartTime, reqVO.getStartTime())
            .betweenIfPresent(CommoditySyncTask::getFinishTime, reqVO.getEndTime())
            .likeIfPresent(CommoditySyncTask::getParams, reqVO.getParams())
            .inIfPresent(CommoditySyncTask::getStoreId, reqVO.getStoreIds())
            .eq(CommoditySyncTask::getBusinessId, BusinessContextHolder.getBusinessId())
            ;

        IPage<CommoditySyncListRespVO> iPage = taskMapper.syncPageList(mpPage, queryWrapper);
        for (CommoditySyncListRespVO record : iPage.getRecords()) {
            if (Objects.equals(record.getStatus(), "进行中")){
                record.setEndTime(null);
            }
        }
        return new PageResult<>(iPage.getRecords(), iPage.getTotal());
    }


    @Override
    public List<CommoditySyncStoreReqVO> listByBatchNo(String batchNo) {
        List<CommoditySyncTask> commoditySyncTasks = taskMapper.listByBatchNo(batchNo);

        if (CollectionUtil.isEmpty(commoditySyncTasks)) {
            return new ArrayList<>();
        }
        List<CommoditySyncStoreReqVO> storeVOS = new ArrayList<>();
        for (CommoditySyncTask commoditySyncTask : commoditySyncTasks) {
            CommoditySyncStoreReqVO storeVO = new CommoditySyncStoreReqVO();
            storeVO.setStoreId(commoditySyncTask.getStoreId());
            storeVO.setStoreName(commoditySyncTask.getStoreName());
            storeVO.setStatus(commoditySyncTask.getStatus());
            storeVOS.add(storeVO);
        }
        return storeVOS;
    }

    /**
     * 构建模板同步参数
     *
     * @param commodityTemplate
     * @return
     */
    private List<CommoditySyncCategoryDTO> buildSyncData(CommodityTemplate commodityTemplate) {
        //查询所有主数据
        Long templateId = commodityTemplate.getCommodityTemplateId();
        //查询模板
        if (ObjectUtil.isNull(commodityTemplate)) {
            throw exception(TEMP_CATE_LIST_CANNOT_BE_EMPTY);
        }

        //查询所有分类模板
        List<CommodityTemplateCategory> templateCategories = templateCategoryService.selectByTemplateId(templateId);
        if (CollectionUtil.isEmpty(templateCategories)) {
            throw exception(TEMP_CATE_LIST_CANNOT_BE_EMPTY);
        }

        //查询所有spu模板
        List<Long> categoryTemplateIdList = templateCategories.stream().map(CommodityTemplateCategory::getId)
            .collect(Collectors.toList());
        List<CommodityTemplateSpus> templateSpusList = templateSpusService.selectByTemplateCategoryIds(
            categoryTemplateIdList);
        if (CollectionUtil.isEmpty(templateSpusList)) {
            throw exception(TEMP_SPU_LIST_CANNOT_BE_EMPTY);
        }

        //查询所有sku
        List<Long> spuTemplateIdList = templateSpusList.stream()
            .map(CommodityTemplateSpus::getCommodityTemplateId).collect(Collectors.toList());
        List<CommodityTemplateSkus> commodityTemplateSkuList = templateSkusService.selectByTemplateSpuIds(
            spuTemplateIdList);
        if (CollectionUtil.isEmpty(commodityTemplateSkuList)) {
            throw exception(TEMP_SKU_LIST_CANNOT_BE_EMPTY);
        }

        //查询所有套餐
        List<CommodityTemplateSetmealGroup> templateSetMealGroupList = templateSetmealGroupService.selectByTemplateSpuIds(
            spuTemplateIdList);

        //查询所有套餐单品
        List<CommodityTemplateGroupSingle> templateGroupSingleList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(templateSetMealGroupList)) {
            List<Long> groupTemplateIdList = templateSetMealGroupList.stream().map(CommodityTemplateSetmealGroup::getId)
                .collect(Collectors.toList());
            templateGroupSingleList = templateGroupSingleService.selectByGroupTemplateIds(groupTemplateIdList);
        }

        //构建同步对象
        List<CommoditySyncCategoryDTO> syncCategoryDTOS = new ArrayList<>();
        for (CommodityTemplateCategory templateCategory : templateCategories) {

            CommoditySyncCategoryDTO categoryDTO = new CommoditySyncCategoryDTO();
            categoryDTO.setCategory(CommodityConvertor.convertToCommodityStoreCategory(templateCategory));

            Long categoryId = templateCategory.getId();
            //获取当前分类下需要同步spu
            List<CommodityTemplateSpus> spuList = templateSpusList.stream()
                .filter(s -> s.getCategoryTemplateId().equals(categoryId)).toList();

            List<CommoditySyncSpuDTO> syncSpuDTOS = new ArrayList<>();

            for (CommodityTemplateSpus spu : spuList) {
                CommoditySyncSpuDTO spuDTO = new CommoditySyncSpuDTO();

                //转换为门店spu对象
                CommodityStoreSpu storeSpu = CommodityConvertor.convertToStoreSpu(spu);

                //设置门店spu 模板属性
                storeSpu.setTemplateFlavor(commodityTemplate.getTemplateFlavor());
                storeSpu.setChoosePrice(commodityTemplate.getChoosePrice());

                //获取当前spu下需要同步sku
                List<CommodityStoreSku> storeSkus = commodityTemplateSkuList.stream()
                    .filter(s -> s.getCommodityTemplateId().equals(spu.getCommodityTemplateId()))
                    .map(CommodityConvertor::convertToStoreSku)
                    .toList();

                //获取当前spu下需要同步的套装分组
                List<CommodityTemplateSetmealGroup> list = templateSetMealGroupList.stream()
                    .filter(s -> s.getCommodityTemplateId().equals(spu.getCommodityTemplateId()))
                    .toList();

                List<CommoditySyncSetMealDTO> setMealList = new ArrayList<>();
                for (CommodityTemplateSetmealGroup setMealGroup : list) {
                    CommoditySyncSetMealDTO syncSetMealDTO = new CommoditySyncSetMealDTO();
                    syncSetMealDTO.setStoreGroup(CommodityConvertor.convertToStoreGroup(setMealGroup));

                    //获取当前spu下需要同步的套装单品
                    List<CommodityStoreSingle> storeSingles = templateGroupSingleList.stream()
                        .filter(s -> s.getTemplateGroupId().equals(setMealGroup.getId()))
                        .map(CommodityConvertor::convertToStoreSingle)
                        .toList();

                    syncSetMealDTO.setStoreSingles(storeSingles);
                    setMealList.add(syncSetMealDTO);
                }


                if (ObjectUtil.isNotEmpty(storeSpu.getTagIds())){
                    List<Long> longs = ConvertUtil.convertStringToListNew(storeSpu.getTagIds());
                    List<CommodityTag> commodityTags = commodityTageService.selectListByIds(longs);
                    Map<Long, CommodityTag> tagMap = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, e -> e));
                    List<CommodityTag> saveTags = new ArrayList<>();
                    for (Long aLong : longs) {
                        saveTags.add(tagMap.get(aLong));
                    }
                    spuDTO.setCommodityTags(saveTags);
                }

                spuDTO.setSetMealList(setMealList);
                spuDTO.setSpu(storeSpu);
                spuDTO.setStoreSkuList(storeSkus);

                syncSpuDTOS.add(spuDTO);
            }

            categoryDTO.setSpuList(syncSpuDTOS);
            syncCategoryDTOS.add(categoryDTO);
        }

        return syncCategoryDTOS;
    }

    /**
     * 构建同步参数
     *
     * @param syncVO
     * @return
     */
    private List<CommoditySyncCategoryDTO> buildSyncData(CommodityBaseToStoreSyncReqVO syncVO) {
        //获取需要同步的商品对象
        List<CommoditySpus> spuList = spuMapper.selectByIds(syncVO.getCommodityStoreSpuIds());

        if (CollectionUtils.isEmpty(spuList)) {
            log.error("spu not found,{}", syncVO.getCommodityStoreSpuIds());
            throw exception(COMMODITY_NO_PRODUCT_SELECTED);
        }

        //获取需要同步的商品sku对象
        List<Long> spuIds = spuList.stream().map(CommoditySpus::getCommodityId).toList();
        List<CommoditySkus> skuList = skusService.selectBySpuIds(spuIds);

        //获取商品的分类对象
        List<Long> categoryIds = spuList.stream().map(CommoditySpus::getCategoryId).distinct()
            .collect(Collectors.toList());
        List<CommodityCategory> commodityCategories = categoryMapper.selectByIds(categoryIds);

        //获取商品的套餐信息
        List<Long> setMealSpuIds = spuList.stream()
            .filter(s -> Objects.equals(s.getIsSingle(), CommodityConstant.SET_MEAL))
            .map(CommoditySpus::getCommodityId)
            .toList();

        List<CommoditySetmealGroup> setMealGroups = new ArrayList<>();
        List<CommodityGroupSingle> groupSingles = new ArrayList<>();

        if (!CollectionUtils.isEmpty(setMealSpuIds)) {
            for (Long setMealSpuId : setMealSpuIds) {
                List<CommoditySetmealGroup> spuSetMealGroups = groupService.selectBySpuId(
                    setMealSpuId);
                setMealGroups.addAll(spuSetMealGroups);
                List<Long> groupIds = spuSetMealGroups.stream().map(CommoditySetmealGroup::getGroupId).toList();
                List<CommodityGroupSingle> spuGroupSingles = singleService.selectByGroupIds(groupIds);
                groupSingles.addAll(spuGroupSingles);
            }
        }

        //构建同步对象
        List<CommoditySyncCategoryDTO> syncCategoryDTOS = new ArrayList<>();
        for (CommodityCategory category : commodityCategories) {

            CommoditySyncCategoryDTO categoryDTO = new CommoditySyncCategoryDTO();
            categoryDTO.setCategory(CommodityConvertor.convertToCommodityStoreCategory(category));

            Long categoryId = category.getId();
            //获取当前分类下需要同步spu
            List<CommoditySpus> syncSpuList = spuList.stream()
                .filter(s -> s.getCategoryId().equals(categoryId)).toList();

            List<CommoditySyncSpuDTO> syncSpuDTOS = new ArrayList<>();

            for (CommoditySpus spu : syncSpuList) {
                CommoditySyncSpuDTO spuDTO = new CommoditySyncSpuDTO();

                //转换为门店spu对象
                CommodityStoreSpu storeSpu = CommodityConvertor.convertToStoreSpu(spu);

                //设置门店spu 模板属性
                storeSpu.setTemplateFlavor(syncVO.getTemplateFlavor());
                storeSpu.setChoosePrice(syncVO.getChoosePrice());

                //获取当前spu下需要同步sku
                List<CommodityStoreSku> storeSkus = skuList.stream()
                    .filter(s -> s.getCommodityId().equals(spu.getCommodityId()))
                    .map(CommodityConvertor::convertToStoreSku)
                    .toList();

                //获取当前spu下需要同步的套装分组
                List<CommoditySetmealGroup> list = setMealGroups.stream()
                    .filter(s -> s.getCommodityId().equals(spu.getCommodityId()))
                    .toList();

                List<CommoditySyncSetMealDTO> setMealList = new ArrayList<>();
                for (CommoditySetmealGroup setMealGroup : list) {
                    CommoditySyncSetMealDTO syncSetMealDTO = new CommoditySyncSetMealDTO();
                    syncSetMealDTO.setStoreGroup(CommodityConvertor.convertToStoreGroup(setMealGroup));

                    //获取当前spu下需要同步的套装单品
                    List<CommodityStoreSingle> storeSingles = groupSingles.stream()
                        .filter(s -> s.getGroupId().equals(setMealGroup.getGroupId()))
                        .map(CommodityConvertor::convertToStoreSingle)
                        .toList();

                    syncSetMealDTO.setStoreSingles(storeSingles);
                    setMealList.add(syncSetMealDTO);
                }

                if (ObjectUtil.isNotEmpty(storeSpu.getTagIds())){
                    List<Long> longs = ConvertUtil.convertStringToListNew(storeSpu.getTagIds());
                    List<CommodityTag> commodityTags = commodityTageService.selectListByIds(longs);
                    Map<Long, CommodityTag> tagMap = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, e -> e));
                    List<CommodityTag> saveTags = new ArrayList<>();
                    for (Long aLong : longs) {
                        saveTags.add(tagMap.get(aLong));
                    }
                    spuDTO.setCommodityTags(saveTags);


                }

                spuDTO.setSetMealList(setMealList);
                spuDTO.setSpu(storeSpu);
                spuDTO.setStoreSkuList(storeSkus);

                syncSpuDTOS.add(spuDTO);
            }

            categoryDTO.setSpuList(syncSpuDTOS);
            syncCategoryDTOS.add(categoryDTO);
        }

        return syncCategoryDTOS;
    }


    /**
     * 批量创建同步任务
     *
     * @param storeIds 门店ID列表
     * @return 批次号
     */
    public String createBatchTasks(Map<Long, String> storeIds, String params, List<CommoditySyncCategoryDTO> syncData) {

        String batchNo = UUID.randomUUID().toString();
        Long businessId = BusinessContextHolder.getBusinessId();

        log.info("项目:{}开始创建同步任务,批次号:{}", businessId ,batchNo);

        storeIds.keySet().forEach(storeId -> {
            String storeName = storeIds.get(storeId);

            CommoditySyncTask task = createTask(storeId, storeName, params, batchNo, syncData);
            try {
                syncExecutor.execute(() -> processTask(businessId ,task, syncData));
            } catch (Exception e) {
                BusinessContextHolder.setBusinessId(businessId);
                log.error("create batch task error ", e);
                task.setErrorMessage("同步线程池已满,稍后提交任务");//第一次 1000 第二次 1000
                updateTaskStatus(task, SyncStatus.FAILED);
            }
        });

        return batchNo;
    }

    /**
     * 任务处理
     */
    private void processTask(Long businessId, CommoditySyncTask task, List<CommoditySyncCategoryDTO> syncData) {
        try {


            BusinessContextHolder.setBusinessId(businessId);
            // 阶段2：执行同步
            updateTaskStatus(task, SyncStatus.EXECUTING);

            Long storeId = task.getStoreId();
            log.info("项目:{},开始执行同步,门店:{}", businessId,storeId);
            storeSpuService.doSync(storeId, syncData);//db
            //更新门店缓存
            saveOrUpdateCache(syncData, storeId);
            //做下架单品与套餐联动动作并同步缓存

            storeSpuService.doSingleDown(storeId);


            log.info("项目:{},同步结束,门店:{}", businessId, storeId);

            // 阶段3：已完成
            updateTaskStatus(task, SyncStatus.COMPLETED);
        } catch (Exception e) {
            log.error("process task error ", e);
            task.setErrorMessage(e.getMessage());
            updateTaskStatus(task, SyncStatus.FAILED);
        }finally {
            BusinessContextHolder.clear();
        }
    }

    /**
     * 更新门店缓存
     *
     * @param syncData
     * @param storeId
     */
    private void saveOrUpdateCache(List<CommoditySyncCategoryDTO> syncData, Long storeId) {
        for (CommoditySyncCategoryDTO syncDatum : syncData) {

            CommodityStoreCategory storeCategory = syncDatum.getCategory();
            Long storeCategoryId = storeCategory.getCommodityStoreCategoryId();

            //新增或更新分类缓存
            CategoryDto categoryDto = CommodityConvertor.convertDoToCategoryDTO(storeCategory);
            try {
                storeRedisDao.saveOrUpdateCategory(storeId, storeCategoryId, categoryDto);
            } catch (Exception e) {
                log.error("Redis saveOrUpdateCategory error ", e);
                throw new RuntimeException(e);
            }

            //新增或更新商品缓存
            for (CommoditySyncSpuDTO syncSpuDTO : syncDatum.getSpuList()) {
                CommodityStoreSpu storeSpu = syncSpuDTO.getSpu();
                List<CommoditySyncSetMealDTO> setMealList = syncSpuDTO.getSetMealList();
                List<CommodityStoreSku> skuList = syncSpuDTO.getStoreSkuList();
                List<CommodityStoreGroup> groupList = setMealList.stream()
                    .map(CommoditySyncSetMealDTO::getStoreGroup).toList();
                List<CommodityStoreSingle> singleList = setMealList.stream()
                    .flatMap(dto -> dto.getStoreSingles().stream()).toList();

                Long storeSpuId = storeSpu.getCommodityStoreSpuId();
                List<CommodityTag> commodityTags = syncSpuDTO.getCommodityTags();
                SpuDto spuDto = CommodityConvertor.convertDosToSpuDTOQ(storeSpu, skuList, singleList, groupList, commodityTags);
                try {
                    storeRedisDao.saveOrUpdateProduct(storeId, storeCategoryId, storeSpuId, spuDto);
                } catch (Exception e) {
                    log.error("Redis saveOrUpdateProduct error ", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public CommoditySyncTask createTask(Long storeId, String storeName, String params, String batchNo,
        List<CommoditySyncCategoryDTO> requestData) {
        CommoditySyncTask task = new CommoditySyncTask();
        task.setStoreId(storeId);
        task.setStoreName(storeName);
        task.setStatus(SyncStatus.PENDING.getCode());
        task.setParams(params);
        task.setBatchNo(batchNo);
//        task.setRequestData(JSON.toJSONString(requestData));
        try {
            taskMapper.insert(task);
        } catch (DataIntegrityViolationException e) {
            log.error("当前门店:{}存在进行中的任务", storeId, e);
            throw new RuntimeException(String.format("当前门店:%s存在进行中的任务", storeId), e);
        }
        return task;
    }

    /**
     * 任务状态更新
     */
    public void updateTaskStatus(CommoditySyncTask freshTask, SyncStatus status) {
        freshTask.setStatus(status.getCode());
        if (status == SyncStatus.EXECUTING) {
            freshTask.setStartTime(LocalDateTime.now());
        } else if (status == SyncStatus.COMPLETED) {
            freshTask.setFinishTime(LocalDateTime.now());
        } else if (status == SyncStatus.FAILED) {
            freshTask.setFinishTime(LocalDateTime.now());
        }

        try {
            taskMapper.updateById(freshTask);
        } catch (Exception e) {
            log.error("任务[{}]并发更新冲突，将重试", freshTask.getId());
        }
    }

}


