package com.htyoudao.youdao.module.promotion.dal.mysql.activitySign;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo.ActivitySignRewardRecordPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign.ActivitySignRewardRecordDO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ActivitySignRewardRecordMapper {
  @Insert(
      """
INSERT INTO ${tableName}(id,activity_id,prize_id,member_mobile,mobile_shard,member_id,member_name,gender,member_category,store_id,store_name,period_key,period_start_time,period_end_time,reset_enabled_snapshot,reset_type_snapshot,reset_days_snapshot,reward_rule_snapshot,prize_type,prize_content,prize_img_url,prize_value,sign_rule_type,trigger_days,sign_date,sign_record_id,grant_key,issue_status,issue_time,fail_reason,external_record_id,prize_state,receive_user,receive_mobile,receive_address,tracking_number,express_company,out_bill_no,claim_status,package_info,create_time,update_time,deleted,business_id)
VALUES(#{r.id},#{r.activityId},#{r.prizeId},#{r.memberMobile},#{r.mobileShard},#{r.memberId},#{r.memberName},#{r.gender},#{r.memberCategory},#{r.storeId},#{r.storeName},#{r.periodKey},#{r.periodStartTime},#{r.periodEndTime},#{r.resetEnabledSnapshot},#{r.resetTypeSnapshot},#{r.resetDaysSnapshot},#{r.rewardRuleSnapshot},#{r.prizeType},#{r.prizeContent},#{r.prizeImgUrl},#{r.prizeValue},#{r.signRuleType},#{r.triggerDays},#{r.signDate},#{r.signRecordId},#{r.grantKey},#{r.issueStatus},#{r.issueTime},#{r.failReason},#{r.externalRecordId},#{r.prizeState},#{r.receiveUser},#{r.receiveMobile},#{r.receiveAddress},#{r.trackingNumber},#{r.expressCompany},#{r.outBillNo},#{r.claimStatus},#{r.packageInfo},NOW(),NOW(),0,#{r.businessId})
""")
  /** 插入奖励发放记录；tableName 为手机号尾号分表名，r 为最终入库记录。 */
  int insertRecord(@Param("tableName") String tableName, @Param("r") ActivitySignRewardRecordDO r);

  @Update(
      """
UPDATE ${tableName}
SET issue_status=#{r.issueStatus},fail_reason=#{r.failReason},external_record_id=#{r.externalRecordId},
    prize_state=#{r.prizeState},out_bill_no=#{r.outBillNo},claim_status=#{r.claimStatus},package_info=#{r.packageInfo},update_time=NOW()
WHERE deleted=0 AND id=#{r.id}
""")
  /** 外部发奖结束后更新奖励发放结果；签到发奖先 insert 占位，发放完成后再回写状态和红包领取凭证。 */
  int updateIssueResult(@Param("tableName") String tableName, @Param("r") ActivitySignRewardRecordDO r);

  @Select(
      """
SELECT * FROM ${tableName} WHERE deleted=0 AND activity_id=#{activityId} AND member_mobile=#{mobile} ORDER BY create_time DESC,id DESC
""")
  /** 查询某活动某手机号奖励记录；用于小程序我的奖励。 */
  List<ActivitySignRewardRecordDO> selectByMobile(
      @Param("tableName") String tableName,
      @Param("activityId") Long activityId,
      @Param("mobile") String mobile);

  @Select(
      """
<script>
SELECT * FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null and q.memberMobile != ""'> AND member_mobile LIKE CONCAT('%',#{q.memberMobile},'%')</if>
<if test='q.prizeType != null'> AND prize_type=#{q.prizeType}</if>
<if test='q.claimStatusList != null and q.claimStatusList.size() &gt; 0'> AND claim_status IN <foreach collection='q.claimStatusList' item='s' open='(' separator=',' close=')'>#{s}</foreach></if>
<if test='q.receiveAddressStatus != null and q.receiveAddressStatus == 1'> AND (receive_address IS NULL OR receive_address='')</if>
<if test='q.receiveAddressStatus != null and q.receiveAddressStatus == 2'> AND receive_address IS NOT NULL AND receive_address!=''</if>
<if test='q.trackingNumberStatus != null and q.trackingNumberStatus == 1'> AND (tracking_number IS NULL OR tracking_number='')</if>
<if test='q.trackingNumberStatus != null and q.trackingNumberStatus == 2'> AND tracking_number IS NOT NULL AND tracking_number!=''</if>
<if test='q.issueTimeStart != null'> AND issue_time &gt;= #{q.issueTimeStart}</if>
<if test='q.issueTimeEnd != null'> AND issue_time &lt;= #{q.issueTimeEnd}</if>
ORDER BY issue_time DESC,id DESC LIMIT #{offset},#{limit}
</script>
""")
  /** PC 奖励发放记录分页查询；筛选项按蓝湖底表。 */
  List<ActivitySignRewardRecordDO> selectPage(
      @Param("tableName") String tableName,
      @Param("q") ActivitySignRewardRecordPageReqVO q,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Select(
      """
<script>
SELECT COUNT(1) FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null and q.memberMobile != ""'> AND member_mobile LIKE CONCAT('%',#{q.memberMobile},'%')</if>
<if test='q.prizeType != null'> AND prize_type=#{q.prizeType}</if>
<if test='q.claimStatusList != null and q.claimStatusList.size() &gt; 0'> AND claim_status IN <foreach collection='q.claimStatusList' item='s' open='(' separator=',' close=')'>#{s}</foreach></if>
<if test='q.receiveAddressStatus != null and q.receiveAddressStatus == 1'> AND (receive_address IS NULL OR receive_address='')</if>
<if test='q.receiveAddressStatus != null and q.receiveAddressStatus == 2'> AND receive_address IS NOT NULL AND receive_address!=''</if>
<if test='q.trackingNumberStatus != null and q.trackingNumberStatus == 1'> AND (tracking_number IS NULL OR tracking_number='')</if>
<if test='q.trackingNumberStatus != null and q.trackingNumberStatus == 2'> AND tracking_number IS NOT NULL AND tracking_number!=''</if>
<if test='q.issueTimeStart != null'> AND issue_time &gt;= #{q.issueTimeStart}</if>
<if test='q.issueTimeEnd != null'> AND issue_time &lt;= #{q.issueTimeEnd}</if>
</script>
""")
  /** PC 奖励发放记录分页/导出统计；与 selectPage 使用同一筛选条件。 */
  long countPage(
      @Param("tableName") String tableName, @Param("q") ActivitySignRewardRecordPageReqVO q);

  @Update(
      """
UPDATE ${tableName} SET receive_user=#{r.receiveUser},receive_mobile=#{r.receiveMobile},receive_address=#{r.receiveAddress},prize_state=2,update_time=NOW()
WHERE deleted=0 AND id=#{r.id} AND prize_type=3 AND prize_state=1
""")
  /** 小程序填写实物奖品收货地址；只有未填写地址状态允许更新，填写后 prize_state 改为待发货。 */
  int updateAddress(@Param("tableName") String tableName, @Param("r") ActivitySignRewardRecordDO r);

  @Select(
      """
SELECT * FROM ${tableName}
WHERE deleted=0
  AND prize_type=3
  AND prize_state=1
  AND issue_time <= #{timeoutTime}
ORDER BY issue_time ASC,id ASC
LIMIT #{limit}
""")
  /** 查询超过48小时仍未填写地址的实物奖励记录；定时任务按分表扫描。 */
  List<ActivitySignRewardRecordDO> selectPhysicalAddressTimeout(
      @Param("tableName") String tableName,
      @Param("timeoutTime") LocalDateTime timeoutTime,
      @Param("limit") int limit);

  @Update(
      """
UPDATE ${tableName}
SET prize_state=9,update_time=NOW()
WHERE deleted=0
  AND id=#{id}
  AND prize_type=3
  AND prize_state=1
  AND issue_time <= #{timeoutTime}
""")
  /** 将超时未填写地址的实物奖励记录更新为 prize_state=9；带旧状态条件保证并发安全。 */
  int updatePhysicalAddressTimeout(
      @Param("tableName") String tableName,
      @Param("id") Long id,
      @Param("timeoutTime") LocalDateTime timeoutTime);

  @Select("SELECT * FROM ${tableName} WHERE deleted=0 AND id=#{id} LIMIT 1")
  /** 按奖励记录ID查询；发货、地址、MQ 同步时使用。 */
  ActivitySignRewardRecordDO selectById(@Param("tableName") String tableName, @Param("id") Long id);


  @Select("SELECT * FROM ${tableName} WHERE deleted=0 AND out_bill_no=#{outBillNo} LIMIT 1")
  /** 按奖励流水号查询；发货、地址、MQ 同步时使用。 */
  ActivitySignRewardRecordDO selectByObn(@Param("tableName") String tableName, @Param("outBillNo") String outBillNo);
  @Update(
      """
UPDATE ${tableName}
SET prize_state=#{r.prizeState},receive_user=#{r.receiveUser},receive_mobile=#{r.receiveMobile},receive_address=#{r.receiveAddress},tracking_number=#{r.trackingNumber},express_company=#{r.expressCompany},update_time=NOW()
WHERE deleted=0 AND id=#{r.id} AND prize_type=3
""")
  /** PC 更新实物奖品发货信息；更新收货人、地址、物流单号和快递公司。 */
  int updateExpress(@Param("tableName") String tableName, @Param("r") ActivitySignRewardRecordDO r);

  @Update(
      "UPDATE ${tableName} SET"
          + " claim_status=#{claimStatus},fail_reason=#{failReason},update_time=NOW() WHERE"
          + " deleted=0 AND out_bill_no=#{outBillNo} AND prize_type=5")
  /** 红包回调按 outBillNo 更新领取状态；claimStatus=1未领取/2已领取/3已过期。 */
  int updateClaimStatusByOutBillNo(
      @Param("tableName") String tableName,
      @Param("outBillNo") String outBillNo,
      @Param("claimStatus") Integer claimStatus,
      @Param("failReason") String failReason);
}
