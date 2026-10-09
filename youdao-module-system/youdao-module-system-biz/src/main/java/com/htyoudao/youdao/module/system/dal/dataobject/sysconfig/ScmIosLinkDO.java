package com.htyoudao.youdao.module.system.dal.dataobject.sysconfig;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("scm_ios_link")
public class ScmIosLinkDO implements Serializable {

    private static final long serialVersionUID = 5872139693829004332L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;

    private String link;
}