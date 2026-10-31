package com.htyoudao.youdao.module.system.controller.admin.complaint.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投诉列表导出数据。
 */
@Data
@ExcelIgnoreUnannotated
public class ComplaintExportVO {

    @ExcelProperty("会员昵称")
    private String memberNickName;

    @ExcelProperty("会员手机号")
    private String memberMobile;

    @ExcelProperty("投诉联系电话")
    private String complaintMobile;

    @ExcelProperty("投诉门店")
    private String storeName;

    @ExcelProperty("组织机构/负责人")
    private String orgNameAndLeader;

    @ExcelProperty("门店标签")
    private String storeTagNames;

    @ExcelProperty("订单号")
    private String orderSn;

    @ExcelProperty("投诉内容")
    @ColumnWidth(40)
    private String complaintNote;

    @ExcelProperty("投诉附件地址")
    @ColumnWidth(60)
    private String complaintImageUrls;

    @ExcelProperty("投诉类型")
    private String businessTypeName;

    @ExcelProperty("是否接收回访")
    private String acceptFollowUpName;

    @ExcelProperty("处理状态")
    private String complaintStatusName;

    @ExcelProperty("处理内容")
    @ColumnWidth(40)
    private String dealNote;

    @ExcelProperty("投诉时间")
    private LocalDateTime createTime;
}
