package com.htyoudao.youdao.module.member.dal.mysql.pointsProduct;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 积分商品Mapper接口
 *
 */
@Mapper
public interface PointsProductMapper extends BaseMapper<PointsProductDO> {

    /**
     * 修改积分商品
     *
     * @param pointsProductDO 积分商品
     * @return 结果
     */
    public int updatePointsProduct(PointsProductDO pointsProductDO);

    /**
     * 删除积分商品
     *
     * @param productId 积分商品主键
     * @return 结果
     */
    public int deletePointsProductByProductId(Long productId);

    /**
     * 查询积分商品列表
     *
     * @param pointsProductDO 积分商品
     * @return 积分商品集合
     */
    public List<PointsProductDO> selectPointsProductList(PointsProductDO pointsProductDO);
    public int deletePointsProductByProductIds(Long[] productIds);


}
