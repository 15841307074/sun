package com.htyoudao.youdao.module.system.dal.dataobject.complaint;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.util.Date;
/**
 *  投诉管理 DO
 *
 * @author ssz
 */
@TableName(value = "system_complaint", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintDO extends BusinessBaseDO {

    @TableId
    private Long id;
    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 会员昵称名称
     */
    private String memberNickName;

    /**
     * 投诉联系电话
     */
    private String complaintMobile;

    /**
     * 会员电话
     */
    private String memberMobile;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 投诉内容
     */
    private String complaintNote;

    /**
     * 处理内容
     */
    private String dealNote;
    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dealTime;
    /**
     * 处理状态 0 未处理 1已处理
     */
    private Integer complaintType;

    /**
     * openid
     */
    private String openid;

    /**
     * 业务类型  0 其他投诉 1 产品投诉 2 服务投诉 3 卫生投诉
     */
    private Integer businessType;


}
