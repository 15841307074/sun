package com.htyoudao.youdao.module.system.dal.dataobject.goodcoupon;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author duht
 * 此服务单独用的BaseEntity
 */
@Data
public class FlexBaseEntity {



    @ExcelIgnore
    private String createUserName;

    @ExcelIgnore
    private String updateUserName;

    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ExcelIgnore
    private Integer isDelete;

    @ExcelIgnore
    private Long projectOwnerShip;
}
