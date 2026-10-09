package com.htyoudao.youdao.module.promotion.service.survey;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerExportVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.FillBlankAnswerRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.SurveyAnswerPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.Points.PointsLogMapper;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyAnswerDetailMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyAnswerMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.SurveyRedisDAO;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
@DS(DsNameConstants.SHARDING)
public class SurveyAnswerServiceImpl implements SurveyAnswerService {

    @Resource
    private SurveyAnswerMapper surveyAnswerMapper;

    @Resource
    private SurveyAnswerDetailMapper surveyAnswerDetailMapper;

    @Resource
    private SurveyMapper surveyMapper;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyQuestionMapper surveyQuestionMapper;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.redis.SurveyRedisDAO surveyRedisDAO;

    @DubboReference
    private WxMemberApi wxMemberApi;

    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private PointsLogMapper pointsLogMapper;

    @Resource
    private CouponPackageService couponPackageService;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Resource
    private LotteryAddLogService lotteryAddLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAnswer(Long surveyId, Long phone, Long memberId, Integer source, String sourceIp, Integer duration, List<SurveyAnswerDetailDO> details) {
        // 1. 获取手机号锁（防并发重复提交）
        if (!surveyRedisDAO.tryLockPhone(surveyId, phone)) {
            throw exception(SURVEY_ALREADY_SUBMITTED);
        }

        try {
            // 2. 校验问卷状态（只有进行中才允许提交）
            SurveyDO survey = surveyMapper.selectById(surveyId);
            if (survey == null) {
                throw exception(SURVEY_NOT_EXISTS);
            }
            // 计算展示状态：0-未发布 1-进行中 2-已结束
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            boolean hasStart = survey.getStartTime() != null;
            boolean hasEnd = survey.getEndTime() != null;
            int computedStatus;
            if (!hasStart && !hasEnd) {
                // 没有时间值，以 DB status 为准
                computedStatus = survey.getStatus() != null ? survey.getStatus() : 0;
            } else if (hasStart && !hasEnd) {
                computedStatus = !now.isBefore(survey.getStartTime()) ? 1 : 0;
            } else if (!hasStart) {
                computedStatus = !now.isAfter(survey.getEndTime()) ? 1 : 2;
            } else {
                if (now.isBefore(survey.getStartTime())) computedStatus = 0;
                else if (now.isAfter(survey.getEndTime())) computedStatus = 2;
                else computedStatus = 1;
            }
            if (computedStatus != 1) {
                throw exception(SURVEY_NOT_IN_PROGRESS);
            }

            // 3. 校验重复提交（Redis Set 快速判断 + DB 兜底）
            if (Integer.valueOf(0).equals(survey.getAllowRepeat())) {
                Boolean redisSubmitted = surveyRedisDAO.hasSubmitted(surveyId, phone);
                if (Boolean.TRUE.equals(redisSubmitted)) {
                    throw exception(SURVEY_ALREADY_SUBMITTED);
                }
                // DB 兜底校验
                if (surveyAnswerMapper.existsBySurveyIdAndPhone(surveyId, phone)) {
                    throw exception(SURVEY_ALREADY_SUBMITTED);
                }
            } else {
                Long count = surveyAnswerMapper.selectCountBySurveyIdAndPhone(surveyId, phone);
                if (count >= survey.getRepeatLimit()) {
                    throw exception(SURVEY_SUBMIT_LIMIT_REACHED);
                }
            }

            // 4. 校验必答题
            validateRequiredQuestions(surveyId, details);

            // 5. 创建答卷记录（以手机号为用户唯一标识）
            SurveyAnswerDO answerDO = SurveyAnswerDO.builder()
                    .surveyId(surveyId)
                    .phone(phone)
                    .memberId(memberId)
                    .source(source)
                    .sourceIp(sourceIp)
                    .duration(duration)
                    .submitTime(LocalDateTime.now())
                    .rewardType(survey.getRewardType() != null ? survey.getRewardType() : 0)
                    .rewardStatus(0) // 未发放
                    .build();
            surveyAnswerMapper.insert(answerDO);

            // 6. 保存答题明细
            for (SurveyAnswerDetailDO detail : details) {
                detail.setAnswerId(answerDO.getId());
                detail.setSurveyId(surveyId);
                surveyAnswerDetailMapper.insert(detail);
            }

            // 7. Redis 提交计数+1
            surveyRedisDAO.incrSubmitCount(surveyId);
            // 记录已提交手机号到 Set
            surveyRedisDAO.addSubmitted(surveyId, phone);

            // 8. DB 提交计数+1
            surveyMapper.incrementSubmitCount(surveyId);

            // 9. 触发奖励发放
            processReward(survey, answerDO);

            return answerDO.getId();
        } finally {
            // 释放手机号锁
            surveyRedisDAO.unlockPhone(surveyId, phone);
        }
    }

