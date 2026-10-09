package com.htyoudao.youdao.module.bpm.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 巡店记录明细规则校验工具类
 *
 * <p>用于校验 {@code system_store_inspection_record_item} 明细项在巡检过程中的填写规则，
 * 并提供与巡检结果相关的通用计算能力。</p>
 *
 * <p>主要职责包括：</p>
 * <ul>
 *     <li>根据图片规则 {@code imgRule} 校验图片是否允许上传、是否必传、上传数量是否满足要求；</li>
 *     <li>根据描述规则 {@code descriptionRule} 校验巡店人备注 {@code actualComment} 是否允许填写、是否必填；</li>
 *     <li>根据不适用规则 {@code inapplicabilityRule} 校验当前点检项是否允许选择“不适用”；</li>
 *     <li>根据奖惩规则 {@code rewardPunishmentRule} 校验奖惩金额 {@code rewardAmount} 是否允许填写、是否必填；</li>
 *     <li>根据检查结果 {@code actualStatus} 计算实际得分 {@code actualScore}；</li>
 *     <li>提供图片数量统计、文本规范化、得分率计算等通用方法。</li>
 * </ul>
 *
 * <p>业务规则说明：</p>
 * <ul>
 *     <li>检查结果：0-不合格，1-合格，2-不适用；</li>
 *     <li>合格、不适用都按满分处理；</li>
 *     <li>不合格时，实际得分必须小于该项满分，且不能小于 0；</li>
 *     <li>本工具类仅负责单条明细的规则判断，不直接处理主表汇总更新。</li>
 * </ul>
 *
 * @author dht
 */
public class InspectionRecordItemRuleValidationUtil {

    /**
     * 校验当前点检项在指定检查结果下是否允许选择“不适用”。
     *
     * @param itemDO       巡店记录明细快照
     * @param actualStatus 检查结果：0-不合格，1-合格，2-不适用
     */
    public static void validateInapplicabilityRule(StoreInspectionRecordItemDO itemDO, Integer actualStatus) {
        if (!Objects.equals(actualStatus, 2)) {
            return;
        }
        int showFlag = getRuleInt(itemDO.getInapplicabilityRule(), "showFlag", 0, "不适用规则");
        if (showFlag != 1) {
            throw invalidParamException("当前点检项不能选择不适用");
        }
    }

    /**
     * 校验巡店备注在指定检查结果下是否允许填写，以及是否必填。
     *
     * @param itemDO         巡店记录明细快照
     * @param actualStatus   检查结果：0-不合格，1-合格，2-不适用
     * @param actualComment  巡店人备注
     */
    public static void validateDescriptionRule(StoreInspectionRecordItemDO itemDO, Integer actualStatus, String actualComment) {
        String comment = normalizeText(actualComment);
        int descriptionFlag = getRuleInt(itemDO.getDescriptionRule(), "descriptionFlag", 0, "描述规则");

        if (descriptionFlag != 1) {
            if (StrUtil.isNotBlank(comment)) {
                throw invalidParamException("当前点检项不允许填写问题描述");
            }
            return;
        }

        int required = switch (actualStatus) {
            case 1 -> getRuleInt(itemDO.getDescriptionRule(), "qualified", 0, "描述规则");
            case 0 -> getRuleInt(itemDO.getDescriptionRule(), "unqualified", 0, "描述规则");
            case 2 -> getRuleInt(itemDO.getDescriptionRule(), "inapplicability", 0, "描述规则");
            default -> 0;
        };

        if (required == 1 && StrUtil.isBlank(comment)) {
            throw invalidParamException("当前状态必须填写问题描述");
        }
    }

    /**
     * 校验图片在指定检查结果下是否允许上传、是否必传以及上传数量是否满足要求。
     *
     * @param itemDO        巡店记录明细快照
     * @param actualStatus  检查结果：0-不合格，1-合格，2-不适用
     * @param actualImages  图片地址，支持逗号分隔或 JSON 数组格式
     */
    public static void validateImageRule(StoreInspectionRecordItemDO itemDO, Integer actualStatus, String actualImages) {
        int imageCount = countImages(actualImages);
        int imgFlag = getRuleInt(itemDO.getImgRule(), "imgFlag", 0, "图片规则");

        if (imgFlag != 1) {
            if (imageCount > 0) {
                throw invalidParamException("当前点检项不允许上传图片");
            }
            return;
        }

        Set<Integer> imgStates = getRuleIntSet(itemDO.getImgRule(), "imgState", "图片规则");
        int imgCount = Math.max(getRuleInt(itemDO.getImgRule(), "imgCount", 1, "图片规则"), 1);

        boolean required = isImageRequired(imgStates, actualStatus);
        if (required && imageCount < imgCount) {
            throw exception(STORE_INSPECTION_RECORD_PIC_LITTLE_ERROR, imgCount);
        }
        if (required && imageCount > 12) {
            throw exception(STORE_INSPECTION_RECORD_PIC_MOST_ERROR);
        }
    }

