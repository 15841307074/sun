package com.htyoudao.youdao.module.promotion.api.activity.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ActivityNjnzDTO extends ActivityBaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2841086221404665193L;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
     */
    @Schema(description = "优惠类型 1（1第二件半件，2买一送一，3自定义优惠）")
    private Integer discountType;

    /**
     * 标签名
     */
    @Schema(description = "标签名")
    private String tag;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 优惠第几件
     */
    @Schema(description = "优惠第几件")
    private Integer discountItemNum;

    /**
     * 优惠打几折
     */
    @Schema(description = "优惠打几折")
    private Double discountRate;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
     */
    @Schema(description = "可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）")
    private List<Integer> stackableActivities;

    /**
     * 活动备注
     */
    private String activityRemark;

    private LocalDateTime createTime;
}
