package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVoteLogExportRespVO {

    @ExcelProperty("会员昵称")
    private String memberName;

    @ExcelProperty("联系方式")
    private String memberMobile;

    @ExcelProperty("投票编号")
    private String id;

    @ExcelProperty("投票选项")
    private String optionName;

    @ExcelProperty("投票时间")
    private LocalDateTime voteTime;

    @ExcelProperty("参与门店")
    private String storeName;

    @ExcelProperty("累计投票次数")
    private Integer totalVoteCount;
}
