package com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券归档 Mapper。
 *
 * <p>所有 SQL 都固定访问 MASTER；动态表名参数只能由已校验的 0~9 分片编号传入。</p>
 */
@Mapper
@DS(DsNameConstants.MASTER)
public interface UserCouponArchiveMapper {

    /**
     * 从在线分表锁定一个待归档批次，并返回来源用户券主键。
     *
     * <p>筛选条件：业务线、couponId、发放时间早于截止时间；按主键升序取固定批量并加行锁。</p>
     */
    List<Long> selectEligibleSourceIds(@Param("shard") Integer shard,
                                       @Param("businessId") Long businessId,
                                       @Param("couponId") Long couponId,
                                       @Param("cutoff") LocalDateTime cutoff,
                                       @Param("batchSize") Integer batchSize);

    /**
     * 将来源记录的全部原字段写入对应归档分表。
     *
     * <p>同时把来源表主键 id 再写入 source_user_coupon_id，便于追溯并通过唯一索引防重。</p>
     */
    int insertBackupBySourceIds(@Param("shard") Integer shard,
                                @Param("sourceIds") List<Long> sourceIds);

    /**
     * 删除在线分表中本批已完整备份的来源记录。
     *
     * <p>除主键集合外再次限定业务线、couponId 和截止时间，避免并发状态变化时扩大删除范围。</p>
     */
    int deleteSourceByIds(@Param("shard") Integer shard,
                          @Param("businessId") Long businessId,
                          @Param("couponId") Long couponId,
                          @Param("cutoff") LocalDateTime cutoff,
                          @Param("sourceIds") List<Long> sourceIds);

    /**
     * 按实际过期时间锁定一批历史用户券。
     *
     * <p>该方法用于人工历史数据归档，不限制业务线和券模板；物理表编号必须在 Service 层完成白名单校验。</p>
     */
    List<Long> selectExpiredSourceIds(@Param("shard") Integer shard,
                                      @Param("expirationTime") LocalDateTime expirationTime,
                                      @Param("batchSize") Integer batchSize);

    /**
     * 删除本批已经完整写入归档表的过期用户券。
     */
    int deleteExpiredSourceByIds(@Param("shard") Integer shard,
                                 @Param("expirationTime") LocalDateTime expirationTime,
                                 @Param("sourceIds") List<Long> sourceIds);
}
