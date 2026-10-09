package com.htyoudao.youdao.module.commodity.service.storeGroup;



import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCGroupUpdateReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityStoreGroupMapper;
import com.htyoudao.youdao.module.commodity.util.TruncateTableUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.commodity.enums.ErrorCodeConstants.STORE_GROUP_NO_HAVE;

/**
 * <p>
 * 门店下商品的套餐分组表 服务实现类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Service
@Slf4j
public class CommodityStoreGroupServiceImpl extends ServiceImpl<CommodityStoreGroupMapper, CommodityStoreGroup>  implements ICommodityStoreGroupService {

    @Resource
    private CommodityStoreGroupMapper commodityStoreGroupMapper;

    @Autowired
    private TruncateTableUtil truncateTableUtil;


    @Override
    public void deleteSpuByStoreId(Long storeId) {
        LambdaQueryWrapper<CommodityStoreGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreGroup::getStoreId, storeId);
        commodityStoreGroupMapper.delete(queryWrapper);
    }

    @Override
    public void deleteAllStoreSpu() {

        try {

            truncateTableUtil.safeTruncateTable("commodity_store_group");



        } catch (SQLException e) {
            log.error("清空分类表失败", e);
            throw new RuntimeException("全表清理失败", e);
        }

    }

    @Override
    public void deletBySpuIds(List<Long> spuIds) {
        LambdaQueryWrapper<CommodityStoreGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityStoreGroup::getCommodityStoreSpuId, spuIds);
        commodityStoreGroupMapper.delete(queryWrapper);
    }

    @Override
    public List<CommodityStoreGroup> selectByGroupIdsAndStoreId(List<Long> groupIds, Long storeId) {
        LambdaQueryWrapper<CommodityStoreGroup> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.in(CommodityStoreGroup::getCommodityStoreGroupId, groupIds);
        groupLambdaQueryWrapper.eq(CommodityStoreGroup::getStoreId, storeId);
        //找到门店下包含这个子品的分组
        return commodityStoreGroupMapper.selectList(groupLambdaQueryWrapper);
    }

    @Override
    public List<CommodityStoreGroup> selectBySpuId(Long storeId, Long commodityStoreSpuId) {
        LambdaQueryWrapper<CommodityStoreGroup> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.eq(CommodityStoreGroup::getStoreId, storeId);
        groupLambdaQueryWrapper.eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpuId);

        return commodityStoreGroupMapper.selectList(groupLambdaQueryWrapper);
    }

    @Override
    public CommodityStoreGroup getOne(Long commodityStoreGroupId) {
        return commodityStoreGroupMapper.selectById(commodityStoreGroupId);
    }

    @Override
    public void deleteByLambda(LambdaQueryWrapper<CommodityStoreGroup> eq) {
        commodityStoreGroupMapper.delete(eq);
    }


    @Override
    public List<CommodityStoreGroup> selectBySpuIds(List<Long> spuIds) {
        LambdaQueryWrapper<CommodityStoreGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityStoreGroup::getCommodityStoreSpuId, spuIds);
        return commodityStoreGroupMapper.selectList(queryWrapper);
    }

    /**、
     * 点餐机修改分组
     * @param reqVO
     */
    @Override
    public void updateByDc(DCSpuUpdateReqVO reqVO) {
        if(ObjectUtil.isEmpty(reqVO.getGroupList())){
            throw exception(STORE_GROUP_NO_HAVE);
        }
        for (DCGroupUpdateReqVO dcGroupUpdateReqVO : reqVO.getGroupList()) {
            LambdaUpdateWrapper<CommodityStoreGroup> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommodityStoreGroup::getCommodityStoreGroupId,dcGroupUpdateReqVO.getCommodityStoreGroupId());
            updateWrapper.set(CommodityStoreGroup::getChooseMany, dcGroupUpdateReqVO.getChooseMany());
            updateWrapper.set(CommodityStoreGroup::getUpdateTime,new Date());
            updateWrapper.set(CommodityStoreGroup::getUpdater,reqVO.getLoginUserId());
            commodityStoreGroupMapper.update(updateWrapper);
        }
    }

    @Override
    public void delbySpuId(Long commodityStoreSpuId) {
        LambdaQueryWrapper<CommodityStoreGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreGroup::getCommodityStoreSpuId, commodityStoreSpuId);
        commodityStoreGroupMapper.delete(queryWrapper);
    }
}
