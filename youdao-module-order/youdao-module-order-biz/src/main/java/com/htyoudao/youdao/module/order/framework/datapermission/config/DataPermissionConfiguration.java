package com.htyoudao.youdao.module.order.framework.datapermission.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * order 模块的数据权限 Configuration
 *
 * @author 0090
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // business
//            rule.addBusinessColumn(BzOrderDO.class);


            //todo 后台页面显示business_id
        };
    }

}
