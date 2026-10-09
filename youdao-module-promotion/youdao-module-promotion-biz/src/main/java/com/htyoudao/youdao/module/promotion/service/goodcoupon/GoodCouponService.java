package com.htyoudao.youdao.module.promotion.service.goodcoupon;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GoodCouponVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.AdvertisingStorePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.tiktokgoodcoupon.vo.TiktokCouponSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigReqDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigResDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import org.locationtech.proj4j.proj.GoodeProjection;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 优惠券 Service 接口
 *
 * @author 13149747939
 */
public interface GoodCouponService extends IService<GoodCouponDO> {

    /**
     * 按优惠券编码查询积分商品详情专用的完整优惠券信息。
     *
     * @param couponCode 优惠券编码
     * @return 优惠券完整信息
     */
    PointsProductCouponDetailVO selectPointsProductCouponByCode(String couponCode);

    /**
     * 分页查询优惠券
     * @param goodCoupon goodCoupon
     * @return GoodCouponPageRespVO
     */
    PageResult<GoodCouponPageRespVO> selectGoodCouponListPage(GoodCouponPageReqVO goodCoupon);

    /**
     * 创建优惠券
     * @param createReqVO createReqVO
     * @return Long
     */
    Long createCoupon(GoodCouponSaveReqVO createReqVO);

    /**
     * 更新优惠券
     * @param updateReqVO updateReqVO
     * @return Integer Integer
     */
    Integer updateCoupon(GoodCouponSaveReqVO updateReqVO);

    /**
     * 删除优惠券
     * @param id id
     */
    void deleteCoupon(Long id);

    /**
     * 根据id查询优惠券
     * @param id id
     * @return GoodCouponDO
     */
    GoodCouponRespVO getCouponById(Long id);

    /**
     * 根据id查询优惠券 app使用 需要开放鉴权
     * @param id id
     * @return GoodCouponDO
     */
    GoodCouponRespVO getAppCouponById(Long id);

    /**
     * 优惠券上下架
     * @param id id
     * @return GoodCouponDO
     */
    Integer updateIsGround(Long id);

    /**
     * 修改优惠券数量
     * @param couponNumUpdateReqVO couponNumUpdateReqVO
     * @return Boolean
     */
    Integer editCouponNum(CouponNumUpdateReqVO couponNumUpdateReqVO);

    /**
     * 优惠券复制
     * @param id id
     * @return return
     */
    Long copy(Long id);

    /**
     * 重新绑定商品
     * @param rebindCommodity rebindCommodity
     * @return Boolean
     */
    Integer rebindCommodity(RebindCommodityReqVO rebindCommodity);

    /**
     * 优惠券页面 优惠券的数据
     * @param id id
     * @return GoodCouponDateRespVO
     */
    GoodCouponDateRespVO selectDataById(Long id);

    /**
     * 优惠券页面 优惠券的数据 按照门店查询
     * @param goodCouponDateReqVO goodCouponDateReqVO
     * @return GoodCouponDateRespV2VO
     */
    GoodCouponDateRespV2VO selectDataByIdV2(GoodCouponDateReqVO goodCouponDateReqVO);

    /**
     * 新建推广
     * @param shareSaveReqVO shareSaveReqVO
     * @return Integer
     */
    Boolean addShare(ShareSaveReqVO shareSaveReqVO);

    /**
     * 推广详情
     * @param shareDetailReqVO shareDetailReqVO
     * @return CouponShareRespVO
     */
    CouponShareRespVO getShareDetail(ShareDetailReqVO shareDetailReqVO);

    /**
     * 积分商城优惠卷下拉列表
     * @return List<GoodCouponRespVO>
     */
    List<GoodCouponRespVO> getCouponListByIntegral();

    /**
     * 根据id查询优惠券
     * @param goodCouponUpdateWrapper goodCouponUpdateWrapper
     * @return GoodCouponDO
     */
    boolean updateByWrapper(UpdateWrapper<GoodCouponDO> goodCouponUpdateWrapper);

