package com.htyoudao.youdao.module.infra.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 群 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GroupRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22512")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "群名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("群名字")
    private String name;

    @Schema(description = "群主id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4715")
    @ExcelProperty("群主id")
    private Long ownerId;

    @Schema(description = "群头像")
    @ExcelProperty("群头像")
    private String headImage;

    @Schema(description = "群头像缩略图")
    @ExcelProperty("群头像缩略图")
    private String headImageThumb;

    @Schema(description = "群公告")
    @ExcelProperty("群公告")
    private String notice;

    @Schema(description = "是否被封禁 0:否 1:是")
    @ExcelProperty("是否被封禁 0:否 1:是")
    private Boolean isBanned;

    @Schema(description = "被封禁原因", example = "不好")
    @ExcelProperty("被封禁原因")
    private String reason;

    @Schema(description = "是否已解散")
    @ExcelProperty("是否已解散")
    private Boolean dissolve;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}