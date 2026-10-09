package com.htyoudao.youdao.module.promotion.dal.dataobject.lottery;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class BaseEntity {

//    /**
//     * 主键ID
//     */
//    @TableId(value = "id", type = IdType.ASSIGN_ID)
//    private Long id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @Schema(name = "createTime",title="这是啥",description = "23")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name",fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createUserName;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name",fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateUserName;

    /**
     * 删除标识(0正常 1删除)
     */
    @TableField(value = "is_delete",fill = FieldFill.INSERT)
    //@TableLogic(value = "0",delval = "UNIX_TIMESTAMP()")
    @ExcelIgnore
    private Integer isDelete;

    /**
     * 项目id
     */
    @TableField(value = "project_owner_ship",fill = FieldFill.INSERT)
    @ExcelIgnore
    private Long projectOwnerShip;
}
