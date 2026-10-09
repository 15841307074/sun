package com.htyoudao.youdao.module.commodity.service.scmCommodity;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.config.BusinessProjectMappingConfig;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodity.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion.ScmUnitConversion;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy.ScmCommodity;
import com.htyoudao.youdao.module.commodity.dal.mysql.ScmCommodity.ScmCommodityMapper;
import com.htyoudao.youdao.module.commodity.enums.DeviceType;
import com.htyoudao.youdao.module.commodity.enums.RawMaterialLog;
import com.htyoudao.youdao.module.commodity.service.materialLog.RawMaterialLogService;
import com.htyoudao.youdao.module.commodity.service.scmUnitConversion.ScmUnitConversionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScmCommodityServiceImpl implements ScmCommodityService {


    @Resource
    private ScmCommodityMapper scmCommodityMapper;

    @Resource
    private ScmUnitConversionService scmUnitConversionService;

    @Resource
    private BusinessProjectMappingConfig businessProjectMappingConfig;

    @Resource
    private RawMaterialLogService rawMaterialLogService;

    @Override
    public List<ScmCommodity> selectListByStatiscsNames(List<String> nameList, Integer projectCode,Long warehouseId,int value) {

        LambdaQueryWrapper<ScmCommodity> scmCommodityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        scmCommodityLambdaQueryWrapper.eq(ScmCommodity::getProjectCode, projectCode);
        if (value == DeviceType.PC.getValue()) {
            //按照商品编码和品牌联合去重
            scmCommodityLambdaQueryWrapper.groupBy(ScmCommodity::getCommodityCode, ScmCommodity::getBrandId);
        }else {
            scmCommodityLambdaQueryWrapper.eq(ScmCommodity::getWarehouseId, warehouseId);
        }

        scmCommodityLambdaQueryWrapper.in(ScmCommodity::getStasticsCommodityName, nameList);
        scmCommodityLambdaQueryWrapper.eq(ScmCommodity::getIsDelete,0);


        return scmCommodityMapper.selectList(scmCommodityLambdaQueryWrapper);
    }

    @Override
    public PageResult<ScmCommodityRespVO> selectPage(ScmCommodityPageReqVO scmCommodityPageReqVO,int value) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Integer projectCode = businessProjectMappingConfig.getProjectCode(String.valueOf(businessId));

        PageResult<ScmCommodityRespVO>  pageResult = new PageResult<>();
        List<ScmCommodityRespVO> scmCommodityRespVOList = new ArrayList<>();
        //拿到商品信息
        PageResult<ScmCommodity> scmCommodityPageResult = getScmCommodityPageResult(scmCommodityPageReqVO, value, projectCode);


        pageResult.setTotal(scmCommodityPageResult.getTotal());


        if (scmCommodityPageResult.getTotal()>=1){



            List<ScmCommodity> commodityList = scmCommodityPageResult.getList();
            List<Long> commodityIds = commodityList.stream().map(ScmCommodity::getId).toList();
            List<ScmUnitConversion> scmUnitConversions =  scmUnitConversionService.selectByCommodityIds(commodityIds);

            convertScmCommodity(scmUnitConversions, commodityList, scmCommodityRespVOList);


        }


        pageResult.setList(scmCommodityRespVOList);

        return pageResult;
    }

    @Override
    public List<ScmCommodityRespVO> selectByList(ScmCommodityDataReqVO scmCommodityDataReqVO) {
        Long businessId = BusinessContextHolder.getBusinessId();

        Integer projectCode = businessProjectMappingConfig.getProjectCode(String.valueOf(businessId));

        LambdaQueryWrapper<ScmCommodity> wrapper = new LambdaQueryWrapper<ScmCommodity>()
                .eq(ScmCommodity::getProjectCode, projectCode)
                .eq(ScmCommodity::getIsDelete, 0) // 建议替换为常量
                .orderByDesc(ScmCommodity::getUpdateTime);

        wrapper.groupBy(ScmCommodity::getCommodityCode, ScmCommodity::getBrandId);


        // 商品名称/编码查询（注意 or 条件的范围）
        if (scmCommodityDataReqVO.getCommodityName() != null) {
            wrapper.and(q -> q
                    .eq(ScmCommodity::getCommodityCode, scmCommodityDataReqVO.getCommodityName())
                    .or()
                    .like(ScmCommodity::getCommodityName, scmCommodityDataReqVO.getCommodityName())
            );
        }

        List<ScmCommodity> scmCommodities = scmCommodityMapper.selectList(wrapper);
        if(ObjectUtil.isNotEmpty(scmCommodities)){
            List<ScmCommodityRespVO> list = BeanCopyUtils.copyBeanList(scmCommodities, ScmCommodityRespVO.class);
            return list;
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public ScmCommodityInfoRespVO selectCommodityInfo(ScmCommodityInfoReqVO scmCommodityInfoReqVO, int value) {
        Long businessId = BusinessContextHolder.getBusinessId();

        Integer projectCode = businessProjectMappingConfig.getProjectCode(String.valueOf(businessId));
        ScmCommodity scmCommodityInfoResult = getScmCommodityInfoResult(scmCommodityInfoReqVO, value, projectCode);

        if(scmCommodityInfoResult!=null){
            Long id = scmCommodityInfoResult.getId();
            List<Long> longList = new ArrayList<>();
            longList.add(id);
            List<ScmUnitConversion> scmUnitConversions =  scmUnitConversionService.selectByCommodityIds(longList);
            ScmCommodityInfoRespVO scmCommodityRespVO = new ScmCommodityInfoRespVO();
            convertScmCommodityInfo(scmUnitConversions, scmCommodityInfoResult, scmCommodityRespVO);
            return scmCommodityRespVO;
        }

        return null;
    }

    private static void convertScmCommodity(List<ScmUnitConversion> scmUnitConversions, List<ScmCommodity> commodityList, List<ScmCommodityRespVO> scmCommodityRespVOList) {
        Map<Long, List<ScmUnitConversion>> unitCollect = new HashMap<>();
        if (ObjectUtil.isNotEmpty(scmUnitConversions)) {
            unitCollect =   scmUnitConversions.stream().collect(Collectors.groupingBy(ScmUnitConversion::getCommodityId));
        }


        for (ScmCommodity scmCommodity : commodityList) {
            ScmCommodityRespVO scmCommodityRespVO = new ScmCommodityRespVO();

            BeanUtils.copyProperties(scmCommodity, scmCommodityRespVO);
            //做一个所有单位 list
            List<String> allUnit = new ArrayList<>();
            allUnit.add(scmCommodity.getMinUnit());

            List<ScmUnitConversion> scmUnitConversions1 = unitCollect.get(scmCommodity.getId());
            if (ObjectUtil.isNotEmpty(scmUnitConversions1)) {
                scmCommodityRespVO.setScmUnitConversionList(scmUnitConversions1);
                List<String> list = scmUnitConversions1.stream().map(ScmUnitConversion::getBeforeUnit).toList();
                allUnit.addAll(list);

            }
            scmCommodityRespVO.setAllUnit(allUnit);
            scmCommodityRespVOList.add(scmCommodityRespVO);

        }
    }


    private static void convertScmCommodityInfo(List<ScmUnitConversion> scmUnitConversions, ScmCommodity scmCommodity, ScmCommodityInfoRespVO scmCommodityRespVO) {
        Map<Long, List<ScmUnitConversion>> unitCollect = new HashMap<>();
        if (ObjectUtil.isNotEmpty(scmUnitConversions)) {
            unitCollect =   scmUnitConversions.stream().collect(Collectors.groupingBy(ScmUnitConversion::getCommodityId));
        }

        BeanUtils.copyProperties(scmCommodity, scmCommodityRespVO);
        //做一个所有单位 list
        List<String> allUnit = new ArrayList<>();
        allUnit.add(scmCommodity.getMinUnit());

        List<ScmUnitConversion> scmUnitConversions1 = unitCollect.get(scmCommodity.getId());
        if (ObjectUtil.isNotEmpty(scmUnitConversions1)) {
            scmCommodityRespVO.setScmUnitConversionList(scmUnitConversions1);
            List<String> list = scmUnitConversions1.stream().map(ScmUnitConversion::getBeforeUnit).toList();
            allUnit.addAll(list);

        }
        scmCommodityRespVO.setAllUnit(allUnit);

    }

    private PageResult<ScmCommodity> getScmCommodityPageResult(ScmCommodityPageReqVO scmCommodityPageReqVO, int value, Integer projectCode) {
        LambdaQueryWrapper<ScmCommodity> wrapper = new LambdaQueryWrapper<ScmCommodity>()
                .eq(ScmCommodity::getProjectCode, projectCode)
                .eq(ScmCommodity::getIsDelete, 0) // 建议替换为常量
                .eq(ScmCommodity::getStasticsCommodityName, scmCommodityPageReqVO.getStasticsName())
                .orderByDesc(ScmCommodity::getUpdateTime);

// 根据设备类型添加条件
        if (value == DeviceType.PC.getValue()) {
            wrapper.groupBy(ScmCommodity::getCommodityCode, ScmCommodity::getBrandId);
        } else {
            wrapper.eq(ScmCommodity::getWarehouseId, scmCommodityPageReqVO.getWarehouseId());
        }

// 商品名称/编码查询（注意 or 条件的范围）
        if (scmCommodityPageReqVO.getCommodityName() != null) {
            wrapper.and(q -> q
                    .eq(ScmCommodity::getCommodityCode, scmCommodityPageReqVO.getCommodityName())
                    .or()
                    .like(ScmCommodity::getCommodityName, scmCommodityPageReqVO.getCommodityName())
            );
        }

        PageParam pageParam = new PageParam();
        pageParam.setPageNo(scmCommodityPageReqVO.getPageNo());
        pageParam.setPageSize(scmCommodityPageReqVO.getPageSize());

        PageResult<ScmCommodity> scmCommodityPageResult = scmCommodityMapper.selectPage(pageParam, wrapper);
        return scmCommodityPageResult;
    }



    private ScmCommodity getScmCommodityInfoResult(ScmCommodityInfoReqVO scmCommodityInfoReqVO, int value, Integer projectCode) {

        LambdaQueryWrapperX<ScmCommodity> wrapper = new LambdaQueryWrapperX<ScmCommodity>()
                .eqIfPresent(ScmCommodity::getProjectCode, projectCode)
                .eqIfPresent(ScmCommodity::getStasticsCommodityName, scmCommodityInfoReqVO.getStasticsName())
                .eqIfPresent(ScmCommodity::getWarehouseId, scmCommodityInfoReqVO.getWarehouseId())
                .eqIfPresent(ScmCommodity::getCommodityCode,scmCommodityInfoReqVO.getCommodityCode());

        List<ScmCommodity> scmCommodities = scmCommodityMapper.selectList(wrapper);
        if(ObjectUtil.isEmpty(scmCommodities)){

            RawMaterialLogDO rawMaterialLogDO = new RawMaterialLogDO();
            rawMaterialLogDO.setLogType(RawMaterialLog.WAREHOUSE_NOT_MATERIAL.getType());
            rawMaterialLogDO.setStoreId(scmCommodityInfoReqVO.getStoreId());
            rawMaterialLogDO.setStatus(1);
            rawMaterialLogService.asyncSaveLog(rawMaterialLogDO);





            LambdaQueryWrapperX<ScmCommodity> commodityLambdaQueryWrapperX = new LambdaQueryWrapperX<ScmCommodity>()
                    .eqIfPresent(ScmCommodity::getProjectCode, projectCode)
                    .eqIfPresent(ScmCommodity::getStasticsCommodityName, scmCommodityInfoReqVO.getStasticsName())
                    .eqIfPresent(ScmCommodity::getCommodityCode,scmCommodityInfoReqVO.getCommodityCode());

            List<ScmCommodity> scmCommodityList = scmCommodityMapper.selectList(commodityLambdaQueryWrapperX);
            if(ObjectUtil.isNotEmpty(scmCommodityList)){
                ScmCommodity scmCommodity = scmCommodityList.get(0);
                return scmCommodity;
            }

        }else{
            ScmCommodity scmCommodity = scmCommodities.get(0);
            return scmCommodity;
        }
        return null;
    }
}
