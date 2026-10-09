package com.htyoudao.youdao.module.system.dal.mysql.businessuser;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;


/**
 * 项目和用户关联 Mapper
 *
 * @author 零零玖零
 */
@Mapper
public interface BusinessUserMapper extends BaseMapperX<BusinessUserDO> {
    @DataPermission(enable = false)
    default List<BusinessUserDO> selectList(Long userId) {
        return selectList(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getUserId, userId)
                .eq(BusinessUserDO::getDeleted, 0)) ;
    }

    @DataPermission(enable = false)
    default List<Long> selectBusinessList(Long userId) {
        List<BusinessUserDO> businessUserDOS = selectList(
            new LambdaQueryWrapperX<BusinessUserDO>()
                .eq(BusinessUserDO::getUserId, userId)
                .eq(BusinessUserDO::getDeleted, 0));
        return businessUserDOS.stream().map(BusinessUserDO::getBusinessId).toList();
    }




    @DataPermission(enable = false) // 关闭数据权限
    default List<Long> enableBusinessIdByUserId(Long userId) {
        LambdaQueryWrapper<BusinessUserDO> queryWrapper = new LambdaQueryWrapper<BusinessUserDO>()
                .eq(BusinessUserDO::getUserId, userId)
                .eq(BusinessUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .select(BusinessUserDO::getBusinessId);
        return selectList(queryWrapper).stream().map(BusinessUserDO::getBusinessId).toList();

    }
    @DataPermission(enable = false) // 关闭数据权限
    default List<Long> enableBusinessIdByBossUserId(Long userId) {
        QueryWrapper<BusinessUserDO> queryWrapper = new QueryWrapper<BusinessUserDO>()
                .eq("user_id", userId)
                .ne("business_id", 11L)
                .eq("status", CommonStatusEnum.ENABLE.getStatus())
                .select("business_id")
                .orderByAsc("CASE WHEN business_id = 10 THEN 0 ELSE 1 END")
                .orderByDesc("create_time");
        return selectList(queryWrapper).stream().map(BusinessUserDO::getBusinessId).toList();

    }

    default List<Long> selectEnableCountUserIds(List<Long> userIds) {
        LambdaQueryWrapper<BusinessUserDO> eq = new LambdaQueryWrapper<BusinessUserDO>()
            .select(BusinessUserDO::getUserId)
            .in(BusinessUserDO::getUserId, userIds)
            .eq(BusinessUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
            ;

        return selectList(eq).stream().map(BusinessUserDO::getUserId).toList();
    }
}