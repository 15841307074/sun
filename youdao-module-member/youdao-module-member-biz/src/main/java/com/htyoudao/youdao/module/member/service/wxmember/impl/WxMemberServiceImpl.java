package com.htyoudao.youdao.module.member.service.wxmember.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.errand.api.runner.ErrandRunnerApi;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberCrowdDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDayDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.constant.WxMemberConstant;
import com.htyoudao.youdao.module.member.controller.admin.tag.vo.MemberTagVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.*;
import com.htyoudao.youdao.module.member.controller.app.wxmember.vo.*;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberCardDataRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CrowdStoreDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.MemberCrowdRefDO;
import com.htyoudao.youdao.module.member.dal.dataobject.crowd.CustomCrowdDO;
import com.htyoudao.youdao.module.member.dal.dataobject.membertag.WxMemberTagDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wecom.WecomGroupMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import com.htyoudao.youdao.module.member.dal.es.NoticeReserveMemberDocument;
import com.htyoudao.youdao.module.member.dal.mysql.wecom.WecomGroupMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmembercard.WxMemberCardMapper;
import com.htyoudao.youdao.module.member.service.appletnotice.AppletNoticeService;
import com.htyoudao.youdao.module.member.service.crowd.CrowdStoreService;
import com.htyoudao.youdao.module.member.service.crowd.CrowdTagService;
import com.htyoudao.youdao.module.member.service.crowd.CustomTagService;
import com.htyoudao.youdao.module.member.service.crowd.WxMemberCrowdRefService;
import com.htyoudao.youdao.module.member.service.pointsLog.IPointsLogService;
import com.htyoudao.youdao.module.member.service.tagvalue.TagValueService;
import com.htyoudao.youdao.module.member.service.wxmember.ExportMemberService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.service.wxmembercard.IWxMemberCardService;
import com.htyoudao.youdao.module.member.util.AccessTimeRestrictionUtil;
import com.htyoudao.youdao.module.member.util.DateUtils;
import com.htyoudao.youdao.module.member.util.enums.AppletPushTemplateTypeEnum;
import com.htyoudao.youdao.module.order.api.order.BzOrderApi;
import com.htyoudao.youdao.module.order.api.order.dto.BzOrderDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import com.htyoudao.youdao.module.system.api.sms.SmsCodeApi;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.enums.sms.SmsSceneEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertStringToListS;
import static com.htyoudao.youdao.module.errand.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.CODEGEN_TABLE_EXISTS;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.FORGET_PASSWORD_FAIL;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.OLD_PASSWORD_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.SMS_CODE_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_ALREADY_SET;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_FORMAT_ERROR;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_LOCKED;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_NOT_MATCH;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_NOT_SET;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.TRAN_PASSWORD_SET_FAIL;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.UPDATE_PASSWORD_FAIL;

/**
 * 会员 Service 实现类
 *
 * @author 零零玖零
 */
@Service
@DS(DsNameConstants.SHARDING)
@Slf4j
public class WxMemberServiceImpl implements WxMemberService {

    @Resource
    private WxMemberMapper wxMemberMapper;

    private static final RateLimiter memberListLimiter = RateLimiter.create(5);

    private static final DateTimeFormatter MM_DD_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    @Resource
    private IWxMemberCardService wxMemberCardService;

    @Resource
    private WxMemberCardMapper wxMemberCardMapper;

    @Resource
    private CustomTagService customTagService;

    @Resource
    private CrowdTagService crowdTagService;

    @Resource
    private CrowdStoreService crowdStoreService;

    @Resource
    @Lazy
    private WxMemberService wxMemberService;

    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private UserCouponApi userCouponApi;

    @Resource
    private ExcelActionService<WxMemberRespVO> excelActionService;

    @DubboReference
    private OrgApi orgApi;


    @DubboReference
    private BzOrderApi bzOrderApi;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @Resource
    private ExportMemberService exportMemberService;

    @Resource
    private IPointsLogService pointsLogService;


    @Resource
    private AppletNoticeService appletNoticeService;

    @Resource
    private TagValueService tagValueService;

    @Resource
    private WxMemberCrowdRefService wxMemberCrowdRefService;

    @Resource
    private WecomGroupMemberMapper wecomGroupMemberMapper;

    @DubboReference
    private ErrandRunnerApi errandRunnerApi;


    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private SmsCodeApi smsCodeApi;

    private static final String TRAN_PASSWORD_LOCK_KEY = "tran:password:lock:member:%s";
    private static final String TRAN_PASSWORD_ERROR_KEY = "tran:password:error:member:%s";
    private static final int MAX_ERROR_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    @Override
    public void deleteWxMember(Long id) {
        // 校验存在
        validateWxMemberExists(id);
        // 删除
        wxMemberMapper.deleteById(id);
    }

    private void validateWxMemberExists(Long id) {
        if (this.getWxMember(id) == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }
    }

    @Override
    public WxMemberDO getWxMember(Long id) {
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxMemberDO::getMemberId,id);
        queryWrapper.eq(WxMemberDO::getShardingValue, Integer.parseInt(String.valueOf(id % 10)));
        return wxMemberMapper.selectOne(queryWrapper);
//        return wxMemberMapper.selectById(id);
    }

    @Override
    @DSTransactional
    public Long createWxMemberByMiniProgram(WxMemberDO wxMember) {
        wxMember.setIntegralFrozen(0L);
        wxMember.setMemberIntegral(0);
        wxMember.setLoginNumber(1);
        wxMember.setMemberChannel(0);
        wxMember.setUserIdentity(0);
        wxMember.setMemberName("0090汉堡工厂");
        wxMember.setLastLoginTime(new Date());
        makeShardingValue(wxMember);
        if (Objects.isNull(wxMember.getShardingValue())) {
            throw exception(WX_MEMBER_SHARDING_VALUE_FAIL);
        }

        long id = identifierGenerator.nextId(null).longValue();
        ;
        // 计算 memberId
        wxMember.setMemberId(Long.parseLong(String.valueOf(id).substring(2) + wxMember.getShardingValue()));
        //wxMember.setRegisterTime(new Date());
        wxMember.setLastLoginTime(new Date());
        wxMemberMapper.insert(wxMember);
        return wxMember.getMemberId();
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

    @Override
    public List<WxMemberDO> selectList(WxMemberDO wxMember) {
        LambdaQueryWrapper<WxMemberDO> queryWrapper = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
            queryWrapper.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
            queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        }
        if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
            char letter = wxMember.getOpenid().charAt(wxMember.getOpenid().length() - 1);
            int number = StringUtils.convertLetterToNumberUsingASCII(letter);
            wxMember.setShardingValue(number % 10);
            queryWrapper.eq(WxMemberDO::getOpenid, wxMember.getOpenid());
            queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        }
        if (ObjectUtil.isNotEmpty(wxMember.getShardingValue())) {
            return wxMemberMapper.selectList(queryWrapper);
        } else {
            return Collections.emptyList();
        }

    }

    @Override
    public MemberOrderDTO updateFinalOrderFinishTime(Long memberId) {

        MemberOrderDTO returnObj = new MemberOrderDTO();

        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<WxMemberDO>();
        queryWrapper.eq(WxMemberDO::getMemberId, memberId);
        queryWrapper.eq(WxMemberDO::getShardingValue, memberId % 10);

        WxMemberDO wxMember = wxMemberMapper.selectOne(queryWrapper);

        if (ObjectUtil.isNotEmpty(wxMember)) {

            if (ObjectUtils.isNotEmpty(wxMember.getRegisterTime())) {
                returnObj.setIsMember(1);
            }

            LambdaQueryWrapper<WxMemberDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WxMemberDO::getMemberId, memberId);
            WxMemberDO wxMemberDO = wxMemberMapper.selectOne(wrapper);
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<WxMemberDO>();
            updateWrapper.eq("sharding_value", memberId % 10);
            updateWrapper.eq("member_id", memberId);
            updateWrapper.set("fourth_order_finish_time", wxMemberDO.getThirdOrderFinishTime());
            updateWrapper.set("third_order_finish_time", wxMemberDO.getSecondOrderFinishTime());
            updateWrapper.set("second_order_finish_time", wxMemberDO.getFinalOrderFinishTime());
            updateWrapper.set("final_order_finish_time", new Date());
            wxMemberMapper.update(updateWrapper);

            Date finalOrderFinishTime = wxMember.getFinalOrderFinishTime();
            //第一次下单
            if (ObjectUtils.isEmpty(finalOrderFinishTime)) {
                returnObj.setIsNew(1);
            } else {
                //下单间隔天数
                long daysBetween = ChronoUnit.DAYS.between(finalOrderFinishTime.toInstant(), new Date().toInstant());
                returnObj.setIntervalDays((int) daysBetween);
                if (daysBetween > 2000) {
                    returnObj.setIsNew(1);
                } else {
                    returnObj.setIsNew(0);
                }
            }
        }
        return returnObj;
    }

    /**
     * 更新会员积分
     *
     * @param memberId
     * @param memberIntegral
     */
    @Override
    public void updateMemberById(Long memberId, int memberIntegral) {
        WxMemberDO wxMemberDO = this.getWxMember(memberId);
        wxMemberDO.setMemberIntegral(memberIntegral);
        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(WxMemberDO::getShardingValue, memberId % 10);
        updateWrapper.eq(WxMemberDO::getMemberId, memberId);
        updateWrapper.set(WxMemberDO::getMemberIntegral, memberIntegral);
        wxMemberMapper.update(updateWrapper);
    }

    @Override
    @DataPermission(enable = false)
    public CommonResult<WxMemberDTO> getMemberById(Long memberId) {
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.lambda().eq(WxMemberDO::getShardingValue, memberId % 10);
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(queryWrapper);
        return CommonResult.success(BeanUtil.toBean(wxMemberDO, WxMemberDTO.class));
    }

    @Override
    @DataPermission(enable = false)
    public List<WxMemberDTO> getMemberByIds(List<Long> memberIds) {
        if (CollectionUtil.isEmpty(memberIds)) {
            return List.of();
        }
        Map<Integer, List<Long>> memberIdsByShard =
                memberIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.groupingBy(memberId -> Math.toIntExact(memberId % 10)));
        if (memberIdsByShard.isEmpty()) {
            return List.of();
        }
        List<WxMemberDO> members = new ArrayList<>();
        for (Map.Entry<Integer, List<Long>> entry : memberIdsByShard.entrySet()) {
            LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(WxMemberDO::getMemberId, entry.getValue());
            queryWrapper.eq(WxMemberDO::getShardingValue, entry.getKey());
            members.addAll(wxMemberMapper.selectList(queryWrapper));
        }
        return BeanUtils.toBean(members, WxMemberDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, String> getMemberNameMapByIds(List<Long> memberIds) {
        if (CollectionUtil.isEmpty(memberIds)) {
            return Map.of();
        }
        Map<Integer, List<Long>> memberIdsByShard =
                memberIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.groupingBy(memberId -> Math.toIntExact(memberId % 10)));
        if (memberIdsByShard.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> memberNameMap = new HashMap<>();
        for (Map.Entry<Integer, List<Long>> entry : memberIdsByShard.entrySet()) {
            LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .select(WxMemberDO::getMemberId, WxMemberDO::getMemberNickName, WxMemberDO::getMemberName)
                    .in(WxMemberDO::getMemberId, entry.getValue())
                    .eq(WxMemberDO::getShardingValue, entry.getKey());
            for (WxMemberDO member : wxMemberMapper.selectList(queryWrapper)) {
                String memberName =
                        StringUtils.isNotBlank(member.getMemberNickName())
                                ? member.getMemberNickName()
                                : member.getMemberName();
                if (member.getMemberId() != null && StringUtils.isNotBlank(memberName)) {
                    memberNameMap.put(member.getMemberId(), memberName);
                }
            }
        }
        return memberNameMap;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void updateIntegral(Long memberId, Integer shardingValue, Long productPrice) {

        LambdaQueryWrapper<WxMemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMemberDO::getMemberId, memberId);
        wrapper.eq(WxMemberDO::getShardingValue, shardingValue);
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(wrapper);

        if (wxMemberDO != null) {
            LambdaUpdateWrapper<WxMemberDO> productUpdateWrapper = new LambdaUpdateWrapper<>();

            productUpdateWrapper.set(WxMemberDO::getMemberIntegral, wxMemberDO.getMemberIntegral() - productPrice);
            productUpdateWrapper.eq(WxMemberDO::getShardingValue, shardingValue);
            productUpdateWrapper.eq(WxMemberDO::getMemberId, memberId);
            wxMemberMapper.update(productUpdateWrapper);
        }

    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void updateErrandFlag(Long memberId,Integer shardingValue,Boolean errandFlag) {

        LambdaQueryWrapper<WxMemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMemberDO::getMemberId, memberId);
        wrapper.eq(WxMemberDO::getShardingValue, shardingValue);
        WxMemberDO wxMemberDO = wxMemberMapper.selectOne(wrapper);

        if (wxMemberDO != null) {
            LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();

            if(errandFlag){
                updateWrapper.set(WxMemberDO::getErrandFlag,1);
                updateWrapper.eq(WxMemberDO::getMemberId, memberId);
                updateWrapper.eq(WxMemberDO::getShardingValue, shardingValue);
                wxMemberMapper.update(updateWrapper);
            }

        }

    }

    @Override
    public WxMemberDTO getMemberByMobile(String memberMobile) {
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxMemberDO::getMemberMobile, memberMobile);
        queryWrapper.eq(WxMemberDO::getMemberCategory, 0);

        List<WxMemberDO> wxMembers = wxMemberMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(wxMembers)) {
            Optional<WxMemberDO> maxLoginTimeMember = wxMembers.stream()
                    .max(Comparator.comparing(WxMemberDO::getLastLoginTime));
            WxMemberDO wxMemberDO = maxLoginTimeMember.get();
            return BeanUtil.toBean(wxMemberDO, WxMemberDTO.class);
        }
        return new WxMemberDTO();
    }

    @Override
    public List<WxMemberDTO> getMemberByMobiles(List<String> memberMobiles) {
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(WxMemberDO::getMemberMobile, memberMobiles);
        queryWrapper.eq(WxMemberDO::getMemberCategory, 0);
        List<WxMemberDO> wxMemberList = wxMemberMapper.selectList(queryWrapper);

        // 按 memberMobile 分组，并取每组中 lastLoginTime 最大的记录
        Map<String, WxMemberDO> result = wxMemberList.stream()
                .collect(Collectors.toMap(
                        WxMemberDO::getMemberMobile,
                        member -> member,
                        (oldVal, newVal) ->
                                oldVal.getLastLoginTime().after(newVal.getLastLoginTime()) ? oldVal : newVal
                ));

        // 如果需要 List 结果
        List<WxMemberDO> maxLoginTimeMembers = new ArrayList<>(result.values());


        if (ObjectUtil.isNotEmpty(maxLoginTimeMembers)) {
            return BeanUtils.toBean(maxLoginTimeMembers, WxMemberDTO.class);
        }
        return List.of();
    }

    @Override
    public PageResult<WxMemberRespVO> selectListPage(WxMemberReqVO wxMemberReqVO) {
        // 限流
        boolean acquire = memberListLimiter.tryAcquire();
        if (!acquire) {
            throw exception(WX_MEMBER_GET_LIMITER);
        }
        // 会员卡等级
        List<WxMemberCardDO> list = wxMemberCardService.list();
        Map<Integer, WxMemberCardDO> collect = list.stream().collect(Collectors.toMap(WxMemberCardDO::getMemberLevel, c -> c));
        MPJLambdaWrapper<WxMemberDO> queryWrapper = new MPJLambdaWrapper<>();
        // 会员手机号
        if (StringUtils.isNotEmpty(wxMemberReqVO.getMemberMobile())) {
            queryWrapper.eq(WxMemberDO::getMemberMobile, wxMemberReqVO.getMemberMobile());
        }

        //性别
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getGender()), WxMemberDO::getGender, wxMemberReqVO.getGender());
        //首次下单门店
        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getDeptId())) {
            if (ObjectUtil.isEmpty(wxMemberReqVO.getIsStore())) {
                throw exception(WX_MEMBER_IS_STORE_NOT_NULL);
            }
            if (Objects.equals(wxMemberReqVO.getIsStore(), WxMemberConstant.IS_STORE_0)) {
                CommonResult<List<Long>> commonResult = storeApi.getStoreIdsByAllOrgId(wxMemberReqVO.getDeptId());
                List<Long> longs = JSON.parseArray(JSON.toJSONString(commonResult.getData()), Long.class);
                if (CollectionUtil.isEmpty(longs)) {
                    return PageResult.empty();
                }
                queryWrapper.in(WxMemberDO::getFirstOrderStoreId, longs);
            } else {
                queryWrapper.eq(WxMemberDO::getFirstOrderStoreId, wxMemberReqVO.getDeptId());
            }

        }
        //活跃度
        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getLiveness())) {
            // 活跃用户：7天内，uv访问次数＞3
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_0)) {
                queryWrapper.ge(WxMemberDO::getFourthLoginTime, DateUtils.getDaysBeforeStart(7));
            }
            // 有效用户：7天内，均单数＞2
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_1)) {
                queryWrapper.ge(WxMemberDO::getThirdOrderFinishTime, DateUtils.getDaysBeforeStart(7));
            }
            // 待促活用户：7天内未下单，14天内下过单
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_2)) {
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(7), DateUtils.getDaysBeforeStart(14));
            }
            // 半休眠用户：14天内未下单，30天内下过单
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_3)) {
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(14), DateUtils.getDaysBeforeStart(30));
            }
            // 休眠用户：30天内未下单，90天内下过单
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_4)) {
                queryWrapper.between(WxMemberDO::getFinalOrderFinishTime, DateUtils.getDaysBeforeStart(30), DateUtils.getDaysBeforeStart(90));
            }
            // 无效用户：＞90天未下单
            if (Objects.equals(wxMemberReqVO.getLiveness(), WxMemberConstant.LIVENESS_5)) {
                queryWrapper.le(WxMemberDO::getThirdOrderFinishTime, DateUtils.getDaysBeforeStart(90));
            }
        }


        // 会员状态
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberStatus()), WxMemberDO::getMemberStatus, wxMemberReqVO.getMemberStatus());
        // 会员类型
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberType()), WxMemberDO::getUserIdentity, wxMemberReqVO.getMemberType());
        // 会员来源
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberCategory()), WxMemberDO::getMemberCategory, wxMemberReqVO.getMemberCategory());
        // 注册时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterEndTime()), WxMemberDO::getRegisterTime, wxMemberReqVO.getRegisterStartTime(), wxMemberReqVO.getRegisterEndTime());
        // 最后登录时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginEndTime()), WxMemberDO::getLastLoginTime, wxMemberReqVO.getLastLoginStartTime(), wxMemberReqVO.getLastLoginEndTime());
        // 最后下单时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderEndTime()), WxMemberDO::getFinalOrderFinishTime, wxMemberReqVO.getLastOrderStartTime(), wxMemberReqVO.getLastOrderEndTime());
        // 是否是配送员 0不是 1是
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getErrandFlag()), WxMemberDO::getErrandFlag, wxMemberReqVO.getErrandFlag());

        // 会员等级
        Integer grade = wxMemberReqVO.getGrade();
        if (ObjectUtils.isNotEmpty(grade)) {
            // 会员等级

            if (Objects.equals(grade, WxMemberConstant.GRADE_1)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.le(WxMemberDO::getIntegralFrozen, wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_2)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_3)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_4)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_5)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.ge(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold());
            }
        }
        extracted(wxMemberReqVO, queryWrapper);
        // 倒序
        queryWrapper.orderByDesc("register_time");
        // 会员标签
        Integer memberLabel = wxMemberReqVO.getMemberLabel();
        if (ObjectUtil.isNotEmpty(memberLabel)) {
            queryWrapper.eq(WxMemberDO::getMemberLabel, memberLabel);
        }
        Integer pageNo = wxMemberReqVO.getPageNo();
        if (pageNo > 1000) {
            throw exception(WX_MEMBER_PAGE_NO_LIMIT);
        }
        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getCrowdId())) {
            queryWrapper.leftJoin(MemberCrowdRefDO.class, MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId);
            queryWrapper.eq(MemberCrowdRefDO::getCrowdId, wxMemberReqVO.getCrowdId());
        }

        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getTagId())) {
            queryWrapper.leftJoin(WxMemberTagDO.class, WxMemberTagDO::getMemberId, WxMemberDO::getMemberId);
            queryWrapper.eq(WxMemberTagDO::getTagId, wxMemberReqVO.getTagId());
            queryWrapper.eq(WxMemberTagDO::getDeleted, false);
        }
