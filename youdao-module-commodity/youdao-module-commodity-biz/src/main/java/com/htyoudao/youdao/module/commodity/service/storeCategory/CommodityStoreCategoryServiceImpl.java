package com.htyoudao.youdao.module.commodity.service.storeCategory;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CategoryStoreSortVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO.CommodityStoreCategoryRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.StoreCategoryUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreCategoryUpReqVO;
import com.htyoudao.youdao.module.commodity.convert.CommodityConvertor;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dto.CategoryDto;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreCategoryMapper;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreSpuMapper;
import com.htyoudao.youdao.module.commodity.dal.redis.CommodityStoreRedisDao;
import com.htyoudao.youdao.module.commodity.util.DateTimeSortUtil;
import com.htyoudao.youdao.module.commodity.util.TruncateTableUtil;
import jakarta.annotation.Resource;

import java.sql.SQLException;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.STORE_SPU_ONLY_ONE_REQUIRED_GROUP_ALLOWED;


/**
 * <p>
 * 门店下商品分类表 服务实现类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Service
@Slf4j
public class CommodityStoreCategoryServiceImpl implements ICommodityStoreCategoryService {

    @Resource
    private CommodityStoreCategoryMapper commodityStoreCategoryMapper;

    @Resource
    private CommodityStoreRedisDao storeRedisDao;

    @Resource
    private CommodityStoreSpuMapper commodityStoreSpuMapper;

    @Autowired
    private TruncateTableUtil truncateTableUtil;





    @Override
    public void deleteSpuByStoreId(Long storeId) {
        LambdaQueryWrapper<CommodityStoreCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(CommodityStoreCategory::getStoreId, storeId);
        commodityStoreCategoryMapper.delete(categoryLambdaQueryWrapper);
    }

    @Override
    public void deleteAllStoreSpu() {
     /*   Long businessId = BusinessContextHolder.getBusinessId();

        LambdaQueryWrapper<CommodityStoreCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(CommodityStoreCategory::getBusinessId, businessId);
        int deleteCount = batchDeleteUtil.safeBatchDelete(
                commodityStoreCategoryMapper,
                categoryLambdaQueryWrapper,
                CommodityStoreCategory::getCommodityStoreCategoryId // 替换为你实体的主键getter方法
        );
*/

        try {

            truncateTableUtil.safeTruncateTable("commodity_store_category");



        } catch (SQLException e) {
            log.error("清空分类表失败", e);
            throw new RuntimeException("全表清理失败", e);
        }


        log.info("成功批量删除b分类数据");

    }

    @Override
    public CommodityStoreCategory getCategoryById(Long storeCategoryId) {

        return commodityStoreCategoryMapper.selectById(storeCategoryId);
    }

    @Override
    public void deleteById(Long commodityStoreCategoryId) {
        commodityStoreCategoryMapper.deleteById(commodityStoreCategoryId);
    }

    @Override
    public void checkUniqueRequiredCategory(StoreCategoryUpdateReqVO updateReqVO) {
        if(updateReqVO.getType()==1){
            List<CommodityStoreCategory> list =    commodityStoreCategoryMapper.selectList(new LambdaQueryWrapper<CommodityStoreCategory>()
                    .eq(CommodityStoreCategory::getStoreId,updateReqVO.getStoreId())
                    .eq(CommodityStoreCategory::getType,1)
                    .ne(CommodityStoreCategory::getCommodityStoreCategoryId,updateReqVO.getCommodityStoreCategoryId()));
            if(ObjectUtil.isNotEmpty(list) ){
                throw  exception(STORE_SPU_ONLY_ONE_REQUIRED_GROUP_ALLOWED);
            }
        }

    }

    @Override
    public void updateStoreCategory(CommodityStoreCategoryUpReqVO commodityStoreCategoryUpReqVO) {
        CommodityStoreCategory commodityStoreCategory = new CommodityStoreCategory();
        BeanUtils.copyProperties(commodityStoreCategoryUpReqVO,commodityStoreCategory);
        commodityStoreCategoryMapper.updateById(commodityStoreCategory);
    }

    @Override
    @DataPermission(enable = false) // 不开启数据权限 异步调用场景会出现问题
    public Long synchronizeSingle(CommodityStoreCategory category) {
//
//        //2025.3.31.分组只能存在一个下单必选分组异常:ab两个分组 a设为了必选 同步给门店后b又设置为了必选 同步给门店 生效的是b分组
//        if (Objects.equals(category.getType(), CommodityConstant.CATEGORY_REQUIRED_TYPE)) {
//            LambdaUpdateWrapper<CommodityStoreCategory> updateWrapper = new LambdaUpdateWrapper<>();
//            updateWrapper.set(CommodityStoreCategory::getType, CommodityConstant.CATEGORY_NOT_REQUIRED_TYPE);
//            updateWrapper.eq(CommodityStoreCategory::getStoreId, category.getStoreId());
//            commodityStoreCategoryMapper.update(updateWrapper);
//        }


        //构建category,门店分类不自增插入之前需要查询当前门店是否存在该分类
        LambdaQueryWrapper<CommodityStoreCategory> storeCategoryQueryWrapper = new LambdaQueryWrapper<>();
        storeCategoryQueryWrapper.eq(CommodityStoreCategory::getCommodityStorePrimitiveCategoryId, category.getCommodityStorePrimitiveCategoryId());
        storeCategoryQueryWrapper.eq(CommodityStoreCategory::getStoreId, category.getStoreId());
        storeCategoryQueryWrapper.last("limit 1");
        CommodityStoreCategory oldCategoryStoreEntity = commodityStoreCategoryMapper.selectOne(storeCategoryQueryWrapper);
        //转换为门店分类对象

        if (ObjectUtil.isNull(oldCategoryStoreEntity)) {
            category.setCommodityStoreCategoryId(null);
            commodityStoreCategoryMapper.insert(category);
        } else {
            category.setCommodityStoreCategoryId(oldCategoryStoreEntity.getCommodityStoreCategoryId());
            //如果之前门店存在所属分组 分组状态不做变更
            category.setCommodityStoreCategoryStatus(oldCategoryStoreEntity.getCommodityStoreCategoryStatus());
            commodityStoreCategoryMapper.updateById(category);
        }

        return category.getCommodityStoreCategoryId();
    }

    @Override
    public List<CommodityStoreCategoryRespVO> getList(Long storeId) {
        List<CommodityStoreCategoryRespVO> commodityStoreCategoryRespVOList = new ArrayList<>();
        LambdaQueryWrapper<CommodityStoreCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(CommodityStoreCategory::getStoreId, storeId);
        categoryLambdaQueryWrapper.orderByAsc(CommodityStoreCategory::getCommodityStoreCategorySort);
        List<CommodityStoreCategory> list = commodityStoreCategoryMapper.selectList(categoryLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            for (CommodityStoreCategory commodityStoreCategory : list) {
                CommodityStoreCategoryRespVO commodityStoreCategoryRespVO = new CommodityStoreCategoryRespVO();
                BeanUtils.copyProperties(commodityStoreCategory, commodityStoreCategoryRespVO);
                commodityStoreCategoryRespVOList.add(commodityStoreCategoryRespVO);
            }
            DateTimeSortUtil.sortDateTime(commodityStoreCategoryRespVOList);

            List<CommodityStoreCategoryRespVO> categoryListUp = new ArrayList<>();
            List<CommodityStoreCategoryRespVO> categoryListDown = new ArrayList<>();


            //如果不在分时置顶时间段内，不显示分时置顶图标
            for (CommodityStoreCategoryRespVO category : commodityStoreCategoryRespVOList) {
                if (!category.getIsUp()) {
                    category.setTimeSharingTopping(2);
                    categoryListDown.add(category);
                } else {
                    categoryListUp.add(category);
                }

            }
            categoryListUp.addAll(categoryListDown);
            commodityStoreCategoryRespVOList = categoryListUp;

        }


        return commodityStoreCategoryRespVOList;
    }

    @Override
    public CommodityStoreCategory selectById(Long commodityStoreCategoryId) {

        return commodityStoreCategoryMapper.selectById(commodityStoreCategoryId);
    }

    @Override
    public void sortCategory(List<CategoryStoreSortVO> categorySortDTOList) {
        for (CategoryStoreSortVO categorySortDTO : categorySortDTOList) {

            CommodityStoreCategory category = commodityStoreCategoryMapper.selectById(categorySortDTO.getCategoryId());
            if (ObjectUtil.isNull(category)) {
                continue;
            }

            LambdaUpdateWrapper<CommodityStoreCategory> categoryLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            categoryLambdaUpdateWrapper.eq(CommodityStoreCategory::getCommodityStoreCategoryId, categorySortDTO.getCategoryId());
            categoryLambdaUpdateWrapper.set(CommodityStoreCategory::getCommodityStoreCategorySort, categorySortDTO.getSort());
            commodityStoreCategoryMapper.update(categoryLambdaUpdateWrapper);

            category.setCommodityStoreCategorySort(categorySortDTO.getSort());
            CategoryDto categoryDto = CommodityConvertor.convertDoToCategoryDTO(category);
            //更新缓存
            storeRedisDao.saveOrUpdateCategory(categorySortDTO.getStoreId(),categorySortDTO.getCategoryId(), categoryDto);
        }


    }

    @Override
    public List<CommodityStoreCategoryRespVO> getListTwo(Long storeId) {
        List<CommodityStoreCategoryRespVO> commodityStoreCategoryRespVOList = new ArrayList<>();
        LambdaQueryWrapper<CommodityStoreCategory> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(CommodityStoreCategory::getStoreId, storeId);
        categoryLambdaQueryWrapper.orderByAsc(CommodityStoreCategory::getCommodityStoreCategorySort);
        List<CommodityStoreCategory> list = commodityStoreCategoryMapper.selectList(categoryLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(list)) {
            for (CommodityStoreCategory commodityStoreCategory : list) {
                CommodityStoreCategoryRespVO commodityStoreCategoryRespVO = new CommodityStoreCategoryRespVO();
                BeanUtils.copyProperties(commodityStoreCategory, commodityStoreCategoryRespVO);

                LambdaQueryWrapper<CommodityStoreSpu> spusLambdaQueryWrapper = new LambdaQueryWrapper<>();
                spusLambdaQueryWrapper.eq(CommodityStoreSpu::getCommodityStoreCategoryId, commodityStoreCategory.getCommodityStoreCategoryId());
//                spusLambdaQueryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);

//                spusLambdaQueryWrapper.orderByAsc(CommodityStoreSpu::getCommodityStoreSpuSort);
                List<CommodityStoreSpu> storeSpuList = commodityStoreSpuMapper.selectList(spusLambdaQueryWrapper);
                commodityStoreCategoryRespVO.setCommodityQuantity(storeSpuList.size());
                commodityStoreCategoryRespVOList.add(commodityStoreCategoryRespVO);
            }
            DateTimeSortUtil.sortDateTime(commodityStoreCategoryRespVOList);

//            List<CommodityStoreCategoryRespVO> categoryListUp = new ArrayList<>();
//            List<CommodityStoreCategoryRespVO> categoryListDown = new ArrayList<>();



//            //如果不在分时置顶时间段内，不显示分时置顶图标
//            for (CommodityStoreCategoryRespVO category : commodityStoreCategoryRespVOList) {
//                if (!category.getIsUp()) {
//                    category.setTimeSharingTopping(2);
//                    categoryListDown.add(category);
//                } else {
//                    categoryListUp.add(category);
//                }
//
//            }
//            categoryListUp.addAll(categoryListDown);
//            commodityStoreCategoryRespVOList = categoryListUp;

        }


        return commodityStoreCategoryRespVOList;
    }
}
