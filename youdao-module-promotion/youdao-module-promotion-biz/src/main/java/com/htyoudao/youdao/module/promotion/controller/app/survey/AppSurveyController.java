package com.htyoudao.youdao.module.promotion.controller.app.survey;

import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.promotion.controller.app.survey.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionOptionDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyAnswerMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyQuestionMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyQuestionOptionMapper;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.survey.SurveyAnswerService;
import com.htyoudao.youdao.module.promotion.service.survey.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Tag(name = "小程序 - 问卷调查")
@RestController
@RequestMapping("/promotion/app-survey")
@Validated
public class AppSurveyController {

    @Resource
    private SurveyService surveyService;

    @Resource
    private SurveyAnswerService surveyAnswerService;

    @Resource
    private SurveyQuestionMapper surveyQuestionMapper;

    @Resource
    private SurveyQuestionOptionMapper surveyQuestionOptionMapper;

    @Resource
    private SurveyAnswerMapper surveyAnswerMapper;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.redis.SurveyRedisDAO surveyRedisDAO;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyMapper surveyMapper;

    @Resource
    private ActivityAppService activityAppService;

    private static final RateLimiter rateLimiter = RateLimiter.create(5);

    @GetMapping("/get")
    @Operation(summary = "获取问卷详情（含题目）")
    @Parameter(name = "id", description = "问卷ID", required = true, example = "1")
    @DataPermission(enable = false)
    public CommonResult<AppSurveyRespVO> get(@RequestParam("id") Long id) {
        // 记录UV：仅新手机号才计数，Redis HyperLogLog 立即更新 + 同步持久化 DB
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        if (mobile != null) {
            Long phone = Long.parseLong(mobile);
            if (Boolean.TRUE.equals(surveyRedisDAO.isUvPhoneNew(id, phone))) {
                surveyRedisDAO.addUv(id, phone);         // 立即写入 HyperLogLog，实时生效
                surveyRedisDAO.addUvPhone(id, phone);    // 写入 Set 去重集合
                surveyMapper.updateUvCount(id, surveyRedisDAO.getUv(id)); // 同步回写 DB
            }
        }

        // 优先从缓存获取完整问卷详情（含题目选项）
        Object cached = surveyRedisDAO.getAppSurveyDetail(id);
        if (cached instanceof AppSurveyRespVO cachedVO) {
            // 缓存中 status 已是展示值(0/1/2)，基于它重新计算保证实时性
            Integer computedStatus = resolveStatus(cachedVO.getStatus(),
                    parseTime(cachedVO.getStartTime()), parseTime(cachedVO.getEndTime()));
            cachedVO.setStatus(computedStatus);
            cachedVO.setStatusText(getStatusText(computedStatus));
            // 计算当前用户提交次数上限（用户维度，不缓存）
            if (mobile != null) {
                cachedVO.setSubmitLimitReached(
                        checkSubmitLimitReached(id, Long.parseLong(mobile), cachedVO.getAllowRepeat(), cachedVO.getRepeatLimit()));
            }
            return success(cachedVO);
        }

        // 缓存未命中，从 DB 组装
        SurveyDO survey = surveyService.getSurvey(id);
        if (survey == null) {
            return success(null);
        }

        AppSurveyRespVO respVO = BeanUtils.toBean(survey, AppSurveyRespVO.class);
        // 手动格式化时间为字符串（BeanUtils无法自动转换LocalDateTime→String）
        respVO.setStartTime(formatTime(survey.getStartTime()));
        respVO.setEndTime(formatTime(survey.getEndTime()));
        respVO.setRedirectUrl(survey.getRedirectUrl());

        // 计算展示状态（0-未发布 1-进行中 2-已结束）
        Integer computedStatus = resolveStatus(survey.getStatus(),
                survey.getStartTime(), survey.getEndTime());
        respVO.setStatus(computedStatus);
        respVO.setStatusText(getStatusText(computedStatus));

        // 设置提示消息
        if (Integer.valueOf(0).equals(computedStatus)) {
            respVO.setHintMessage(survey.getStartHint());
        } else if (Integer.valueOf(2).equals(computedStatus)) {
            respVO.setHintMessage(survey.getEndHint());
        }

        // 查询题目和选项
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(id);
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(SurveyQuestionDO::getId).collect(Collectors.toList());
            List<SurveyQuestionOptionDO> allOptions = surveyQuestionOptionMapper.selectListByQuestionIds(questionIds);
            Map<Long, List<SurveyQuestionOptionDO>> optionsMap = allOptions.stream()
                    .collect(Collectors.groupingBy(SurveyQuestionOptionDO::getQuestionId));

            List<AppSurveyRespVO.QuestionVO> questionVOs = questions.stream().map(q -> {
                AppSurveyRespVO.QuestionVO qVO = BeanUtils.toBean(q, AppSurveyRespVO.QuestionVO.class);
                qVO.setQuestionTypeText(getQuestionTypeText(q.getQuestionType()));
                List<SurveyQuestionOptionDO> opts = optionsMap.getOrDefault(q.getId(), Collections.emptyList());
                qVO.setOptions(BeanUtils.toBean(opts, AppSurveyRespVO.OptionVO.class));
                return qVO;
            }).collect(Collectors.toList());
            respVO.setQuestions(questionVOs);
        } else {
            respVO.setQuestions(Collections.emptyList());
        }

