package com.htyoudao.youdao.module.commodity.service.triInventory.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriCommodityRecipeMaterialMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriFormulationMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.triInventory.TriRecipeMapper;
import com.htyoudao.youdao.module.commodity.service.triInventory.ITriRecipeService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;

@Service
@Slf4j
public class TriRecipeServiceImpl implements ITriRecipeService {

    @Resource
    private TriRecipeMapper triRecipeMapper;

    @Resource
    private TriFormulationMapper triFormulationMapper;

    @Resource
    private TriCommodityRecipeMaterialMapper triCommodityRecipeMaterialMapper;

    @Override
    public List<TriRecipeDO> selectById(Long id) {
        LambdaQueryWrapper<TriRecipeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TriRecipeDO::getTriId, id);
        return triRecipeMapper.selectList(wrapper);
    }

    @Override
    public List<TriRecipeDO> selectByNames(TriRecipeReqVO recipeReqVO) {
        LambdaQueryWrapper<TriRecipeDO> wrapper = new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(recipeReqVO.getTriCommodityName())) {
            wrapper.in(TriRecipeDO::getTriCommodityName, recipeReqVO.getTriCommodityName());
        }
        if (StringUtils.isNotBlank(recipeReqVO.getTriType())) {
            wrapper.eq(TriRecipeDO::getTriType, recipeReqVO.getTriType());
        }
        return triRecipeMapper.selectList(wrapper);
    }

    @Override
    public void truncRecipeMaterial(Long triId, Long materialsId) {
        LambdaQueryWrapper<TriRecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TriRecipeMaterialDO::getTriId, triId);
        wrapper.eq(TriRecipeMaterialDO::getReplaceMaterialsId, materialsId);
        triCommodityRecipeMaterialMapper.delete(wrapper);
        LambdaQueryWrapper<TriRecipeDO> recipeWrapper = new LambdaQueryWrapper<>();
        recipeWrapper.eq(TriRecipeDO::getTriId, triId);
        recipeWrapper.eq(TriRecipeDO::getMaterialsId, materialsId);
        triRecipeMapper.delete(recipeWrapper);

        String truncRecipeMaterialIds = "triId=" + triId + ", materialsId=" + materialsId;
        // 记录操作日志上下文
        LogRecordContext.putVariable("truncRecipeMaterial", truncRecipeMaterialIds);
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_INSERT_RECIPE, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_INSERT_RECIPE_SUCCESS)
    public void insertRecipe(List<TriFormulationRecipeReqVO> triList, Long triId) {
        if(triId == null){
            throw exception(TRI_RECIPE_COMMODITY_ID_ERROR);
        }

        // 物理删除历史记录
        triRecipeMapper.deleteRecipe(triId);
        // 批量新增
        List<TriRecipeDO> recipes = BeanUtils.toBean(triList, TriRecipeDO.class);

        triRecipeMapper.insertBatch(recipes);

        //取出关联配方
        List<TriRecipeDO> relatedRecipes = Optional.ofNullable(recipes)
                .orElse(Collections.emptyList())
                .stream()
                .filter(recipe -> null != recipe.getRelatedRecipeId())
                .toList();
        //
        if (CollectionUtil.isNotEmpty(relatedRecipes)) {
            //执行关联复制 补全原料信息
            for (TriRecipeDO recipe : relatedRecipes) {
                Long relatedRecipeId = recipe.getRelatedRecipeId();
                LambdaQueryWrapper<TriRecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TriRecipeMaterialDO::getRecipeId, relatedRecipeId);
                List<TriRecipeMaterialDO> recipeMaterialDOS = triCommodityRecipeMaterialMapper.selectList(wrapper);
                Optional.ofNullable(recipeMaterialDOS)
                        .filter(CollectionUtil::isNotEmpty) // 仅保留非空、非空集合
                        .ifPresent(list -> {
                            // 第一步：修改集合属性（recipeId赋值 + recipeMaterialId置空）
                            list.forEach(rm -> {
                                if (rm != null) {
                                    rm.setRecipeId(recipe.getRecipeId());
                                    rm.setTriId(recipe.getTriId());
                                    rm.setRecipeMaterialId(null);
                                }
                            });
                            // 第二步：批量插入（无需再次判空，已通过filter过滤）
                            triCommodityRecipeMaterialMapper.insertBatch(list);
                        });

            }
        }


        //新增三方商品配置配方完毕后调整状态为已设置
        LambdaUpdateWrapper<TriFormulationDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(TriFormulationDO::getSkuFlag, 1);
        wrapper.eq(TriFormulationDO::getId, triId);
        triFormulationMapper.update(wrapper);

        String log = "commodityId=" + triId ;
        // 记录操作日志上下文
        LogRecordContext.putVariable("commodityId", log);
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS_SUCCESS)
    public void saveUnSelectedRecipeMaterial(List<TriRecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList) {
        if (CollectionUtil.isNotEmpty(recipeMaterialUnSelectedReqVOList)){
            Long triId = recipeMaterialUnSelectedReqVOList.get(0).getTriId();
            Long replaceMaterialsId = recipeMaterialUnSelectedReqVOList.get(0).getReplaceMaterialsId();
            // 查询替换的配方ID
            LambdaQueryWrapper<TriRecipeDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TriRecipeDO::getTriId, triId);
            wrapper.eq(TriRecipeDO::getMaterialsId, replaceMaterialsId);
            TriRecipeDO triRecipeDO = triRecipeMapper.selectOne(wrapper);
            if (triRecipeDO != null && triRecipeDO.getRecipeId() != null){
                for (TriRecipeMaterialUnSelectedReqVO reqVO : recipeMaterialUnSelectedReqVOList) {
                    // 确保对象不为null，避免操作null对象
                    if (reqVO != null) {
                        reqVO.setRecipeId(triRecipeDO.getRecipeId());
                    }
                }
            }
            triCommodityRecipeMaterialMapper.deleteSkuRecipeMaterial(triId, replaceMaterialsId);
            triCommodityRecipeMaterialMapper.insertBatch(BeanUtils.toBean(recipeMaterialUnSelectedReqVOList, TriRecipeMaterialDO.class));
            String materials = "triId=" + triId + ", replaceMaterialsId=" + replaceMaterialsId;
            // 记录操作日志上下文
            LogRecordContext.putVariable("materials", materials);
        }
    }

    @Override
    public List<TriRecipeMaterialUnSelectedRespVO> getUnSelectedRecipeMaterials(Long triId, Long replaceMaterialsId) {
        LambdaQueryWrapper<TriRecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TriRecipeMaterialDO::getTriId, triId);
        wrapper.eq(TriRecipeMaterialDO::getReplaceMaterialsId, replaceMaterialsId);

        List<TriRecipeMaterialDO> recipeMaterialDOS = triCommodityRecipeMaterialMapper.selectList(wrapper);

        return BeanUtils.toBean(recipeMaterialDOS, TriRecipeMaterialUnSelectedRespVO.class);
    }

    @Override
    public PageResult<TriFormulationDO> selectTriFormulationList(TriFormulationReqVO recipeReqVO) {
        LambdaQueryWrapper<TriFormulationDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(recipeReqVO.getTriCommodityName())) {
            wrapper.like(TriFormulationDO::getTriCommodityName, recipeReqVO.getTriCommodityName());
        }
        if (StringUtils.isNotBlank(recipeReqVO.getTriType())) {
            wrapper.eq(TriFormulationDO::getTriType, recipeReqVO.getTriType());
        }
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(recipeReqVO.getPageNo());
        pageParam.setPageSize(recipeReqVO.getPageSize());

        return triFormulationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public PageResult<TriFormulationRelatedRespVO> selectTriFormulationRelatedList(TriFormulationReqVO recipeReqVO) {
        LambdaQueryWrapper<TriFormulationDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(recipeReqVO.getTriCommodityName())) {
            wrapper.like(TriFormulationDO::getTriCommodityName, recipeReqVO.getTriCommodityName());
        }
        /*
        if (StringUtils.isNotBlank(recipeReqVO.getTriType())) {
            wrapper.eq(TriFormulationDO::getTriType, recipeReqVO.getTriType());
        }*/

        wrapper.eq(TriFormulationDO::getSkuFlag, 1);

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(recipeReqVO.getPageNo());
        pageParam.setPageSize(recipeReqVO.getPageSize());
        PageResult<TriFormulationDO> triFormulationDOPageResult = triFormulationMapper.selectPage(pageParam, wrapper);

        PageResult<TriFormulationRelatedRespVO> triFormulationRelatedRespVOPageResult = BeanUtils.toBean(triFormulationDOPageResult, TriFormulationRelatedRespVO.class);
        if (ObjectUtil.isEmpty(triFormulationRelatedRespVOPageResult) && CollectionUtils.isEmpty(triFormulationRelatedRespVOPageResult.getList())){
            return PageResult.empty();
        }

        for (TriFormulationRelatedRespVO ele : triFormulationRelatedRespVOPageResult.getList()){
            Long triId = ele.getId();
            LambdaQueryWrapper<TriRecipeDO> recipeDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            recipeDOLambdaQueryWrapper.eq(TriRecipeDO::getTriId, triId);
            List<TriRecipeDO> triRecipeList = triRecipeMapper.selectList(recipeDOLambdaQueryWrapper);
            List<TriRecipeRespVO> recipeRespVOList = BeanUtils.toBean(triRecipeList, TriRecipeRespVO.class);
            if (CollectionUtil.isNotEmpty(recipeRespVOList)){
                recipeRespVOList.forEach(vo -> vo.setRelatedRecipeId(vo.getRecipeId()));
            }
            ele.setRecipeList(recipeRespVOList);
        }
        return triFormulationRelatedRespVOPageResult;
    }

    @Override
    public void delTriFormulationList(Long id) {
        triFormulationMapper.deleteById(id);
    }

    @Override
    public void batchInsert(List<TriRecipeMaterialSelectReqVO> selectReqVOS) {
        if (CollectionUtil.isNotEmpty(selectReqVOS)){
            Long triId = selectReqVOS.get(0).getTriId();
            triRecipeMapper.deleteRecipe(triId);
            triRecipeMapper.insertBatch(BeanUtils.toBean(selectReqVOS, TriRecipeDO.class));
            String materials = "tri_id=" + triId ;
            // 记录操作日志上下文
            LogRecordContext.putVariable("materials", materials);
        }
    }

    @Override
    public TriFormulationDO insertTriFormulation(TriRecipeInsertOrUpdateReqVO triRecipeReqVO) {
        String commodityName = triRecipeReqVO.getTriCommodityName();
        LambdaQueryWrapper<TriFormulationDO> wrapper =  new LambdaQueryWrapper<>();
        wrapper.eq(TriFormulationDO::getTriCommodityName, commodityName);
        wrapper.eq(TriFormulationDO::getTriType, triRecipeReqVO.getTriType());
        List<TriFormulationDO> triFormulationDOS = triFormulationMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(triFormulationDOS)) {
            throw exception(TRI_RECIPE_COMMODITY_NAME_ERROR);
        }
        TriFormulationDO bean = BeanUtils.toBean(triRecipeReqVO, TriFormulationDO.class);
        triFormulationMapper.insert(bean);
        return bean;
    }

    @Override
    public void delTriFormulation(Long triId) {
        // 删除三方商品表
        triFormulationMapper.deleteById(triId);
        LambdaQueryWrapper<TriRecipeDO> recipeDOWrapper = new LambdaQueryWrapper<>();
        recipeDOWrapper.eq(TriRecipeDO::getTriId, triId);
        // 删除三方配方表
        triRecipeMapper.delete(recipeDOWrapper);
        LambdaQueryWrapper<TriRecipeMaterialDO> recipeMaterialDOWrapper = new LambdaQueryWrapper<>();
        recipeMaterialDOWrapper.eq(TriRecipeMaterialDO::getTriId, triId);
        // 删除三方取消选中原料表
        triCommodityRecipeMaterialMapper.delete(recipeMaterialDOWrapper);
    }

}
