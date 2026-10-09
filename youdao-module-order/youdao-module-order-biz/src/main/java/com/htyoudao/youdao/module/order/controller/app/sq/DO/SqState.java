package com.htyoudao.youdao.module.order.controller.app.sq.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sq_state")
public class SqState {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    /**
     * 0=否 1=是
     */
    private Integer state;

    /**
     * 0=未删除 1=删除
     */
    private Integer deleted;
}
