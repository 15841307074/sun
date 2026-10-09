package com.htyoudao.youdao.module.member.service.wxmember.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.date.DateUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRegisterRepVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercardbenefit.WxMemberCardBenefitRefDO;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercardbenefit.WxMemberCardBenefitRefMapper;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberManagerService;
import com.htyoudao.youdao.module.member.util.StringUtils;
import com.htyoudao.youdao.module.member.util.redis.RedisCache;
import com.htyoudao.youdao.framework.ip.core.utils.SensitiveUtils;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CouponNumDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.system.api.auth.AuthApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;

/**
 * 会员 Service 实现类
 *
 * @author 零零玖零
 */
@Service
@RefreshScope
@Slf4j
public class WxMemberManagerServiceImpl implements WxMemberManagerService {

    @Value("${bindingMax}")
    private Integer bindingMax;
    @Resource
    private WxMemberMapper wxMemberMapper;
    @Resource
    private RedisCache redisCache;

    @DubboReference
    private AuthApi authApi;

    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @Resource
    private WxMemberCardBenefitRefMapper wxMemberCardBenefitRefMapper;

    @DubboReference
    private UserCouponApi userCouponApi;


    @Override
    @DS(DsNameConstants.SHARDING)
    //@DSTransactional
    public CommonResult<WxMemberRespVO> register(WxMemberRegisterRepVO wxMemberRegisterRepVO, String token) {

        // ===================== 调用校验方法 =====================
        CommonResult<WxMemberRespVO> validResult = validateWxMemberRegister(wxMemberRegisterRepVO);
        if (validResult != null) {
            return validResult; // 校验失败，直接返回
        }

        Boolean securityFlag = SecurityFrameworkUtils.checkSecurityWxMember(wxMemberRegisterRepVO.getMemberId());

        if(!securityFlag){
            return error(WX_MEMBER_ID_ERROR);
        }

        if(ObjectUtil.isEmpty(wxMemberRegisterRepVO.getMemberMobile())){
            return error(WX_MEMBER_IS_EMPTY);
        }

        WxMemberDO wxMember = new WxMemberDO();
        org.springframework.beans.BeanUtils.copyProperties(wxMemberRegisterRepVO, wxMember, "memberBirthday");
        if (ObjectUtil.isNotEmpty(wxMemberRegisterRepVO.getMemberBirthday()) && !"请选择日期".equals(wxMemberRegisterRepVO.getMemberBirthday())) {
            wxMember.setMemberBirthday(DateUtil.parse(wxMemberRegisterRepVO.getMemberBirthday(), DateUtils.YYYY_MM_DD));
        }
        //会员头像
        if (ObjectUtil.isNotEmpty(wxMemberRegisterRepVO.getMemberAvatar())) {
            wxMember.setMemberAvatar(wxMemberRegisterRepVO.getMemberAvatar());
        }
        //会员昵称
        if (ObjectUtil.isNotEmpty(wxMemberRegisterRepVO.getMemberNickName())) {
            //boolean a = SensitiveWordFilter.containsSensitiveWord(redisCache, wxMemberRegisterRepVO.getMemberNickName());
            boolean a = SensitiveUtils.checkSensitive(wxMemberRegisterRepVO.getMemberNickName());
            log.info(a + "用户填写的昵称是={}", wxMemberRegisterRepVO.getMemberNickName());
            if (a) {
                return error(WX_MEMBER_NAME_ERROR);
            }
            wxMember.setMemberNickName(wxMemberRegisterRepVO.getMemberNickName());
        }
        //会员性别
        if (ObjectUtil.isNotEmpty(wxMemberRegisterRepVO.getGender())) {
            wxMember.setGender(wxMemberRegisterRepVO.getGender());
        }

        // 判断是否换绑
        boolean changeBindingFlag = wxMemberRegisterRepVO.getChangeBinding() != null && wxMemberRegisterRepVO.getChangeBinding() == 1;
        //会员手机号
        if (ObjectUtil.isNotEmpty(wxMemberRegisterRepVO.getMemberMobile())) {
            wxMember.setMemberMobile(wxMemberRegisterRepVO.getMemberMobile());
            // 获取用户登录的手机号 判断是否需要换绑 更新token
            if(changeBindingFlag){
                try{
                    CommonResult<Boolean> result = authApi.updateToken(token, wxMemberRegisterRepVO.getMemberMobile());
                    log.info("远程调用更新手机号绑定 {}, msg:{}", result.getCode(), result.getMsg());
                }catch (Exception e){
                    log.warn("注册接口换绑失败{}",token);
                    return error(WX_MEMBER_PHONE_CHANGE_ERROR);
                }
                int bindingCount = ObjectUtil.isEmpty(wxMemberRegisterRepVO.getBindingCount()) ? 1 : wxMemberRegisterRepVO.getBindingCount() + 1;
                if(bindingCount > bindingMax){
                    return error(WX_MEMBER_MOBILE_BINDING_ERROR);
                }
                wxMember.setBindingCount(bindingCount);
            }
        }

        // 注册后 为会员
        wxMember.setUserIdentity(1);
        wxMember.setMemberLevel(1);
        wxMember.setRegisterTime(DateUtils.getNowDate());
        //todo 领取注册礼
        edit(wxMember);
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
        queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(queryWrapper);

        if (ObjectUtil.isEmpty(wxMemberDO)){
            log.warn("register接口 memberId not found:{}", wxMemberRegisterRepVO.getMemberId());
            throw new ServiceException(WX_MEMBER_NOT_EXISTS);
        }



        WxMemberRespVO wxMemberRespVO = BeanUtils.toBean(wxMemberDO, WxMemberRespVO.class);
        wxMemberRespVO.setBindingMax(bindingMax);
        LambdaQueryWrapper<WxMemberCardDO> queryWrapperCard = new LambdaQueryWrapper<>();
        queryWrapperCard.select(WxMemberCardDO::getMemberCardId);
        queryWrapperCard.eq(WxMemberCardDO::getMemberLevel, 1);

        if(!changeBindingFlag){
            WxMemberCardDO wxMemberCardDO = wxMemberCardMapper.selectOne(queryWrapperCard);
            if(ObjectUtil.isNotEmpty(wxMemberCardDO)){
                LambdaQueryWrapper<WxMemberCardBenefitRefDO> queryWrapperCardBenefit = new LambdaQueryWrapper<>();
                queryWrapperCardBenefit.select(WxMemberCardBenefitRefDO::getCouponId, WxMemberCardBenefitRefDO::getSendNum);
                queryWrapperCardBenefit.eq(WxMemberCardBenefitRefDO::getMemberCardId, wxMemberCardDO.getMemberCardId());
                queryWrapperCardBenefit.eq(WxMemberCardBenefitRefDO::getBenefitScene, 3);
                queryWrapperCardBenefit.eq(WxMemberCardBenefitRefDO::getCouponType, 1);
                List<WxMemberCardBenefitRefDO> wxMemberCardBenefitRefDOS = wxMemberCardBenefitRefMapper.selectList(queryWrapperCardBenefit);
                List<Long> couponIds = wxMemberCardBenefitRefDOS.stream().map(WxMemberCardBenefitRefDO::getCouponId).collect(Collectors.toList());
                List<CouponNumDTO> list = wxMemberCardBenefitRefDOS.stream().map(item -> {
                    CouponNumDTO couponNumDTO = new CouponNumDTO();
                    couponNumDTO.setCouponId(item.getCouponId());
                    couponNumDTO.setNum(item.getSendNum());
                    return couponNumDTO;
                }).toList();
                if(CollectionUtil.isNotEmpty(list)){
                    MemberCouponDTO memberCouponDTO = new MemberCouponDTO();
                    memberCouponDTO.setMemberId(wxMemberDO.getMemberId());
                    memberCouponDTO.setMemberName(wxMemberDO.getMemberNickName());
                    memberCouponDTO.setMemberMobile(wxMemberDO.getMemberMobile());
                    memberCouponDTO.setCouponNumDTOList(list);
                    memberCouponDTO.setCouponIds(couponIds);
                    userCouponApi.insertCouponWithRegister(memberCouponDTO);
                }

            }
        }


        authApi.updateTokenFirst(token, wxMemberRegisterRepVO.getMemberMobile());

        return success(wxMemberRespVO);
    }

