package com.htyoudao.youdao.module.member.service.wxmembercard;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardEditReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardPageRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.*;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;

import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertStringToListS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_ERROR;

@Service
public class WxMemberCardServiceImpl extends ServiceImpl<WxMemberCardMapper, WxMemberCardDO> implements IWxMemberCardService {


    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @Resource
    private WxMemberMapper wxMemberMapper;
    @DubboReference
    private GoodCouponApi goodCouponApi;

    @Override
    public PageResult<WxMemberCardPageRespVO> listPage(long pageNo, long pageSize) {
        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<WxMemberCardDO>();
        queryWrapper.orderByDesc("member_level");
        Page<WxMemberCardDO> page = new Page<>(pageNo,pageSize);
        Page<WxMemberCardDO> doPage = wxMemberCardMapper.selectPage(page, queryWrapper);

        PageResult<WxMemberCardPageRespVO> pageResult = new PageResult<WxMemberCardPageRespVO>();
        List<WxMemberCardPageRespVO> wxMemberCardPageRespVOS = BeanCopyUtils.copyBeanList(doPage.getRecords(), WxMemberCardPageRespVO.class);

        if(ObjectUtil.isNotEmpty(wxMemberCardPageRespVOS)){
            for (WxMemberCardPageRespVO wxMemberCardPageRespVO : wxMemberCardPageRespVOS) {
                List<String> stringList = new ArrayList<>();

                if(ObjectUtil.isNotEmpty(wxMemberCardPageRespVO.getBackgroundImage())){
                    stringList.add(wxMemberCardPageRespVO.getBackgroundImage());
                }

                wxMemberCardPageRespVO.setBackgroundImageList(stringList);
            }
        }
        pageResult.setList(wxMemberCardPageRespVOS);
        pageResult.setTotal(doPage.getTotal());
        return pageResult;
    }

