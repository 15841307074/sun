package com.htyoudao.youdao.module.bpm.service.storeinspectionitemlog;

import java.util.*;

import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo.*;
import com.htyoudao.youdao.framework.common.pojo.PageResult;

/**
 * 巡店项操作变更日志 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreInspectionItemLogService {

    /**
     * 创建巡店项操作变更日志
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoreInspectionItemLog(@Valid StoreInspectionItemLogSaveReqVO createReqVO);

    /**
     * 更新巡店项操作变更日志
     *
     * @param updateReqVO 更新信息
     */
    void updateStoreInspectionItemLog(@Valid StoreInspectionItemLogSaveReqVO updateReqVO);

    /**
     * 删除巡店项操作变更日志
     *
     * @param id 编号
     */
    void deleteStoreInspectionItemLog(Long id);

    /**
     * 获得巡店项操作变更日志
     *
     * @param id 编号
     * @return 巡店项操作变更日志
     */
    StoreInspectionItemLogDO getStoreInspectionItemLog(Long id);

    /**
     * 获得巡店项操作变更日志分页
     *
     * @param pageReqVO 分页查询
     * @return 巡店项操作变更日志分页
     */
    PageResult<StoreInspectionItemLogDO> getStoreInspectionItemLogPage(StoreInspectionItemLogPageReqVO pageReqVO);

}