package com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 群消息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GroupMessageRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5693")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "临时id,由前端生成", example = "6020")
    @ExcelProperty("临时id,由前端生成")
    private String tmpId;

    @Schema(description = "群id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4782")
    @ExcelProperty("群id")
    private Long groupId;

    @Schema(description = "发送用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12894")
    @ExcelProperty("发送用户id")
    private Long sendId;

    @Schema(description = "发送用户昵称", example = "张三")
    @ExcelProperty("发送用户昵称")
    private String sendNickName;

    @Schema(description = "发送内容")
    @ExcelProperty("发送内容")
    private String content;

    @Schema(description = "被@的用户id列表，逗号分隔")
    @ExcelProperty("被@的用户id列表，逗号分隔")
    private String atUserIds;

    @Schema(description = "是否回执消息")
    @ExcelProperty("是否回执消息")
    private Boolean receipt;

    @Schema(description = "回执消息是否完成")
    @ExcelProperty("回执消息是否完成")
    private Boolean receiptOk;

    @Schema(description = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示")
    private Integer type;

    @Schema(description = "状态 0:未发出  2:撤回 ", example = "1")
    @ExcelProperty("状态 0:未发出  2:撤回 ")
    private Integer status;

    @Schema(description = "已读消息数量")
    private Integer readCount;

    @Schema(description = "发送时间")
    @ExcelProperty("发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
