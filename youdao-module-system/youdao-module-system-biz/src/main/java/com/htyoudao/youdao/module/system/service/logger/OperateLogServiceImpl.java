package com.htyoudao.youdao.module.system.service.logger;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.ArrayUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import com.htyoudao.youdao.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogDO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogExtendDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.logger.OperateLogExtendMapper;
import com.htyoudao.youdao.module.system.dal.mysql.logger.OperateLogMapper;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * 操作日志 Service 实现类
 *
 * @author 0090
 */
@Service
@Validated
@Slf4j
public class OperateLogServiceImpl implements OperateLogService {

    @Resource
    private OperateLogMapper operateLogMapper;
    @Resource
    private OperateLogExtendMapper operateLogExtendMapper;
    @Resource
    private AdminUserService adminUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        // 1. 插入操作日志
        OperateLogDO logDO = BeanUtils.toBean(createReqDTO, OperateLogDO.class);
        // 存在部分情况会丢失用户上下文信息，需要补充
        logDO.setCreator(String.valueOf(logDO.getUserId()));
        logDO.setUpdater(String.valueOf(logDO.getUserId()));
        if (StrUtil.isBlank(logDO.getUsername())) {
            AdminUserDO user = adminUserService.getUser(logDO.getUserId());
            logDO.setUsername(StrUtil.isBlank(user.getNickname()) ? user.getUsername() : user.getNickname());
        }
        operateLogMapper.insert(logDO);

        // 2. 如果有扩展信息，则插入扩展日志
        if (hasExtendInfo(createReqDTO)) {
            OperateLogExtendDO extendDO = new OperateLogExtendDO();
            extendDO.setLogId(logDO.getId());
            extendDO.setRequestParams(createReqDTO.getRequestParams());
            extendDO.setRequestHeaders(createReqDTO.getRequestHeaders());
            operateLogExtendMapper.insert(extendDO);
        }
    }

    /**
     * 判断是否有扩展信息
     *
     * @param createReqDTO 操作日志请求DTO
     * @return 是否有扩展信息
     */
    private boolean hasExtendInfo(OperateLogCreateReqDTO createReqDTO) {
        return StringUtils.hasText(createReqDTO.getRequestParams()) ||
                StringUtils.hasText(createReqDTO.getRequestHeaders());
    }

    @Override
    public PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO) {
        return operateLogMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<OperateLogRespVO> getOperateLogPageByJoin(OperateLogPageReqVO pageReqVO) {
        LocalDateTime[] createTimeList = pageReqVO.getCreateTime();
        boolean hasCreateTime = ArrayUtil.isNotEmpty(createTimeList) && createTimeList.length == 2;
        MPJLambdaWrapper<OperateLogDO> wrapper = new MPJLambdaWrapper<OperateLogDO>()
                .selectAll(OperateLogDO.class, OperateLogDO::getUsername)
                .selectAs(AdminUserDO::getNickname, OperateLogDO::getUsername)
                .leftJoin(AdminUserDO.class, AdminUserDO::getId, OperateLogDO::getCreator)
                .eqIfExists(OperateLogDO::getBizId, pageReqVO.getBizId())
                .likeIfExists(AdminUserDO::getNickname, pageReqVO.getUsername())
                .likeIfExists(OperateLogDO::getType, pageReqVO.getType())
                .likeIfExists(OperateLogDO::getSubType, pageReqVO.getSubType())
                .likeIfExists(OperateLogDO::getAction, pageReqVO.getAction())
                .between(hasCreateTime, OperateLogDO::getCreateTime, ArrayUtils.get(createTimeList, 0), ArrayUtils.get(createTimeList, 1))
                .orderByDesc(OperateLogDO::getId);
        return operateLogMapper.selectJoinPage(pageReqVO, OperateLogRespVO.class, wrapper);
    }

    @Override
    public PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        return operateLogMapper.selectPage(pageReqDTO);
    }

    @Override
    public Integer cleanOperateLog(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
        // 循环删除，直到没有满足条件的数据
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = operateLogMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
            operateLogExtendMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
            count += deleteCount;
            // 达到删除预期条数，说明到底了
            if (deleteCount < deleteLimit) {
                break;
            }
        }
        return count;
    }
}
