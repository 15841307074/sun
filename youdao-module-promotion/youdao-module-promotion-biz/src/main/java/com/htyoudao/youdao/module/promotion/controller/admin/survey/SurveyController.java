package com.htyoudao.youdao.module.promotion.controller.admin.survey;

import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionOptionDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyQuestionMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.SurveyQuestionOptionMapper;
import com.htyoudao.youdao.module.promotion.service.survey.SurveyAnswerService;
import com.htyoudao.youdao.module.promotion.service.survey.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 问卷调查")
@RestController
@RequestMapping("/promotion/survey")
@Validated
public class SurveyController {

    @Resource
    private SurveyService surveyService;

    @Resource
    private SurveyAnswerService surveyAnswerService;

    @Resource
    private SurveyQuestionMapper surveyQuestionMapper;

    @Resource
    private SurveyQuestionOptionMapper surveyQuestionOptionMapper;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.redis.SurveyRedisDAO surveyRedisDAO;


    @PostMapping("/create")
    @Operation(summary = "创建问卷")
    public CommonResult<Long> create(@Valid @RequestBody SurveySaveReqVO reqVO) {
        return success(surveyService.createSurvey(reqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新问卷")
    public CommonResult<Boolean> update(@Valid @RequestBody SurveySaveReqVO reqVO) {
        surveyService.updateSurvey(reqVO);
        return success(true);
    }

    @GetMapping("/delete")
    @Operation(summary = "删除问卷")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    public CommonResult<Boolean> delete(@RequestParam("id") @NotNull(message = "id不能为空") Long id) {
        surveyService.deleteSurvey(id);
        return success(true);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制问卷")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    public CommonResult<Long> copy(@RequestParam("id") @NotNull(message = "id不能为空") Long id) {
        return success(surveyService.copySurvey(id));
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "发布/停用问卷")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody SurveyUpdateStatusReqVO reqVO) {
        surveyService.updateStatus(reqVO.getId(), reqVO.getStatus(), reqVO.getStartTime(), reqVO.getEndTime());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取问卷详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    public CommonResult<SurveyDetailRespVO> get(@RequestParam("id") Long id) {
        SurveyDetailRespVO vo = surveyService.getSurveyDetail(id);
        if (vo != null) {
            vo.setStatus(resolveStatus(vo.getStatus(),
                    vo.getStartTime(), vo.getEndTime()));
        }
        return success(vo);
    }

    @PostMapping("/page")
    @Operation(summary = "获取问卷分页列表")
    public CommonResult<PageResult<SurveyRespVO>> page(@Valid @RequestBody SurveyPageReqVO reqVO) {
        PageResult<SurveyDO> pageResult = surveyService.getSurveyPage(reqVO);
        PageResult<SurveyRespVO> voPageResult = BeanUtils.toBean(pageResult, SurveyRespVO.class);
        // 填充状态文本、奖励类型文本，并从 Redis 获取实时 UV 和提交人数
        voPageResult.getList().forEach(vo -> {
            // 计算展示状态（0-未发布 1-进行中 2-已结束）
            Integer computedStatus = resolveStatus(vo.getStatus(),
                    vo.getStartTime(), vo.getEndTime());
            vo.setStatus(computedStatus);
            vo.setStatusText(getStatusText(computedStatus));
            vo.setRewardTypeText(getRewardTypeText(vo.getRewardType()));
            // 优先从 Redis 获取实时计数（比 DB 更准确）
            Long redisUv = surveyRedisDAO.getUv(vo.getId());
            Long redisSubmitCount = surveyRedisDAO.getSubmitCount(vo.getId());
            if (redisUv > 0) {
                vo.setUvCount(redisUv.intValue());
            }
            if (redisSubmitCount > 0) {
                vo.setSubmitCount(redisSubmitCount.intValue());
            }
        });
        return success(voPageResult);
    }


    @GetMapping("/selectSpread")
    @Operation(summary = "查询问卷活动的推广")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1")
    public CommonResult<SurveySpreadRespVO> selectSpread(@RequestParam("id") Long id) {
        return success(surveyService.selectSpread(id));
    }

    @PostMapping("/updateSpread")
    @Operation(summary = "修改问卷活动的推广")
    public CommonResult<Boolean> updateSpread(@Valid @RequestBody SurveySpreadSaveReqVO reqVO) {
        surveyService.updateSpread(reqVO);
        return success(true);
    }

    // ========== 数据统计 ==========

    @GetMapping("/stats")
    @Operation(summary = "获取题目统计概览")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1")
    public CommonResult<SurveyStatsRespVO> getStats(@RequestParam("id") Long id) {
        return success(surveyService.getStats(id));
    }

    @GetMapping("/export")
    @Operation(summary = "导出问卷答卷数据")
    @Parameter(name = "id", description = "问卷编号", required = true, example = "1")
    public void export(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {
        surveyService.exportAnswers(id, response);
    }

    @PostMapping("/fillBlankAnswers")
    @Operation(summary = "获取填空题答案分页列表")
    public CommonResult<PageResult<FillBlankAnswerRespVO>> fillBlankAnswers(
            @RequestBody FillBlankAnswerPageReqVO fillBlankAnswerPageReqVO) {
        return success(surveyAnswerService.getFillBlankAnswerPage(fillBlankAnswerPageReqVO));
    }

    @GetMapping("/fillBlankAnswers/export")
    @Operation(summary = "导出填空题答案")
    @Parameter(name = "surveyId", description = "问卷ID", required = true, example = "1")
    @Parameter(name = "questionId", description = "题目ID", required = true, example = "1")
    public void exportFillBlankAnswers(@RequestParam("surveyId") Long surveyId,
                                        @RequestParam("questionId") Long questionId,
                                        HttpServletResponse response) throws IOException {
        surveyAnswerService.exportFillBlankAnswers(surveyId, questionId, response);
    }

    @PostMapping("/answers/page")
    @Operation(summary = "获取答卷明细列表")
    public CommonResult<PageResult<SurveyAnswerDO>> answerPage(@Valid @RequestBody SurveyAnswerPageReqVO reqVO) {
        return success(surveyAnswerService.getAnswerPage(reqVO));
    }

    @GetMapping("/answers/detail")
    @Operation(summary = "获取单份答卷详情")
    @Parameter(name = "answerId", description = "答卷ID", required = true, example = "1")
    public CommonResult<SurveyAnswerDetailRespVO> answerDetail(@RequestParam("answerId") Long answerId) {
        SurveyAnswerDO answer = surveyAnswerService.getAnswer(answerId);
        if (answer == null) {
            return success(null);
        }
        List<SurveyAnswerDetailDO> details = surveyAnswerService.getAnswerDetails(answerId);

        SurveyAnswerDetailRespVO respVO = BeanUtils.toBean(answer, SurveyAnswerDetailRespVO.class);
        respVO.setAnswerId(answer.getId());
        // 设置来源文本
        respVO.setSourceText(getSourceText(answer.getSource()));
        // 来源IP拼接城市
        if (StrUtil.isNotBlank(answer.getSourceIp())) {
            respVO.setSourceIp(formatIpWithCity(answer.getSourceIp()));
        }

        // 查询该问卷的所有题目和选项，用于填充 questionTitle 和 answerText
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(answer.getSurveyId());
        Map<Long, SurveyQuestionDO> questionMap = questions.stream()
                .collect(Collectors.toMap(SurveyQuestionDO::getId, q -> q));
        Map<Long, List<SurveyQuestionOptionDO>> optionsMap = new HashMap<>();
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(SurveyQuestionDO::getId).collect(Collectors.toList());
            List<SurveyQuestionOptionDO> allOptions = surveyQuestionOptionMapper.selectListByQuestionIds(questionIds);
            optionsMap = allOptions.stream().collect(Collectors.groupingBy(SurveyQuestionOptionDO::getQuestionId));
        }

        List<SurveyAnswerDetailRespVO.DetailVO> detailVOs = new java.util.ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            SurveyAnswerDetailDO detailDO = details.get(i);
            SurveyAnswerDetailRespVO.DetailVO dvo = new SurveyAnswerDetailRespVO.DetailVO();
            // 手动拷贝基础字段
            dvo.setQuestionNo(detailDO.getQuestionNo());
            dvo.setQuestionId(detailDO.getQuestionId());

            // 设置题目标题、类型、必答
            SurveyQuestionDO question = questionMap.get(detailDO.getQuestionId());
            if (question != null) {
                dvo.setQuestionTitle(question.getQuestionTitle());
                dvo.setQuestionType(question.getQuestionType());
                dvo.setIsRequired(question.getIsRequired());
            }

            // selectedOptionIds：逗号分隔 String → List<Long>
            String idsStr = detailDO.getSelectedOptionIds();
            if (cn.hutool.core.util.StrUtil.isNotBlank(idsStr)) {
                dvo.setSelectedOptionIds(java.util.Arrays.stream(idsStr.split(","))
                        .map(String::trim).filter(cn.hutool.core.util.StrUtil::isNotBlank)
                        .map(Long::parseLong).collect(java.util.stream.Collectors.toList()));
            }

            // 构建 answerText：选项题拼接选项文本，填空题取填空文本
            StringBuilder answerTextBuilder = new StringBuilder();
            if (question != null && question.getQuestionType() != 3 && StrUtil.isNotBlank(idsStr)) {
                // 单选/多选题：将选项ID转换为选项文本
                List<SurveyQuestionOptionDO> options = optionsMap.getOrDefault(detailDO.getQuestionId(), Collections.emptyList());
                Map<Long, String> optionTextMap = options.stream()
                        .collect(Collectors.toMap(SurveyQuestionOptionDO::getId, SurveyQuestionOptionDO::getOptionText));
                List<String> texts = java.util.Arrays.stream(idsStr.split(","))
                        .map(String::trim).filter(cn.hutool.core.util.StrUtil::isNotBlank)
                        .map(Long::parseLong)
                        .map(optionTextMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                answerTextBuilder.append(String.join("、", texts));
            } else if (StrUtil.isNotBlank(detailDO.getFillBlankText())) {
                // 填空题：从 JSON 中提取纯文本
                String fbText = detailDO.getFillBlankText().trim();
                if (fbText.startsWith("[")) {
                    // JSON数组格式：[{"optionId":"101","text":"速度"}]
                    try {
                        List<cn.hutool.json.JSONObject> items = cn.hutool.json.JSONUtil.toList(fbText, cn.hutool.json.JSONObject.class);
                        String text = items.stream()
                                .map(obj -> obj.getStr("text", ""))
                                .filter(cn.hutool.core.util.StrUtil::isNotBlank)
                                .collect(Collectors.joining("、"));
                        answerTextBuilder.append(text);
                    } catch (Exception e) {
                        answerTextBuilder.append(detailDO.getFillBlankText());
                    }
                } else if (fbText.startsWith("{")) {
                    // JSON对象格式：{"101":"速度"}
                    try {
                        java.util.Map<String, String> map = cn.hutool.json.JSONUtil.toBean(fbText,
                                new cn.hutool.core.lang.TypeReference<java.util.Map<String, String>>() {}, false);
                        answerTextBuilder.append(String.join("、", map.values()));
                    } catch (Exception e) {
                        answerTextBuilder.append(detailDO.getFillBlankText());
                    }
                } else {
                    // 纯文本
                    answerTextBuilder.append(detailDO.getFillBlankText());
                }
            }
            dvo.setAnswerText(answerTextBuilder.toString());

            // fillBlankText：JSON String → List<FillBlankItem>（兼容旧格式数据）
            String fbText = detailDO.getFillBlankText();
            if (cn.hutool.core.util.StrUtil.isNotBlank(fbText)) {
                if (fbText.trim().startsWith("[")) {
                    // 新格式：JSON 数组 [{"optionId":"101","text":"苹果"}]
                    try {
                        dvo.setFillBlankText(cn.hutool.json.JSONUtil.toList(fbText,
                                SurveyAnswerDetailRespVO.FillBlankItem.class));
                    } catch (Exception ignored) {}
                } else if (fbText.trim().startsWith("{")) {
                    // 旧格式：JSON 对象 {"101":"苹果"} → 转换为 List
                    try {
                        java.util.Map<String, String> map = cn.hutool.json.JSONUtil.toBean(fbText,
                                new cn.hutool.core.lang.TypeReference<java.util.Map<String, String>>() {}, false);
                        List<SurveyAnswerDetailRespVO.FillBlankItem> items = new java.util.ArrayList<>();
                        map.forEach((k, v) -> {
                            SurveyAnswerDetailRespVO.FillBlankItem item = new SurveyAnswerDetailRespVO.FillBlankItem();
                            item.setOptionId(k);
                            item.setText(v);
                            items.add(item);
                        });
                        dvo.setFillBlankText(items);
                    } catch (Exception ignored) {}
                } else {
                    // 最早格式：纯文本，以题目ID为 optionId 包装成 List
                    SurveyAnswerDetailRespVO.FillBlankItem item = new SurveyAnswerDetailRespVO.FillBlankItem();
                    item.setOptionId(String.valueOf(detailDO.getQuestionId()));
                    item.setText(fbText);
                    dvo.setFillBlankText(java.util.Collections.singletonList(item));
                }
            }
            detailVOs.add(dvo);
        }
        respVO.setDetails(detailVOs);
        return success(respVO);
    }

    @GetMapping("/reward-records")
    @Operation(summary = "获取奖励发放记录")
    @Parameter(name = "surveyId", description = "问卷ID", required = true, example = "1")
    public CommonResult<PageResult<SurveyAnswerDO>> rewardRecords(
            @RequestParam("surveyId") Long surveyId,
            @RequestParam(value = "rewardStatus", required = false) Integer rewardStatus,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        SurveyAnswerPageReqVO reqVO = new SurveyAnswerPageReqVO();
        reqVO.setSurveyId(surveyId);
        reqVO.setRewardStatus(rewardStatus);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        return success(surveyAnswerService.getAnswerPage(reqVO));
    }

    // ========== 私有方法 ==========

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未发布";
            case 1 -> "进行中";
            case 2 -> "已结束";
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

    private String getSourceText(Integer source) {
        if (source == null) return "未知";
        return switch (source) {
            case 1 -> "微信";
            case 2 -> "支付宝";
            default -> "未知";
        };
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
     * 将 IP 地址格式化为 "IP (省-市)" 形式，如 "123.245.48.39 (辽宁-沈阳)"
     */
    private String formatIpWithCity(String ip) {
        if (StrUtil.isBlank(ip)) {
            return ip;
        }
        try {
            com.htyoudao.youdao.framework.ip.core.Area area = com.htyoudao.youdao.framework.ip.core.utils.IPUtils.getArea(ip);
            if (area == null) {
                return ip;
            }
            String province = null;
            String city = null;
            com.htyoudao.youdao.framework.ip.core.Area current = area;
            for (int i = 0; i < 10 && current != null; i++) {
                if (current.getType() != null) {
                    if (current.getType() == 2) {
                        province = stripAreaSuffix(current.getName());
                    } else if (current.getType() == 3) {
                        city = stripAreaSuffix(current.getName());
                    }
                }
                current = current.getParent();
            }
            if (province != null && city != null) {
                return ip + " (" + province + "-" + city + ")";
            } else if (province != null) {
                return ip + " (" + province + ")";
            }
            return ip;
        } catch (Exception e) {
            return ip;
        }
    }

    private String stripAreaSuffix(String name) {
        if (name == null) return null;
        return name.replaceAll("(省|市|自治区|特别行政区)$", "");
    }
}
