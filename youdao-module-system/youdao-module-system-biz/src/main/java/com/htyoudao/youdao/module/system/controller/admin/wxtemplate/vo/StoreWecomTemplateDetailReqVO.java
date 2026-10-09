package com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Schema(description = "企业微信模版 - 企业微信模版表 Request VO")
@Data
public class StoreWecomTemplateDetailReqVO extends BusinessBaseDO {

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
     * 模版状态（0 默认  1正常）
     */
    @Schema(description = "模版状态（0 默认  1正常）")
    private Integer state;



    /**
     * 展示门店（1 全部门店  2部分门店）
     */
    @Schema(description = "展示门店（1 全部门店  2部分门店）")
    private Integer showStore;


    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS =new ArrayList<>();

    @Schema(description = "标签列表逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String tagNumbers;


    @Schema(description = "指定标签列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> tagList;


    @Schema(description = "指定标签名称列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TemplateTagRespVO> tagNameList;



    public void setTagList(List<Long> tagList) {
        this.tagList = tagList;
        if(ObjectUtil.isNotEmpty(tagList)){
            this.tagNumbers = tagList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.tagNumbers = null;
        }
    }
    public void setTagNumbers(String tagNumbers) {
        this.tagNumbers = tagNumbers;
        if(ObjectUtil.isNotEmpty(tagNumbers)){
            String[] array = tagNumbers.split(",");
            this.tagList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.tagList.add(Long.parseLong(array[i]));
            }
        }else {
            this.tagList = null;
        }
    }


}
