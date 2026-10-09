package com.htyoudao.youdao.module.system.dal.dataobject.dept;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 部门表
 *
 * @author ruoyi
 * @author 0090
 */
@TableName("system_store_delivery_scope")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptDeliveryScopeDO extends BusinessBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 门店id
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


    @TableField(exist = false)
    private List<Long> deptIdList;

}
