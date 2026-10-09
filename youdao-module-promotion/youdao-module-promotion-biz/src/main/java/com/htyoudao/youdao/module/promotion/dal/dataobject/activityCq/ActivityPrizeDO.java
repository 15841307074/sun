package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;

@TableName(value = "activity_prize", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPrizeDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 奖品类型
     */
    private Integer prizeType;

    /**
     * 奖品名称
     */
    private String prizeName;

    /**
     * 奖品数量
     */
    private Integer prizeNum;

    /**
     * 奖品价值
     */
    private BigDecimal prizeValue;

    /**
     * 中奖概率
     */
    private BigDecimal probability;

    /**
     * 奖品图片
     */
    private String prizeImgUrl;

    /**
     * 奖品业务id
     */
    private Long awardId;

    /**
     * 已领取数量
     */
    private Integer remainNum;

    /**
     * 是否多次获取 0 否 1 是
     */
    private Integer isRepeat;

    /**
     * 中奖城市范围
     */
    private String winningCitys;

    /**
     * 券名称
     */
    private String couponName;

    /**
     * 是否保底
     */
    private Integer isGuarantees;

    /**
     * 保底次数
     */
    private Integer minimumNumber;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateUserName;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 编码
     */
    private String code;
}
