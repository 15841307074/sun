package com.htyoudao.youdao.module.commodity.dal.dataobject.commodityStastics;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
@Data
@TableName("scm_commodity_stastics")
public class ScmCommodityStastics {

    /**
     * 主键ID（自增）
     */
    private Long id;

    /**
     * 统计名称
     */
    private String stasticsName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 删除标识(0正常 1删除)
     */
    private Integer isDelete;
}
