package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import cn.hutool.core.date.DateTime;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ActivityCardLogExportRespVO {

    @ExcelProperty("会员昵称")
    @Schema(name = "memberName", description = "会员名称")
    private String memberName;

    @ExcelProperty("联系方式")
    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;

    @ExcelProperty("获得卡片")
    @Schema(name = "cardName", description = "卡片名")
    private String cardName;

    @ExcelProperty("卡片类型")
    @Schema(name = "cardTypeName", description = "卡片类型 1 兜底卡 2 套系卡 3 万能卡 4 隐藏卡")
    private String cardTypeName;

    @ExcelProperty("卡片图片")
    @Schema(name = "cardImgUrl", description = "卡片图片")
    private String cardImgUrl;

    @ExcelProperty("卡片状态")
    @Schema(name = "cardStatus", description = "卡片状态")
    private String cardStatus;


    @ExcelProperty("抽卡时间")
    private LocalDateTime createTime;

    @ExcelProperty("抽卡门店")
    @Schema(name = "storeName", description = "抽奖门店")
    private String storeName;


}