    @Override
    public PageResult<SurveyAnswerDO> getAnswerPage(SurveyAnswerPageReqVO reqVO) {
        return surveyAnswerMapper.selectPage(reqVO);
    }

    @Override
    public SurveyAnswerDO getAnswer(Long answerId) {
        return surveyAnswerMapper.selectById(answerId);
    }

    @Override
    public List<SurveyAnswerDetailDO> getAnswerDetails(Long answerId) {
        return surveyAnswerDetailMapper.selectListByAnswerId(answerId);
    }

    @Override
    public PageResult<SurveyAnswerDO> getMyAnswerPage(Long phone, Integer pageNo, Integer pageSize) {
        Page<SurveyAnswerDO> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SurveyAnswerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyAnswerDO::getPhone, phone)
                .orderByDesc(SurveyAnswerDO::getSubmitTime);
        Page<SurveyAnswerDO> result = surveyAnswerMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public SurveyAnswerDO getRewardResult(Long surveyId, Long answerId) {
        return surveyAnswerMapper.selectById(answerId);
    }

    @Override
    public PageResult<FillBlankAnswerRespVO> getFillBlankAnswerPage(FillBlankAnswerPageReqVO reqVO) {
        Page<FillBlankAnswerRespVO> mpPage = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        Page<FillBlankAnswerRespVO> result = surveyAnswerDetailMapper.selectFillBlankAnswerPage(
                mpPage, reqVO.getSurveyId(), reqVO.getQuestionId(), reqVO.getKeyword());
        // 将 fill_blank_text 的 JSON 解析为纯文本
        result.getRecords().forEach(vo -> vo.setAnswerText(extractFillBlankText(vo.getAnswerText())));
        // 过滤空答案（在Java层过滤，避免SQL中JSON解析的兼容性问题）
        if (Boolean.TRUE.equals(reqVO.getFilterEmpty())) {
            List<FillBlankAnswerRespVO> filtered = result.getRecords().stream()
                    .filter(vo -> cn.hutool.core.util.StrUtil.isNotBlank(vo.getAnswerText()))
                    .collect(Collectors.toList());
            return new PageResult<>(filtered, result.getTotal());
        }
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public void exportFillBlankAnswers(Long surveyId, Long questionId, HttpServletResponse response) throws IOException {
        // 查询该题目的所有填空答案
        List<FillBlankAnswerRespVO> answers = surveyAnswerDetailMapper.selectAllFillBlankAnswers(surveyId, questionId);

        // 构建导出数据，添加序号
        List<FillBlankAnswerExportVO> exportData = new java.util.ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            FillBlankAnswerRespVO answer = answers.get(i);
            FillBlankAnswerExportVO exportVO = new FillBlankAnswerExportVO();
            exportVO.setSeq(i + 1);
            exportVO.setSubmitTime(answer.getSubmitTime());
            exportVO.setAnswerText(extractFillBlankText(answer.getAnswerText()));
            exportVO.setAnswerId(answer.getAnswerId());
            exportData.add(exportVO);
        }

        // 写出 Excel
        String filename = "填空题答案_" + surveyId + "_" + questionId;
        ExcelUtils.write(response, filename + ".xlsx", "填空题答案", FillBlankAnswerExportVO.class, exportData);
    }

