package com.htyoudao.youdao.module.promotion.dal.mysql.activitySign;

import com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo.ActivitySignRecordPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign.ActivitySignRecordDO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ActivitySignRecordMapper {

  @Insert(
      """
INSERT INTO ${tableName}(id,activity_id,member_mobile,mobile_shard,trigger_member_id,member_name,store_id,store_name,period_key,period_start_time,period_end_time,reset_enabled_snapshot,reset_type_snapshot,reset_days_snapshot,sign_date,sign_time,sign_type,continuous_days_after,total_days_after,create_time,deleted,business_id)
VALUES(#{r.id},#{r.activityId},#{r.memberMobile},#{r.mobileShard},#{r.triggerMemberId},#{r.memberName},#{r.storeId},#{r.storeName},#{r.periodKey},#{r.periodStartTime},#{r.periodEndTime},#{r.resetEnabledSnapshot},#{r.resetTypeSnapshot},#{r.resetDaysSnapshot},#{r.signDate},#{r.signTime},#{r.signType},#{r.continuousDaysAfter},#{r.totalDaysAfter},NOW(),0,#{r.businessId})
""")
  /** 插入签到记录；tableName 为手机号尾号分表名，r 为最终入库记录。 */
  int insertRecord(@Param("tableName") String tableName, @Param("r") ActivitySignRecordDO r);

  @Select(
      """
SELECT COUNT(1) FROM ${tableName}
WHERE deleted=0 AND activity_id=#{activityId} AND member_mobile=#{mobile} AND sign_date=#{signDate}
""")
  /** 判断手机号当天是否已签到；用于签到防重，activityId + mobile + signDate 唯一判断。 */
  long countByMobileDate(
      @Param("tableName") String tableName,
      @Param("activityId") Long activityId,
      @Param("mobile") String mobile,
      @Param("signDate") LocalDate signDate);

  @Select("SELECT * FROM ${tableName} WHERE deleted=0 AND id=#{id} LIMIT 1")
  /** 按记录ID查询签到记录；MQ 同步 ES 时根据消息回查 MySQL 使用。 */
  ActivitySignRecordDO selectById(@Param("tableName") String tableName, @Param("id") Long id);

  @Select(
      """
      SELECT * FROM ${tableName}
      WHERE deleted=0 AND activity_id=#{activityId} AND member_mobile=#{mobile}
      ORDER BY sign_date ASC
      """)
  /** 查询某活动某手机号全部签到记录；用于小程序签到日期列表和 MySQL 降级。 */
  List<ActivitySignRecordDO> selectByMobile(
      @Param("tableName") String tableName,
      @Param("activityId") Long activityId,
      @Param("mobile") String mobile);

  @Select(
      """
      SELECT * FROM ${tableName}
      WHERE deleted=0 AND activity_id=#{activityId} AND member_mobile=#{mobile}
        AND sign_date BETWEEN #{startDate} AND #{endDate}
      ORDER BY sign_date ASC
      """)
  /** 查询某周期内签到记录；用于计算当前周期连续/累计签到天数。 */
  List<ActivitySignRecordDO> selectByMobileDateRange(
      @Param("tableName") String tableName,
      @Param("activityId") Long activityId,
      @Param("mobile") String mobile,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Select(
      """
<script>
SELECT * FROM ${tableName}
WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null and q.memberMobile != ""'> AND member_mobile LIKE CONCAT('%',#{q.memberMobile},'%')</if>
<if test='q.storeId != null'> AND store_id=#{q.storeId}</if>
<if test='q.signTimeStart != null'> AND sign_time &gt;= #{q.signTimeStart}</if>
<if test='q.signTimeEnd != null'> AND sign_time &lt;= #{q.signTimeEnd}</if>
ORDER BY sign_time DESC,id DESC
LIMIT #{offset},#{limit}
</script>
""")
  /** PC 签到记录分页查询；条件与蓝湖筛选项保持一致。 */
  List<ActivitySignRecordDO> selectPage(
      @Param("tableName") String tableName,
      @Param("q") ActivitySignRecordPageReqVO q,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Select(
      """
<script>
SELECT COUNT(1) FROM ${tableName}
WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null and q.memberMobile != ""'> AND member_mobile LIKE CONCAT('%',#{q.memberMobile},'%')</if>
<if test='q.storeId != null'> AND store_id=#{q.storeId}</if>
<if test='q.signTimeStart != null'> AND sign_time &gt;= #{q.signTimeStart}</if>
<if test='q.signTimeEnd != null'> AND sign_time &lt;= #{q.signTimeEnd}</if>
</script>
""")
  /** PC 签到记录分页/导出统计；与 selectPage 使用同一筛选条件。 */
  long countPage(@Param("tableName") String tableName, @Param("q") ActivitySignRecordPageReqVO q);

  @Select(
      """
SELECT COUNT(DISTINCT member_mobile) FROM ${tableName} WHERE deleted=0 AND activity_id=#{activityId}
""")
  /** 统计参与手机号数量；用于数据分析 MySQL 降级。 */
  long countDistinctMobile(
      @Param("tableName") String tableName, @Param("activityId") Long activityId);

  @Select(
      """
      SELECT DISTINCT activity_id FROM ${tableName}
      WHERE deleted=0 AND member_mobile=#{mobile}
      ORDER BY activity_id DESC
      """)
  /** 查询手机号参与过的活动ID；用于小程序首页只展示参与过的签到进度。 */
  List<Long> selectActivityIdsByMobile(
      @Param("tableName") String tableName, @Param("mobile") String mobile);

  @Select(
      """
      SELECT COUNT(1) FROM ${tableName} WHERE deleted=0 AND activity_id=#{activityId}
      """)
  /** 统计活动签到次数；用于数据分析 MySQL 降级。 */
  long countByActivity(@Param("tableName") String tableName, @Param("activityId") Long activityId);
}
