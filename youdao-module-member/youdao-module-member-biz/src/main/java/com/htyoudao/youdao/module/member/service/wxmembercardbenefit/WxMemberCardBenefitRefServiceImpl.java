package com.htyoudao.youdao.module.member.service.wxmembercardbenefit;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateSaveReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardBenefitItemRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardBenefitItemSaveReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardBenefitPageReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercardbenefit.WxMemberCardBenefitRefDO;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercardbenefit.WxMemberCardBenefitRefMapper;
import com.htyoudao.youdao.module.promotion.api.couponpackage.CouponPackageApi;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BIRTHDAY_BENEFIT_PACKAGE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BIRTHDAY_BENEFIT_TOTAL_SEND_NUM_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_COUPON_ID_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_COUPON_TYPE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_DUPLICATE;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_ISSUE_VALUE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_MEMBER_CARD_NOT_EXISTS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_REPEAT_TYPE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_SCENE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_BENEFIT_SEND_NUM_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_MEMBER_BENEFIT_PACKAGE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_MEMBER_BENEFIT_TOTAL_SEND_NUM_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_POINTS_RANGE_CONFLICT;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_UPGRADE_BENEFIT_PACKAGE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_CARD_UPGRADE_BENEFIT_TOTAL_SEND_NUM_ERROR;