        // 写入缓存
        surveyRedisDAO.setAppSurveyDetail(id, respVO);

        // 计算当前用户提交次数上限（用户维度，不缓存）
        if (mobile != null) {
            respVO.setSubmitLimitReached(
                    checkSubmitLimitReached(id, Long.parseLong(mobile), survey.getAllowRepeat(), survey.getRepeatLimit()));
        }

        return success(respVO);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交答卷")
    public CommonResult<AppSurveySubmitRespVO> submit(@Valid @RequestBody AppSurveySubmitReqVO reqVO,
                                                       HttpServletRequest request) {

        boolean acquire = rateLimiter.tryAcquire();
        if (!acquire) {
            throw exception(CLAIM_COUPON_LIMITER);
        }
        // 以手机号为唯一标识
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        if (mobile == null) {
            return CommonResult.error(400, "请先登录");
        }

        boolean canJoin = activityAppService.checkSurveyCanJoin(reqVO.getSurveyId());
        if (!canJoin) {
            throw exception(SURVEY_WECOMGROUP_ERROR);
        }
        Long phone = Long.parseLong(mobile);
        Long memberId = SecurityFrameworkUtils.getLoginUserId();

        // 获取客户端IP
        String sourceIp = ServletUtils.getClientIP(request);

        // 转换答题明细
        List<SurveyAnswerDetailDO> details = reqVO.getAnswers().stream().map(a -> {
            SurveyAnswerDetailDO detail = new SurveyAnswerDetailDO();
            detail.setQuestionId(a.getQuestionId());
            detail.setQuestionNo(a.getQuestionNo());
            // List<Long> → 逗号分隔字符串（DB 存储格式）
            if (a.getSelectedOptionIds() != null && !a.getSelectedOptionIds().isEmpty()) {
                detail.setSelectedOptionIds(a.getSelectedOptionIds().stream()
                        .map(String::valueOf).collect(Collectors.joining(",")));
            }
            // List<FillBlankItem> → JSON 字符串（DB 存储格式）
            if (a.getFillBlankText() != null && !a.getFillBlankText().isEmpty()) {
                detail.setFillBlankText(cn.hutool.json.JSONUtil.toJsonStr(a.getFillBlankText()));
            }
            return detail;
        }).collect(Collectors.toList());

        // 提交答卷（以手机号为用户标识）
        Long answerId = surveyAnswerService.submitAnswer(reqVO.getSurveyId(), phone, memberId, reqVO.getSource(), sourceIp, reqVO.getDuration(), details);

        // 构建响应
        SurveyDO survey = surveyService.getSurvey(reqVO.getSurveyId());
        SurveyAnswerDO answer = surveyAnswerService.getAnswer(answerId);

        AppSurveySubmitRespVO respVO = new AppSurveySubmitRespVO();
        respVO.setAnswerId(answerId);
        respVO.setHasReward(survey != null && survey.getRewardType() != null && survey.getRewardType() > 0);
        if (survey != null) {
            respVO.setRewardType(survey.getRewardType());
            respVO.setRewardTypeText(getRewardTypeText(survey.getRewardType()));
        }
        if (answer != null) {
            respVO.setRewardValue(answer.getRewardValue());
            respVO.setRewardStatus(answer.getRewardStatus());
            respVO.setRewardStatusText(getRewardStatusText(answer.getRewardStatus()));
            respVO.setRewardMessage(buildRewardMessage(survey, answer));
        } else {
            respVO.setRewardStatusText(getRewardStatusText(null));
        }
        respVO.setRedirectUrl(survey != null ? survey.getRedirectUrl() : null);
        // 奖励明细字段
        if (survey != null && survey.getRewardType() != null) {
            if (survey.getRewardType() == 1) {
                respVO.setRewardPoints(survey.getRewardPoints());
            } else if (survey.getRewardType() == 2) {
                respVO.setCouponName(survey.getCouponName());
            } else if (survey.getRewardType() == 3) {
                respVO.setCouponPackageName(survey.getCouponName());
            }
        }
        respVO.setShowThanks(survey != null && survey.getShowThanks() != null && survey.getShowThanks() == 1);
        respVO.setThanksText(survey != null ? survey.getThanksText() : null);

        return success(respVO);
    }