    private CommonResult<WxMemberRespVO> validateWxMemberRegister(WxMemberRegisterRepVO wxMemberRegisterRepVO) {
        if (wxMemberRegisterRepVO == null) {
            return error(WX_MEMBER_ID_ERROR);
        }

        // 用户名长度校验
        if (wxMemberRegisterRepVO.getMemberName() != null && wxMemberRegisterRepVO.getMemberName().length() > 50) {
            return error(WX_MEMBER_NAME_LENGTH_ERROR);
        }

        // 昵称长度校验
        if (wxMemberRegisterRepVO.getMemberNickName() != null && wxMemberRegisterRepVO.getMemberNickName().length() > 255) {
            return error(WX_MEMBER_NICK_NAME_LENGTH_ERROR);
        }

        // 头像地址长度校验
        if (wxMemberRegisterRepVO.getMemberAvatar() != null && wxMemberRegisterRepVO.getMemberAvatar().length() > 255) {
            return error(WX_MEMBER_AVATAR_LENGTH_ERROR);
        }

        // 性别范围 0-2
        if (wxMemberRegisterRepVO.getGender() != null) {
            if (wxMemberRegisterRepVO.getGender() < 0 || wxMemberRegisterRepVO.getGender() > 2) {
                return error(WX_MEMBER_GENDER_ERROR);
            }
        }

        // openid 长度
        if (wxMemberRegisterRepVO.getOpenid() != null && wxMemberRegisterRepVO.getOpenid().length() > 32) {
            return error(WX_MEMBER_OPENID_ERROR);
        }

        // 微信头像长度
        if (wxMemberRegisterRepVO.getWxAvatarImg() != null && wxMemberRegisterRepVO.getWxAvatarImg().length() > 255) {
            return error(WX_MEMBER_AVATAR_ERROR);
        }

        // 是否换绑 1-2
        if (wxMemberRegisterRepVO.getChangeBinding() != null) {
            if (wxMemberRegisterRepVO.getChangeBinding() < 1 || wxMemberRegisterRepVO.getChangeBinding() > 2) {
                return error(WX_MEMBER_CHANGE_BINDING_ERROR);
            }
        }

        // 绑定次数 0-99
        if (wxMemberRegisterRepVO.getBindingCount() != null) {
            if (wxMemberRegisterRepVO.getBindingCount() < 0 || wxMemberRegisterRepVO.getBindingCount() > 99) {
                return error(WX_MEMBER_BINDING_COUNT_ERROR);
            }
        }

        // 校验通过，返回 null 表示无错误
        return null;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @DSTransactional
    public CommonResult<WxMemberRespVO> show(WxMemberRegisterRepVO wxMemberRegisterRepVO) {
        WxMemberDO wxMember = new WxMemberDO();
        if(ObjectUtil.isEmpty(wxMemberRegisterRepVO.getMemberId())){
            return error(WX_MEMBER_ID_IS_EMPTY);
        }
        wxMember.setMemberId(wxMemberRegisterRepVO.getMemberId());
        makeShardingValue(wxMember);
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
        queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(queryWrapper);

        WxMemberRespVO wxMemberRespVO = BeanUtils.toBean(wxMemberDO, WxMemberRespVO.class);
        wxMemberRespVO.setBindingMax(bindingMax);
        return success(wxMemberRespVO);
    }

    private void edit(WxMemberDO wxMember) {

        makeShardingValue(wxMember);
        if (ObjectUtil.isNotEmpty(wxMember.getShardingValue())) {
            Integer shardingShare = wxMember.getShardingValue();
            LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WxMemberDO::getShardingValue, shardingShare);
            if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
                updateWrapper.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
            }
            if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
                updateWrapper.eq(WxMemberDO::getOpenid, wxMember.getOpenid());
            }

            updateWrapper.le(WxMemberDO::getBindingCount, bindingMax);
            if (wxMember.getRegisterTime() != null) {
                String timeStr = DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", wxMember.getRegisterTime());
                updateWrapper.setSql("register_time = IFNULL(register_time, '" + timeStr + "')");
                wxMember.setRegisterTime(null);
            }
            success(wxMemberMapper.update(BeanUtils.toBean(wxMember, WxMemberDO.class), updateWrapper));
        } else {
            error(WX_MEMBER_REJECT_UPDATE);
        }
    }

    public void makeShardingValue(WxMemberDO wxMember) {

        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
        }
        if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
            int number = 0;
            char letter = wxMember.getOpenid().charAt(wxMember.getOpenid().length() - 1);
            if (StringUtils.isLetter(letter)) {
                number = StringUtils.convertLetterToNumberUsingASCII(letter);
            }
            if (StringUtils.isNumber(letter)) {
                number = Integer.parseInt(String.valueOf(letter));
            }
            wxMember.setShardingValue(number % 10);
        }
    }
}
