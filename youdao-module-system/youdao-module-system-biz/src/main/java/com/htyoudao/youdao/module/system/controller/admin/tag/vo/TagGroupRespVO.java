package com.htyoudao.youdao.module.system.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 标签组 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TagGroupRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11000")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "标签名称", example = "李四")
    @ExcelProperty("标签组名称")
    private String name;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "标签集合")
    private List<TagValueRespVO> tagValues;
}