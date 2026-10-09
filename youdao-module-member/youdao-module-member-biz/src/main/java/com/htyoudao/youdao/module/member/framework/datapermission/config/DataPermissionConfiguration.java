package com.htyoudao.youdao.module.member.framework.datapermission.config;

import com.htyoudao.youdao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import com.htyoudao.youdao.module.member.dal.dataobject.address.WxMemberAddressDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的数据权限 Configuration
 *
 * @author 0090
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // 会员
            rule.addBusinessColumn(WxMemberAddressDO.class);
            rule.addBusinessColumn(PointsLogDO.class);
            rule.addBusinessColumn(PointsProductDO.class);
            rule.addBusinessColumn(WxMemberDO.class);
            rule.addBusinessColumn(WxMemberCardDO.class);
            rule.addBusinessColumn(CustomCrowdDO.class);

            //todo 后台页面显示business_id
        };
    }

}
