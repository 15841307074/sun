package com.htyoudao.youdao.module.member.service.pointsLog;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsExchangeDetailReqVO;
import com.htyoudao.youdao.module.member.controller.app.pointsLog.VO.PointsLogVO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 积分记录Service接口
 *
 * @author Qizhongnan
 * @date 2024-02-03
 */
public interface IPointsLogService extends IService<PointsLogDO> {

    /**
     * 查询积分记录列表
     *
     * @return 积分记录集合
     */
    public List<PointsLogDO> selectPointsLogList(Long memberId);


    /**
     * 新增积分记录
     *
     * @param
     * @return 结果
     */
    public int insertPointsLog(PointsLogSaveReqVO saveReqVO);

    /**
     * 修改积分记录
     *
     * @return 结果
     */
    public int updatePointsLog(@Valid PointsLogEditReqVO pointsLogVO);



//     public void addMemberPoint(BzOrder bzOrder);

    void productInsert(PointsLogEditReqVO pointsLogVO);

    WxPointsLogAndAllPointsDTO getListByMemberId(PointsLogVO pointsLogVO);


    List<WxPointsLogDetailDTO> wxGetPointsLogDetailList(PointsLogVO pointsLogVO);

    /**
     * 查询小程序积分商品兑换详情。
     *
     * @param reqVO 兑换详情查询参数
     * @return 单条兑换详情
     */
    WxPointsLogDetailDTO getExchangeDetail(PointsExchangeDetailReqVO reqVO);

    /**
     * 查询积分记录列表分页
     *
     * @param
     * @return 积分记录集合
     */
    public PageResult<PointsLogDO> selectPointsLogListPage(long pageNum, long pageSize, PointsLogPageListReqVo pageListReqVo);

    /**
     * 查询兑换积分商品列表分页
     */
    PageResult<PointsLogDO> selectExchangeListPage(long pageNum, long pageSize, PointsLogExchangeListReqVo exchangeListReqVo);

    /**
     * 清理过期积分
     */
    void clearExpiredPoints(Long businessId);


    Boolean addMemberPoint(ClientAddMemberPointReqVO reqVO);

    WxPointsLogAndAllPointsDTO getMemberAllPoints(PointsLogVO pointsLogVO);

    void updatePointsLogWithOverDue(Long businessId);


    void export(PointsLogExportReqVo pointsLogExportReqVo, HttpServletRequest request, HttpServletResponse response);
}
