package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorePageReqVO extends PageParam {
    @Schema(description = "门店名称/门店编码，模糊匹配", example = "youdao")
    private String text;
    @Schema(description = "门店状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer storeStatus;
    @Schema(description = "经营状态，参见 CommonStatusEnum 枚举类", example = "1")
    private Integer openStatus;
    @Schema(description = "校园配送筛选：null全部，0支持，1不支持", example = "0")
    private Integer campusDeliveryStatus;
    @Schema(description = "部门编号，同时筛选子部门", example = "1024")
    private Long orgId;
    @Schema(description = "标签id", example = "1024")
    private Long tagId;

    /**
     * 多选标签 ID，最多选择 100 个标签。
     */
    @Schema(description = "多选标签ID，最多100个", example = "[1024, 1025]")
    @Size(max = 100, message = "标签最多选择100个")
    private List<Long> tagIds;

    /**
     * 标签匹配类型：1 同时满足全部标签，2 满足任意标签，3 无标签。
     */
    @Schema(description = "标签匹配类型：1同时满足全部标签，2满足任意标签，3无标签", example = "1")
    @Min(value = 1, message = "标签匹配类型只能为1、2或3")
    @Max(value = 3, message = "标签匹配类型只能为1、2或3")
    private Integer type;

    private Set<Long> orgIds;

    @Schema(description = "每页条数，最大值为 2000", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = 2000, message = "每页条数最大值为 2000")
    private Integer pageSize = 10;
    @Schema(description = "门店店长/店长电话，模糊匹配", example = "youdao")
    private String leaderText;
}
