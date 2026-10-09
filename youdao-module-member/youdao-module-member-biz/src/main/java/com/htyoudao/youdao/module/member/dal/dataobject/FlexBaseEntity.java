package com.htyoudao.youdao.module.member.dal.dataobject;

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


    private String createUserName;


    private String updateUserName;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")

    private Date createTime;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;



    private Integer isDelete;


    private Long projectOwnerShip;
}
