package com.htyoudao.youdao.module.member.service.crowd.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.member.constant.CustomCrowdConstant;
import com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.*;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdStoreDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdTagDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.CrowdStoreMapper;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.CrowdTagMapper;
import com.htyoudao.youdao.module.member.dal.mysql.crowd.CustomCrowdMapper;
import com.htyoudao.youdao.module.member.service.crowd.CustomCrowdService;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.member.api.enums.LogRecordConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;

import java.util.*;

/**
 * 自定义人群 Service 实现类
 *
 * @author dht
 */
@Service
@Validated
public class CustomCrowdServiceImpl implements CustomCrowdService {

    @Resource
    private CustomCrowdMapper customCrowdMapper;

    @Resource
    private CrowdStoreMapper crowdStoreMapper;

    @Resource
    private CrowdTagMapper crowdTagMapper;

    @DubboReference
    private GoodCouponApi goodCouponApi;

    @Override
    @LogRecord(type = MEMBER_CUSTOM_CROWDS_TYPE, subType = MEMBER_CUSTOM_CROWDS_CREATE_SUB_TYPE, bizNo = "{{1}}", success = MEMBER_CUSTOM_CROWDS_CREATE_SUCCESS)
    public Long createCrowd(CustomCrowdSaveReqVO createReqVO) {

        long l = customCrowdMapper.selectCount();
        if(l >= CustomCrowdConstant.CUSTOM_CROWD_TOTAL_NUM){
            throw exception(GOODCOUPON_CROWD_TOTAL_ERROR);
        }
        // 插入
        CustomCrowdDO crowd = BeanUtils.toBean(createReqVO, CustomCrowdDO.class);
        customCrowdMapper.insert(crowd);
        // 绑定门店
        if(crowd.getBelongStore().equals(1)){
            List<CrowdStoreSaveReqVO> crowdStoreSaveReqVOList = createReqVO.getCrowdStores();
            List<CrowdStoreDO> bean = BeanUtils.toBean(crowdStoreSaveReqVOList, CrowdStoreDO.class);
            bean.forEach(crowdStoreDO -> crowdStoreDO.setCrowdId(crowd.getId()));
            crowdStoreMapper.insertBatch(bean);
        }
        // 绑定标签
        if(crowd.getMemberTag().equals(1)){
            List<CrowdTagSaveReqVO> crowdTagSaveReqVOList = createReqVO.getCrowdTags();
            List<CrowdTagDO> bean = BeanUtils.toBean(crowdTagSaveReqVOList, CrowdTagDO.class);
            bean.forEach(crowdTagDO -> crowdTagDO.setCrowdId(crowd.getId()));
            crowdTagMapper.insertBatch(bean);
        }
        // 返回
        LogRecordContext.putVariable("createReqVO", createReqVO);
        return crowd.getId();
    }

    @Override
    @LogRecord(type = MEMBER_CUSTOM_CROWDS_TYPE, subType = MEMBER_CUSTOM_CROWDS_UPDATE_SUB_TYPE, bizNo = "{{1}}", success = MEMBER_CUSTOM_CROWDS_UPDATE_SUCCESS)
    public void updateCrowd(CustomCrowdSaveReqVO updateReqVO) {
        // 校验存在
        validateCrowdExists(updateReqVO.getId());
        // 更新
        CustomCrowdDO updateObj = BeanUtils.toBean(updateReqVO, CustomCrowdDO.class);
        customCrowdMapper.updateById(updateObj);
        // 绑定门店
        if(updateReqVO.getBelongStore().equals(1)){
            crowdStoreMapper.delete(new QueryWrapper<CrowdStoreDO>().eq("crowd_id",updateReqVO.getId()));
            List<CrowdStoreSaveReqVO> crowdStoreSaveReqVOList = updateReqVO.getCrowdStores();
            List<CrowdStoreDO> bean = BeanUtils.toBean(crowdStoreSaveReqVOList, CrowdStoreDO.class);
            bean.forEach(crowdStoreDO -> crowdStoreDO.setCrowdId(updateReqVO.getId()));
            crowdStoreMapper.insertBatch(bean);
        }
        // 绑定标签
        if(updateReqVO.getMemberTag().equals(1)){
            crowdTagMapper.delete(new QueryWrapper<CrowdTagDO>().eq("crowd_id",updateReqVO.getId()));
            List<CrowdTagSaveReqVO> crowdTagSaveReqVOList = updateReqVO.getCrowdTags();
            List<CrowdTagDO> bean = BeanUtils.toBean(crowdTagSaveReqVOList, CrowdTagDO.class);
            bean.forEach(crowdTagDO -> crowdTagDO.setCrowdId(updateReqVO.getId()));
            crowdTagMapper.insertBatch(bean);
        }
        LogRecordContext.putVariable("updateReqVO", updateReqVO);
    }