    // ========== 私有方法 ==========

    /**
     * 从 fill_blank_text 的 JSON 字符串中提取纯文本
     * 支持三种格式：
     * 1. JSON数组 [{"optionId":"101","text":"速度"}] → 提取所有text拼接
     * 2. JSON对象 {"101":"速度"} → 提取所有value拼接
     * 3. 纯文本 → 直接返回
     */
    private String extractFillBlankText(String fillBlankText) {
        if (cn.hutool.core.util.StrUtil.isBlank(fillBlankText)) {
            return fillBlankText;
        }
        String trimmed = fillBlankText.trim();
        if (trimmed.startsWith("[")) {
            // JSON数组格式：[{"optionId":"101","text":"速度"}]
            try {
                List<cn.hutool.json.JSONObject> items = cn.hutool.json.JSONUtil.toList(trimmed, cn.hutool.json.JSONObject.class);
                return items.stream()
                        .map(obj -> obj.getStr("text", ""))
                        .filter(cn.hutool.core.util.StrUtil::isNotBlank)
                        .collect(java.util.stream.Collectors.joining("、"));
            } catch (Exception e) {
                return fillBlankText;
            }
        } else if (trimmed.startsWith("{")) {
            // JSON对象格式：{"101":"速度"}
            try {
                java.util.Map<String, String> map = cn.hutool.json.JSONUtil.toBean(trimmed,
                        new cn.hutool.core.lang.TypeReference<java.util.Map<String, String>>() {}, false);
                return String.join("、", map.values());
            } catch (Exception e) {
                return fillBlankText;
            }
        }
        // 纯文本，直接返回
        return fillBlankText;
    }

    private void validateRequiredQuestions(Long surveyId, List<SurveyAnswerDetailDO> details) {
        // 获取必填题目
        List<com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO> requiredQuestions =
                surveyQuestionMapper.selectList(
                        new LambdaQueryWrapper<com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO>()
                                .eq(com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO::getSurveyId, surveyId)
                                .eq(com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO::getIsRequired, 1));

        // 检查每道必答题是否都有对应的答案
        Set<Long> answeredQuestionIds = details.stream()
                .map(SurveyAnswerDetailDO::getQuestionId)
                .collect(Collectors.toSet());

        for (var q : requiredQuestions) {
            if (!answeredQuestionIds.contains(q.getId())) {
                throw exception(SURVEY_REQUIRED_NOT_ANSWERED);
            }
        }
    }

    @DS(DsNameConstants.SHARDING)
    private void processReward(SurveyDO survey, SurveyAnswerDO answerDO) {
        if (survey.getRewardType() == null || survey.getRewardType() == 0) {
            return;
        }
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        if (memberId == null) {
            log.warn("问卷奖励发放跳过：memberId为空, surveyId={}, answerId={}", survey.getId(), answerDO.getId());
            return;
        }

        // 获取用户信息
        WxMemberVO wxMemberVO = wxMemberApi.getWxMemberById(memberId).getData();
        if (wxMemberVO == null) {
            throw new RuntimeException("用户不存在, memberId=" + memberId);
        }
        WxMemberDTO wxMember = BeanUtils.toBean(wxMemberVO, WxMemberDTO.class);

        switch (survey.getRewardType()) {
            case 1: // 积分奖励
                grantPointsReward(survey, answerDO, wxMember);
                break;
            case 2: // 优惠券奖励
                grantCouponReward(survey, answerDO, wxMember);
                break;
            case 3: // 优惠券包奖励
                grantCouponPackageReward(survey, answerDO, wxMember);
                break;
            default:
                log.warn("未知的奖励类型: {}, surveyId={}", survey.getRewardType(), survey.getId());
                return;
        }

        // 更新奖励状态为已发放
        answerDO.setRewardStatus(1);
        answerDO.setRewardGrantedAt(LocalDateTime.now());
        answerDO.setRewardValue(getRewardValue(survey));
        surveyAnswerMapper.updateById(answerDO);

        log.info("问卷奖励发放成功, surveyId={}, answerId={}, rewardType={}, memberId={}",
                survey.getId(), answerDO.getId(), survey.getRewardType(), memberId);
//        try {
//
//        } catch (Exception e) {
//            log.warn("问卷奖励发放失败, surveyId={}, answerId={}, rewardType={}",
//                    survey.getId(), answerDO.getId(), survey.getRewardType(), e);
//            answerDO.setRewardStatus(2);
//            String failMsg = e.getMessage();
//            if (failMsg != null && failMsg.length() > 490) {
//                failMsg = failMsg.substring(0, 490) + "...(truncated)";
//            }
//            answerDO.setRewardFailReason(failMsg);
//            surveyAnswerMapper.updateById(answerDO);
//        }
    }

