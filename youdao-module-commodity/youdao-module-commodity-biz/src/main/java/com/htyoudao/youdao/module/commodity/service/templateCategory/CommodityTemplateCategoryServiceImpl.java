package com.htyoudao.youdao.module.commodity.service.templateCategory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.TemplateCategoryListRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSortSpuReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplate;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSpus;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 模板商品分类 服务实现类
 * </p>
 *
 * @author wangwei
 * @since 2025-02-05
 */
@Service
public class CommodityTemplateCategoryServiceImpl implements ICommodityTemplateCategoryService {

@Resource
    private CommodityTemplateCategoryMapper commodityTemplateCategoryMapper;

    @Override
    public List<CommodityTemplateCategory> selectByTemplateId(Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityTemplateCategory::getTemplateId, commodityTemplateId);
        queryWrapper.orderByAsc(CommodityTemplateCategory::getSort);
        return commodityTemplateCategoryMapper.selectList(queryWrapper);
    }

    @Override
    public void saveBatch(List<CommodityTemplateCategory> addList) {
        commodityTemplateCategoryMapper.insertBatch(addList);
    }

    @Override
    public void deleteById(Long templateCategoryId) {
        commodityTemplateCategoryMapper.deleteById(templateCategoryId);
    }

    @Override
    public void sortCategory(List<CommodityTemplateSortReqVO> sortReqVOS) {
        for (CommodityTemplateSortReqVO sortReqVO : sortReqVOS) {
            LambdaUpdateWrapper<CommodityTemplateCategory> categoryLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            categoryLambdaUpdateWrapper.eq(CommodityTemplateCategory::getId, sortReqVO.getId());
            categoryLambdaUpdateWrapper.set(CommodityTemplateCategory::getSort, sortReqVO.getSort());
            commodityTemplateCategoryMapper.update(categoryLambdaUpdateWrapper);
        }
    }


}
