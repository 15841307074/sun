package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class SysBaseEntity {
    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
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
    @TableField(value = "create_by",fill = FieldFill.INSERT)
    @ExcelIgnore
    private String createBy;

    /**
     * 修改人名称
     */
    @TableField(value = "update_by",fill = FieldFill.UPDATE)
    @ExcelIgnore
    private String updateBy;

    /**
     * 删除标识(0正常 1删除)
     */
    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    //@TableLogic(value = "0",delval = "UNIX_TIMESTAMP()")
    @ExcelIgnore
    private Integer delFlag;

    /**
     * 项目id
     */
    @TableField(value = "project_owner_ship",fill = FieldFill.INSERT)
    @ExcelIgnore
    private Long projectOwnerShip;
}
