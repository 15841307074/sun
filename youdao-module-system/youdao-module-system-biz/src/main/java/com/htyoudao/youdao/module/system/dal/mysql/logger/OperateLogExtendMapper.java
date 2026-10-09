package com.htyoudao.youdao.module.system.dal.mysql.logger;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogExtendDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 操作日志扩展 Mapper
 *
 * @author 0090
 */
@Mapper
public interface OperateLogExtendMapper extends BaseMapperX<OperateLogExtendDO> {

    /**
     * 物理删除指定时间之前的日志
     *
     * @param createTime 最大时间
     * @param limit 删除条数，防止一次删除太多
     * @return 删除条数
     */
    @Delete("DELETE FROM system_operate_log_extend WHERE create_time < #{createTime} LIMIT #{limit}")
    void deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit")Integer limit);

}
