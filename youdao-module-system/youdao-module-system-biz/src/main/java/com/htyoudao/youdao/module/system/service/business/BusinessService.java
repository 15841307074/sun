package com.htyoudao.youdao.module.system.service.business;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 项目 Service 接口
 *
 * @author 零零玖零
 */
public interface BusinessService {

    /**
     * 创建项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBusiness(@Valid BusinessSaveReqVO createReqVO);

    /**
     * 更新项目
     *
     * @param updateReqVO 更新信息
     */
    void updateBusiness(@Valid BusinessSaveReqVO updateReqVO);

    /**
     * 删除项目
     *
     * @param id 编号
     */
    void deleteBusiness(Long id);

    /**
     * 获得项目
     *
     * @param id 编号
     * @return 项目
     */
    BusinessDO getBusiness(Long id);

    String getBusinessName(Long id);

    /**
     * 获得项目分页
     *
     * @param pageReqVO 分页查询
     * @return 项目分页
     */
    PageResult<BusinessPageRespVO> getBusinessPage(BusinessPageReqVO pageReqVO);

    /**
     * 修改状态
     *
     * @param id     项目编号
     * @param status 状态
     */
    void updateBusinessStatus(Long id, Integer status);

    /**
     * 根据 用户ID查询所属项目 及 角色
     */
    BusinessSimpleRespVO getBusinessRoleByUserId(Long userId);


    List<BusinessUserRespVO> getUserList(Long loginUserId);
    /**
     * 项目精简列表
     */
    List<BusinessDO> getBusinessList();


    /**
     * 通过项目id获取项目列表
     *
     * @param businessIds 业务id
     * @return {@link List }<{@link BusinessDO }>
     */
    List<BusinessDO> getBusinessList(Collection<Long> businessIds);

    /**
     * 根据 用户ID查询所属项目List 及 角色
     */
   List<BusinessSimpleRespVO>  getBusinessListRoleByUserId(Long userId);


    List<BusinessDTO> listAll();

    List<BusinessStoreRespVO> listAllStore();

    List<BusinessUserRespVO> getUserByBossList(Long loginUserId);
}
