package com.htyoudao.youdao.module.commodity.service.templateSpu;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityDetailRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityGroupSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommoditySingleSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template.TemplateCommodityUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortSpuReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateSpusMapper;
import com.htyoudao.youdao.module.commodity.service.templateGroup.ICommodityTemplateSetmealGroupService;
import com.htyoudao.youdao.module.commodity.service.templateSingle.ICommodityTemplateGroupSingleService;
import com.htyoudao.youdao.module.commodity.service.templateSku.ICommodityTemplateSkusService;
import com.htyoudao.youdao.module.commodity.service.spuTag.ICommodityTageService;
import com.htyoudao.youdao.module.commodity.util.ConvertUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.COMMODITY_TEMP_SPU_UPDATE_SUB_TYPE;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.COMMODITY_TEMP_SPU_UPDATE_SUCCESS;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.COMMODITY_TEMP_TYPE;

/**
 * <p>
 * 模板商品表 服务实现类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
@Service
public class CommodityTemplateSpusServiceImpl implements ICommodityTemplateSpusService {

    @Resource
    private CommodityTemplateSpusMapper commodityTemplateSpusMapper;

    @Resource
    private ICommodityTemplateSkusService commodityTemplateSkusService;

    @Resource
    private ICommodityTemplateSetmealGroupService commodityTemplateSetmealGroupService;

    @Resource
    private ICommodityTemplateGroupSingleService commodityTemplateGroupSingleService;

    @Resource
    private ICommodityTageService commodityTageService;

    @Override
    public void saveBatch(List<CommodityTemplateSpus> addCommodityTemplateSpusList) {

        commodityTemplateSpusMapper.insertBatch(addCommodityTemplateSpusList);
    }


    @Override
    public List<CommodityTemplateSpus> selectByTemplateCategoryId(Long templateCategorId) {
        LambdaQueryWrapper<CommodityTemplateSpus> commodityTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTemplateLambdaQueryWrapper.eq(CommodityTemplateSpus::getCategoryTemplateId,templateCategorId);
        commodityTemplateLambdaQueryWrapper.orderByAsc(CommodityTemplateSpus::getSort);
        commodityTemplateLambdaQueryWrapper.orderByDesc(CommodityTemplateSpus::getCreateTime);
       return commodityTemplateSpusMapper.selectList(commodityTemplateLambdaQueryWrapper);
    }

    @Override
    public List<CommodityTemplateSpus> selectByTemplateCategoryIds(List<Long> templateCategorIds) {
        LambdaQueryWrapper<CommodityTemplateSpus> commodityTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTemplateLambdaQueryWrapper.in(CommodityTemplateSpus::getCategoryTemplateId,templateCategorIds);
        commodityTemplateLambdaQueryWrapper.orderByAsc(CommodityTemplateSpus::getSort);
        commodityTemplateLambdaQueryWrapper.orderByDesc(CommodityTemplateSpus::getCreateTime);
        return commodityTemplateSpusMapper.selectList(commodityTemplateLambdaQueryWrapper);
    }

    @Override
    public void deleteByIds(List<Long> commodityTemplateIds) {
        LambdaQueryWrapper<CommodityTemplateSpus> commodityTemplateSpusLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTemplateSpusLambdaQueryWrapper.in(CommodityTemplateSpus::getCommodityTemplateId,commodityTemplateIds);
        commodityTemplateSpusMapper.delete(commodityTemplateSpusLambdaQueryWrapper);
    }

    @Override
    public List<CommodityTemplateSpus> selectByTemplateId(Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateSpus> commodityTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commodityTemplateLambdaQueryWrapper.in(CommodityTemplateSpus::getTemplateId,commodityTemplateId);
        commodityTemplateLambdaQueryWrapper.orderByAsc(CommodityTemplateSpus::getSort);
        commodityTemplateLambdaQueryWrapper.orderByDesc(CommodityTemplateSpus::getCreateTime);
        return commodityTemplateSpusMapper.selectList(commodityTemplateLambdaQueryWrapper);
    }

    @Override
    public void sortSpu(List<CommodityTemplateSortSpuReqVO> sortReqVOS) {
        for (CommodityTemplateSortSpuReqVO sortReqVO : sortReqVOS) {
            LambdaUpdateWrapper<CommodityTemplateSpus> spuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            spuLambdaUpdateWrapper.eq(CommodityTemplateSpus::getCommodityTemplateId, sortReqVO.getId());
            spuLambdaUpdateWrapper.set(CommodityTemplateSpus::getSort, sortReqVO.getSort());
            commodityTemplateSpusMapper.update(spuLambdaUpdateWrapper);
        }

    }

    @Override
    public List<CommodityTemplateSpus> selectByCommodityIds(List<Long> commodityIds, Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateSpus> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(CommodityTemplateSpus::getCommodityId, commodityIds);
        queryWrapper.eq(CommodityTemplateSpus::getTemplateId, commodityTemplateId);
        return commodityTemplateSpusMapper.selectList(queryWrapper);
    }



    @Override
    public void changeSpuNameAndImage(Long commodityId, String commodityName, List<String> imageUrlVO) {
        LambdaUpdateWrapper<CommodityTemplateSpus> spuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spuLambdaUpdateWrapper.eq(CommodityTemplateSpus::getCommodityId, commodityId);
        spuLambdaUpdateWrapper.set(CommodityTemplateSpus::getCommodityName, commodityName);
        spuLambdaUpdateWrapper.set(CommodityTemplateSpus::getImageUrl, ConvertUtil.convertListToStringS(imageUrlVO));
        commodityTemplateSpusMapper.update(spuLambdaUpdateWrapper);
    }

    @Override
    public void changeSpuName(Long commodityId, String commodityName) {
        LambdaUpdateWrapper<CommodityTemplateSpus> spuLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        spuLambdaUpdateWrapper.eq(CommodityTemplateSpus::getCommodityId, commodityId);
        spuLambdaUpdateWrapper.set(CommodityTemplateSpus::getCommodityName, commodityName);
        commodityTemplateSpusMapper.update(spuLambdaUpdateWrapper);
    }

    @Override
    public TemplateCommodityDetailRespVO getTemplateSpuInfo(TemplateCommodityDetailReqVO detailReqVO) {
        CommodityTemplateSpus templateSpu = commodityTemplateSpusMapper.selectById(detailReqVO.getCommodityTemplateId());
        if (ObjectUtil.isEmpty(templateSpu)) {
            throw exception(SPU_IS_DEL);
        }

        TemplateCommodityDetailRespVO respVO = BeanUtils.toBean(templateSpu, TemplateCommodityDetailRespVO.class);
        respVO.setDayNumberList(parseIntegerCsv(respVO.getDayNumbers()));
        respVO.setWeekNumberList(parseIntegerCsv(respVO.getWeekNumbers()));
        respVO.setTimeRangeList(parseStringCsv(respVO.getTimeRange()));


        // 处理标签信息
        if (ObjectUtil.isNotEmpty(templateSpu.getTagIds())) {
            List<Long> tagIds = ConvertUtil.convertStringToList(templateSpu.getTagIds());
            List<CommodityTag> commodityTags = commodityTageService.selectListByIds(tagIds);
            Map<Long, CommodityTag> tagMap = commodityTags.stream().collect(Collectors.toMap(CommodityTag::getId, tag -> tag));
            for (Long tagId : tagIds) {
                CommodityTag commodityTag = tagMap.get(tagId);
                if (ObjectUtil.isNotEmpty(commodityTag)) {
                    respVO.getCommodityTagList().add(commodityTag);
                }
            }
        }

        if (ObjectUtil.isNotEmpty(templateSpu.getImageUrl())) {
            respVO.setImageUrlVO(ConvertUtil.convertStringToListS(templateSpu.getImageUrl()));
        }

        List<CommodityTemplateSkus> skus = commodityTemplateSkusService.selectByTemplateSpuIds(List.of(templateSpu.getCommodityTemplateId()));
        respVO.setCommodityTemplateSkusList(skus);

        ObjectMapper objectMapper = new ObjectMapper();
        if (ObjectUtil.isNotEmpty(templateSpu.getFlavor())) {
            try {
                respVO.setFlavorList(objectMapper.readValue(templateSpu.getFlavor(), new TypeReference<>() {
                }));
            } catch (JsonProcessingException e) {
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_DESERIALIZATION_EXCEPTION);
            }
        }
        if (ObjectUtil.isNotEmpty(templateSpu.getCondiments())) {
            try {
                respVO.setCondimentsList(objectMapper.readValue(templateSpu.getCondiments(), new TypeReference<>() {
                }));
            } catch (JsonProcessingException e) {
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_DESERIALIZATION_EXCEPTION);
            }
        }

        if (ObjectUtil.equals(templateSpu.getIsSingle(), 2)) {
            List<CommodityTemplateSetmealGroup> groups = commodityTemplateSetmealGroupService.selectByTemplateSpuIds(
                    List.of(templateSpu.getCommodityTemplateId()));
            if (ObjectUtil.isNotEmpty(groups)) {
                List<Long> groupIds = groups.stream().map(CommodityTemplateSetmealGroup::getId).toList();
                List<CommodityTemplateGroupSingle> singles = commodityTemplateGroupSingleService.selectByGroupTemplateIds(groupIds);
                Map<Long, List<CommodityTemplateGroupSingle>> singleMap = new HashMap<>();
                if (ObjectUtil.isNotEmpty(singles)) {
                    singleMap = singles.stream().collect(Collectors.groupingBy(CommodityTemplateGroupSingle::getTemplateGroupId));
                }
                List<TemplateCommodityGroupSaveVO> groupSaveVOS = new ArrayList<>();
                for (CommodityTemplateSetmealGroup group : groups) {
                    TemplateCommodityGroupSaveVO groupSaveVO = BeanUtils.toBean(group, TemplateCommodityGroupSaveVO.class);
                    List<CommodityTemplateGroupSingle> groupSingles = singleMap.get(group.getId());
                    if (ObjectUtil.isNotEmpty(groupSingles)) {
                        groupSaveVO.setCommodityTemplateSingleList(BeanUtils.toBean(groupSingles, TemplateCommoditySingleSaveVO.class));
                    }
                    groupSaveVOS.add(groupSaveVO);
                }
                respVO.setGroupList(groupSaveVOS);
            }
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = COMMODITY_TEMP_TYPE, subType = COMMODITY_TEMP_SPU_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.commodityTemplateId}}", success = COMMODITY_TEMP_SPU_UPDATE_SUCCESS)
    public void updateTemplateCommodity(TemplateCommodityUpdateReqVO updateReqVO) {
        CommodityTemplateSpus oldTemplateSpu = commodityTemplateSpusMapper.selectById(updateReqVO.getCommodityTemplateId());
        if (ObjectUtil.isEmpty(oldTemplateSpu)) {
            throw exception(SPU_IS_DEL);
        }

        CommodityTemplateSpus templateSpu = new CommodityTemplateSpus();
        BeanUtils.copyProperties(updateReqVO, templateSpu);

        // 处理标签列表
        if (ObjectUtil.isNotEmpty(updateReqVO.getCommodityTagList())) {
            List<Long> tagIds = updateReqVO.getCommodityTagList().stream()
                    .map(CommodityTag::getId)
                    .toList();
            templateSpu.setTagIds(ConvertUtil.convertListToString(tagIds));
        } else {
            templateSpu.setTagIds(null);
        }

        if (ObjectUtil.isNotEmpty(updateReqVO.getImageUrlVO())) {
            templateSpu.setImageUrl(ConvertUtil.convertListToStringS(updateReqVO.getImageUrlVO()));
        } else {
            templateSpu.setImageUrl(null);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        if (ObjectUtil.isNotEmpty(updateReqVO.getFlavorList())) {
            try {
                templateSpu.setFlavor(objectMapper.writeValueAsString(updateReqVO.getFlavorList()));
            } catch (JsonProcessingException e) {
                throw exception(BASE_FLAVOR_HANDLE_PROPERTY_SERIALIZATION_EXCEPTION);
            }
        } else {
            templateSpu.setFlavor(null);
        }

        if (ObjectUtil.isNotEmpty(updateReqVO.getCondimentsList())) {
            try {
                templateSpu.setCondiments(objectMapper.writeValueAsString(updateReqVO.getCondimentsList()));
            } catch (JsonProcessingException e) {
                throw exception(BASE_CONDIMENT_HANDLE_INGREDIENT_SERIALIZATION_EXCEPTION);
            }
        } else {
            templateSpu.setCondiments(null);
        }

        // 处理时间相关的列表字段转换
        if (ObjectUtil.isNotEmpty(updateReqVO.getDayNumberList())) {
            templateSpu.setDayNumbers(updateReqVO.getDayNumberList().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        } else {
            templateSpu.setDayNumbers(null);
        }

        if (ObjectUtil.isNotEmpty(updateReqVO.getWeekNumberList())) {
            templateSpu.setWeekNumbers(updateReqVO.getWeekNumberList().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        } else {
            templateSpu.setWeekNumbers(null);
        }

        if (ObjectUtil.isNotEmpty(updateReqVO.getTimeRangeList())) {
            templateSpu.setTimeRange(String.join(",", updateReqVO.getTimeRangeList()));
        } else {
            templateSpu.setTimeRange(null);
        }

        List<CommodityTemplateSkus> skusList = updateReqVO.getCommodityTemplateSkusList();
        if (ObjectUtil.isEmpty(skusList)) {
            throw exception(BASE_SKU_PRODUCT_MUST_HAVE_SPECIFICATION);
        }
        if (skusList.size() > 1) {
            skusList.forEach(sku -> {
                if (StringUtils.isBlank(sku.getSkusName()) || StringUtils.isBlank(sku.getSkusValue())) {
                    throw exception(BASE_SKU_MULTIPLE_SPECIFICATION_NAMES_REQUIRED);
                }
            });
        }
        boolean hasStatusOne = skusList.stream()
                .anyMatch(sku -> ObjectUtil.equals(sku.getWxStatus(), 1) || ObjectUtil.equals(sku.getStoreStatus(), 1));
        if (!hasStatusOne) {
            throw exception(STORE_SKU_PRODUCT_CANNOT_HAVE_NO_PUBLISHED_SPECIFICATIONS);
        }

        skusList.forEach(sku -> {
            sku.setTemplateSkuId(null);
            sku.setTemplateId(updateReqVO.getTemplateId());
            sku.setCommodityTemplateId(updateReqVO.getCommodityTemplateId());
            sku.setCommodityId(updateReqVO.getCommodityId());
        });
        commodityTemplateSkusService.deleteByTemplateSpuIds(List.of(updateReqVO.getCommodityTemplateId()));
        commodityTemplateSkusService.saveBatch(skusList);

        if (ObjectUtil.equals(updateReqVO.getIsSingle(), 2)) {
            if (ObjectUtil.isEmpty(updateReqVO.getGroupList())) {
                throw exception(STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_TWO_ITEMS);
            }
            if (ObjectUtil.equals(updateReqVO.getSetmealType(), 1)) {
                updateReqVO.getGroupList().forEach(group -> {
                    if (ObjectUtil.isNotEmpty(group.getCommodityTemplateSingleList()) && group.getCommodityTemplateSingleList().size() < 2) {
                        if (group.getCommodityTemplateSingleList().size() == 1
                                && group.getCommodityTemplateSingleList().get(0).getCopies() < 2) {
                            throw exception(STORE_GROUP_FIXED_COLLOCATION_PACKAGE_REQUIRES_AT_LEAST_TWO_ITEMS);
                        }
                    }
                });
            } else {
                updateReqVO.getGroupList().forEach(group -> {
                    if (ObjectUtil.isEmpty(group.getCommodityTemplateSingleList())) {
                        throw exception(STORE_GROUP_PACKAGE_GROUP_REQUIRES_AT_LEAST_ONE_ITEM);
                    }
                });
            }

            commodityTemplateSetmealGroupService.deleteByTemplateSpuIds(List.of(updateReqVO.getCommodityTemplateId()));
            commodityTemplateGroupSingleService.deleteByTemplateSpuIds(List.of(updateReqVO.getCommodityTemplateId()));

            for (TemplateCommodityGroupSaveVO groupSaveVO : updateReqVO.getGroupList()) {
                CommodityTemplateSetmealGroup group = BeanUtils.toBean(groupSaveVO, CommodityTemplateSetmealGroup.class);
                group.setTemplateId(updateReqVO.getTemplateId());
                group.setCommodityTemplateId(updateReqVO.getCommodityTemplateId());
                commodityTemplateSetmealGroupService.saveBatch(List.of(group));

                if (ObjectUtil.isNotEmpty(groupSaveVO.getCommodityTemplateSingleList())) {
                    List<CommodityTemplateGroupSingle> singleList = BeanUtils.toBean(groupSaveVO.getCommodityTemplateSingleList(), CommodityTemplateGroupSingle.class);
                    singleList.forEach(single -> {
                        single.setTemplateId(updateReqVO.getTemplateId());
                        single.setCommodityTemplateId(updateReqVO.getCommodityTemplateId());
                        single.setTemplateGroupId(group.getId());
                        single.setSpuId(updateReqVO.getCommodityId());
                    });
                    commodityTemplateGroupSingleService.saveBatch(singleList);
                }
            }
        }

        commodityTemplateSpusMapper.update(templateSpu, new LambdaUpdateWrapper<CommodityTemplateSpus>()
                .eq(CommodityTemplateSpus::getCommodityTemplateId, templateSpu.getCommodityTemplateId()));
        LogRecordContext.putVariable("commodityTemplateId", updateReqVO.getCommodityTemplateId());
    }

    private List<Integer> parseIntegerCsv(String csv) {
        if (StringUtils.isBlank(csv)) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Integer::valueOf)
                .toList();
    }

    private List<String> parseStringCsv(String csv) {
        if (StringUtils.isBlank(csv)) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }
}
