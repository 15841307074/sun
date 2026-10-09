package com.htyoudao.youdao.module.system.dal.dataobject.wxtemplate;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@TableName("store_wecom_template")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StoreWecomTemplateDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 6855594069250478981L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模版名称
     */
    private String templateName;

    /**
     * 店长企微码展示图
     */
    private String wechatImage;


    /**
     * 门店群活码展示图
     */
    private String groupImage;

    /**
     * 最后发布时间
     */
    private Date releaseTime;


    /**
     * 发布状态
     */
    private Integer releaseStatus;


    /**
     * 应用范围（1按门店  2按标签）
     */
    private Integer applicationScope;



    /**
     * 展示门店（1 全部门店  2部分门店）
     */
    private Integer showStore;

    /**
     * 模版状态（0 默认  1正常）
     */
    private Integer state;

    /**
     * 标签列表 按逗号分隔
     */
    private String tagNumbers;

    @TableField(exist = false)
    private List<Long> tagList;


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
