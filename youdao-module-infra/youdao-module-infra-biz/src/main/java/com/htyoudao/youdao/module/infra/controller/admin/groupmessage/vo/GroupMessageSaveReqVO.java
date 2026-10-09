package com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 群消息新增/修改 Request VO")
@Data
public class GroupMessageSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "5693")
    private Long id;

    @Schema(description = "临时id,由前端生成", example = "6020")
    private String tmpId;

    @Schema(description = "群id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4782")
    @NotNull(message = "群id不能为空")
    private Long groupId;

    @Schema(description = "发送用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12894")
    private Long sendId;

    @Schema(description = "发送用户昵称", example = "张三")
    private String sendNickName;

    @Schema(description = "接收用户id,逗号分隔，为空表示发给所有成员")
    private String receiverIds;

    @Schema(description = "发送内容")
    private String content;

    @Schema(description = "被@的用户id列表，逗号分隔")
    private String atUserIds;

    @Schema(description = "是否回执消息")
    private Boolean receipt;

    @Schema(description = "回执消息是否完成")
    private Boolean receiptOk;

    @Schema(description = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示不能为空")
    private Integer type;

    @Schema(description = "状态 0:未发出  2:撤回 ", example = "1")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

}
