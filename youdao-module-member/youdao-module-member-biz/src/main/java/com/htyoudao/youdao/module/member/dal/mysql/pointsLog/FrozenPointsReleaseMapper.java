package com.htyoudao.youdao.module.member.dal.mysql.pointsLog;

import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

/** 单分片、有限批次的解冻 SQL；锁等待也受 JDBC statement timeout 约束。 */
@Mapper
public interface FrozenPointsReleaseMapper {
    @Select("""
            <script>SELECT points_log_id AS pointsLogId,member_id AS memberId,create_time AS createTime FROM points_log
            WHERE business_id=#{business} AND sharding_value=#{shard} AND deleted=0 AND points_type=4
            AND member_id IS NOT NULL AND create_time &lt;= #{before}
            <if test="after != null">AND create_time &gt;= #{after}</if>
            <if test="member != 0">AND member_id=#{member}</if>
            <if test="cursorTime != null">AND (create_time &gt; #{cursorTime}
            OR (create_time=#{cursorTime} AND points_log_id &gt; #{cursorId}))</if>
            ORDER BY create_time,points_log_id LIMIT #{limit}</script>
            """)
    @Options(timeout = 5)
    List<PointsLogDO> pending(@Param("business") long business, @Param("shard") int shard, @Param("member") long member,
            @Param("before") LocalDateTime before, @Param("after") LocalDateTime after,
            @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") Long cursorId, @Param("limit") int limit);

    @Select("""
            <script>SELECT COALESCE(SUM(points_change),0) FROM points_log
            WHERE member_id=#{member} AND business_id=#{business} AND sharding_value=#{shard}
            AND deleted=0 AND points_type=4 AND create_time &lt;= #{before}
            <if test="after != null">AND create_time &gt;= #{after}</if></script>
            """)
    @Options(timeout = 5)
    long pendingAmount(@Param("member") long member, @Param("business") long business, @Param("shard") int shard,
            @Param("before") LocalDateTime before, @Param("after") LocalDateTime after);

    @Select("SELECT member_level FROM wx_member_card WHERE business_id=#{business} AND deleted=0 AND card_status=1 AND max_points_threshold > #{points} AND min_points_threshold < #{points}")
    @Options(timeout = 5)
    Integer memberLevel(@Param("business") long business, @Param("points") long points);
    @Select("SELECT member_id AS memberId,member_integral AS memberIntegral,freeze_points AS freezePoints,integral_frozen AS integralFrozen FROM wx_member WHERE member_id=#{member} AND business_id=#{business} AND sharding_value=#{shard} AND deleted=0 FOR UPDATE")
    @Options(timeout = 5)
    WxMemberDO lockMember(@Param("member") long member, @Param("business") long business, @Param("shard") int shard);

    @Select("""
            <script>SELECT points_log_id AS pointsLogId,points_change AS pointsChange FROM points_log
            WHERE member_id=#{member} AND business_id=#{business} AND sharding_value=#{shard}
            AND deleted=0 AND points_type=4 AND create_time &lt;= #{before}
            <if test="after != null">AND create_time &gt;= #{after}</if>
            AND points_log_id IN <foreach collection="ids" item="id" open="(" close=")" separator=",">#{id}</foreach>
            ORDER BY points_log_id FOR UPDATE</script>
            """)
    @Options(timeout = 5)
    List<PointsLogDO> lockDetails(@Param("member") long member, @Param("business") long business,
            @Param("shard") int shard, @Param("ids") List<Long> ids,
            @Param("before") LocalDateTime before, @Param("after") LocalDateTime after);

    @Update("""
            UPDATE wx_member SET member_integral=#{available},freeze_points=#{frozen},integral_frozen=#{lifetime},
            member_level=COALESCE(#{level},member_level),update_time=NOW()
            WHERE member_id=#{member} AND business_id=#{business} AND sharding_value=#{shard} AND deleted=0
            """)
    @Options(timeout = 5)
    int updateAccount(@Param("member") long member, @Param("business") long business, @Param("shard") int shard,
            @Param("available") int available, @Param("frozen") long frozen, @Param("lifetime") long lifetime,
            @Param("level") Integer level);

    @Update("""
            <script>UPDATE points_log SET points_type=1,update_time=NOW()
            WHERE member_id=#{member} AND business_id=#{business} AND sharding_value=#{shard}
            AND deleted=0 AND points_type=4 AND points_log_id IN
            <foreach collection="ids" item="id" open="(" close=")" separator=",">#{id}</foreach></script>
            """)
    @Options(timeout = 5)
    int updateDetails(@Param("member") long member, @Param("business") long business, @Param("shard") int shard,
            @Param("ids") List<Long> ids);
}
