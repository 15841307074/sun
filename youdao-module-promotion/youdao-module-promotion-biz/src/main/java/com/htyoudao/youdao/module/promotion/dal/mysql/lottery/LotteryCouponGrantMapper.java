package com.htyoudao.youdao.module.promotion.dal.mysql.lottery;
import org.apache.ibatis.annotations.*;
import java.util.Map;
/** 含软删除记录也要核对：用户删除券不能导致补发重复生成。 */
@Mapper
public interface LotteryCouponGrantMapper {
    @Options(timeout=5)
    @Select("SELECT business_id,user_id,coupon_id,package_id FROM user_coupon WHERE user_id=#{member} AND id=#{id}")
    Map<String,Object> coupon(@Param("member")long member,@Param("id")long id);
    @Options(timeout=5)
    @Select("SELECT business_id,user_id,package_id FROM user_coupon_package WHERE user_id=#{member} AND id=#{id}")
    Map<String,Object> header(@Param("member")long member,@Param("id")long id);
}