    @Override
    public boolean edit(WxMemberCardEditReqVO wxMemberCard) {
        if(!StringUtils.isEmpty(wxMemberCard.getCouponCode())){
            GoodCouponCardVO goodCouponCardVO = new GoodCouponCardVO();
            goodCouponCardVO.setCouponCode(wxMemberCard.getCouponCode());
            goodCouponCardVO.setMemberLevel(wxMemberCard.getMemberLevel());
            goodCouponApi.updateGoodCoupon(goodCouponCardVO);
        }
        LambdaQueryWrapper<WxMemberCardDO>  queryWrapper = new LambdaQueryWrapper<WxMemberCardDO> ();
        queryWrapper.eq(WxMemberCardDO::getName, wxMemberCard.getName());
        queryWrapper.ne(WxMemberCardDO::getMemberCardId, wxMemberCard.getMemberCardId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw exception(WX_MEMBER_CARD_ERROR);
        }
        WxMemberCardDO wxMemberCardDO = new WxMemberCardDO();
        BeanUtils.copyProperties(wxMemberCard,wxMemberCardDO);

        return this.updateById(wxMemberCardDO);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public WxMemberAndCardRespVO getMemberCardWithMemberId(WxMemberAndCardReqVO reqVO) {
        WxMemberDO wxMemberDO = new WxMemberDO();
        //获取查询的用户 id
        Long memberId = reqVO.getMemberId();
        long l = memberId % 10;
        QueryWrapper<WxMemberDO> memberQuery = new QueryWrapper<>();
        memberQuery.eq("member_id", memberId);
        memberQuery.eq("sharding_value", l);
        wxMemberDO = wxMemberMapper.selectOne(memberQuery);
        //新建一个用户vipcard
        WxMemberAndCardRespVO wxMemberVO = new WxMemberAndCardRespVO();
        //WxMember one = wxMemberMapper.selectOneById(memberId);
        if (ObjectUtil.isEmpty(wxMemberDO)) {

            WxMemberCardDO wxMemberCardDO = new WxMemberCardDO();
            WxMemberCardDataRespVO wxMemberCardDataRespVO = new WxMemberCardDataRespVO();
            QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<WxMemberCardDO>();
            queryWrapper.eq("card_status", 1);
            queryWrapper.eq("member_level", 1);
            wxMemberCardDO = wxMemberCardMapper.selectOne(queryWrapper);
            if(wxMemberCardDO!=null){
                com.htyoudao.youdao.framework.common.util.object.BeanUtils.copyProperties(wxMemberCardDO,wxMemberCardDataRespVO);
            }
            if(wxMemberCardDataRespVO!=null){
                //详情图
                if (wxMemberCardDataRespVO.getDetailImage() != null && !wxMemberCardDataRespVO.getDetailImage().isEmpty()) {
                    List<String> detailImageList = new ArrayList<>(convertStringToListS(wxMemberCardDataRespVO.getDetailImage()));
                    wxMemberCardDataRespVO.setDetailImageList(detailImageList);
                }
                //优惠卷编码
                if (wxMemberCardDataRespVO.getCouponCode() != null && !wxMemberCardDataRespVO.getCouponCode().isEmpty()) {
                    List<String> couponCodeList = new ArrayList<>(convertStringToListS(wxMemberCardDataRespVO.getCouponCode()));
                    wxMemberCardDataRespVO.setCouponCodeList(couponCodeList);
                }
                //缩略图
                if (wxMemberCardDataRespVO.getThumbnailImage() != null && !wxMemberCardDataRespVO.getThumbnailImage().isEmpty()) {
                    List<String> thumbnailImageList = new ArrayList<>(convertStringToListS(wxMemberCardDataRespVO.getThumbnailImage()));
                    wxMemberCardDataRespVO.setThumbnailImageList(thumbnailImageList);
                }
                //背景图
                if (wxMemberCardDataRespVO.getBackgroundImage() != null && !wxMemberCardDataRespVO.getBackgroundImage().isEmpty()) {
                    List<String> backgroundImageList = new ArrayList<>(convertStringToListS(wxMemberCardDataRespVO.getBackgroundImage()));
                    wxMemberCardDataRespVO.setBackgroundImageList(backgroundImageList);
                }
                //小标图
                if (wxMemberCardDataRespVO.getIconImage() != null && !wxMemberCardDataRespVO.getIconImage().isEmpty()) {
                    List<String> iconImageList = new ArrayList<>(convertStringToListS(wxMemberCardDataRespVO.getIconImage()));
                    wxMemberCardDataRespVO.setIconImageList(iconImageList);
                }
                if(wxMemberCardDataRespVO!=null){
                    wxMemberVO.setBackgroundImage(wxMemberCardDataRespVO.getBackgroundImage());
                    wxMemberVO.setThumbnailImage(wxMemberCardDataRespVO.getThumbnailImage());
                    wxMemberVO.setDescription(wxMemberCardDataRespVO.getDescription());
                    wxMemberVO.setMaxPointsThreshold(wxMemberCardDataRespVO.getMaxPointsThreshold());
                }
//                if(ObjectUtil.isNotEmpty(wxMemberDO.getMemberLevel())){
//                    wxMemberVO.setMemberLevel(wxMemberDO.getMemberLevel());
//                }
//                if(ObjectUtil.isNotEmpty(wxMemberDO.getFreezePoints())){
//                    wxMemberVO.setIntegralFrozen(wxMemberDO.getFreezePoints());
//                }

                return wxMemberVO;
            }

        }
        if (ObjectUtil.isNotEmpty(wxMemberDO) && ObjectUtil.isNotEmpty(wxMemberDO.getMemberNickName())) {
            wxMemberVO.setMemberNickName(wxMemberDO.getMemberNickName());
        } else {
            wxMemberVO.setMemberNickName("");
        }

        wxMemberVO.setMemberAvatar(wxMemberDO.getMemberAvatar());
        wxMemberVO.setIntegralFrozen(wxMemberDO.getIntegralFrozen());

        wxMemberVO.setAllPoints(wxMemberDO.getIntegralFrozen());
        //用用户的冻结积分查询匹配的会员卡信息
        //WHERE #{allPoints} BETWEEN min_points_threshold AND max_points_threshold and card_status = '1'
        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<>();
        //queryWrapper.le("card_status", wxMember.getIntegralFrozen());
        queryWrapper.le("min_points_threshold", wxMemberDO.getIntegralFrozen());
        queryWrapper.eq("card_status", 1);
        queryWrapper.orderByDesc("member_level");
        List<WxMemberCardDO> wxMemberCardDOS = wxMemberCardMapper.selectList(queryWrapper);
        WxMemberCardDO wxMemberCard = new WxMemberCardDO();
        if(ObjectUtil.isNotEmpty(wxMemberCardDOS)){
            wxMemberCard = wxMemberCardDOS.get(0);
            int size = wxMemberCardDOS.size();
            QueryWrapper<WxMemberCardDO> wrapper = new QueryWrapper<>();
            wrapper.eq("card_status", 1);
            List<WxMemberCardDO> wxMemberCardDOS1 = wxMemberCardMapper.selectList(wrapper);
            if(size == wxMemberCardDOS1.size()){
                wxMemberVO.setIsFlag(1);
            }
        }
        String backgroundImage = "";
        if (ObjectUtil.isNotEmpty(wxMemberCard) && ObjectUtil.isNotEmpty(wxMemberCard.getBackgroundImage())) {
            backgroundImage = wxMemberCard.getBackgroundImage();
            wxMemberVO.setMemberLevel(wxMemberCard.getMemberLevel());
            wxMemberVO.setDescription(wxMemberCard.getDescription());
            wxMemberVO.setMaxPointsThreshold(wxMemberCard.getMaxPointsThreshold());
        }
        if (ObjectUtil.isNotEmpty(wxMemberCard) && ObjectUtil.isNotEmpty(wxMemberCard.getThumbnailImage())) {
            String thumbnailImage = wxMemberCard.getThumbnailImage();
            wxMemberVO.setThumbnailImage(thumbnailImage);
        }
        wxMemberVO.setBackgroundImage(backgroundImage);
        return wxMemberVO;
    }

    @Override
    public List<WxMemberIdRespVO> getAllMemberCardIds() {
        List<WxMemberIdRespVO> wxMemberIdVOList = new ArrayList<>();
        // where card_status != '3'
        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_status", 1);
        queryWrapper.ne("card_status", 3);
        List<WxMemberCardDO> wxMemberCards = wxMemberCardMapper.selectList(queryWrapper);

        for (WxMemberCardDO memberCard : wxMemberCards) {
            WxMemberIdRespVO wxMemberIdVO = new WxMemberIdRespVO();
            wxMemberIdVO.setMemberCardId(memberCard.getMemberCardId());
            wxMemberIdVO.setBackgroundImage(memberCard.getBackgroundImage());
            List<ImageRespVO> detailImageList = new ArrayList<>();
            if (memberCard.getDetailImage() != null && !memberCard.getDetailImage().isEmpty()) {
                for (String s : convertStringToListS(memberCard.getDetailImage())) {
                    ImageRespVO imageDTO = new ImageRespVO();
                    imageDTO.setUrl(s);
                    detailImageList.add(imageDTO);
                }
            }
            wxMemberIdVO.setDetailImageList(detailImageList);
            wxMemberIdVOList.add(wxMemberIdVO);
        }
        return wxMemberIdVOList;
    }


}
