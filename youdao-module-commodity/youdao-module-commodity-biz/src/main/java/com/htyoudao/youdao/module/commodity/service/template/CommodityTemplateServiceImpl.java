package com.htyoudao.youdao.module.commodity.service.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateListRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSaveReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityUpdateTemplateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplate;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityCategoryMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityGroupSingleMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommoditySpusMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateMapper;
import com.htyoudao.youdao.module.commodity.service.group.ICommoditySetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.sku.ICommoditySkusService;
import com.htyoudao.youdao.module.commodity.service.templateCategory.ICommodityTemplateCategoryService;
import com.htyoudao.youdao.module.commodity.service.templateGroup.ICommodityTemplateSetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.templateSingle.ICommodityTemplateGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.templateSku.ICommodityTemplateSkusService;
import com.htyoudao.youdao.module.commodity.service.templateSpu.ICommodityTemplateSpusService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;

@Service
@Slf4j
public class CommodityTemplateServiceImpl  implements ICommodityTemplateService {

    @Resource
    private CommodityTemplateMapper templateMapper;

    @Resource
    private CommoditySpusMapper spuMapper;

    @Resource
    private ICommoditySetmealGroupService groupService;

    @Resource
    private CommodityGroupSingleMapper commodityGroupSingleMapper;

    @Resource
    private ICommodityTemplateSetmealGroupService templateGroupService;

    @Resource
    private ICommodityTemplateGroupSingleService templateSingleService;

    @Resource
    private ICommodityTemplateCategoryService templateCategoryService;

    @Resource
    private ICommodityTemplateSpusService templateSpuService;

    @Resource
    private ICommodityTemplateSkusService templateSkuService;

    @Resource
    private ICommoditySkusService skuService;

    @Resource
    private CommodityCategoryMapper commodityCategoryMapper;

