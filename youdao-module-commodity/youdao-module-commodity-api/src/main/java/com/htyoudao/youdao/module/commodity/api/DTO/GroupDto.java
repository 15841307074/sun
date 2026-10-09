package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class GroupDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -429507949546248831L;

    private String groupName;
    private Long groupId;
    /**
     * 必选商品数量
     */
    private int choose;
    /**
     * 是否多选商品
     */
    private Integer chooseMany;
    /**
     * 门店下套餐分组里可选的数量
     */
    private int commodityStoreGroupChoose;

    private List<SingleDto> singleList = new ArrayList<>();
    //顺序
    Integer sort;

    /**
     * 门店下套餐分组里分组属性（本组单品是否包含以下所有商品）
     */
    private Integer commodityStoreGroupAttribute;
}
