package com.htyoudao.youdao.module.commodity.service.templateSingle;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.CommodityUNameReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import com.htyoudao.youdao.module.commodity.dal.mysql.CommodityTemplateGroupSingleMapper;
import jakarta.annotation.Resource;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author DELL
* @description 针对表【commodity_template_group_single】的数据库操作Service实现
* @createDate 2025-02-07 11:27:21
*/
@Service
public class CommodityTemplateGroupSingleServiceImpl implements ICommodityTemplateGroupSingleService {

    @Resource
    private CommodityTemplateGroupSingleMapper commodityTemplateGroupSingleMapper;

    @Override
    public void saveBatch(List<CommodityTemplateGroupSingle> addCommodityTemplateGroupSingleList) {
        commodityTemplateGroupSingleMapper.insertBatch(addCommodityTemplateGroupSingleList);
    }

    @Override
    public void deleteByTemplateSpuIds(List<Long> commodityTemplateIds) {
        LambdaQueryWrapper<CommodityTemplateGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CommodityTemplateGroupSingle::getCommodityTemplateId, commodityTemplateIds);
        commodityTemplateGroupSingleMapper.delete(queryWrapper);
    }

    @Override
    public List<CommodityTemplateGroupSingle> selectByGroupTemplateIds(List<Long> groupTemplateIdList) {
        LambdaQueryWrapper<CommodityTemplateGroupSingle> templateSingleQueryWrapper = new LambdaQueryWrapper<>();
        templateSingleQueryWrapper.in(CommodityTemplateGroupSingle::getTemplateGroupId, groupTemplateIdList);
        return commodityTemplateGroupSingleMapper.selectList(templateSingleQueryWrapper);
    }

    @Override
    public List<CommodityTemplateGroupSingle> selectByTemplateId(Long commodityTemplateId) {
        LambdaQueryWrapper<CommodityTemplateGroupSingle> templateSingleQueryWrapper = new LambdaQueryWrapper<>();
        templateSingleQueryWrapper.eq(CommodityTemplateGroupSingle::getTemplateId, commodityTemplateId);
        return commodityTemplateGroupSingleMapper.selectList(templateSingleQueryWrapper);
    }



    @Override
    public void changeName(CommoditySpusUpdateReqVo updateReqVo) {
        String commodityName = updateReqVo.getCommodityName();
        LambdaQueryWrapper<CommodityTemplateGroupSingle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityTemplateGroupSingle::getCommodityId, updateReqVo.getCommodityId());
        List<CommodityTemplateGroupSingle> commodityTemplateGroupSingles = commodityTemplateGroupSingleMapper.selectList(queryWrapper);
        if (ObjectUtil.isNotEmpty(commodityTemplateGroupSingles)) {
            for (CommodityTemplateGroupSingle commodityTemplateGroupSingle : commodityTemplateGroupSingles) {
                String oldName = commodityTemplateGroupSingle.getCommodityName();
                String newName ;
                if (oldName.contains("(") && oldName.contains(")")) {
                    // 提取括号部分（例如"（中）"）
                    String suffix = oldName.substring(oldName.indexOf("("));
                    // 组合新名称：新前缀 + 原有括号部分
                    newName = commodityName + suffix;
                } else {
                    // 不包含括号，直接替换为新名称
                    newName = commodityName;
                }
                commodityTemplateGroupSingle.setCommodityName(newName);
                if (ObjectUtil.isNotEmpty(updateReqVo.getImageUrlVO())) {
                    commodityTemplateGroupSingle.setCommodityUrl(updateReqVo.getImageUrlVO().get(0));
                }

            }
            commodityTemplateGroupSingleMapper.updateBatch(commodityTemplateGroupSingles);
        }
    }



}




