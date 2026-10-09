package com.htyoudao.youdao.module.errand.dal.mysql.errandRunner;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface ErrandRunnerMapper extends BaseMapper<ErrandRunnerDO> {

    @Select("SELECT * FROM bz_errand_runner WHERE member_id = #{memberId} AND deleted = b'0' LIMIT 1 FOR UPDATE")
    ErrandRunnerDO selectByMemberIdForUpdate(@Param("memberId") Long memberId);

    @Select("SELECT * FROM bz_errand_runner WHERE id = #{id} AND deleted = b'0' LIMIT 1 FOR UPDATE")
    ErrandRunnerDO selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM bz_errand_runner WHERE member_id = #{memberId} AND business_id = #{businessId} AND deleted = b'1' ORDER BY update_time DESC LIMIT 1")
    ErrandRunnerDO selectDeletedByMemberId(@Param("memberId") Long memberId, @Param("businessId") Long businessId);

    @Update("UPDATE bz_errand_runner SET store_id = #{storeId}, name = #{name}, phone = #{phone}, student_no = #{studentNo}, " +
            "id_card_no = #{idCardNo}, gender = #{gender}, id_card_front = #{idCardFront}, id_card_back = #{idCardBack}, " +
            "student_card_img = #{studentCardImg}, audit_status = #{auditStatus}, first_audit_status = #{firstAuditStatus}, " +
            "popup_status = #{popupStatus}, audit_reason = #{auditReason}, audit_time = #{auditTime}, audit_user_id = #{auditUserId}, " +
            "deleted = b'0', update_time = #{updateTime},ban_status = #{banStatus} WHERE id = #{id} AND business_id = #{businessId} AND deleted = b'1'")
    int restoreDeletedRunner(ErrandRunnerDO runner);

    // ErrandRunnerMapper.java
    @Update("UPDATE bz_errand_runner SET balance = #{newBalance} WHERE member_id = #{memberId} AND balance = #{oldBalance}")
    int updateBalance(@Param("memberId") Long memberId,
                      @Param("oldBalance") BigDecimal oldBalance,
                      @Param("newBalance") BigDecimal newBalance);

    @Select("SELECT * FROM bz_errand_runner WHERE member_id = #{memberId} LIMIT 1 FOR UPDATE")
    ErrandRunnerDO selectByMemberId(@Param("memberId") Long memberId);


    @Select("SELECT * FROM bz_errand_runner WHERE id = #{runnerId}  LIMIT 1 FOR UPDATE")
    ErrandRunnerDO selectByRunnerIdForUpdate(@Param("runnerId") Long runnerId);
}
