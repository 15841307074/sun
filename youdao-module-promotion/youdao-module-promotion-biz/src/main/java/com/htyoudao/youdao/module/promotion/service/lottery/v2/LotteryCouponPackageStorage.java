package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryCouponGrantMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import java.util.Map;
import java.util.Objects;
/** 每张券独立短事务；发券身份固定，人工补发只补缺失券。 */
@Service
@DS("sharding")
public class LotteryCouponPackageStorage {
    @Resource private UserCouponMapper coupons;
    @Resource private UserCouponPackageMapper packages;
    @Resource private LotteryCouponGrantMapper receipts;
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public void coupon(UserCouponDO row) {
        var previous=receipts.coupon(row.getUserId(),row.getId());
        if(previous!=null){verify(previous,row.getBusinessId(),row.getUserId(),row.getPackageId(),row.getCouponId());return;}
        try{if(coupons.insert(row)!=1)throw new IllegalStateException("优惠券未保存");}catch(DuplicateKeyException conflict){verify(receipts.coupon(row.getUserId(),row.getId()),row.getBusinessId(),row.getUserId(),row.getPackageId(),row.getCouponId());}
    }
    @Transactional(rollbackFor=Exception.class,timeout=5)
    public void header(UserCouponPackageDO row) {
        var previous=receipts.header(row.getUserId(),row.getId());
        if(previous!=null){verify(previous,row.getBusinessId(),row.getUserId(),row.getPackageId(),null);return;}
        try{if(packages.insert(row)!=1)throw new IllegalStateException("优惠券包关系未保存");}catch(DuplicateKeyException conflict){verify(receipts.header(row.getUserId(),row.getId()),row.getBusinessId(),row.getUserId(),row.getPackageId(),null);}
    }
    private void verify(Map<String,Object> row,Long business,Long member,Long pack,Long coupon) {
        if(row==null||!equal(row.get("business_id"),business)||!equal(row.get("user_id"),member)||!equal(row.get("package_id"),pack)
                ||coupon!=null&&!equal(row.get("coupon_id"),coupon))throw new IllegalStateException("券包固定发放编号与原奖品不一致");
    }
    private boolean equal(Object value,Long expected){return value instanceof Number n&&Objects.equals(n.longValue(),expected);}
}