    /**
     * 发放积分奖励
     * 参照抽奖模块 LotteryMobileServiceImpl.updateData() 中 prizeType=2 的逻辑
     */
    private void grantPointsReward(SurveyDO survey, SurveyAnswerDO answerDO, WxMemberDTO wxMember) {
        Integer rewardPoints = survey.getRewardPoints();
        if (rewardPoints == null || rewardPoints <= 0) {
            log.warn("积分奖励数值无效: {}, surveyId={}", rewardPoints, survey.getId());
            return;
        }

        // 累加积分
        int newIntegral = wxMember.getMemberIntegral() + rewardPoints;
        wxMemberApi.updateMemberById(wxMember.getMemberId(), newIntegral);

        // 记录积分日志
        addPointsLog(wxMember, rewardPoints, survey.getId());

        log.info("积分奖励发放成功, memberId={}, points={}, surveyId={}",
                wxMember.getMemberId(), rewardPoints, survey.getId());
    }

    /**
     * 发放优惠券奖励
     * 参照抽奖模块 LotteryMobileServiceImpl.updateData() 中 prizeType=1 的逻辑
     */
    @DS(DsNameConstants.SHARDING)
    private void grantCouponReward(SurveyDO survey, SurveyAnswerDO answerDO, WxMemberDTO wxMember) {
        Long couponId = survey.getCouponId();
        if (couponId == null) {
            throw new RuntimeException("优惠券ID为空, surveyId=" + survey.getId());
        }

        // 查询优惠券模板
        GoodCouponDO goodCoupon = goodCouponService.getById(couponId);
        if (goodCoupon == null) {
            throw new RuntimeException("优惠券模板不存在, couponId=" + couponId);
        }

        // 复制模板创建用户优惠券
        UserCouponDO coupon = BeanUtils.toBean(goodCoupon, UserCouponDO.class);
        coupon.setId(null);
        coupon.setUserId(wxMember.getMemberId());
        coupon.setCouponId(couponId);
        coupon.setIsUsed(0);
        coupon.setUseTime(null);
        coupon.setMemberMobile(wxMember.getMemberMobile());
        coupon.setCouponSource(CouponSourceType.SURVEY_REWARD.getCode());
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setCouponCreateTime(LocalDateTime.now());
        coupon.setBusinessId(BusinessContextHolder.getBusinessId());

        // 解析优惠券有效期
        parseUserCouponTime(goodCoupon, coupon);
        
        // 插入用户优惠券（走sharding数据源，由ShardingSphere按user_id路由到对应分表）
        userCouponMapper.insert(coupon);
//        // 插入用户优惠券（走sharding数据源，由ShardingSphere按user_id路由到对应分表）
//        lotteryAddLogService.addCoupon(coupon);

        // 更新优惠券已领取数量
        goodCouponService.updateReceivedNumAndCouponNumById(1, couponId);

        log.info("优惠券奖励发放成功, memberId={}, couponId={}, surveyId={}",
                wxMember.getMemberId(), couponId, survey.getId());
    }

