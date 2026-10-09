package com.htyoudao.youdao.module.promotion.service.survey;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.promotion.controller.admin.survey.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.survey.vo.AppSurveyShareVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyAnswerDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.survey.SurveyQuestionOptionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.survey.*;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    @Resource
    private SurveyMapper surveyMapper;

    @Resource
    private SurveyQuestionMapper surveyQuestionMapper;

    @Resource
    private SurveyQuestionOptionMapper surveyQuestionOptionMapper;

    @Resource
    private SurveyAnswerMapper surveyAnswerMapper;

    @Resource
    private SurveyAnswerDetailMapper surveyAnswerDetailMapper;

    @Resource
    private ActivityChannelService activityChannelService;

    @Resource
    private com.htyoudao.youdao.module.promotion.dal.redis.SurveyRedisDAO surveyRedisDAO;

    // 分享图片
    @Value("${activity.wj.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.wj.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.wj.shareDescription}")
    private String shareDescription;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSurvey(SurveySaveReqVO reqVO) {
        // 创建问卷主表
        SurveyDO surveyDO = BeanUtils.toBean(reqVO, SurveyDO.class);
        // VO 的 startTime/endTime 是 String，DO 是 LocalDateTime，需手动转换
        surveyDO.setStartTime(parseDateTime(reqVO.getStartTime()));
        surveyDO.setEndTime(parseDateTime(reqVO.getEndTime()));
        surveyDO.setId(null);
        surveyDO.setStatus(0); // 未发布
        // 清理时间字段：开关关闭时置 null，同时兜底清理 epoch 脏值
        cleanTimeFields(surveyDO);
        surveyDO.setUvCount(0);
        surveyDO.setSubmitCount(0);
        surveyDO.setShareTitle(shareTitle);
        surveyDO.setShareImgUrl(shareImageUrl);
        surveyDO.setShareNote(shareDescription);
        surveyMapper.insert(surveyDO);

        createChannelDO(surveyDO.getId(), 12);

        // 初始化 Redis 提交计数
        surveyRedisDAO.setSubmitCount(surveyDO.getId(), 0L);

        // 创建题目和选项
        saveQuestionsAndOptions(surveyDO.getId(), reqVO.getQuestions());

        return surveyDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSurvey(SurveySaveReqVO reqVO) {
        // 校验存在
        SurveyDO existSurvey = validateSurveyExists(reqVO.getId());
        // 仅未发布状态可编辑
//        if (!Integer.valueOf(0).equals(existSurvey.getStatus())) {
//            throw exception(SURVEY_NOT_ALLOW_EDIT);
//        }

        // 更新问卷主表
        SurveyDO updateDO = BeanUtils.toBean(reqVO, SurveyDO.class);
        // VO 的 startTime/endTime 是 String，DO 是 LocalDateTime，需手动转换
        updateDO.setStartTime(parseDateTime(reqVO.getStartTime()));
        updateDO.setEndTime(parseDateTime(reqVO.getEndTime()));
        cleanTimeFields(updateDO);
        surveyMapper.updateById(updateDO);

        // 删除旧题目和选项，重新创建
        surveyQuestionOptionMapper.deleteByQuestionIds(
                surveyQuestionMapper.selectListBySurveyId(reqVO.getId())
                        .stream().map(SurveyQuestionDO::getId).collect(Collectors.toList()));
        surveyQuestionMapper.deleteBySurveyId(reqVO.getId());

        // 重新创建题目和选项
        saveQuestionsAndOptions(reqVO.getId(), reqVO.getQuestions());

        // 清除问卷详情缓存（含小程序端完整缓存）
        surveyRedisDAO.deleteSurveyDetail(reqVO.getId());
        surveyRedisDAO.deleteAppSurveyDetail(reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSurvey(Long id) {
        SurveyDO survey = validateSurveyExists(id);
        // 进行中的问卷不可删除
        if (Integer.valueOf(1).equals(survey.getStatus())) {
            throw exception(SURVEY_NOT_ALLOW_DELETE);
        }
        // 删除题目和选项
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(id);
        if (!questions.isEmpty()) {
            surveyQuestionOptionMapper.deleteByQuestionIds(
                    questions.stream().map(SurveyQuestionDO::getId).collect(Collectors.toList()));
        }
        surveyQuestionMapper.deleteBySurveyId(id);
        // 删除问卷
        surveyMapper.deleteById(id);

        // 清除问卷所有缓存
        surveyRedisDAO.deleteAllSurveyCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copySurvey(Long id) {
        SurveyDO source = validateSurveyExists(id);
        // 复制问卷主表
        SurveyDO copyDO = BeanUtils.toBean(source, SurveyDO.class);
        copyDO.setId(null);
        copyDO.setSurveyName(source.getSurveyName() + "（副本）");
        copyDO.setStatus(0); // 未发布
        copyDO.setUvCount(0);
        copyDO.setSubmitCount(0);
        // 分享配置重置为默认值（与创建时一致）
        copyDO.setShareTitle(shareTitle);
        copyDO.setShareImgUrl(shareImageUrl);
        copyDO.setShareNote(shareDescription);
        // 时间控制重置：清空开始/结束时间
        copyDO.setTimeControl(0);
        copyDO.setStartTime(null);
        copyDO.setStartTimeEnabled(0);
        copyDO.setEndTime(null);
        copyDO.setEndTimeEnabled(0);
        copyDO.setCreateTime(LocalDateTime.now());
        surveyMapper.insert(copyDO);

        // 创建推广渠道
        createChannelDO(copyDO.getId(), 12);

        // 复制题目和选项
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(id);
        for (SurveyQuestionDO question : questions) {
            SurveyQuestionDO copyQuestion = BeanUtils.toBean(question, SurveyQuestionDO.class);
            copyQuestion.setId(null);
            copyQuestion.setSurveyId(copyDO.getId());
            copyQuestion.setCreateTime(LocalDateTime.now());
            surveyQuestionMapper.insert(copyQuestion);

            List<SurveyQuestionOptionDO> options = surveyQuestionOptionMapper.selectListByQuestionId(question.getId());
            for (SurveyQuestionOptionDO option : options) {
                SurveyQuestionOptionDO copyOption = BeanUtils.toBean(option, SurveyQuestionOptionDO.class);
                copyOption.setId(null);
                copyOption.setQuestionId(copyQuestion.getId());
                copyOption.setCreateTime(LocalDateTime.now());
                surveyQuestionOptionMapper.insert(copyOption);
            }
        }

        // 初始化副本的 Redis 计数，清除源问卷缓存
        surveyRedisDAO.setSubmitCount(copyDO.getId(), 0L);
        surveyRedisDAO.deleteSurveyDetail(id);

        return copyDO.getId();
    }

    @Override
    public void updateStatus(Long id, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        SurveyDO survey = validateSurveyExists(id);
        // 截断到分钟，不保留秒
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        if (Integer.valueOf(1).equals(status)) {
            // 发布：仅未发布状态可发布
//            if (!Integer.valueOf(0).equals(survey.getStatus())) {
//                throw exception(SURVEY_NOT_ALLOW_PUBLISH);
//            }
            // 校验至少2道题目
//            Long questionCount = surveyQuestionMapper.selectCount(
//                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SurveyQuestionDO>()
//                            .eq(SurveyQuestionDO::getSurveyId, id));
//            if (questionCount < 2) {
//                throw exception(SURVEY_QUESTION_NOT_ENOUGH);
//            }
            // 发布时：设置开始时间为当前时间
            LambdaUpdateWrapper<SurveyDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SurveyDO::getId, id)
                    .set(SurveyDO::getStatus, 1)
                    .set(SurveyDO::getStartTime, now)
                    .set(SurveyDO::getStartTimeEnabled, 1);
            surveyMapper.update(null, updateWrapper);
        } else if (Integer.valueOf(2).equals(status)) {
            // 停用：仅进行中状态可停用
//            if (!Integer.valueOf(1).equals(survey.getStatus())) {
//                throw exception(SURVEY_NOT_ALLOW_STOP);
//            }
            // 停用时：设置结束时间为当前时间
            LambdaUpdateWrapper<SurveyDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SurveyDO::getId, id)
                    .set(SurveyDO::getStatus, 2)
                    .set(SurveyDO::getEndTime, now)
                    .set(SurveyDO::getEndTimeEnabled, 1);
            surveyMapper.update(null, updateWrapper);
        }

        // 清除问卷详情缓存（含小程序端完整缓存）
        surveyRedisDAO.deleteSurveyDetail(id);
        surveyRedisDAO.deleteAppSurveyDetail(id);
    }

    @Override
    public SurveyDO getSurvey(Long id) {
        // 优先读缓存
        Object cached = surveyRedisDAO.getSurveyDetail(id);
        if (cached instanceof SurveyDO) {
            return (SurveyDO) cached;
        }
        SurveyDO survey = surveyMapper.selectById(id);
        if (survey != null) {
            surveyRedisDAO.setSurveyDetail(id, survey);
        }
        return survey;
    }

    @Override
    public SurveyDetailRespVO getSurveyDetail(Long id) {
        SurveyDO survey = validateSurveyExists(id);
        SurveyDetailRespVO respVO = BeanUtils.toBean(survey, SurveyDetailRespVO.class);

        // 查询题目
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(id);
        if (!questions.isEmpty()) {
            // 查询所有选项
            List<Long> questionIds = questions.stream().map(SurveyQuestionDO::getId).collect(Collectors.toList());
            List<SurveyQuestionOptionDO> allOptions = surveyQuestionOptionMapper.selectListByQuestionIds(questionIds);
            Map<Long, List<SurveyQuestionOptionDO>> optionsMap = allOptions.stream()
                    .collect(Collectors.groupingBy(SurveyQuestionOptionDO::getQuestionId));

            // 组装题目和选项
            List<SurveyDetailRespVO.QuestionVO> questionVOs = questions.stream().map(q -> {
                SurveyDetailRespVO.QuestionVO qVO = BeanUtils.toBean(q, SurveyDetailRespVO.QuestionVO.class);
                List<SurveyQuestionOptionDO> opts = optionsMap.getOrDefault(q.getId(), Collections.emptyList());
                qVO.setOptions(BeanUtils.toBean(opts, SurveyDetailRespVO.OptionVO.class));
                return qVO;
            }).collect(Collectors.toList());

            respVO.setQuestions(questionVOs);
        } else {
            respVO.setQuestions(Collections.emptyList());
        }

        return respVO;
    }

    @Override
    public PageResult<SurveyDO> getSurveyPage(SurveyPageReqVO reqVO) {
        return surveyMapper.selectPage(reqVO);
    }

    @Override
    public SurveySpreadRespVO selectSpread(Long id) {
        SurveyDO survey = validateSurveyExists(id);
        SurveySpreadRespVO respVO = new SurveySpreadRespVO();
        respVO.setId(id);
        respVO.setSurveyType(12);
        respVO.setShareTitle(survey.getShareTitle());
        respVO.setShareNote(survey.getShareNote());
        respVO.setShareImgUrl(survey.getShareImgUrl());

        // 查询推广渠道列表
        List<ActivityChannelDO> channelList = activityChannelService.selectByActivityId(id);
        if (channelList != null && !channelList.isEmpty()) {
            List<ActivityChannelRespVO> channelRespVOS = new ArrayList<>();
            channelList.forEach(channel -> {
                ActivityChannelRespVO channelRespVO = new ActivityChannelRespVO();
                BeanUtils.copyProperties(channel, channelRespVO);
                channelRespVOS.add(channelRespVO);
            });
            respVO.setActivityChannelRespVOS(channelRespVOS);
        }
        return respVO;
    }

    @Override
    public void updateSpread(SurveySpreadSaveReqVO reqVO) {
        validateSurveyExists(reqVO.getId());
        LambdaUpdateWrapper<SurveyDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SurveyDO::getId, reqVO.getId())
                .set(SurveyDO::getShareTitle, reqVO.getShareTitle())
                .set(SurveyDO::getShareImgUrl, reqVO.getShareImgUrl())
                .set(SurveyDO::getShareNote, reqVO.getShareNote());
        surveyMapper.update(updateWrapper);

        // 清除问卷详情缓存（含小程序端完整缓存）
        surveyRedisDAO.deleteSurveyDetail(reqVO.getId());
        surveyRedisDAO.deleteAppSurveyDetail(reqVO.getId());
    }

    @Override
    public SurveyStatsRespVO getStats(Long id) {
        validateSurveyExists(id);

        // 查询题目
        List<SurveyQuestionDO> questions = surveyQuestionMapper.selectListBySurveyId(id);
        // 查询总答卷数
        Long totalAnswers = surveyAnswerMapper.selectCountBySurveyId(id);

        List<SurveyStatsRespVO.QuestionStatsVO> questionStatsList = new ArrayList<>();

        for (SurveyQuestionDO question : questions) {
            SurveyStatsRespVO.QuestionStatsVO qStats = new SurveyStatsRespVO.QuestionStatsVO();
            qStats.setQuestionId(question.getId());
            qStats.setQuestionNo(question.getQuestionNo());
            qStats.setQuestionTitle(question.getQuestionTitle());
            qStats.setQuestionType(question.getQuestionType());
            qStats.setQuestionTypeText(getQuestionTypeText(question.getQuestionType()));

            // 查询该题的答题明细
            List<SurveyAnswerDetailDO> details = surveyAnswerDetailMapper.selectListBySurveyIdAndQuestionId(id, question.getId());
            qStats.setValidCount(details.size());

            // 统计选项
            if (question.getQuestionType() != 3) { // 非填空题
                List<SurveyQuestionOptionDO> options = surveyQuestionOptionMapper.selectListByQuestionId(question.getId());
                List<SurveyStatsRespVO.OptionStatsVO> optionStatsList = new ArrayList<>();
                for (SurveyQuestionOptionDO option : options) {
                    long count = details.stream()
                            .filter(d -> StrUtil.isNotBlank(d.getSelectedOptionIds())
                                    && Arrays.asList(d.getSelectedOptionIds().split(",")).contains(String.valueOf(option.getId())))
                            .count();
                    SurveyStatsRespVO.OptionStatsVO oStats = new SurveyStatsRespVO.OptionStatsVO();
                    oStats.setOptionId(option.getId());
                    oStats.setOptionText(option.getOptionText());
                    oStats.setCount(count);
                    oStats.setPercent(details.isEmpty() ? 0.0 : Math.round(count * 10000.0 / details.size()) / 100.0);
                    optionStatsList.add(oStats);
                }
                qStats.setOptions(optionStatsList);
            }

            questionStatsList.add(qStats);
        }

        SurveyStatsRespVO respVO = new SurveyStatsRespVO();
        respVO.setSurveyId(id);
        respVO.setTotalAnswers(totalAnswers);
        respVO.setUvCount(surveyRedisDAO.getUv(id));
        respVO.setQuestions(questionStatsList);
        return respVO;
    }

    @Override
    public void exportAnswers(Long id, HttpServletResponse response) {
        SurveyDO survey = validateSurveyExists(id);

        // 查询题目（按序号排序）
        List<SurveyQuestionDO> questions = null;
        try {
            questions = surveyQuestionMapper.selectListBySurveyId(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        questions.sort(Comparator.comparingInt(SurveyQuestionDO::getQuestionNo));

        // 查询所有选项（用于将选项ID转换为选项文本）
        Map<Long, SurveyQuestionOptionDO> optionMap = new HashMap<>();
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(SurveyQuestionDO::getId).collect(Collectors.toList());
            List<SurveyQuestionOptionDO> allOptions = surveyQuestionOptionMapper.selectListByQuestionIds(questionIds);
            allOptions.forEach(opt -> optionMap.put(opt.getId(), opt));
        }

        // 构建动态表头
        List<List<String>> head = new ArrayList<>();
        head.add(Collections.singletonList("序号"));
        head.add(Collections.singletonList("提交答卷时间"));
        head.add(Collections.singletonList("所用时间"));
        head.add(Collections.singletonList("来源"));
        head.add(Collections.singletonList("来自IP"));
        for (SurveyQuestionDO q : questions) {
            head.add(Collections.singletonList(q.getQuestionNo() + ". " + q.getQuestionTitle()));
        }

        // 查询所有答卷
        List<SurveyAnswerDO> answers = surveyAnswerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SurveyAnswerDO>()
                        .eq(SurveyAnswerDO::getSurveyId, id)
                        .orderByAsc(SurveyAnswerDO::getSubmitTime));

        // 构建数据行
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        List<List<Object>> data = new ArrayList<>();
        int seq = 0;
        for (SurveyAnswerDO answer : answers) {
            seq++;
            List<Object> row = new ArrayList<>();
            row.add(seq);
            row.add(answer.getSubmitTime() != null ? answer.getSubmitTime().format(dtf) : "");
            row.add(answer.getDuration() != null ? answer.getDuration() + "秒" : "");
            row.add(getSourceText(answer.getSource()));
            row.add(answer.getSourceIp() != null ? formatIpWithCity(answer.getSourceIp()) : "");
            // 查询该答卷的答题明细
            List<SurveyAnswerDetailDO> details = surveyAnswerDetailMapper.selectListByAnswerId(answer.getId());
            Map<Long, SurveyAnswerDetailDO> detailMap = details.stream()
                    .collect(Collectors.toMap(SurveyAnswerDetailDO::getQuestionId, d -> d, (a, b) -> a));
            // 按题目顺序填充答案
            for (SurveyQuestionDO q : questions) {
                SurveyAnswerDetailDO detail = detailMap.get(q.getId());
                if (detail == null) {
                    row.add("");
                } else if (q.getQuestionType() == 3) {
                    // 填空题：将 JSON 解析为纯文本
                    row.add(extractFillBlankText(detail.getFillBlankText()));
                } else {
                    // 单选/多选：将选项ID转为选项文本
                    String answerText = buildAnswerText(detail.getSelectedOptionIds(), detail.getFillBlankText(), optionMap);
                    row.add(answerText);
                }
            }
            data.add(row);
        }

        // 写出Excel
        String filename = survey.getSurveyName() + "_" + id;
        try {
            EasyExcel.write(response.getOutputStream())
                    .autoCloseStream(false)
                    .head(head)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("问卷数据")
                    .doWrite(data);
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8.name()));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        } catch (IOException e) {
            log.error("导出问卷答卷数据失败, surveyId={}", id, e);
            throw new RuntimeException("导出失败");
        }
    }

    private String getSourceText(Integer source) {
        if (source == null) return "";
        return switch (source) {
            case 1 -> "微信";
            case 2 -> "支付宝";
            default -> "";
        };
    }

    private String buildAnswerText(String selectedOptionIds, String fillBlankText, Map<Long, SurveyQuestionOptionDO> optionMap) {
        // 解析 fillBlankText：JSON数组/JSON map（多选项各自填空）或纯文本（向后兼容）
        Map<String, String> fillBlankMap = null;
        if (StrUtil.isNotBlank(fillBlankText)) {
            String trimmed = fillBlankText.trim();
            if (trimmed.startsWith("{")) {
                try {
                    fillBlankMap = cn.hutool.json.JSONUtil.toBean(trimmed, new cn.hutool.core.lang.TypeReference<Map<String, String>>() {}, false);
                } catch (Exception e) {
                    log.warn("fillBlankText JSON 解析失败，按纯文本处理: {}", fillBlankText);
                }
            } else if (trimmed.startsWith("[")) {
                // JSON数组格式：[{"optionId":"101","text":"速度"}] → 转为Map便于后续统一处理
                try {
                    List<cn.hutool.json.JSONObject> items = cn.hutool.json.JSONUtil.toList(trimmed, cn.hutool.json.JSONObject.class);
                    fillBlankMap = new java.util.LinkedHashMap<>();
                    for (cn.hutool.json.JSONObject item : items) {
                        String optId = item.getStr("optionId", "");
                        String text = item.getStr("text", "");
                        if (StrUtil.isNotBlank(optId)) {
                            fillBlankMap.put(optId, text);
                        } else if (StrUtil.isNotBlank(text)) {
                            // 无optionId时（问答题），用空key存储
                            fillBlankMap.put("", text);
                        }
                    }
                } catch (Exception e) {
                    log.warn("fillBlankText JSON数组解析失败，按纯文本处理: {}", fillBlankText);
                }
            }
        }

        if (StrUtil.isBlank(selectedOptionIds)) {
            // 无选项选中（可能是纯填空题）
            if (fillBlankMap != null) {
                // JSON 格式但无选项 → 取第一个值
                return fillBlankMap.values().stream().findFirst().orElse("");
            }
            return StrUtil.isNotBlank(fillBlankText) ? fillBlankText : "";
        }

        StringBuilder sb = new StringBuilder();
        String[] ids = selectedOptionIds.split(",");
        for (String idStr : ids) {
            try {
                Long optId = Long.parseLong(idStr.trim());
                SurveyQuestionOptionDO option = optionMap.get(optId);
                if (option != null) {
                    if (!sb.isEmpty()) sb.append("┋");
                    sb.append(option.getOptionText());
                    // 如果有该选项的填空文本，追加在括号内
                    if (fillBlankMap != null && fillBlankMap.containsKey(idStr.trim())) {
                        String optFill = fillBlankMap.get(idStr.trim());
                        if (StrUtil.isNotBlank(optFill)) {
                            sb.append("（").append(optFill).append("）");
                        }
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
        // 纯文本模式（非 JSON）：追加在末尾
        if (fillBlankMap == null && StrUtil.isNotBlank(fillBlankText)) {
            if (!sb.isEmpty()) sb.append("┋");
            sb.append(fillBlankText);
        }
        return sb.toString();
    }

    // ========== 私有方法 ==========

    /**
     * 将 IP 地址格式化为 "省-市" 形式，如 "123.245.48.39 (辽宁-沈阳)"
     * 去掉行政区划后缀（省/市/自治区/特别行政区）
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
            // 向上查找省份（type=2）和城市（type=3）
            String province = null;
            String city = null;
            com.htyoudao.youdao.framework.ip.core.Area current = area;
            for (int i = 0; i < 10 && current != null; i++) {
                if (current.getType() != null) {
                    if (current.getType() == 2) { // 省份
                        province = stripAreaSuffix(current.getName());
                    } else if (current.getType() == 3) { // 城市
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
            log.warn("IP地址解析城市失败: {}", ip, e);
            return ip;
        }
    }

    /**
     * 去掉行政区划名称的后缀（省/市/自治区/特别行政区）
     */
    private String stripAreaSuffix(String name) {
        if (name == null) return null;
        return name.replaceAll("(省|市|自治区|特别行政区)$", "");
    }

    private SurveyDO validateSurveyExists(Long id) {
        SurveyDO survey = surveyMapper.selectById(id);
        if (survey == null) {
            throw exception(SURVEY_NOT_EXISTS);
        }
        return survey;
    }

    private void saveQuestionsAndOptions(Long surveyId, List<SurveySaveReqVO.QuestionVO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        int sortOrder = 0;
        for (int i = 0; i < questions.size(); i++) {
            SurveySaveReqVO.QuestionVO qVO = questions.get(i);
            SurveyQuestionDO questionDO = BeanUtils.toBean(qVO, SurveyQuestionDO.class);
            questionDO.setId(null);
            questionDO.setSurveyId(surveyId);
            questionDO.setQuestionNo(i + 1);
            questionDO.setSortOrder(sortOrder++);
            surveyQuestionMapper.insert(questionDO);

            // 保存选项
            if (qVO.getOptions() != null && !qVO.getOptions().isEmpty()) {
                int optSortOrder = 0;
                for (SurveySaveReqVO.OptionVO oVO : qVO.getOptions()) {
                    SurveyQuestionOptionDO optionDO = BeanUtils.toBean(oVO, SurveyQuestionOptionDO.class);
                    optionDO.setId(null);
                    optionDO.setQuestionId(questionDO.getId());
                    optionDO.setSortOrder(optSortOrder++);
                    surveyQuestionOptionMapper.insert(optionDO);
                }
            }
        }
    }

    private String getQuestionTypeText(Integer questionType) {
        return switch (questionType) {
            case 1 -> "单选题";
            case 2 -> "多选题";
            case 3 -> "填空题";
            default -> "未知类型";
        };
    }

    /**
     * 新增时添加默认短链
     */
    private void createChannelDO(Long activityId, int type) {
        activityChannelService.createChannelDO(activityId, type);
    }

    @Override
    public AppSurveyShareVO getShareVO(Long id) {
        SurveyDO survey = validateSurveyExists(id);
        AppSurveyShareVO vo = new AppSurveyShareVO();
        vo.setId(survey.getId());
        vo.setSurveyName(survey.getSurveyName());
        vo.setShareTitle(survey.getShareTitle());
        vo.setShareNote(survey.getShareNote());
        vo.setShareImgUrl(survey.getShareImgUrl());
        return vo;
    }

    /**
     * 清理时间字段：
     * 1. 开关关闭时强制置 null（前端可能仍传了时间值）
     * 2. 兜底清理 epoch 脏值（全局 TimestampLocalDateTimeDeserializer 会把 null/0 反序列化为 1970-01-01 08:00:00）
     */
    private void cleanTimeFields(SurveyDO surveyDO) {
        // 开始时间：开关关闭 或 值为 epoch 0 → 置 null
        if (!Integer.valueOf(1).equals(surveyDO.getStartTimeEnabled())
                || isEpochZero(surveyDO.getStartTime())) {
            surveyDO.setStartTime(null);
        }
        // 结束时间：同上
        if (!Integer.valueOf(1).equals(surveyDO.getEndTimeEnabled())
                || isEpochZero(surveyDO.getEndTime())) {
            surveyDO.setEndTime(null);
        }
    }

    /** 判断 LocalDateTime 是否为 epoch 0（1970-01-01 08:00:00 in UTC+8） */
    private boolean isEpochZero(LocalDateTime time) {
        return time != null && time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() == 0;
    }

    /** 将字符串时间转为 LocalDateTime，支持 "yyyy-MM-dd HH:mm:ss" 格式和数字时间戳，null/空返回 null */
    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();
        // 尝试按日期格式解析
        try {
            return LocalDateTime.parse(text, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        // 尝试按 "yyyy-MM-dd HH:mm" 格式解析
        try {
            return LocalDateTime.parse(text, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception ignored) {
        }
        // 尝试当时间戳解析
        try {
            long millis = Long.parseLong(text);
            if (millis == 0) return null;
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    /**
     * 从 fill_blank_text 的 JSON 字符串中提取纯文本
     * 支持三种格式：
     * 1. JSON数组 [{"optionId":"101","text":"速度"}] → 提取所有text用"、"连接
     * 2. JSON对象 {"101":"速度"} → 提取所有值用"、"连接
     * 3. 纯文本 → 直接返回
     */
    private String extractFillBlankText(String fillBlankText) {
        if (StrUtil.isBlank(fillBlankText)) {
            return fillBlankText;
        }
        String trimmed = fillBlankText.trim();
        if (trimmed.startsWith("[")) {
            // JSON数组格式
            try {
                List<cn.hutool.json.JSONObject> items = cn.hutool.json.JSONUtil.toList(trimmed, cn.hutool.json.JSONObject.class);
                return items.stream()
                        .map(obj -> obj.getStr("text", ""))
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.joining("、"));
            } catch (Exception e) {
                return fillBlankText;
            }
        } else if (trimmed.startsWith("{")) {
            // JSON对象格式
            try {
                java.util.Map<String, String> map = cn.hutool.json.JSONUtil.toBean(trimmed,
                        new cn.hutool.core.lang.TypeReference<java.util.Map<String, String>>() {}, false);
                return String.join("、", map.values());
            } catch (Exception e) {
                return fillBlankText;
            }
        }
        return fillBlankText;
    }
}
