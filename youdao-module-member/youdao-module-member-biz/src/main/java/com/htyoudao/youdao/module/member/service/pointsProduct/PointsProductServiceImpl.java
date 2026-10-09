package com.htyoudao.youdao.module.member.service.pointsProduct;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.*;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.CouponForProductsRespVo;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.ImageDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.coupon.CouponForProduct;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct.PointsProductDO;

import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsProduct.PointsProductMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UserCouponVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GoodCouponVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertListToStringS;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertStringToListS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.member.api.enums.LogRecordConstants.*;
import static com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants.*;


/**
 * 积分商品Service业务层处理
 *
 * @author Qizhongnan
 * @date 2024-02-02
 */
@Service
@Slf4j
public class PointsProductServiceImpl extends ServiceImpl<PointsProductMapper, PointsProductDO> implements IPointsProductService {


    @Resource
    private PointsProductMapper pointsProductMapper;

    @Resource
    private WxMemberMapper wxMemberMapper;

    @DubboReference
    private UserCouponApi userCouponApi;
    /**
     * 查询积分商品
     *
     * @param productId 积分商品主键
     * @return 积分商品
     */
    @Override
    public PointsProductDO selectPointsProductByProductId(Long productId) {
        LambdaQueryWrapper<PointsProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsProductDO::getProductId,productId);
        PointsProductDO product = pointsProductMapper.selectOne(wrapper);
        if (Objects.nonNull(product)) {
            fillProductAttachments(product, true);
            if (product.getProductHeaderImage() != null && !product.getProductHeaderImage().isEmpty()) {
                List<String> productHeaderImageList = new ArrayList<>(convertStringToListS(product.getProductHeaderImage()));
                product.setProductHeaderImageList(productHeaderImageList);
            }
            if (product.getProductDetailImages() != null && !product.getProductDetailImages().isEmpty()) {
                List<String> productDetailImageList = new ArrayList<>(convertStringToListS(product.getProductDetailImages()));
                product.setProductDetailImageList(productDetailImageList);
            }

            if (product.getProductThumbnail() != null && !product.getProductThumbnail().isEmpty()) {
                List<String> productThumbnailList = new ArrayList<>(convertStringToListS(product.getProductThumbnail()));
                product.setProductThumbnailList(productThumbnailList);
            }

            CouponForProduct couponForProduct = new CouponForProduct();
            if (product.getCouponCode() != null) {
                couponForProduct.setCouponTypeName(product.getCouponTypeName());
                couponForProduct.setCouponCode(product.getCouponCode());
                couponForProduct.setCouponImageUrl(product.getCouponImageUrl());
                couponForProduct.setCouponName(product.getCouponName());
            }
            product.setCouponForProduct(couponForProduct);
        }
        return product;
    }

    /**
     * 查询 PC 管理后台积分商品详情，并按 couponCode 补充优惠券实时完整信息。
     * 原商品查询方法保持不变，避免小程序详情重复调用优惠券服务。
     */
    @Override
    public PointsProductDO selectAdminPointsProductByProductId(Long productId) {
        PointsProductDO product = selectPointsProductByProductId(productId);
        if (product == null || !Objects.equals(product.getProductType(), 1)
                || !StringUtils.hasText(product.getCouponCode())) {
            return product;
        }
        GoodCouponVO couponInfo = loadCouponInfo(product.getCouponCode());
        if (couponInfo == null) {
            return product;
        }
        CouponForProduct couponForProduct = Optional.ofNullable(product.getCouponForProduct())
                .orElseGet(CouponForProduct::new);
        couponForProduct.setCouponCode(couponInfo.getCouponCode());
        couponForProduct.setCouponName(couponInfo.getCouponName());
        couponForProduct.setCouponImageUrl(couponInfo.getCouponImageUrl());
        couponForProduct.setCouponTypeName(couponTypeName(couponInfo.getCouponType()));
        couponForProduct.setCouponInfo(couponInfo);
        product.setCouponForProduct(couponForProduct);
        return product;
    }

    /**
     * 查询积分商品列表
     *
     * @param
     * @return 积分商品
     */
    @Override
    public PageResult<PointsProductDO> selectPointsProductListPage(long pageNum, long pageSize, PointsProductPageListReqVo pageListReqVo) {
        Long businessId = BusinessContextHolder.getBusinessId();

        LambdaQueryWrapper<PointsProductDO> queryWrapper = new LambdaQueryWrapper<PointsProductDO>();
//        queryWrapper.orderByAsc(PointsProduct::getProductSort);
        queryWrapper.orderByDesc(PointsProductDO::getCreateTime);
        queryWrapper.eq(PointsProductDO::getBusinessId,businessId);
        if(!StringUtils.isEmpty(pageListReqVo.getProductName())){
            queryWrapper.like(PointsProductDO::getProductName, pageListReqVo.getProductName());
        }
        if(!StringUtils.isEmpty(pageListReqVo.getProductType())){
            queryWrapper.eq(PointsProductDO::getProductType, pageListReqVo.getProductType());
        }
        if (!StringUtils.isEmpty(pageListReqVo.getIsAvailable())) {
            queryWrapper.eq(PointsProductDO::getIsAvailable, pageListReqVo.getIsAvailable());
        }

        Page<PointsProductDO> page = new Page<>(pageNum,pageSize);
//        List<PointsProduct> pointsProducts = pointsProductMapper.selectList(null);
        Page<PointsProductDO> list = pointsProductMapper.selectPage(page, queryWrapper);
        for (PointsProductDO product : list.getRecords()) {
            fillProductAttachments(product, false);
            if (product.getProductHeaderImage() != null && !product.getProductHeaderImage().isEmpty()) {
                List<String> productHeaderImageList = new ArrayList<>(convertStringToListS(product.getProductHeaderImage()));
                product.setProductHeaderImageList(productHeaderImageList);
            }
            // 后台列表不返回商品详情附件，详情接口按需组装。
            product.setProductDetailImages(null);
            product.setProductDetailImageList(null);
            product.setProductDetailAttachments(null);
            if (product.getProductThumbnail() != null && !product.getProductThumbnail().isEmpty()) {
                List<String> productThumbnailList = new ArrayList<>(convertStringToListS(product.getProductThumbnail()));
                product.setProductThumbnailList(productThumbnailList);
            }
            CouponForProduct couponForProduct = new CouponForProduct();
            if (product.getCouponCode() != null) {
                couponForProduct.setCouponTypeName(product.getCouponTypeName());
                couponForProduct.setCouponCode(product.getCouponCode());
                couponForProduct.setCouponImageUrl(product.getCouponImageUrl());
                couponForProduct.setCouponName(product.getCouponName());
            }

            product.setCouponForProduct(couponForProduct);
        }
        PageResult<PointsProductDO> pageResult = new PageResult();
        pageResult.setList(list.getRecords());
        pageResult.setTotal(list.getTotal());
        return pageResult;
    }


    /**
     * 新增积分商品
     *
     * @param
     * @return 结果
     */
    @Override
    @LogRecord(type = MEMBER_POINTSPRODUCT_TYPE, subType = MEMBER_POINTSPRODUCT_CREATE_TYPE, bizNo = "{{1}}", success = MEMBER_POINTSPRODUCT_CREATE_SUCCESS)
    public int insertPointsProduct(PointsProductSaveReqVo saveReqVo) {

        LogRecordContext.putVariable("pointsProduct", saveReqVo);
        if (saveReqVo.getIsAvailable() == null) {
            saveReqVo.setIsAvailable(1);
        }
        if (saveReqVo.getProductHeaderImageList() != null && !saveReqVo.getProductHeaderImageList().isEmpty()) {
            saveReqVo.setProductHeaderImage(convertListToStringS(saveReqVo.getProductHeaderImageList()));
        }
        if (saveReqVo.getProductDetailImageList() != null && !saveReqVo.getProductDetailImageList().isEmpty()) {
            saveReqVo.setProductDetailImages(convertListToStringS(saveReqVo.getProductDetailImageList()));
        }
        if(StringUtils.isEmpty(saveReqVo.getProductName())){
            throw exception(POINTS_PRODUCT_NOT_NAME);
        }

        if(StringUtils.isEmpty(saveReqVo.getProductSort())){
            throw exception(POINTS_PRODUCT_NOT_S);
        }

        if(StringUtils.isEmpty(saveReqVo.getProductType())){
            throw exception(POINTS_PRODUCT_NOT_F);
        }


        if(StringUtils.isEmpty(saveReqVo.getProductPrice())){
            throw exception(POINTS_PRODUCT_NOT_J);
        }


        if(StringUtils.isEmpty(saveReqVo.getProductInventory())){
            throw exception(POINTS_PRODUCT_NOT_K);
        }

        if(saveReqVo.getProductPrice()<0){
            throw exception(POINTS_PRODUCT_NOT);
        }

        if(saveReqVo.getProductPrice()==0){
            throw exception(POINTS_PRODUCT_NOT_LING);
        }

        if(saveReqVo.getProductSort()<0){
            throw exception(POINTS_PRODUCT_NOT_SORT);
        }

        if(saveReqVo.getProductInventory()<0){
            throw exception(POINTS_PRODUCT_NOT_SORT);
        }

        LambdaQueryWrapper<PointsProductDO> productDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        productDOLambdaQueryWrapper.eq(PointsProductDO::getProductName,saveReqVo.getProductName());
        List<PointsProductDO> pointsProductDOS = pointsProductMapper.selectList(productDOLambdaQueryWrapper);
        if(ObjectUtil.isNotEmpty(pointsProductDOS)){
            throw exception(POINTS_PRODUCT_ERROR_NAME);
        }
        if (saveReqVo.getProductThumbnailList() != null && !saveReqVo.getProductThumbnailList().isEmpty()) {
            saveReqVo.setProductThumbnail(convertListToStringS(saveReqVo.getProductThumbnailList()));
        }
        CouponForProduct selectedCoupon = saveReqVo.getCouponForProduct();
        String couponCode = selectedCoupon != null && StringUtils.hasText(selectedCoupon.getCouponCode())
                ? selectedCoupon.getCouponCode() : saveReqVo.getCouponCode();
        GoodCouponVO couponInfo = loadCouponInfo(couponCode);
        if (couponInfo != null) {
            fillCouponDisplayInfo(saveReqVo, couponInfo);
        } else if (selectedCoupon != null && StringUtils.hasText(selectedCoupon.getCouponCode())) {
            saveReqVo.setCouponName(selectedCoupon.getCouponName());
            saveReqVo.setCouponTypeName(selectedCoupon.getCouponTypeName());
            saveReqVo.setCouponCode(selectedCoupon.getCouponCode());
            saveReqVo.setCouponImageUrl(selectedCoupon.getCouponImageUrl());
        }
        saveReqVo.setCreateTime(new DateTime());
        saveReqVo.setProductStatus(2);
        PointsProductDO pointsProductDO = new PointsProductDO();
        BeanUtils.copyProperties(saveReqVo, pointsProductDO);
        writeAttachments(pointsProductDO, saveReqVo.getProductHeaderAttachments(),
                saveReqVo.getProductDetailAttachments(), true);
        pointsProductDO.setDeleted(false);
        pointsProductMapper.insert(pointsProductDO);
        return 1;
    }

    /**
     * 修改积分商品
     *
     * @param
     * @return 结果
     */
    @Override
    @LogRecord(type = MEMBER_POINTSPRODUCT_TYPE, subType = MEMBER_POINTSPRODUCT_UPDATE_TYPE, bizNo = "{{#pointsProduct.productId}}", success = MEMBER_POINTSPRODUCT_UPDATE_SUCCESS)
    public int updatePointsProduct(PointsProductEditReqVo editReqVo) {

        if(StringUtils.isEmpty(editReqVo.getProductName())){
            throw exception(POINTS_PRODUCT_NOT_NAME);
        }

        if(StringUtils.isEmpty(editReqVo.getProductSort())){
            throw exception(POINTS_PRODUCT_NOT_S);
        }

        if(StringUtils.isEmpty(editReqVo.getProductType())){
            throw exception(POINTS_PRODUCT_NOT_F);
        }

        if(StringUtils.isEmpty(editReqVo.getProductPrice())){
            throw exception(POINTS_PRODUCT_NOT_J);
        }

        if(StringUtils.isEmpty(editReqVo.getProductInventory())){
            throw exception(POINTS_PRODUCT_NOT_K);
        }

        if(editReqVo.getProductPrice()<0){
            throw exception(POINTS_PRODUCT_NOT);
        }

        if(editReqVo.getProductPrice()==0){
            throw exception(POINTS_PRODUCT_NOT_LING);
        }

        if(editReqVo.getProductSort()<0){
            throw exception(POINTS_PRODUCT_NOT_SORT);
        }

        if(editReqVo.getProductInventory()<0){
            throw exception(POINTS_PRODUCT_NOT_SORT);
        }
        LogRecordContext.putVariable("pointsProduct", editReqVo);
        if (editReqVo.getProductHeaderImageList() != null && !editReqVo.getProductHeaderImageList().isEmpty()) {
            editReqVo.setProductHeaderImage(convertListToStringS(editReqVo.getProductHeaderImageList()));
        }
        if (editReqVo.getProductDetailImageList() != null && !editReqVo.getProductDetailImageList().isEmpty()) {
            editReqVo.setProductDetailImages(convertListToStringS(editReqVo.getProductDetailImageList()));
        }
        if (editReqVo.getProductThumbnailList() != null && !editReqVo.getProductThumbnailList().isEmpty()) {
            editReqVo.setProductThumbnail(convertListToStringS(editReqVo.getProductThumbnailList()));
        }
        CouponForProduct selectedCoupon = editReqVo.getCouponForProduct();
        String couponCode = selectedCoupon != null && StringUtils.hasText(selectedCoupon.getCouponCode())
                ? selectedCoupon.getCouponCode() : editReqVo.getCouponCode();
        GoodCouponVO couponInfo = loadCouponInfo(couponCode);
        if (couponInfo != null) {
            fillCouponDisplayInfo(editReqVo, couponInfo);
        } else if (selectedCoupon != null && StringUtils.hasText(selectedCoupon.getCouponCode())) {
            editReqVo.setCouponName(selectedCoupon.getCouponName());
            editReqVo.setCouponTypeName(selectedCoupon.getCouponTypeName());
            editReqVo.setCouponCode(selectedCoupon.getCouponCode());
            editReqVo.setCouponImageUrl(selectedCoupon.getCouponImageUrl());
        }
        PointsProductDO pointsProductDO = new PointsProductDO();
        BeanUtils.copyProperties(editReqVo, pointsProductDO);
        writeAttachments(pointsProductDO, editReqVo.getProductHeaderAttachments(),
                editReqVo.getProductDetailAttachments(), false);

        return pointsProductMapper.updatePointsProduct(pointsProductDO);
    }

    /**
     * 独立修改积分商品上下架状态，不影响商品售罄状态和其他商品信息。
     */
    @Override
    public int updateAvailability(PointsProductAvailabilityReqVO reqVO) {
        Long businessId = BusinessContextHolder.getBusinessId();
        return pointsProductMapper.update(null, new LambdaUpdateWrapper<PointsProductDO>()
                .eq(PointsProductDO::getProductId, reqVO.getProductId())
                .eq(PointsProductDO::getBusinessId, businessId)
                .eq(PointsProductDO::getDeleted, false)
                .set(PointsProductDO::getIsAvailable, reqVO.getIsAvailable()));
    }

    @Override
    @LogRecord(type = MEMBER_POINTSPRODUCT_TYPE, subType = MEMBER_POINTSPRODUCT_DELETE_TYPE, bizNo = "{{#pointsProduct.productId}}", success = MEMBER_POINTSPRODUCT_DELETE_SUCCESS)
    public int deletePointsProductByProductIds(Long productIds) {

        LambdaQueryWrapper<PointsProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsProductDO::getProductId,productIds);
        PointsProductDO pointsProductDO = pointsProductMapper.selectOne(wrapper);
        LogRecordContext.putVariable("pointsProduct", pointsProductDO);
        LambdaQueryWrapper<PointsProductDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PointsProductDO::getProductId,productIds);
        int delete = pointsProductMapper.delete(lambdaQueryWrapper);
        return delete;
    }

    /**
     * 小程序端查询积分商品列表
     *
     * @return
     */
    @Override
    public List<PointsProductDTO> selectPointsProductListDTO(Integer productType) {
        //新建一个返回给小程序端的积分商品 list
        List<PointsProductDTO> pointsProductDTOS = new ArrayList<>();
        //新建一个空的积分商品用来查询商品 list
        PointsProductDO pointsProductDO = new PointsProductDO();
        pointsProductDO.setIsAvailable(1);//小程序上架
        pointsProductDO.setProductType(productType);
        List<PointsProductDO> pointsProductDOS = selectPointsProductList(pointsProductDO);
        for (PointsProductDO product : pointsProductDOS) {
            PointsProductDTO pointsProductDTO = new PointsProductDTO();
            pointsProductDTO.setProductId(product.getProductId());
            // 新商品读取独立封面；历史商品封面为空时兼容第一张旧头图。
            pointsProductDTO.setProductThumbnail(PointsProductAttachmentUtils.resolveThumbnail(
                    product.getProductThumbnail(), product.getProductHeaderAttachments()));
            pointsProductDTO.setProductThumbnailType(2);
            pointsProductDTO.setProductName(product.getProductName());
            pointsProductDTO.setProductPrice(product.getProductPrice());
            pointsProductDTO.setProductStatus(product.getProductStatus());
            pointsProductDTO.setProductType(product.getProductType());
            pointsProductDTO.setProductSort(product.getProductSort());
            pointsProductDTO.setProductInventory(Math.max(0,
                    Optional.ofNullable(product.getProductInventory()).orElse(0)));

            CouponForProductsRespVo couponForProduct = new CouponForProductsRespVo();
            if (Objects.equals(product.getProductType(), 1)) {
                couponForProduct.setCouponTypeName(product.getCouponTypeName());
                couponForProduct.setCouponCode(product.getCouponCode());
                couponForProduct.setCouponImageUrl(product.getCouponImageUrl());
                couponForProduct.setCouponName(product.getCouponName());
                pointsProductDTO.setCouponForProduct(couponForProduct);
            }
            pointsProductDTOS.add(pointsProductDTO);
        }
        return pointsProductDTOS;
    }

    /**
     * 导出积分商品列表
     *
     * @param pointsProductDO 积分商品
     * @return 积分商品
     */

    public List<PointsProductDO> selectPointsProductList(PointsProductDO pointsProductDO) {
        Long businessId = BusinessContextHolder.getBusinessId();
        pointsProductDO.setBusinessId(businessId);
        List<PointsProductDO> pointsProductDOS = pointsProductMapper.selectPointsProductList(pointsProductDO);
        for (PointsProductDO product : pointsProductDOS) {
            fillProductAttachments(product, true);
            if (product.getProductHeaderImage() != null && !product.getProductHeaderImage().isEmpty()) {
                List<String> productHeaderImageList = new ArrayList<>(convertStringToListS(product.getProductHeaderImage()));
                product.setProductHeaderImageList(productHeaderImageList);
            }
            if (product.getProductDetailImages() != null && !product.getProductDetailImages().isEmpty()) {
                List<String> productDetailImageList = new ArrayList<>(convertStringToListS(product.getProductDetailImages()));
                product.setProductDetailImageList(productDetailImageList);
            }
            if (product.getProductThumbnail() != null && !product.getProductThumbnail().isEmpty()) {
                List<String> productThumbnailList = new ArrayList<>(convertStringToListS(product.getProductThumbnail()));
                product.setProductThumbnailList(productThumbnailList);
            }
            CouponForProduct couponForProduct = new CouponForProduct();
            if (product.getCouponCode() != null) {
                couponForProduct.setCouponTypeName(product.getCouponTypeName());
                couponForProduct.setCouponCode(product.getCouponCode());
                couponForProduct.setCouponImageUrl(product.getCouponImageUrl());
                couponForProduct.setCouponName(product.getCouponName());
            }

            product.setCouponForProduct(couponForProduct);
        }
        return pointsProductDOS;
    }




    /**
     * 小程序查询积分商品详情
     *
     * @param pointsProductVO
     * @return
     */

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public PointsProductDetailDTO getpointProductDetail(PointsProductVO pointsProductVO) {

        PointsProductDetailDTO pointsProductDetailDTO = new PointsProductDetailDTO();
        if(ObjectUtil.isEmpty(pointsProductVO.getProductId())){
            throw exception(POINTS_PRODUCT_ID_NOT_NULL);
        }
        PointsProductDO pointsProductDO = selectPointsProductByProductId(pointsProductVO.getProductId());
        if (pointsProductDO == null) {
            return null;
        }
        WxMemberDO wxMember =new WxMemberDO();
        wxMember.setMemberId(pointsProductVO.getMemberId());
        //获取到当前订单的用户 id
        makeShardingValueForWx(wxMember);
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<WxMemberDO>();
        queryWrapper.eq("member_id", wxMember.getMemberId());
        queryWrapper.eq("sharding_value",wxMember.getShardingValue());
        //用用户 id 查询到当前用户的对象
        wxMember = wxMemberMapper.selectOne(queryWrapper);
        //根据ID 查询是否领取过改卷
        if (ObjectUtil.isNotEmpty(wxMember)) {
            List<UserCouponVO> userCouponVOS = userCouponApi.selectCouponData(wxMember.getMemberId());
            if (ObjectUtil.isNotEmpty(userCouponVOS)) {
                boolean received = StringUtils.hasText(pointsProductDO.getCouponCode())
                        && userCouponVOS.stream().anyMatch(userCoupon ->
                        Objects.equals(pointsProductDO.getCouponCode(), userCoupon.getCouponCode()));
                pointsProductDetailDTO.setIsReceive(received);

            }
        }
        pointsProductDetailDTO.setProductId(pointsProductDO.getProductId());
        // 详情与列表使用相同封面兼容规则。
        pointsProductDetailDTO.setProductThumbnail(PointsProductAttachmentUtils.resolveThumbnail(
                pointsProductDO.getProductThumbnail(), pointsProductDO.getProductHeaderAttachments()));
        pointsProductDetailDTO.setProductName(pointsProductDO.getProductName());
        pointsProductDetailDTO.setProductType(pointsProductDO.getProductType());
        pointsProductDetailDTO.setProductPrice(pointsProductDO.getProductPrice());
        pointsProductDetailDTO.setProductDescription(pointsProductDO.getProductDescription());
        pointsProductDetailDTO.setProductStatus(pointsProductDO.getProductStatus());
        pointsProductDetailDTO.setProductHeaderAttachments(pointsProductDO.getProductHeaderAttachments());
        pointsProductDetailDTO.setProductDetailAttachments(pointsProductDO.getProductDetailAttachments());
        List<ImageDTO> productDetailImagelist = new ArrayList<>();
        if (pointsProductDO.getProductDetailAttachments() != null) {
            for (PointsProductAttachmentVO productDetailImage : pointsProductDO.getProductDetailAttachments()) {
                if (productDetailImage == null) {
                    continue;
                }
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setUrl(productDetailImage.getUrl());
                imageDTO.setType(productDetailImage.getType());
                productDetailImagelist.add(imageDTO);
            }
        }
        pointsProductDetailDTO.setProductDetailImagelist(productDetailImagelist);
        List<ImageDTO> productHeaderImagelist = new ArrayList<>();
        if (pointsProductDO.getProductHeaderAttachments() != null) {
            for (PointsProductAttachmentVO productHeaderImage : pointsProductDO.getProductHeaderAttachments()) {
                if (productHeaderImage == null) {
                    continue;
                }
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setUrl(productHeaderImage.getUrl());
                imageDTO.setType(productHeaderImage.getType());
                productHeaderImagelist.add(imageDTO);
            }
        }
        CouponForProductsRespVo couponForProduct = new CouponForProductsRespVo();
        if (Objects.equals(pointsProductDO.getProductType(), 1)) {
            PointsProductCouponDetailVO couponInfo =
                    loadPointsProductCouponDetail(pointsProductDO.getCouponCode());
            couponForProduct.setCouponCode(couponInfo != null ? couponInfo.getCouponCode() : pointsProductDO.getCouponCode());
            couponForProduct.setCouponName(couponInfo != null ? couponInfo.getCouponName() : pointsProductDO.getCouponName());
            couponForProduct.setCouponImageUrl(couponInfo != null
                    ? couponInfo.getCouponImageUrl() : pointsProductDO.getCouponImageUrl());
            couponForProduct.setCouponTypeName(couponInfo != null
                    ? pointsProductCouponTypeName(couponInfo.getCouponType()) : pointsProductDO.getCouponTypeName());
            couponForProduct.setCouponInfo(couponInfo);
        }
        pointsProductDetailDTO.setCouponForProduct(couponForProduct);

        pointsProductDetailDTO.setProductHeaderImagelist(productHeaderImagelist);
        pointsProductDetailDTO.setProductInventory(
                Optional.ofNullable(pointsProductDO.getProductInventory()).orElse(0));
        return pointsProductDetailDTO;
    }

    /**
     * 查询积分商品详情专用的完整优惠券信息。
     *
     * <p>该方法只供小程序积分商品详情调用，不改变原优惠券查询及兑换逻辑。</p>
     */
    private PointsProductCouponDetailVO loadPointsProductCouponDetail(String couponCode) {
        if (!StringUtils.hasText(couponCode)) {
            log.warn("积分商品详情未配置优惠券编码");
            return null;
        }
        try {
            PointsProductCouponDetailVO couponInfo =
                    userCouponApi.selectPointsProductCouponByCode(couponCode.trim());
            if (couponInfo == null) {
                log.warn("积分商品详情未获取到完整优惠券信息，couponCode={}", couponCode);
            }
            return couponInfo;
        } catch (RuntimeException exception) {
            log.warn("积分商品详情查询完整优惠券信息失败，couponCode={}", couponCode, exception);
            return null;
        }
    }

    /**
     * 将积分商品详情优惠券类型转换为展示文案。
     */
    private String pointsProductCouponTypeName(Integer couponType) {
        return couponTypeName(couponType == null ? null : couponType.toString());
    }

    /**
     * 按积分商品保存的 couponCode 查询优惠券实时数据。
     */
    private GoodCouponVO loadCouponInfo(String couponCode) {
        try {
            if (StringUtils.hasText(couponCode)) {
                return userCouponApi.selectCouponByCode(couponCode);
            }
        } catch (RuntimeException exception) {
            log.warn("积分商品按couponCode查询优惠券信息失败，couponCode={}", couponCode, exception);
        }
        return null;
    }

    /**
     * 新增商品时使用优惠券主数据填充展示字段。
     */
    private void fillCouponDisplayInfo(PointsProductSaveReqVo request, GoodCouponVO couponInfo) {
        request.setCouponCode(couponInfo.getCouponCode());
        request.setCouponName(couponInfo.getCouponName());
        request.setCouponImageUrl(couponInfo.getCouponImageUrl());
        request.setCouponTypeName(couponTypeName(couponInfo.getCouponType()));
    }

    /**
     * 修改商品时使用优惠券主数据填充展示字段。
     */
    private void fillCouponDisplayInfo(PointsProductEditReqVo request, GoodCouponVO couponInfo) {
        request.setCouponCode(couponInfo.getCouponCode());
        request.setCouponName(couponInfo.getCouponName());
        request.setCouponImageUrl(couponInfo.getCouponImageUrl());
        request.setCouponTypeName(couponTypeName(couponInfo.getCouponType()));
    }

    /**
     * 将优惠券类型编码转换为小程序展示文案。
     */
    private String couponTypeName(String couponType) {
        if ("0".equals(couponType)) {
            return "满减券";
        }
        if ("1".equals(couponType)) {
            return "折扣券";
        }
        return couponType;
    }

    /**
     * 将附件写入新 JSON 字段，并同步维护历史逗号分隔 URL 字段。
     */
    private void writeAttachments(PointsProductDO product, List<PointsProductAttachmentVO> headerAttachments,
                                  List<PointsProductAttachmentVO> detailAttachments, boolean createMode) {
        if (createMode || headerAttachments != null || StringUtils.hasText(product.getProductHeaderImage())) {
            List<PointsProductAttachmentVO> headers = headerAttachments;
            if (headers == null && StringUtils.hasText(product.getProductHeaderImage())) {
                headers = PointsProductAttachmentUtils.parseAttachments(null, product.getProductHeaderImage());
            }
            headers = Optional.ofNullable(headers).orElseGet(Collections::emptyList);
            product.setProductHeaderAttachmentsJson(PointsProductAttachmentUtils.toJson(headers));
            product.setProductHeaderImage(PointsProductAttachmentUtils.toLegacyUrls(headers));
        }
        if (createMode || detailAttachments != null || StringUtils.hasText(product.getProductDetailImages())) {
            List<PointsProductAttachmentVO> details = detailAttachments;
            if (details == null && StringUtils.hasText(product.getProductDetailImages())) {
                details = PointsProductAttachmentUtils.parseAttachments(null, product.getProductDetailImages());
            }
            details = Optional.ofNullable(details).orElseGet(Collections::emptyList);
            product.setProductDetailAttachmentsJson(PointsProductAttachmentUtils.toJson(details));
            product.setProductDetailImages(PointsProductAttachmentUtils.toLegacyUrls(details));
        }
    }

    /**
     * 组装积分商品附件。后台列表只需要头部附件，详情场景同时组装详情附件。
     */
    private void fillProductAttachments(PointsProductDO product, boolean includeDetail) {
        product.setProductHeaderAttachments(PointsProductAttachmentUtils.parseAttachments(
                product.getProductHeaderAttachmentsJson(), product.getProductHeaderImage()));
        if (includeDetail) {
            product.setProductDetailAttachments(PointsProductAttachmentUtils.parseAttachments(
                    product.getProductDetailAttachmentsJson(), product.getProductDetailImages()));
        }
    }

    public void makeShardingValueForWx(WxMemberDO wxMember) {

        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
        }

    }

    @Override
    public CommonResult<Long> getCountByCouponCode(String couponCode) {
        QueryWrapper<PointsProductDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PointsProductDO::getIsAvailable, 1);
        queryWrapper.lambda().eq(PointsProductDO::getCouponCode, couponCode);
        return CommonResult.success(pointsProductMapper.selectCount(queryWrapper));
    }
}
