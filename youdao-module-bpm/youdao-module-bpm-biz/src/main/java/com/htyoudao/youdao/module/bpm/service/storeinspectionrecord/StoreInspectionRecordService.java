package com.htyoudao.youdao.module.bpm.service.storeinspectionrecord;

import java.util.*;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionItemStatRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionOverviewRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionReportReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.StoreInspectionIntervalRespVO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 巡店记录主 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreInspectionRecordService extends IService<StoreInspectionRecordDO> {

    /**
     * 创建巡店记录主
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoreInspectionRecord(@Valid StoreInspectionRecordSaveReqVO createReqVO);

    /**
     * 更新巡店记录主
     *
     * @param updateReqVO 更新信息
     */
    void updateStoreInspectionRecord(@Valid StoreInspectionRecordSaveReqVO updateReqVO);

    /**
     * 删除巡店记录主
     *
     * @param id 编号
     */
    void deleteStoreInspectionRecord(Long id);

    /**
     * 获得巡店记录主
     *
     * @return 巡店记录主
     */
    StoreInspectionRecordDetailRespVO getStoreInspectionRecord(StoreInspectionRecordDetailReqVO reqVO);

    /**
     * 获得巡店记录主分页
     *
     * @param pageReqVO 分页查询
     * @return 巡店记录主分页
     */
    PageResult<StoreInspectionRecordDO> getStoreInspectionRecordPage(StoreInspectionRecordPageReqVO pageReqVO);

    /**
     * 巡店概览
     *
     * @param reqVO reqVO
     * @return InspectionOverviewRespVO
     */
    PageResult<InspectionOverviewRespVO> getInspectionOverview(InspectionReportReqVO reqVO);

    /**
     * 不合格点检项
     *
     * @param reqVO reqVO
     * @return InspectionItemStatRespVO
     */
    PageResult<InspectionItemStatRespVO> getInspectionItemStat(@Valid InspectionReportReqVO reqVO);

    PageResult<InspectionItemStatRespVO> getInspectionGroupStat(@Valid InspectionReportReqVO reqVO);

    /**
     * 不合格点检项
     *
     * @param reqVO reqVO
     * @return InspectionItemStatRespVO
     */
    PageResult<StoreInspectionIntervalRespVO> getStoreInspectionInterval(@Valid InspectionReportReqVO reqVO);

    /**
     * 通过门店查看巡店记录
     *
     * @param reqVO reqVO
     * @return StoreInspectionRecordRespVO
     */
    StoreInspectionRecordRespVO getRecordByStoreId(@Valid StoreInspectionRecordReqVO reqVO);

    /**
     * 通过recordId查看巡店记录
     *
     * @param recordId recordId
     * @return StoreInspectionRecordDetailRespVO
     */
    StoreInspectionRecordDetailRespVO getItemByRecordId(Long recordId);

    /**
     * 结束巡店
     * @param reqVO reqVO
     */
    void overInspectionRecord(OverInspectionRecordReqVO reqVO);

    /**
     * 所有门店插入一波 后端自用
     * @return
     */
    Long createAll();
}