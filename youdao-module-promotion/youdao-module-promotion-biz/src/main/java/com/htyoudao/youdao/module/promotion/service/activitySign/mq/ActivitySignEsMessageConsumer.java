//package com.htyoudao.youdao.module.promotion.service.activitySign.mq;
//
//import cn.hutool.json.JSONUtil;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.FieldValue;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign.ActivitySignRecordDO;
//import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign.ActivitySignRewardRecordDO;
//import com.htyoudao.youdao.module.promotion.dal.mysql.activitySign.ActivitySignRecordMapper;
//import com.htyoudao.youdao.module.promotion.dal.mysql.activitySign.ActivitySignRewardRecordMapper;
//import com.rabbitmq.client.Channel;
//import jakarta.annotation.Resource;
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.ExchangeTypes;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.*;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
///** 签到活动 MQ 消费者：把 MySQL 最终账本同步到 ES 读模型。 ES 只做查询加速，失败不影响用户签到主链路，查询侧保留 MySQL 降级。 */
//@Slf4j
//@Component
//public class ActivitySignEsMessageConsumer {
//
//  public static final String SIGN_EXCHANGE = "promotion.sign.exchange";
//  public static final String SIGN_RECORD_ROUTING_KEY = "promotion.sign.record";
//  public static final String SIGN_REWARD_ROUTING_KEY = "promotion.sign.reward";
//  public static final String SIGN_REWARD_CLAIM_ROUTING_KEY = "promotion.sign.reward.claim";
//  public static final String SIGN_RECORD_QUEUE = "promotion.sign.record.es.queue";
//  public static final String SIGN_REWARD_QUEUE = "promotion.sign.reward.es.queue";
//  public static final String SIGN_REWARD_CLAIM_QUEUE = "promotion.sign.reward.claim.es.queue";
//  public static final String SIGN_RECORD_INDEX = "promotion_sign_record";
//  public static final String SIGN_REWARD_INDEX = "promotion_sign_reward_record";
//
//  @Resource private ElasticsearchClient elasticsearchClient;
//  @Resource private ActivitySignRecordMapper activitySignRecordMapper;
//  @Resource private ActivitySignRewardRecordMapper activitySignRewardRecordMapper;
//
//  @RabbitListener(
//      bindings =
//          @QueueBinding(
//              value = @Queue(name = SIGN_RECORD_QUEUE, durable = "true"),
//              exchange =
//                  @Exchange(name = SIGN_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
//              key = SIGN_RECORD_ROUTING_KEY))
//  public void onSignRecord(Message amqpMessage, Channel channel) {
//    long deliveryTag = deliveryTag(amqpMessage);
//    try {
//      Map<String, Object> message = parseMessage(amqpMessage);
//      Long recordId = toLong(message.get("recordId"));
//      String mobile = toStringValue(message.get("memberMobile"));
//      // 如果MQ 消息缺少记录ID或手机号，无法定位签到分表记录
//      if (recordId == null || !StringUtils.hasText(mobile)) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      ActivitySignRecordDO record =
//          activitySignRecordMapper.selectById(recordTable(shard(mobile)), recordId);
//      // 如果MySQL 没有查到对应记录，可能事务未提交或数据已删除
//      if (record == null) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      elasticsearchClient.index(
//          i -> i.index(SIGN_RECORD_INDEX).id(String.valueOf(record.getId())).document(toSignDoc(record)));
//      ack(channel, deliveryTag);
//    } catch (Exception e) {
//      log.warn("签到记录同步 ES 失败 message={}", messageText(amqpMessage), e);
//      nack(channel, deliveryTag);
//    }
//  }
//
//  @RabbitListener(
//      bindings =
//          @QueueBinding(
//              value = @Queue(name = SIGN_REWARD_QUEUE, durable = "true"),
//              exchange =
//                  @Exchange(name = SIGN_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
//              key = SIGN_REWARD_ROUTING_KEY))
//  public void onRewardRecord(Message amqpMessage, Channel channel) {
//    long deliveryTag = deliveryTag(amqpMessage);
//    try {
//      Map<String, Object> message = parseMessage(amqpMessage);
//      Long rewardRecordId = toLong(message.get("rewardRecordId"));
//      String mobile = toStringValue(message.get("memberMobile"));
//      // 如果MQ 消息缺少奖励记录ID或手机号，无法定位奖励分表记录
//      if (rewardRecordId == null || !StringUtils.hasText(mobile)) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      ActivitySignRewardRecordDO record =
//          activitySignRewardRecordMapper.selectById(rewardTable(shard(mobile)), rewardRecordId);
//      // 如果MySQL 没有查到对应记录，可能事务未提交或数据已删除
//      if (record == null) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      elasticsearchClient.index(
//          i -> i.index(SIGN_REWARD_INDEX).id(String.valueOf(record.getId())).document(toRewardDoc(record)));
//      ack(channel, deliveryTag);
//    } catch (Exception e) {
//      log.warn("签到奖励记录同步 ES 失败 message={}", messageText(amqpMessage), e);
//      nack(channel, deliveryTag);
//    }
//  }
//
//  @RabbitListener(
//      bindings =
//          @QueueBinding(
//              value = @Queue(name = SIGN_REWARD_CLAIM_QUEUE, durable = "true"),
//              exchange =
//                  @Exchange(name = SIGN_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
//              key = SIGN_REWARD_CLAIM_ROUTING_KEY))
//  public void onRewardClaimStatus(Message amqpMessage, Channel channel) {
//    long deliveryTag = deliveryTag(amqpMessage);
//    try {
//      Map<String, Object> message = parseMessage(amqpMessage);
//      String outBillNo = toStringValue(message.get("outBillNo"));
//      Integer claimStatus = toInteger(message.get("claimStatus"));
//      String failReason = toStringValue(message.get("failReason"));
//      // 如果红包商户单号为空，或者领取状态为空，无法同步领取状态
//      if (!StringUtils.hasText(outBillNo) || claimStatus == null) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      SearchResponse<Map> response =
//          elasticsearchClient.search(
//              s ->
//                  s.index(SIGN_REWARD_INDEX)
//                      .query(
//                          q ->
//                              q.bool(
//                                  b ->
//                                      b.filter(
//                                          f ->
//                                              f.term(
//                                                  t ->
//                                                      t.field("outBillNo")
//                                                          .value(FieldValue.of(outBillNo))))))
//                      .size(100),
//              Map.class);
//      // 如果ES 没有返回命中列表，不需要更新文档
//      if (response.hits() == null || response.hits().hits() == null) {
//        ack(channel, deliveryTag);
//        return;
//      }
//      for (Hit<Map> hit : response.hits().hits()) {
//        // 如果ES 文档ID为空，跳过当前命中记录
//        if (!StringUtils.hasText(hit.id())) continue;
//        elasticsearchClient.update(
//            u ->
//                u.index(SIGN_REWARD_INDEX)
//                    .id(hit.id())
//                    .doc(
//                        Map.of(
//                            "claimStatus",
//                            claimStatus,
//                            "failReason",
//                            failReason == null ? "" : failReason)),
//            Map.class);
//      }
//      ack(channel, deliveryTag);
//    } catch (Exception e) {
//      log.warn("签到红包领取状态同步 ES 失败 message={}", messageText(amqpMessage), e);
//      nack(channel, deliveryTag);
//    }
//  }
//
//  private static final DateTimeFormatter DATE_TIME_FORMATTER =
//      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//  /** 签到记录转 ES 文档：PC 签到记录和数据分析从这些字段查询。 */
//  private Map<String, Object> toSignDoc(ActivitySignRecordDO r) {
//    Map<String, Object> doc = new HashMap<>();
//    doc.put("id", r.getId());
//    doc.put("activityId", r.getActivityId());
//    doc.put("memberMobile", r.getMemberMobile());
//    doc.put("mobileShard", r.getMobileShard());
//    doc.put("triggerMemberId", r.getTriggerMemberId());
//    doc.put("memberName", r.getMemberName());
//    doc.put("storeId", r.getStoreId());
//    doc.put("storeName", r.getStoreName());
//    doc.put("periodKey", r.getPeriodKey());
//    doc.put("periodStartTime", formatDateTime(r.getPeriodStartTime()));
//    doc.put("periodEndTime", formatDateTime(r.getPeriodEndTime()));
//    doc.put("resetEnabledSnapshot", r.getResetEnabledSnapshot());
//    doc.put("resetTypeSnapshot", r.getResetTypeSnapshot());
//    doc.put("resetDaysSnapshot", r.getResetDaysSnapshot());
//    doc.put("signDate", formatDate(r.getSignDate()));
//    doc.put("signTime", formatDateTime(r.getSignTime()));
//    doc.put("signType", r.getSignType());
//    doc.put("continuousDaysAfter", r.getContinuousDaysAfter());
//    doc.put("totalDaysAfter", r.getTotalDaysAfter());
//    doc.put("createTime", formatDateTime(r.getCreateTime()));
//    doc.put("updateTime", formatDateTime(r.getUpdateTime()));
//    doc.put("deleted", r.getDeleted());
//    doc.put("businessId", r.getBusinessId());
//    return doc;
//  }
//
//  /** 奖励发放记录转 ES 文档：PC 发放记录和数据分析从这些字段查询。 */
//  private Map<String, Object> toRewardDoc(ActivitySignRewardRecordDO r) {
//    Map<String, Object> doc = new HashMap<>();
//    doc.put("id", r.getId());
//    doc.put("activityId", r.getActivityId());
//    doc.put("prizeId", r.getPrizeId());
//    doc.put("memberMobile", r.getMemberMobile());
//    doc.put("mobileShard", r.getMobileShard());
//    doc.put("memberId", r.getMemberId());
//    doc.put("memberName", r.getMemberName());
//    doc.put("gender", r.getGender());
//    doc.put("memberCategory", r.getMemberCategory());
//    doc.put("storeId", r.getStoreId());
//    doc.put("storeName", r.getStoreName());
//    doc.put("periodKey", r.getPeriodKey());
//    doc.put("periodStartTime", formatDateTime(r.getPeriodStartTime()));
//    doc.put("periodEndTime", formatDateTime(r.getPeriodEndTime()));
//    doc.put("resetEnabledSnapshot", r.getResetEnabledSnapshot());
//    doc.put("resetTypeSnapshot", r.getResetTypeSnapshot());
//    doc.put("resetDaysSnapshot", r.getResetDaysSnapshot());
//    doc.put("rewardRuleSnapshot", r.getRewardRuleSnapshot());
//    doc.put("prizeType", r.getPrizeType());
//    doc.put("prizeContent", r.getPrizeContent());
//    doc.put("prizeImgUrl", r.getPrizeImgUrl());
//    doc.put("prizeValue", decimalPlain(r.getPrizeValue()));
//    doc.put("signRuleType", r.getSignRuleType());
//    doc.put("triggerDays", r.getTriggerDays());
//    doc.put("signDate", formatDate(r.getSignDate()));
//    doc.put("signRecordId", r.getSignRecordId());
//    doc.put("grantKey", r.getGrantKey());
//    doc.put("issueStatus", r.getIssueStatus());
//    doc.put("issueTime", formatDateTime(r.getIssueTime()));
//    doc.put("failReason", r.getFailReason());
//    doc.put("externalRecordId", r.getExternalRecordId());
//    doc.put("prizeState", r.getPrizeState());
//    doc.put("receiveUser", r.getReceiveUser());
//    doc.put("receiveMobile", r.getReceiveMobile());
//    doc.put("receiveAddress", r.getReceiveAddress());
//    doc.put("trackingNumber", r.getTrackingNumber());
//    doc.put("expressCompany", r.getExpressCompany());
//    doc.put("outBillNo", r.getOutBillNo());
//    doc.put("claimStatus", r.getClaimStatus());
//    doc.put("packageInfo", r.getPackageInfo());
//    doc.put("createTime", formatDateTime(r.getCreateTime()));
//    doc.put("updateTime", formatDateTime(r.getUpdateTime()));
//    doc.put("deleted", r.getDeleted());
//    doc.put("businessId", r.getBusinessId());
//    return doc;
//  }
//
//  private String formatDateTime(LocalDateTime time) {
//    return time == null ? null : DATE_TIME_FORMATTER.format(time);
//  }
//
//  private String formatDate(LocalDate date) {
//    return date == null ? null : date.toString();
//  }
//
//  private String decimalPlain(BigDecimal value) {
//    return value == null ? null : value.toPlainString();
//  }
//
//  /** 获取 MQ 原始消息文本：仅用于异常日志，方便排查投递内容。 */
//  private String messageText(Message amqpMessage) {
//    return new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
//  }
//
//  /** 解析 MQ 消息体：当前 RabbitMQService 发送 JSON 后 listener 收到的是 byte[]，这里统一转成 Map。 */
//  private Map<String, Object> parseMessage(Message amqpMessage) {
//    String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
//    return JSONUtil.toBean(json, Map.class);
//  }
//
//  /** 获取 MQ 投递标记；手动 ACK/NACK 需要用它告诉 RabbitMQ 当前消息处理结果。 */
//  private long deliveryTag(Message amqpMessage) {
//    return amqpMessage.getMessageProperties().getDeliveryTag();
//  }
//
//  /** 消息处理成功或消息本身无效时确认消费，避免 manual ack 模式下消息一直停在 unacked。 */
//  private void ack(Channel channel, long deliveryTag) {
//    try {
//      channel.basicAck(deliveryTag, false);
//    } catch (Exception e) {
//      log.warn("签到活动 MQ ACK 失败 deliveryTag={}", deliveryTag, e);
//    }
//  }
//
//  /** 消息处理异常时拒绝并重新入队，保留 MQ 侧重试机会，避免 ES 短暂异常导致数据丢失。 */
//  private void nack(Channel channel, long deliveryTag) {
//    try {
//      channel.basicNack(deliveryTag, false, true);
//    } catch (Exception e) {
//      log.warn("签到活动 MQ NACK 失败 deliveryTag={}", deliveryTag, e);
//    }
//  }
//
//  /** 根据手机号最后一位数字计算分表尾号。 */
//  private int shard(String mobile) {
//    // 如果手机号为空，无法按手机号尾号分表
//    if (!StringUtils.hasText(mobile)) return 0;
//    for (int i = mobile.length() - 1; i >= 0; i--) {
//      // 如果当前字符是数字，可以作为手机号尾号计算分表
//      if (Character.isDigit(mobile.charAt(i))) return (mobile.charAt(i) - '0') % 10;
//    }
//    return 0;
//  }
//
//  /** 生成签到记录物理分表名。 */
//  private String recordTable(int shard) {
//    return "activity_sign_record_" + shard;
//  }
//
//  /** 生成奖励发放记录物理分表名。 */
//  private String rewardTable(int shard) {
//    return "activity_sign_reward_record_" + shard;
//  }
//
//  private Long toLong(Object value) {
//    // 如果待转换的值为空，返回 null
//    if (value == null) return null;
//    // 如果待转换的值本身是数字类型，直接转换
//    if (value instanceof Number number) return number.longValue();
//    try {
//      return Long.parseLong(String.valueOf(value));
//    } catch (Exception e) {
//      return null;
//    }
//  }
//
//  private Integer toInteger(Object value) {
//    // 如果待转换的值为空，返回 null
//    if (value == null) return null;
//    // 如果待转换的值本身是数字类型，直接转换
//    if (value instanceof Number number) return number.intValue();
//    try {
//      return Integer.parseInt(String.valueOf(value));
//    } catch (Exception e) {
//      return null;
//    }
//  }
//
//  private String toStringValue(Object value) {
//    return Objects.toString(value, null);
//  }
//}