    /**
     * 发放优惠券包奖励
     * 参照抽奖模块，调用 CouponPackageService 的通用领取方法
     */
    @DS(DsNameConstants.SHARDING)
    private void grantCouponPackageReward(SurveyDO survey, SurveyAnswerDO answerDO, WxMemberDTO wxMember) {
        Long couponId = survey.getCouponId();
        if (couponId == null) {
            throw new RuntimeException("优惠券包ID为空, surveyId=" + survey.getId());
        }

        // 调用优惠券包的通用领取方法
        couponPackageService.claimCouponPackage(wxMember, couponId,
                CouponSourceType.SURVEY_REWARD.getCode(), null, null);

        log.info("优惠券包奖励发放成功, memberId={}, packageId={}, surveyId={}",
                wxMember.getMemberId(), couponId, survey.getId());
    }

    /**
     * 解析优惠券有效期（复用抽奖模块的时间解析逻辑）
     * useType=0: 固定时间段（使用模板的couponStartTime/couponEndTime）
     * useType=1: 立即生效，N天后过期
     * useType=2: 领券后N天生效，再M天过期
     */
    private void parseUserCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        if (goodCoupon.getUseType() == 0) {
            userCoupon.setExpirationTime(goodCoupon.getCouponEndTime());
            userCoupon.setVaildStartTime(goodCoupon.getCouponStartTime());
        }
        if (goodCoupon.getUseType() == 1) {
            userCoupon.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(
                    java.time.LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1),
                    DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(
                    java.time.LocalDate.now().plusDays(Integer.valueOf(split[0])),
                    DateUtils.YYYY_MM_DD);
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(
                    java.time.LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1),
                    DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    /**
     * 添加积分变更记录（参照抽奖模块 LotteryMobileServiceImpl.addPointsLog）
     * @param wxMember 用户信息
     * @param points 积分数量
     * @param surveyId 问卷ID（作为业务关联ID）
     */
    private void addPointsLog(WxMemberDTO wxMember, int points, Long surveyId) {
        PointsLogDO pointsLog = new PointsLogDO();
        pointsLog.setMemberId(wxMember.getMemberId());
        pointsLog.setMemberMobile(wxMember.getMemberMobile());
        pointsLog.setMemberName(wxMember.getMemberName());
        pointsLog.setMemberNickName(wxMember.getMemberNickName());
        pointsLog.setPointsChange((long) points);
        pointsLog.setPointsType(1); // 1=获取
        pointsLog.setIsDelete(0);
        pointsLog.setPointsLogStatus(1);
        pointsLog.setIsPointsProduct(2);
        pointsLog.setProductType(5); // 5=问卷奖励
        pointsLog.setProductId(surveyId);

        // 生成积分记录ID（与抽奖模块保持一致的分片ID生成逻辑）
        Number number = identifierGenerator.nextId(null);
        long logId = number.longValue();
        pointsLog.setPointsLogId(logId);
        pointsLog.setLogCode(String.valueOf(logId));

        pointsLog.setShardingValue(wxMember.getShardingValue());
        if (pointsLog.getShardingValue() != null) {
            String pointsLogIdStr = logId + "" + pointsLog.getShardingValue();
            BigInteger finalId = (new BigInteger(pointsLogIdStr)).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLog.setPointsLogId(finalId.longValueExact());
        }

        pointsLog.setBusinessId(BusinessContextHolder.getBusinessId());
        pointsLog.setCreateTime(new Date());
        pointsLog.setUpdateTime(new Date());

        pointsLogMapper.insert(pointsLog);
    }

    private String getRewardValue(SurveyDO survey) {
        return switch (survey.getRewardType()) {
            case 1 -> String.valueOf(survey.getRewardPoints());
            case 2, 3 -> String.valueOf(survey.getCouponId());
            default -> null;
        };
    }
}