    private static boolean isImageRequired(Set<Integer> imgStates, Integer actualStatus) {
        if (CollectionUtil.isEmpty(imgStates)) {
            return false;
        }

        // 0 = 不必传
        // 如果配置里包含 0，建议按“不必传”处理，优先级最高
        if (imgStates.contains(0)) {
            return false;
        }

        if (Objects.equals(actualStatus, 1)) {
            return imgStates.contains(1);
        }
        if (Objects.equals(actualStatus, 0)) {
            return imgStates.contains(2);
        }
        if (Objects.equals(actualStatus, 2)) {
            return imgStates.contains(3);
        }
        return false;
    }



    /**
     * 校验奖惩金额在指定检查结果下是否允许填写，以及是否必填。
     *
     * @param itemDO         巡店记录明细快照
     * @param actualStatus   检查结果：0-不合格，1-合格，2-不适用
     * @param rewardAmount   奖惩金额
     */
    public static void validateRewardRule(StoreInspectionRecordItemDO itemDO, Integer actualStatus, BigDecimal rewardAmount) {
        // 不适用没有奖惩分支，直接按不允许处理
        if (Objects.equals(actualStatus, 2)) {
            if (rewardAmount != null && rewardAmount.compareTo(BigDecimal.ZERO) != 0) {
                throw invalidParamException("不适用时不允许填写奖惩金额");
            }
            return;
        }

        int rewardFlag = getRuleInt(itemDO.getRewardPunishmentRule(), "rewardPunishmentFlag", 0, "奖惩规则");
        if (rewardFlag != 1) {
            if (rewardAmount != null && rewardAmount.compareTo(BigDecimal.ZERO) != 0) {
                throw invalidParamException("当前点检项不允许填写奖惩金额");
            }
            return;
        }

        int required = Objects.equals(actualStatus, 1)
                ? getRuleInt(itemDO.getRewardPunishmentRule(), "qualified", 0, "奖惩规则")
                : getRuleInt(itemDO.getRewardPunishmentRule(), "unqualified", 0, "奖惩规则");

        if (required == 1 && rewardAmount == null) {
            throw invalidParamException("当前状态必须填写奖惩金额");
        }
    }

    /**
     * 根据检查结果计算实际得分。
     *
     * <p>规则说明：</p>
     * <ul>
     *     <li>合格、不适用：按满分处理；</li>
     *     <li>不合格：实际得分必须小于满分，且不能小于 0。</li>
     * </ul>
     *
     * @param maxScoreSnap   该项满分快照
     * @param actualStatus   检查结果：0-不合格，1-合格，2-不适用
     * @param reqActualScore 前端传入的实际得分
     * @return 最终实际得分
     */
    public static Integer resolveActualScore(Integer maxScoreSnap, Integer actualStatus, Integer reqActualScore) {
        int maxScore = maxScoreSnap == null ? 0 : maxScoreSnap;

        // 合格、不适用都按满分
        if (Objects.equals(actualStatus, 1) || Objects.equals(actualStatus, 2)) {
            return maxScore;
        }

        // 不合格：必须小于满分
        if (reqActualScore == null) {
            throw invalidParamException("不合格时实际得分不能为空");
        }
        if (reqActualScore < 0) {
            throw invalidParamException("实际得分不能小于 0");
        }
        if (reqActualScore >= maxScore) {
            throw invalidParamException("不合格时实际得分必须小于满分");
        }
        return reqActualScore;
    }

    /**
     * 对奖惩金额进行规范化处理。
     *
     * <p>当当前状态不允许填写奖惩金额，或前端未传值时，统一返回 0。</p>
     *
     * @param actualStatus            检查结果
     * @param rewardPunishmentRule   奖惩规则
     * @param rewardAmount            原始奖惩金额
     * @return 规范化后的奖惩金额
     */
    public static BigDecimal normalizeRewardAmount(Integer actualStatus, String rewardPunishmentRule, BigDecimal rewardAmount) {
        if (Objects.equals(actualStatus, 2)) {
            return BigDecimal.ZERO;
        }
        int rewardFlag = getRuleInt(rewardPunishmentRule, "rewardPunishmentFlag", 0, "奖惩规则");
        if (rewardFlag != 1 || rewardAmount == null) {
            return BigDecimal.ZERO;
        }
        return rewardAmount;
    }

    /**
     * 规范化文本内容：空字符串转为 {@code null}，非空字符串去除首尾空格。
     *
     * @param text 原始文本
     * @return 规范化后的文本
     */
    public static String normalizeText(String text) {
        return StrUtil.isBlank(text) ? null : text.trim();
    }

