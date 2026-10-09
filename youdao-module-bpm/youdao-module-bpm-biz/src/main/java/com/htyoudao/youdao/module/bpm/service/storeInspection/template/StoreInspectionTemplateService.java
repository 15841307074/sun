package com.htyoudao.youdao.module.bpm.service.storeInspection.template;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.TemplateChecklistsSortReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.TemplateQueryReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.TemplateReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.TemplateRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo.InspectionTemplateDropDownRespVO;

import java.util.List;

/**
 * 巡店模板 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreInspectionTemplateService {


    List<InspectionTemplateDropDownRespVO> getInspectionTemplate();

    Integer create(TemplateReqVO templateReqVO);

    TemplateRespVO getTemplateById(Long templateId);

    PageResult<TemplateRespVO> query(TemplateQueryReqVO templateName);

    Integer updateTemplate(TemplateReqVO templateReqVO);

    Integer delTemplate(Long templateId);
}