package com.htyoudao.youdao.module.commodity.service.category;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryIdReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategoryRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySaveReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.category.VO.CategorySortReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCategory;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityCategoryMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;



/**
 * 商品分类Service业务层处理
 *
 * @author Qizhongnan
 * @date 2024-01-10
 */
@Slf4j
@Service
public class CommodityCategoryServiceImpl implements ICommodityCategoryService {

    @Resource
    private CommodityCategoryMapper commodityCategoryMapper;

    @Override
    public List<CategoryRespVo> getCommodityCategoryList() {

        List<CommodityCategory> commodityCategories = commodityCategoryMapper.selectList();
        List<CategoryRespVo> categoryRespVoList = commodityCategories
                .stream()
                .map(category -> {
                    CategoryRespVo categoryRespVo = new CategoryRespVo();
                    BeanUtils.copyProperties(category, categoryRespVo);
                    return categoryRespVo;
                }).collect(Collectors.toList());

        return categoryRespVoList;
    }

    /**
     * V3.0 商品分组新建接口(PC)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(success = COMMODITY_CATE_CREATE_SUCCESS, type = COMMODITY_TYPE, subType = COMMODITY_CATE_CREATE_SUB_TYPE, bizNo = "{{#commodityCategory.id}}")
    public Long createCategoryV3(CategorySaveReqVo saveReqVo) {
        if (ObjectUtil.isNotEmpty(saveReqVo.getStartDate())){
            if (ObjectUtil.isEmpty(saveReqVo.getEndDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
        if (ObjectUtil.isNotEmpty(saveReqVo.getEndDate())){
            if (ObjectUtil.isEmpty(saveReqVo.getStartDate())){
                throw exception(COMMODITY_DATE_INCOMPLETE);
            }
        }
        LambdaQueryWrapper<CommodityCategory> categoryLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        List<CommodityCategory> list1 = commodityCategoryMapper.selectList(categoryLambdaQueryWrapper1);
        if (ObjectUtil.isNotEmpty(list1)) {
            if (saveReqVo.getType().equals(1)) {
                List<CommodityCategory> list2 = list1.stream().filter(c -> c.getType().equals(1)).toList();
                if (ObjectUtil.isNotEmpty(list2)) {
                    throw exception(BASE_CATE_WITH_GROUPING);
                }
            }
            List<CommodityCategory> list3 = list1.stream().filter(c -> c.getName().equals(saveReqVo.getName())).toList();
            if (ObjectUtil.isNotEmpty(list3)) {
                throw exception(BASE_CATE_EXISTING_NAME_CATEGORY);
            }
        }
        CommodityCategory commodityCategory = new CommodityCategory();
        BeanUtils.copyProperties(saveReqVo, commodityCategory);
        commodityCategoryMapper.insert(commodityCategory);
        LogRecordContext.putVariable("commodityCategory", commodityCategory);
        return commodityCategory.getId();
    }


    @Override
    public CategoryRespVo getCategoryInfo(CategoryIdReqVo idReqVo) {
        CategoryRespVo categoryRespVo = new CategoryRespVo();
        CommodityCategory commodityCategory = commodityCategoryMapper.selectById(idReqVo.getId());

        BeanUtils.copyProperties(commodityCategory, categoryRespVo);
        return categoryRespVo;
    }

    @Override
    public CommodityCategory getById(Long id) {
        return commodityCategoryMapper.selectById(id);
    }

    @Override
    public void sortCategory(List<CategorySortReqVo> categorySortReqVoList) {
        for (CategorySortReqVo categorySortDTO : categorySortReqVoList) {
            LambdaUpdateWrapper<CommodityCategory> categoryLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            categoryLambdaUpdateWrapper.eq(CommodityCategory::getId, categorySortDTO.getCategoryId());
            categoryLambdaUpdateWrapper.set(CommodityCategory::getSort, categorySortDTO.getSort());
            commodityCategoryMapper.update(categoryLambdaUpdateWrapper);
        }
    }

    @Override
    public void deleteById(Long id) {
        commodityCategoryMapper.deleteById(id);
    }

    @Override
    public List<CommodityCategory> selectListByAsc() {
        LambdaQueryWrapper<CommodityCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.orderByAsc(CommodityCategory::getSort);
        categoryLambdaQueryWrapper.eq(CommodityCategory::getIsHidden, 0);
        return commodityCategoryMapper.selectList(categoryLambdaQueryWrapper);
    }

    @Override
    public List<CommodityCategory> selectAllListByAsc() {
        LambdaQueryWrapper<CommodityCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.orderByAsc(CommodityCategory::getSort);
        return commodityCategoryMapper.selectList(categoryLambdaQueryWrapper);
    }

    @Override
    public List<CommodityCategory> selectListByIdsAndAsc(List<Long> categoryIds) {
        LambdaQueryWrapper<CommodityCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.in(CommodityCategory::getId, categoryIds);
        categoryLambdaQueryWrapper.orderByAsc(CommodityCategory::getSort);


        return commodityCategoryMapper.selectList(categoryLambdaQueryWrapper);
    }


    @Override
    public void updateById(CommodityCategory commodityCategory) {
        commodityCategoryMapper.updateById(commodityCategory);
    }

    @Override
    public void updateHiddenByIds(List<Long> categoryIds, Integer isHidden) {
        LambdaUpdateWrapper<CommodityCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CommodityCategory::getId, categoryIds);
        updateWrapper.set(CommodityCategory::getIsHidden, isHidden);
        commodityCategoryMapper.update(updateWrapper);
    }
}
