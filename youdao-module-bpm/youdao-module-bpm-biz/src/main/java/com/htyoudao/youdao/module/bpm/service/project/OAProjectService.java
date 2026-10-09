package com.htyoudao.youdao.module.bpm.service.project;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectCreateReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectPageRespVO;
import com.htyoudao.youdao.module.bpm.controller.admin.oa.vo.OAProjectRelationshipReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.project.OAProjectDO;
import jakarta.validation.Valid;

import java.util.Map;

/**
 * oa项目 Service 接口
 *
 * @author jason
 * @author 0090
 */
public interface OAProjectService extends IService<OAProjectDO> {

    PageResult<OAProjectPageRespVO> getProjectPage(@Valid OAProjectPageReqVO pageVO);

    Long createOAProject(@Valid OAProjectCreateReqVO createReqVO);

    Long updateOAProject(OAProjectCreateReqVO createReqVO);

    void deleteProject(Long id);

    void addRelationship(@Valid OAProjectRelationshipReqVO reqVO);

    void removeRelationship(@Valid OAProjectRelationshipReqVO reqVO);

    OAProjectPageRespVO detailById(Long id);

    OAProjectPageRespVO selectById(Long oaProjectId);

    Map<Long, String> getAll();
}
