package com.htyoudao.youdao.module.commodity.dal.dataobject.ScmUnitConversion;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 单位换算表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-06-04
 */
@Getter
@Setter
@TableName("scm_unit_conversion")
public class ScmUnitConversion  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID 主键
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long commodityId;

    /**
     * 换算前数量
     */
    private Integer beforeNumber;

    /**
     * 换算前单位
     */
    private String beforeUnit;

    /**
     * 换算后数量
     */
    private Integer afterNumber;

    /**
     * 换算后单位
     */
    private String afterUnit;
    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @ExcelIgnore
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
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
}
