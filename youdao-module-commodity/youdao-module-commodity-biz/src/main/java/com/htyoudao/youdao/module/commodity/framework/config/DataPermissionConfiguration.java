package com.htyoudao.youdao.module.commodity.framework.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.activity.CommodityActivityDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // 活动
            rule.addBusinessColumn(CommodityActivityDO.class);
            rule.addBusinessColumn(AfterOrderDO.class);
            //商品
            rule.addBusinessColumn(CommodityCategory.class);
            rule.addBusinessColumn(CommoditySpus.class);
            //门店商品
            rule.addBusinessColumn(CommodityStoreSpu.class);
            rule.addBusinessColumn(CommodityStoreCategory.class);
            rule.addBusinessColumn(CommodityStoreGroup.class);
            rule.addBusinessColumn(CommodityStoreSingle.class);
            rule.addBusinessColumn(CommodityStoreSku.class);
            //同步记录 先不加
//            rule.addBusinessColumn(CommoditySyncTask.class);
            //模板
            rule.addBusinessColumn(CommodityTemplate.class);
            rule.addBusinessColumn(CommodityTemplateCategory.class);
            rule.addBusinessColumn(CommodityTemplateSpus.class);
            //标签
            rule.addBusinessColumn(CommodityTag.class);

        };
    }
}
