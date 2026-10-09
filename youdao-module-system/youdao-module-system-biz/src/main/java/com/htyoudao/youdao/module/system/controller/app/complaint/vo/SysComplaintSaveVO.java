package com.htyoudao.youdao.module.system.controller.app.complaint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SysComplaintSaveVO   {

    @Schema(name = "imgUrl", description = "投诉图片")
    private List<String> imgUrl;
    @Schema(name = "isStore", description = "是否是门店 0 否 1是")
    private Integer isStore  ;
    @Schema(name = "id", description = "id")
    private Integer id;
    @Schema(name = "storeId", description = "门店id")
    private Long storeId;
    @Schema(name = "storeName", description = "门店名称")
    private String storeName;
    @Schema(name = "memberId", description = "会员id")
    private Long memberId;
    @Schema(name = "memberNickName", description = "会员昵称名称")
    private String memberNickName;
    @Schema(name = "complaintMobile", description = "投诉联系电话")
    private String complaintMobile;
    @Schema(name = "memberMobile", description = "会员电话")
    private String memberMobile;
    @Schema(name = "orderSn", description = "订单号")
    private String orderSn;
    @Schema(name = "complaintNote", description = "投诉内容")
    private String complaintNote;
    @Schema(name = "dealNote", description = "处理内容")
    private String dealNote;
    @Schema(name = "dealTime", description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dealTime;
    @Schema(name = "complaintType", description = "处理状态 0 未处理 1已处理")
    private Integer complaintType;
    @Schema(name = "openid", description = "openid")
    private String openid;
    @Schema(name = "businessType", description = "业务类型  0 其他投诉 1 产品投诉 2 服务投诉 3 卫生投诉")
    private Integer businessType;
}