    @GetMapping("/status")
    @Operation(summary = "获取问卷状态")
    @Parameter(name = "id", description = "问卷ID", required = true, example = "1")
    @DataPermission(enable = false)
    public CommonResult<Map<String, Object>> getStatus(@RequestParam("id") Long id) {
        SurveyDO survey = surveyService.getSurvey(id);
        Integer computedStatus = resolveStatus(survey.getStatus(),
                survey.getStartTime(), survey.getEndTime());

        Map<String, Object> result = new HashMap<>();
        result.put("status", computedStatus);
        result.put("statusText", getStatusText(computedStatus));

        // 以手机号检查用户是否已提交（Redis 快速判断 + DB 兜底）
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        boolean hasSubmitted = false;
        if (mobile != null) {
            Long phone = Long.parseLong(mobile);
            Boolean redisSubmitted = surveyRedisDAO.hasSubmitted(id, phone);
            if (Boolean.TRUE.equals(redisSubmitted)) {
                hasSubmitted = true;
            } else {
                hasSubmitted = surveyAnswerService.getMyAnswerPage(phone, 1, Integer.MAX_VALUE)
                        .getList().stream().anyMatch(a -> a.getSurveyId().equals(id));
            }
        }
        result.put("hasSubmitted", hasSubmitted);
        result.put("repeatLimit", survey.getRepeatLimit());

        // 设置提示消息
        String hintMessage = null;
        if (Integer.valueOf(0).equals(computedStatus)) {
            hintMessage = survey.getStartHint();
        } else if (Integer.valueOf(2).equals(computedStatus)) {
            hintMessage = survey.getEndHint();
        }
        result.put("hintMessage", hintMessage);

        return success(result);
    }

    @GetMapping("/getShareVO")
    @Operation(summary = "获取问卷分享详情")
    @Parameter(name = "id", description = "问卷ID", required = true, example = "1")
    @PermitAll
    public CommonResult<AppSurveyShareVO> getShareVO(@RequestParam("id") Long id) {
        AppSurveyShareVO shareVO = surveyService.getShareVO(id);
        return success(shareVO);
    }

    @GetMapping("/my-records")
    @Operation(summary = "我的答卷记录")
    public CommonResult<PageResult<AppMyRecordRespVO>> myRecords(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        String mobile = SecurityFrameworkUtils.getLoginMobile();
        if (mobile == null) {
            return CommonResult.error(400, "请先登录");
        }
        Long phone = Long.parseLong(mobile);
        PageResult<SurveyAnswerDO> pageResult = surveyAnswerService.getMyAnswerPage(phone, pageNo, pageSize);

        PageResult<AppMyRecordRespVO> voPageResult = BeanUtils.toBean(pageResult, AppMyRecordRespVO.class);
        voPageResult.getList().forEach(vo -> {
            vo.setAnswerId(vo.getAnswerId());
            SurveyDO survey = surveyService.getSurvey(vo.getSurveyId());
            if (survey != null) {
                vo.setSurveyName(survey.getSurveyName());
            }
            vo.setRewardTypeText(getRewardTypeText(vo.getRewardType()));
            vo.setRewardStatusText(getRewardStatusText(vo.getRewardStatus()));
        });

        return success(voPageResult);
    }


    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未发布";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }

