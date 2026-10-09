package com.htyoudao.youdao.module.commodity.util;


import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductParserUtilTest {

    @Test
    void parseElemeProducts() {
        String s = "甄选套餐C[饮品:可乐,汉堡:香辣鸡腿堡,盒子炸鸡口味:奶香芝士酱-盒子炸鸡]_1*25.9";
        List<ProductQuantity> productQuantities = ProductParserUtil.parseProducts(s, ChannelType.ELE_ME);
        System.out.println(productQuantities);
    }



    @Test
    void parseMeiTuanProducts() {
        String s = "乐享单人自选4件套(1人份,香辣鸡腿堡,薯条（中）,川蜀琵琶腿,可乐),单价20.9*数量1";
        List<ProductQuantity> productQuantities = ProductParserUtil.parseProducts(s, ChannelType.SAN_KUAI);
        System.out.println(productQuantities);
    }
}