    /**
     * 统计图片数量。
     *
     * <p>支持两种格式：</p>
     * <ul>
     *     <li>JSON 数组，例如：["a.jpg","b.jpg"]</li>
     *     <li>逗号分隔字符串，例如：a.jpg,b.jpg</li>
     * </ul>
     *
     * @param actualImages 图片地址字符串
     * @return 图片数量
     */
    public static int countImages(String actualImages) {
        String images = normalizeText(actualImages);
        if (StrUtil.isBlank(images)) {
            return 0;
        }

        try {
            if (JsonUtils.isJson(images)) {
                JsonNode jsonNode = JsonUtils.parseTree(images);

                // 1. 直接就是数组
                if (jsonNode.isArray()) {
                    return jsonNode.size();
                }

                // 2. 是对象，尝试取 actualImages
                if (jsonNode.isObject()) {
                    JsonNode actualImagesNode = jsonNode.get("actualImages");
                    if (actualImagesNode == null || actualImagesNode.isNull()) {
                        return 0;
                    }
                    if (actualImagesNode.isArray()) {
                        return actualImagesNode.size();
                    }
                    if (actualImagesNode.isTextual()) {
                        return countImages(actualImagesNode.asText());
                    }
                }
            }
        } catch (Exception ignored) {
            // 解析失败，继续走逗号分隔
        }

        return (int) Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .count();
    }


