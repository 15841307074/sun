package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.util.Date;

/**
 * <p>
 * 拼单
 * </p>
 *
 * @author zhangjihe
 * @since 2024-12-24
 */
@Data
@TableName("bz_splicing_order")
public class BzSplicingOrderDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -4411669884217038343L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String openId;

    private String mainId;

    /**
     * 状态 0正常 1锁定 2取消 3完结
     */
    private Integer status;

    private Long storeId;

    private Long projectOwnerShip;
}
