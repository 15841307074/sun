package com.htyoudao.youdao.module.promotion.dal.mysql.activityVote;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.ActivityVoteRewardLogPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityVoteRewardLogMapper extends BaseMapperX<ActivityVoteRewardLogDO> {

    @Select("""
<script>
SELECT COUNT(1) FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null'> AND member_mobile=#{q.memberMobile}</if>
<if test='q.prizeType != null'> AND prize_type=#{q.prizeType}</if>
<if test='q.prizeState != null'> AND prize_state=#{q.prizeState}</if>
<if test='q.claimStatus != null and q.claimStatus.size() &gt; 0'> AND claim_status IN <foreach collection='q.claimStatus' item='s' open='(' separator=',' close=')'>#{s}</foreach></if>
<if test='q.receiveAddress != null and q.receiveAddress == 1'> AND (receive_address IS NULL OR receive_address='')</if>
<if test='q.receiveAddress != null and q.receiveAddress == 2'> AND receive_address IS NOT NULL AND receive_address!=''</if>
<if test='q.trackingNumber != null and q.trackingNumber == 1'> AND (tracking_number IS NULL OR tracking_number='')</if>
<if test='q.trackingNumber != null and q.trackingNumber == 2'> AND tracking_number IS NOT NULL AND tracking_number!=''</if>
<if test='q.startTime != null'> AND grant_time &gt;= #{q.startTime}</if>
<if test='q.endTime != null'> AND grant_time &lt;= #{q.endTime}</if>
</script>
""")
    /** 导出按分表统计总量；与 selectPage 使用同一筛选条件。 */
    long countPage(@Param("tableName") String tableName, @Param("q") ActivityVoteRewardLogPageReqVO q);

    @Select("""
<script>
SELECT * FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.memberMobile != null'> AND member_mobile=#{q.memberMobile}</if>
<if test='q.prizeType != null'> AND prize_type=#{q.prizeType}</if>
<if test='q.prizeState != null'> AND prize_state=#{q.prizeState}</if>
<if test='q.claimStatus != null and q.claimStatus.size() &gt; 0'> AND claim_status IN <foreach collection='q.claimStatus' item='s' open='(' separator=',' close=')'>#{s}</foreach></if>
<if test='q.receiveAddress != null and q.receiveAddress == 1'> AND (receive_address IS NULL OR receive_address='')</if>
<if test='q.receiveAddress != null and q.receiveAddress == 2'> AND receive_address IS NOT NULL AND receive_address!=''</if>
<if test='q.trackingNumber != null and q.trackingNumber == 1'> AND (tracking_number IS NULL OR tracking_number='')</if>
<if test='q.trackingNumber != null and q.trackingNumber == 2'> AND tracking_number IS NOT NULL AND tracking_number!=''</if>
<if test='q.startTime != null'> AND grant_time &gt;= #{q.startTime}</if>
<if test='q.endTime != null'> AND grant_time &lt;= #{q.endTime}</if>
ORDER BY create_time DESC LIMIT #{offset},#{limit}
</script>
""")
    /** 导出按分表分页查询；offset/limit 为跨表全局偏移与本批剩余条数。 */
    List<ActivityVoteRewardLogDO> selectPage(
            @Param("tableName") String tableName,
            @Param("q") ActivityVoteRewardLogPageReqVO q,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
