package com.htyoudao.youdao.module.system.service.complant;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintDetailRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintRespVO;
import com.htyoudao.youdao.module.system.controller.admin.complaint.vo.ComplaintUpdateReqVO;
import com.htyoudao.youdao.module.system.controller.app.complaint.vo.SysComplaintSaveVO;

import java.util.List;
import java.util.Map;

public interface ComplaintService {
    /**
     * 投诉建议列表
     */
    PageResult<ComplaintRespVO> getComplaintListPage(ComplaintPageReqVO pageReqVO);
    /**
     * 投诉建议详情
     */
    ComplaintDetailRespVO getComplaint(Long id);
    /**
     * 投诉建议处理
     */
    void updateComplaint(ComplaintUpdateReqVO reqVO);
    /**
     * 待处理数量
     */
    Long getComplaintCount();

    /** 当前账号组织及下级门店、直接关联店长门店的未处理投诉总数，供角标使用。 */
    Long getRegionPendingCount();
    /**
     * 新增投诉
     */
    void addComplaint(SysComplaintSaveVO sysComplaintVo);
    /**
     * 投诉建议列表
     */
    PageResult<ComplaintRespVO> getComplaintListPageApp(ComplaintPageReqVO pageReqVO);
    /**
     * 投诉建议列表数量
     */
    Map<Integer, Long> pageByStoreSum(ComplaintPageReqVO pageReqVO);
    void exportComplaintList(ComplaintPageReqVO pageReqVO);
}