    @Override
    public List<CommodityTemplateListRespVo> selectCommodityTemplateListNew() {

        LambdaQueryWrapper<CommodityTemplate> commodityTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTemplateLambdaQueryWrapper.orderByDesc(CommodityTemplate::getCreateTime);
        List<CommodityTemplate> commodityTemplates = templateMapper.selectList(commodityTemplateLambdaQueryWrapper);
        List<CommodityTemplateListRespVo> templateListRespVos = BeanCopyUtils.copyBeanList(commodityTemplates, CommodityTemplateListRespVo.class);
        return templateListRespVos;

    }

    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_CREATE_SUB_TYPE, bizNo = "{{#commodityTemplate.commodityTemplateId}}", success = COMMODITY_TEMP_CREATE_SUCCESS)
    public void add(CommodityTemplateSaveReqVo saveReqVo) {
        validateNameDuplicate(saveReqVo.getCommodityTemplateName(), null);
        CommodityTemplate commodityTemplate = new CommodityTemplate();
        BeanUtils.copyProperties(saveReqVo,commodityTemplate);
        templateMapper.insert(commodityTemplate);
        LogRecordContext.putVariable("commodityTemplate", commodityTemplate);
    }




    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_UPDATE_SUB_TYPE, bizNo = "{{#commodityTemplate.commodityTemplateId}}", success = COMMODITY_TEMP_UPDATE_SUCCESS)
    public void update(CommodityUpdateTemplateReqVO commodityTemplate) {
        validateNameDuplicate(commodityTemplate.getCommodityTemplateName(), commodityTemplate.getCommodityTemplateId());
        templateMapper.updateById(BeanUtils.toBean(commodityTemplate, CommodityTemplate.class));
        LogRecordContext.putVariable("commodityTemplate", commodityTemplate);
    }

    private void validateNameDuplicate(String name, Long id) {
        // 1. 该 name 名字被其它角色所使用
        CommodityTemplate candidateTemplate = templateMapper.selectOne(new LambdaQueryWrapper<CommodityTemplate>()
            .eq(CommodityTemplate::getCommodityTemplateName, name));
        if (candidateTemplate != null && !candidateTemplate.getCommodityTemplateId().equals(id)) {
            throw exception(TEMP_NAME_DUPLICATED);
        }
    }


    @Override
    public void deleteByTemplateId(Long commodityTemplateId) {
        templateMapper.deleteById(commodityTemplateId);
    }

    @Override
    public CommodityTemplate selectById(Long templateId) {
        return templateMapper.selectById(templateId);
    }

    @Override
    @Transactional
    public void copy(Long commodityTemplateId) {
        // 查询模板
        CommodityTemplate commodityTemplate = templateMapper.selectById(commodityTemplateId);
        if (commodityTemplate == null) {
            throw exception(TEMP_ID_CANNOT_BE_EMPTY);
        }
        commodityTemplate.setCommodityTemplateName(commodityTemplate.getCommodityTemplateName() + "的副本");
        commodityTemplate.setCommodityTemplateId(null);
        templateMapper.insert(commodityTemplate);
        Long newTemplateId = commodityTemplate.getCommodityTemplateId();

        //分类
        List<CommodityTemplateCategory> templateCategories = templateCategoryService.selectByTemplateId(commodityTemplateId);
        if (CollectionUtils.isEmpty(templateCategories)){
            return;
        }

        List<Long> categoryIds = templateCategories.stream().map(CommodityTemplateCategory::getId).toList();
        templateCategories.forEach(c -> c.setTemplateId(newTemplateId));
        templateCategories.forEach(c -> c.setId(null));
        templateCategoryService.saveBatch(templateCategories);

        Map<Long,Long> categoryIdMap = new HashMap<>();
        for (int i = 0; i < templateCategories.size(); i++) {
            categoryIdMap.put(categoryIds.get(i),templateCategories.get(i).getId());
        }


        //spu
        List<CommodityTemplateSpus> templateSpus = templateSpuService.selectByTemplateId(commodityTemplateId);
        if (CollectionUtils.isEmpty(templateSpus)){
            return;
        }
        List<Long> spuIds = templateSpus.stream().map(CommodityTemplateSpus::getCommodityTemplateId).toList();
        for (CommodityTemplateSpus spu : templateSpus) {
            spu.setCommodityTemplateId(null);
            spu.setTemplateId(newTemplateId);
            spu.setCategoryTemplateId(categoryIdMap.get(spu.getCategoryTemplateId()));
        }
        templateSpuService.saveBatch(templateSpus);
        Map<Long, Long> spuIdMap = new HashMap<>();
        for (int i = 0; i < templateSpus.size(); i++) {
            spuIdMap.put(spuIds.get(i),templateSpus.get(i).getCommodityTemplateId());
        }


        //sku
        List<CommodityTemplateSkus> templateSkus = templateSkuService.selectByTemplateId(commodityTemplateId);
        if (CollectionUtils.isEmpty(templateSkus)){
            throw exception(TEMP_SKU_LIST_CANNOT_BE_EMPTY);
        }
//        List<Long> skuIds = templateSkus.stream().map(CommodityTemplateSkus::getSkuId).toList();
        for (CommodityTemplateSkus sku : templateSkus) {
            sku.setTemplateId(newTemplateId);
            sku.setTemplateSkuId(null);
            sku.setCommodityTemplateId(spuIdMap.get(sku.getCommodityTemplateId()));
        }
        templateSkuService.saveBatch(templateSkus);

        //套餐分组
        List<CommodityTemplateSetmealGroup> templateSetmealGroups = templateGroupService.selectByTemplateId(commodityTemplateId);
        if (!CollectionUtils.isEmpty(templateSetmealGroups)) {

            List<Long> groupIds = templateSetmealGroups.stream().map(CommodityTemplateSetmealGroup::getId).toList();
            templateSetmealGroups.forEach(c -> c.setCommodityTemplateId(spuIdMap.get(c.getCommodityTemplateId())));
            templateSetmealGroups.forEach(c -> c.setTemplateId(newTemplateId));
            templateSetmealGroups.forEach(c -> c.setId(null));
            templateGroupService.saveBatch(templateSetmealGroups);
            Map<Long, Long> groupIdMap = new HashMap<>();
            for (int i = 0; i < templateSetmealGroups.size(); i++) {
                groupIdMap.put(groupIds.get(i), templateSetmealGroups.get(i).getId());
            }

            //套餐单品
            List<CommodityTemplateGroupSingle> templateGroupSingles = templateSingleService.selectByTemplateId(commodityTemplateId);
            if (!CollectionUtils.isEmpty(templateGroupSingles)) {
                for (CommodityTemplateGroupSingle templateGroupSingle : templateGroupSingles) {
                    templateGroupSingle.setCommodityTemplateId(spuIdMap.get(templateGroupSingle.getCommodityTemplateId()));
                    templateGroupSingle.setTemplateId(newTemplateId);
                    templateGroupSingle.setId(null);
                    templateGroupSingle.setTemplateGroupId(groupIdMap.get(templateGroupSingle.getTemplateGroupId()));
                }
                templateSingleService.saveBatch(templateGroupSingles);
            }
        }


    }



    @Override
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_SYNC_SUB_TYPE,  bizNo = "{{#commodityTemplateId}}", success = "同步到商品模板")
    public void base2template(List<Long> commodityIds, Long commodityTemplateId) {
        List<CommoditySpus> commoditySpus = spuMapper.selectByIds(commodityIds);
        if (CollectionUtils.isEmpty(commoditySpus)) {
            throw exception(TEMP_SPU_LIST_CANNOT_BE_EMPTY);
        }


        // 模板存在的分类
        List<CommodityTemplateCategory> commodityTemplateCategories = templateCategoryService.selectByTemplateId(commodityTemplateId);

        Set<Long> oldCategoryIds = commodityTemplateCategories.stream()
            .map(CommodityTemplateCategory::getCategoryId)
            .collect(Collectors.toSet());

        //需要新增的分类就是 之前模板不存在的分类
        List<Long> newCategoryIds = commoditySpus.stream().map(CommoditySpus::getCategoryId)
            .distinct()
            .filter(c ->  !oldCategoryIds.contains(c))
            .toList();
        if (!CollectionUtils.isEmpty(newCategoryIds)) {

            List<CommodityCategory> commodityCategories = commodityCategoryMapper.selectByIds(newCategoryIds);
            List<CommodityTemplateCategory> addList = new ArrayList<>();
            for (CommodityCategory commodityCategory : commodityCategories) {
                CommodityTemplateCategory commodityTemplateCategory = new CommodityTemplateCategory();
                BeanUtils.copyProperties(commodityCategory, commodityTemplateCategory);
                commodityTemplateCategory.setTemplateId(commodityTemplateId);
                commodityTemplateCategory.setCategoryId(commodityCategory.getId());
                commodityTemplateCategory.setId(null);
                addList.add(commodityTemplateCategory);
            }
            // 将分类添加至模板
            templateCategoryService.saveBatch(addList);
        }

        // 查询模板分类
        List<CommodityTemplateCategory> commodityTemplateCategoryList = templateCategoryService.selectByTemplateId(commodityTemplateId);

        //0630 同步到模板 先删再增 覆盖同步
        List<CommodityTemplateSpus> commodityTemplateSpuses = templateSpuService.selectByCommodityIds(commodityIds, commodityTemplateId);
        if (!CollectionUtils.isEmpty(commodityTemplateSpuses)){
            List<Long> ids = commodityTemplateSpuses.stream()
                .map(CommodityTemplateSpus::getCommodityTemplateId).distinct()
                .toList();
            templateSpuService.deleteByIds(ids);
            templateSkuService.deleteByTemplateSpuIds(ids);
            templateGroupService.deleteByTemplateSpuIds(ids);
            templateSingleService.deleteByTemplateSpuIds(ids);
        }


        // 商品spu添加
        List<CommoditySpus> commoditySpusList = spuMapper.selectByIds(commodityIds);
        List<CommodityTemplateSpus> addCommodityTemplateSpusList = createCommodityTemplateSpus(commoditySpusList, commodityTemplateCategoryList, commodityTemplateId);
        templateSpuService.saveBatch(addCommodityTemplateSpusList);

        for (CommodityTemplateSpus commodityTemplateSpus : addCommodityTemplateSpusList) {
            Long commodityId = commodityTemplateSpus.getCommodityId();
            Long templateSpuId = commodityTemplateSpus.getCommodityTemplateId();

            //1.sku
            List<CommoditySkus> commoditySkuList = skuService.selectBySpuId(commodityId);
            List<CommodityTemplateSkus> addCommodityTemplateSkuList = new ArrayList<>();
            for (CommoditySkus commoditySkus : commoditySkuList) {
                CommodityTemplateSkus commodityTemplateSkus = new CommodityTemplateSkus();
                BeanUtils.copyProperties(commoditySkus, commodityTemplateSkus);
                commodityTemplateSkus.setTemplateId(commodityTemplateId);
                commodityTemplateSkus.setCommodityTemplateId(templateSpuId);
                commodityTemplateSkus.setSkuId(commoditySkus.getSkuId());
                addCommodityTemplateSkuList.add(commodityTemplateSkus);
            }
            templateSkuService.saveBatch(addCommodityTemplateSkuList);

            if (Objects.equals(commodityTemplateSpus.getIsSingle(),1)){
                continue;
            }

            //2.group
            List<CommoditySetmealGroup> commoditySetmealGroupList = groupService.selectBySpuId(commodityId);
            for (CommoditySetmealGroup commoditySetmealGroup : commoditySetmealGroupList) {
                CommodityTemplateSetmealGroup commodityTemplateSetmealGroup = new CommodityTemplateSetmealGroup();
                BeanUtils.copyProperties(commoditySetmealGroup, commodityTemplateSetmealGroup);
                commodityTemplateSetmealGroup.setTemplateId(commodityTemplateId);
                commodityTemplateSetmealGroup.setCommodityTemplateId(templateSpuId);
                if (commoditySetmealGroup.getChoose() != null){
                    commodityTemplateSetmealGroup.setChoose(Math.toIntExact(commoditySetmealGroup.getChoose()));
                }
                commodityTemplateSetmealGroup.setId(IdWorker.getId());
                templateGroupService.saveBatch(List.of(commodityTemplateSetmealGroup));

                List<CommodityTemplateGroupSingle> addCommodityTemplateGroupSingleList = new ArrayList<>();
                List<CommodityGroupSingle> commodityGroupSingleList = commodityGroupSingleMapper.listByGroupId(commodityId, commoditySetmealGroup.getGroupId());
                for (CommodityGroupSingle commodityGroupSingle : commodityGroupSingleList) {
                    CommodityTemplateGroupSingle commodityTemplateGroupSingle = new CommodityTemplateGroupSingle();
                    BeanUtils.copyProperties(commodityGroupSingle, commodityTemplateGroupSingle);
                    commodityTemplateGroupSingle.setTemplateId(commodityTemplateId);
                    commodityTemplateGroupSingle.setCommodityTemplateId(templateSpuId);
                    commodityTemplateGroupSingle.setTemplateGroupId(commodityTemplateSetmealGroup.getId());//模板groupID
                    commodityTemplateGroupSingle.setCommodityGroupSingleId(commodityGroupSingle.getCommodityGroupSingleId());// 原商品SingleId
                    addCommodityTemplateGroupSingleList.add(commodityTemplateGroupSingle);
                }
                templateSingleService.saveBatch(addCommodityTemplateGroupSingleList);
            }
        }
    }



    private List<CommodityTemplateSpus> createCommodityTemplateSpus(List<CommoditySpus> commoditySpusList, List<CommodityTemplateCategory> commodityTemplateCategoryList, Long commodityTemplateId) {
        if (commoditySpusList == null) {
            throw exception(TEMP_SPU_LIST_CANNOT_BE_EMPTY);
        }
        if (commodityTemplateCategoryList == null) {
            throw exception(TEMP_CATE_LIST_CANNOT_BE_EMPTY);
        }
        if (commodityTemplateId == null) {
            throw exception(TEMP_ID_CANNOT_BE_EMPTY);
        }
        List<CommodityTemplateSpus> addCommodityTemplateSpusList = new ArrayList<>();
        for (CommoditySpus commoditySpus : commoditySpusList) {
            CommodityTemplateSpus commodityTemplateSpus = new CommodityTemplateSpus();
            BeanUtils.copyProperties(commoditySpus, commodityTemplateSpus);
            commodityTemplateSpus.setSort(Long.valueOf(commoditySpus.getSort()));
            // 查找对应的 CommodityTemplateCategory
            Optional<CommodityTemplateCategory> optionalCommodityTemplateCategory = findCommodityTemplateCategory(commodityTemplateCategoryList, commoditySpus.getCategoryId());
            // 如果找到对应的 CommodityTemplateCategory，则设置 categoryTemplateId
            optionalCommodityTemplateCategory.ifPresent(templateCategory ->
                commodityTemplateSpus.setCategoryTemplateId(templateCategory.getId())
            );
            if (StringUtils.isEmpty(commodityTemplateSpus.getCondiments())) {
                commodityTemplateSpus.setCondiments(null);
            }
            if (StringUtils.isEmpty(commodityTemplateSpus.getFlavor())) {
                commodityTemplateSpus.setFlavor(null);
            }


            commodityTemplateSpus.setTemplateId(commodityTemplateId);
            addCommodityTemplateSpusList.add(commodityTemplateSpus);
        }
        return addCommodityTemplateSpusList;
    }
    private Optional<CommodityTemplateSpus> findCommodityTemplateSpus(List<CommodityTemplateSpus> addCommodityTemplateSpusList, Long commodityId) {

        if (addCommodityTemplateSpusList == null) {
            throw exception(TEMP_SPU_LIST_CANNOT_BE_EMPTY);
        }
        if (commodityId == null) {
            throw exception(TEMP_SPU_ID_CANNOT_BE_EMPTY);
        }
        return addCommodityTemplateSpusList.stream()
            .filter(commodityTemplateSpus -> commodityTemplateSpus.getCommodityId().equals(commodityId)  )
            .findFirst();
    }


    private Optional<CommodityTemplateCategory> findCommodityTemplateCategory(List<CommodityTemplateCategory> commodityTemplateCategoryList, Long categoryId) {
        if (commodityTemplateCategoryList == null) {
            throw exception(TEMP_CATE_LIST_CANNOT_BE_EMPTY);
        }
        if (categoryId == null) {
            throw exception(TEMP_CATE_ERROR_CODE_CATEGORY_ID_CANNOT_BE_EMPTY);
        }
        return commodityTemplateCategoryList.stream()
            .filter(templateCategory -> templateCategory.getCategoryId().equals(categoryId) )
            .findFirst();
    }

}
