package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店加盟商信息关系表 DO
 */
@TableName(value = "system_store_franchisee_info", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemStoreFranchiseeInfoDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 加盟商姓名
     */
    private String franchiseeName;

    /**
     * 加盟商手机号
     */
    private String franchiseeMobile;

    /**
     * 加盟商身份证号
     */
    private String idCardNo;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 开户省份
     */
    private String bankProvince;

    /**
     * 开户城市
     */
    private String bankCity;

    /**
     * 银行卡账号
     */
    private String bankCardNo;
}
