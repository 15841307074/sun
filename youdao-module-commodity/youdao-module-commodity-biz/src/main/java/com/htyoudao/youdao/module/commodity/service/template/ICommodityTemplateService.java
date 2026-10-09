package com.htyoudao.youdao.module.commodity.service.template;


import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateListRespVo;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityTemplateSaveReqVo;
import com.htyoudao.youdao.module.commodity.controller.admin.template.VO.CommodityUpdateTemplateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplate;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 商品模板Service接口
 * 
 * @author Qizhongnan
 * @date 2024-03-18
 */
public interface ICommodityTemplateService {

    List<CommodityTemplateListRespVo> selectCommodityTemplateListNew();

    void add(@Valid CommodityTemplateSaveReqVo saveReqVo);

    void update(CommodityUpdateTemplateReqVO commodityTemplate);

    void deleteByTemplateId(Long commodityTemplateId);

    CommodityTemplate selectById(Long templateId);

    void base2template(List<Long> commodityIds, Long commodityTemplateId);

    void copy(Long commodityTemplateId);

}