@Service
public class WxMemberCardBenefitRefServiceImpl extends ServiceImpl<WxMemberCardBenefitRefMapper, WxMemberCardBenefitRefDO>
        implements IWxMemberCardBenefitRefService {

    private static final int BENEFIT_SCENE_MEMBER = 1;
    private static final int BENEFIT_SCENE_BIRTHDAY = 2;
    private static final int BENEFIT_SCENE_UPGRADE = 3;
    private static final int COUPON_TYPE_COUPON = 1;
    private static final int COUPON_TYPE_PACKAGE = 2;
    private static final int BENEFIT_MAX_SEND_NUM = 5;
    private static final int REPEAT_TYPE_WEEK = 1;
    private static final int REPEAT_TYPE_MONTH = 2;

    @Resource
    private WxMemberCardBenefitRefMapper wxMemberCardBenefitRefMapper;

    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @DubboReference
    private GoodCouponApi goodCouponApi;

    @DubboReference
    private CouponPackageApi couponPackageApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBenefit(WxMemberCardAggregateSaveReqVO reqVO) {
        validateMemberCardName(reqVO.getName(), null);
        validatePointsRange(reqVO.getMinPointsThreshold(), reqVO.getMaxPointsThreshold(), null);

        WxMemberCardDO memberCardDO = new WxMemberCardDO();
        BeanUtils.copyProperties(reqVO, memberCardDO);
        wxMemberCardMapper.insert(memberCardDO);

        validateBenefitItems(reqVO.getMemberBenefits(), BENEFIT_SCENE_MEMBER);
        validateBenefitItems(reqVO.getBirthdayBenefits(), BENEFIT_SCENE_BIRTHDAY);
        validateBenefitItems(reqVO.getUpgradeBenefits(), BENEFIT_SCENE_UPGRADE);
        saveBenefitItems(memberCardDO, reqVO.getMemberBenefits(), BENEFIT_SCENE_MEMBER);
        saveBenefitItems(memberCardDO, reqVO.getBirthdayBenefits(), BENEFIT_SCENE_BIRTHDAY);
        saveBenefitItems(memberCardDO, reqVO.getUpgradeBenefits(), BENEFIT_SCENE_UPGRADE);
        return memberCardDO.getMemberCardId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBenefit(WxMemberCardAggregateSaveReqVO reqVO) {
        WxMemberCardDO memberCardDO = validateMemberCard(reqVO.getMemberCardId());
        validateMemberCardName(reqVO.getName(), reqVO.getMemberCardId());
        validatePointsRange(reqVO.getMinPointsThreshold(), reqVO.getMaxPointsThreshold(), reqVO.getMemberCardId());

        WxMemberCardDO updateObj = new WxMemberCardDO();
        BeanUtils.copyProperties(reqVO, updateObj);
        wxMemberCardMapper.updateById(updateObj);

        validateBenefitItems(reqVO.getMemberBenefits(), BENEFIT_SCENE_MEMBER);
        validateBenefitItems(reqVO.getBirthdayBenefits(), BENEFIT_SCENE_BIRTHDAY);
        validateBenefitItems(reqVO.getUpgradeBenefits(), BENEFIT_SCENE_UPGRADE);

        wxMemberCardBenefitRefMapper.delete(new LambdaQueryWrapper<WxMemberCardBenefitRefDO>()
                .eq(WxMemberCardBenefitRefDO::getMemberCardId, reqVO.getMemberCardId())
                .in(WxMemberCardBenefitRefDO::getBenefitScene, BENEFIT_SCENE_MEMBER, BENEFIT_SCENE_BIRTHDAY));
        if (reqVO.getUpgradeBenefits() != null) {
            wxMemberCardBenefitRefMapper.delete(new LambdaQueryWrapper<WxMemberCardBenefitRefDO>()
                    .eq(WxMemberCardBenefitRefDO::getMemberCardId, reqVO.getMemberCardId())
                    .eq(WxMemberCardBenefitRefDO::getBenefitScene, BENEFIT_SCENE_UPGRADE));
        }

        saveBenefitItems(memberCardDO, reqVO.getMemberBenefits(), BENEFIT_SCENE_MEMBER);
        saveBenefitItems(memberCardDO, reqVO.getBirthdayBenefits(), BENEFIT_SCENE_BIRTHDAY);
        saveBenefitItems(memberCardDO, reqVO.getUpgradeBenefits(), BENEFIT_SCENE_UPGRADE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMemberCard(Long memberCardId) {
        validateMemberCard(memberCardId);
        wxMemberCardMapper.deleteById(memberCardId);
        wxMemberCardBenefitRefMapper.delete(new LambdaQueryWrapper<WxMemberCardBenefitRefDO>()
                .eq(WxMemberCardBenefitRefDO::getMemberCardId, memberCardId));
        return true;
    }

    @Override
    public WxMemberCardAggregateRespVO getCardBenefitDetail(Long memberCardId) {
        WxMemberCardDO memberCardDO = validateMemberCard(memberCardId);
        List<WxMemberCardBenefitRefDO> benefitRefs = listByMemberCardIds(List.of(memberCardId));
        return buildCardResp(memberCardDO, benefitRefs, buildNameMap(benefitRefs));
    }

    @Override
    public PageResult<WxMemberCardAggregateRespVO> pageBenefits(WxMemberCardBenefitPageReqVO reqVO) {
        QueryWrapper<WxMemberCardDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("member_level");
        Page<WxMemberCardDO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        Page<WxMemberCardDO> memberCardPage = wxMemberCardMapper.selectPage(page, queryWrapper);
        List<WxMemberCardDO> memberCards = memberCardPage.getRecords();
        if (memberCards.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), memberCardPage.getTotal());
        }

        List<Long> memberCardIds = memberCards.stream()
                .map(WxMemberCardDO::getMemberCardId)
                .filter(Objects::nonNull)
                .toList();
        List<WxMemberCardBenefitRefDO> benefitRefs = listByMemberCardIds(memberCardIds);

        Map<Long, List<WxMemberCardBenefitRefDO>> benefitGroupMap = benefitRefs.stream()
                .collect(Collectors.groupingBy(WxMemberCardBenefitRefDO::getMemberCardId, LinkedHashMap::new, Collectors.toList()));
        Map<String, String> benefitNameMap = buildNameMap(benefitRefs);

        List<WxMemberCardAggregateRespVO> result = new ArrayList<>(memberCards.size());
        for (WxMemberCardDO memberCard : memberCards) {
            List<WxMemberCardBenefitRefDO> currentBenefits = benefitGroupMap.getOrDefault(memberCard.getMemberCardId(), Collections.emptyList());
            result.add(buildCardResp(memberCard, currentBenefits, benefitNameMap));
        }
        return new PageResult<>(result, memberCardPage.getTotal());
    }

    private List<WxMemberCardBenefitRefDO> listByMemberCardIds(List<Long> memberCardIds) {
        if (memberCardIds == null || memberCardIds.isEmpty()) {
            return Collections.emptyList();
        }
        return wxMemberCardBenefitRefMapper.selectList(new LambdaQueryWrapper<WxMemberCardBenefitRefDO>()
                .in(WxMemberCardBenefitRefDO::getMemberCardId, memberCardIds)
                .orderByAsc(WxMemberCardBenefitRefDO::getBenefitScene)
                .orderByAsc(WxMemberCardBenefitRefDO::getId));
    }

    private void saveBenefitItems(WxMemberCardDO memberCardDO, List<WxMemberCardBenefitItemSaveReqVO> items, Integer benefitScene) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (WxMemberCardBenefitItemSaveReqVO item : items) {
            WxMemberCardBenefitRefDO benefitRefDO = new WxMemberCardBenefitRefDO();
            benefitRefDO.setMemberCardId(memberCardDO.getMemberCardId());
            benefitRefDO.setBenefitScene(benefitScene);
            benefitRefDO.setCouponType(item.getCouponType());
            benefitRefDO.setCouponId(item.getCouponId());
            benefitRefDO.setSendNum(item.getSendNum());
            benefitRefDO.setRepeatType(item.getRepeatType());
            benefitRefDO.setIssueValue(item.getIssueValue());
            benefitRefDO.setBusinessId(memberCardDO.getBusinessId());
            normalizeBenefitRule(benefitRefDO);
            save(benefitRefDO);
        }
    }

    private void validateBenefitItems(List<WxMemberCardBenefitItemSaveReqVO> items, Integer benefitScene) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<String> duplicateKeys = new HashSet<>();
        int totalSendNum = 0;
        for (WxMemberCardBenefitItemSaveReqVO item : items) {
            validateCommonFields(benefitScene, item.getCouponType());
            validateCouponOnly(benefitScene, item.getCouponType());
            validateCouponId(item.getCouponId());
            validateSendNum(item.getSendNum());
            totalSendNum += item.getSendNum();
            validateBenefitRule(benefitScene, item);
            String duplicateKey = buildDuplicateKey(benefitScene, item.getCouponType(), item.getCouponId());
            if (!duplicateKeys.add(duplicateKey)) {
                throw exception(WX_MEMBER_CARD_BENEFIT_DUPLICATE);
            }
        }
        validateBenefitTotalSendNum(benefitScene, totalSendNum);
    }

    private void validateCouponId(Long couponId) {
        if (couponId == null || couponId <= 0) {
            throw exception(WX_MEMBER_CARD_BENEFIT_COUPON_ID_ERROR);
        }
    }

    private void validateSendNum(Integer sendNum) {
        if (sendNum == null || sendNum <= 0) {
            throw exception(WX_MEMBER_CARD_BENEFIT_SEND_NUM_ERROR);
        }
    }

    private void validateBenefitRule(Integer benefitScene, WxMemberCardBenefitItemSaveReqVO item) {
        if (Objects.equals(benefitScene, BENEFIT_SCENE_BIRTHDAY)
                || Objects.equals(benefitScene, BENEFIT_SCENE_UPGRADE)) {
            return;
        }
        Integer repeatType = item.getRepeatType();
        Integer issueValue = item.getIssueValue();
        if (repeatType == null) {
            return;
        }
        if (!Objects.equals(repeatType, REPEAT_TYPE_WEEK) && !Objects.equals(repeatType, REPEAT_TYPE_MONTH)) {
            throw exception(WX_MEMBER_CARD_BENEFIT_REPEAT_TYPE_ERROR);
        }
        if (issueValue == null) {
            throw exception(WX_MEMBER_CARD_BENEFIT_ISSUE_VALUE_ERROR);
        }
        if (Objects.equals(repeatType, REPEAT_TYPE_WEEK) && (issueValue < 1 || issueValue > 7)) {
            throw exception(WX_MEMBER_CARD_BENEFIT_ISSUE_VALUE_ERROR);
        }
        if (Objects.equals(repeatType, REPEAT_TYPE_MONTH) && (issueValue < 1 || issueValue > 31)) {
            throw exception(WX_MEMBER_CARD_BENEFIT_ISSUE_VALUE_ERROR);
        }
    }

    private WxMemberCardDO validateMemberCard(Long memberCardId) {
        WxMemberCardDO memberCardDO = wxMemberCardMapper.selectById(memberCardId);
        if (memberCardDO == null) {
            throw exception(WX_MEMBER_CARD_BENEFIT_MEMBER_CARD_NOT_EXISTS);
        }
        return memberCardDO;
    }

    private void validateMemberCardName(String name, Long memberCardId) {
        LambdaQueryWrapper<WxMemberCardDO> queryWrapper = new LambdaQueryWrapper<WxMemberCardDO>()
                .eq(WxMemberCardDO::getName, name);
        if (memberCardId != null) {
            queryWrapper.ne(WxMemberCardDO::getMemberCardId, memberCardId);
        }
        if (wxMemberCardMapper.selectCount(queryWrapper) > 0) {
            throw exception(WX_MEMBER_CARD_ERROR);
        }
    }

    private void validatePointsRange(Integer minPointsThreshold, Long maxPointsThreshold, Long memberCardId) {
        if (minPointsThreshold == null || maxPointsThreshold == null) {
            return;
        }
        LambdaQueryWrapper<WxMemberCardDO> queryWrapper = new LambdaQueryWrapper<WxMemberCardDO>()
                .le(WxMemberCardDO::getMinPointsThreshold, maxPointsThreshold)
                .ge(WxMemberCardDO::getMaxPointsThreshold, minPointsThreshold);
        if (memberCardId != null) {
            queryWrapper.ne(WxMemberCardDO::getMemberCardId, memberCardId);
        }
        if (wxMemberCardMapper.selectCount(queryWrapper) > 0) {
            throw exception(WX_MEMBER_CARD_POINTS_RANGE_CONFLICT);
        }
    }

    private void validateCommonFields(Integer benefitScene, Integer couponType) {
        if (!Objects.equals(benefitScene, BENEFIT_SCENE_MEMBER)
                && !Objects.equals(benefitScene, BENEFIT_SCENE_BIRTHDAY)
                && !Objects.equals(benefitScene, BENEFIT_SCENE_UPGRADE)) {
            throw exception(WX_MEMBER_CARD_BENEFIT_SCENE_ERROR);
        }
        if (!Objects.equals(couponType, COUPON_TYPE_COUPON) && !Objects.equals(couponType, COUPON_TYPE_PACKAGE)) {
            throw exception(WX_MEMBER_CARD_BENEFIT_COUPON_TYPE_ERROR);
        }
    }

    private void validateCouponOnly(Integer benefitScene, Integer couponType) {
        if (Objects.equals(couponType, COUPON_TYPE_COUPON)) {
            return;
        }
        if (Objects.equals(benefitScene, BENEFIT_SCENE_BIRTHDAY)) {
            throw exception(WX_MEMBER_CARD_BIRTHDAY_BENEFIT_PACKAGE_ERROR);
        }
        if (Objects.equals(benefitScene, BENEFIT_SCENE_UPGRADE)) {
            throw exception(WX_MEMBER_CARD_UPGRADE_BENEFIT_PACKAGE_ERROR);
        }
        throw exception(WX_MEMBER_CARD_MEMBER_BENEFIT_PACKAGE_ERROR);
    }

    private void validateBenefitTotalSendNum(Integer benefitScene, int totalSendNum) {
        if (totalSendNum <= BENEFIT_MAX_SEND_NUM) {
            return;
        }
        if (Objects.equals(benefitScene, BENEFIT_SCENE_BIRTHDAY)) {
            throw exception(WX_MEMBER_CARD_BIRTHDAY_BENEFIT_TOTAL_SEND_NUM_ERROR);
        }
        if (Objects.equals(benefitScene, BENEFIT_SCENE_UPGRADE)) {
            throw exception(WX_MEMBER_CARD_UPGRADE_BENEFIT_TOTAL_SEND_NUM_ERROR);
        }
        throw exception(WX_MEMBER_CARD_MEMBER_BENEFIT_TOTAL_SEND_NUM_ERROR);
    }

    private void normalizeBenefitRule(WxMemberCardBenefitRefDO benefitRefDO) {
        if (Objects.equals(benefitRefDO.getBenefitScene(), BENEFIT_SCENE_BIRTHDAY)
                || Objects.equals(benefitRefDO.getBenefitScene(), BENEFIT_SCENE_UPGRADE)) {
            benefitRefDO.setRepeatType(null);
            benefitRefDO.setIssueValue(null);
            return;
        }
        if (benefitRefDO.getRepeatType() == null) {
            benefitRefDO.setIssueValue(null);
        }
    }

    private Map<String, String> buildNameMap(List<WxMemberCardBenefitRefDO> benefitRefs) {
        if (benefitRefs.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> couponIds = benefitRefs.stream()
                .filter(item -> Objects.equals(item.getCouponType(), COUPON_TYPE_COUPON))
                .map(WxMemberCardBenefitRefDO::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> packageIds = benefitRefs.stream()
                .filter(item -> Objects.equals(item.getCouponType(), COUPON_TYPE_PACKAGE))
                .map(WxMemberCardBenefitRefDO::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> nameMap = new HashMap<>();
        Map<Long, String> couponNameMap = couponIds.isEmpty() ? Collections.emptyMap() : goodCouponApi.getCouponNameMap(new ArrayList<>(couponIds));
        if (couponNameMap != null && !couponNameMap.isEmpty()) {
            couponNameMap.forEach((couponId, couponName) ->
                    nameMap.put(buildNameKey(COUPON_TYPE_COUPON, couponId), couponName));
        }
        Map<Long, String> packageNameMap = packageIds.isEmpty() ? Collections.emptyMap() : couponPackageApi.getPackageNameMap(new ArrayList<>(packageIds));
        if (packageNameMap != null && !packageNameMap.isEmpty()) {
            packageNameMap.forEach((packageId, packageName) ->
                    nameMap.put(buildNameKey(COUPON_TYPE_PACKAGE, packageId), packageName));
        }
        return nameMap;
    }

    private WxMemberCardAggregateRespVO buildCardResp(WxMemberCardDO memberCardDO,
                                                      List<WxMemberCardBenefitRefDO> benefitRefs,
                                                      Map<String, String> benefitNameMap) {
        WxMemberCardAggregateRespVO respVO = new WxMemberCardAggregateRespVO();
        BeanUtils.copyProperties(memberCardDO, respVO);
        if (ObjectUtil.isNotEmpty(memberCardDO.getBackgroundImage())) {
            respVO.setBackgroundImageList(Collections.singletonList(memberCardDO.getBackgroundImage()));
        } else {
            respVO.setBackgroundImageList(new ArrayList<>());
        }

        List<WxMemberCardBenefitItemRespVO> memberBenefits = new ArrayList<>();
        List<WxMemberCardBenefitItemRespVO> birthdayBenefits = new ArrayList<>();
        List<WxMemberCardBenefitItemRespVO> upgradeBenefits = new ArrayList<>();
        for (WxMemberCardBenefitRefDO benefitRef : benefitRefs) {
            WxMemberCardBenefitItemRespVO itemRespVO = BeanCopyUtils.copyBean(benefitRef, WxMemberCardBenefitItemRespVO.class);
            itemRespVO.setBenefitName(benefitNameMap.getOrDefault(
                    buildNameKey(benefitRef.getCouponType(), benefitRef.getCouponId()), ""));
            if (Objects.equals(benefitRef.getBenefitScene(), BENEFIT_SCENE_MEMBER)) {
                memberBenefits.add(itemRespVO);
            } else if (Objects.equals(benefitRef.getBenefitScene(), BENEFIT_SCENE_BIRTHDAY)) {
                birthdayBenefits.add(itemRespVO);
            } else if (Objects.equals(benefitRef.getBenefitScene(), BENEFIT_SCENE_UPGRADE)) {
                upgradeBenefits.add(itemRespVO);
            }
        }
        respVO.setMemberBenefits(memberBenefits);
        respVO.setBirthdayBenefits(birthdayBenefits);
        respVO.setUpgradeBenefits(upgradeBenefits);
        return respVO;
    }

    private String buildNameKey(Integer couponType, Long couponId) {
        return couponType + "_" + couponId;
    }

    private String buildDuplicateKey(Integer benefitScene, Integer couponType, Long couponId) {
        return benefitScene + "_" + couponType + "_" + couponId;
    }
}
