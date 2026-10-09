package com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 群消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMessagePageReqVO extends PageParam {

    @Schema(description = "临时id,由前端生成", example = "6020")
    private String tmpId;

    @Schema(description = "群id", example = "4782")
    private Long groupId;

    @Schema(description = "发送用户id", example = "12894")
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

    @Schema(description = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示", example = "1")
    private Integer type;

    @Schema(description = "状态 0:未发出  2:撤回 ", example = "1")
    private Integer status;

    @Schema(description = "发送时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sendTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}