package com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会员卡表
 * </p>
 *
 * @author dht
 * @since 2024-10-09
 */


@TableName("wx_member_card")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMemberCardDO extends BusinessBaseDO implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员卡ID
     */
    @Schema(description = "会员卡ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableId(value = "member_card_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberCardId;



    /**
     * 名称
     */
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 积分最低门槛
     */
    @Schema(description = "积分最低门槛", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minPointsThreshold;

    /**
     * 积分最高门槛
     */
    @Schema(description = "积分最高门槛", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long maxPointsThreshold;

    /**
     * 优惠卷编号
     */
    @Schema(description = "优惠卷编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    /**
     * 会员卡状态 1启用 2禁用 3 删除
     */
    @Schema(description = "会员卡状态 1启用 2禁用 3 删除", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cardStatus;

    /**
     * 会员卡背景图
     */
    @Schema(description = "会员卡背景图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String backgroundImage;

    /**
     * 会员卡详情图
     */
    @Schema(description = "会员卡详情图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detailImage;

    /**
     * 会员卡描述
     */
    @Schema(description = "会员卡描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * 会员卡缩略图
     */
    @Schema(description = "会员卡缩略图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String thumbnailImage;

    /**
     * 会员卡小标图
     */
    @Schema(description = "会员卡小标图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String iconImage;

    /**
     * 会员等级
     */
    @Schema(description = "会员等级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer memberLevel;

    /**
     * 会员日
     */
    @Schema(description = "会员日", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date memberDay;


    @TableField(exist = false)
    List<String> backgroundImageList;

    @TableField(exist = false)
    List<String> detailImageList;
//
//    @TableField(exist = false)
//    List<String> couponCodeList;
//
//    @TableField(exist = false)
//    List<String> thumbnailImageList;
//
//    @TableField(exist = false)
//    List<String> backgroundImageList;
//
//    @TableField(exist = false)
//    List<String> iconImageList;

    public void setDetailImage(String dayNumbers) {
        this.detailImage = dayNumbers;
        if(ObjectUtil.isNotEmpty(dayNumbers)){
            String[] array = dayNumbers.split(",");
            this.detailImageList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.detailImageList.add(array[i]);
            }
        }else {
            this.detailImageList = null;
        }
    }

    public void setDetailImageList(List<String> detailImageList) {
        this.detailImageList = detailImageList;
        if(ObjectUtil.isNotEmpty(detailImageList)){
            this.detailImage = detailImageList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.detailImage = null;
        }
    }

}
