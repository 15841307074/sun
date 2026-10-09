package com.htyoudao.youdao.module.system.service.logger;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogDO;

/**
 * 操作日志 Service 接口
 *
 * @author 0090
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 创建请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    /**
     * 通过join获取操作日志页
     *
     * @param pageReqVO 页请求视图对象
     * @return {@link PageResult }<{@link OperateLogPageReqVO }>
     */
    PageResult<OperateLogRespVO> getOperateLogPageByJoin(OperateLogPageReqVO pageReqVO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqVO);

    /**
     * 清理操作日志
     *
     * @param exceedDay   超过天数
     * @param deleteLimit 删除限制
     * @return {@link Integer }
     */
    Integer cleanOperateLog(Integer exceedDay, Integer deleteLimit);
}
