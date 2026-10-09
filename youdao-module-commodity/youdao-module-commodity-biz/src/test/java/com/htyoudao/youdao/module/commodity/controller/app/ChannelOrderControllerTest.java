package com.htyoudao.youdao.module.commodity.controller.app;


import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.commodity.CommodityServerApplication;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ChannelSankuaiOrder;
import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import com.htyoudao.youdao.module.commodity.util.ProductParserUtil;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest(classes = CommodityServerApplication.class)
class ChannelOrderControllerTest {

    @Resource
    private ExcelActionService excelActionService;

    @Test
    void importSankuaiExcel() throws IOException {
        File file  = new File("/Users/suddong/Desktop/美团订单.csv");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", // form字段名
            file.getName(), // 原始文件名
            "application/octet-stream", // 内容类型
            input // 文件内容
        );
        List<ChannelSankuaiOrder> list = excelActionService.importExcel(multipartFile.getInputStream(), ChannelSankuaiOrder.class);
        for (ChannelSankuaiOrder channelSankuaiOrder : list) {
            String productInfo = channelSankuaiOrder.getProductInfo();
            List<ProductQuantity> products = ProductParserUtil.parseProducts(productInfo, ChannelType.SAN_KUAI);
        }
    }

    @Test
    void importElemeExcel() {
    }
}