package com.htyoudao.youdao.module.errand.api.runner;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.object.ObjectUtils;
import com.htyoudao.youdao.module.errand.api.runner.dto.ErrandRunnerDTO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.enums.errandRunner.ErrandRunnerAuditStatusEnum;
import com.htyoudao.youdao.module.errand.enums.errandRunner.ErrandRunnerBanStatusEnum;
import com.htyoudao.youdao.module.errand.service.errandRunner.ErrandRunnerService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
public class ErrandRunnerApiImpl implements ErrandRunnerApi {

    @Resource
    private ErrandRunnerService errandRunnerService;

    @Override
    public CommonResult<ErrandRunnerDTO> getRunnerByMemberId(Long memberId) {
        ErrandRunnerDO runner = errandRunnerService.getByMemberId(memberId);
        return success(runner == null ? null : BeanUtils.toBean(runner, ErrandRunnerDTO.class));
    }

    @Override
    public CommonResult<ErrandRunnerDTO> getAvailableRunnerByMemberId(Long memberId) {
        ErrandRunnerDO runner = errandRunnerService.getByMemberId(memberId);
        if (runner == null) {
            return success(null);
        }
        if (!Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())) {
            return success(null);
        }
        if (Objects.equals(runner.getBanStatus(), ErrandRunnerBanStatusEnum.BANNED.getCode())) {
            return success(null);
        }
        if (!Objects.equals(runner.getFirstAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())) {
            return success(null);
        }
        return success(BeanUtils.toBean(runner, ErrandRunnerDTO.class));
    }

    @Override
    public CommonResult<Boolean> createRewardIncome(Long runnerMemberId, String orderSn, BigDecimal amount) {
        return success(errandRunnerService.createRewardIncome(runnerMemberId, orderSn, amount));
    }

    @Override
    public CommonResult<Boolean> refundRewardDeduct(Long runnerMemberId, String orderSn, BigDecimal amount) {
        return success(errandRunnerService.refundRewardDeduct(runnerMemberId, orderSn, amount));
    }

    public Map<Long, BigDecimal> getRunnerBalanceByMemberId(List<Long> memberIds) {
        LambdaQueryWrapper<ErrandRunnerDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ErrandRunnerDO::getMemberId, ErrandRunnerDO::getBalance);
        queryWrapper.in(CollectionUtil.isNotEmpty(memberIds),ErrandRunnerDO::getMemberId, memberIds);
        List<ErrandRunnerDO> runners = errandRunnerService.list(queryWrapper);
        return runners.stream().collect(java.util.stream.Collectors.toMap(ErrandRunnerDO::getMemberId, ErrandRunnerDO::getBalance));
    }
}
