package com.htyoudao.youdao.module.system.controller.admin.complaint.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ComplaintRespVO {
    @Schema(name = "id", description = "id")
    private Long id;
    @Schema(name = "memberNickName", description = "会员昵称名称")
    private String memberNickName;
    @Schema(name = "memberMobile", description = "预留电话")
    private String memberMobile;
    @Schema(name = "complaintMobile", description = "投诉联系电话")
    private String complaintMobile;
    @Schema(name = "storeName", description = "投诉对象")
    private String storeName;
    @Schema(name = "orderSn", description = "订单号")
    private String orderSn;
    @Schema(name = "complaintNote", description = "投诉内容")
    private String complaintNote;
    @Schema(name = "dealNote", description = "处理内容")
    private String dealNote;
    @Schema(name = "处理状态", description = "处理状态 0 未处理 1已处理")
    private Integer complaintType;
    @Schema(name = "业务类型", description = "业务类型  0 其他投诉 1 产品投诉 2 服务投诉 3 卫生投诉")
    private Integer businessType;
    @Schema(name = "投诉时间")
    private LocalDateTime createTime;
    // 添加格式化方法
    public String getCreateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createTime != null ? createTime.format(formatter) : null;
    }

}