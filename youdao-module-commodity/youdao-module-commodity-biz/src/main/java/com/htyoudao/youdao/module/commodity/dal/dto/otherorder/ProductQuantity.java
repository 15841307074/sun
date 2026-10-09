package com.htyoudao.youdao.module.commodity.dal.dto.otherorder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductQuantity {
    private String productName;
    private Integer quantity;

    @Override
    public String toString() {
        return  "[" +productName + ":" + quantity  + "]" ;
    }
}