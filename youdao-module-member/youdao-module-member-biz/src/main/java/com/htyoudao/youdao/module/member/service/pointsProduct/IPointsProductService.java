package com.htyoudao.youdao.module.member.service.pointsProduct;




import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.*;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 积分商品Service接口
 * 
 * @author Qizhongnan
 * @date 2024-02-02
 */
public interface IPointsProductService extends IService<PointsProductDO>
{
    /**
     * 查询积分商品
     * 
     * @param productId 积分商品主键
     * @return 积分商品
     */
    public PointsProductDO selectPointsProductByProductId(Long productId);

    /**
     * 查询 PC 管理后台积分商品详情，并补充优惠券实时信息。
     *
     * @param productId 积分商品主键
     * @return PC 管理后台积分商品详情
     */
    PointsProductDO selectAdminPointsProductByProductId(Long productId);

    /**
     * 查询积分商品列表
     * 
     * @param
     * @return 积分商品集合
     */
    public PageResult<PointsProductDO> selectPointsProductListPage(long pageNum, long pageSize, PointsProductPageListReqVo pageListReqVo);

    /**
     * 新增积分商品
     * 
     * @param
     * @return 结果
     */
    public int insertPointsProduct(@Valid PointsProductSaveReqVo saveReqVo);

    /**
     * 修改积分商品
     * 
     * @param
     * @return 结果
     */
    public int updatePointsProduct(@Valid PointsProductEditReqVo editReqVo);

    /**
     * 修改积分商品上下架状态。
     */
    int updateAvailability(PointsProductAvailabilityReqVO reqVO);

    /**
     * 批量删除积分商品
     * 
     * @param productIds 需要删除的积分商品主键集合
     * @return 结果
     */
    public int deletePointsProductByProductIds(Long productIds);


    /**
     * 小程序请求积分商品列表
     * @return
     */
    List<PointsProductDTO> selectPointsProductListDTO(Integer productType);

    /**
     * 小程序查询积分商品详情
     * @return
     */
    PointsProductDetailDTO getpointProductDetail(PointsProductVO pointsProductVO);

    /**
     * 积分商城优惠券的数量
     * @param couponCode couponCode
     * @return Long
     */
    CommonResult<Long> getCountByCouponCode(String couponCode);
}
