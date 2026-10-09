package com.htyoudao.youdao.module.promotion.dal.mysql.activityVote;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.ActivityVoteLogPageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityVoteLogMapper extends BaseMapperX<ActivityVoteLogDO> {

    @Select("""
<script>
SELECT COUNT(1) FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.optionId != null'> AND option_id=#{q.optionId}</if>
<if test='q.memberMobile != null'> AND member_mobile=#{q.memberMobile}</if>
<if test='q.storeId != null'> AND store_id=#{q.storeId}</if>
<if test='q.startTime != null'> AND vote_time &gt;= #{q.startTime}</if>
<if test='q.endTime != null'> AND vote_time &lt;= #{q.endTime}</if>
</script>
""")
    /** 导出按分表统计总量；与 selectPage 使用同一筛选条件。 */
    long countPage(@Param("tableName") String tableName, @Param("q") ActivityVoteLogPageReqVO q);

    @Select("""
<script>
SELECT * FROM ${tableName} WHERE deleted=0 AND activity_id=#{q.activityId}
<if test='q.optionId != null'> AND option_id=#{q.optionId}</if>
<if test='q.memberMobile != null'> AND member_mobile=#{q.memberMobile}</if>
<if test='q.storeId != null'> AND store_id=#{q.storeId}</if>
<if test='q.startTime != null'> AND vote_time &gt;= #{q.startTime}</if>
<if test='q.endTime != null'> AND vote_time &lt;= #{q.endTime}</if>
ORDER BY vote_time DESC LIMIT #{offset},#{limit}
</script>
""")
    /** 导出按分表分页查询；offset/limit 为跨表全局偏移与本批剩余条数。 */
    List<ActivityVoteLogDO> selectPage(
            @Param("tableName") String tableName,
            @Param("q") ActivityVoteLogPageReqVO q,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT member_id AS memberId, COUNT(1) AS cnt FROM ${tableName} WHERE deleted=0 AND activity_id=#{activityId} GROUP BY member_id")
    /** 按分表统计各会员投票次数；导出 totalVoteCount 列需跨分表累加。 */
    List<Map<String, Object>> countGroupByMember(@Param("tableName") String tableName, @Param("activityId") Long activityId);
}
