package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CommodityConversion{
    private Long id;
    private Long commodityId;
    private Integer beforeNumber;
    private String beforeUnit;
    private BigDecimal afterNumber;
    private String afterUnit;
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String createUserName;

    private String updateUserName;

    private Boolean isDelete;




}
