package com.htyoudao.youdao.module.member.controller.admin.tag.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 会员批量添加/删除标签 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MembersUpdateVO {
    @Schema(description = "会员批量标签", requiredMode = Schema.RequiredMode.REQUIRED)
   List<MembersBatchVO> storesBatchVOList = new ArrayList<>();
}