    /**
     * 根据优惠券code查询优惠券
     * @param couponCode couponCode
     * @return GoodCouponRespVO
     */
    GoodCouponRespVO getCouponByCode(String couponCode);

    /**
     * 单笔发放优惠卷
     * @param issueCouponReqVO issueCouponReqVO
     * @return Boolean
     */
    Boolean issueCoupon(IssueCouponReqVO issueCouponReqVO);

    /**
     * 下载用户模版
     * @param file file
     * @param num num
     * @param couponId couponId
     * @return Boolean
     */
    Boolean importCouponExl(MultipartFile file, int num, Long couponId);

    /**
     * 根据ids查询优惠券
     * @param couponIds couponIds
     * @return List<GoodCouponDO>
     */
    List<GoodCouponDO> getByIds(List<Long> couponIds);

    Map<Long, Long> getCouponReceiveCountByStore(List<Long> couponIds, List<Long> storeIds,
                                                 LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成微信跳转链接
     * @param wechatJump wechatJump
     * @return String
     */
    String generateUrlLink(WechatJumpParam wechatJump);

    List<GoodCouponRespVO> getCouponListByActivity();

    /**
     * 小程序优惠券图片 小
     * @return String
     */
    String getLittlePic();

    /**
     * 小程序优惠券图片 大
     * @return String
     */
    String getLargePic();

    List<GoodCouponDO> getListByCouponIds(List<Long> couponIds);


    PageResult<GoodCouponRespVO> goodCouponPage(Integer pageNum, Integer pageSize, String couponName, String remark);


    boolean updateBatch(List<GoodCouponDO> goodCoupons);

    void updateReceivedNumAndCouponNumById(int sendNum, Long goodCouponId);
    GoodCouponVO selectCouponByCode(String couponCode);

    GoodCouponRespVO goodCouponInfo(Long id);

    PageResult<GoodCouponRespVO> getGoodCouponPage(GoodCouponGetPageReqVO goodCouponPageReqVO);

    CommonResult<Boolean> updateGoodCoupon(GoodCouponCardVO cardVO);

    PageResult<GoodCouponPageRespVO> couponPageWithPoints(GoodCouponPageReqVO reqVO);

    List<StoreWecomConfigResDTO> selectStoreList(StoreWecomConfigReqDTO reqDTO);

    /**
     * 优惠券领取数量同步
     */
    void nocCouponStoreNum();

    /**
     * 根据会员等级 定时发放优惠券
     * @return Void
     */
    Void scheduledSend();

    /**
     * 判断优惠券是否在领取时间
     * @param id id
     * @return Boolean
     */
    Boolean judgmentTime(Long id);

    GoodCouponDataDTO getByIdCoupon(Long couponId);

    /**
     * 抖音券新增
     * @param reqVO reqVO
     * @return Integer
     */
    Integer saveTiktokGoodCoupon(TiktokCouponSaveReqVO reqVO);

    /**
     * 抖音券修改
     * @param reqVO reqVO
     * @return Integer
     */
    Integer updateTiktokGoodCoupon(TiktokCouponSaveReqVO reqVO);

    /**
     * 抖音门店列表
     * @param storePageReqVO storePageReqVO
     * @return PageResult<StorePageResVO>
     */
    PageResult<StorePageResVO> selectByStoreList(AdvertisingStorePageReqVO storePageReqVO);

    /**
     * 抖音券复制
     * @param id id
     * @return Long
     */
    Integer tiktokCopy(Long id);

    /**
     * 微信推消息
     * @param map map
     */
    void insertBatchByThread(Map<Long, List<UserCouponDO>> map);

    /**
     * 通过人群id获取优惠券数量
     * @param crowdId crowdId
     * @return Long
     */
    Long countByCrowdId(Long crowdId);

    /**
     * 批量更新所有H5
     * @return Integer
     */
    Integer updateAllH5();

    void memberDayCoupon();

    /**
     * 获取社区二维码
     * @param couponId couponId
     * @return List<String>
     */
    List<String> getCommunityQrImage(Long couponId);
}
