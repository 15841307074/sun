package com.htyoudao.youdao.module.member.dal.dataobject.address;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.math.BigDecimal;

@TableName("wx_member_address")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMemberAddressDO extends BusinessBaseDO {

    /**
     * 地址ID
     */
    @TableId
    private Long addressId;

    /**
     * 用户ID
     */
    private Long memberId;

    /**
     * 是否默认
     */
    Integer defaultAddress;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人性别
     */
    private Integer receiverGender;

    /**
     * 收货地址标签
     */
    private String addressLabel;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String address;
    /**
     * 收货详细地址
     */
    private String addressDetail;

    /**
     * 收货地址经度
     */
    private BigDecimal longitude;

    /**
     * 收货地址纬度
     */
    private BigDecimal latitude;

    /**
     * 删除标识
     */
    private int delFlag;
    //城市名字
    private String cityName;

}