    private String getQuestionTypeText(Integer questionType) {
        if (questionType == null) return "未知";
        return switch (questionType) {
            case 1 -> "单选题";
            case 2 -> "多选题";
            case 3 -> "填空题";
            default -> "未知";
        };
    }

    private String getRewardTypeText(Integer rewardType) {
        if (rewardType == null || rewardType == 0) return "无";
        return switch (rewardType) {
            case 1 -> "积分";
            case 2 -> "优惠券";
            case 3 -> "优惠券包";
            default -> "未知";
        };
    }

    private String getRewardStatusText(Integer rewardStatus) {
        if (rewardStatus == null) return "未发放";
        return switch (rewardStatus) {
            case 0 -> "未发放";
            case 1 -> "已发放";
            case 2 -> "发放失败";
            default -> "未知";
        };
    }

    private String buildRewardMessage(SurveyDO survey, SurveyAnswerDO answer) {
        if (survey == null || answer == null) {
            return null;
        }
        if (survey.getRewardType() == null || survey.getRewardType() == 0) {
            return null;
        }
        if (Integer.valueOf(1).equals(answer.getRewardStatus())) {
            return switch (survey.getRewardType()) {
                case 1 -> "恭喜您获得" + survey.getRewardPoints() + "积分！";
                case 2 -> "恭喜您获得优惠券：" + survey.getCouponName() + "！";
                case 3 -> "恭喜您获得优惠券包：" + survey.getCouponName() + "！";
                default -> null;
            };
        } else if (Integer.valueOf(2).equals(answer.getRewardStatus())) {
            return "奖励发放失败，请联系客服处理";
        }
        return "奖励发放中，请稍后查看";
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** LocalDateTime → "yyyy-MM-dd HH:mm" 字符串，null 安全 */
    private String formatTime(LocalDateTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    /** "yyyy-MM-dd HH:mm" 字符串 → LocalDateTime，null/空 安全 */
    private LocalDateTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        return LocalDateTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 根据 DB status + 时间值，计算活动的展示状态
     * 0-未发布 1-进行中 2-已结束
     */
    private Integer resolveStatus(Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasStart = startTime != null;
        boolean hasEnd = endTime != null;

        // 没有任何时间值 → 以 DB status 为准
        if (!hasStart && !hasEnd) {
            if (status == null || status == 0) return 0; // 未发布
            if (status == 2) return 2;                    // 已结束
            return 1;                                     // 进行中
        }

        // 有时间值，根据时间计算
        LocalDateTime now = LocalDateTime.now();

        if (hasStart && !hasEnd) {
            return !now.isBefore(startTime) ? 1 : 0;
        }
        if (!hasStart) {
            return !now.isAfter(endTime) ? 1 : 2;
        }

        // 两个都有
        if (now.isBefore(startTime)) return 0;
        if (now.isAfter(endTime)) return 2;
        return 1;
    }

    /**
     * 判断当前用户是否已达到提交次数上限
     * allowRepeat=0 → 提交过即上限；allowRepeat!=0 → 提交次数 >= repeatLimit 即上限
     */
    private boolean checkSubmitLimitReached(Long surveyId, Long phone, Integer allowRepeat, Integer repeatLimit) {
        if (Integer.valueOf(0).equals(allowRepeat)) {
            // 不允许重复：提交过任何一次即达上限
            Boolean redisSubmitted = surveyRedisDAO.hasSubmitted(surveyId, phone);
            if (Boolean.TRUE.equals(redisSubmitted)) {
                return true;
            }
            return surveyAnswerMapper.existsBySurveyIdAndPhone(surveyId, phone);
        }
        // 允许重复：检查是否达到次数上限
        if (repeatLimit != null && repeatLimit > 0) {
            Long count = surveyAnswerMapper.selectCountBySurveyIdAndPhone(surveyId, phone);
            return count >= repeatLimit;
        }
        return false;
    }
}
