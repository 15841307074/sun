package com.htyoudao.youdao.module.commodity.service.templateSingle;


import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.CommodityUNameReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateGroupSingle;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
* @author DELL
* @description 针对表【commodity_template_group_single】的数据库操作Service
* @createDate 2025-02-07 11:27:21
*/
public interface ICommodityTemplateGroupSingleService  {

    void saveBatch(List<CommodityTemplateGroupSingle> addCommodityTemplateGroupSingleList);

    void deleteByTemplateSpuIds(List<Long> commodityTemplateIds);

    List<CommodityTemplateGroupSingle> selectByGroupTemplateIds(List<Long> groupTemplateIdList);

    List<CommodityTemplateGroupSingle> selectByTemplateId(Long commodityTemplateId);




    void changeName(CommoditySpusUpdateReqVo updateReqVo);


}
