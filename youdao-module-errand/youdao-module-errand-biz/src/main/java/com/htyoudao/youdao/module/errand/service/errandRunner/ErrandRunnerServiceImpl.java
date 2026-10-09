package com.htyoudao.youdao.module.errand.service.errandRunner;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.errand.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO.*;
import com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO.*;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogMapper;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunner.ErrandRunnerMapper;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerWithdraw.ErrandRunnerWithdrawMapper;
import com.htyoudao.youdao.module.errand.enums.errandRunner.ErrandRunnerAuditStatusEnum;
import com.htyoudao.youdao.module.errand.enums.errandRunner.ErrandRunnerBanStatusEnum;
import com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog.DirectionEnum;
import com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog.FlowTypeEnum;
import com.htyoudao.youdao.module.errand.framework.config.ErrandRunnerProperties;
import com.htyoudao.youdao.module.errand.service.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogService;
import com.htyoudao.youdao.module.errand.service.errandRunnerBalanceLog.ErrandRunnerBalanceLogService;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.sms.SmsCodeApi;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import com.htyoudao.youdao.module.system.enums.sms.SmsSceneEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.errand.api.enums.ErrorCodeConstants.*;

/**
 * 跑腿员服务实现类。
 */
@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class ErrandRunnerServiceImpl extends ServiceImpl<ErrandRunnerMapper, ErrandRunnerDO> implements ErrandRunnerService {

    private static final int FEMALE = 2;
    private static final int TRAN_PASSWORD_STATUS_NOT_SET = 0;
    private static final int TRAN_PASSWORD_STATUS_SET = 1;
    private static final String REWARD_INCOME_BIZ_NO_PREFIX = "ERRAND_REWARD:";
    private static final String REWARD_REFUND_BIZ_NO_PREFIX = "ERRAND_REWARD_REFUND:";
    private static final String REWARD_UNFREEZE_BIZ_NO_PREFIX = "ERRAND_REWARD_UNFREEZE:";
    private static final int REWARD_UNFREEZE_BATCH_SIZE = 100;

    // 审核状态常量
    private static final Integer AUDIT_STATUS_PENDING = 0;  // 待审核
    private static final Integer AUDIT_STATUS_PASSED = 1;   // 通过
    private static final Integer AUDIT_STATUS_FAILED = 2;   // 失败

    // 首次审核状态
    private static final Integer FIRST_AUDIT_STATUS_PASSED = 1;  // 首次审核通过



    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private AdminUserApi adminUserApi;

    @DubboReference
    private WxMemberApi wxMemberApi;
    @DubboReference
    private BzOrderApi bzOrderApi;

    @Autowired
    private ErrandRunnerBalanceLogService errandRunnerBalanceLogService;

    @Autowired
    private ErrandRunnerApplyChangeLogMapper errandRunnerApplyChangeLogMapper;

    @Autowired
    private ErrandRunnerApplyChangeLogService errandRunnerApplyChangeLogService;

    @Autowired
    private ErrandRunnerApplyChangeLogMapper changeLogMapper;

    @Autowired
    private ErrandRunnerWithdrawMapper errandRunnerWithdrawMapper;

    @Autowired
    private ErrandRunnerMapper errandRunnerMapper;

    @Autowired
    private ErrandRunnerProperties errandRunnerProperties;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    /**
     * 提交骑手进驻申请。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long memberId, AppErrandRunnerApplyReqVO reqVO) {
        validateLoginMember(memberId);
        validateApplyReq(reqVO);
        validateMemberIdUnique(memberId);
        validatePhoneUnique(reqVO.getPhone(), memberId);
        validateIdCardNoUnique(reqVO.getIdCardNo(), memberId);

        ErrandRunnerDO deletedRunner = getDeletedByMemberId(memberId);
        if (deletedRunner != null) {
            String batchNo = buildApplyChangeBatchNo();
            List<ErrandRunnerApplyChangeLogDO> changeLogs = buildApplyChangeLogs(deletedRunner, reqVO, batchNo);
            changeLogs.forEach(errandRunnerApplyChangeLogMapper::insert);
            restoreDeletedApplyData(deletedRunner, reqVO);
            if (baseMapper.restoreDeletedRunner(deletedRunner) <= 0) {
                throw exception(ERRAND_RUNNER_AUDIT_FAIL);
            }
            return deletedRunner.getId();
        }

        ErrandRunnerDO runner = BeanUtils.toBean(reqVO, ErrandRunnerDO.class);
        runner.setMemberId(memberId);
        fillApplyDefault(runner);
        if (!save(runner)) {
            throw exception(ERRAND_RUNNER_AUDIT_FAIL);
        }
        return runner.getId();
    }

    /**
     * 修改审核失败后的骑手进驻申请。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateApply(Long memberId, AppErrandRunnerApplyReqVO reqVO) {
        validateLoginMember(memberId);
        validateApplyReq(reqVO);

        ErrandRunnerDO runner = getByMemberId(memberId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_APPLY_NOT_EXISTS);
        }
        validateCanUpdate(runner);
        validatePhoneUnique(reqVO.getPhone(), memberId);
        validateIdCardNoUnique(reqVO.getIdCardNo(), memberId);
        String batchNo = buildApplyChangeBatchNo();
        List<ErrandRunnerApplyChangeLogDO> changeLogs = buildApplyChangeLogs(runner, reqVO, batchNo);
        if (CollectionUtils.isEmpty(changeLogs)) {
            throw exception(ERRAND_RUNNER_APPLY_NO_CHANGE);
        }
        changeLogs.forEach(errandRunnerApplyChangeLogMapper::insert);
        return resetApplyAuditStatusAndPopup(runner.getId());
    }

    /**
     * 查询当前会员的骑手进驻详情。
     */
    @Override
    /**
     * 前端展示弹窗后，标记骑手弹窗状态为已弹窗。
     */
    public Boolean markPopupShown(Long memberId) {
        validateLoginMember(memberId);
        ErrandRunnerDO runner = getByMemberId(memberId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_APPLY_NOT_EXISTS);
        }
        ErrandRunnerDO update = new ErrandRunnerDO();
        update.setId(runner.getId());
        update.setPopupStatus(1);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    public AppErrandRunnerRespVO getApplyInfo(Long memberId) {
        validateLoginMember(memberId);
        ErrandRunnerDO runner = getByMemberId(memberId);
        if (runner == null) {
            return null;
        }
        AppErrandRunnerRespVO respVO = BeanUtils.toBean(runner, AppErrandRunnerRespVO.class);
        respVO.setStoreName(getStoreName(runner.getStoreId()));
        return respVO;
    }

    @Override
    public boolean checkCurrentRunnerAvailable() {
        Long memberId = WebFrameworkUtils.getLoginUserId();
        validateLoginMember(memberId);

        ErrandRunnerDO runner = getByMemberId(memberId);
        if (runner == null) {
            return false;
        }
        if (Objects.equals(runner.getBanStatus(), ErrandRunnerBanStatusEnum.BANNED.getCode())) {
            throw exception(ERRAND_RUNNER_BANNED);
        }
        return Objects.equals(runner.getFirstAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode());
    }

    /**
     * 分页查询跑腿员。
     */
    @Override
    public Page<ErrandRunnerPageVO> pageQuery(ErrandRunnerPageReqVO reqVO) {
        // 1. 构建查询条件并分页查询 DO
        Page<ErrandRunnerDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();

        if (reqVO.getStoreId() != null) {
            wrapper.eq(ErrandRunnerDO::getStoreId, reqVO.getStoreId());
        }

        if (reqVO.getOrgId() != null) {
            CommonResult<List<Long>> listCommonResult = orgStoreApi.selectByOrgStoreList(reqVO.getOrgId());
            List<Long> data = listCommonResult.getData();
            wrapper.in(ErrandRunnerDO::getStoreId, data);
        }
        if (reqVO.getAuditStatus() != null) {
            wrapper.eq(ErrandRunnerDO::getAuditStatus, reqVO.getAuditStatus());
        }
        if (StringUtils.hasText(reqVO.getPhone())) {
            wrapper.like(ErrandRunnerDO::getPhone, reqVO.getPhone());
        }
        if (StringUtils.hasText(reqVO.getName())) {
            wrapper.like(ErrandRunnerDO::getName, reqVO.getName());
        }
        if (StringUtils.hasText(reqVO.getStudentNo())) {
            wrapper.like(ErrandRunnerDO::getStudentNo, reqVO.getStudentNo());
        }
        if (reqVO.getBanStatus() != null) {
            wrapper.eq(ErrandRunnerDO::getBanStatus, reqVO.getBanStatus());
        }
        wrapper.eq(ErrandRunnerDO::getDeleted, false);
        wrapper.orderByDesc(ErrandRunnerDO::getCreateTime);

        Page<ErrandRunnerDO> doPage = page(page, wrapper);

        // 2. 收集所有门店 ID
        List<Long> storeIds = doPage.getRecords().stream()
                .map(ErrandRunnerDO::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 3. 批量查询门店信息获取学校名称
        Map<Long, String> storeSchoolNameMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            // 假设 storeApi.getStoreByStoreId() 方法支持批量查询
            // 如果不支持，需要循环调用或请后端同事提供批量接口
            for (Long storeId : storeIds) {
                try {
                    // 调用门店服务获取门店信息，假设返回的对象中有 schoolName 字段
                    CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(storeId);
                    if (storeByStoreId != null && storeByStoreId.isSuccess() && storeByStoreId.getData() != null) {
                        storeSchoolNameMap.put(storeId, storeByStoreId.getData().getStoreName());
                    }
                } catch (Exception e) {
                    log.error("获取门店信息失败, storeId: {}", storeId, e);
                }
            }
        }

        // 4. 转换为 PageVO 并填充 schoolName
        Page<ErrandRunnerPageVO> voPage = new Page<>(doPage.getCurrent(), doPage.getSize(), doPage.getTotal());
        List<ErrandRunnerPageVO> voList = doPage.getRecords().stream()
                .map(doObj -> {
                    ErrandRunnerPageVO vo = new ErrandRunnerPageVO();
                    BeanUtils.copyProperties(doObj, vo);
                    if(!ObjectUtils.isEmpty(doObj.getAuditUserId())){
                        CommonResult<AdminUserRespDTO> user = adminUserApi.getUser(doObj.getAuditUserId());
                        if (user != null && user.isSuccess() && user.getData() != null) {
                            vo.setAuditName(user.getData().getNickname());
                        }
                    }

                    vo.setSchoolName(storeSchoolNameMap.get(doObj.getStoreId()));
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }



    /**
     * 验证交易密码。
     */
    @Override
    public boolean verifyTranPassword(Long memberId, String tranPassword) {
       return wxMemberApi.verifyTranPassword(memberId,tranPassword);
    }





    /**
     * 验证会员是否存在跑腿员记录。
     */
    @Override
    public boolean checkMemberExists(Long memberId) {
        return getByMemberId(memberId) != null;
    }

    /**
     * 根据手机号获取跑腿员信息。
     */
    @Override
    public ErrandRunnerDO getByMobile(String mobile) {
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerDO::getPhone, mobile)
                .eq(ErrandRunnerDO::getDeleted, false);
        return getOne(wrapper, false);
    }

    /**
     * 根据会员 ID 获取跑腿员信息。
     */
    @Override
    public ErrandRunnerDO getByMemberId(Long memberId) {
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerDO::getMemberId, memberId)
                .eq(ErrandRunnerDO::getDeleted, false);
        return getOne(wrapper, false);
    }

    /**
     * 根据会员 ID 查询已删除的骑手记录，用于重新入驻时恢复历史数据。
     */
    private ErrandRunnerDO getDeletedByMemberId(Long memberId) {
        return baseMapper.selectDeletedByMemberId(memberId, BusinessContextHolder.getBusinessId());
    }

    /**
     * 审核跑腿员（含变更记录处理）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRunner(Long runnerId, Integer auditStatus, String auditReason) {
        log.info("开始审核跑腿员: runnerId={}, auditStatus={}", runnerId, auditStatus);

        // 1. 获取跑腿员信息
        ErrandRunnerDO runner = getById(runnerId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }

        // 2. 校验审核状态（只审核待审核的跑腿员）
        if (Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())) {
            throw exception(ERRAND_RUNNER_AUDIT_ALREADY_PASSED);
        }
        if (Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.REJECTED.getCode())) {
            throw exception(ERRAND_RUNNER_AUDIT_ALREADY_FAILED);
        }



        // 5. 审核失败处理
        if (Objects.equals(auditStatus, ErrandRunnerAuditStatusEnum.REJECTED.getCode())) {
            // 5.1 更新跑腿员审核状态为失败
            runner.setAuditStatus(auditStatus);
            runner.setAuditReason(auditReason);
            runner.setAuditTime(new Date());
            Long loginUserId = WebFrameworkUtils.getLoginUserId();
            runner.setAuditUserId(loginUserId);
            runner.setUpdateTime(LocalDateTime.now());


            boolean result = updateById(runner);
            if (!result) {
                throw exception(ERRAND_RUNNER_AUDIT_FAIL_ERROR);
            }

            if(runner.getFirstAuditStatus().equals(ErrandRunnerAuditStatusEnum.APPROVED.getCode())){
                String batchNo = errandRunnerApplyChangeLogService.getLatestBatchNoByRunnerId(runnerId);
                if (StringUtils.hasText(batchNo)) {
                    errandRunnerApplyChangeLogService.batchUpdateAuditStatus(batchNo, auditStatus);
                }
            }

            log.info("审核失败: runnerId={}, reason={}", runnerId, auditReason);
            return true;
        }

        // 6. 审核通过处理
        if (Objects.equals(auditStatus, ErrandRunnerAuditStatusEnum.APPROVED.getCode())) {


            if(runner.getFirstAuditStatus().equals(ErrandRunnerAuditStatusEnum.APPROVED.getCode())){
                String batchNo = errandRunnerApplyChangeLogService.getLatestBatchNoByRunnerId(runnerId);
                if (StringUtils.hasText(batchNo)) {
                    // 获取该批次的所有变更记录
                    List<ErrandRunnerApplyChangeLogDO> changeLogs = errandRunnerApplyChangeLogService.getByBatchNo(batchNo);

                    if (!CollectionUtils.isEmpty(changeLogs)) {
                        boolean needUpdateRunner = false;

                        // 根据变更字段更新跑腿员数据
                        for (ErrandRunnerApplyChangeLogDO changeLog : changeLogs) {
                            // 只处理待审核的变更记录
                            if (!Objects.equals(changeLog.getAuditStatus(), ErrandRunnerAuditStatusEnum.PENDING.getCode())) {
                                continue;
                            }

                            String fieldName = changeLog.getChangeField();
                            String newValue = changeLog.getNewValue();

                            if (updateRunnerField(runner, fieldName, newValue)) {
                                needUpdateRunner = true;
                                log.info("更新跑腿员字段: runnerId={}, field={}, oldValue={}, newValue={}",
                                        runnerId, fieldName, changeLog.getOldValue(), newValue);
                            }
                        }

                        // 如果有字段变更，执行跑腿员更新
                        if (needUpdateRunner) {
                            runner.setUpdateTime(LocalDateTime.now());
                            boolean updateResult = updateById(runner);
                            if (!updateResult) {
                                throw exception(ERRAND_RUNNER_AUDIT_PASS_FAIL);
                            }
                        }

                        // 批量更新变更记录状态为审核通过
                        errandRunnerApplyChangeLogService.batchUpdateAuditStatus(batchNo, auditStatus);
                    }
                }
            }

            // 6.2 更新跑腿员审核状态
            runner.setAuditStatus(auditStatus);
            runner.setFirstAuditStatus(ErrandRunnerAuditStatusEnum.APPROVED.getCode());
            runner.setAuditReason(null);
            runner.setAuditTime(new Date());
            Long loginUserId = WebFrameworkUtils.getLoginUserId();
            runner.setAuditUserId(loginUserId);
            runner.setUpdateTime(LocalDateTime.now());
            runner.setFirstAuditStatus(ErrandRunnerAuditStatusEnum.APPROVED.getCode());

            boolean result = updateById(runner);
            if (!result) {
                throw exception(ERRAND_RUNNER_AUDIT_PASS_FAIL);
            }

            // 6.3 调用会员接口更新跑腿员标识
            try {
                Integer shardingValue = (int) (runner.getMemberId() % 10);
                wxMemberApi.updateErrandFlag(runner.getMemberId(), shardingValue, true);
                log.info("审核通过，更新会员跑腿员标识成功: memberId={}, shardingValue={}",
                        runner.getMemberId(), shardingValue);
            } catch (Exception e) {
                log.warn("审核通过但更新会员跑腿员标识失败: memberId={}, error={}",
                        runner.getMemberId(), e.getMessage(), e);
            }

            log.info("审核通过: runnerId={}", runnerId);
            return true;
        }

        return false;
    }

    /**
     * 更新跑腿员单个字段
     *
     * @param runner 跑腿员对象
     * @param fieldName 字段名
     * @param newValue 新值
     * @return 是否更新
     */
    private boolean updateRunnerField(ErrandRunnerDO runner, String fieldName, String newValue) {
        if (newValue == null) {
            return false;
        }

        switch (fieldName) {
            case "name":
                if (!Objects.equals(runner.getName(), newValue)) {
                    runner.setName(newValue);
                    return true;
                }
                break;
            case "phone":
                if (!Objects.equals(runner.getPhone(), newValue)) {
                    runner.setPhone(newValue);
                    return true;
                }
                break;
            case "studentNo":
                if (!Objects.equals(runner.getStudentNo(), newValue)) {
                    runner.setStudentNo(newValue);
                    return true;
                }
                break;
            case "idCardNo":
                if (!Objects.equals(runner.getIdCardNo(), newValue)) {
                    runner.setIdCardNo(newValue);
                    return true;
                }
                break;
            case "gender":
                Integer gender = StringUtils.hasText(newValue) ? Integer.parseInt(newValue) : null;
                if (!Objects.equals(runner.getGender(), gender)) {
                    runner.setGender(gender);
                    return true;
                }
                break;
            case "idCardFront":
                if (!Objects.equals(runner.getIdCardFront(), newValue)) {
                    runner.setIdCardFront(newValue);
                    return true;
                }
                break;
            case "idCardBack":
                if (!Objects.equals(runner.getIdCardBack(), newValue)) {
                    runner.setIdCardBack(newValue);
                    return true;
                }
                break;
            case "studentCardImg":
                if (!Objects.equals(runner.getStudentCardImg(), newValue)) {
                    runner.setStudentCardImg(newValue);
                    return true;
                }
                break;
            case "storeId":
                Long storeId = StringUtils.hasText(newValue) ? Long.parseLong(newValue) : null;
                if (!Objects.equals(runner.getStoreId(), storeId)) {
                    runner.setStoreId(storeId);
                    return true;
                }
                break;
            default:
                log.warn("未知的字段名: {}", fieldName);
                break;
        }
        return false;
    }

    /**
     * 封禁跑腿员。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean banRunner(Long runnerId) {

        log.info("封禁人开始");
        ErrandRunnerDO runner = getById(runnerId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }
        if (Objects.equals(runner.getBanStatus(), ErrandRunnerBanStatusEnum.BANNED.getCode())) {
            throw exception(ERRAND_RUNNER_BAN_ALREADY);
        }

        // ========== 新增：检查是否有未完结订单 ==========
        // 获取跑腿员关联的会员ID（根据您的业务逻辑，跑腿员可能有对应的memberId）
        Long memberId = runner.getMemberId(); // 假设ErrandRunnerDO中有memberId字段
        if (memberId == null) {
            // 如果跑腿员没有关联会员ID，可能需要通过其他方式查询
            // 或者跳过此校验（根据业务决定）
            log.warn("跑腿员未关联会员ID，跳过未完结订单校验，runnerId: {}", runnerId);
        } else {
            // 查询该跑腿员是否有未完结订单
            boolean hasUnfinishedOrders = bzOrderApi.checkRunnerHasUnfinishedOrders(memberId);
            if (hasUnfinishedOrders) {
                throw exception(ERRAND_RUNNER_HAS_UNFINISHED_ORDERS);
            }
        }

        runner.setBanStatus(ErrandRunnerBanStatusEnum.BANNED.getCode());
        runner.setBanTime(new Date());
        runner.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(runner);
        if (result) {
            log.info("封禁跑腿员成功，runnerId: {}", runnerId);
            return true;
        }
        throw exception(ERRAND_RUNNER_BAN_FAIL);
    }



    /**
     * 解封跑腿员。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbanRunner(Long runnerId) {
        ErrandRunnerDO runner = getById(runnerId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }
        if (runner.getBanStatus() == null || Objects.equals(runner.getBanStatus(), ErrandRunnerBanStatusEnum.NORMAL.getCode())) {
            throw exception(ERRAND_RUNNER_UNBAN_ALREADY);
        }

        runner.setBanStatus(ErrandRunnerBanStatusEnum.NORMAL.getCode());
        runner.setBanReason(null);
        runner.setBanTime(null);
        runner.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(runner);
        if (result) {
            log.info("解封跑腿员成功，runnerId: {}", runnerId);
            return true;
        }
        throw exception(ERRAND_RUNNER_UNBAN_FAIL);
    }

    /**
     * 检查跑腿员是否可用。
     */
    @Override
    public boolean isRunnerAvailable(Long runnerId) {
        ErrandRunnerDO runner = getById(runnerId);
        if (runner == null) {
            return false;
        }
        return Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())
                && (runner.getBanStatus() == null || Objects.equals(runner.getBanStatus(), ErrandRunnerBanStatusEnum.NORMAL.getCode()));
    }

    /**
     * 获取待审核跑腿员列表。
     */
    @Override
    public Page<ErrandRunnerDO> getPendingAuditList(Integer current, Integer size) {
        Page<ErrandRunnerDO> page = new Page<>(current, size);
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerDO::getAuditStatus, ErrandRunnerAuditStatusEnum.PENDING.getCode())
                .eq(ErrandRunnerDO::getDeleted, false)
                .orderByAsc(ErrandRunnerDO::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 获取跑腿员余额明细。
     */
    @Override
    public BalanceDetailRespVO getBalanceDetail(Long memberId,ErrandRunnerDO runner) {
//        ErrandRunnerDO runner = getByMemberId(memberId);
//        if (runner == null) {
//            BalanceDetailRespVO  balanceDetailRespVO = new BalanceDetailRespVO();
//            BigDecimal bigDecimal = new BigDecimal(0);
//            balanceDetailRespVO.setAvailableBalance(bigDecimal);
//            balanceDetailRespVO.setTotalBalance(bigDecimal);
//            balanceDetailRespVO.setFrozenBalance(bigDecimal);
//            return balanceDetailRespVO;
//        }

        BigDecimal availableBalance = runner.getBalance() != null ? runner.getBalance() : BigDecimal.ZERO;
        BigDecimal frozenBalance = runner.getFrozenBalance() != null ? runner.getFrozenBalance() : BigDecimal.ZERO;
        BigDecimal totalBalance = availableBalance.add(frozenBalance);

        return BalanceDetailRespVO.builder()
                .totalBalance(totalBalance)
                .availableBalance(availableBalance)
                .frozenBalance(frozenBalance)
                .build();
    }

    @Override
    public BalanceLogPageRespVO pageBalanceLog(Long memberId, BalanceLogPageReqVO reqVO,ErrandRunnerDO runner) {
//        ErrandRunnerDO runner = getByMemberId(memberId);
//        if (runner == null) {
//            BalanceLogPageRespVO balanceLogPageRespVO= new BalanceLogPageRespVO();
//            balanceLogPageRespVO.setTotal(0L);
//            List<BalanceLogVO> records = new ArrayList<>();
//            balanceLogPageRespVO.setRecords(records);
//            return balanceLogPageRespVO;
//        }

        Page<ErrandRunnerBalanceLogDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runner.getId())
                .eq(ErrandRunnerBalanceLogDO::getDeleted, false);

        if (reqVO.getFlowType() != null) {
            wrapper.eq(ErrandRunnerBalanceLogDO::getFlowType, reqVO.getFlowType());
        }
        if (reqVO.getDirection() != null) {
            wrapper.eq(ErrandRunnerBalanceLogDO::getDirection, reqVO.getDirection());
        }
        if (StringUtils.hasText(reqVO.getStartTime())) {
            wrapper.ge(ErrandRunnerBalanceLogDO::getCreateTime, reqVO.getStartTime());
        }
        if (StringUtils.hasText(reqVO.getEndTime())) {
            wrapper.le(ErrandRunnerBalanceLogDO::getCreateTime, reqVO.getEndTime());
        }
        wrapper.orderByDesc(ErrandRunnerBalanceLogDO::getCreateTime);

        Page<ErrandRunnerBalanceLogDO> pageResult = errandRunnerBalanceLogService.page(page, wrapper);

        // 收集所有需要查询提现记录的 logId（有 withdrawId 的流水）
        List<Long> withdrawIds = pageResult.getRecords().stream()
                .filter(log -> Set.of(2, 4).contains(log.getFlowType()))
                .map(ErrandRunnerBalanceLogDO::getWithdrawId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询提现记录
        final Map<Long, ErrandRunnerWithdrawDO> withdrawMap;
        if (!withdrawIds.isEmpty()) {
            List<ErrandRunnerWithdrawDO> withdraws = errandRunnerWithdrawMapper.selectBatchIds(withdrawIds);
            withdrawMap = withdraws.stream()
                    .collect(Collectors.toMap(ErrandRunnerWithdrawDO::getId, Function.identity()));
        } else {
            withdrawMap = Collections.emptyMap();  // 使用空Map而不是null
        }

        // 转换并填充提现信息
        List<BalanceLogVO> records = pageResult.getRecords().stream()
                .map(log -> convertToBalanceLogVO(log, withdrawMap.get(log.getWithdrawId())))
                .collect(Collectors.toList());

        return BalanceLogPageRespVO.builder()
                .total(pageResult.getTotal())
                .records(records)
                .build();
    }

    private BalanceLogVO convertToBalanceLogVO(ErrandRunnerBalanceLogDO log, ErrandRunnerWithdrawDO withdraw) {
        BalanceLogVO vo = new BalanceLogVO();
        BeanUtils.copyProperties(log, vo);


        // 填充提现相关字段
        if (withdraw != null) {
            vo.setStatus(withdraw.getStatus());
            vo.setWechatBatchNo(withdraw.getWechatBatchNo());
            // 转换时设置
            if (withdraw.getCreateTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                vo.setCreateTime(log.getCreateTime().format(formatter));
            }
        }else{
            if (log.getCreateTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                vo.setCreateTime(log.getCreateTime().format(formatter));
            }
        }

        // 填充流水类型名称
        vo.setFlowTypeName(getFlowTypeName(vo.getFlowType()));

        return vo;
    }

    private String getFlowTypeName(Integer flowType) {
        if (flowType == null) {
            return null;
        }
        switch (flowType) {
            case 1:
                return "赏金入账";
            case 2:
                return "提现扣减";
            case 3:
                return "退款扣回";
            case 4:
                return "提现失败退回";
            case 5:
                return "人工调整";
            case 6:
                return "赏金解冻";
            default:
                return "未知类型";
        }
    }
    /**
     * 获取余额明细及流水。
     */
    @Override
    public BalanceDetailWithLogRespVO getBalanceDetailWithLog(Long memberId, BalanceLogPageReqVO reqVO) {

        ErrandRunnerDO runner = errandRunnerMapper.selectByMemberId(memberId);
        if (runner == null) {
            BalanceDetailWithLogRespVO balanceDetailWithLogRespVO = new BalanceDetailWithLogRespVO();
            BalanceDetailRespVO  balanceDetailRespVO = new BalanceDetailRespVO();
            BigDecimal bigDecimal = new BigDecimal(0);
            balanceDetailRespVO.setAvailableBalance(bigDecimal);
            balanceDetailRespVO.setTotalBalance(bigDecimal);
            balanceDetailRespVO.setFrozenBalance(bigDecimal);
            BalanceLogPageRespVO balanceLogPageRespVO= new BalanceLogPageRespVO();
            balanceLogPageRespVO.setTotal(0L);
            List<BalanceLogVO> records = new ArrayList<>();
            balanceLogPageRespVO.setRecords(records);
            balanceDetailWithLogRespVO.setBalanceDetail(balanceDetailRespVO);
            balanceDetailWithLogRespVO.setBalanceLogPage(balanceLogPageRespVO);
            return balanceDetailWithLogRespVO;
        }
        BalanceDetailRespVO balanceDetail = getBalanceDetail(memberId,runner);
        BalanceLogPageRespVO balanceLogPage = pageBalanceLog(memberId, reqVO,runner);

        return BalanceDetailWithLogRespVO.builder()
                .balanceDetail(balanceDetail)
                .balanceLogPage(balanceLogPage)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRewardIncome(Long runnerMemberId, String orderSn, BigDecimal amount) {
        validateRewardIncome(runnerMemberId, orderSn, amount);
        String bizNo = REWARD_INCOME_BIZ_NO_PREFIX + orderSn;
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            log.info("跑腿赏金已入账，跳过重复处理，orderSn: {}", orderSn);
            return true;
        }

        ErrandRunnerDO runner = baseMapper.selectByMemberIdForUpdate(runnerMemberId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            log.info("跑腿赏金已入账，跳过重复处理，orderSn: {}", orderSn);
            return true;
        }

        BigDecimal balance = nullToZero(runner.getBalance());
        BigDecimal frozenBalance = nullToZero(runner.getFrozenBalance());
        BigDecimal beforeBalance = balance.add(frozenBalance);
        BigDecimal afterFrozenBalance = frozenBalance.add(amount);
        BigDecimal afterBalance = beforeBalance.add(amount);

        ErrandRunnerDO update = new ErrandRunnerDO();
        update.setId(runner.getId());
        update.setFrozenBalance(afterFrozenBalance);
        if (!updateById(update)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }

        ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                .runnerId(runner.getId())
                .runnerMemberId(runner.getMemberId())
                .orderSn(orderSn)
                .flowType(FlowTypeEnum.INCOME.getCode())
                .direction(DirectionEnum.INCOME.getCode())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .balance(balance)
                .frozenBalance(afterFrozenBalance)
                .bizNo(bizNo)
                .remark("赏金入账，冻结" + getRewardFreezeMinutes() + "分钟")
                .build();
        if (!errandRunnerBalanceLogService.createBalanceLog(balanceLog)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundRewardDeduct(Long runnerMemberId, String orderSn, BigDecimal amount) {
        validateRewardIncome(runnerMemberId, orderSn, amount);
        if (errandRunnerBalanceLogService.getByBizNo(REWARD_INCOME_BIZ_NO_PREFIX + orderSn) == null) {
            log.info("跑腿赏金未入账，无需扣回，orderSn: {}", orderSn);
            return true;
        }
        String bizNo = REWARD_REFUND_BIZ_NO_PREFIX + orderSn;
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            log.info("跑腿赏金已扣回，跳过重复处理，orderSn: {}", orderSn);
            return true;
        }

        ErrandRunnerDO runner = baseMapper.selectByMemberIdForUpdate(runnerMemberId);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            log.info("跑腿赏金已扣回，跳过重复处理，orderSn: {}", orderSn);
            return true;
        }

        BigDecimal balance = nullToZero(runner.getBalance());
        BigDecimal frozenBalance = nullToZero(runner.getFrozenBalance());
        BigDecimal beforeBalance = balance.add(frozenBalance);
        if (beforeBalance.compareTo(amount) < 0) {
            throw exception(WITHDRAW_BALANCE_NOT_ENOUGH);
        }

        BigDecimal frozenDeductAmount = amount.min(frozenBalance);
        BigDecimal balanceDeductAmount = amount.subtract(frozenDeductAmount);
        BigDecimal afterFrozenBalance = frozenBalance.subtract(frozenDeductAmount);
        BigDecimal afterAvailableBalance = balance.subtract(balanceDeductAmount);
        BigDecimal afterBalance = afterAvailableBalance.add(afterFrozenBalance);

        ErrandRunnerDO update = new ErrandRunnerDO();
        update.setId(runner.getId());
        update.setBalance(afterAvailableBalance);
        update.setFrozenBalance(afterFrozenBalance);
        if (!updateById(update)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }

        ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                .runnerId(runner.getId())
                .runnerMemberId(runner.getMemberId())
                .orderSn(orderSn)
                .flowType(FlowTypeEnum.REFUND_DEDUCT.getCode())
                .direction(DirectionEnum.EXPENDITURE.getCode())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .balance(afterAvailableBalance)
                .frozenBalance(afterFrozenBalance)
                .bizNo(bizNo)
                .remark("退款扣回跑腿赏金")
                .build();
        if (!errandRunnerBalanceLogService.createBalanceLog(balanceLog)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unfreezeExpiredRewards() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(getRewardFreezeMinutes());
        List<ErrandRunnerBalanceLogDO> incomeLogs = errandRunnerBalanceLogService.list(
                new LambdaQueryWrapper<ErrandRunnerBalanceLogDO>()
                        .eq(ErrandRunnerBalanceLogDO::getFlowType, FlowTypeEnum.INCOME.getCode())
                        .eq(ErrandRunnerBalanceLogDO::getDirection, DirectionEnum.INCOME.getCode())
                        .le(ErrandRunnerBalanceLogDO::getCreateTime, expiredTime)
                        .isNotNull(ErrandRunnerBalanceLogDO::getOrderSn)
                        .orderByAsc(ErrandRunnerBalanceLogDO::getCreateTime)
                        .last("LIMIT " + REWARD_UNFREEZE_BATCH_SIZE)
        );
        int count = 0;
        for (ErrandRunnerBalanceLogDO incomeLog : incomeLogs) {
            BusinessContextHolder.setBusinessId(incomeLog.getBusinessId());
            if (unfreezeReward(incomeLog)) {
                count++;
            }
        }
        return count;
    }

    private int getRewardFreezeMinutes() {
        return errandRunnerProperties.getRewardFreezeMinutes();
    }

    private boolean unfreezeReward(ErrandRunnerBalanceLogDO incomeLog) {
        String bizNo = REWARD_UNFREEZE_BIZ_NO_PREFIX + incomeLog.getOrderSn();
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            return false;
        }

        ErrandRunnerDO runner = baseMapper.selectByIdForUpdate(incomeLog.getRunnerId());
        if (runner == null) {
            log.warn("赏金解冻失败，跑腿员不存在，runnerId: {}, orderSn: {}", incomeLog.getRunnerId(), incomeLog.getOrderSn());
            return false;
        }
        if (errandRunnerBalanceLogService.getByBizNo(bizNo) != null) {
            return false;
        }

        BigDecimal balance = nullToZero(runner.getBalance());
        BigDecimal frozenBalance = nullToZero(runner.getFrozenBalance());
        BigDecimal amount = nullToZero(incomeLog.getAmount());
        BigDecimal unfreezeAmount = amount.min(frozenBalance);
        if (unfreezeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("赏金解冻跳过，冻结余额不足，runnerId: {}, orderSn: {}", runner.getId(), incomeLog.getOrderSn());
            return false;
        }

        BigDecimal beforeBalance = balance.add(frozenBalance);
        BigDecimal afterAvailableBalance = balance.add(unfreezeAmount);
        BigDecimal afterFrozenBalance = frozenBalance.subtract(unfreezeAmount);
        BigDecimal afterBalance = afterAvailableBalance.add(afterFrozenBalance);

        ErrandRunnerDO update = new ErrandRunnerDO();
        update.setId(runner.getId());
        update.setBalance(afterAvailableBalance);
        update.setFrozenBalance(afterFrozenBalance);
        if (!updateById(update)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }

        ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                .runnerId(runner.getId())
                .runnerMemberId(runner.getMemberId())
                .orderSn(incomeLog.getOrderSn())
                .flowType(FlowTypeEnum.REWARD_UNFREEZE.getCode())
                .direction(DirectionEnum.INCOME.getCode())
                .amount(unfreezeAmount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .balance(afterAvailableBalance)
                .frozenBalance(afterFrozenBalance)
                .bizNo(bizNo)
                .remark("赏金解冻")
                .build();
        if (!errandRunnerBalanceLogService.createBalanceLog(balanceLog)) {
            throw exception(ERRAND_REWARD_INCOME_FAIL);
        }
        return true;
    }


    /**
     * 根据ID查询跑腿员详情（包含变更记录）
     * 特殊逻辑：当auditStatus=0（待审核）且firstAuditStatus=1（首次审核通过）时，
     * 需要用最新一批次的变更记录中的值覆盖原跑腿员信息
     */
    @Override
    public ErrandRunnerDetailVO getDetailById(Long id) {
        // 1. 查询主表信息
        ErrandRunnerDO runner = baseMapper.selectById(id);
        if (runner == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS.getCode(), ERRAND_RUNNER_NOT_EXISTS.getMsg());
        }

        // 2. 查询变更记录列表
        LambdaQueryWrapper<ErrandRunnerApplyChangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerApplyChangeLogDO::getRunnerId, id)
                .orderByDesc(ErrandRunnerApplyChangeLogDO::getCreateTime);
        List<ErrandRunnerApplyChangeLogDO> allChangeLogs = changeLogMapper.selectList(wrapper);

        // 3. 判断是否需要使用变更记录中的最新值
        ErrandRunnerDO displayRunner = runner;
        List<ErrandRunnerApplyChangeLogDO> displayChangeLogs = allChangeLogs;

        if (AUDIT_STATUS_PENDING.equals(runner.getAuditStatus())
                && FIRST_AUDIT_STATUS_PASSED.equals(runner.getFirstAuditStatus())) {
            // 待审核且首次审核通过，需要用最新批次的变更数据覆盖原值
            displayRunner = applyLatestBatchChanges(runner, allChangeLogs);
            // 过滤掉已经应用的最新批次变更记录（因为这些已经体现在displayRunner中了）
//            displayChangeLogs = filterOutLatestBatchChanges(allChangeLogs);
        }

        // 4. 转换为VO
        ErrandRunnerDetailVO detailVO = convertToDetailVO(displayRunner);
        detailVO.setChangeLogs(convertToChangeLogVOList(allChangeLogs));

        return detailVO;
    }

    /**
     * 应用最新一批次的变更记录，覆盖原跑腿员信息
     *
     * @param originalRunner 原始跑腿员信息
     * @param changeLogs 所有变更记录
     * @return 应用变更后的跑腿员信息
     */
    private ErrandRunnerDO applyLatestBatchChanges(ErrandRunnerDO originalRunner, List<ErrandRunnerApplyChangeLogDO> changeLogs) {
        if (changeLogs == null || changeLogs.isEmpty()) {
            return originalRunner;
        }

        // 获取最新一批次的变更记录（按batch_no分组，取最新的一条记录的batch_no）
        Map<String, List<ErrandRunnerApplyChangeLogDO>> batchGroupMap = changeLogs.stream()
                .filter(log -> log.getBatchNo() != null)
                .collect(Collectors.groupingBy(ErrandRunnerApplyChangeLogDO::getBatchNo));

        if (batchGroupMap.isEmpty()) {
            return originalRunner;
        }

        // 找出最新的批次号（按创建时间倒序，取第一个批次号）
        String latestBatchNo = changeLogs.stream()
                .filter(log -> log.getBatchNo() != null)
                .max(Comparator.comparing(ErrandRunnerApplyChangeLogDO::getCreateTime))
                .map(ErrandRunnerApplyChangeLogDO::getBatchNo)
                .orElse(null);

        if (latestBatchNo == null) {
            return originalRunner;
        }

        // 获取最新批次的所有变更记录
        List<ErrandRunnerApplyChangeLogDO> latestBatchChanges = batchGroupMap.get(latestBatchNo);

        // 创建新的跑腿员对象，复制原值
        ErrandRunnerDO updatedRunner = new ErrandRunnerDO();
        BeanUtils.copyProperties(originalRunner, updatedRunner);

        // 应用变更记录中的新值
        for (ErrandRunnerApplyChangeLogDO changeLog : latestBatchChanges) {
            applyChangeToRunner(updatedRunner, changeLog);
        }

        return updatedRunner;
    }

    /**
     * 将变更记录应用到跑腿员对象
     *
     * @param runner 跑腿员对象
     * @param changeLog 变更记录
     */
    private void applyChangeToRunner(ErrandRunnerDO runner, ErrandRunnerApplyChangeLogDO changeLog) {
        String changeField = changeLog.getChangeField();
        String newValue = changeLog.getNewValue();

        if (StrUtil.isBlank(changeField) || newValue == null) {
            return;
        }

        // 根据字段名设置对应的值
        switch (changeField) {
            case "name":
                runner.setName(newValue);
                break;
            case "phone":
                runner.setPhone(newValue);
                break;
            case "studentNo":
                runner.setStudentNo(newValue);
                break;
            case "idCardNo":
                runner.setIdCardNo(newValue);
                break;
            case "gender":
                if (StrUtil.isNotBlank(newValue)) {
                    runner.setGender(Integer.parseInt(newValue));
                }
                break;
            case "idCardFront":
                runner.setIdCardFront(newValue);
                break;
            case "idCardBack":
                runner.setIdCardBack(newValue);
                break;
            case "studentCardImg":
                runner.setStudentCardImg(newValue);
                break;
            case "storeId":
                if (StrUtil.isNotBlank(newValue)) {
                    runner.setStoreId(Long.parseLong(newValue));
                }
                break;
            default:
                log.debug("未处理的变更字段: {}", changeField);
                break;
        }
    }

    /**
     * 过滤掉最新一批次的变更记录（因为这些已经应用到主数据中了）
     *
     * @param allChangeLogs 所有变更记录
     * @return 过滤后的变更记录列表
     */
    private List<ErrandRunnerApplyChangeLogDO> filterOutLatestBatchChanges(List<ErrandRunnerApplyChangeLogDO> allChangeLogs) {
        if (allChangeLogs == null || allChangeLogs.isEmpty()) {
            return new ArrayList<>();
        }

        // 找出最新的批次号
        String latestBatchNo = allChangeLogs.stream()
                .filter(log -> log.getBatchNo() != null)
                .max(Comparator.comparing(ErrandRunnerApplyChangeLogDO::getCreateTime))
                .map(ErrandRunnerApplyChangeLogDO::getBatchNo)
                .orElse(null);

        if (latestBatchNo == null) {
            return allChangeLogs;
        }

        // 过滤掉最新批次的记录
        return allChangeLogs.stream()
                .filter(log -> !latestBatchNo.equals(log.getBatchNo()))
                .collect(Collectors.toList());
    }

    /**
     * 转换主表DO为DetailVO
     */
    private ErrandRunnerDetailVO convertToDetailVO(ErrandRunnerDO runner) {
        ErrandRunnerDetailVO vo = new ErrandRunnerDetailVO();
        BeanUtils.copyProperties(runner, vo);

        // 获取门店名称
        String storeName = getStoreName(runner.getStoreId());
        vo.setStoreName(storeName);
        vo.setSchoolName(storeName);

        return vo;
    }

    /**
     * 转换变更记录DO列表为VO列表
     */
    private List<ErrandRunnerChangeLogVO> convertToChangeLogVOList(List<ErrandRunnerApplyChangeLogDO> changeLogs) {
        if (changeLogs == null || changeLogs.isEmpty()) {
            return new ArrayList<>();
        }

        return changeLogs.stream()
                .map(this::convertToChangeLogVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换变更记录DO为VO
     */
    private ErrandRunnerChangeLogVO convertToChangeLogVO(ErrandRunnerApplyChangeLogDO changeLog) {
        ErrandRunnerChangeLogVO vo = new ErrandRunnerChangeLogVO();
        BeanUtils.copyProperties(changeLog, vo);
        return vo;
    }




    /**
     * 校验当前会员是否已登录。
     */
    private void validateLoginMember(Long memberId) {
        if (memberId == null) {
            throw exception(ERRAND_RUNNER_LOGIN_USER_EMPTY);
        }
    }

    /**
     * 校验当前会员是否已经存在未删除的骑手记录。
     */
    private void validateMemberIdUnique(Long memberId) {
        if (getByMemberId(memberId) != null) {
            throw exception(ERRAND_RUNNER_ALREADY_EXISTS);
        }
    }

    /**
     * 校验骑手进驻申请资料。
     */
    private void validateApplyReq(AppErrandRunnerApplyReqVO reqVO) {
        if (!StringUtils.hasText(reqVO.getIdCardFront())
                || !StringUtils.hasText(reqVO.getIdCardBack())
                || !StringUtils.hasText(reqVO.getStudentCardImg())) {
            throw exception(ERRAND_RUNNER_IMAGE_EMPTY);
        }
    }

    /**
     * 校验当前申请状态是否允许提交。
     */
    private void validateCanSubmit(ErrandRunnerDO runner) {
        if (Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.PENDING.getCode())) {
            throw exception(ERRAND_RUNNER_APPLY_PENDING);
        }
        if (Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())) {
            throw exception(ERRAND_RUNNER_APPLY_APPROVED);
        }
    }

    /**
     * 校验当前申请状态是否允许修改。
     */
    private void validateCanUpdate(ErrandRunnerDO runner) {
        if (Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.PENDING.getCode())) {
            throw exception(ERRAND_RUNNER_APPLY_AUDITING_NOT_ALLOW_UPDATE);
        }
        if (!Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.APPROVED.getCode())
                && !Objects.equals(runner.getAuditStatus(), ErrandRunnerAuditStatusEnum.REJECTED.getCode())) {
            throw exception(ERRAND_RUNNER_APPLY_STATUS_NOT_ALLOW_UPDATE);
        }
    }

    /**
     * 校验手机号是否被其他会员占用。
     */
    private void validatePhoneUnique(String phone, Long memberId) {
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerDO::getPhone, phone)
                .ne(memberId != null, ErrandRunnerDO::getMemberId, memberId)
                .eq(ErrandRunnerDO::getDeleted, false)
                .last("LIMIT 1");
        if (getOne(wrapper, false) != null) {
            throw exception(ERRAND_RUNNER_PHONE_DUPLICATE);
        }
    }

    /**
     * 更新申请资料并重置审核信息。
     */
    /**
     * 校验身份证号是否被其他会员占用。
     */
    private void validateIdCardNoUnique(String idCardNo, Long memberId) {
        LambdaQueryWrapper<ErrandRunnerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerDO::getIdCardNo, idCardNo)
                .ne(memberId != null, ErrandRunnerDO::getMemberId, memberId)
                .eq(ErrandRunnerDO::getDeleted, false)
                .last("LIMIT 1");
        if (getOne(wrapper, false) != null) {
            throw exception(ERRAND_RUNNER_ID_CARD_NO_DUPLICATE);
        }
    }

    private void updateApplyData(ErrandRunnerDO runner, AppErrandRunnerApplyReqVO reqVO) {
        runner.setStoreId(reqVO.getStoreId());
        runner.setName(reqVO.getName());
        runner.setPhone(reqVO.getPhone());
        runner.setStudentNo(reqVO.getStudentNo());
        runner.setIdCardNo(reqVO.getIdCardNo());
        runner.setGender(reqVO.getGender());
        runner.setIdCardFront(reqVO.getIdCardFront());
        runner.setIdCardBack(reqVO.getIdCardBack());
        runner.setStudentCardImg(reqVO.getStudentCardImg());
        runner.setAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        fillApplyDefault(runner);
        runner.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 恢复已删除的骑手入驻记录，余额和冻结余额保持历史值不变。
     */
    private void restoreDeletedApplyData(ErrandRunnerDO runner, AppErrandRunnerApplyReqVO reqVO) {
        runner.setStoreId(reqVO.getStoreId());
        runner.setName(reqVO.getName());
        runner.setPhone(reqVO.getPhone());
        runner.setStudentNo(reqVO.getStudentNo());
        runner.setIdCardNo(reqVO.getIdCardNo());
        runner.setGender(reqVO.getGender());
        runner.setIdCardFront(reqVO.getIdCardFront());
        runner.setIdCardBack(reqVO.getIdCardBack());
        runner.setStudentCardImg(reqVO.getStudentCardImg());
        runner.setAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        runner.setFirstAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        runner.setPopupStatus(0);
        runner.setBanStatus(0);
        runner.setAuditReason(null);
        runner.setAuditTime(null);
        runner.setAuditUserId(null);
        runner.setDeleted(false);
        runner.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 判断本次提交的申请资料是否发生变化。
     */
    private boolean hasApplyDataChanged(ErrandRunnerDO runner, AppErrandRunnerApplyReqVO reqVO) {
        return !Objects.equals(runner.getStoreId(), reqVO.getStoreId())
                || !Objects.equals(runner.getName(), reqVO.getName())
                || !Objects.equals(runner.getPhone(), reqVO.getPhone())
                || !Objects.equals(runner.getStudentNo(), reqVO.getStudentNo())
                || !Objects.equals(runner.getIdCardNo(), reqVO.getIdCardNo())
                || !Objects.equals(runner.getGender(), reqVO.getGender())
                || !Objects.equals(runner.getIdCardFront(), reqVO.getIdCardFront())
                || !Objects.equals(runner.getIdCardBack(), reqVO.getIdCardBack())
                || !Objects.equals(runner.getStudentCardImg(), reqVO.getStudentCardImg());
    }

    /**
     * 构建非图片字段的申请资料变更记录。
     */
    private List<ErrandRunnerApplyChangeLogDO> buildApplyChangeLogs(ErrandRunnerDO runner, AppErrandRunnerApplyReqVO reqVO) {
        List<ErrandRunnerApplyChangeLogDO> logs = new ArrayList<>();
        addChangeLog(logs, runner, "storeId", "门店ID", toChangeValue(runner.getStoreId()), toChangeValue(reqVO.getStoreId()));
        addChangeLog(logs, runner, "name", "姓名", runner.getName(), reqVO.getName());
        addChangeLog(logs, runner, "phone", "手机号", runner.getPhone(), reqVO.getPhone());
        addChangeLog(logs, runner, "studentNo", "学号", runner.getStudentNo(), reqVO.getStudentNo());
        addChangeLog(logs, runner, "idCardNo", "身份证号", runner.getIdCardNo(), reqVO.getIdCardNo());
        addChangeLog(logs, runner, "gender", "性别", toChangeValue(runner.getGender()), toChangeValue(reqVO.getGender()));
        return logs;
    }

    /**
     * 根据门店 ID 查询门店名称，详情回显失败时不影响主流程。
     */
    private String getStoreName(Long storeId) {
        if (storeId == null) {
            return null;
        }
        try {
            CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(storeId);
            if (storeResult == null || storeResult.getData() == null) {
                return null;
            }
            return storeResult.getData().getStoreName();
        } catch (Exception e) {
            log.warn("查询骑手入驻门店名称失败，storeId: {}", storeId, e);
            return null;
        }
    }

    /**
     * 字段发生变化时追加一条变更记录。
     */
    private void addChangeLog(List<ErrandRunnerApplyChangeLogDO> logs, ErrandRunnerDO runner,
                              String changeField, String changeFieldName, String oldValue, String newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        logs.add(ErrandRunnerApplyChangeLogDO.builder()
                .runnerId(runner.getId())
                .memberId(runner.getMemberId())
                .changeField(changeField)
                .changeFieldName(changeFieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }

    /**
     * 转换变更记录值，避免 null 被写成字符串。
     */
    /**
     * 构建本次修改提交的非图片字段变更记录。
     */
    private List<ErrandRunnerApplyChangeLogDO> buildApplyChangeLogs(ErrandRunnerDO runner, AppErrandRunnerApplyReqVO reqVO, String batchNo) {
        List<ErrandRunnerApplyChangeLogDO> logs = new ArrayList<>();
        addChangeLog(logs, runner, batchNo, "storeId", "门店ID", toChangeValue(runner.getStoreId()), toChangeValue(reqVO.getStoreId()));
        addChangeLog(logs, runner, batchNo, "name", "姓名", runner.getName(), reqVO.getName());
        addChangeLog(logs, runner, batchNo, "phone", "手机号", runner.getPhone(), reqVO.getPhone());
        addChangeLog(logs, runner, batchNo, "studentNo", "学号", runner.getStudentNo(), reqVO.getStudentNo());
        addChangeLog(logs, runner, batchNo, "idCardNo", "身份证号", runner.getIdCardNo(), reqVO.getIdCardNo());
        addChangeLog(logs, runner, batchNo, "gender", "性别", toChangeValue(runner.getGender()), toChangeValue(reqVO.getGender()));
        addChangeLog(logs, runner, batchNo, "idCardFront", "身份证人像面", runner.getIdCardFront(), reqVO.getIdCardFront());
        addChangeLog(logs, runner, batchNo, "idCardBack", "身份证国徽面", runner.getIdCardBack(), reqVO.getIdCardBack());
        addChangeLog(logs, runner, batchNo, "studentCardImg", "学生证照片", runner.getStudentCardImg(), reqVO.getStudentCardImg());
        return logs;
    }

    /**
     * 字段发生变化时追加一条待审核变更记录。
     */
    private void addChangeLog(List<ErrandRunnerApplyChangeLogDO> logs, ErrandRunnerDO runner, String batchNo,
                              String changeField, String changeFieldName, String oldValue, String newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        logs.add(ErrandRunnerApplyChangeLogDO.builder()
                .runnerId(runner.getId())
                .memberId(runner.getMemberId())
                .batchNo(batchNo)
                .auditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode())
                .changeField(changeField)
                .changeFieldName(changeFieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }

    /**
     * 生成同一次修改提交的批次号。
     */
    private String buildApplyChangeBatchNo() {
        return "AC" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 修改资料提交后，仅重置主表审核状态和弹窗状态，不回写资料字段。
     */
    private boolean resetApplyAuditStatusAndPopup(Long runnerId) {
        ErrandRunnerDO update = new ErrandRunnerDO();
        update.setId(runnerId);
        update.setAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        update.setPopupStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    private String toChangeValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 补齐骑手进驻申请默认值。
     */
    private void fillApplyDefault(ErrandRunnerDO runner) {
        if (runner.getAuditStatus() == null) {
            runner.setAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        }
        if (runner.getBanStatus() == null) {
            runner.setBanStatus(ErrandRunnerBanStatusEnum.NORMAL.getCode());
        }
        if (runner.getFirstAuditStatus() == null) {
            runner.setFirstAuditStatus(ErrandRunnerAuditStatusEnum.PENDING.getCode());
        }
        if (runner.getPopupStatus() == null) {
            runner.setPopupStatus(0);
        }
        if (runner.getBalance() == null) {
            runner.setBalance(BigDecimal.ZERO);
        }
        if (runner.getFrozenBalance() == null) {
            runner.setFrozenBalance(BigDecimal.ZERO);
        }
    }

    private void validateRewardIncome(Long runnerMemberId, String orderSn, BigDecimal amount) {
        if (runnerMemberId == null) {
            throw exception(ERRAND_RUNNER_NOT_EXISTS);
        }
        if (!StringUtils.hasText(orderSn) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ERRAND_REWARD_AMOUNT_INVALID);
        }
    }

    private BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 转换为余额流水 VO。
     */
    private BalanceLogVO convertToBalanceLogVO(ErrandRunnerBalanceLogDO logDO) {
        return BalanceLogVO.builder()
                .id(logDO.getId())
                .flowType(logDO.getFlowType())
                .flowTypeName(FlowTypeEnum.getNameByCode(logDO.getFlowType()))
                .direction(logDO.getDirection())
                .directionName(DirectionEnum.getNameByCode(logDO.getDirection()))
                .amount(logDO.getAmount())
                .afterBalance(logDO.getAfterBalance())
                .balance(logDO.getBalance())
                .frozenBalance(logDO.getFrozenBalance())
                .orderSn(logDO.getOrderSn())
                .remark(logDO.getRemark())
                .build();
    }
}
