package com.htyoudao.youdao.module.commodity.service.afterorder;

import java.util.*;

import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterOrderSimpleDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusUpdateReqVo;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderAppVO;
import com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo.AfterOrderReqVO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder.AfterOrderDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 订单生成后加购商品 Service 接口
 *
 * @author dht
 */
public interface AfterOrderService {

    /**
     * 创建订单生成后加购商品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAfterOrder(@Valid AfterOrderSaveReqVO createReqVO);

    /**
     * 更新订单生成后加购商品
     *
     * @param updateReqVO 更新信息
     */
    void updateAfterOrder(@Valid AfterOrderSaveReqVO updateReqVO);

    /**
     * 删除订单生成后加购商品
     *
     * @param afterId 编号
     */
    void deleteAfterOrder(Long afterId);

    /**
     * 获得订单生成后加购商品
     * @param afterId 主键
     * @return 订单生成后加购商品
     */
    AfterOrderDO getAfterOrder(Long afterId);

    /**
     * 获得订单生成后加购商品分页
     *
     * @param pageReqVO 分页查询
     * @return 订单生成后加购商品分页
     */
    PageResult<AfterOrderRespVO> getAfterOrderPage(AfterOrderPageReqVO pageReqVO);

    /**
     * 根据spuId更新商品图片
     * @param commoditySpurs commoditySpurs
     */
    void updateThumbnailUrlBySpuId(CommoditySpusUpdateReqVo updateReqVo);

    /**
     * 根据spuId更新商品图片
     */
    void updateEmptyThumbnailUrlBySpuId(CommoditySpusUpdateReqVo updateReqVo);

    /**
     * 根据spuId查询加购商品集合
     * @param commodityId commodityId
     * @return AfterOrderDO
     */
    List<AfterOrderDO> selectListBySpuId(Long commodityId);

    /**
     * 根据spuId查询加购商品集合
     * @param commodityIds commodityIds
     * @return AfterOrderDO
     */
    List<AfterOrderDO> selectListBySpuIds(List<Long> commodityIds);

    /**
     * 批量删除指定商品的加购配置
     *
     * @param commodityIds 商品 ID 集合
     */
    void deleteByCommodityIds(List<Long> commodityIds);

    /**
     * app 选完商品,提交订单之前获得加购商品集合
     * @param reqVO reqVO
     * @return AfterOrderAppVO
     */
    List<AfterOrderAppVO> getAfterOrderList(AfterOrderReqVO reqVO);

    /**
     * 根据加购商品id集合查询加购商品集合
     *
     * @param afterIds afterIds
     * @return AfterOrderDO
     */
    List<AfterInfoDTO> selectAfterListForRpc(Set<Long> afterIds, Long storeId);

    List<AfterOrderSimpleDTO> selectAfterOrderSimpleListForRpc();

    void updateAfterOrderName(@NotNull(message = "商品ID不能为空") Long commodityId, @NotEmpty(message = "商品名称不能为空") String commodityName);

    void updateAfterOrderNameAndImage(@NotNull(message = "商品ID不能为空") Long commodityId, @NotEmpty(message = "商品名称不能为空") String commodityName,@NotEmpty(message = "商品图片不能为空") String image);

    void deleteMore(AfterOrderDeleteMoreReq afterOrderDeleteMoreReq);
}
