package com.htyoudao.youdao.module.commodity.service.scmCommodityStastics;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.commodity.config.BusinessProjectMappingConfig;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmCommodityStasticRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.scmCommodityStastic.VO.ScmStasticsReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityStastics.ScmCommodityStastics;
import com.htyoudao.youdao.module.commodity.dal.dataobject.scmCommodiy.ScmCommodity;
import com.htyoudao.youdao.module.commodity.dal.mysql.commodityStastic.ScmCommodityStasticsMapper;
import com.htyoudao.youdao.module.commodity.service.scmCommodity.ScmCommodityService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScmCommodityStasticsServiceImpl implements ScmCommodityStasticsService {


    @Resource
    private ScmCommodityStasticsMapper scmCommodityStasticsMapper;

    @Resource
    private ScmCommodityService scmCommodityService;

    @Resource
    private BusinessProjectMappingConfig businessProjectMappingConfig;

    @Override
    public List<ScmCommodityStasticRespVO> selectList(ScmStasticsReqVO scmStasticsReqVO, int value) {
        Long businessId = BusinessContextHolder.getBusinessId();

        Integer projectCode = businessProjectMappingConfig.getProjectCode(String.valueOf(businessId));


        List<ScmCommodityStasticRespVO> stasticRespVOS = new ArrayList<>();

        LambdaQueryWrapper<ScmCommodityStastics> queryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(scmStasticsReqVO.getStasticsName())) {
            queryWrapper.like(ScmCommodityStastics::getStasticsName,scmStasticsReqVO.getStasticsName() );
        }
        queryWrapper.eq(ScmCommodityStastics::getProjectCode,projectCode);
        queryWrapper.eq(ScmCommodityStastics::getIsDelete,0);
        queryWrapper.orderByDesc(ScmCommodityStastics::getUpdateTime);
        List<ScmCommodityStastics> scmCommodityStastics = scmCommodityStasticsMapper.selectList(queryWrapper);

        if (ObjectUtil.isNotEmpty(scmCommodityStastics)) {


            // 如果只想收集stasticsName属性的List
            List<String> nameList = scmCommodityStastics.stream()
                    .map(ScmCommodityStastics::getStasticsName)
                    .toList();

            List<ScmCommodity> scmCommodities = scmCommodityService.selectListByStatiscsNames(nameList,projectCode,scmStasticsReqVO.getWarehouseId(),value);
            Map<String, List<ScmCommodity>> collect = new HashMap<>();
            if (ObjectUtil.isNotEmpty(scmCommodities)) {
                collect=  scmCommodities.stream().collect(Collectors.groupingBy(ScmCommodity::getStasticsCommodityName));



            }
            for (ScmCommodityStastics scmCommodityStastic : scmCommodityStastics) {
                ScmCommodityStasticRespVO  stasticRespVO = new ScmCommodityStasticRespVO();
                stasticRespVO.setId(scmCommodityStastic.getId());
                stasticRespVO.setStasticsName(scmCommodityStastic.getStasticsName());
                List<ScmCommodity> scmCommodities1 = collect.get(scmCommodityStastic.getStasticsName());
                if (ObjectUtil.isNotEmpty(scmCommodities1)) {
                    stasticRespVO.setNum(scmCommodities1.size());
                }else {
                    stasticRespVO.setNum(0);
                }
                stasticRespVOS.add(stasticRespVO);
            }

        }


        return stasticRespVOS;
    }


}
