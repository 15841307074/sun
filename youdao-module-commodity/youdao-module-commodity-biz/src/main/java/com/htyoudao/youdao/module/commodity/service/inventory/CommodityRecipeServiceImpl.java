package com.htyoudao.youdao.module.commodity.service.inventory;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeMaterialDO;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityRecipeMaterialMapper;
import com.htyoudao.youdao.module.commodity.service.sku.ICommoditySkusService;
import com.htyoudao.youdao.module.commodity.service.spus.ICommoditySpusService;
import com.htyoudao.youdao.module.commodity.util.redis.RedisCache;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.commodity.enums.LogRecordConstans.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Service
@Slf4j
public class CommodityRecipeServiceImpl implements ICommodityRecipeService {

    @Resource
    private ICommoditySkusService commoditySkusService;

    @Resource
    private CommodityRecipeMapper commodityRecipeMapper;

    @Resource
    private ICommoditySpusService commoditySpusService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CommodityRecipeMaterialMapper commodityRecipeMaterialMapper;

    @Override
    public List<RecipeCommoditySkuRespVO> selectByCommodityId(Long commodityId) {
        List<CommoditySkus> commoditySkuses = commoditySkusService.selectBySpuId(commodityId);
        List<RecipeCommoditySkuRespVO> result = BeanUtils.toBean(commoditySkuses, RecipeCommoditySkuRespVO.class);
        for (RecipeCommoditySkuRespVO recipeCommoditySkuRespVO : result) {
            // 获取规格ID
            Long skuId = recipeCommoditySkuRespVO.getSkuId();
            LambdaQueryWrapper<RecipeDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecipeDO::getCommodityId, commodityId);
            wrapper.eq(RecipeDO::getSkuId, recipeCommoditySkuRespVO.getSkuId());
            wrapper.orderBy(true, false, RecipeDO::getMaterialsTypeName);
            List<RecipeDO> recipeDOS = commodityRecipeMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(recipeDOS)){
                recipeCommoditySkuRespVO.setRecipeList(BeanUtils.toBean(recipeDOS, RecipeRespVO.class));
            }
        }
        return result;
    }

    @Override
    public List<RecipeCommoditySkuRespVO> selectRelatedByCommodityId(Long commodityId) {
        List<CommoditySkus> commoditySkuses = commoditySkusService.selectBySpuId(commodityId);
        List<RecipeCommoditySkuRespVO> result = BeanUtils.toBean(commoditySkuses, RecipeCommoditySkuRespVO.class);
        for (RecipeCommoditySkuRespVO recipeCommoditySkuRespVO : result) {
            // 获取规格ID
            Long skuId = recipeCommoditySkuRespVO.getSkuId();
            LambdaQueryWrapper<RecipeDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecipeDO::getCommodityId, commodityId);
            wrapper.eq(RecipeDO::getSkuId, recipeCommoditySkuRespVO.getSkuId());
            wrapper.orderBy(true, false, RecipeDO::getMaterialsTypeName);
            List<RecipeDO> recipeDOS = commodityRecipeMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(recipeDOS)){
                List<RecipeRespVO> recipeRespVOList = BeanUtils.toBean(recipeDOS, RecipeRespVO.class);
                recipeRespVOList.forEach(vo -> vo.setRelatedRecipeId(vo.getRecipeId()));
                recipeCommoditySkuRespVO.setRecipeList(BeanUtils.toBean(recipeRespVOList, RecipeRespVO.class));
            }
        }
        return result;
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_TRUNCATE_REPLACE_MATERIALS, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_TRUNCATE_REPLACE_MATERIALS_SUCCESS)
    public void truncRecipeMaterial(Long commodityId, Long skuId, Long materialsId) {
        LambdaQueryWrapper<RecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeMaterialDO::getCommodityId, commodityId);
        wrapper.eq(RecipeMaterialDO::getSkuId, skuId);
        wrapper.eq(RecipeMaterialDO::getReplaceMaterialsId, materialsId);
        commodityRecipeMaterialMapper.delete(wrapper);
        LambdaQueryWrapper<RecipeDO> recipeWrapper = new LambdaQueryWrapper<>();
        recipeWrapper.eq(RecipeDO::getCommodityId, commodityId);
        recipeWrapper.eq(RecipeDO::getSkuId, skuId);
        recipeWrapper.eq(RecipeDO::getMaterialsId, materialsId);
        commodityRecipeMapper.delete(recipeWrapper);

        // 查询删除完毕 此规格下是否还存在配方
        LambdaQueryWrapper<RecipeDO> recipeQueryWrapper = new LambdaQueryWrapper<>();
        recipeQueryWrapper.eq(RecipeDO::getCommodityId, commodityId);
        recipeQueryWrapper.eq(RecipeDO::getSkuId, skuId);
        Long l = commodityRecipeMapper.selectCount(recipeQueryWrapper);

        if (l == null || l == 0){
            // 更新连锁商品单品规格配方设置状态
            commoditySpusService.updateCommoditySkuFlag(commodityId, 0);
        }
        String truncRecipeMaterialIds = "commodityId=" + commodityId + ", skuId=" + skuId + ", materialsId=" + materialsId;
        // 记录操作日志上下文
        LogRecordContext.putVariable("truncRecipeMaterial", truncRecipeMaterialIds);
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_INSERT_RECIPE, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_INSERT_RECIPE_SUCCESS)
    public void insertRecipe(List<RecipeReqVO> recipeReqVOList, Long commodityId) {
        if(commodityId == null){
            throw exception(RECIPE_COMMODITY_ID_ERROR);
        }

        RLock lock = redissonClient.getLock("updateSpu:" + commodityId);

        if (lock.isLocked()){
            throw exception(RECIPE_COMMODITY_CHANGE);
        }

        if (CollectionUtil.isEmpty(recipeReqVOList)) {
            throw exception(RECIPE_LIST_ERROR);
        }
        // 物理删除历史记录
        commodityRecipeMapper.deleteRecipe(commodityId);
        // 批量新增
        List<RecipeDO> recipes = BeanUtils.toBean(recipeReqVOList, RecipeDO.class);

        commodityRecipeMapper.insertBatch(recipes);
        //取出关联配方
        List<RecipeDO> relatedRecipes = Optional.ofNullable(recipes)
                .orElse(Collections.emptyList())
                .stream()
                .filter(recipe -> null != recipe.getRelatedRecipeId())
                .toList();
        //
        if (CollectionUtil.isNotEmpty(relatedRecipes)) {
            //执行关联复制 补全原料信息
            for (RecipeDO recipe : relatedRecipes) {
                Long relatedRecipeId = recipe.getRelatedRecipeId();
                LambdaQueryWrapper<RecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RecipeMaterialDO::getRecipeId, relatedRecipeId);
                List<RecipeMaterialDO> recipeMaterialDOS = commodityRecipeMaterialMapper.selectList(wrapper);
                Optional.ofNullable(recipeMaterialDOS)
                        .filter(CollectionUtil::isNotEmpty) // 仅保留非空、非空集合
                        .ifPresent(list -> {
                            // 第一步：修改集合属性（recipeId赋值 + recipeMaterialId置空）
                            list.forEach(rm -> {
                                if (rm != null) {
                                    rm.setRecipeId(recipe.getRecipeId());
                                    rm.setCommodityId(recipe.getCommodityId());
                                    rm.setSkuId(recipe.getSkuId());
                                    rm.setRecipeMaterialId(null);
                                }
                            });
                            // 第二步：批量插入（无需再次判空，已通过filter过滤）
                            commodityRecipeMaterialMapper.insertBatch(list);
                        });

            }
        }

        // 获取新增或者修改中已经设置配方的规格数量
        long skuIdCount = countDistinctSkuId(recipeReqVOList);

        // 查询商品有多少规格
        long commoditySkuIdCount = commoditySkusService.selectSkuIdCountByCommodityId(commodityId);

        // 标记商品规格配置是否全部设置完毕
        int skuFlag = commoditySkuIdCount - skuIdCount == 0 ? 1 : 0;
        // 更新连锁商品单品规格配方设置状态
        commoditySpusService.updateCommoditySkuFlag(commodityId, skuFlag);

        String log = "commodityId=" + commodityId ;
        // 记录操作日志上下文
        LogRecordContext.putVariable("commodityId", log);
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_INSERT_MATERIALS, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_INSERT_MATERIALS_SUCCESS)
    public void batchInsert(List<RecipeMaterialSelectReqVO> recipeMaterialSelectReqVOList) {
        if (CollectionUtil.isNotEmpty(recipeMaterialSelectReqVOList)){
            Long skuId = recipeMaterialSelectReqVOList.get(0).getSkuId();
            Long commodityId = recipeMaterialSelectReqVOList.get(0).getCommodityId();
            commodityRecipeMapper.deleteSkuRecipe(skuId,commodityId);
            commodityRecipeMapper.insertBatch(BeanUtils.toBean(recipeMaterialSelectReqVOList, RecipeDO.class));
            String materials = "commodityId=" + commodityId + ", skuId=" + skuId ;
            // 记录操作日志上下文
            LogRecordContext.putVariable("materials", materials);
        }
    }

    @Override
    @LogRecord(type = COMMODITY_RECIPE_MANAGER, subType = COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS, bizNo = "1", success = COMMODITY_RECIPE_MANAGER_INSERT_UN_SELECTED_REPLACE_MATERIALS_SUCCESS)
    public void saveUnSelectedRecipeMaterial(List<RecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList) {
        if (CollectionUtil.isNotEmpty(recipeMaterialUnSelectedReqVOList)){
            Long commodityId = recipeMaterialUnSelectedReqVOList.get(0).getCommodityId();
            Long skuId = recipeMaterialUnSelectedReqVOList.get(0).getSkuId();
            Long replaceMaterialsId = recipeMaterialUnSelectedReqVOList.get(0).getReplaceMaterialsId();
            // 查询替换的配方ID
            LambdaQueryWrapper<RecipeDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecipeDO::getCommodityId, commodityId);
            wrapper.eq(RecipeDO::getSkuId, skuId);
            wrapper.eq(RecipeDO::getMaterialsId, replaceMaterialsId);
            RecipeDO recipeDO = commodityRecipeMapper.selectOne(wrapper);
            if (recipeDO != null && recipeDO.getRecipeId() != null){
                for (RecipeMaterialUnSelectedReqVO reqVO : recipeMaterialUnSelectedReqVOList) {
                    // 确保对象不为null，避免操作null对象
                    if (reqVO != null) {
                        reqVO.setRecipeId(recipeDO.getRecipeId());
                    }
                }
            }
            commodityRecipeMaterialMapper.deleteSkuRecipeMaterial(commodityId, skuId, replaceMaterialsId);
            commodityRecipeMaterialMapper.insertBatch(BeanUtils.toBean(recipeMaterialUnSelectedReqVOList, RecipeMaterialDO.class));
            String materials = "commodityId=" + commodityId + ", skuId=" + skuId + ", replaceMaterialsId=" + replaceMaterialsId;
            // 记录操作日志上下文
            LogRecordContext.putVariable("materials", materials);
        }
    }

    @Override
    public List<RecipeMaterialUnSelectedRespVO> getUnSelectedRecipeMaterials(Long commodityId, Long skuId, Long replaceMaterialsId) {
        LambdaQueryWrapper<RecipeMaterialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeMaterialDO::getCommodityId, commodityId);
        wrapper.eq(RecipeMaterialDO::getSkuId, skuId);
        wrapper.eq(RecipeMaterialDO::getReplaceMaterialsId, replaceMaterialsId);

        List<RecipeMaterialDO> recipeMaterialDOS = commodityRecipeMaterialMapper.selectList(wrapper);

        return BeanUtils.toBean(recipeMaterialDOS, RecipeMaterialUnSelectedRespVO.class);
    }

    // 统计不同 skuId 的数量
    public long countDistinctSkuId(List<RecipeReqVO> recipeReqVOList) {
        // 1. 提取列表中所有的 skuId → 2. 过滤掉 null 值（避免空指针）→ 3. 转为 Set 去重 → 4. 获取 Set 大小
        return recipeReqVOList.stream()
                .map(RecipeReqVO::getSkuId)  // 提取每个 RecipeReqVO 的 skuId
                .filter(skuId -> skuId != null)  // 过滤 null（若业务允许 skuId 为 null，可删除此步）
                .collect(Collectors.toSet())  // 去重（Set 不允许重复元素）
                .size();  // 统计去重后的数量
    }
}
