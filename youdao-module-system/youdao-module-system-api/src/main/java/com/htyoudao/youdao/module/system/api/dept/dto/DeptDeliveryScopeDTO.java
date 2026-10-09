package com.htyoudao.youdao.module.system.api.dept.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 门店配送范围
 * </p>
 *
 * @author
 */
@Data
public class DeptDeliveryScopeDTO implements Serializable {

    /**
     * 部门id
     */
    private Long storeId;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 维度
     */
    private Double latitude;

}