    /**
     * 从规则 JSON 中读取指定字段的整型值。
     *
     * <p>当字段不存在、为空或规则字符串为空时，返回默认值；
     * 当规则 JSON 格式异常时，抛出配置错误异常。</p>
     *
     * @param ruleJson      规则 JSON 字符串
     * @param fieldName     字段名
     * @param defaultValue  默认值
     * @param ruleName      规则名称，用于异常提示
     * @return 对应字段的整型值
     */
    public static int getRuleInt(String ruleJson, String fieldName, int defaultValue, String ruleName) {
        if (StrUtil.isBlank(ruleJson)) {
            return defaultValue;
        }
        try {
            JsonNode fieldNode = JsonUtils.parseTree(ruleJson).path(fieldName);
            if (fieldNode == null || fieldNode.isMissingNode() || fieldNode.isNull()) {
                return defaultValue;
            }
            if (fieldNode.isNumber()) {
                return fieldNode.asInt(defaultValue);
            }
            String text = fieldNode.asText();
            return StrUtil.isBlank(text) ? defaultValue : Integer.parseInt(text.trim());
        } catch (Exception e) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_CONFIGURATION_ERROR, ruleName);
        }
    }

    /**
     * 计算得分率，结果保留两位小数。
     *
     * @param actualScore 实际得分
     * @param totalScore  总分
     * @return 得分率；当总分小于等于 0 时返回 0
     */
    public static BigDecimal calculateScoreRate(int actualScore, int totalScore) {
        if (totalScore <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(actualScore)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalScore), 2, RoundingMode.HALF_UP);
    }

    public static boolean isFinishedStatus(Integer status) {
        return Objects.equals(status, 0)
                || Objects.equals(status, 1)
                || Objects.equals(status, 2);
    }

    public static Map<String, Object> buildItemLogSnap(String titleSnap,
                                                 Integer maxScoreSnap,
                                                 Integer actualStatus,
                                                 Integer actualScore,
                                                 BigDecimal rewardAmount,
                                                 String actualComment,
                                                 String actualImages) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("titleSnap", titleSnap);
        snap.put("maxScoreSnap", maxScoreSnap);
        snap.put("actualStatus", actualStatus);
        snap.put("actualScore", actualScore);
        snap.put("rewardAmount", rewardAmount);
        snap.put("actualComment", actualComment);
        snap.put("actualImages", actualImages);
        return snap;
    }
    public static boolean hasLogSnapChanged(Map<String, Object> beforeSnap, Map<String, Object> afterSnap) {
        // 1. 基础校验：如果有一个为空，视为发生了变化（根据业务决定）
        if (beforeSnap == null || afterSnap == null) {
            return beforeSnap != afterSnap;
        }

        // 2. 普通字段对比（String, Integer 等）
        if (!Objects.equals(beforeSnap.get("titleSnap"), afterSnap.get("titleSnap"))) return true;
        if (!Objects.equals(beforeSnap.get("maxScoreSnap"), afterSnap.get("maxScoreSnap"))) return true;
        if (!Objects.equals(beforeSnap.get("actualStatus"), afterSnap.get("actualStatus"))) return true;
        if (!Objects.equals(beforeSnap.get("actualScore"), afterSnap.get("actualScore"))) return true;

        // 3. 金额对比（安全处理类型转换）
        if (!rewardAmountEquals(beforeSnap.get("rewardAmount"), afterSnap.get("rewardAmount"))) {
            return true;
        }

        // 4. 文本标准化对比
        if (!Objects.equals(normalizeText(beforeSnap.get("actualComment")),
                normalizeText(afterSnap.get("actualComment")))) {
            return true;
        }

        return !Objects.equals(normalizeText(beforeSnap.get("actualImages")),
                normalizeText(afterSnap.get("actualImages")));
    }

    /**
     * 安全的金额对比方法
     * 解决 Map 中 Object 转 BigDecimal 的潜在问题
     */
    private static boolean rewardAmountEquals(Object leftObj, Object rightObj) {
        if (leftObj == null && rightObj == null) return true;

        try {
            BigDecimal l = toBigDecimal(leftObj);
            BigDecimal r = toBigDecimal(rightObj);
            // compareTo == 0 忽略了精度差异（0.00 等于 0）
            return l.compareTo(r) == 0;
        } catch (Exception e) {
            // 如果转换失败，为保险起见认为已改变，或者直接返回 Objects.equals
            return Objects.equals(leftObj, rightObj);
        }
    }

    private static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return new BigDecimal(obj.toString());
        if (obj instanceof String) return new BigDecimal((String) obj);
        return BigDecimal.ZERO;
    }

    private static String normalizeText(Object obj) {
        if (obj == null) return "";
        return String.valueOf(obj).trim();
    }

    public static Set<Integer> getRuleIntSet(String ruleJson, String fieldName, String ruleName) {
        LinkedHashSet<Integer> result = new LinkedHashSet<>();

        if (StrUtil.isBlank(ruleJson)) {
            result.add(0);
            return result;
        }

        try {
            JsonNode fieldNode = JsonUtils.parseTree(ruleJson).path(fieldName);
            if (fieldNode == null || fieldNode.isMissingNode() || fieldNode.isNull()) {
                result.add(0);
                return result;
            }

            if (fieldNode.isArray()) {
                for (JsonNode node : fieldNode) {
                    addRuleInt(result, node.asText(), ruleName);
                }
            } else if (fieldNode.isNumber()) {
                result.add(fieldNode.asInt());
            } else {
                String text = fieldNode.asText();
                if (StrUtil.isBlank(text)) {
                    result.add(0);
                    return result;
                }
                for (String part : text.split(",")) {
                    addRuleInt(result, part, ruleName);
                }
            }

            if (result.isEmpty()) {
                result.add(0);
            }

            return result;
        } catch (Exception e) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_CONFIGURATION_ERROR, ruleName);
        }
    }

    private static void addRuleInt(Set<Integer> result, String text, String ruleName) {
        if (StrUtil.isBlank(text)) {
            return;
        }
        int value = Integer.parseInt(text.trim());
        if (value < 0 || value > 3) {
            throw exception(STORE_INSPECTION_RECORD_ITEM_CONFIGURATION_ERROR, ruleName);
        }
        result.add(value);
    }


    /**
     * 计算距今多少天
     */
    public static Integer calculateDaysSinceLastInspection(LocalDateTime lastTime) {
        if (lastTime == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(lastTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        return (int) Math.max(days, 0L);
    }

    /**
     * 计算整数列表平均值，保留两位小数
     */
    public static BigDecimal averageInteger(List<Integer> values) {
        List<Integer> validValues = values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(validValues)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        int sum = validValues.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(validValues.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算 BigDecimal 列表平均值，保留两位小数
     */
    public static BigDecimal averageBigDecimal(List<BigDecimal> values) {
        List<BigDecimal> validValues = values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(validValues)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal sum = validValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(validValues.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 校验BigDecimal的整数部分和小数部分位数
     * @param value 待校验数值
     * @param maxIntegerDigits 允许的最大整数位数（>0）
     * @param maxFractionDigits 允许的最大小数位数（>=0）
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public static void validateDigits(BigDecimal value, int maxIntegerDigits, int maxFractionDigits) {
//        if (value == null) {
//            throw new IllegalArgumentException("数值不能为空");
//        }
        // 转换为不带科学计数法的字符串，避免精度丢失
        String plainString = value.toPlainString();
        String[] parts = plainString.split("\\.");
        String integerPart = parts[0];
        // 处理负数，去除负号
        if (integerPart.startsWith("-")) {
            integerPart = integerPart.substring(1);
        }
        int integerDigits = integerPart.length();
        int fractionDigits = parts.length > 1 ? parts[1].length() : 0;

        if (integerDigits > maxIntegerDigits) {
            throw exception(STORE_INSPECTION_RECORD_AWARD_ERROR);
        }
        if (fractionDigits > maxFractionDigits) {
            throw exception(STORE_INSPECTION_RECORD_AWARD_ERROR_2);
        }
    }


}