//        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getCommunityFlag())){
//            if(Objects.equals(wxMemberReqVO.getCommunityFlag(), WxMemberConstant.COMMUNITY_FLAG_1)){
//                //queryWrapper.isNotNull(WxMemberDO::getWxUnionid);
//                // in
//                //queryWrapper.leftJoin(WecomGroupMemberDO.class, WecomGroupMemberDO::getUnionId, WxMemberDO::getWxUnionid);
//                //queryWrapper.isNull(WecomGroupMemberDO::getUnionId);
//                queryWrapper.notExists(
//                        "select 1 from wecom_group_member where wecom_group_member.union_id = t.wx_unionid"
//                );
//            }else {
//                // notin
//                queryWrapper.isNotNull(WxMemberDO::getWxUnionid);
//                queryWrapper.innerJoin(WecomGroupMemberDO.class, WecomGroupMemberDO::getUnionId, WxMemberDO::getWxUnionid);
//                queryWrapper.isNotNull(WecomGroupMemberDO::getUnionId);
//            }
//        }
//        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getCommunityFlag())){
//            if(Objects.equals(wxMemberReqVO.getCommunityFlag(), WxMemberConstant.COMMUNITY_FLAG_1)){
//                //queryWrapper.isNotNull(WxMemberDO::getWxUnionid);
//                // in
//                queryWrapper.leftJoin(WecomGroupMemberDO.class, WecomGroupMemberDO::getUnionId, WxMemberDO::getWxUnionid);
//                queryWrapper.isNull(WecomGroupMemberDO::getUnionId);
//            }else {
//                // notin
//                queryWrapper.isNotNull(WxMemberDO::getWxUnionid);
//                queryWrapper.innerJoin(WecomGroupMemberDO.class, WecomGroupMemberDO::getUnionId, WxMemberDO::getWxUnionid);
//                queryWrapper.isNotNull(WecomGroupMemberDO::getUnionId);
//            }
//        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(wxMemberReqVO.getCommunityFlag()), WxMemberDO::getCommunityFlag, wxMemberReqVO.getCommunityFlag());
        Page<WxMemberDO> page = wxMemberMapper.selectPage(new Page<>(pageNo, wxMemberReqVO.getPageSize()), queryWrapper);

        PageResult<WxMemberRespVO> result = PageResult.empty();
        List<WxMemberDO> records = page.getRecords();
        List<WxMemberRespVO> voList = BeanUtils.toBean(records, WxMemberRespVO.class);
        Map<Long, BigDecimal> runnerBalanceMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(voList)) {
            List<Long> storeIds = records.stream().map(WxMemberDO::getFirstOrderStoreId).toList();
            List<Long> memberIds = records.stream().map(WxMemberDO::getMemberId).toList();
            List<Long> errandIds = records.stream().filter(item-> Objects.equals(item.getErrandFlag(), 1)).map(WxMemberDO::getMemberId).toList();

            if(CollectionUtil.isNotEmpty(errandIds)){
                runnerBalanceMap = errandRunnerApi.getRunnerBalanceByMemberId(errandIds);
            }
            List<MemberTagVO> memberTageListByMemberId = tagValueService.getMemberTageListByMemberId(memberIds);
            CommonResult<Set<StoreOrgDTO>> commonResult = orgApi.getOrgListByStoreIdV2(storeIds);
            Set<StoreOrgDTO> data = commonResult.getData();
            Map<Long, String> storeMap = new HashMap<>(8);
            Map<Long, String> storeIdToOrgNameMap = new HashMap<>(8);
            Map<Long, String> memberTagMap = new HashMap<>(8);
            // member是否在群里
            Set<String> memberInGroup = new HashSet<>();
            if(ObjectUtil.isEmpty(wxMemberReqVO.getCommunityFlag())){
                LambdaQueryWrapper<WecomGroupMemberDO> wecomGroupMemberQueryWrapper = new LambdaQueryWrapper<>();
                List<String> unionIds = records.stream().map(WxMemberDO::getWxUnionid).toList();
                if(CollectionUtil.isNotEmpty(unionIds)){
                    wecomGroupMemberQueryWrapper.in(WecomGroupMemberDO::getUnionId,unionIds);
                    wecomGroupMemberQueryWrapper.select(WecomGroupMemberDO::getUnionId);
                    List<WecomGroupMemberDO> wecomGroupMemberDOList = wecomGroupMemberMapper.selectList(wecomGroupMemberQueryWrapper);
                    if(CollectionUtil.isNotEmpty(wecomGroupMemberDOList)){
                        memberInGroup = wecomGroupMemberDOList.stream().filter(w -> ObjectUtil.isNotEmpty(w.getUnionId())).map(WecomGroupMemberDO::getUnionId).collect(Collectors.toSet());
                    }
                }
            }

            if (CollectionUtil.isNotEmpty(data)) {
                storeMap = data.stream().collect(Collectors.toMap(StoreOrgDTO::getStoreId, StoreOrgDTO::getStoreName));

                storeIdToOrgNameMap = data.stream()
                        .collect(Collectors.toMap(
                                StoreOrgDTO::getStoreId,
                                StoreOrgDTO::getOrgName
                        ));
            }
            if (CollectionUtil.isNotEmpty(memberTageListByMemberId)) {
                memberTagMap = memberTageListByMemberId.stream()
                        .collect(Collectors.toMap(
                                MemberTagVO::getMemberId,
                                MemberTagVO::getTagNames
                        ));
            }
//            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
//            List<StoreInfoDTO> stores = JSON.parseArray(JSON.toJSONString(storesByStoreIds.getData()), StoreInfoDTO.class);
//            Map<Long, String> storeMap = new HashMap<>(8);
//            if (CollectionUtil.isNotEmpty(stores)) {
//                storeMap = stores.stream().collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName));
//            }
            for (WxMemberRespVO vo : voList) {
                Long firstOrderStoreId = vo.getFirstOrderStoreId();
                if (storeMap.containsKey(firstOrderStoreId)) {
                    vo.setFirstOrderStoreName(storeMap.get(firstOrderStoreId));
                    vo.setOrgName(storeIdToOrgNameMap.get(firstOrderStoreId));
                }
                if (memberTagMap.containsKey(vo.getMemberId())) {
                    vo.setMemberTags(memberTagMap.get(vo.getMemberId()));
                }
                if(runnerBalanceMap.containsKey(vo.getMemberId())){
                    vo.setBalance(runnerBalanceMap.get(vo.getMemberId()));
                }

//                if(ObjectUtil.isNotEmpty(wxMemberReqVO.getCommunityFlag())){
//                    if(Objects.equals(wxMemberReqVO.getCommunityFlag(), WxMemberConstant.COMMUNITY_FLAG_1)){
//                        vo.setCommunityFlag(WxMemberConstant.COMMUNITY_FLAG_1);
//                    }else {
//                        vo.setCommunityFlag(WxMemberConstant.COMMUNITY_FLAG_2);
//                    }
//                }else {
//                    if(ObjectUtil.isEmpty(vo.getWxUnionid())){
//                        vo.setCommunityFlag(WxMemberConstant.COMMUNITY_FLAG_1);
//                    }else {
//                        if(memberInGroup.contains(vo.getWxUnionid())){
//                            vo.setCommunityFlag(WxMemberConstant.COMMUNITY_FLAG_2);
//                        }else {
//                            vo.setCommunityFlag(WxMemberConstant.COMMUNITY_FLAG_1);
//                        }
//                    }
//                }
                Long integralFrozen = vo.getIntegralFrozen();
                vo.setMemberLevel(5);
                vo.setGrade(5);
                if (ObjectUtil.isNotEmpty(wxMemberReqVO.getCrowdId())) {
                    queryWrapper.leftJoin(MemberCrowdRefDO.class, MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId);
                    queryWrapper.eq(MemberCrowdRefDO::getCrowdId, wxMemberReqVO.getCrowdId());
                }
                for (WxMemberCardDO wxMemberCard : list) {
                    if (integralFrozen <= wxMemberCard.getMaxPointsThreshold()) {
                        vo.setMemberLevel(wxMemberCard.getMemberLevel());
                        vo.setGrade(wxMemberCard.getMemberLevel());
                        break;
                    }
                }
            }
        }
        result.setList(voList);
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public void exportMemberList(WxMemberReqVO wxMemberReqVO, HttpServletRequest request, HttpServletResponse response) {
        AccessTimeRestrictionUtil.checkAccessTime();
        Page<WxMemberExcelRespVO> page = new Page<>(1, 1000000);
        //CommonResult<List<StoreInfoDTO>> commonResult = storeApi.getAllStoreList();
        CommonResult<Set<StoreOrgDTO>> commonResult = orgApi.getAllOrgListByStoreIdV2();
        List<MemberTagVO> memberTageListByMemberId = tagValueService.getMemberTageListByMemberId(List.of());
        Map<Long, BigDecimal> runnerBalanceByMemberId = errandRunnerApi.getRunnerBalanceByMemberId(List.of());

        Map<Long, String> memberToTagMap = new HashMap<>(8);
        Set<StoreOrgDTO> data = commonResult.getData();
        Map<Long, String> storeMap = new HashMap<>(8);
        Map<Long, String> storeIdToOrgNameMap = new HashMap<>(8);
        Map<Long, BigDecimal> balanceMap = new HashMap<>(8);
        if (CollectionUtil.isNotEmpty(data)) {
            storeMap = data.stream().collect(Collectors.toMap(StoreOrgDTO::getStoreId, StoreOrgDTO::getStoreName));

            storeIdToOrgNameMap = data.stream()
                    .filter(dto -> dto.getStoreId() != null)
                    .filter(dto -> dto.getOrgName() != null)
                    .collect(Collectors.toMap(
                            StoreOrgDTO::getStoreId,
                            StoreOrgDTO::getOrgName
                    ));
        }
        if (CollectionUtil.isNotEmpty(memberTageListByMemberId)) {
            memberToTagMap = memberTageListByMemberId.stream()
                    .collect(Collectors.toMap(
                            MemberTagVO::getMemberId,
                            MemberTagVO::getTagNames
                    ));
        }
        Map<Long, String> finalStoreMap = storeMap;
        Map<Long, String> orgMap = storeIdToOrgNameMap;
        Map<Long, String> memberTagMap = memberToTagMap;
        Map<Long, BigDecimal> runnerBalanceMap = runnerBalanceByMemberId;
        excelActionService.exportAsyncExcel(WxMemberExcelRespVO.class, page, param -> exportMemberService.getMemberExportList(param, wxMemberReqVO, finalStoreMap, orgMap, memberTagMap, runnerBalanceMap), "会员列表");
    }

    @Override
    public void updateLastLoginTime(WxMemberUpdateLoginTimeReqVO wxMember) {
        WxMemberDO wxMemberDO = new WxMemberDO();
        wxMemberDO.setMemberId(wxMember.getMemberId());
        wxMemberDO.setOpenid(wxMember.getOpenid());
        wxMemberDO.setWxUnionid(wxMember.getWxUnionid());
        makeShardingValue(wxMemberDO);

        UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .setSql("fourth_login_time = third_login_time")
                .setSql("third_login_time = second_login_time")
                .setSql("second_login_time = last_login_time")
                .setSql("last_login_time = now()")
                .lambda().eq(WxMemberDO::getShardingValue, wxMemberDO.getShardingValue())
                //.eq(WxMemberDO::getMemberId, wxMember.getMemberId())
                .eq(WxMemberDO::getOpenid, wxMember.getOpenid());
        if(ObjectUtil.isNotEmpty(wxMember.getWxUnionid())){
            updateWrapper.setSql("wx_unionid = '" + wxMemberDO.getWxUnionid() + "'");
        }
        wxMemberMapper.update(updateWrapper);
    }

    @Override
    public WxMemberInfoRespVO listByEntity(WxMemberInfoReqVO wxMember) {
        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotEmpty(wxMember.getBusinessId())) {
            queryWrapper.eq(WxMemberDO::getBusinessId, wxMember.getBusinessId());
        }
        if (ObjectUtil.isNotEmpty(wxMember.getMemberId())) {
            wxMember.setShardingValue(Integer.parseInt(String.valueOf(wxMember.getMemberId() % 10)));
            queryWrapper.eq(WxMemberDO::getMemberId, wxMember.getMemberId());
            queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        }
        if (ObjectUtil.isNotEmpty(wxMember.getOpenid())) {
            char letter = wxMember.getOpenid().charAt(wxMember.getOpenid().length() - 1);
            int number = StringUtils.convertLetterToNumberUsingASCII(letter);
            wxMember.setShardingValue(number % 10);
            queryWrapper.eq(WxMemberDO::getOpenid, wxMember.getOpenid());
            queryWrapper.eq(WxMemberDO::getShardingValue, wxMember.getShardingValue());
        }
        if (ObjectUtil.isNotEmpty(wxMember.getShardingValue())) {
            WxMemberDO wxMemberDO = wxMemberMapper.selectOne(queryWrapper);
            WxMemberInfoRespVO bean = BeanUtils.toBean(wxMemberDO, WxMemberInfoRespVO.class);
            if (ObjectUtil.isNotEmpty(wxMemberDO) && ObjectUtil.isNotEmpty(wxMemberDO.getMemberId())) {
                List<Long> crowdIds = wxMemberCrowdRefService.selectCrowdIdsByMemberId(wxMemberDO.getMemberId());
                bean.setCrowdIds(crowdIds);
            }
            return bean;
        } else {
            throw exception(WX_MEMBER_REJECT_QUERY);
        }
    }

    @Override
    public void updateFirstLogin(WxMemberUpdateFirstLoginReqVO wxMember) {
        WxMemberDO wxMemberDO = new WxMemberDO();
        wxMemberDO.setMemberId(wxMember.getMemberId());
        wxMemberDO.setOpenid(wxMember.getOpenid());
        makeShardingValue(wxMemberDO);
        UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(WxMemberDO::getShardingValue, wxMemberDO.getShardingValue()).eq(WxMemberDO::getMemberId, wxMember.getMemberId())
                //.set(WxMemberDO::getIsFirstLogin, wxMember.getIsFirstLogin())
                .set(WxMemberDO::getUpdateTime, new Date());
        wxMemberMapper.update(updateWrapper);
    }

    /**
     * 获取导出列表
     *
     * @param param         param
     * @param wxMemberReqVO wxMemberReqVO
     * @param storeMap      storeMap
     * @return WxMemberExcelRespVO
     */
    private List<WxMemberExcelRespVO> getMemberExportList(Page<WxMemberExcelRespVO> param, WxMemberReqVO wxMemberReqVO, Map<Long, String> storeMap) {

        boolean acquire = memberListLimiter.tryAcquire();
        if (!acquire) {
            throw exception(WX_MEMBER_GET_LIMITER);
        }
        // 会员卡等级
        List<WxMemberCardDO> list = wxMemberCardService.list();
        Map<Integer, WxMemberCardDO> collect = list.stream().collect(Collectors.toMap(WxMemberCardDO::getMemberLevel, c -> c));
        MPJLambdaWrapper<WxMemberDO> queryWrapper = new MPJLambdaWrapper<>();
        // 会员手机号
        if (StringUtils.isNotEmpty(wxMemberReqVO.getMemberMobile())) {
            queryWrapper.and(wrapper ->
                    wrapper.eq(WxMemberDO::getMemberMobile, wxMemberReqVO.getMemberMobile())
                            .or()
                            .eq(WxMemberDO::getMemberNickName, wxMemberReqVO.getMemberMobile())
            );
        }
        //性别
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getGender()), WxMemberDO::getGender, wxMemberReqVO.getGender());
        //首次下单门店
        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getAttribute()) && ObjectUtil.isNotEmpty(wxMemberReqVO.getAttribute())) {
            if (wxMemberReqVO.getAttribute().equals(WxMemberConstant.ATTRIBUTE_5)) {
                queryWrapper.eq(WxMemberDO::getFirstOrderStoreId, wxMemberReqVO.getDeptId());
            } else {
                CommonResult<List<Long>> commonResult = storeApi.getStoreIdsByAllOrgId(wxMemberReqVO.getDeptId());
                if (commonResult.getCode() == 0) {
                    throw new RuntimeException(commonResult.getMsg());
                } else {
                    List<Long> longs = JSON.parseArray(JSON.toJSONString(commonResult.getData()), Long.class);
                    if (CollectionUtil.isEmpty(longs)) {
                        return List.of();
                    }
                    queryWrapper.in(WxMemberDO::getFirstOrderStoreId, longs);
                }
            }
        }

        // 会员状态
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberStatus()), WxMemberDO::getMemberStatus, wxMemberReqVO.getMemberStatus());
        // 会员类型
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberType()), WxMemberDO::getUserIdentity, wxMemberReqVO.getMemberType());
        // 会员来源
        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberCategory()), WxMemberDO::getMemberCategory, wxMemberReqVO.getMemberCategory());
        // 注册时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterEndTime()), WxMemberDO::getRegisterTime, wxMemberReqVO.getRegisterStartTime(), wxMemberReqVO.getRegisterEndTime());
        // 最后登录时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginEndTime()), WxMemberDO::getLastLoginTime, wxMemberReqVO.getLastLoginStartTime(), wxMemberReqVO.getLastLoginEndTime());
        // 最后下单时间
        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderEndTime()), WxMemberDO::getFinalOrderFinishTime, wxMemberReqVO.getLastOrderStartTime(), wxMemberReqVO.getLastOrderEndTime());

        // 会员等级
        Integer grade = wxMemberReqVO.getGrade();
        if (ObjectUtils.isNotEmpty(grade)) {
            // 会员等级

            if (Objects.equals(grade, WxMemberConstant.GRADE_1)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.le(WxMemberDO::getIntegralFrozen, wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_2)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_3)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_4)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(grade, WxMemberConstant.GRADE_5)) {
                WxMemberCardDO wxMemberCard = collect.get(grade);
                queryWrapper.ge(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold());
            }
        }

        extracted(wxMemberReqVO, queryWrapper);
        // 倒序
        queryWrapper.orderByDesc("register_time");
        // 会员标签
        Integer memberLabel = wxMemberReqVO.getMemberLabel();
        if (ObjectUtil.isNotEmpty(memberLabel)) {
            queryWrapper.eq(WxMemberDO::getMemberLabel, memberLabel);
        }
        Page<WxMemberDO> page = wxMemberMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()), queryWrapper);

        List<WxMemberDO> records = page.getRecords();
        List<WxMemberExcelRespVO> voList = BeanUtils.toBean(records, WxMemberExcelRespVO.class);

        if (CollectionUtil.isNotEmpty(voList)) {
            for (WxMemberExcelRespVO vo : voList) {
                Long firstOrderStoreId = vo.getFirstOrderStoreId();
                if (storeMap.containsKey(firstOrderStoreId)) {
                    vo.setFirstOrderStoreName(storeMap.get(firstOrderStoreId));
                }
                Long integralFrozen = vo.getIntegralFrozen();
                vo.setGrade(5);
                for (WxMemberCardDO wxMemberCard : list) {
                    if (integralFrozen <= wxMemberCard.getMaxPointsThreshold()) {
                        vo.setGrade(wxMemberCard.getMemberLevel());
                        break;
                    }
                }
            }
        }
        return voList;
    }

    private static void extracted(WxMemberReqVO wxMemberReqVO, MPJLambdaWrapper<WxMemberDO> queryWrapper) {
        Integer orderFrequencyTime = wxMemberReqVO.getOrderFrequencyTime();
        Integer orderFrequency = wxMemberReqVO.getOrderFrequency();
        if ((ObjectUtil.isEmpty(orderFrequencyTime) && ObjectUtil.isNotEmpty(orderFrequency)) || (ObjectUtil.isNotEmpty(orderFrequencyTime) && ObjectUtil.isEmpty(orderFrequency))) {
            throw exception(WX_MEMBER_ORDER_FREQUENCY);
        } else {
            // 下单频次时间
            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_0)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getThirtyDayOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getThirtyDayOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_1)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getSevenDayOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getSevenDayOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_2)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getHalfYearOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getHalfYearOrderCount, 1);
                }
            }

            if (Objects.equals(orderFrequencyTime, WxMemberConstant.MEMBER_TIME_3)) {
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_0)) {
                    queryWrapper.gt(WxMemberDO::getOneYearOrderCount, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_1)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderCount, 2, 3);
                }
                if (Objects.equals(orderFrequency, WxMemberConstant.ORDER_COUNT_2)) {
                    queryWrapper.eq(WxMemberDO::getOneYearOrderCount, 1);
                }
            }
        }

        Integer orderAvg = wxMemberReqVO.getOrderAvg();
        Integer orderAvgTime = wxMemberReqVO.getOrderAvgTime();
        if ((ObjectUtil.isEmpty(orderAvg) && ObjectUtil.isNotEmpty(orderAvgTime)) || (ObjectUtil.isNotEmpty(orderAvg) && ObjectUtil.isEmpty(orderAvgTime))) {
            throw exception(WX_MEMBER_ORDER_AVG_TIME);
        } else {
            // 下单时间
            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_0)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getThirtyDayOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getThirtyDayOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_1)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getSevenDayOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getSevenDayOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_2)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 0, 8);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 8, 11);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 11, 14);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getHalfYearOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getHalfYearOrderAvg, 17);
                }
            }

            if (Objects.equals(orderAvgTime, WxMemberConstant.MEMBER_TIME_3)) {
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_0)) {
                    queryWrapper.lt(WxMemberDO::getOneYearOrderAvg, 3);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_1)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderAvg, 2, 3);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_2)) {
                    queryWrapper.eq(WxMemberDO::getOneYearOrderAvg, 1);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_3)) {
                    queryWrapper.between(WxMemberDO::getOneYearOrderAvg, 14, 17);
                }
                if (Objects.equals(orderAvg, WxMemberConstant.ORDER_AVG_4)) {
                    queryWrapper.gt(WxMemberDO::getOneYearOrderAvg, 17);
                }
            }
        }
        Integer orderInterval = wxMemberReqVO.getOrderInterval();
        if (ObjectUtil.isNotEmpty(orderInterval)) {
            switch (orderInterval) {
                case 0:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) < 10");
                    break;
                case 1:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) >= 10 and DATEDIFF(final_order_finish_time,second_order_finish_time) < 20");
                    break;
                case 2:
                    queryWrapper.apply(" DATEDIFF(final_order_finish_time,second_order_finish_time) >= 21 and DATEDIFF(final_order_finish_time,second_order_finish_time) < 30");
                    break;
                default:
                    break;
            }
        }
    }

    public Integer isMoreThan30Days(Date finalOrderFinishTime, Date orderPayTime) {
        if (ObjectUtils.isEmpty(finalOrderFinishTime)) {
            return 1;
        }
        long daysBetween = ChronoUnit.DAYS.between(finalOrderFinishTime.toInstant(), orderPayTime.toInstant());
        if (daysBetween > 30) {
            return 1;
        } else {
            return 0;
        }
    }


    @Override
    public void updatePointBatch(Long businessId, List<WxMemberDO> memberList) {
        if (CollectionUtils.isEmpty(memberList)) {
            return;
        }

        // 分批处理会员数据，每批10000条
        int batchSize = 10000;
        for (int i = 0; i < memberList.size(); i += batchSize) {
            List<WxMemberDO> batchList = memberList.subList(i, Math.min(i + batchSize, memberList.size()));

            // 使用批量更新替代单条记录处理
            for (WxMemberDO wxMember : batchList) {
                LambdaQueryWrapper<WxMemberDO> wxQw = new LambdaQueryWrapper<>();
                wxQw.eq(WxMemberDO::getBusinessId, businessId) // 添加businessId过滤
                        .eq(WxMemberDO::getMemberId, wxMember.getMemberId())
                        .eq(WxMemberDO::getShardingValue, wxMember.getMemberId() % 10);

                WxMemberDO existingMember = wxMemberMapper.selectOne(wxQw);

                if (existingMember != null && existingMember.getMemberIntegral() - wxMember.getMemberIntegral().intValue()>0) {
                    // 更新用户积分
                    existingMember.setMemberIntegral(existingMember.getMemberIntegral() - wxMember.getMemberIntegral().intValue());
                    wxMemberMapper.update(existingMember, wxQw);

                } else {
                    log.warn(">>> 未找到businessId={}, memberId={}的用户", businessId, wxMember.getMemberId());
                }
            }
        }

        // 清理积分记录
        pointsLogService.updatePointsLogWithOverDue(businessId);
    }

    @Override
    public WxMemberDO selectById(Long memberId) {
        LambdaQueryWrapper<WxMemberDO> wxQw = new LambdaQueryWrapper<>();
        wxQw.eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, memberId % 10);
        return wxMemberMapper.selectOne(wxQw);
    }

    @Override
    public WxMemberDO selectByIdAndBusinessId(Long memberId, String businessId) {
        LambdaQueryWrapper<WxMemberDO> wxQw = new LambdaQueryWrapper<>();
        wxQw.eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, memberId % 10)
                .eq(WxMemberDO::getBusinessId, businessId);
        return wxMemberMapper.selectOne(wxQw);
    }

    @Override
    public void updateMemberByJustId(Long memberId, WxMemberDO existingMember) {
        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, memberId % 10);

        wxMemberMapper.update(existingMember, updateWrapper);
    }

    @Override
    public void updateMemberByJustIdAndBusinessId(Long memberId, String businessId, WxMemberDO existingMember) {
        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, memberId % 10)
                .eq(WxMemberDO::getBusinessId, businessId);

        wxMemberMapper.update(existingMember, updateWrapper);
    }


    @Override
    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public CommonResult<List<WxMemberDataVO>> listWechat(long current, int size) {
        List<WxMemberCardDO> list = wxMemberCardService.list();
        Page<WxMemberDO> page = new Page<>(current, size);
        LambdaQueryWrapper<WxMemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMemberDO::getUserIdentity, 1);
        wrapper.eq(WxMemberDO::getState, 1);
        wrapper.eq(WxMemberDO::getMemberCategory, 0);
        IPage<WxMemberDO> wxMemberPage = wxMemberMapper.selectPage(page, wrapper);
        List<WxMemberDataVO> pageList = BeanCopyUtils.copyBeanList(wxMemberPage.getRecords(), WxMemberDataVO.class);
        if (ObjectUtil.isNotEmpty(pageList)) {
            for (WxMemberDataVO wxMemberDataVO : pageList) {
                Long integralFrozen = wxMemberDataVO.getIntegralFrozen();
                for (WxMemberCardDO wxMemberCard : list) {
                    if (ObjectUtil.isNotEmpty(wxMemberDataVO.getBusinessId()) && ObjectUtil.isNotEmpty(wxMemberCard.getBusinessId())) {
                        if (wxMemberDataVO.getBusinessId().equals(wxMemberCard.getBusinessId())) {
                            if (integralFrozen <= wxMemberCard.getMaxPointsThreshold() && integralFrozen >= wxMemberCard.getMinPointsThreshold()) {
                                wxMemberDataVO.setMemberLevel(wxMemberCard.getMemberLevel());
                                break;
                            }
                        }
                    }

                }
            }
        }
        return CommonResult.success(pageList);
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
            queryWrapper.eq("member_card_id", 1);
            wxMemberCardDO = wxMemberCardMapper.selectOne(queryWrapper);
            if (wxMemberCardDO != null) {
                BeanUtils.copyProperties(wxMemberCardDO, wxMemberCardDataRespVO);
            }
            if (wxMemberCardDataRespVO != null) {
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
                wxMemberVO.setBackgroundImage(wxMemberCardDataRespVO.getBackgroundImage());
                wxMemberVO.setMemberLevel(wxMemberCardDataRespVO.getMemberLevel());
                wxMemberVO.setDescription(wxMemberCardDataRespVO.getDescription());
                wxMemberVO.setIntegralFrozen(wxMemberDO.getFreezePoints());
                wxMemberVO.setMaxPointsThreshold(wxMemberCardDataRespVO.getMaxPointsThreshold());
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
        if (!org.springframework.util.StringUtils.isEmpty(wxMemberCardDOS)) {
            wxMemberCard = wxMemberCardDOS.get(0);
        }
        String backgroundImage = "";
        if (ObjectUtil.isNotEmpty(wxMemberCard) && ObjectUtil.isNotEmpty(wxMemberCard.getBackgroundImage())) {
            backgroundImage = wxMemberCard.getBackgroundImage();
            wxMemberVO.setMemberLevel(wxMemberCard.getMemberLevel());
            wxMemberVO.setDescription(wxMemberCard.getDescription());
            wxMemberVO.setMaxPointsThreshold(wxMemberCard.getMaxPointsThreshold());
        }
        wxMemberVO.setBackgroundImage(backgroundImage);
        return wxMemberVO;
    }

    @Override
    public List<WxMemberDTO> getMemberByMemberLevel(Integer memberLevel) {
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<>();
        // 会员卡等级
        List<WxMemberCardDO> list = wxMemberCardService.list();
        Map<Integer, WxMemberCardDO> collect = list.stream().collect(Collectors.toMap(WxMemberCardDO::getMemberLevel, c -> c));
        // 会员等级
        if (ObjectUtils.isNotEmpty(memberLevel)) {
            // 会员等级

            if (Objects.equals(memberLevel, WxMemberConstant.GRADE_1)) {
                WxMemberCardDO wxMemberCard = collect.get(memberLevel);
                queryWrapper.lambda().le(WxMemberDO::getIntegralFrozen, wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(memberLevel, WxMemberConstant.GRADE_2)) {
                WxMemberCardDO wxMemberCard = collect.get(memberLevel);
                queryWrapper.lambda().between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(memberLevel, WxMemberConstant.GRADE_3)) {
                WxMemberCardDO wxMemberCard = collect.get(memberLevel);
                queryWrapper.lambda().between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(memberLevel, WxMemberConstant.GRADE_4)) {
                WxMemberCardDO wxMemberCard = collect.get(memberLevel);
                queryWrapper.lambda().between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
            }
            if (Objects.equals(memberLevel, WxMemberConstant.GRADE_5)) {
                WxMemberCardDO wxMemberCard = collect.get(memberLevel);
                queryWrapper.lambda().ge(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold());
            }
        }
        List<WxMemberDO> wxMembers = wxMemberMapper.selectList(queryWrapper);
        return BeanUtils.toBean(wxMembers, WxMemberDTO.class);
    }


    @Override
    public Boolean updateHistoryOrderData() {

        List<BzOrderDTO> oneYear = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(366), DateUtils.getDaysBeforeEnd(366));
        // 查询180days订单数据
        List<BzOrderDTO> halfYear = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(181), DateUtils.getDaysBeforeEnd(181));

        List<BzOrderDTO> thirtyDays = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(31), DateUtils.getDaysBeforeEnd(31));

        List<BzOrderDTO> sevenDays = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(8), DateUtils.getDaysBeforeEnd(8));

        List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderData(DateUtils.getDaysBeforeStart(1), DateUtils.getDaysBeforeEnd(1));

        Map<Long, BzOrderDTO> yesterdayMap = yesterday.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
        Map<Long, BzOrderDTO> oneYearMap = oneYear.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
        Map<Long, BzOrderDTO> halfYearMap = halfYear.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
        Map<Long, BzOrderDTO> thirtyDaysMap = thirtyDays.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));
        Map<Long, BzOrderDTO> sevenDaysMap = sevenDays.stream().collect(Collectors.toMap(BzOrderDTO::getMemberId, item -> item));

        List<BzOrderDTO> listAll = new ArrayList<>();
        listAll.addAll(oneYear);
        listAll.addAll(halfYear);
        listAll.addAll(thirtyDays);
        listAll.addAll(sevenDays);
        listAll.addAll(yesterday);
        List<Long> memberIds = listAll.stream().map(BzOrderDTO::getMemberId).distinct().collect(Collectors.toList());
        System.gc();
        List<List<Long>> split = ListUtil.split(memberIds, 10);
        Date date = new Date();
        UpdateWrapper<WxMemberDO> wxMemberUpdateChain = new UpdateWrapper<>();
        for (List<Long> longs : split) {
//            threadPoolTaskExecutor.submit(() -> {
            for (Long memberId : longs) {
                try {
                    wxMemberUpdateChain = new UpdateWrapper<>();
                    wxMemberUpdateChain.lambda().eq(WxMemberDO::getUpdateTime, date)
                            .eq(WxMemberDO::getShardingValue, memberId % 10)
                            .eq(WxMemberDO::getMemberId, memberId);

                    int yestCount = 0;
                    BigDecimal yestSum = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(yesterday)) {
                        if (yesterdayMap.containsKey(memberId)) {
                            yestCount = yesterdayMap.get(memberId).getCount();
                            yestSum = yesterdayMap.get(memberId).getSum();
                            wxMemberUpdateChain.setSql("total_order_num = total_order_num +" + yestCount);
                            // one_year_order_sum

                            BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
                            if (ObjectUtil.isEmpty(BzOrderDTO)) {
                                BzOrderDTO = new BzOrderDTO();
                                BzOrderDTO.setSum(BigDecimal.ZERO);
                                BzOrderDTO.setCount(0);
                            }
                            wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                    .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                    .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                            + "(one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");

                            // half_year_order_sum
                            BzOrderDTO BzOrderDTO1 = halfYearMap.get(memberId);
                            if (ObjectUtil.isEmpty(BzOrderDTO1)) {
                                BzOrderDTO1 = new BzOrderDTO();
                                BzOrderDTO1.setSum(BigDecimal.ZERO);
                                BzOrderDTO1.setCount(0);
                            }
                            wxMemberUpdateChain.setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO1.getSum() + "+" + yestSum)
                                    .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO1.getCount() + "+" + yestCount)
                                    .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO1.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(half_year_order_sum -" + BzOrderDTO1.getSum() + "+" + yestSum + ") / "
                                            + "(half_year_order_count -" + BzOrderDTO1.getCount() + "+" + yestCount + "), 2) END");

                            // thirty_day_order_sum

                            BzOrderDTO BzOrderDTO2 = thirtyDaysMap.get(memberId);
                            if (ObjectUtil.isEmpty(BzOrderDTO2)) {
                                BzOrderDTO2 = new BzOrderDTO();
                                BzOrderDTO2.setSum(BigDecimal.ZERO);
                                BzOrderDTO2.setCount(0);
                            }
                            wxMemberUpdateChain.setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO2.getSum() + "+" + yestSum)
                                    .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO2.getCount() + "+" + yestCount)
                                    .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO2.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(thirty_day_order_sum -" + BzOrderDTO2.getSum() + "+" + yestSum + ") / "
                                            + "(thirty_day_order_count -" + BzOrderDTO2.getCount() + "+" + yestCount + "), 2) END");

                            // seven_day_order_sum

                            BzOrderDTO BzOrderDTO3 = thirtyDaysMap.get(memberId);
                            if (ObjectUtil.isEmpty(BzOrderDTO3)) {
                                BzOrderDTO3 = new BzOrderDTO();
                                BzOrderDTO3.setSum(BigDecimal.ZERO);
                                BzOrderDTO3.setCount(0);
                            }
                            wxMemberUpdateChain.setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO3.getSum() + "+" + yestSum)
                                    .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO3.getCount() + "+" + yestCount)
                                    .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO3.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(seven_day_order_sum -" + BzOrderDTO3.getSum() + "+" + yestSum + ") / "
                                            + "(seven_day_order_count -" + BzOrderDTO3.getCount() + "+" + yestCount + "), 2) END");

                        } else {
                            yestCount = 0;
                            yestSum = BigDecimal.ZERO;
                            // one_year_order_sum
                            if (oneYearMap.containsKey(memberId)) {
                                BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
                                wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                        .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                        .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                                "ELSE ROUND(" + "(one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                                + "(one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                            }
                            // half_year_order_sum
                            if (halfYearMap.containsKey(memberId)) {
                                BzOrderDTO BzOrderDTO = halfYearMap.get(memberId);
                                wxMemberUpdateChain.setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                        .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                        .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                                "ELSE ROUND(" + "(half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                                + "(half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                            }
                            // thirty_day_order_sum
                            if (thirtyDaysMap.containsKey(memberId)) {
                                BzOrderDTO BzOrderDTO = thirtyDaysMap.get(memberId);
                                wxMemberUpdateChain.setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                        .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                        .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                                "ELSE ROUND(" + "(thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                                + "(thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                            }
                            // seven_day_order_sum
                            if (sevenDaysMap.containsKey(memberId)) {
                                BzOrderDTO BzOrderDTO = sevenDaysMap.get(memberId);
                                wxMemberUpdateChain.setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                        .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                        .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                                "ELSE ROUND(" + "(seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                                + "(seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                            }
                        }
                    } else {
                        yestCount = 0;
                        yestSum = BigDecimal.ZERO;
                        // one_year_order_sum
                        if (oneYearMap.containsKey(memberId)) {
                            BzOrderDTO BzOrderDTO = oneYearMap.get(memberId);
                            wxMemberUpdateChain.setSql("one_year_order_sum = one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                    .setSql("one_year_order_count = one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                    .setSql("one_year_order_avg = CASE WHEN  one_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(one_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                            + "(one_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                        }
                        // half_year_order_sum
                        if (halfYearMap.containsKey(memberId)) {
                            BzOrderDTO BzOrderDTO = halfYearMap.get(memberId);
                            wxMemberUpdateChain.setSql("half_year_order_sum = half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                    .setSql("half_year_order_count = half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                    .setSql("half_year_order_avg = CASE WHEN  half_year_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(half_year_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                            + "(half_year_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                        }
                        // thirty_day_order_sum
                        if (thirtyDaysMap.containsKey(memberId)) {
                            BzOrderDTO BzOrderDTO = thirtyDaysMap.get(memberId);
                            wxMemberUpdateChain.setSql("thirty_day_order_sum = thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                    .setSql("thirty_day_order_count = thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                    .setSql("thirty_day_order_avg = CASE WHEN  thirty_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(thirty_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                            + "(thirty_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                        }
                        // seven_day_order_sum
                        if (sevenDaysMap.containsKey(memberId)) {
                            BzOrderDTO BzOrderDTO = sevenDaysMap.get(memberId);
                            wxMemberUpdateChain.setSql("seven_day_order_sum = seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum)
                                    .setSql("seven_day_order_count = seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount)
                                    .setSql("seven_day_order_avg = CASE WHEN  seven_day_order_count - " + BzOrderDTO.getCount() + " + " + yestSum + "= 0 THEN 0 " +
                                            "ELSE ROUND(" + "(seven_day_order_sum -" + BzOrderDTO.getSum() + "+" + yestSum + ") / "
                                            + "(seven_day_order_count -" + BzOrderDTO.getCount() + "+" + yestCount + "), 2) END");
                        }
                    }
                    wxMemberMapper.update(wxMemberUpdateChain);
                } catch (Exception e) {
                    log.warn("更新会员数据失败：{}", e.getMessage());
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateMemberLabel() {
        for (int i = 0; i < 10; i++) {
            final int index = i;
            //threadPoolTaskExecutor.submit(() -> {
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .setSql("member_label = CASE  WHEN  TIMESTAMPDIFF(DAY,fourth_login_time, NOW()) <= 7 THEN 1 " +
                            " WHEN TIMESTAMPDIFF(DAY,third_order_finish_time, NOW()) <= 7 THEN 2 " +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 7 AND 14 THEN 3" +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 14 AND 30 THEN 4" +
                            " WHEN TIMESTAMPDIFF(DAY,final_order_finish_time, NOW()) BETWEEN 30 AND 90 THEN 5" +
                            "        ELSE 6" +
                            " END")
                    .lambda().eq(WxMemberDO::getShardingValue, index);
            wxMemberMapper.update(updateWrapper);
            //});
        }
        return true;
    }


    @Override
    @DS(DsNameConstants.SHARDING)
    public void updateFirstOrderStoreId() {
        LocalDateTime yesterdayStart = LocalDate.now()
                .minusDays(1)
                .atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now()
                .minusDays(1)
                .atTime(23, 59, 59);
        //List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderStoreData(DateUtils.getAfterDayDateToString(1), DateUtils.getAfterDayDate(1));
        List<BzOrderDTO> yesterday = bzOrderApi.selectBzOrderStoreData(yesterdayStart, yesterdayEnd);
        Map<Long, Long> firstStoreId = yesterday.stream()
                .collect(Collectors.groupingBy(BzOrderDTO::getMemberId,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(BzOrderDTO::getCreateTime)),
                                list -> list.map(BzOrderDTO::getStoreId).orElse(null)
                        )
                ));

        for (Map.Entry<Long, Long> entry : firstStoreId.entrySet()) {
            Long key = entry.getKey();
            Long value = entry.getValue();
            UpdateWrapper<WxMemberDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("first_order_store_id = case when first_order_store_id = 0 then " + value + " else first_order_store_id end");
            updateWrapper.lambda().eq(WxMemberDO::getMemberId, key).eq(WxMemberDO::getShardingValue, key % 10);
            wxMemberMapper.update(updateWrapper);
        }
    }

    @Override
    public Boolean saveNoticeReserveMember(NoticeReserveMemberReqVO reqVO) {
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

        String[] arr = reqVO.getReserverTemplateType().split(",");
        for (String reserverTemplateType : arr) {
            NoticeReserveMemberDocument saveNoticReserveMember = new NoticeReserveMemberDocument();
            BeanUtils.copyProperties(reqVO, saveNoticReserveMember);
            saveNoticReserveMember.setReserverTemplateType(reserverTemplateType);
            Number number = identifierGenerator.nextId(null);
            saveNoticReserveMember.setId(number.longValue());

            bulkBuilder.operations(op -> op
                    .index(IndexOperation.of(io -> io
                            .index("wx_member_reserver")
                            .document(saveNoticReserveMember)
                    ))
            );
        }
        BulkRequest bulkRequest = bulkBuilder.build();
        try {
            BulkResponse response = elasticsearchClient.bulk(bulkRequest);
            return response.errors();
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @Override
    public Set<String> getSmsPhone(Integer key, List<Long> value) {
        return wxMemberMapper.getSmsPhone(key, value);
    }


    @Override
    public List<WxMemberDTO> getMembers() {
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMemberDO::getUserIdentity, 1);
        List<WxMemberDO> wxMemberDOList = wxMemberMapper.selectList(queryWrapper);
        return BeanUtils.toBean(wxMemberDOList, WxMemberDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<WxMemberDayDTO> getDayMembers(int shardingValue) {
        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WxMemberDO::getUserIdentity, 1);
        queryWrapper.lambda().eq(WxMemberDO::getShardingValue, shardingValue);
        List<WxMemberDO> wxMemberDOList = wxMemberMapper.selectList(queryWrapper);
        WxMemberDayDTO wxMemberDayDTO = new WxMemberDayDTO();
        List<WxMemberDayDTO> result = new ArrayList<>(wxMemberDOList.size());
        for (WxMemberDO wxMemberDO : wxMemberDOList) {
            wxMemberDayDTO = new WxMemberDayDTO();
            wxMemberDayDTO.setMemberId(wxMemberDO.getMemberId());
            wxMemberDayDTO.setMemberMobile(wxMemberDO.getMemberMobile());
            wxMemberDayDTO.setMemberNickName(wxMemberDO.getMemberNickName());
            result.add(wxMemberDayDTO);
        }
        return result;
    }

    @Override
    public Boolean sendCoupon(WxMemberSendReqVO wxMemberSendReqVO) {
//        // 限流
//        boolean acquire = memberListLimiter.tryAcquire();
//        if (!acquire) {
//            throw exception(WX_MEMBER_GET_LIMITER);
//        }
//        // 会员卡等级
//        List<WxMemberCardDO> list = wxMemberCardService.list();
//        Map<Integer, WxMemberCardDO> collect = list.stream().collect(Collectors.toMap(WxMemberCardDO::getMemberLevel, c -> c));
//        MPJLambdaWrapper<WxMemberDO> queryWrapper = new MPJLambdaWrapper<>();
//        // 会员手机号
//        queryWrapper.eq(StringUtils.isNotEmpty(wxMemberReqVO.getMemberMobile()), WxMemberDO::getMemberMobile, wxMemberReqVO.getMemberMobile());
//        //性别
//        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getGender()), WxMemberDO::getGender, wxMemberReqVO.getGender());
//        //首次下单门店
//        if (ObjectUtil.isNotEmpty(wxMemberReqVO.getDeptId())) {
//            if(ObjectUtil.isEmpty(wxMemberReqVO.getIsStore())){
//                throw exception(WX_MEMBER_IS_STORE_NOT_NULL);
//            }
//            if(Objects.equals(wxMemberReqVO.getIsStore(), WxMemberConstant.IS_STORE_0)){
//                CommonResult<List<Long>> commonResult = storeApi.getStoreIdsByAllOrgId(wxMemberReqVO.getDeptId());
//                List<Long> longs = JSON.parseArray(JSON.toJSONString(commonResult.getData()), Long.class);
//                if (CollectionUtil.isEmpty(longs)) {
//                    return Boolean.FALSE;
//                }
//                queryWrapper.in(WxMemberDO::getFirstOrderStoreId, longs);
//            }else {
//                queryWrapper.eq(WxMemberDO::getFirstOrderStoreId, wxMemberReqVO.getDeptId());
//            }
//
//        }
//
//        // 会员状态
//        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberStatus()),WxMemberDO::getMemberStatus, wxMemberReqVO.getMemberStatus());
//        // 会员类型
//        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberType()),WxMemberDO::getUserIdentity, wxMemberReqVO.getMemberType());
//        // 会员来源
//        queryWrapper.eq(ObjectUtils.isNotEmpty(wxMemberReqVO.getMemberCategory()),WxMemberDO::getMemberCategory, wxMemberReqVO.getMemberCategory());
//        // 注册时间
//        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getRegisterEndTime()),WxMemberDO::getRegisterTime, wxMemberReqVO.getRegisterStartTime(), wxMemberReqVO.getRegisterEndTime());
//        // 最后登录时间
//        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastLoginEndTime()),WxMemberDO::getLastLoginTime, wxMemberReqVO.getLastLoginStartTime(), wxMemberReqVO.getLastLoginEndTime());
//        // 最后下单时间
//        queryWrapper.between(ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderStartTime()) && ObjectUtils.isNotEmpty(wxMemberReqVO.getLastOrderEndTime()),WxMemberDO::getFinalOrderFinishTime, wxMemberReqVO.getLastOrderStartTime(), wxMemberReqVO.getLastOrderEndTime());
//
//        // 会员等级
//        Integer grade = wxMemberReqVO.getGrade();
//        if (ObjectUtils.isNotEmpty(grade)) {
//            // 会员等级
//
//            if (Objects.equals(grade, WxMemberConstant.GRADE_1)) {
//                WxMemberCardDO wxMemberCard = collect.get(grade);
//                queryWrapper.le(WxMemberDO::getIntegralFrozen, wxMemberCard.getMaxPointsThreshold());
//            }
//            if (Objects.equals(grade, WxMemberConstant.GRADE_2)) {
//                WxMemberCardDO wxMemberCard = collect.get(grade);
//                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
//            }
//            if (Objects.equals(grade, WxMemberConstant.GRADE_3)) {
//                WxMemberCardDO wxMemberCard = collect.get(grade);
//                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
//            }
//            if (Objects.equals(grade, WxMemberConstant.GRADE_4)) {
//                WxMemberCardDO wxMemberCard = collect.get(grade);
//                queryWrapper.between(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold(), wxMemberCard.getMaxPointsThreshold());
//            }
//            if (Objects.equals(grade, WxMemberConstant.GRADE_5)) {
//                WxMemberCardDO wxMemberCard = collect.get(grade);
//                queryWrapper.ge(WxMemberDO::getIntegralFrozen, wxMemberCard.getMinPointsThreshold());
//            }
//        }
//        extracted(wxMemberReqVO,queryWrapper);
//        // 倒序
//        queryWrapper.orderByDesc("register_time");
//        // 会员标签
//        Integer memberLabel = wxMemberReqVO.getMemberLabel();
//        if (ObjectUtil.isNotEmpty(memberLabel)) {
//            queryWrapper.eq(WxMemberDO::getMemberLabel, memberLabel);
//        }
//        Integer pageNo = wxMemberReqVO.getPageNo();
//        if(pageNo > 1000){
//            throw exception(WX_MEMBER_PAGE_NO_LIMIT);
//        }
//        queryWrapper.select(WxMemberDO::getMemberId, WxMemberDO::getMemberMobile,WxMemberDO::getMemberName);
//        if(ObjectUtil.isNotEmpty(wxMemberReqVO.getCrowdId())){
//            queryWrapper.leftJoin(MemberCrowdRefDO.class, MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId);
//            queryWrapper.eq(MemberCrowdRefDO::getCrowdId, wxMemberReqVO.getCrowdId());
//        }
////        queryWrapper.leftJoin(MemberCrowdRefDO.class, "cmr",
////                (cb, main) -> cb.eq(MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId)
////                        .eq(MemberCrowdRefDO::getCrowdId, wxMemberReqVO.getCrowdId())
////        );
//        //List<WxMemberDO> members = wxMemberMapper.selectJoinList(WxMemberDO.class,queryWrapper);
//        List<WxMemberDO> members = wxMemberMapper.selectList(queryWrapper);
        List<WxMemberCouponReqVO> members = wxMemberSendReqVO.getMembers();
        if (CollectionUtil.isEmpty(members) || members.size() > 500) {
            throw exception(WX_MEMBER_SEND_COUPON_SIZE);
        }
        Set<Long> memberIds = members.stream().map(m -> m.getMemberId()).collect(Collectors.toSet());
        Set<MemberCouponDTO> result = members.stream().map(m -> {
            MemberCouponDTO memberCouponDTO = new MemberCouponDTO();
            memberCouponDTO.setMemberId(m.getMemberId());
            memberCouponDTO.setMemberMobile(m.getMemberMobile());
            memberCouponDTO.setMemberName(m.getMemberNickName());
            return memberCouponDTO;
        }).collect(Collectors.toSet());
        Integer couponType = wxMemberSendReqVO.getCouponType();
        if (Objects.equals(couponType, 1)) {
            userCouponApi.sendCoupon(result, wxMemberSendReqVO.getCouponId(), wxMemberSendReqVO.getSendNum());
        } else {
            userCouponApi.sendPackage(result, wxMemberSendReqVO.getCouponId(), wxMemberSendReqVO.getSendNum());
        }

        return Boolean.TRUE;
    }

    @Override
    public List<Long> getMemberWithCrowd(CustomCrowdDO customCrowdDO) {

        LambdaQueryWrapper<WxMemberDO> memberWrapper = new LambdaQueryWrapper<>();

        // 构建基础信息 搜索对象
        // 1.会员性别
        int gender = 0;
        Integer crowdGender = customCrowdDO.getGender();
        if (crowdGender != null && crowdGender != 0) {
            gender = switch (crowdGender) {
                case 2 -> 1;
                case 3 -> 2;
                default -> 0;
            };
            // 封装性别信息
            if (gender != 0) {
                memberWrapper.eq(WxMemberDO::getGender, gender);
            }

        }
        // 2.会员生日
        Integer birthdayType = customCrowdDO.getBirthdayType();
        if (birthdayType != null && birthdayType != 0) {
            String start;
            String end;
            String birthdayValue = customCrowdDO.getBirthdayValue();
            if (birthdayType == 1) {
                // 人群配置了会员生日选项 生日为区间段
                start = birthdayValue.split("-")[0];
                end = birthdayValue.split("-")[1];
            } else if (birthdayType == 2) {
                // 人群配置了会员生日选项 生日为相对时间 最近 包含今天
                start = getOldStartTimestamp(Integer.parseInt(birthdayValue));
                end = getNewtartTimestamp(0);

            } else if (birthdayType == 3) {
                start = getNewtartTimestamp(1);
                end = getNewtartTimestamp(Integer.parseInt(birthdayValue));
            } else {
                start = null;
                end = null;
            }
            // 封装会员生日信息
            if (ObjectUtils.isEmpty(start) || ObjectUtils.isEmpty(end)) {
                // 抛出异常
                throw exception(MEMBER_CROWD_BIRTHDAY_NULL_ERROR);
            }

            // 定义日期格式化器（处理 MM/dd 格式）
            DateTimeFormatter mmddFormatter = DateTimeFormatter.ofPattern("MM/dd");

            try {
                // 生日格式校验
                TemporalAccessor startTA = mmddFormatter.parse(start);
                TemporalAccessor endTA = mmddFormatter.parse(end);
                // 补全年份构建LocalDate（用于判断是否跨年）
                int currentYear = LocalDate.now().getYear();
                LocalDate startDate = LocalDate.of(
                        currentYear,
                        Month.from(startTA),
                        startTA.get(ChronoField.DAY_OF_MONTH)
                );
                LocalDate endDate = LocalDate.of(
                        currentYear,
                        Month.from(endTA),
                        endTA.get(ChronoField.DAY_OF_MONTH)
                );
                // 处理跨年度的情况（如 start=12/20，end=01/10）
                if (startDate.isAfter(endDate)) {
                    // 跨年：生日 >= start 或生日 <= end（匹配今年start到年底 或 明年年初到end）
                    memberWrapper.and(wrapper -> wrapper
                            .apply("DATE_FORMAT(member_birthday, '%m/%d') >= {0}", start)
                            .or()
                            .apply("DATE_FORMAT(member_birthday, '%m/%d') <= {0}", end)
                    );
                } else {
                    // 不跨年：生日在 [start, end] 之间
                    memberWrapper.apply("DATE_FORMAT(member_birthday, '%m/%d') BETWEEN {0} AND {1}",
                            start, end);
                }
            } catch (Exception e) {
                // 日期格式解析失败，忽略该条件（或添加日志）
                log.error("生日区间格式错误，start={}, end={}", start, end, e);
            }
        }
        // 3.会员等级
        String memberLevel = customCrowdDO.getMemberLevel();
        if (StringUtils.isNotBlank(memberLevel) && !"0".equals(memberLevel)) {
            List<String> cardIds = toListOfString(memberLevel);
            LambdaQueryWrapper<WxMemberCardDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WxMemberCardDO::getBusinessId, customCrowdDO.getBusinessId());
            wrapper.in(WxMemberCardDO::getMemberCardId, cardIds);
            wrapper.select(WxMemberCardDO::getMinPointsThreshold, WxMemberCardDO::getMaxPointsThreshold);
            List<WxMemberCardDO> list = wxMemberCardService.list(wrapper);

            if (CollectionUtils.isNotEmpty(list)) {
                memberWrapper.and(orWrapper -> {
                    boolean isFirst = true;
                    for (WxMemberCardDO card : list) {
                        Integer min = card.getMinPointsThreshold();
                        Long max = card.getMaxPointsThreshold();

                        if (isFirst) {
                            // 第一个条件直接 between，无多余 OR
                            orWrapper.between(WxMemberDO::getMemberIntegral, min, max);
                            isFirst = false;
                        } else {
                            // 后续条件用 OR 连接
                            orWrapper.or().between(WxMemberDO::getMemberIntegral, min, max);
                        }
                    }
                });
            }

            // 封装会员等级
            /*for (int i = 0; i < list.size(); i++) {
                Integer min = list.get(i).getMinPointsThreshold();
                Long max = list.get(i).getMaxPointsThreshold();

                // 避免空值导致的查询异常
                if (min == null || max == null) {
                    continue;
                }

                // 第一个条件用AND，后续用OR连接
                if (i == 0) {
                    memberWrapper.between(WxMemberDO::getMemberIntegral, min, max);
                } else {
                    memberWrapper.or().between(WxMemberDO::getMemberIntegral, min, max);
                }
            }*/
        }

        // 4.会员标签 涉及人群标签表 获取标签   通过标签会员关系表 确认会员
        List<Long> memberIdsByTags = null;

        Integer memberTag = customCrowdDO.getMemberTag();
        if (memberTag != null && memberTag == 1) {
            List<Long> tags = crowdTagService.getTagsByCrowdId(customCrowdDO.getId());
            // 通过标签集合获取 标签下会员集合
            if (CollectionUtil.isNotEmpty(tags)) {
                memberIdsByTags = customTagService.getMembersByTags(tags);
            } else {
                return List.of();
            }
        }


        // 5.注册平台 人群表 1微信 2支付宝(多选)  会员表 0 微信 1支付宝
        String category = null;
        String memberCategory = customCrowdDO.getMemberCategory();
        if (memberCategory != null) {
            category = switch (memberCategory) {
                case "1" -> "0";
                case "2" -> "1";
                default -> null;
            };
            // 封装注册平台信息
            if (category != null) {
                memberWrapper.eq(WxMemberDO::getMemberCategory, category);
            }
        }

        // 6.会员积分
        Integer memberIntegralType = customCrowdDO.getMemberIntegralType();
        int minIntegral = 0;
        int maxIntegral = 0;
        if (memberIntegralType != null && memberIntegralType != 0) {
            String memberIntegralValue = customCrowdDO.getMemberIntegralValue();
            if (memberIntegralType == 1) {
                // 区间
                minIntegral = Integer.parseInt(memberIntegralValue.split("-")[0]);
                maxIntegral = Integer.parseInt(memberIntegralValue.split("-")[1]);
                memberWrapper.between(WxMemberDO::getMemberIntegral, minIntegral, maxIntegral);
            } else if (memberIntegralType == 2) {
                // 小于
                maxIntegral = Integer.parseInt(memberIntegralValue);
                memberWrapper.lt(WxMemberDO::getMemberIntegral, maxIntegral);
            } else if (memberIntegralType == 3) {
                // 大于
                minIntegral = Integer.parseInt(memberIntegralValue);
                memberWrapper.gt(WxMemberDO::getMemberIntegral, minIntegral);
            }

        }
        // 7.所属门店
        Integer belongStore = customCrowdDO.getBelongStore();
        if (belongStore != null && belongStore == 1) {
            List<Long> stores = crowdStoreService.getStoreIdsByCrowdId(customCrowdDO.getId());
            // 通过门店集合获取 门店下会员集合 first_order_store_id  会员表 首次下单门店ID
            if (CollectionUtil.isNotEmpty(stores)) {
                memberWrapper.in(WxMemberDO::getFirstOrderStoreId, stores);
            } else {
                return List.of();
            }
        }
        // 获取人群是否选择了客户分析
        Integer customAnalysis = customCrowdDO.getCustomAnalysis();
        if (customAnalysis != null && customAnalysis == 1) {
            // 8.回购周期 会员表最后一次订单完成时间finalOrderFinishTime 与 倒数第二次下单时间secondOrderFinishTime 之差
            Integer repurchaseType = customCrowdDO.getRepurchaseType();
            if (repurchaseType != null && repurchaseType != 0) {
                String repurchaseValue = customCrowdDO.getRepurchaseValue();
                int repurchaseMinDay;
                int repurchaseMaxDay;
                // 新增回购周期统计时间范围
                Integer censusDateType = customCrowdDO.getCensusDateType();
                LocalDateTime start = null;
                LocalDateTime end = null;

                if (censusDateType != null && censusDateType != 0) {
                    String censusDateValue = customCrowdDO.getCensusDateValue();
                    if (censusDateType == 1) {
                        // 指定时间 0 近7天    1 近30天  2 近180天半年
                        int count = switch (censusDateValue) {
                            case "0" -> 7;
                            case "1" -> 30;
                            case "2" -> 180;
                            default -> 0;
                        };
                        if (count > 0) {
                            end = LocalDateTime.now();
                            start = end.minusDays(count)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0);
                        }
                    } else if (censusDateType == 2) {
                        // 自定义时间 如2025/05/01 转格式yyyy-MM-dd'T'HH:mm:ss
                        String censusDateValueStart = censusDateValue.split("-")[0];
                        String censusDateValueEnd = censusDateValue.split("-")[1];
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                        // 字符串 → LocalDate（仅日期）
                        LocalDate startDate = LocalDate.parse(censusDateValueStart, dateFormatter);
                        LocalDate endDate = LocalDate.parse(censusDateValueEnd, dateFormatter);

                        // LocalDate → LocalDateTime（补充时间为 00:00:00）
                        start = startDate.atStartOfDay();
                        end = endDate.atTime(23, 59, 59);

                    }
                    memberWrapper.le(WxMemberDO::getFinalOrderFinishTime, end);
                    memberWrapper.ge(WxMemberDO::getSecondOrderFinishTime, start);
                }
                if (repurchaseType == 1) {
                    // 区间
                    repurchaseMinDay = Integer.parseInt(repurchaseValue.split("-")[0]);
                    repurchaseMaxDay = Integer.parseInt(repurchaseValue.split("-")[1]);
                    // 计算时间差（天数）：TIMESTAMPDIFF(DAY, 倒数第二次时间, 最后一次时间)
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, second_order_finish_time, final_order_finish_time) BETWEEN {0} AND {1}",
                                    repurchaseMinDay, repurchaseMaxDay)
                            // 确保两个时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                            .isNotNull(WxMemberDO::getSecondOrderFinishTime)
                    );
                } else if (repurchaseType == 2) {
                    // 小于
                    repurchaseMaxDay = Integer.parseInt(repurchaseValue);
                    // 计算时间差（天数）：TIMESTAMPDIFF(DAY, 倒数第二次时间, 最后一次时间)
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, second_order_finish_time, final_order_finish_time) < {0}",
                                    repurchaseMaxDay)
                            // 确保两个时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                            .isNotNull(WxMemberDO::getSecondOrderFinishTime)
                    );
                } else if (repurchaseType == 3) {
                    // 大于
                    repurchaseMinDay = Integer.parseInt(repurchaseValue);
                    // 计算时间差（天数）：TIMESTAMPDIFF(DAY, 倒数第二次时间, 最后一次时间)
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, second_order_finish_time, final_order_finish_time) > {0}",
                                    repurchaseMinDay)
                            // 确保两个时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                            .isNotNull(WxMemberDO::getSecondOrderFinishTime)
                    );
                }

            }
            // 9.末次距今下单时间
            Integer finalRepurchaseType = customCrowdDO.getFinalRepurchaseType();
            if (finalRepurchaseType != null && finalRepurchaseType != 0) {
                String finalRepurchaseValue = customCrowdDO.getFinalRepurchaseValue();
                int finalRepurchaseMinDay;
                int finalRepurchaseMaxDay;
                if (finalRepurchaseType == 1) {
                    // 区间
                    finalRepurchaseMinDay = Integer.parseInt(finalRepurchaseValue.split("-")[0]);
                    finalRepurchaseMaxDay = Integer.parseInt(finalRepurchaseValue.split("-")[1]);
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, final_order_finish_time, now()) BETWEEN {0} AND {1}",
                                    finalRepurchaseMinDay, finalRepurchaseMaxDay)
                            // 确保时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                    );

                } else if (finalRepurchaseType == 2) {
                    // 小于
                    finalRepurchaseMaxDay = Integer.parseInt(finalRepurchaseValue);
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, final_order_finish_time, now()) < {0}",
                                    finalRepurchaseMaxDay)
                            // 确保时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                    );
                } else if (finalRepurchaseType == 3) {
                    // 大于
                    finalRepurchaseMinDay = Integer.parseInt(finalRepurchaseValue);
                    memberWrapper.and(wrapper -> wrapper
                            .apply("TIMESTAMPDIFF(DAY, final_order_finish_time, now()) > {0}",
                                    finalRepurchaseMinDay)
                            // 确保时间字段不为空（避免计算异常）
                            .isNotNull(WxMemberDO::getFinalOrderFinishTime)
                    );
                }

            }
        }
        memberWrapper.eq(WxMemberDO::getBusinessId, customCrowdDO.getBusinessId());

        // 新增：近一年的lastLoginTime条件（lastLoginTime >= 一年前的今天）
        // 计算一年前的日期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1); // 当前时间减去1年
        Date oneYearAgo = calendar.getTime();

        // 添加时间范围条件：lastLoginTime >= 一年前的今天
        memberWrapper.ge(WxMemberDO::getLastLoginTime, oneYearAgo);
        // 查询满足条件的会员信息
        List<Long> memberIds = wxMemberService.getMemberIdByMemberWarpper(memberWrapper);
        if (CollectionUtil.isNotEmpty(memberIdsByTags)) {
            if (CollectionUtil.isNotEmpty(memberIds)) {
                return getIntersectionOptimized(memberIdsByTags, memberIds);
            } else
                return memberIdsByTags;
        } else {
            return memberIds;
        }
    }

    @Override
    public List<Long> getMemberIdByMemberWarpper(LambdaQueryWrapper<WxMemberDO> memberWrapper) {
        // 只查询memberId字段，避免查询所有字段
        memberWrapper.select(WxMemberDO::getMemberId);

        // 2. 创建自定义 ResultHandler 收集结果
        List<Long> memberIdList = new ArrayList<>();

        ResultHandler<Long> resultHandler = new ResultHandler<Long>() {
            @Override
            public void handleResult(ResultContext<? extends Long> resultContext) {
                // 获取当前行的结果（即 memberId）
                Long memberId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(memberId)) {
                    memberIdList.add(memberId);
                }
            }
        };
        wxMemberMapper.selectObjs(memberWrapper, resultHandler);
        return memberIdList;
    }

    @Override
    public List<Long> getAllMemberId(Long busId) {
        // 只查询memberId字段，避免查询所有字段
        LambdaQueryWrapper<WxMemberDO> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.select(WxMemberDO::getMemberId);
        memberWrapper.eq(WxMemberDO::getBusinessId, busId);
        // 新增：近一年的lastLoginTime条件（lastLoginTime >= 一年前的今天）
        // 计算一年前的日期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1); // 当前时间减去1年
        Date oneYearAgo = calendar.getTime();

        // 添加时间范围条件：lastLoginTime >= 一年前的今天
        memberWrapper.ge(WxMemberDO::getLastLoginTime, oneYearAgo);

        // 2. 创建自定义 ResultHandler 收集结果
        List<Long> memberIdList = new ArrayList<>();

        ResultHandler<Long> resultHandler = new ResultHandler<Long>() {
            @Override
            public void handleResult(ResultContext<? extends Long> resultContext) {
                // 获取当前行的结果（即 memberId）
                Long memberId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(memberId)) {
                    memberIdList.add(memberId);
                }
            }
        };

        wxMemberMapper.selectObjs(memberWrapper, resultHandler);
        return memberIdList;
    }

    @Override
    public List<WxMemberCrowdDTO> getMemberDataByCrowdId(String crowdId) {
        MPJLambdaWrapper<WxMemberDO> queryWrapper = new MPJLambdaWrapper<>();
        if (ObjectUtil.isNotEmpty(crowdId)) {
            List<Long> longList = Arrays.stream(crowdId.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
            queryWrapper.leftJoin(MemberCrowdRefDO.class, MemberCrowdRefDO::getMemberId, WxMemberDO::getMemberId);
            queryWrapper.in(MemberCrowdRefDO::getCrowdId, longList);
        }
        queryWrapper.select(WxMemberDO::getMemberId, WxMemberDO::getMemberMobile);
        List<WxMemberDO> wxMemberDOS = wxMemberMapper.selectList(queryWrapper);
        List<WxMemberCrowdDTO> wxMemberCrowdDTOS = wxMemberDOS.stream()
                .map(wxMemberDO -> new WxMemberCrowdDTO()
                        .setMemberId(wxMemberDO.getMemberId())
                        .setMemberMobile(wxMemberDO.getMemberMobile()))
                .toList();
        return wxMemberCrowdDTOS;
    }
    @Override
    @DataPermission(enable = false)
    public Map<Integer, List<WxMemberDTO>> getMembersMapByShard(int shardingValue, Long lastCursorId, int pageSize) {
        Map<Integer, List<WxMemberDTO>> levelMemberMap = new HashMap<>();

        List<WxMemberCardDO> cardList = wxMemberCardService.list();
        Map<Integer, WxMemberCardDO> levelCardMap = cardList.stream()
                .collect(Collectors.toMap(
                        WxMemberCardDO::getMemberLevel,
                        c -> c,
                        (k1, k2) -> k1 // 重复等级取第一个
                ));

        LocalDateTime oneYearAgo = LocalDateTime.now().minus(1, ChronoUnit.YEARS);
        Date oneYearAgoDate = Date.from(oneYearAgo.atZone(ZoneId.systemDefault()).toInstant());

        QueryWrapper<WxMemberDO> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<WxMemberDO> lambda = queryWrapper.lambda()
                .eq(WxMemberDO::getUserIdentity, 1)
                .ge(WxMemberDO::getFinalOrderFinishTime, oneYearAgoDate)
                .eq(WxMemberDO::getBusinessId, 10L)
                .eq(WxMemberDO::getShardingValue, shardingValue)
                .gt(WxMemberDO::getMemberId, lastCursorId) // 游标核心
                .orderByAsc(WxMemberDO::getMemberId);

        WxMemberCardDO grade1Config = levelCardMap.get(WxMemberConstant.GRADE_1);
        if (Objects.nonNull(grade1Config)) {
            lambda.gt(WxMemberDO::getIntegralFrozen, grade1Config.getMaxPointsThreshold());
        } else {
            lambda.gt(WxMemberDO::getIntegralFrozen, 0);
        }

        // 限制批次大小
        queryWrapper.last("LIMIT " + pageSize);

        List<WxMemberDO> memberDOList = wxMemberMapper.selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(memberDOList)) {
            for (WxMemberDO memberDO : memberDOList) {
                // 此时计算出的等级只会是2-5级（或0级，可根据业务决定是否过滤）
                Integer memberLevel = calculateMemberLevel(memberDO.getIntegralFrozen(), levelCardMap);
                // 可选：过滤0级（配置缺失导致的无效等级）
                if (memberLevel == 0) {
                    continue;
                }
                WxMemberDTO dto = convertToDTO(memberDO);
                dto.setMemberId(memberDO.getMemberId());
                levelMemberMap.computeIfAbsent(memberLevel, k -> new ArrayList<>()).add(dto);
            }
        }

        return levelMemberMap;
    }

    @Override
    @DataPermission(enable = false)
    public List<WxMemberBenefitJobDTO> listMemberCardBenefitJobMembers(Long businessId, int shardingValue, Long lastCursorId, int pageSize) {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
        Date oneYearAgoDate = Date.from(oneYearAgo.atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<WxMemberDO> queryWrapper = new LambdaQueryWrapper<WxMemberDO>()
                .select(WxMemberDO::getMemberId,
                        WxMemberDO::getMemberName,
                        WxMemberDO::getMemberNickName,
                        WxMemberDO::getMemberMobile,
                        WxMemberDO::getIntegralFrozen,
                        WxMemberDO::getLastLoginTime,
                        WxMemberDO::getUserIdentity,
                        WxMemberDO::getMemberBirthday,
                        WxMemberDO::getShardingValue,
                        WxMemberDO::getBusinessId,
                        WxMemberDO::getMemberLevel)
                .eq(WxMemberDO::getUserIdentity, 1)
                .eq(WxMemberDO::getShardingValue, shardingValue)
                .eq(WxMemberDO::getBusinessId, businessId)
                .ge(WxMemberDO::getLastLoginTime, oneYearAgoDate)
                .gt(WxMemberDO::getMemberId, lastCursorId)
                .eq(WxMemberDO::getDeleted, false)
                .orderByAsc(WxMemberDO::getMemberId)
                .last("limit " + pageSize);

        List<WxMemberDO> memberDOList = wxMemberMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(memberDOList)) {
            return Collections.emptyList();
        }
        return memberDOList.stream().map(this::convertToBenefitJobDTO).toList();
    }

    @Override
    @DataPermission(enable = false)
    public void updateMemberLevel(Long businessId, Long memberId, Integer shardingValue, Integer memberLevel) {
        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<WxMemberDO>()
                .eq(WxMemberDO::getMemberId, memberId)
                .eq(WxMemberDO::getShardingValue, shardingValue)
                .eq(WxMemberDO::getBusinessId, businessId)
                .set(WxMemberDO::getMemberLevel, memberLevel);
        wxMemberMapper.update(null, updateWrapper);
    }

    @Override
    public List<Long> getCommunityList(String busId) {
        return wxMemberMapper.getCommunityList(busId);
    }

    @Override
    public List<Long> getNotInCommunityList(String busId) {
        return wxMemberMapper.getNotInCommunityList(busId);
    }

    private WxMemberDTO convertToDTO(WxMemberDO memberDO) {
        WxMemberDTO dto = new WxMemberDTO();
        dto.setMemberId(memberDO.getMemberId());
        dto.setMemberMobile(memberDO.getMemberMobile());
        dto.setMemberName(memberDO.getMemberName());
        dto.setMemberNickName(memberDO.getMemberNickName());
        dto.setFinalOrderFinishTime(memberDO.getFinalOrderFinishTime());
        dto.setLastLoginTime(memberDO.getLastLoginTime());
        dto.setIntegralFrozen(memberDO.getIntegralFrozen());
        dto.setMemberLevel(memberDO.getMemberLevel());
        dto.setMemberBirthday(memberDO.getMemberBirthday());
        dto.setMemberStatus(memberDO.getMemberStatus());
        dto.setState(memberDO.getState());
        dto.setBusinessId(memberDO.getBusinessId());
        dto.setShardingValue(memberDO.getShardingValue());
        dto.setUserIdentity(memberDO.getUserIdentity());
        return dto;
    }

    private WxMemberBenefitJobDTO convertToBenefitJobDTO(WxMemberDO memberDO) {
        WxMemberBenefitJobDTO dto = new WxMemberBenefitJobDTO();
        dto.setMemberId(memberDO.getMemberId());
        dto.setMemberMobile(memberDO.getMemberMobile());
        dto.setMemberNickName(memberDO.getMemberNickName());
        dto.setIntegralFrozen(memberDO.getIntegralFrozen());
        dto.setMemberLevel(memberDO.getMemberLevel());
        dto.setMemberBirthday(memberDO.getMemberBirthday());
        dto.setBusinessId(memberDO.getBusinessId());
        dto.setShardingValue(memberDO.getShardingValue());
        return dto;
    }
    private Integer calculateMemberLevel(Long integralFrozen, Map<Integer, WxMemberCardDO> levelCardMap) {
        if (Objects.isNull(integralFrozen) || CollectionUtils.isEmpty(levelCardMap)) {
            return 0; // 无积分/无配置，返回默认等级
        }
        List<Integer> fixedLevelOrder = Arrays.asList(
                WxMemberConstant.GRADE_1,
                WxMemberConstant.GRADE_2,
                WxMemberConstant.GRADE_3,
                WxMemberConstant.GRADE_4,
                WxMemberConstant.GRADE_5
        );

        for (Integer level : fixedLevelOrder) {
            WxMemberCardDO cardConfig = levelCardMap.get(level);
            if (Objects.isNull(cardConfig)) {
                continue; // 配置缺失则跳过，不影响其他等级
            }

            // 按等级规则匹配
            if (level == WxMemberConstant.GRADE_1) {
                if (integralFrozen <= cardConfig.getMaxPointsThreshold()) {
                    return level;
                }
            } else if (level == WxMemberConstant.GRADE_5) {
                if (integralFrozen >= cardConfig.getMinPointsThreshold()) {
                    return level;
                }
            } else if (level == WxMemberConstant.GRADE_2 || level == WxMemberConstant.GRADE_3 || level == WxMemberConstant.GRADE_4) {
                if (integralFrozen >= cardConfig.getMinPointsThreshold() && integralFrozen <= cardConfig.getMaxPointsThreshold()) {
                    return level; // 2、3、4级会按顺序匹配，确保各自区间的积分返回对应等级
                }
            }
        }

        return 0; // 无匹配等级
    }
    /**
     * 将List<WxMemberDO>转换为包含MemberId的List<Long>
     *
     * @param memberDOList 源列表（WxMemberDO对象集合）
     * @return 提取出的memberId列表（Long类型）
     */
    private List<Long> convertToMemberIdList(List<WxMemberDO> memberDOList) {
        // 处理空列表，避免空指针异常
        if (memberDOList == null || memberDOList.isEmpty()) {
            return List.of();
        }

        // 流式处理：提取每个CrowdTagDO的tagId，收集为List<Long>
        return memberDOList.stream()
                .map(WxMemberDO::getMemberId) // 调用getter方法获取tagId
                .toList();
    }

    // 高性能版本（适用于大数据量）
    private List<Long> getIntersectionOptimized(List<Long> list1, List<Long> list2) {
        if (list1 == null || list2 == null) return new ArrayList<>();
        // 将较小的列表转为HashSet提升效率
        List<Long> smaller = list1.size() <= list2.size() ? list1 : list2;
        List<Long> larger = list1.size() > list2.size() ? list1 : list2;
        return larger.stream()
                .filter(new HashSet<>(smaller)::contains)
                .collect(Collectors.toList());
    }

    /**
     * 将"1-2-3-4-5"形式的字符串转换为List<String>
     */
    public static List<String> toListOfString(String str) {
        if (str == null || str.isEmpty()) {
            return List.of(); // 返回空列表
        }
        // 按"-"分割字符串并转换为List
        return Arrays.asList(str.split("-"));
    }

    /**
     * 将"1-2-3-4-5"形式的字符串转换为List<Integer>
     */
    public static List<Integer> toListOfInteger(String str) {
        if (str == null || str.isEmpty()) {
            return List.of(); // 返回空列表
        }
        // 分割后转换为Integer类型
        return Arrays.stream(str.split("-"))
                .map(Integer::parseInt)
                .toList();
    }


    /**
     * 获取昨天0点0分0秒的时间戳（毫秒级）
     *
     * @return 时间戳（从1970-01-01 00:00:00 UTC开始计算的毫秒数）
     */
    public static String getOldStartTimestamp(Integer count) {
        LocalDateTime dateTime = LocalDateTime.now()
                .minusDays(count)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        // 格式化为 MM/dd
        return dateTime.format(MM_DD_FORMATTER);
    }

    /**
     * 获取明天0点0分0秒的时间戳（毫秒级）
     *
     * @return 时间戳（从1970-01-01 00:00:00 UTC开始计算的毫秒数）
     */
    public static String getNewtartTimestamp(Integer count) {
        LocalDateTime dateTime = LocalDateTime.now()
                .plusDays(count)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        // 格式化为 MM/dd
        return dateTime.format(MM_DD_FORMATTER);
    }

    @DataPermission(enable = false)
    @Override
    public void resetCommunityFlag(int i) {
        wxMemberMapper.resetCommunityFlag(i);
    }

    @DataPermission(enable = false)
    @Override
    public void setCommunityFlag(int i) {
        wxMemberMapper.setCommunityFlag(i);
    }



    /**
     * 设置交易密码
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    @Transactional(rollbackFor = Exception.class)
    public boolean setTranPassword(Long memberId, String tranPassword, String confirmPassword) {
        // 1. 验证两次密码是否一致
        if (!tranPassword.equals(confirmPassword)) {
            throw exception(TRAN_PASSWORD_NOT_MATCH);
        }

        // 2. 验证密码格式（6位数字）
        if (!tranPassword.matches("^\\d{6}$")) {
            throw exception(TRAN_PASSWORD_FORMAT_ERROR);
        }

        // 3. 获取会员信息（仅用于校验是否存在和是否已设置密码）
        WxMemberDO member = this.getWxMember(memberId);
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }

        // 4. 检查是否已设置交易密码
        if (StringUtils.hasText(member.getTranPassword())) {
            throw exception(TRAN_PASSWORD_ALREADY_SET);
        }

        // 5. 使用 UpdateWrapper 只更新需要修改的字段
        String encryptedPassword = BCrypt.hashpw(tranPassword, BCrypt.gensalt());

        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, memberId)
                .set(WxMemberDO::getTranPassword, encryptedPassword)
                .set(WxMemberDO::getUpdateTime, LocalDateTime.now());

        boolean result = wxMemberMapper.update(null, updateWrapper) > 0;
        if (result) {
            log.info("设置交易密码成功，memberId: {}", memberId);
            return true;
        }
        throw exception(TRAN_PASSWORD_SET_FAIL);
    }

    /**
     * 验证交易密码
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    public boolean verifyTranPassword(Long memberId, String tranPassword) {
        // 1. 检查是否被锁定
        checkTranPasswordLock(memberId);

        // 2. 获取会员信息
        WxMemberDO member = this.getWxMember(memberId);
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }

        // 3. 检查是否已设置交易密码
        if (!StringUtils.hasText(member.getTranPassword())) {
            throw exception(TRAN_PASSWORD_NOT_SET);
        }

        // 4. 验证密码
        if (!BCrypt.checkpw(tranPassword, member.getTranPassword())) {
            // 密码错误，增加错误次数
            incrementPasswordErrorCount(memberId);
            throw exception(TRAN_PASSWORD_ERROR);
        }

        // 5. 密码正确，清除错误记录
        clearPasswordErrorInfo(memberId);
        return true;
    }

    /**
     * 检查是否已设置交易密码
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    public boolean isTranPasswordSet(Long memberId) {
        WxMemberDO member = this.getWxMember(memberId);
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }
        return StringUtils.hasText(member.getTranPassword());
    }

    /**
     * 找回交易密码（通过手机号和验证码）
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    @Transactional(rollbackFor = Exception.class)
    public boolean forgetPassword(String mobile, String code, String newPassword,
                                  String confirmPassword, String ip) {
        // 1. 验证两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw exception(PASSWORD_NOT_MATCH);
        }

        // 2. 验证密码格式
        if (!newPassword.matches("^\\d{6}$")) {
            throw exception(TRAN_PASSWORD_FORMAT_ERROR);
        }

        // 3. 验证短信验证码
        try {
            SmsCodeUseReqDTO reqDTO = new SmsCodeUseReqDTO();
            reqDTO.setCode(code);
            reqDTO.setMobile(mobile);
            reqDTO.setScene(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene());
            reqDTO.setUsedIp(ip);
            smsCodeApi.useSmsCode(reqDTO);
        } catch (Exception e) {
            log.error("验证码校验失败：{}", e.getMessage());
            throw exception(SMS_CODE_ERROR);
        }



        // 4. 根据手机号查找会员（只需要验证是否存在，不需要全部字段）
        WxMemberDO member = wxMemberMapper.selectOne(
                new LambdaQueryWrapper<WxMemberDO>()
                        .select(WxMemberDO::getMemberId, WxMemberDO::getTranPassword)  // 只查询需要的字段
                        .eq(WxMemberDO::getMemberId, WebFrameworkUtils.getLoginUserId())
        );
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }

        // 5. 使用 UpdateWrapper 只更新密码字段（避免更新分片键）
        String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, member.getMemberId())
                .set(WxMemberDO::getTranPassword, encryptedPassword)
                .set(WxMemberDO::getUpdateTime, LocalDateTime.now());

        boolean result = wxMemberMapper.update(null, updateWrapper) > 0;
        if (result) {
            // 重置成功后清除错误记录
            clearPasswordErrorInfo(member.getMemberId());
            log.info("找回交易密码成功，mobile: {}, memberId: {}", mobile, member.getMemberId());
            return true;
        }
        throw exception(FORGET_PASSWORD_FAIL);
    }

    /**
     * 修改交易密码
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long memberId, String oldPassword,
                                  String newPassword, String confirmPassword) {
        // 1. 验证两次密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw exception(PASSWORD_NOT_MATCH);
        }

        // 2. 验证新密码格式
        if (!newPassword.matches("^\\d{6}$")) {
            throw exception(TRAN_PASSWORD_FORMAT_ERROR);
        }

        // 3. 检查是否被锁定
//        checkTranPasswordLock(memberId);

        // 4. 获取会员信息（只查询需要的字段，避免查询分片键）
        WxMemberDO member = wxMemberMapper.selectOne(
                new LambdaQueryWrapper<WxMemberDO>()
                        .select(WxMemberDO::getMemberId, WxMemberDO::getTranPassword)
                        .eq(WxMemberDO::getMemberId, memberId)
        );
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }

        // 5. 检查是否已设置交易密码
        if (!StringUtils.hasText(member.getTranPassword())) {
            throw exception(TRAN_PASSWORD_NOT_SET);
        }

        // 6. 验证原密码
        if (!BCrypt.checkpw(oldPassword, member.getTranPassword())) {
            // 密码错误，增加错误次数
            incrementPasswordErrorCount(memberId);
            throw exception(OLD_PASSWORD_ERROR);
        }

        // 7. 密码正确，清除错误记录
        clearPasswordErrorInfo(memberId);

        // 8. 使用 UpdateWrapper 只更新密码字段
        String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        LambdaUpdateWrapper<WxMemberDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxMemberDO::getMemberId, memberId)
                .set(WxMemberDO::getTranPassword, encryptedPassword)
                .set(WxMemberDO::getUpdateTime, LocalDateTime.now());

        boolean result = wxMemberMapper.update(null, updateWrapper) > 0;
        if (result) {
            log.info("修改交易密码成功，memberId: {}", memberId);
            return true;
        }
        throw exception(UPDATE_PASSWORD_FAIL);
    }

    @Override
    public boolean checkTranOldPassword(Long memberId, String oldPassword) {
        checkTranPasswordLock(memberId);
        // 获取会员信息（只查询需要的字段，避免查询分片键）
        WxMemberDO member = wxMemberMapper.selectOne(
                new LambdaQueryWrapper<WxMemberDO>()
                        .select(WxMemberDO::getMemberId, WxMemberDO::getTranPassword)
                        .eq(WxMemberDO::getMemberId, memberId)
        );
        if (member == null) {
            throw exception(WX_MEMBER_NOT_EXISTS);
        }

        // 检查是否已设置交易密码
        if (!StringUtils.hasText(member.getTranPassword())) {
            throw exception(TRAN_PASSWORD_NOT_SET);
        }
        // 验证原密码
        if (!BCrypt.checkpw(oldPassword, member.getTranPassword())) {
            // 密码错误，增加错误次数
            incrementPasswordErrorCount(memberId);
            throw exception(OLD_PASSWORD_ERROR);
        }
        clearPasswordErrorInfo(memberId);

        return true;
    }

    /**
     * 检查交易密码是否被锁定（基于Redis）
     */
    private void checkTranPasswordLock(Long memberId) {
        String lockKey = String.format(TRAN_PASSWORD_LOCK_KEY, memberId);
        String lockValue = redisTemplate.opsForValue().get(lockKey);

        if (StringUtils.hasText(lockValue)) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            ttl = Math.max(1, ttl != null ? ttl : 1);
            throw exception(TRAN_PASSWORD_LOCKED, ttl);
        }
    }

    /**
     * 增加密码错误次数（基于Redis）
     */
    private void incrementPasswordErrorCount(Long memberId) {
        String errorKey = String.format(TRAN_PASSWORD_ERROR_KEY, memberId);
        String lockKey = String.format(TRAN_PASSWORD_LOCK_KEY, memberId);

        // 先检查是否已锁定
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return;
        }

        // 增加错误次数
        Long errorCount = redisTemplate.opsForValue().increment(errorKey);
        if (errorCount == null) {
            errorCount = 1L;
        }

        // 设置错误计数过期时间为1小时
        if (errorCount == 1) {
            redisTemplate.expire(errorKey, 1, TimeUnit.HOURS);
        }

        // 达到最大错误次数，锁定账户
        if (errorCount >= MAX_ERROR_COUNT) {
            redisTemplate.opsForValue().set(lockKey, "LOCKED", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            // 清除错误计数
            redisTemplate.delete(errorKey);
            log.warn("交易密码输入错误{}次，已锁定{}分钟，memberId: {}", errorCount, LOCK_DURATION_MINUTES, memberId);
            throw exception(TRAN_PASSWORD_LOCKED, LOCK_DURATION_MINUTES);
        }

        log.warn("交易密码输入错误，当前错误次数: {}/{}，memberId: {}", errorCount, MAX_ERROR_COUNT, memberId);
    }

    /**
     * 清除密码错误信息（基于Redis）
     */
    private void clearPasswordErrorInfo(Long memberId) {
        String errorKey = String.format(TRAN_PASSWORD_ERROR_KEY, memberId);
        String lockKey = String.format(TRAN_PASSWORD_LOCK_KEY, memberId);
        redisTemplate.delete(errorKey);
        redisTemplate.delete(lockKey);
    }
}

