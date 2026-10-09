package com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 有奖问答任务记录 Mapper。
 */
@Mapper
public interface ActivityAnswerTaskMapper extends BaseMapperX<ActivityAnswerTaskDO> {

    /**
     * 在任务上限内原子增加完成次数和获得次数。
     */
    @Update("""
            UPDATE activity_answer_task
            SET finish_count = finish_count + 1,
                gain_count = gain_count + 1
            WHERE id = #{id}
              AND member_mobile = #{memberMobile}
              AND finish_count < #{limitCount}
            """)
    int increaseTaskCount(@Param("id") Long id,
                          @Param("memberMobile") Long memberMobile,
                          @Param("limitCount") Integer limitCount);
}
