package com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 企微模版 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreWecomTemplatePageRespVO{

    @Serial
    private static final long serialVersionUID = 6855594069250478981L;

    @Schema(description = "主键ID")
    private Long id;

    /**
     * 模版名称
     */
    @Schema(description = "模版名称")
    private String templateName;

    /**
     * 店长企微码展示图
     */
    @Schema(description = "店长企微码展示图")
    private String wechatImage;


    /**
     * 门店群活码展示图
     */
    @Schema(description = "门店群活码展示图")
    private String groupImage;

    /**
     * 最后发布时间
     */
    @Schema(description = "最后发布时间")
    private Date releaseTime;


    /**
     * 发布状态
     */
    @Schema(description = "发布状态")
    private Integer releaseStatus;


    /**
     * 应用范围（1按门店  2按标签）
     */
    @Schema(description = "应用范围（1按门店  2按标签）")
    private Integer applicationScope;



    /**
     * 展示门店（1 全部门店  2部分门店）
     */
    @Schema(description = "展示门店（1 全部门店  2部分门店）")
    private Integer showStore;

    /**
     * 模版状态（0 默认  1正常）
     */
    @Schema(description = "模版状态（0 默认  1正常）")
    private Integer state;


    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS =new ArrayList<>();

}
