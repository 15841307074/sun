package com.htyoudao.youdao.module.bpm.service.storeinspectionrecorditem;

import java.util.*;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordDetailRespVO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;

/**
 * 巡店记录明细快照 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreInspectionRecordItemService extends IService<StoreInspectionRecordItemDO> {

    /**
     * 创建巡店记录明细快照
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStoreInspectionRecordItem(@Valid StoreInspectionRecordItemSaveReqVO createReqVO);

    /**
     * 更新巡店记录明细快照
     *
     * @param updateReqVO 更新信息
     */
    StoreInspectionRecordDetailRespVO updateStoreInspectionRecordItem(@Valid StoreInspectionRecordItemSaveReqVO updateReqVO);

    /**
     * 删除巡店记录明细快照
     *
     * @param id 编号
     */
    void deleteStoreInspectionRecordItem(Long id);

    /**
     * 获得巡店记录明细快照
     *
     * @param id 编号
     * @return 巡店记录明细快照
     */
    StoreInspectionRecordItemDO getStoreInspectionRecordItem(Long id);

    /**
     * 获得巡店记录明细快照分页
     *
     * @param pageReqVO 分页查询
     * @return 巡店记录明细快照分页
     */
    PageResult<StoreInspectionRecordItemDO> getStoreInspectionRecordItemPage(StoreInspectionRecordItemPageReqVO pageReqVO);

}