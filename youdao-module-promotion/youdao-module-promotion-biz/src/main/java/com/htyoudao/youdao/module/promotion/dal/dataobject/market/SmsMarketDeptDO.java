package com.htyoudao.youdao.module.promotion.dal.dataobject.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author dht
 */
@Data
@TableName("sms_market_dept")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SmsMarketDeptDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短信营销表id
     */
    private Long smsMarketId;

    /**
     * 门店id
     */
    private Long deptId;
}
