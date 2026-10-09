package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardStoreStockDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ActivityAnswerRewardStoreStockMapper extends BaseMapperX<ActivityAnswerRewardStoreStockDO> {

    /**
     * 原子扣减门店独立奖励库存，total_num=0 表示不限库存。
     */
    @Update("""
            UPDATE activity_answer_reward_store_stock
            SET used_num = used_num + 1
            WHERE activity_id = #{activityId}
              AND reward_id = #{rewardId}
              AND store_id = #{storeId}
              AND deleted = 0
              AND (total_num = 0 OR used_num < total_num)
            """)
    int increaseUsedNum(@Param("activityId") Long activityId,
                        @Param("rewardId") Long rewardId,
                        @Param("storeId") Long storeId);
}
