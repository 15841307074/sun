package com.htyoudao.youdao.module.promotion.dal.dataobject.lottery;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("lottery_prize")
@AllArgsConstructor
@NoArgsConstructor
public class LotteryPrizeDO extends BusinessBaseDO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 奖品设置id */
    @Schema(name = "lotteryId", description = "奖品设置id")
    private Long lotteryId;

    /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
    @Schema(name = "prizeType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 5 现金红包")
    private Integer prizeType;

    /** 奖品名称 */
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;

    /** 奖品数量 */
    @Schema(name = "prizeNum", description = "奖品数量")
    private Integer prizeNum;

    /** 已领取数量 */
    @Schema(name = "remainNum", description = "已领取数量")
    private Integer remainNum;

    /** 奖品价值 */
    @Schema(name = "prizeValue", description = "奖品价值")
    private BigDecimal prizeValue;

    /** 是否多次获取 0 否 1 是 */
    @Schema(name = "isRepeat", description = "是否多次获取 0 否 1 是")
    private Integer isRepeat;

    /** 奖品id(根据类型判断是优惠卷 id 还是商品 id) */
    @Schema(name = "awardId", description = "奖品id(根据类型判断是优惠卷 id 还是商品 id)")
    private Long awardId;

    /** 获取概率 单位% */
    @Schema(name = "probability", description = "获取概率 单位%")
    private BigDecimal probability;

    /** 奖品图片 */
    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;

    /** 是否为保底商品(必须为 无奖品 必须 存在至少1条) */
    @Schema(name = "isGuarantees", description = "是否为保底商品(必须为 无奖品 必须 存在至少1条)")
    private Integer isGuarantees;

    @Schema(name = "minimumNumber", description = "保底次数")
    private Integer minimumNumber;

    @Schema(name = "winningCitys", description = "中奖区域范围")
    private String winningCitys;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name",fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name",fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateUserName;
    /** 门店id */
    @Schema(name = "storeId", description = "门店id")
    private Long storeId;


    @Schema(name = "couponName", description = "优惠卷名称")
    private String couponName;


    @Schema(name = "code", description = "编码")
    private String code;
    /**
     * 删除标识(0正常 1删除)
     */
    /*@TableField(value = "is_delete",fill = FieldFill.INSERT)
    //@TableLogic(value = "0",delval = "UNIX_TIMESTAMP()")
    @ExcelIgnore
    private Integer isDelete;*/

}
