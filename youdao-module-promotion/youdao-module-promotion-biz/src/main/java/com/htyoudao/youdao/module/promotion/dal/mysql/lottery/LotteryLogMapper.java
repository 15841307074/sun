package com.htyoudao.youdao.module.promotion.dal.mysql.lottery;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LotteryLogMapper extends BaseMapperX<LotteryLogDO> {

    @Select("""
            SELECT
                lottery_prize_id AS lotteryPrizeId,
                COUNT(*) AS usedCount
            FROM lottery_log
            WHERE (lottery_id = #{lotteryId} OR lottery_id = #{activityId})
              AND lottery_prize_id IS NOT NULL
              AND (
                    (prize_type = 5 AND claim_status IN (1, 2))
                    OR
                    ((prize_type <> 5 OR prize_type IS NULL) AND (prize_state IS NULL OR prize_state <> 9))
                  )
            GROUP BY lottery_prize_id
            """)
    List<Map<String, Object>> selectPrizeUsedCountGroupByPrizeId(@Param("lotteryId") Long lotteryId,
                                                                 @Param("activityId") Long activityId);

    @Select("""
            SELECT
                lottery_prize_id AS lotteryPrizeId,
                COUNT(*) AS usedCount
            FROM lottery_log
            WHERE (lottery_id = #{lotteryId} OR lottery_id = #{activityId})
              AND store_id = #{storeId}
              AND lottery_prize_id IS NOT NULL
              AND (
                    (prize_type = 5 AND claim_status IN (1, 2))
                    OR
                    ((prize_type <> 5 OR prize_type IS NULL) AND (prize_state IS NULL OR prize_state <> 9))
                  )
            GROUP BY lottery_prize_id
            """)
    List<Map<String, Object>> selectPrizeUsedCountGroupByPrizeIdAndStoreId(@Param("lotteryId") Long lotteryId,
                                                                            @Param("activityId") Long activityId,
                                                                            @Param("storeId") Long storeId);
}
