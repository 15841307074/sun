package com.htyoudao.youdao.module.bpm.service.storeinspectionitemlog;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;

import com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionitemlog.StoreInspectionItemLogMapper;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.STORE_INSPECTION_ITEM_LOG_NOT_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

/**
 * 巡店项操作变更日志 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StoreInspectionItemLogServiceImpl implements StoreInspectionItemLogService {

    @Resource
    private StoreInspectionItemLogMapper storeInspectionItemLogMapper;

    @Override
    public Long createStoreInspectionItemLog(StoreInspectionItemLogSaveReqVO createReqVO) {
        // 插入
        StoreInspectionItemLogDO storeInspectionItemLog = BeanUtils.toBean(createReqVO, StoreInspectionItemLogDO.class);
        storeInspectionItemLogMapper.insert(storeInspectionItemLog);
        // 返回
        return storeInspectionItemLog.getId();
    }

    @Override
    public void updateStoreInspectionItemLog(StoreInspectionItemLogSaveReqVO updateReqVO) {
        // 校验存在
        validateStoreInspectionItemLogExists(updateReqVO.getId());
        // 更新
        StoreInspectionItemLogDO updateObj = BeanUtils.toBean(updateReqVO, StoreInspectionItemLogDO.class);
        storeInspectionItemLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteStoreInspectionItemLog(Long id) {
        // 校验存在
        validateStoreInspectionItemLogExists(id);
        // 删除
        storeInspectionItemLogMapper.deleteById(id);
    }

    private void validateStoreInspectionItemLogExists(Long id) {
        if (storeInspectionItemLogMapper.selectById(id) == null) {
            throw exception(STORE_INSPECTION_ITEM_LOG_NOT_EXISTS);
        }
    }

    @Override
    public StoreInspectionItemLogDO getStoreInspectionItemLog(Long id) {
        return storeInspectionItemLogMapper.selectById(id);
    }

    @Override
    public PageResult<StoreInspectionItemLogDO> getStoreInspectionItemLogPage(StoreInspectionItemLogPageReqVO pageReqVO) {
        return storeInspectionItemLogMapper.selectPage(pageReqVO);
    }

}