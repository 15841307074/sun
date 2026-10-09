package com.htyoudao.youdao.module.promotion.service.couponPackageShare;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponPackageShare.CouponPackageShareDO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface CouponPackageShareService  {
    void createCouponPackageShare(@Valid CouponPackageSaveReqVO couponPackageSaveReqVO);

    CouponPackageShareRespVO getPackageShareDetail(CouponPackageDetailReqVO couponPackageDetailReqVO);

    void issueCoupon(CouponPackageSendAO couponPackageSendAO);

    void importCouponExl(MultipartFile file, int num, Long packageId);

    String getLittlePic();

    String getLargePic();

    /**
     * 新增券包时候 新增分享  只有标题
     * @param shareSaveReqVO shareSaveReqVO
     */
    void insert(CouponPackageShareSaveReqVO shareSaveReqVO);

    /**
     * 修改券包的时候 修改分享 只改标题
     * @param shareSaveReqVO shareSaveReqVO
     */
    void updateTitle(CouponPackageShareSaveReqVO shareSaveReqVO);
}
