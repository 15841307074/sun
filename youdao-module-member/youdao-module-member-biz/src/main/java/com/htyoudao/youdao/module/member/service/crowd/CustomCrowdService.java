package com.htyoudao.youdao.module.member.service.crowd;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.api.crowd.dto.CrowdNameDTO;
import com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.*;
import com.htyoudao.youdao.module.member.controller.admin.customcrowd.vo.CustomCrowdSaveReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import jakarta.validation.*;

/**
 * 自定义人群 Service 接口
 *
 * @author 芋道源码
 */
public interface CustomCrowdService {

    /**
     * 创建自定义人群
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCrowd(@Valid CustomCrowdSaveReqVO createReqVO);

    /**
     * 更新自定义人群
     *
     * @param updateReqVO 更新信息
     */
    void updateCrowd(@Valid CustomCrowdSaveReqVO updateReqVO);

    /**
     * 删除自定义人群
     *
     * @param id 编号
     */
    void deleteCrowd(Long id);

    /**
    * 批量删除自定义人群
    *
    * @param ids 编号
    */
    void deleteCrowdListByIds(List<Long> ids);

    /**
     * 获得自定义人群分页
     *
     * @param reqVO 人群名称
     * @return 自定义人群分页
     */
    PageResult<CustomCrowdPageRespVO> page(CustomCrowdPageReqVO reqVO);

    /**
     * 获得自定义人群详情
     *
     * @param id 人群id
     * @return 自定义人群
     */
    CustomCrowdRespVO getById(Long id);
    /**
     * 查询所有自定义人群
     *
     * @return
     */
    List<CustomCrowdDO> getAll(String busId);

//    /**
//     * 获得自定义人群
//     *
//     * @param id 编号
//     * @return 自定义人群
//     */
//    CustomCrowdDO getCrowd(Long id);
//
//    /**
//     * 获得自定义人群分页
//     *
//     * @param pageReqVO 分页查询
//     * @return 自定义人群分页
//     */
//    PageResult<CustomCrowdDO> getCrowdPage(CrowdPageReqVO pageReqVO);


    /**
     * 获得自定义人群下拉
     * @return 自定义人群
     */
    List<CustomCrowdDropDownRespVO> dropDown();

    /**
     * 根据ids查询
     * @param allCrowd allCrowd
     * @return Boolean
     */
    List<CrowdNameDTO> getByIds(List<Long> allCrowd);
}