    @Override
    @LogRecord(type = MEMBER_CUSTOM_CROWDS_TYPE, subType = MEMBER_CUSTOM_CROWDS_DELETE_SUB_TYPE, bizNo = "{{1}}", success = MEMBER_CUSTOM_CROWDS_DELETE_SUCCESS)
    public void deleteCrowd(Long id) {
        // 校验存在
        validateCrowdExists(id);

        Long l = goodCouponApi.countByCrowdId(id);
        if(l > 0){
            throw exception(GOODCOUPON_CROWD_EXISTS);
        }
        LogRecordContext.putVariable("id", id);
        // 删除
        customCrowdMapper.deleteById(id);
    }

    @Override
    public void deleteCrowdListByIds(List<Long> ids) {
        // 校验存在
        validateCrowdExists(ids);
        // 删除
        customCrowdMapper.deleteByIds(ids);
    }

    @Override
    public List<CustomCrowdDO> getAll(String busId) {
        return customCrowdMapper.selectList(CustomCrowdDO::getBusinessId, busId);
    }

    private void validateCrowdExists(List<Long> ids) {
        List<CustomCrowdDO> list = customCrowdMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(CROWD_NOT_EXISTS);
        }
    }

    private void validateCrowdExists(Long id) {
        if (customCrowdMapper.selectById(id) == null) {
            throw exception(CROWD_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<CustomCrowdPageRespVO> page(CustomCrowdPageReqVO reqVO) {
        PageResult<CustomCrowdDO> list = customCrowdMapper.selectPage(reqVO);
        return BeanUtils.toBean(list, CustomCrowdPageRespVO.class);
    }

    @Override
    public CustomCrowdRespVO getById(Long id) {
        CustomCrowdDO crowd = customCrowdMapper.selectById(id);
        if(crowd == null){
            return null;
        }
        CustomCrowdRespVO bean = BeanUtils.toBean(crowd, CustomCrowdRespVO.class);
        List<CrowdStoreDO> crowdStoreDOList = crowdStoreMapper.selectList(new QueryWrapper<CrowdStoreDO>().eq("crowd_id",id));
        if(crowd.getBelongStore().equals(1)){
            bean.setCrowdStores(BeanUtils.toBean(crowdStoreDOList, CrowdStoreRespVO.class));
        }else {
            bean.setCrowdStores(List.of());
        }
        List<CrowdTagDO> crowdTagDOList = crowdTagMapper.selectList(new QueryWrapper<CrowdTagDO>().eq("crowd_id",id));
        if(crowd.getMemberTag().equals(1)){
            bean.setCrowdTags(BeanUtils.toBean(crowdTagDOList, CrowdTagRespVO.class));
        }else {
            bean.setCrowdTags(List.of());
        }
        return bean;
    }

    @Override
    public List<CustomCrowdDropDownRespVO> dropDown() {
        List<CustomCrowdDO> customCrowds = customCrowdMapper.selectList();
        List<CustomCrowdDropDownRespVO> list = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(customCrowds)){
            list = customCrowds.stream().map(item -> {
                CustomCrowdDropDownRespVO result = new CustomCrowdDropDownRespVO();
                result.setId(item.getId());
                result.setCrowdName(item.getCrowdName());
                return result;
            }).toList();
        }
        return list;
    }

    @Override
    public List<CrowdNameDTO> getByIds(List<Long> allCrowd) {
        List<CustomCrowdDO> customCrowds = customCrowdMapper.selectList(new QueryWrapper<CustomCrowdDO>().in("id",allCrowd));
        if(CollectionUtil.isNotEmpty(customCrowds)){
            return customCrowds.stream().map(item -> {
                CrowdNameDTO result = new CrowdNameDTO();
                result.setId(item.getId());
                result.setCrowdName(item.getCrowdName());
                return result;
            }).toList();
        }
        return List.of();

    }
}