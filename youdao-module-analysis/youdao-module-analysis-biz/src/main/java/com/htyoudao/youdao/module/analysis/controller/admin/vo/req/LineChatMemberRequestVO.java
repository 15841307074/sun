package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 会员 聚合请求参数对象 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineChatMemberRequestVO extends AggMemberRequestVO{

    @Schema(description = "指标code")
    @NotNull
    private String code;

}
