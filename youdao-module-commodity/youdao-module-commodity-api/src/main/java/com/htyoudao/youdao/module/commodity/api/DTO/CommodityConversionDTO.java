package com.htyoudao.youdao.module.commodity.api.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CommodityConversionDTO  implements Serializable {

    @Serial
    private static final long serialVersionUID = 4556171774940310985L;

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
