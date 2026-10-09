package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreTagReqVO  {
    @Schema(description = "查询方式  1 满足任意  2 全部满足 3 无标签", example = "1")
    private Integer type;
    @Schema(description = "标签ids", example = "1024")
    private List<Long> tagIds = new ArrayList<>();
    @Schema(description = "查询方式  1全部门店")
    private Integer status;

}
