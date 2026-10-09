package com.htyoudao.youdao.module.promotion.service.activitySign;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.excel.pojo.SearchAfterPage;
//import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySign.*;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStore.ActivityStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
@RefreshScope
public class ActivitySignServiceImpl implements ActivitySignService {
  /** 活动启用状态：1 表示开启，只有开启且在活动时间内的小程序端才允许命中和签到。 */
  private static final int ENABLED = 1;

  /** 奖励类型：1 优惠券，复用抽签奖品类型编码。 */
  private static final int PRIZE_TYPE_COUPON = 1;

  /** 奖励类型：5 现金红包，签到时调用红包转账能力生成 outBillNo/packageInfo，领取状态由微信回调更新。 */
  private static final int PRIZE_TYPE_RED_PACKET = 5;

  /** 奖励类型：6 优惠券包，复用抽签奖品类型编码。 */
  private static final int PRIZE_TYPE_COUPON_PACKAGE = 6;

  /** 奖励类型：3 实物奖品，发放后需要小程序填写地址、PC 填写物流。 */
  private static final int PRIZE_TYPE_PHYSICAL = 3;

  /** 奖励发放状态：2 已发放，表示积分/券/券包/红包/实物发放记录处理成功。 */
  private static final int ISSUE_SUCCESS = 2;

  /** 奖励发放状态：3 发放失败，失败原因写入 failReason，签到主流程不回滚。 */
  private static final int ISSUE_FAILED = 3;

  /** 红包领取状态：1 未领取，红包发放成功后默认状态，后续由微信回调改为已领取或已过期。 */
  private static final int CLAIM_PENDING = 1;

  /** 奖品状态：0 已发放，非实物奖励默认状态；与抽签发放记录奖品状态保持一致。 */
  private static final int PRIZE_STATE_ISSUED = 0;

  /** 奖品状态：1 未填写收货地址，实物奖品签到发放后的默认状态。 */
  private static final int PRIZE_STATE_ADDRESS_EMPTY = 1;

  /** 奖品状态：9 超时未填写收货地址，实物奖品发放超过48小时未填写地址时由定时任务更新。 */
  private static final int PRIZE_STATE_ADDRESS_TIMEOUT = 9;

  /** ES 索引：签到记录索引，用于 PC 签到记录分页、数据分析统计，异常时降级 MySQL。 */
  private static final String SIGN_RECORD_INDEX = "promotion_sign_record";

  /** ES 索引：奖励发放记录索引，用于 PC 发放记录分页、奖励发放统计，异常时降级 MySQL。 */
  private static final String SIGN_REWARD_INDEX = "promotion_sign_reward_record";

  /** 前端时间筛选空字符串会被全局时间戳反序列化成 1970-01-01 08:00:00，这类值要按未选择时间处理。 */
  private static final LocalDateTime EMPTY_CLIENT_TIME_END = LocalDateTime.of(1971, 1, 1, 0, 0, 0);

  /** 签到模块内部测试日期请求头：仅在非 prod 且 promotion.sign.test-date-enabled=true 时生效，不写入接口文档。 */
  private static final String SIGN_TEST_DATE_HEADER = "X-Sign-Test-Date";

  /** PC 列表/导出批量补会员昵称时的单批 memberId 数量，避免一次 Dubbo 请求过大。 */
  private static final int MEMBER_NAME_QUERY_BATCH_SIZE = 500;

  @Resource private ActivityService activityService;
  @Resource private ActivityAppService activityAppService;
  @Resource private ActivityMapper activityMapper;
  @Resource private ActivityStoreService activityStoreService;
  @Resource private ActivityStoreMapper activityStoreMapper;
  @Resource private ActivityChannelService activityChannelService;
  @Resource private ActivitySignMapper activitySignMapper;
  @Resource private ActivitySignRewardMapper activitySignRewardMapper;
  @Resource private ActivitySignRecordMapper activitySignRecordMapper;
  @Resource private ActivitySignRewardRecordMapper activitySignRewardRecordMapper;
  @Resource private LotteryRedPacketService lotteryRedPacketService;
  @Resource private GoodCouponService goodCouponService;
  @Resource private GoodCouponMapper goodCouponMapper;
  @Resource private UserCouponService userCouponService;
  @Resource private CouponPackageService couponPackageService;
  @Resource private RedisCache redisCache;
//  @Resource private RabbitMQService rabbitMQService;
  @Resource private ElasticsearchClient elasticsearchClient;
  @Resource private LotteryAddLogService lotteryAddLogService;
  @Resource private IdentifierGenerator identifierGenerator;
  @Resource private IEventService eventService;
  @Resource private ExcelActionService<ActivitySignRecordExportRespVO> signRecordExcelActionService;

  @Resource
  private ExcelActionService<ActivitySignRewardRecordExportRespVO> signRewardExcelActionService;

  @DubboReference(
      check = false,
      methods = @Method(name = "getMemberNameMapByIds", timeout = 30000))
  private WxMemberApi wxMemberApi;

  @DubboReference(check = false)
  private StoreApi storeApi;

  @Value("${activity.sign.shareImageUrl:}")
  private String defaultShareImageUrl;

  @Value("${activity.sign.shareTitle:}")
  private String defaultShareTitle;

  @Value("${activity.sign.shareDescription:}")
  private String defaultShareDescription;


  @Value("${promotion.sign.test-date-enabled:false}")
  private Boolean signTestDateEnabled;

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  /** 创建签到活动：保存活动主表、签到扩展配置、奖励配置、适用门店，并写入门店命中缓存和推广渠道。参数 reqVO 为 PC 新建活动 JSON 结构。 */
  @Override
  @Transactional
  public Boolean create(ActivitySignSaveReqVO reqVO) {
    validateSaveReq(reqVO);
    ActivityDO activity = buildActivity(reqVO, null);
    Long activityId = activityService.createActivity(activity);
    ActivitySignDO sign = buildSign(reqVO, activityId);
    activitySignMapper.insert(sign);
    saveRewards(activityId, reqVO.getPrizeList());
    // 如果请求里传了门店ID列表
    if (!CollectionUtils.isEmpty(reqVO.getStoreIds()))
      activityStoreService.createBatch(reqVO.getStoreIds(), activityId);
    refreshStoreHitCache(activityId, reqVO.getStoreIds(), reqVO.getEnabled());
    refreshActivityCache(activityId);
    activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.SIGN.getCode());
    return true;
  }

  /** 修改签到活动：覆盖活动基础信息、签到配置、奖励配置和门店关系；注意活动期间修改规则后只对后续计算即时生效，不追溯旧奖励。参数 reqVO 必须带 id。 */
  @Override
  @Transactional
  public Boolean update(ActivitySignSaveReqVO reqVO) {
    // 如果修改活动没有传活动ID，无法定位要修改的活动。
    if (reqVO.getId() == null) {
      throw exception(SIGN_ACTIVITY_ID_REQUIRED);
    }
    validateSaveReq(reqVO);
    //已有用户签到，不允许修改规则
    validateRuleRewardEditable(reqVO);
    ActivityDO activity = buildActivity(reqVO, reqVO.getId());
    activityService.updateActivity(activity);
    ActivitySignDO old = selectSignByActivityId(reqVO.getId());
    ActivitySignDO sign = buildSign(reqVO, reqVO.getId());
    // 如果旧签到配置不存在，需要新增签到扩展配置
    if (old == null) activitySignMapper.insert(sign);
    else {
      sign.setId(old.getId());
      updateSignById(sign);
    }
    activitySignRewardMapper.delete(
        new LambdaQueryWrapper<ActivitySignRewardDO>()
            .eq(ActivitySignRewardDO::getActivityId, reqVO.getId()));
    List<Long> oldStoreIds = activityStoreService.selectStoreIdsByActivityId(reqVO.getId());
    activityStoreService.deleteByActivityId(reqVO.getId());
    // 如果请求里传了门店ID列表
    if (!CollectionUtils.isEmpty(reqVO.getStoreIds()))
      activityStoreService.createBatch(reqVO.getStoreIds(), reqVO.getId());
    clearStoreHitCache(oldStoreIds, reqVO.getId());
    refreshStoreHitCache(reqVO.getId(), reqVO.getStoreIds(), reqVO.getEnabled());
    saveRewards(reqVO.getId(), reqVO.getPrizeList());
    refreshActivityCache(reqVO.getId());
    return true;
  }

  /** 查询签到活动详情：组装活动基础信息、签到配置、门店列表、奖励列表给 PC 回显。参数 id 为活动ID。 */
  @Override
  public ActivitySignRespVO get(Long id) {
    ActivityDO a = activityService.selectById(id);
    // 如果活动主表不存在，说明传入的活动ID无效。
    if (a == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    ActivitySignDO s = selectSignByActivityId(id);
    List<ActivitySignRewardDO> rewards = selectRewards(id);
    ActivitySignRespVO vo = new ActivitySignRespVO();
    vo.setId(a.getId());
    vo.setActivityName(a.getActivityName());
    vo.setActivityRemark(a.getActivityRemark());
    vo.setEnabled(a.getIsEnabled());
    vo.setDateType(a.getEndDate() == null ? 2 : 1);
    vo.setStartTime(toLdt(a.getStartDate()));
    vo.setEndTime(toEndLdt(a.getEndDate()));
    vo.setActivityStoreType(Objects.equals(a.getActivityStore(), 1) ? 1 : 2);
    // 如果签到扩展配置存在，需要回填签到专属字段
    if (s != null) copySignToResp(s, vo);
    List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(id);
    vo.setStoreIds(storeIds);
    try {
      List<StoreInfoDTO> stores = activityStoreService.storesByActivityId(id);
      // 如果门店列表查询结果不为空，需要组装门店名称列表
      if (stores != null)
        vo.setStoreList(
            stores.stream()
                .map(
                    st -> {
                      ActivitySignStoreRespVO sv = new ActivitySignStoreRespVO();
                      sv.setStoreId(st.getStoreId());
                      sv.setStoreName(st.getStoreName());
                      return sv;
                    })
                .toList());
    } catch (Exception ignore) {
    }
    vo.setPrizeList(toPrizeReqList(rewards));
    return vo;
  }

  /** 启停签到活动：同步活动状态，并维护门店命中 Redis 缓存。参数 enabled=1 表示启用。 */
  @Override
  public Boolean updateStatus(ActivitySignStatusUpdateReqVO reqVO) {
    ActivityDO activity = activityService.selectById(reqVO.getId());
    // 如果活动主表不存在，说明传入的活动ID无效。
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    activityService.updateStatus(reqVO.getId(), reqVO.getEnabled());
    List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(reqVO.getId());
    clearStoreHitCache(storeIds, reqVO.getId());
    refreshStoreHitCache(reqVO.getId(), storeIds, reqVO.getEnabled());
    refreshActivityCache(reqVO.getId());
    return true;
  }

  /** 删除签到活动：先校验活动未开启，再逻辑删除活动主表、签到扩展、奖励、门店和推广渠道数据，并清理门店命中缓存。参数 id 为活动ID。 */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean delete(Long id) {
    ActivityDO activity = activityService.selectById(id);
    // 如果活动主表不存在，说明前端传入的活动ID无效。
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    // 活动正在开启时，用户端仍可能命中或签到，必须先停用再删除。
    if (Objects.equals(activity.getIsEnabled(), ENABLED)) {
      throw exception(SIGN_DELETE_ENABLED);
    }
    List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(id);
    // 这些 DO 都继承 BaseDO.deleted，MyBatis-Plus 的 delete/deleteById 会更新 deleted 字段，不会物理删除数据。
    activityService.deleteActivity(id);
    activitySignMapper.delete(new LambdaQueryWrapper<ActivitySignDO>().eq(ActivitySignDO::getActivityId, id));
    activitySignRewardMapper.delete(
        new LambdaQueryWrapper<ActivitySignRewardDO>().eq(ActivitySignRewardDO::getActivityId, id));
    activityStoreService.deleteByActivityId(id);
    activityChannelService.deleteByActivityId(id);
    clearStoreHitCache(storeIds, id);
    clearActivityCache(id);
    return true;
  }

  /** 查询签到活动推广配置：返回分享标题、描述、图片。参数 id 为活动ID。 */
  @Override
  public ActivitySignSpreadRespVO selectActivitySpread(Long id) {
    ActivityDO activity = activityService.selectById(id);
    // 如果活动主表不存在，不允许返回空推广配置，避免前端把无效活动当成正常活动处理。
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    ActivitySignDO s = selectSignByActivityId(id);
    ActivitySignSpreadRespVO vo = new ActivitySignSpreadRespVO();
    vo.setId(id);
    // 如果签到扩展配置存在，需要回填签到专属字段
    if (s != null) {
      vo.setShareTitle(s.getShareTitle());
      vo.setShareNote(s.getShareNote());
      vo.setShareImgUrl(s.getShareImgUrl());
    }
    return vo;
  }

  /** 修改签到活动推广配置：只更新分享标题、描述、图片。参数 reqVO 为 PC 推广配置 JSON。 */
  @Override
  public Boolean updateSpread(ActivitySignSpreadSaveReqVO reqVO) {
    ActivityDO activity = activityService.selectById(reqVO.getId());
    // 如果活动主表不存在，说明前端传入的活动ID无效。
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    ActivitySignDO s = selectSignByActivityId(reqVO.getId());
    // 如果活动存在但签到扩展配置不存在，说明这不是一条完整的签到活动数据。
    if (s == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    s.setShareTitle(reqVO.getShareTitle());
    s.setShareNote(reqVO.getShareNote());
    s.setShareImgUrl(reqVO.getShareImgUrl());
    activitySignMapper.updateById(s);
    refreshActivityCache(reqVO.getId());
    return true;
  }

  /** PC 数据分析汇总：优先查 ES，失败或强制 MySQL 时降级查分表。参数 forceMysql=true 仅后端调试使用。 */
  @Override
  public ActivitySignAnalysisSummaryRespVO analysisSummary(ActivitySignAnalysisSummaryReqVO reqVO) {
    // 如果没有强制指定查 MySQL，优先读取 Redis/ES
    if (!forceMysql(reqVO.getForceMysql())) {
      try {
        ActivitySignAnalysisSummaryRespVO esResult = analysisSummaryFromEs(reqVO);
        // 如果ES 查询到了可用结果，直接返回，不再查 MySQL
        if (esResult != null) return esResult;
      } catch (Exception e) {
        log.warn("签到活动数据分析 ES 查询失败，降级查 MySQL, req={}", reqVO, e);
      }
    }
    ActivitySignAnalysisSummaryRespVO vo = new ActivitySignAnalysisSummaryRespVO();
    long signCount = 0, join = 0, rewardCount = 0, rewardUsers = 0;
    BigDecimal red = BigDecimal.ZERO;
    long point = 0, coupon = 0, pack = 0, phy = 0;
    Set<String> joinMobiles = new HashSet<>(), rewardMobiles = new HashSet<>();
    for (int i = 0; i < 10; i++) {
      String rt = recordTable(i), wt = rewardTable(i);
      signCount += activitySignRecordMapper.countByActivity(rt, reqVO.getActivityId());
      List<ActivitySignRecordDO> rs =
          activitySignRecordMapper.selectPage(rt, fakeRecordReq(reqVO.getActivityId()), 0, 1000000);
      rs.forEach(r -> joinMobiles.add(r.getMemberMobile()));
      ActivitySignRewardRecordPageReqVO q = new ActivitySignRewardRecordPageReqVO();
      q.setActivityId(reqVO.getActivityId());
      List<ActivitySignRewardRecordDO> wrs =
          activitySignRewardRecordMapper.selectPage(wt, q, 0, 1000000);
      rewardCount += wrs.size();
      for (ActivitySignRewardRecordDO r : wrs) {
        rewardMobiles.add(r.getMemberMobile());
        if (Objects.equals(r.getPrizeType(), 2) && r.getPrizeValue() != null)
          point += r.getPrizeValue().longValue();
        if (Objects.equals(r.getPrizeType(), 1)) coupon++;
        if (Objects.equals(r.getPrizeType(), 6)) pack++;
        if (Objects.equals(r.getPrizeType(), 3)) phy++;
        if (Objects.equals(r.getPrizeType(), 5) && r.getPrizeValue() != null)
          red = red.add(r.getPrizeValue());
      }
    }
    join = joinMobiles.size();
    rewardUsers = rewardMobiles.size();
    Map<String, Long> visitMap = statEventLogPvUv(reqVO);
    vo.setPv(visitMap.getOrDefault("pv", 0L));
    vo.setUv(visitMap.getOrDefault("uv", 0L));
    vo.setShareCount(shareCount(reqVO));
    vo.setJoinCount(join);
    vo.setSignCount(signCount);
    vo.setRewardUserCount(rewardUsers);
    vo.setRewardIssueCount(rewardCount);
    vo.setPointAmount(BigDecimal.valueOf(point));
    vo.setCouponCount(coupon);
    vo.setCouponPackageCount(pack);
    vo.setRedPacketAmount(red);
    vo.setPhysicalCount(phy);
    return vo;
  }

  /** PC 签到记录分页：优先查 ES，失败降级 MySQL 分表；continuousCycleDays 用于计算连签周期次数。 */
  @Override
  public ActivitySignRecordPageRespVO recordPage(ActivitySignRecordPageReqVO reqVO) {
    if (reqVO.getPageNo()>1000){
      throw exception(BASE_ACTIVITY_PAGE_TOO_LARGE);
    }

    normalizeRecordPageReq(reqVO);
    // 如果没有强制指定查 MySQL，优先读取 Redis/ES
    if (!forceMysql(reqVO.getForceMysql())) {
      try {
        ActivitySignRecordPageRespVO esResult = recordPageFromEs(reqVO);
        // 如果ES 查询到了可用结果，直接返回，不再查 MySQL
        if (esResult != null) return esResult;
      } catch (Exception e) {
        log.warn("签到记录 ES 查询失败，降级查 MySQL, req={}", reqVO, e);
      }
    }
    List<ActivitySignRecordDO> all = new ArrayList<>();
    long total = 0;
    int pageNo = Math.max(reqVO.getPageNo(), 1), pageSize = Math.max(reqVO.getPageSize(), 10);
    int needStart = (pageNo - 1) * pageSize, needEnd = needStart + pageSize;
    for (int i = 0; i < 10; i++) {
      String t = recordTable(i);
      total += activitySignRecordMapper.countPage(t, reqVO);
      all.addAll(activitySignRecordMapper.selectPage(t, reqVO, 0, needEnd));
    }
    all.sort(
        Comparator.comparing(
                ActivitySignRecordDO::getSignTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed());
    List<ActivitySignRecordDO> page = all.stream().skip(needStart).limit(pageSize).toList();
    fillRecordDisplayNames(page);
    ActivitySignRecordPageRespVO vo = new ActivitySignRecordPageRespVO();
    vo.setTotal(total);
    vo.setSignCount(total);
    vo.setSignUserCount(
        all.stream()
            .map(ActivitySignRecordDO::getMemberMobile)
            .filter(Objects::nonNull)
            .distinct()
            .count());
    vo.setList(page.stream().map(r -> toRecordResp(r, reqVO.getContinuousCycleDays())).toList());
    return vo;
  }

  /** 签到记录真实导出：按查询条件异步生成 Excel，PC 端超过 30W 拒绝，exportSource=999 手动任务可绕过。 */
  @Override
  public void recordExport(ActivitySignRecordExportReqVO reqVO) throws ServerException {
    ActivitySignRecordPageReqVO q = buildRecordPageReq(reqVO);
    normalizeRecordPageReq(q);
    Long esTotal = countRecordExportFromEs(q);
    boolean useEsExport = esTotal != null;
    long total = esTotal == null ? countRecordExport(q) : esTotal;
    // 如果 ES 正常但没有数据，再查一次 MySQL，避免 ES 读模型延迟导致导出空文件。
    if (useEsExport && total == 0L) {
      long mysqlTotal = countRecordExport(q);
      if (mysqlTotal > 0L) {
        useEsExport = false;
        total = mysqlTotal;
      }
    }
    // 如果不是手动导出并且数据量超过30万，PC端不允许导出
    if (!Objects.equals(reqVO.getExportSource(), 999) && total > 300000) {
      throw exception(SIGN_EXPORT_LIMIT);
    }
    Page<ActivitySignRecordExportRespVO> page = new Page<>(1, useEsExport ? 5000 : 10000);
    // 如果 ES 可用，导出走 ES search_after，避免深分页限制；ES 异常或读模型缺数据时才降级 MySQL。
    if (useEsExport) {
      signRecordExcelActionService.exportAsyncExcel(
          ActivitySignRecordExportRespVO.class,
          page,
          this::getRecordExportDataFromEs,
          reqVO,
          buildSignRecordExportFileName(reqVO),
          true);
      return;
    }
    signRecordExcelActionService.exportAsyncExcel(
        ActivitySignRecordExportRespVO.class,
        page,
        param -> getRecordExportData(param, q),
        buildSignRecordExportFileName(reqVO));
  }

  /** PC 奖励发放记录分页：优先查 ES，失败降级 MySQL 分表；筛选项与蓝湖底表一致。 */
  @Override
  public ActivitySignRewardRecordPageRespVO rewardRecordPage(
      ActivitySignRewardRecordPageReqVO reqVO) {
    if (reqVO.getPageNo()>1000){
      throw exception(BASE_ACTIVITY_PAGE_TOO_LARGE);
    }
    normalizeRewardPageReq(reqVO);
    // 如果没有强制指定查 MySQL，优先读取 Redis/ES
    if (!forceMysql(reqVO.getForceMysql())) {
      try {
        ActivitySignRewardRecordPageRespVO esResult = rewardRecordPageFromEs(reqVO);
        // 如果 ES 查询到了可用结果，并且返回列表没有越过发放时间筛选范围，直接返回，不再查 MySQL。
        if (esResult != null && rewardPageIssueTimeMatched(esResult, reqVO)) return esResult;
        // 如果 ES 返回了超出 issueTimeStart/issueTimeEnd 的记录，说明读模型时间查询口径异常，立刻降级 MySQL 保证 PC 筛选准确。
        if (esResult != null) {
          log.warn("签到奖励发放记录 ES 发放时间筛选结果异常，降级查 MySQL, req={}", reqVO);
        }
      } catch (Exception e) {
        log.warn("签到奖励发放记录 ES 查询失败，降级查 MySQL, req={}", reqVO, e);
      }
    }
    List<ActivitySignRewardRecordDO> all = new ArrayList<>();
    long total = 0;
    int pageNo = Math.max(reqVO.getPageNo(), 1), pageSize = Math.max(reqVO.getPageSize(), 10);
    int needStart = (pageNo - 1) * pageSize, needEnd = needStart + pageSize;
    for (int i = 0; i < 10; i++) {
      String t = rewardTable(i);
      total += activitySignRewardRecordMapper.countPage(t, reqVO);
      all.addAll(activitySignRewardRecordMapper.selectPage(t, reqVO, 0, needEnd));
    }
    all.sort(
        Comparator.comparing(
                ActivitySignRewardRecordDO::getIssueTime,
                Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed());
    ActivitySignRewardRecordPageRespVO vo = new ActivitySignRewardRecordPageRespVO();
    vo.setTotal(total);
    vo.setRewardIssueCount(total);
    vo.setRewardUserCount(
        all.stream()
            .map(ActivitySignRewardRecordDO::getMemberMobile)
            .filter(Objects::nonNull)
            .distinct()
            .count());
    List<ActivitySignRewardRecordDO> page = all.stream().skip(needStart).limit(pageSize).toList();
    fillRewardDisplayNames(page);
    vo.setList(page.stream().map(this::toRewardResp).toList());
    return vo;
  }

  /** PC 填写/修改实物奖品快递单号：PC 入参只传发放记录 id，先从 ES 读模型拿手机号/分片，再更新 MySQL 主账本。 */
  @Override
  public Integer updateExpress(ActivitySignUpdateExpressReqVO reqVO) {
    ActivitySignRewardRecordDO route = rewardRecordRouteFromEs(reqVO.getId());
    // 发放记录表按手机号尾号分表，id 本身不带分片信息；不能按 id 循环扫 10 张表。
    if (route == null) {
      return 0;
    }
    String mobile = route.getMemberMobile();
    // 兼容 ES 旧文档缺 memberMobile 的情况：用 ES 里的 memberId 查会员手机号，再按手机号定位分表。
    if (!StringUtils.hasText(mobile)) {
      mobile = memberMobileByMemberId(route.getMemberId());
    }
    if (!StringUtils.hasText(mobile) && route.getMobileShard() == null) {
      return 0;
    }
    int tableShard = route.getMobileShard() == null ? shard(mobile) : route.getMobileShard();
    String table = rewardTable(tableShard);
    ActivitySignRewardRecordDO record =
        activitySignRewardRecordMapper.selectById(table, reqVO.getId());
    // ES 只负责路由定位，真正更新前仍以 MySQL 主账本记录为准。
    if (record == null) {
      return 0;
    }
    // 只有实物奖品才允许填写/修改快递单号，积分/券/券包/红包没有物流信息。
    if (!Objects.equals(record.getPrizeType(), PRIZE_TYPE_PHYSICAL)) return 0;
    if (record.getReceiveAddress() == null) {
      throw new ServiceException(SIGN_REWARD_RECORD_NOT_EXIST);
    }
    record.setPrizeState(3);
    record.setTrackingNumber(reqVO.getTrackingNumber());
    record.setExpressCompany(reqVO.getExpressCompany());
    int updated = activitySignRewardRecordMapper.updateExpress(table, record);
    // 如果 MySQL 更新成功，回查最新记录同步 ES，避免 PC 发放记录列表仍显示旧物流信息。
    if (updated > 0) {
      ActivitySignRewardRecordDO latest =
          activitySignRewardRecordMapper.selectById(table, reqVO.getId());
      afterCommit(() -> sendRewardRecordEvent(latest));
    }
    return updated;
  }

  /** 按发放记录 id 从 ES 读模型拿路由字段；只用于定位分表，不作为最终更新依据。 */
  private ActivitySignRewardRecordDO rewardRecordRouteFromEs(Long rewardRecordId) {
    if (rewardRecordId == null) return null;
    try {
      var response =
          elasticsearchClient.get(
              g -> g.index(SIGN_REWARD_INDEX).id(String.valueOf(rewardRecordId)), Map.class);
      if (response == null || !response.found() || response.source() == null) {
        return null;
      }
      return toRewardRecordFromEs(response.source());
    } catch (Exception e) {
      log.warn("按发放记录ID查询签到奖励 ES 路由失败 rewardRecordId={}", rewardRecordId, e);
      return null;
    }
  }

  /** 按会员 id 查询手机号：用于兼容 ES 旧文档缺 memberMobile 时补充分表路由。 */
  private String memberMobileByMemberId(Long memberId) {
    if (memberId == null) return null;
    try {
      CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(memberId);
      WxMemberDTO member = result == null ? null : result.getData();
      return member == null ? null : member.getMemberMobile();
    } catch (Exception e) {
      log.warn("按会员ID查询手机号失败 memberId={}", memberId, e);
      return null;
    }
  }

  /** 奖励发放记录真实导出：按查询条件异步生成 Excel，PC 端超过 30W 拒绝，exportSource=999 手动任务可绕过。 */
  @Override
  public void rewardRecordExport(ActivitySignRewardRecordExportReqVO reqVO) throws ServerException {
    ActivitySignRewardRecordPageReqVO q = buildRewardPageReq(reqVO);
    normalizeRewardPageReq(q);
    Long esTotal = countRewardExportFromEs(q);
    boolean useEsExport = esTotal != null;
    long total = esTotal == null ? countRewardExport(q) : esTotal;
    // 如果 ES 正常但没有数据，再查一次 MySQL，避免 ES 读模型延迟导致导出空文件。
    if (useEsExport && total == 0L) {
      long mysqlTotal = countRewardExport(q);
      if (mysqlTotal > 0L) {
        useEsExport = false;
        total = mysqlTotal;
      }
    }
    // 如果不是手动导出并且数据量超过30万，PC端不允许导出
    if (!Objects.equals(reqVO.getExportSource(), 999) && total > 300000) {
      throw exception(SIGN_EXPORT_LIMIT);
    }
    Page<ActivitySignRewardRecordExportRespVO> page =
        new Page<>(1, useEsExport ? 5000 : 10000);
    // 如果 ES 可用，导出走 ES search_after，避免深分页限制；ES 异常或读模型缺数据时才降级 MySQL。
    if (useEsExport) {
      signRewardExcelActionService.exportAsyncExcel(
          ActivitySignRewardRecordExportRespVO.class,
          page,
          this::getRewardExportDataFromEs,
          reqVO,
          buildSignRewardExportFileName(reqVO),
          true);
      return;
    }
    signRewardExcelActionService.exportAsyncExcel(
        ActivitySignRewardRecordExportRespVO.class,
        page,
        param -> getRewardExportData(param, q),
        buildSignRewardExportFileName(reqVO));
  }

  /** 小程序首页签到活动命中：按当前门店返回可进入的签到活动，前端再按签到记录是否为空决定是否展示进度卡。storeId 为当前门店。 */
  @Override
  @DataPermission(enable = false)
  public AppActivitySignHitRespVO hit(Long storeId, Boolean forceMysql) {
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    WxMemberDTO m = getMember(memberId);
    String mobile = mobileOrNull(m, memberId);
    AppActivitySignHitRespVO vo = new AppActivitySignHitRespVO();
    vo.setStoreId(storeId);
    vo.setShowProgress(false);
    ActivityDO a = findLatestRunningActivity(storeId);
    // 如果当前门店没有命中进行中的签到活动，首页没有可进入的签到活动。
    if (a == null) return vo;
    ActivitySignDO s = selectSignByActivityId(a.getId());
    fillHitActivityBase(vo, a, s);
    // 首页命中接口不强制要求绑定手机号：未绑手机号时只返回活动入口，不展示进度卡，签到接口再明确拦截。
    if (!StringUtils.hasText(mobile)) {
      fillHitDefaultProgress(vo, a.getId());
      return vo;
    }
    Progress p = calcProgress(a.getId(), mobile, s);
    vo.setShowProgress(p.total > 0);
    vo.setTodaySigned(todaySigned(a.getId(), mobile));
    vo.setRewardFinished(p.finished);
    vo.setRewardProgressDays(p.progress);
    vo.setNextRewardTargetDays(p.target);
    vo.setNextRewardNeedDays(p.need);
    vo.setButtonText(
        p.finished ? "查看活动" : (Boolean.TRUE.equals(vo.getTodaySigned()) ? "今日已签到" : "去签到"));
    return vo;
  }

  /** 小程序签到活动详情：返回活动图片、规则、周日历和奖励进度；用户签到状态统一由 sign-record 接口判断。 */
  @Override
  @DataPermission(enable = false)
  public AppActivitySignDetailRespVO detail(AppActivitySignReqVO reqVO) {
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    WxMemberDTO m = getMember(memberId);
    String mobile = mobileOrNull(m, memberId);
    ActivityDO a = getActivityById(reqVO.getActivityId());
    ActivitySignDO s = selectSignByActivityId(reqVO.getActivityId());
    AppActivitySignDetailRespVO vo = new AppActivitySignDetailRespVO();
    vo.setActivityId(reqVO.getActivityId());
    vo.setStoreId(reqVO.getStoreId());
    // 如果活动主表数据存在，需要回填活动基础信息
    if (a != null) {
      vo.setActivityName(a.getActivityName());
      vo.setStartTime(toLdt(a.getStartDate()));
      vo.setEndTime(toEndLdt(a.getEndDate()));
      vo.setActivityStatus(statusOf(a));
    }
    // 如果签到扩展配置存在，需要回填签到专属字段
    if (s != null) {
      vo.setActivityBackgroundImage(s.getActivityBackgroundImage());
      vo.setUnsignedImage(s.getUnsignedImage());
      vo.setSignedImage(s.getSignedImage());
      vo.setActivityDetailImage(s.getActivityDetailImage());
      vo.setActivityRule(s.getActivityRule());
      vo.setThemeColor(s.getThemeColor());
      vo.setShareType(s.getShareType());
      vo.setShareTitle(s.getShareTitle());
      vo.setShareNote(s.getShareNote());
      vo.setShareImgUrl(s.getShareImgUrl());
    }
    // 详情页允许未绑定手机号用户查看活动配置；未绑定时不计算任何用户态进度，避免读取签到/发奖记录。
    if (!StringUtils.hasText(mobile)) {
      vo.setPrizeList(
          selectRewards(reqVO.getActivityId()).stream().map(this::toAppPrizeBase).toList());
      return vo;
    }
    Progress p = calcProgress(reqVO.getActivityId(), mobile, s);
//    vo.setTodaySigned(todaySigned(reqVO.getActivityId(), mobile));
    vo.setContinuousDays(p.continuous);
    vo.setTotalDays(p.total);
    vo.setNextRewardNeedDays(p.need);
    vo.setRewardFinished(p.finished);
    vo.setWeekDateList(weekDates(reqVO.getActivityId(), mobile));
    vo.setPrizeList(
        selectRewards(reqVO.getActivityId()).stream().map(r -> toAppPrize(r, p)).toList());
    return vo;
  }

  /** 小程序立即签到主流程：按手机号维度防重、落 MySQL、更新 Redis 日历、同步发奖，并在事务提交后投递 MQ 到 ES。 */
  @Override
  @DataPermission(enable = false)
  @Transactional
  public AppActivitySignResultRespVO sign(AppActivitySignReqVO reqVO) {
    // 从登录上下文获取当前小程序会员ID，后续按会员ID查询手机号和会员基础信息。
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    // 查询会员信息：签到业务以手机号为唯一身份，不能只依赖 memberId。
    WxMemberDTO m = getMember(memberId);
    // 提取并校验手机号；如果当前会员未绑定手机号，这里会抛出业务异常。
    String mobile = mobileOf(m, memberId);
    // 根据手机号尾号计算签到记录分表，下方签到记录和奖励记录都按该分表写入。
    int shard = shard(mobile);
    // 获取签到业务日期；本地自测时可能由隐藏 Header 模拟日期，正常业务取服务器当天。
    LocalDate today = signToday();
    // 查询活动主表，用于校验活动状态、门店适用范围以及计算 Redis bitmap 偏移量。
    ActivityDO activity = getActivityById(reqVO.getActivityId());
    // 如果活动不存在，直接返回明确错误码，不能静默返回 signSuccess=false。
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    // 如果活动未开始、已结束或已停用，直接返回明确错误码，方便前端提示原因。
    if (statusOf(activity) != 1) {
      throw exception(SIGN_ACTIVITY_NOT_RUNNING);
    }
    // 如果活动不适用当前门店，直接返回明确错误码，避免前端只看到签到失败却不知道是门店问题。
    if (!activityMatchStore(activity, reqVO.getStoreId())) {
      throw exception(SIGN_STORE_NOT_MATCH);
    }
    // 计算今天在活动日期中的偏移量：活动开始日为 0，用于读写 Redis bitmap。
    int offset = signOffset(activity, today);
    // 生成当前活动+手机号的签到日历缓存 key，只存已签到日期，未签到日期不落缓存。
    String calendarKey = calendarKey(reqVO.getActivityId(), mobile);
    // 如果 Redis 日历里今天已经标记为已签到，说明用户重复点击，不查 MySQL、不重复落库和发奖。
    if (offset >= 0 && Boolean.TRUE.equals(redisCache.getBit(calendarKey, offset))) {
      // 直接组装幂等响应：signSuccess=false 表示本次没有新增签到，todaySigned=true 表示今天已签到。
      return signResult(reqVO, mobile, Collections.emptyList(), false, true);
    }
    // 调用活动统一参与校验：社群专享不满足时这里会抛出业务异常，阻断后续落库和发奖。
    activityAppService.checkCanJoin(reqVO.getActivityId());
    // 查询签到扩展配置，用于计算当前重置周期、快照重置规则和后续奖励规则。
    ActivitySignDO s = selectSignByActivityId(reqVO.getActivityId());
    // 同一手机号+同一活动+同一天只允许一个签到流程进入临界区，避免重复插入和重复发奖。
    String lockKey = signLockKey(reqVO.getActivityId(), mobile, today);
    // 锁值使用随机 UUID，释放锁时必须值相同，避免误删其他请求刚抢到的锁。
    String lockValue = UUID.randomUUID().toString();
    // 如果没有抢到当天签到锁，说明同一手机号同一活动同一天正在签到处理中，直接返回明确错误码。
    if (!redisCache.lock(lockKey, lockValue, 5)) {
      throw exception(SIGN_PROCESSING);
    }
    try {
      // 拿到分布式锁后再读一次 Redis：处理两个请求几乎同时通过锁外快速判断的并发场景。
      if (offset >= 0 && Boolean.TRUE.equals(redisCache.getBit(calendarKey, offset))) {
        // 锁等待期间其他请求已完成签到，不再查 MySQL、落库和发奖，直接返回幂等结果。
        return signResult(reqVO, mobile, Collections.emptyList(), false, true);
      }
      // 根据当前活动重置配置计算当前周期窗口；后端不做定时重置，只按规则实时计算周期。
      PeriodWindow window = currentPeriodWindow(s);
      // 查询当前周期内用户已签到日期：优先 Redis/ES，缓存不可用时降级 MySQL。
      List<LocalDate> dates = new ArrayList<>(signedDates(reqVO.getActivityId(), mobile, window));
      // 如果 Redis 缺失后降级 MySQL 发现今天已签到，需要补回 Redis 并按幂等返回。
      if (dates.contains(today)) {
        // 今天已签到但 Redis 位缺失时，补写 bitmap，避免下次重复点击继续打到 MySQL。
        if (offset >= 0) {
          setCalendarBit(activity, calendarKey, offset);
        }
        // MySQL 已有今天记录，当前请求不新增签到、不触发奖励，只返回今天已签到。
        return signResult(reqVO, mobile, Collections.emptyList(), false, true);
      }
      // 组装签到记录主账本：MySQL 是最终幂等账本，Redis/ES 都只是读模型或加速模型。
      ActivitySignRecordDO r = new ActivitySignRecordDO();
      r.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId());
      r.setActivityId(reqVO.getActivityId());
      r.setMemberMobile(mobile);
      r.setMobileShard(shard);
      r.setTriggerMemberId(memberId);
      r.setMemberName(nameOf(m));
      r.setStoreId(reqVO.getStoreId());
      r.setPeriodKey(window.key);
      r.setPeriodStartTime(window.start.atStartOfDay());
      r.setPeriodEndTime(window.end.atTime(23, 59, 59));
      r.setResetEnabledSnapshot(s == null ? null : s.getResetEnabled());
      r.setResetTypeSnapshot(s == null ? null : s.getResetType());
      r.setResetDaysSnapshot(s == null ? null : s.getResetDays());
      r.setSignDate(today);
      r.setSignTime(signNow());
      r.setSignType(1);
      // 把今天加入当前周期签到日期，再计算签到后连续天数和累计天数。
      dates.add(today);
      r.setContinuousDaysAfter(calcContinuous(dates, today));
      r.setTotalDaysAfter(dates.size());

      try {
        // 写入签到记录分表；唯一键兜底防并发，确保同手机号同活动同一天只落一条。
        activitySignRecordMapper.insertRecord(recordTable(shard), r);
      } catch (DuplicateKeyException e) {
        // 如果数据库唯一键冲突，说明其他请求已经写入今天签到，补写 Redis 后按幂等返回。
        if (offset >= 0) {
          setCalendarBit(activity, calendarKey, offset);
        }
        return signResult(reqVO, mobile, Collections.emptyList(), false, true);
      }
      // 根据本次签到后的连续/累计天数同步发奖；外部发奖失败只落失败记录，不回滚签到记录。
      List<ActivitySignRewardRecordDO> issued = issueRewards(reqVO, m, mobile, r, s);
      // 奖励发放流程完成后再写 Redis bitmap：避免奖励记录入库异常导致事务回滚，但 Redis 已经显示已签到。
      if (offset >= 0) {
        setCalendarBit(activity, calendarKey, offset);
      }
      // 事务提交后再同步 ES，避免 ES 读模型先于 MySQL 主账本可见。
      afterCommit(
          () -> {
            // 同步签到记录到 ES，供 PC 签到记录、数据分析查询使用。
            sendSignRecordEvent(r);
            // 同步本次触发的奖励记录到 ES，供 PC 发放记录、我的奖励和数据分析查询使用。
            issued.forEach(this::sendRewardRecordEvent);
          });
      // 组装本次签到结果：包含今日签到状态、最新天数和本次命中奖励。
      return signResult(reqVO, mobile, issued, true);
    } finally {
      // 无论签到成功、发奖失败还是业务异常，都释放当前请求持有的分布式锁。
      redisCache.unlock(lockKey, lockValue);
    }
  }

  /** 小程序签到记录：入参只有活动ID，返回该手机号在活动中的已签到日期列表；优先 Redis/ES，失败降级 MySQL。 */
  @Override
  @DataPermission(enable = false)
  public AppActivitySignCalendarRespVO signRecord(AppActivitySignRecordReqVO reqVO) {
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    WxMemberDTO m = getMember(memberId);
    String mobile = mobileOf(m, memberId);
    AppActivitySignCalendarRespVO vo = new AppActivitySignCalendarRespVO();
    vo.setActivityId(reqVO.getActivityId());
    // 如果没有强制指定查 MySQL，优先读取 Redis/ES
    if (!forceMysql(reqVO.getForceMysql())) {
      try {
        List<LocalDate> dates = signDateList(reqVO.getActivityId(), mobile);
        vo.setSignDateList(dates.stream().map(LocalDate::toString).toList());
        return vo;
      } catch (Exception e) {
        log.warn(
            "小程序签到记录 Redis/ES 查询失败，降级查 MySQL, activityId={}, mobile={}",
            reqVO.getActivityId(),
            mobile,
            e);
      }
    }
    vo.setSignDateList(
        activitySignRecordMapper
            .selectByMobile(recordTable(shard(mobile)), reqVO.getActivityId(), mobile)
            .stream()
            .map(ActivitySignRecordDO::getSignDate)
            .map(LocalDate::toString)
            .toList());
    return vo;
  }

  /** 小程序我的奖励：按当前登录手机号查询当前活动已获得的奖励记录。 */
  @Override
  @DataPermission(enable = false)
  public AppActivitySignMyRewardRespVO myRewards(Long activityId) {
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    WxMemberDTO m = getMember(memberId);
    String mobile = mobileOf(m, memberId);
    AppActivitySignMyRewardRespVO vo = new AppActivitySignMyRewardRespVO();
    vo.setActivityId(activityId);
    vo.setRewardList(rewardRecords(activityId, mobile).stream().map(this::toMyReward).toList());
    return vo;
  }

  /** 小程序填写实物奖品收货地址：按当前登录会员手机号尾号直接定位分表并更新地址。 */
  @Override
  @DataPermission(enable = false)
  public Boolean savePhysicalAddress(AppActivitySignAddressReqVO reqVO) {
    Long memberId = SecurityFrameworkUtils.getLoginUserId();
    String mobile = mobileOf(getMember(memberId), memberId);
    String t = rewardTable(shard(mobile));
    ActivitySignRewardRecordDO r =
        activitySignRewardRecordMapper.selectById(t, reqVO.getRewardRecordId());
    // 奖励记录按手机号分表，且只能由奖励所属手机号填写地址，避免扫表和越权修改。
    if (r == null || !Objects.equals(r.getMemberMobile(), mobile)) {
      return false;
    }
    // 如果不是实物奖品，不允许填写收货地址，避免非实物奖励被误写物流信息。
    if (!Objects.equals(r.getPrizeType(), PRIZE_TYPE_PHYSICAL)) {
      return false;
    }
    // 如果实物奖品已经超时未填写，不能再通过地址接口把状态改回待发货。
    if (Objects.equals(r.getPrizeState(), PRIZE_STATE_ADDRESS_TIMEOUT)) {
      return false;
    }
    r.setReceiveUser(reqVO.getReceiveUser());
    r.setReceiveMobile(reqVO.getReceiveMobile());
    r.setReceiveAddress(reqVO.getReceiveAddress());
    int updated = activitySignRewardRecordMapper.updateAddress(t, r);
    // 如果收货地址更新成功，要同步 ES 读模型，否则 PC 发放记录按“已填写地址”筛选时会查到旧数据。
    if (updated > 0) {
      ActivitySignRewardRecordDO latest =
          activitySignRewardRecordMapper.selectById(t, reqVO.getRewardRecordId());
      afterCommit(() -> sendRewardRecordEvent(latest));
      return true;
    }
    return false;
  }

  /** 定时刷新实物奖品地址超时状态：发放超过48小时仍未填写地址的记录改为 prizeState=9，并同步 ES。 */
  @Override
  @DataPermission(enable = false)
  public Integer refreshPhysicalAddressTimeout() {
    LocalDateTime timeoutTime = signNow().minusHours(48);
    try {
      return refreshPhysicalAddressTimeoutFromEs(timeoutTime);
    } catch (Exception e) {
      log.warn("签到实物地址超时任务查询 ES 失败，降级扫描 MySQL timeoutTime={}", timeoutTime, e);
      return refreshPhysicalAddressTimeoutFromMysql(timeoutTime);
    }
  }

  /** 从 ES 查询超时实物候选记录，再按 id 回写 MySQL 主账本；MySQL 更新成功后同步 ES。 */
  private Integer refreshPhysicalAddressTimeoutFromEs(LocalDateTime timeoutTime) throws Exception {
    int total = 0;
    int batchSize = 500;
    while (true) {
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(
                          q ->
                              q.bool(
                                  b ->
                                      b.filter(
                                              f ->
                                                  f.term(
                                                      t ->
                                                          t.field("prizeType")
                                                              .value(FieldValue.of(PRIZE_TYPE_PHYSICAL))))
                                          .filter(
                                              f ->
                                                  f.term(
                                                      t ->
                                                          t.field("prizeState")
                                                              .value(
                                                                  FieldValue.of(
                                                                      PRIZE_STATE_ADDRESS_EMPTY))))
                                          .filter(
                                              f ->
                                                  f.range(
                                                      r ->
                                                          r.date(
                                                              dr ->
                                                                  dr.field("issueTime")
                                                                      .format(ES_LOCAL_DATE_TIME_FORMAT)
                                                                      .lte(
                                                                          toEsDateTimeString(
                                                                              timeoutTime)))))))
                      .sort(so -> so.field(f -> f.field("issueTime").order(SortOrder.Asc)))
                      .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)))
                      .size(batchSize),
              Map.class);
      List<Hit<Map>> hits = response.hits() == null ? Collections.emptyList() : response.hits().hits();
      // 如果 ES 没有超时候选记录，任务结束。
      if (CollectionUtils.isEmpty(hits)) {
        break;
      }
      int updatedInBatch = 0;
      for (Hit<Map> hit : hits) {
        ActivitySignRewardRecordDO record = toRewardRecordFromEs(hit.source());
        if (record == null || record.getId() == null || record.getMobileShard() == null) {
          continue;
        }
        String table = rewardTable(record.getMobileShard());
        // ES 只负责筛候选，最终仍以 MySQL 主账本的旧状态和 issue_time 条件做原子更新。
        int updated =
            activitySignRewardRecordMapper.updatePhysicalAddressTimeout(
                table, record.getId(), timeoutTime);
        if (updated > 0) {
          updatedInBatch += updated;
          total += updated;
          ActivitySignRewardRecordDO latest =
              activitySignRewardRecordMapper.selectById(table, record.getId());
          sendRewardRecordEvent(latest);
        }
      }
      // 如果 ES 返回不足一个批次，说明候选已扫完。
      if (hits.size() < batchSize) {
        break;
      }
      // 如果本批没有任何 MySQL 更新，可能 ES 有延迟脏数据，避免死循环。
      if (updatedInBatch == 0) {
        break;
      }
    }
    return total;
  }

  /** ES 不可用时降级扫描 MySQL，保证超时状态最终能被修正。 */
  private Integer refreshPhysicalAddressTimeoutFromMysql(LocalDateTime timeoutTime) {
    int total = 0;
    int batchSize = 500;
    // 奖励记录按手机号尾号分 10 张表，需要逐表扫描超时未填写地址的实物奖励。
    for (int i = 0; i < 10; i++) {
      String table = rewardTable(i);
      while (true) {
        List<ActivitySignRewardRecordDO> records =
            activitySignRewardRecordMapper.selectPhysicalAddressTimeout(
                table, timeoutTime, batchSize);
        // 如果当前分表没有超时记录，直接扫描下一张分表。
        if (CollectionUtils.isEmpty(records)) {
          break;
        }
        int updatedInBatch = 0;
        for (ActivitySignRewardRecordDO record : records) {
          // 按 id 和旧状态做原子更新；如果用户刚好填写地址，prize_state 已不是 1，这里不会误改成超时。
          int updated =
              activitySignRewardRecordMapper.updatePhysicalAddressTimeout(
                  table, record.getId(), timeoutTime);
          // 如果 MySQL 更新成功，需要回查最新记录同步 ES，保证 PC 查询/导出和小程序我的奖励状态一致。
          if (updated > 0) {
            updatedInBatch += updated;
            total += updated;
            ActivitySignRewardRecordDO latest =
                activitySignRewardRecordMapper.selectById(table, record.getId());
            sendRewardRecordEvent(latest);
          }
        }
        // 如果本批不足 batchSize，说明当前分表已经扫完。
        if (records.size() < batchSize) {
          break;
        }
        // 如果本批一条都没更新，说明数据被并发处理或状态已变化，避免死循环。
        if (updatedInBatch == 0) {
          break;
        }
      }
    }
    return total;
  }

  /** 红包回调状态更新入口：供微信回调同事按 outBillNo 修改未领取/已领取/已过期状态，并同步到 ES。 */
  @Override
  public Boolean updateSignRedPacketClaimStatusByOutBillNo(
      String outBillNo, Integer claimStatus, String failReason) {
    boolean ok = false;
    for (int i = 0; i < 10; i++)
      ok |=
          activitySignRewardRecordMapper.updateClaimStatusByOutBillNo(
                  rewardTable(i), outBillNo, claimStatus, failReason)
              > 0;
    // 如果红包状态至少有一条记录更新成功，需要同步到 ES
    if (ok) sendRewardClaimStatusEvent(outBillNo, claimStatus, failReason);
    return ok;
  }

  /** 红包回调状态更新入口：供微信回调同事按 outBillNo 修改未领取/已领取/已过期状态，并同步 MQ 到 ES。 */
  @Override
  public Boolean updateSignRedPacketClaimStatusByOutBillNo(
          String outBillNo, String failReason) {

    boolean ok = false;
    Integer claimStatus = null;

    for (int i = 0; i < 10; i++) {
      ActivitySignRewardRecordDO activitySignRewardRecordDO =
              activitySignRewardRecordMapper.selectByObn(rewardTable(i), outBillNo);

      if (activitySignRewardRecordDO != null) {
        // 计算 claimStatus
        claimStatus = calculateClaimStatus(activitySignRewardRecordDO.getUpdateTime());

        ok |= activitySignRewardRecordMapper.updateClaimStatusByOutBillNo(
                rewardTable(i), outBillNo, claimStatus, failReason)
                > 0;
      }
    }

    // 如果红包状态至少有一条记录更新成功，需要同步 MQ 到 ES
    if (ok && claimStatus != null) {
      sendRewardClaimStatusEvent(outBillNo, claimStatus, failReason);
    }
    return ok;
  }

  /**
   * 根据更新时间计算红包领取状态
   * @param updateTime 更新时间
   * @return 2-24小时内，3-超过24小时
   */
  private Integer calculateClaimStatus(LocalDateTime updateTime) {
    if (updateTime == null) {
      return 2; // 默认返回2（24小时内）
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime twentyFourHoursAgo = now.minusHours(24);

    // 如果更新时间在24小时之前，返回3；否则返回2
    if (updateTime.isBefore(twentyFourHoursAgo)) {
      return 3; // 超过24小时
    } else {
      return 2; // 24小时内
    }
  }

  private static final String ES_LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static final DateTimeFormatter ES_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(ES_LOCAL_DATE_TIME_FORMAT);

  /** 事务提交后执行回调：保证 ES 同步发生在 MySQL 最终账本提交之后。 */
  private void afterCommit(Runnable runnable) {
    // 如果当前存在 Spring 事务，ES 同步要等事务提交后执行，避免读模型早于 MySQL 最终账本。
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              runnable.run();
            }
          });
    } else {
      runnable.run();
    }
  }

  /** 同步签到记录到 ES：MySQL 已经提交成功，ES 写失败只打日志，后续可按活动手动补偿。 */
  private void sendSignRecordEvent(ActivitySignRecordDO record) {
    try {
      if (record == null || record.getId() == null) return;
      elasticsearchClient.index(
          i ->
              i.index(SIGN_RECORD_INDEX)
                  .id(String.valueOf(record.getId()))
                  .document(toSignDoc(record)));
    } catch (Exception e) {
      log.warn("签到记录同步 ES 失败 recordId={}", record == null ? null : record.getId(), e);
    }
  }

  /** 同步奖励发放记录到 ES：用于 PC 发放记录和奖励统计，失败不回滚已发奖励。 */
  private void sendRewardRecordEvent(ActivitySignRewardRecordDO record) {
    try {
      if (record == null || record.getId() == null) return;
      elasticsearchClient.index(
          i ->
              i.index(SIGN_REWARD_INDEX)
                  .id(String.valueOf(record.getId()))
                  .document(toRewardDoc(record)));
    } catch (Exception e) {
      log.warn("签到奖励记录同步 ES 失败 rewardRecordId={}", record == null ? null : record.getId(), e);
    }
  }

  /** 同步红包领取状态到 ES：红包回调已更新 MySQL 后，按 outBillNo 更新 ES 读模型。 */
  private void sendRewardClaimStatusEvent(
      String outBillNo, Integer claimStatus, String failReason) {
    try {
      if (!StringUtils.hasText(outBillNo) || claimStatus == null) return;
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(
                          q ->
                              q.bool(
                                  b ->
                                      b.filter(
                                          f ->
                                              f.term(
                                                  t ->
                                                      t.field("outBillNo")
                                                          .value(FieldValue.of(outBillNo))))))
                      .size(100),
              Map.class);
      if (response.hits() == null || response.hits().hits() == null) return;
      for (Hit<Map> hit : response.hits().hits()) {
        if (!StringUtils.hasText(hit.id())) continue;
        Map<String, Object> doc = new HashMap<>();
        doc.put("claimStatus", claimStatus);
        doc.put("failReason", failReason == null ? "" : failReason);
        doc.put("updateTime", formatEsDateTime(LocalDateTime.now()));
        elasticsearchClient.update(
            u -> u.index(SIGN_REWARD_INDEX).id(hit.id()).doc(doc), Map.class);
      }
    } catch (Exception e) {
      log.warn("签到红包领取状态同步 ES 失败 outBillNo={}", outBillNo, e);
    }
  }

  /** 签到记录转 ES 文档：字段名和 promotion_sign_record 索引 mapping 保持一致。 */
  private Map<String, Object> toSignDoc(ActivitySignRecordDO r) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", r.getId());
    doc.put("activityId", r.getActivityId());
    doc.put("memberMobile", r.getMemberMobile());
    doc.put("mobileShard", r.getMobileShard());
    doc.put("triggerMemberId", r.getTriggerMemberId());
    doc.put("memberName", r.getMemberName());
    doc.put("storeId", r.getStoreId());
    doc.put("storeName", r.getStoreName());
    doc.put("periodKey", r.getPeriodKey());
    doc.put("periodStartTime", formatEsDateTime(r.getPeriodStartTime()));
    doc.put("periodEndTime", formatEsDateTime(r.getPeriodEndTime()));
    doc.put("resetEnabledSnapshot", r.getResetEnabledSnapshot());
    doc.put("resetTypeSnapshot", r.getResetTypeSnapshot());
    doc.put("resetDaysSnapshot", r.getResetDaysSnapshot());
    doc.put("signDate", r.getSignDate() == null ? null : r.getSignDate().toString());
    doc.put("signTime", formatEsDateTime(r.getSignTime()));
    doc.put("signType", r.getSignType());
    doc.put("continuousDaysAfter", r.getContinuousDaysAfter());
    doc.put("totalDaysAfter", r.getTotalDaysAfter());
    doc.put("createTime", formatEsDateTime(r.getCreateTime()));
    doc.put("updateTime", formatEsDateTime(r.getUpdateTime()));
    doc.put("deleted", r.getDeleted() == null ? Boolean.FALSE : r.getDeleted());
    doc.put("businessId", r.getBusinessId());
    return doc;
  }

  /** 奖励发放记录转 ES 文档：字段名和 promotion_sign_reward_record 索引 mapping 保持一致。 */
  private Map<String, Object> toRewardDoc(ActivitySignRewardRecordDO r) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", r.getId());
    doc.put("activityId", r.getActivityId());
    doc.put("prizeId", r.getPrizeId());
    doc.put("memberMobile", r.getMemberMobile());
    doc.put("mobileShard", r.getMobileShard());
    doc.put("memberId", r.getMemberId());
    doc.put("memberName", r.getMemberName());
    doc.put("gender", r.getGender());
    doc.put("memberCategory", r.getMemberCategory());
    doc.put("storeId", r.getStoreId());
    doc.put("storeName", r.getStoreName());
    doc.put("periodKey", r.getPeriodKey());
    doc.put("periodStartTime", formatEsDateTime(r.getPeriodStartTime()));
    doc.put("periodEndTime", formatEsDateTime(r.getPeriodEndTime()));
    doc.put("resetEnabledSnapshot", r.getResetEnabledSnapshot());
    doc.put("resetTypeSnapshot", r.getResetTypeSnapshot());
    doc.put("resetDaysSnapshot", r.getResetDaysSnapshot());
    doc.put("rewardRuleSnapshot", r.getRewardRuleSnapshot());
    doc.put("prizeType", r.getPrizeType());
    doc.put("prizeContent", r.getPrizeContent());
    doc.put("prizeImgUrl", r.getPrizeImgUrl());
    doc.put("prizeValue", r.getPrizeValue() == null ? null : r.getPrizeValue().toPlainString());
    doc.put("signRuleType", r.getSignRuleType());
    doc.put("triggerDays", r.getTriggerDays());
    doc.put("signDate", r.getSignDate() == null ? null : r.getSignDate().toString());
    doc.put("signRecordId", r.getSignRecordId());
    doc.put("grantKey", r.getGrantKey());
    doc.put("issueStatus", r.getIssueStatus());
    doc.put("issueTime", formatEsDateTime(r.getIssueTime()));
    doc.put("failReason", r.getFailReason());
    doc.put("externalRecordId", r.getExternalRecordId());
    doc.put("prizeState", r.getPrizeState());
    doc.put("receiveUser", r.getReceiveUser());
    doc.put("receiveMobile", r.getReceiveMobile());
    doc.put("receiveAddress", r.getReceiveAddress());
    doc.put("trackingNumber", r.getTrackingNumber());
    doc.put("expressCompany", r.getExpressCompany());
    doc.put("outBillNo", r.getOutBillNo());
    doc.put("claimStatus", r.getClaimStatus());
    doc.put("packageInfo", r.getPackageInfo());
    doc.put("createTime", formatEsDateTime(r.getCreateTime()));
    doc.put("updateTime", formatEsDateTime(r.getUpdateTime()));
    doc.put("deleted", r.getDeleted() == null ? Boolean.FALSE : r.getDeleted());
    doc.put("businessId", r.getBusinessId());
    return doc;
  }

  /** LocalDateTime 转 ES mapping 支持的字符串格式。 */
  private String formatEsDateTime(LocalDateTime time) {
    return time == null ? null : ES_DATE_TIME_FORMATTER.format(time);
  }


  /**
   * 兼容 PC 签到记录查询入参：前端时间组件未选择时可能传空字符串，框架会转成 1970 年时间。
   * 这种时间不是用户真实筛选条件，必须清掉，否则 ES/MySQL 都会按 1970 时间范围过滤成空列表。
   */
  private void normalizeRecordPageReq(ActivitySignRecordPageReqVO reqVO) {
    if (reqVO == null) return;
    // 如果签到开始时间来自前端空字符串，按未选择开始时间处理。
    if (isEmptyClientTime(reqVO.getSignTimeStart())) reqVO.setSignTimeStart(null);
    // 如果签到结束时间来自前端空字符串，按未选择结束时间处理。
    if (isEmptyClientTime(reqVO.getSignTimeEnd())) reqVO.setSignTimeEnd(null);
  }

  /**
   * 兼容 PC 发放记录查询入参：前端发放时间为空字符串时清掉，避免把真实记录全部过滤掉。
   */
  private void normalizeRewardPageReq(ActivitySignRewardRecordPageReqVO reqVO) {
    if (reqVO == null) return;
    // 如果发放开始时间来自前端空字符串，按未选择开始时间处理。
    if (isEmptyClientTime(reqVO.getIssueTimeStart())) reqVO.setIssueTimeStart(null);
    // 如果发放结束时间来自前端空字符串，按未选择结束时间处理。
    if (isEmptyClientTime(reqVO.getIssueTimeEnd())) reqVO.setIssueTimeEnd(null);
    // 如果手机号只带了前后空格，按真实手机号内容查询，避免 ES 和 MySQL 条件不一致。
    if (reqVO.getMemberMobile() != null) reqVO.setMemberMobile(reqVO.getMemberMobile().trim());
  }

  /** 判断是否为前端空字符串被反序列化后的 1970 年占位时间。 */
  private boolean isEmptyClientTime(LocalDateTime time) {
    return time != null && time.isBefore(EMPTY_CLIENT_TIME_END);
  }

  /** 签到业务当前日期：默认取真实日期；后端自测时可通过隐藏 Header 模拟第 N 天签到。 */
  private LocalDate signToday() {
    LocalDate testDate = signTestDate();
    // 如果本次请求没有命中测试日期，使用服务器真实日期，保证线上和正常业务不受影响。
    if (testDate == null) return LocalDate.now();
    return testDate;
  }

  /** 签到业务当前时间：日期跟随测试日期，时分秒仍取服务器真实时间，避免影响 token 等全局校验。 */
  private LocalDateTime signNow() {
    LocalDate testDate = signTestDate();
    // 如果没有测试日期，使用真实当前时间。
    if (testDate == null) return LocalDateTime.now();
    return LocalDateTime.of(testDate, LocalTime.now());
  }

  /** 读取隐藏测试日期 Header：只在显式打开配置且非 prod 环境时生效，避免误带 Header 影响线上。 */
  private LocalDate signTestDate() {
    // 如果没有打开测试日期开关，完全忽略 Header。
    if (!Boolean.TRUE.equals(signTestDateEnabled)) return null;
    // 如果当前 profile 包含 prod，强制忽略 Header，防止线上被测试日期影响。
    if (isProdProfile()) return null;
    HttpServletRequest request = ServletUtils.getRequest();
    // 如果不是 HTTP 请求上下文，比如定时或 MQ 回调，不存在测试日期。
    if (request == null) return null;
    String text = request.getHeader(SIGN_TEST_DATE_HEADER);
    // 如果后端自测没有传 Header，继续走真实日期。
    if (!StringUtils.hasText(text)) return null;
    try {
      return LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      // 如果传了测试日期但格式不对，直接报错，避免误以为已经按指定日期测试。
      throw exception(SIGN_TEST_DATE_FORMAT_ERROR);
    }
  }

  /** 判断当前是否生产环境：兼容 spring.profiles.active=prod 或 local,prod 这类配置。 */
  private boolean isProdProfile() {
    if (!StringUtils.hasText(activeProfile)) return false;
    return Arrays.stream(activeProfile.split(",")).map(String::trim).anyMatch("prod"::equalsIgnoreCase);
  }

  /** 判断是否强制走 MySQL：true 表示跳过 Redis/ES 中间件，方便后端测试和线上排查。 */
  private boolean forceMysql(Boolean forceMysql) {
    return Boolean.TRUE.equals(forceMysql);
  }

  /** 生成门店命中缓存 key：ZSet 中按活动创建时间排序保存 activityId。 */
  private String storeHitKey(Long storeId) {
    return "promotion:sign:store:hit:" + storeId;
  }

  /** 生成全部门店活动命中缓存 key：全部门店活动不写 1300 多个门店 key，统一写全局 key。 */
  private String storeHitAllKey() {
    return "promotion:sign:store:hit:all";
  }

  /** 生成签到活动配置缓存 key：缓存活动主表、签到扩展和奖励配置，供小程序详情/签到高频读取。 */
  private String activityCacheKey(Long activityId) {
    return "promotion:sign:activity:v2:" + activityId;
  }

  /** 刷新签到活动配置缓存：创建、修改、启停后同步更新，长期有效活动不设置过期时间。 */
  private void refreshActivityCache(Long activityId) {
    try {
      if (activityId == null) return;
      ActivityDO activity = activityService.selectById(activityId);
      // 如果活动不存在，直接清理缓存，避免小程序读到旧配置。
      if (activity == null) {
        clearActivityCache(activityId);
        return;
      }
      ActivitySignCache cache = new ActivitySignCache();
      cache.setActivity(activity);
      cache.setStartTimeMs(activity.getStartDate() == null ? null : activity.getStartDate().getTime());
      cache.setEndTimeMs(activity.getEndDate() == null ? null : activity.getEndDate().getTime());
      cache.setSign(selectSignByActivityIdFromDb(activityId));
      cache.setRewards(selectRewardsFromDb(activityId));
      redisCache.setCacheObject(activityCacheKey(activityId), cache);
      long ttlSeconds = calcStoreHitTtlSeconds(activity.getEndDate());
      // 如果活动有结束时间，活动配置缓存保留到结束后7天；长期有效不设置 TTL。
      if (ttlSeconds > 0) {
        redisCache.expire(activityCacheKey(activityId), ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      log.warn("刷新签到活动配置缓存失败 activityId={}", activityId, e);
    }
  }

  /** 清理签到活动配置缓存：删除活动或发现活动不存在时使用。 */
  private void clearActivityCache(Long activityId) {
    if (activityId == null) return;
    redisCache.deleteObject(activityCacheKey(activityId));
  }

  /** 获取签到活动配置缓存：缓存缺失时查库并回写，保证小程序后续请求走缓存。 */
  private ActivitySignCache getActivityCache(Long activityId) {
    try {
      Object cache = redisCache.getCacheObject(activityCacheKey(activityId));
      // 如果 Redis 里有完整活动配置，直接返回，避免小程序详情/签到重复查活动、签到扩展和奖励配置表。
      if (cache instanceof ActivitySignCache activitySignCache
          && activitySignCache.getActivity() != null
          && activitySignCache.getSign() != null
          && activitySignCache.getRewards() != null) {
        restoreActivityTime(activitySignCache);
        return activitySignCache;
      }
    } catch (Exception e) {
      log.warn("读取签到活动配置缓存失败，降级查 MySQL activityId={}", activityId, e);
    }
    ActivityDO activity = activityService.selectById(activityId);
    if (activity == null) return null;
    ActivitySignCache cache = new ActivitySignCache();
    cache.setActivity(activity);
    cache.setStartTimeMs(activity.getStartDate() == null ? null : activity.getStartDate().getTime());
    cache.setEndTimeMs(activity.getEndDate() == null ? null : activity.getEndDate().getTime());
    cache.setSign(selectSignByActivityIdFromDb(activityId));
    cache.setRewards(selectRewardsFromDb(activityId));
    restoreActivityTime(cache);
    refreshActivityCache(activityId);
    return cache;
  }

  /** 用缓存里保存的原始毫秒回填活动时间，避免 Redis 序列化导致 Date 时区偏移。 */
  private void restoreActivityTime(ActivitySignCache cache) {
    if (cache == null || cache.getActivity() == null) {
      return;
    }
    ActivityDO activity = cache.getActivity();
    if (cache.getStartTimeMs() != null) {
      activity.setStartDate(new Date(cache.getStartTimeMs()));
    }
    if (cache.getEndTimeMs() != null) {
      activity.setEndDate(new Date(cache.getEndTimeMs()));
    }
  }

  /** 刷新门店命中缓存：活动启用时写入门店或全部门店命中缓存，供小程序快速命中活动。 */
  private void refreshStoreHitCache(Long activityId, List<Long> storeIds, Integer enabled) {
    // 如果活动不是启用状态，不需要写门店命中缓存
    if (!Objects.equals(enabled, ENABLED)) return;
    ActivityDO activity = activityService.selectById(activityId);
    // 如果活动不存在，或者活动不是进行中，不需要写门店命中缓存
    if (activity == null || statusOf(activity) != 1) return;
    double score =
        activity.getCreateTime() == null
            ? activityId.doubleValue()
            : activity.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    // 如果活动适用全部门店，写全局 key，避免循环写入 1300 多个门店 key
    if (Objects.equals(activity.getActivityStore(), 1)) {
      addStoreHit(storeHitAllKey(), activityId, score, null, activity.getEndDate());
      return;
    }
    // 如果部分门店活动没有门店ID列表，不需要写门店命中缓存
    if (CollectionUtils.isEmpty(storeIds)) return;
    for (Long storeId : storeIds) {
      addStoreHit(storeHitKey(storeId), activityId, score, storeId, activity.getEndDate());
    }
  }

  /** 写入单个门店命中 ZSet；key 可以是具体门店 key，也可以是全部门店 key。 */
  private void addStoreHit(String key, Long activityId, double score, Long storeId, Date endDate) {
    try {
      redisCache.getRedisTemplate().opsForZSet().add(key, String.valueOf(activityId), score);
      long ttlSeconds = calcStoreHitTtlSeconds(endDate);
      // 如果活动已配置结束时间，门店命中缓存保留到活动结束后7天，避免活动期间 key 提前过期。
      if (ttlSeconds > 0) {
        redisCache.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      log.warn("刷新签到门店命中缓存失败 activityId={}, storeId={}", activityId, storeId, e);
    }
  }

  /** 计算门店命中缓存 TTL：活动结束时间到当前时间的秒数 + 7天缓冲。 */
  private long calcStoreHitTtlSeconds(Date endDate) {
    if (endDate == null) {
      return 0;
    }
    long secondsToEnd =
        Duration.between(LocalDateTime.now(), toEndLdt(endDate)).getSeconds();
    return secondsToEnd + Duration.ofDays(7).getSeconds();
  }

  /** 清理门店命中缓存：活动修改门店、停用或删除时移除旧 activityId。 */
  private void clearStoreHitCache(List<Long> storeIds, Long activityId) {
    // 如果活动ID为空，不需要清理门店命中缓存
    if (activityId == null) return;
    removeStoreHit(storeHitAllKey(), activityId, null);
    // 如果门店ID列表为空，只需要清理全部门店 key
    if (CollectionUtils.isEmpty(storeIds)) return;
    for (Long storeId : storeIds) {
      removeStoreHit(storeHitKey(storeId), activityId, storeId);
    }
  }

  /** 从单个门店命中 ZSet 移除 activityId；key 可以是具体门店 key，也可以是全部门店 key。 */
  private void removeStoreHit(String key, Long activityId, Long storeId) {
    try {
      redisCache.getRedisTemplate().opsForZSet().remove(key, String.valueOf(activityId));
    } catch (Exception e) {
      log.warn("清理签到门店命中缓存失败 activityId={}, storeId={}", activityId, storeId, e);
    }
  }

  /** 从 Redis 门店命中缓存读取当前可用活动：活动详情优先走活动配置缓存，缓存脏数据会边读边清理，失败由调用方降级 MySQL。 */
  private ActivityDO findRunningActivityFromStoreHitCache(Long storeId) {
    try {
      LinkedHashSet<Long> allStoreActivityIds = new LinkedHashSet<>();
      LinkedHashSet<Long> currentStoreActivityIds = new LinkedHashSet<>();
      readStoreHitIds(storeHitAllKey(), allStoreActivityIds);
      // 如果当前门店ID不为空，需要同时读取该门店自己的部分门店活动缓存
      if (storeId != null) readStoreHitIds(storeHitKey(storeId), currentStoreActivityIds);
      LinkedHashSet<Long> activityIds = new LinkedHashSet<>();
      activityIds.addAll(allStoreActivityIds);
      activityIds.addAll(currentStoreActivityIds);
      // 如果全局 key 和门店 key 都没有命中缓存，返回 null 交给 MySQL 降级查询
      if (CollectionUtils.isEmpty(activityIds)) return null;
      List<ActivityDO> validActivities = new ArrayList<>();
      for (Long activityId : activityIds) {
        ActivityDO activity = getActivityById(activityId);
        // 如果缓存里的活动不存在、未开始、已结束或已停用，需要清理脏缓存
        if (activity == null || statusOf(activity) != 1) {
          removeStaleStoreHit(storeId, activityId);
          continue;
        }
        boolean fromAllStoreKey = allStoreActivityIds.contains(activityId);
        boolean fromCurrentStoreKey = currentStoreActivityIds.contains(activityId);
        // 如果活动来自全部门店 key，则活动本身必须仍是全部门店；否则说明缓存脏了，清理后跳过。
        if (fromAllStoreKey && !Objects.equals(activity.getActivityStore(), 1)) {
          removeStoreHit(storeHitAllKey(), activityId, null);
          // 如果该活动没有来自当前门店 key，说明当前门店也不能命中这个部分门店活动。
          if (!fromCurrentStoreKey) continue;
        }
        // 如果活动不是全部门店，也没有来自当前门店 key，说明当前门店不能命中该活动。
        if (!Objects.equals(activity.getActivityStore(), 1) && !fromCurrentStoreKey) continue;
        validActivities.add(activity);
      }
      validActivities.sort(
          Comparator.comparing(
                  ActivityDO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(ActivityDO::getId, Comparator.nullsLast(Comparator.naturalOrder())));
      // 能走到这里的部分门店活动已经由当前门店 Redis key 命中校验过，不再查 activity_store 表。
      return validActivities.stream().findFirst().orElse(null);
    } catch (Exception e) {
      log.warn("读取签到门店命中缓存失败，降级查 MySQL, storeId={}", storeId, e);
      return null;
    }
  }

  /** 读取一个门店命中 ZSet 的 activityId，按创建时间升序最多读取 20 个候选，保证同门店多个活动时最早创建的活动优先命中。 */
  private void readStoreHitIds(String key, Set<Long> activityIds) {
    Set<Object> values = redisCache.getRedisTemplate().opsForZSet().range(key, 0, 20);
    // 如果当前 key 没有缓存候选活动，不需要处理
    if (CollectionUtils.isEmpty(values)) return;
    values.stream()
        .filter(Objects::nonNull)
        .map(obj -> Long.valueOf(String.valueOf(obj)))
        .forEach(activityIds::add);
  }

  /** 清理缓存脏 activityId：全局 key 一定清，具体门店 key 在 storeId 不为空时清。 */
  private void removeStaleStoreHit(Long storeId, Long activityId) {
    removeStoreHit(storeHitAllKey(), activityId, null);
    // 如果当前门店ID不为空，需要同步清理该门店自己的命中缓存
    if (storeId != null) removeStoreHit(storeHitKey(storeId), activityId, storeId);
  }

  /** PC 数据分析折线图：按日期返回 PV/UV，访问数据统一从埋点 event_logs 统计，不再维护签到访问流水表。 */
  @Override
  public ActivitySignDailyAnalysisRespVO dailyAnalysis(ActivitySignDailyAnalysisReqVO reqVO) {
    LocalDateTime start = reqVO.getStartTime();
    LocalDateTime end = reqVO.getEndTime();
    // 如果前端未传时间，默认查询最近 30 天，和集卡 getActivityJkDailyAnalysis 保持一致。
    if (start == null && end == null) {
      end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
      start = end.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    // 如果只传开始或结束，补齐另一端，避免 ES range 条件不完整。
    if (start == null) start = end.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
    if (end == null) end = start.plusDays(29).withHour(23).withMinute(59).withSecond(59).withNano(0);
    if (!forceMysql(reqVO.getForceMysql())) {
      try {
        ActivitySignDailyAnalysisRespVO eventResult = dailyAnalysisFromEventLog(reqVO, start, end);
        // 统一埋点返回完整日期数组，说明查询链路正常，直接给 PC 折线图使用。
        if (eventResult != null && !CollectionUtils.isEmpty(eventResult.getDateList())) return eventResult;
      } catch (Exception e) {
        log.warn("签到活动数据分析折线图统一埋点查询失败，降级查 MySQL, req={}", reqVO, e);
      }
    }
    // 如果统一埋点查询失败，旧访问流水表已废弃，返回空日期轴避免接口报错。
    return toDailyResp(initDailyMap(start, end));
  }

  /**
   * 统一埋点按天统计签到活动 PV/UV：前端活动埋点统一上报 eventType=activity，
   * 签到趋势图按 eventType=activity + eventId=活动ID 统计，避免把分享埋点算入 PV/UV。
   */
  private ActivitySignDailyAnalysisRespVO dailyAnalysisFromEventLog(
      ActivitySignDailyAnalysisReqVO reqVO, LocalDateTime start, LocalDateTime end) {
    try {
      Map<String, Map<String, Long>> dayMap = initDailyMap(start, end);
      List<String> indices = resolveEventLogIndices(start, end);
      if (CollectionUtils.isEmpty(indices)) return toDailyResp(dayMap);
      BoolQuery.Builder b = new BoolQuery.Builder();
      b.filter(f -> f.term(t -> t.field("eventType").value(FieldValue.of("activity"))));
      b.filter(
          f ->
              f.term(
                  t ->
                      t.field("eventId")
                          .value(FieldValue.of(String.valueOf(reqVO.getActivityId())))));
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("timestamp")
                                  .gte(toEsDateMillis(start))
                                  .lte(toEsDateMillis(end)))));
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(indices)
                      .ignoreUnavailable(true)
                      .query(q -> q.bool(b.build()))
                      .size(0)
                      .aggregations(
                          "day_stats",
                          Aggregation.of(
                              a ->
                                  a.dateHistogram(
                                          dh ->
                                              dh.field("timestamp")
                                                  .fixedInterval(Time.of(t -> t.time("1d")))
                                                  .format("yyyy-MM-dd")
                                                  .timeZone("Asia/Shanghai")
                                                  .minDocCount(0))
                                      .aggregations(
                                          "uv",
                                          Aggregation.of(
                                              uv ->
                                                  uv.cardinality(
                                                      c ->
                                                          c.field("memberId")
                                                              .precisionThreshold(40000)))))),
              Map.class);
      Aggregate aggregate = response.aggregations() == null ? null : response.aggregations().get("day_stats");
      if (aggregate == null || aggregate.dateHistogram() == null) return null;
      for (DateHistogramBucket bucket : aggregate.dateHistogram().buckets().array()) {
        String day = bucket.keyAsString();
        if (!StringUtils.hasText(day)) continue;
        Map<String, Long> values = dayMap.computeIfAbsent(day, k -> new HashMap<>());
        values.put("pv", bucket.docCount());
        values.put("uv", cardinality(bucket.aggregations().get("uv")));
      }
      return toDailyResp(dayMap);
    } catch (Exception e) {
      log.warn("签到活动统一埋点折线图查询失败 activityId={}", reqVO.getActivityId(), e);
      return null;
    }
  }

  /** 初始化日期范围，保证折线图每天都有 pv/uv 两个值。 */
  private Map<String, Map<String, Long>> initDailyMap(LocalDateTime start, LocalDateTime end) {
    Map<String, Map<String, Long>> map = new LinkedHashMap<>();
    LocalDate current = start.toLocalDate();
    LocalDate last = end.toLocalDate();
    while (!current.isAfter(last)) {
      Map<String, Long> values = new HashMap<>();
      values.put("pv", 0L);
      values.put("uv", 0L);
      map.put(current.toString(), values);
      current = current.plusDays(1);
    }
    return map;
  }

  /** 把按天 Map 转成前端折线图需要的数组结构。 */
  private ActivitySignDailyAnalysisRespVO toDailyResp(Map<String, Map<String, Long>> dayMap) {
    ActivitySignDailyAnalysisRespVO vo = new ActivitySignDailyAnalysisRespVO();
    vo.setDateList(new ArrayList<>(dayMap.keySet()));
    vo.setPvList(dayMap.values().stream().map(v -> v.getOrDefault("pv", 0L)).toList());
    vo.setUvList(dayMap.values().stream().map(v -> v.getOrDefault("uv", 0L)).toList());
    return vo;
  }

  /** Date 转 LocalDateTime：活动主表保存的是 Date，数据分析和事件统计统一用 LocalDateTime。 */
  private LocalDateTime toLocalDateTime(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  /** 统一埋点汇总 PV/UV：只按 eventId=活动ID 统计，不再依赖前端 eventType。 */
  private Map<String, Long> statEventLogPvUv(ActivitySignAnalysisSummaryReqVO reqVO) {
    try {
      LocalDateTime start = analysisStartTime(reqVO);
      LocalDateTime end = analysisEndTime(reqVO);
      List<String> indices = resolveEventLogIndices(start, end);
      if (CollectionUtils.isEmpty(indices)) return zeroPvUvMap();
      BoolQuery.Builder b = new BoolQuery.Builder();
      b.filter(f -> f.term(t -> t.field("eventType").value(FieldValue.of("activity"))));
      b.filter(
          f ->
              f.term(
                  t ->
                      t.field("eventId")
                          .value(FieldValue.of(String.valueOf(reqVO.getActivityId())))));
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("timestamp")
                                  .gte(toEsDateMillis(start))
                                  .lte(toEsDateMillis(end)))));
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(indices)
                      .ignoreUnavailable(true)
                      .query(q -> q.bool(b.build()))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true))
                      .aggregations(
                          "uv_users",
                          Aggregation.of(
                              a ->
                                  a.cardinality(
                                      c -> c.field("memberId").precisionThreshold(40000)))),
              Map.class);
      long pv = totalHits(response);
      Map<String, Long> result = new HashMap<>(2);
      result.put("pv", pv);
      result.put("uv", cardinality(response.aggregations().get("uv_users")));
      return result;
    } catch (Exception e) {
      log.warn("签到活动统一埋点汇总查询失败 activityId={}", reqVO.getActivityId(), e);
      return zeroPvUvMap();
    }
  }

  /** 返回 PV/UV 都为 0 的默认统计结果，避免埋点为空时影响签到和奖励统计。 */
  private Map<String, Long> zeroPvUvMap() {
    Map<String, Long> result = new HashMap<>(2);
    result.put("pv", 0L);
    result.put("uv", 0L);
    return result;
  }

  /** 按时间范围解析 event_logs 月索引。 */
  private List<String> resolveEventLogIndices(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) return Collections.emptyList();
    YearMonth current = YearMonth.from(start);
    YearMonth last = YearMonth.from(end);
    List<String> indices = new ArrayList<>();
    while (!current.isAfter(last)) {
      indices.add("event_logs_" + current.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
      current = current.plusMonths(1);
    }
    return indices;
  }

  /** 数据分析开始时间：优先使用周期筛选，没有周期时按活动开始日期统计。 */
  private LocalDateTime analysisStartTime(ActivitySignAnalysisSummaryReqVO reqVO) {
    if (StringUtils.hasText(reqVO.getPeriodKey())) {
      String[] arr = reqVO.getPeriodKey().split("_");
      if (arr.length == 2) return LocalDate.parse(arr[0]).atStartOfDay();
    }
    ActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
    if (activity != null && activity.getStartDate() != null) {
      return toLocalDateTime(activity.getStartDate()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    return LocalDate.now().minusDays(29).atStartOfDay();
  }

  /** 数据分析结束时间：优先使用周期筛选，没有周期时按活动结束日期统计。 */
  private LocalDateTime analysisEndTime(ActivitySignAnalysisSummaryReqVO reqVO) {
    if (StringUtils.hasText(reqVO.getPeriodKey())) {
      String[] arr = reqVO.getPeriodKey().split("_");
      if (arr.length == 2) return LocalDate.parse(arr[1]).atTime(23, 59, 59);
    }
    ActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
    if (activity != null && activity.getEndDate() != null) {
      return toLocalDateTime(activity.getEndDate()).withHour(23).withMinute(59).withSecond(59).withNano(0);
    }
    return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
  }

  /** 分享人数：参考集卡 buildShareResult，按埋点 share 事件 UV 统计分享人数。 */
  private Long shareCount(ActivitySignAnalysisSummaryReqVO reqVO) {
    try {
      LocalDateTime start = null;
      LocalDateTime end = null;
      ActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
      if (StringUtils.hasText(reqVO.getPeriodKey())) {
        String[] arr = reqVO.getPeriodKey().split("_");
        if (arr.length == 2) {
          start = LocalDate.parse(arr[0]).atStartOfDay();
          end = LocalDate.parse(arr[1]).atTime(23, 59, 59);
        }
      }
      if (start == null && activity != null && activity.getStartDate() != null) {
        start = toLocalDateTime(activity.getStartDate()).withHour(0).withMinute(0).withSecond(0).withNano(0);
      }
      if (end == null && activity != null && activity.getEndDate() != null) {
        end = toLocalDateTime(activity.getEndDate()).withHour(23).withMinute(59).withSecond(59).withNano(0);
      }
      if (start == null || end == null) return 0L;
      return eventService.statUv(
          EventType.SIGN_SHARE,
          String.valueOf(reqVO.getActivityId()),
          start,
          end,
          BusinessContextHolder.getBusinessId());
    } catch (Exception e) {
      log.warn("签到活动分享人数统计失败 activityId={}", reqVO.getActivityId(), e);
      return 0L;
    }
  }

  /** 从 ES 聚合 PC 数据分析顶部指标：无数据或异常返回 null，外层降级 MySQL。 */
  private ActivitySignAnalysisSummaryRespVO analysisSummaryFromEs(
      ActivitySignAnalysisSummaryReqVO reqVO) {
    try {
      SearchResponse<ActivitySignRecordDO> signResponse =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_RECORD_INDEX)
                      .query(q -> q.bool(buildAnalysisSignBool(reqVO)))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true))
                      .aggregations(
                          "join_users",
                          Aggregation.of(
                              a ->
                                  a.cardinality(
                                      c -> c.field("memberMobile").precisionThreshold(40000)))),
              ActivitySignRecordDO.class);

      Map<String, Long> visitMap = statEventLogPvUv(reqVO);
      if (visitMap == null) return null;

      SearchResponse<ActivitySignRewardRecordDO> rewardResponse =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(q -> q.bool(buildAnalysisRewardBool(reqVO)))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true))
                      .aggregations(
                          "reward_users",
                          Aggregation.of(
                              a ->
                                  a.cardinality(
                                      c -> c.field("memberMobile").precisionThreshold(40000))))
                      .aggregations(
                          "point_amount",
                          Aggregation.of(
                              a ->
                                  a.filter(
                                          f ->
                                              f.term(
                                                  t ->
                                                      t.field("prizeType").value(FieldValue.of(2))))
                                      .aggregations(
                                          "sum_value",
                                          Aggregation.of(
                                              sa -> sa.sum(sm -> sm.field("prizeValue"))))))
                      .aggregations(
                          "coupon_count",
                          Aggregation.of(
                              a ->
                                  a.filter(
                                      f ->
                                          f.term(
                                              t -> t.field("prizeType").value(FieldValue.of(1))))))
                      .aggregations(
                          "coupon_package_count",
                          Aggregation.of(
                              a ->
                                  a.filter(
                                      f ->
                                          f.term(
                                              t -> t.field("prizeType").value(FieldValue.of(6))))))
                      .aggregations(
                          "physical_count",
                          Aggregation.of(
                              a ->
                                  a.filter(
                                      f ->
                                          f.term(
                                              t -> t.field("prizeType").value(FieldValue.of(3))))))
                      .aggregations(
                          "red_packet_amount",
                          Aggregation.of(
                              a ->
                                  a.filter(
                                          f ->
                                              f.term(
                                                  t ->
                                                      t.field("prizeType").value(FieldValue.of(5))))
                                      .aggregations(
                                          "sum_value",
                                          Aggregation.of(
                                              sa -> sa.sum(sm -> sm.field("prizeValue")))))),
              ActivitySignRewardRecordDO.class);

      long visitTotal = visitMap == null ? 0L : visitMap.getOrDefault("pv", 0L);
      long signTotal = totalHits(signResponse);
      long rewardTotal = totalHits(rewardResponse);
      if (visitTotal == 0L && signTotal == 0L && rewardTotal == 0L) return null;
      ActivitySignAnalysisSummaryRespVO vo = new ActivitySignAnalysisSummaryRespVO();
      vo.setPv(visitTotal);
      vo.setUv(visitMap == null ? 0L : visitMap.getOrDefault("uv", 0L));
      vo.setShareCount(shareCount(reqVO));
      vo.setSignCount(signTotal);
      vo.setJoinCount(cardinality(signResponse.aggregations().get("join_users")));
      vo.setRewardIssueCount(rewardTotal);
      vo.setRewardUserCount(cardinality(rewardResponse.aggregations().get("reward_users")));
      vo.setPointAmount(
          BigDecimal.valueOf(
              nestedSum(rewardResponse.aggregations().get("point_amount"), "sum_value")));
      vo.setCouponCount(filterCount(rewardResponse.aggregations().get("coupon_count")));
      vo.setCouponPackageCount(
          filterCount(rewardResponse.aggregations().get("coupon_package_count")));
      vo.setPhysicalCount(filterCount(rewardResponse.aggregations().get("physical_count")));
      vo.setRedPacketAmount(
          BigDecimal.valueOf(
              nestedSum(rewardResponse.aggregations().get("red_packet_amount"), "sum_value")));
      return vo;
    } catch (Exception e) {
      log.warn("签到活动数据分析 ES 查询异常，准备降级 MySQL, req={}", reqVO, e);
      return null;
    }
  }

  /** 从 ES 查询签到记录分页：无数据或异常返回 null，外层降级 MySQL。 */
  private ActivitySignRecordPageRespVO recordPageFromEs(ActivitySignRecordPageReqVO reqVO) {
    try {
      int pageNo = Math.max(reqVO.getPageNo(), 1);
      int pageSize = Math.max(reqVO.getPageSize(), 10);
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_RECORD_INDEX)
                      .query(q -> q.bool(buildRecordPageBool(reqVO)))
                      .from((pageNo - 1) * pageSize)
                      .size(pageSize)
                      .trackTotalHits(t -> t.enabled(true))
                      .sort(sort -> sort.field(f -> f.field("signTime").order(SortOrder.Desc)))
                      .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc)))
                      .aggregations(
                          "sign_users",
                          Aggregation.of(
                              a ->
                                  a.cardinality(
                                      c -> c.field("memberMobile").precisionThreshold(40000)))),
              Map.class);
      long total = totalHits(response);
      if (total == 0L) return null;
      ActivitySignRecordPageRespVO vo = new ActivitySignRecordPageRespVO();
      vo.setTotal(total);
      vo.setSignCount(vo.getTotal());
      vo.setSignUserCount(cardinality(response.aggregations().get("sign_users")));
      List<ActivitySignRecordDO> records =
          response.hits().hits().stream()
              .map(Hit::source)
              .filter(Objects::nonNull)
              .map(this::toSignRecordFromEs)
              .filter(Objects::nonNull)
              .toList();
      fillRecordDisplayNames(records);
      vo.setList(records.stream().map(r -> toRecordResp(r, reqVO.getContinuousCycleDays())).toList());
      return vo;
    } catch (Exception e) {
      log.warn("签到记录 ES 查询异常，准备降级 MySQL, req={}", reqVO, e);
      return null;
    }
  }

  /** 校验 ES 返回的发放记录是否仍落在请求时间范围内；用于防御 ES date 查询格式/时区口径异常。 */
  private boolean rewardPageIssueTimeMatched(
      ActivitySignRewardRecordPageRespVO esResult, ActivitySignRewardRecordPageReqVO reqVO) {
    if (esResult == null || CollectionUtils.isEmpty(esResult.getList())) return true;
    if (reqVO.getIssueTimeStart() == null && reqVO.getIssueTimeEnd() == null) return true;
    for (ActivitySignRewardRecordRespVO item : esResult.getList()) {
      LocalDateTime issueTime = item.getIssueTime();
      if (issueTime == null) continue;
      if (reqVO.getIssueTimeStart() != null && issueTime.isBefore(reqVO.getIssueTimeStart())) {
        return false;
      }
      if (reqVO.getIssueTimeEnd() != null && issueTime.isAfter(reqVO.getIssueTimeEnd())) {
        return false;
      }
    }
    return true;
  }

  /** 从 ES 查询奖励发放记录分页：无数据或异常返回 null，外层降级 MySQL。 */
  private ActivitySignRewardRecordPageRespVO rewardRecordPageFromEs(
      ActivitySignRewardRecordPageReqVO reqVO) {
    try {
      int pageNo = Math.max(reqVO.getPageNo(), 1);
      int pageSize = Math.max(reqVO.getPageSize(), 10);
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(q -> q.bool(buildRewardPageBool(reqVO)))
                      .from((pageNo - 1) * pageSize)
                      .size(pageSize)
                      .trackTotalHits(t -> t.enabled(true))
                      .sort(sort -> sort.field(f -> f.field("issueTime").order(SortOrder.Desc)))
                      .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc)))
                      .aggregations(
                          "reward_users",
                          Aggregation.of(
                              a ->
                                  a.cardinality(
                                      c -> c.field("memberMobile").precisionThreshold(40000)))),
              Map.class);
      long total = totalHits(response);
      if (total == 0L) return null;
      ActivitySignRewardRecordPageRespVO vo = new ActivitySignRewardRecordPageRespVO();
      vo.setTotal(total);
      vo.setRewardIssueCount(vo.getTotal());
      vo.setRewardUserCount(cardinality(response.aggregations().get("reward_users")));
      List<ActivitySignRewardRecordDO> records =
          response.hits().hits().stream()
              .map(Hit::source)
              .filter(Objects::nonNull)
              .map(this::toRewardRecordFromEs)
              .filter(Objects::nonNull)
              .toList();
      fillRewardDisplayNames(records);
      vo.setList(records.stream().map(this::toRewardResp).toList());
      return vo;
    } catch (Exception e) {
      log.warn("签到奖励发放记录 ES 查询异常，准备降级 MySQL, req={}", reqVO, e);
      return null;
    }
  }

  /** 构建 ES 活动访问数据分析查询条件：activityId 必填，periodKey/storeId 可选。 */
  private BoolQuery buildAnalysisVisitBool(ActivitySignAnalysisSummaryReqVO reqVO) {
    BoolQuery.Builder b = new BoolQuery.Builder();
    b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(reqVO.getActivityId()))));
    if (StringUtils.hasText(reqVO.getPeriodKey()))
      b.filter(f -> f.term(t -> t.field("periodKey").value(FieldValue.of(reqVO.getPeriodKey()))));
    return b.build();
  }

  /** 构建 ES 签到记录数据分析查询条件：activityId 必填，periodKey/storeId 可选。 */
  private BoolQuery buildAnalysisSignBool(ActivitySignAnalysisSummaryReqVO reqVO) {
    BoolQuery.Builder b = new BoolQuery.Builder();
    b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(reqVO.getActivityId()))));
    if (StringUtils.hasText(reqVO.getPeriodKey()))
      b.filter(f -> f.term(t -> t.field("periodKey").value(FieldValue.of(reqVO.getPeriodKey()))));
    return b.build();
  }

  /** 构建 ES 奖励记录数据分析查询条件：activityId 必填，periodKey/storeId 可选。 */
  private BoolQuery buildAnalysisRewardBool(ActivitySignAnalysisSummaryReqVO reqVO) {
    BoolQuery.Builder b = new BoolQuery.Builder();
    b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(reqVO.getActivityId()))));
    if (StringUtils.hasText(reqVO.getPeriodKey()))
      b.filter(f -> f.term(t -> t.field("periodKey").value(FieldValue.of(reqVO.getPeriodKey()))));
    return b.build();
  }

  /** 构建 ES 签到记录分页查询条件：手机号模糊、门店、签到时间区间。签到 ES 文档按本地业务时间字符串写入，查询也必须用同格式。 */
  private BoolQuery buildRecordPageBool(ActivitySignRecordPageReqVO reqVO) {
    BoolQuery.Builder b = new BoolQuery.Builder();
    b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(reqVO.getActivityId()))));
    if (StringUtils.hasText(reqVO.getMemberMobile()))
      b.filter(
          f -> f.wildcard(w -> w.field("memberMobile").value("*" + reqVO.getMemberMobile() + "*")));
    if (reqVO.getStoreId() != null)
      b.filter(f -> f.term(t -> t.field("storeId").value(FieldValue.of(reqVO.getStoreId()))));
    if (reqVO.getSignTimeStart() != null)
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("signTime")
                                  .format(ES_LOCAL_DATE_TIME_FORMAT)
                                  .gte(toEsDateTimeString(reqVO.getSignTimeStart())))));
    if (reqVO.getSignTimeEnd() != null)
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("signTime")
                                  .format(ES_LOCAL_DATE_TIME_FORMAT)
                                  .lte(toEsDateTimeString(reqVO.getSignTimeEnd())))));
    return b.build();
  }

  /** 构建 ES 奖励发放记录分页查询条件：手机号、奖励类型、红包状态、地址/物流状态、发放时间。发放时间按本地业务时间字符串比较。 */
  private BoolQuery buildRewardPageBool(ActivitySignRewardRecordPageReqVO reqVO) {
    BoolQuery.Builder b = new BoolQuery.Builder();
    b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(reqVO.getActivityId()))));
    addEsNotDeletedFilter(b);
    if (StringUtils.hasText(reqVO.getMemberMobile()))
      b.filter(
          f -> f.wildcard(w -> w.field("memberMobile").value("*" + reqVO.getMemberMobile() + "*")));
    if (reqVO.getPrizeType() != null)
      b.filter(f -> f.term(t -> t.field("prizeType").value(FieldValue.of(reqVO.getPrizeType()))));
    if (!CollectionUtils.isEmpty(reqVO.getClaimStatusList()))
      b.filter(
          f ->
              f.terms(
                  t ->
                      t.field("claimStatus")
                          .terms(
                              ts ->
                                  ts.value(
                                      reqVO.getClaimStatusList().stream()
                                          .map(FieldValue::of)
                                          .toList()))));
    if (Objects.equals(reqVO.getReceiveAddressStatus(), 1))
      addEsEmptyOrMissingFilter(b, "receiveAddress");
    if (Objects.equals(reqVO.getReceiveAddressStatus(), 2))
      addEsNotEmptyFilter(b, "receiveAddress");
    if (Objects.equals(reqVO.getTrackingNumberStatus(), 1))
      addEsEmptyOrMissingFilter(b, "trackingNumber");
    if (Objects.equals(reqVO.getTrackingNumberStatus(), 2))
      addEsNotEmptyFilter(b, "trackingNumber");
    if (reqVO.getIssueTimeStart() != null)
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("issueTime")
                                  .format(ES_LOCAL_DATE_TIME_FORMAT)
                                  .gte(toEsDateTimeString(reqVO.getIssueTimeStart())))));
    if (reqVO.getIssueTimeEnd() != null)
      b.filter(
          f ->
              f.range(
                  r ->
                      r.date(
                          dr ->
                              dr.field("issueTime")
                                  .format(ES_LOCAL_DATE_TIME_FORMAT)
                                  .lte(toEsDateTimeString(reqVO.getIssueTimeEnd())))));
    return b.build();
  }

  /** ES 过滤未删除数据：新文档按 deleted=false 过滤，历史缺少 deleted 字段的文档按未删除兼容。 */
  private void addEsNotDeletedFilter(BoolQuery.Builder b) {
    b.filter(
        f ->
            f.bool(
                bb ->
                    bb.minimumShouldMatch("1")
                        .should(
                            s -> s.term(t -> t.field("deleted").value(FieldValue.of(false))))
                        .should(
                            s ->
                                s.bool(
                                    nb ->
                                        nb.mustNot(
                                            m -> m.exists(e -> e.field("deleted")))))));
  }

  /** ES 空值筛选：PC 选择“未填写”时，MySQL 口径是 NULL 或空字符串，ES 也必须保持一致。 */
  private void addEsEmptyOrMissingFilter(BoolQuery.Builder b, String field) {
    b.filter(
        f ->
            f.bool(
                bb ->
                    bb.minimumShouldMatch("1")
                        .should(
                            s ->
                                s.bool(
                                    nb ->
                                        nb.mustNot(
                                            m -> m.exists(e -> e.field(field)))))
                        .should(
                            s -> s.term(t -> t.field(field).value(FieldValue.of(""))))));
  }

  /** ES 非空筛选：PC 选择“已填写”时，字段既要存在，也不能是空字符串。 */
  private void addEsNotEmptyFilter(BoolQuery.Builder b, String field) {
    b.filter(f -> f.exists(e -> e.field(field)));
    b.mustNot(f -> f.term(t -> t.field(field).value(FieldValue.of(""))));
  }

  /**
   * LocalDateTime 转 ES date range 使用的业务时间字符串。
   * ES 文档写入的是 yyyy-MM-dd HH:mm:ss 本地业务时间，如果查询条件转 epoch_millis，
   * ES 会按 UTC 比较导致 +8 小时时区偏移，PC 发放/签到时间筛选会串到前一天或漏掉当天晚间数据。
   */
  private String toEsDateTimeString(LocalDateTime time) {
    return formatEsDateTime(time);
  }

  /** LocalDateTime 转 ES date range 使用的毫秒时间戳字符串；仅用于统一埋点 timestamp 这类按 UTC 时间戳写入的索引。 */
  private String toEsDateMillis(LocalDateTime time) {
    return String.valueOf(time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }

  /** 读取 ES total hits，统一处理空值。 */
  private long totalHits(SearchResponse<?> response) {
    return response == null || response.hits() == null || response.hits().total() == null
        ? 0L
        : response.hits().total().value();
  }

  /** 读取 ES cardinality 聚合值，统一处理空值。 */
  private long cardinality(Aggregate aggregate) {
    return aggregate == null || aggregate.cardinality() == null
        ? 0L
        : aggregate.cardinality().value();
  }

  /** 读取 ES filter 聚合文档数，统一处理空值。 */
  private long filterCount(Aggregate aggregate) {
    return aggregate == null || aggregate.filter() == null ? 0L : aggregate.filter().docCount();
  }

  /** 读取 ES filter 下嵌套 sum 聚合值，统一处理空值。 */
  private double nestedSum(Aggregate aggregate, String name) {
    // null
    if (aggregate == null
        || aggregate.filter() == null
        || aggregate.filter().aggregations() == null) return 0D;
    Aggregate nested = aggregate.filter().aggregations().get(name);
    return nested == null || nested.sum() == null || Double.isNaN(nested.sum().value())
        ? 0D
        : nested.sum().value();
  }

  /** 从 Redis bitmap 还原用户已签到日期列表：只存已签到日，未签到不存。 */
  private List<LocalDate> signDateListFromRedis(ActivityDO activity, Long activityId, String mobile) {
    if (activity == null) return null;
    String key = calendarKey(activityId, mobile);
    // Redis key 不存在代表缓存缺失，不能当成未签到，需要让外层降级 MySQL。
    if (!Boolean.TRUE.equals(redisCache.hasKey(key))) return null;
    LocalDate today = signToday();
    LocalDate start = activityStartDate(activity, today);
    LocalDate end =
        activity.getEndDate() == null
            ? today
            : toLdt(activity.getEndDate()).toLocalDate();
    if (end.isAfter(today)) end = today;
    if (end.isBefore(start)) return Collections.emptyList();
    List<LocalDate> dates = new ArrayList<>();
    long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
    for (int i = 0; i <= days && i <= Integer.MAX_VALUE; i++) {
      if (Boolean.TRUE.equals(redisCache.getBit(key, i))) dates.add(start.plusDays(i));
    }
    return dates;
  }

  /** 获取用户在活动内的所有已签到日期：优先 Redis，其次 ES，ES 不可用时才降级 MySQL 并回填 bitmap。 */
  private List<LocalDate> signDateList(Long activityId, String mobile) {
    ActivityDO activity = getActivityById(activityId);
    if (activity == null) return Collections.emptyList();
    try {
      List<LocalDate> redisDates = signDateListFromRedis(activity, activityId, mobile);
      // 如果 Redis key 存在，即使没有任何 bit，也代表缓存有效，可以直接使用。
      if (redisDates != null) return redisDates;
    } catch (Exception e) {
      log.warn("读取签到 Redis 日历失败，降级查 MySQL activityId={}, mobile={}", activityId, mobile, e);
    }
    List<LocalDate> esDates = signDateListFromEs(activity, activityId, mobile);
    // 如果 ES 查询正常，直接使用 ES 读模型；空集合也代表当前中间件没有签到记录，不再打 MySQL。
    if (esDates != null) return esDates;
    return signDatesFromMysqlAndBackfill(activity, activityId, mobile);
  }

  /** 从 ES 查询用户活动内所有签到日期；返回 null 表示 ES 异常，空集合表示 ES 正常但没有记录。 */
  private List<LocalDate> signDateListFromEs(ActivityDO activity, Long activityId, String mobile) {
    try {
      BoolQuery.Builder b = new BoolQuery.Builder();
      b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(activityId))));
      b.filter(f -> f.term(t -> t.field("memberMobile").value(FieldValue.of(mobile))));
      // ES 历史文档可能没有 deleted 字段，不能用 deleted=false 精确过滤；这里只排除明确 deleted=true 的记录。
      b.mustNot(f -> f.term(t -> t.field("deleted").value(FieldValue.of(true))));
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_RECORD_INDEX)
                      .query(q -> q.bool(b.build()))
                      .source(src -> src.filter(f -> f.includes("signDate")))
                      .size(10000)
                      .sort(sort -> sort.field(f -> f.field("signDate").order(SortOrder.Asc)))
                      .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Asc))),
              Map.class);
      if (response.hits() == null || response.hits().hits() == null) return Collections.emptyList();
      List<LocalDate> dates =
          response.hits().hits().stream()
              .map(Hit::source)
              .filter(Objects::nonNull)
              .map(source -> esLocalDate(source.get("signDate")))
              .filter(Objects::nonNull)
              .distinct()
              .sorted()
              .toList();
      // 如果 ES 里查到了签到记录，顺手回填 Redis bitmap，后续 C 端高频查询直接走 Redis。
      if (!CollectionUtils.isEmpty(dates)) backfillCalendar(activity, activityId, mobile, dates);
      return dates;
    } catch (Exception e) {
      log.warn("查询签到记录 ES 失败，降级查 MySQL activityId={}, mobile={}", activityId, mobile, e);
      return null;
    }
  }

  /** MySQL 降级查询用户活动内所有签到日期，并把已有签到日期回填 Redis bitmap。 */
  private List<LocalDate> signDatesFromMysqlAndBackfill(
      ActivityDO activity, Long activityId, String mobile) {
    List<LocalDate> dates =
        activitySignRecordMapper.selectByMobile(recordTable(shard(mobile)), activityId, mobile).stream()
            .map(ActivitySignRecordDO::getSignDate)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    backfillCalendar(activity, activityId, mobile, dates);
    return dates;
  }

  /** 回填用户签到 bitmap：只写已签到日期，TTL 按活动结束后7天；长期有效不设置 TTL。 */
  private void backfillCalendar(
      ActivityDO activity, Long activityId, String mobile, List<LocalDate> dates) {
    if (activity == null || CollectionUtils.isEmpty(dates)) return;
    String key = calendarKey(activityId, mobile);
    for (LocalDate date : dates) {
      int offset = signOffset(activity, date);
      // 如果日期在活动开始前，说明是脏数据，不写 Redis。
      if (offset >= 0) redisCache.setBit(key, offset);
    }
    applyCalendarTtl(activity, key);
  }

  /** 写入单个签到 bitmap 位，并刷新 TTL。 */
  private void setCalendarBit(ActivityDO activity, String key, int offset) {
    redisCache.setBit(key, offset);
    applyCalendarTtl(activity, key);
  }

  /** 设置用户签到 bitmap 过期时间：活动结束后7天过期，长期有效活动不设置过期时间。 */
  private void applyCalendarTtl(ActivityDO activity, String key) {
    long ttlSeconds = calcStoreHitTtlSeconds(activity == null ? null : activity.getEndDate());
    if (ttlSeconds > 0) {
      redisCache.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }
  }

  /** 计算当前签到周期窗口：支持不重置、按周、按月；29/30/31 遇小月按月末，32 表示月末。 */
  private PeriodWindow currentPeriodWindow(ActivitySignDO sign) {
    LocalDate today = signToday();
    ActivityDO activity =
        sign == null || sign.getActivityId() == null
            ? null
            : getActivityById(sign.getActivityId());
    LocalDate activityStart =
        activityStartDate(activity, today);
    boolean forever = activity == null || activity.getEndDate() == null;
    LocalDate activityEnd =
        forever
            ? LocalDate.of(9999, 12, 31)
            : toLdt(activity.getEndDate()).toLocalDate();
    LocalDate start;
    LocalDate end;
    boolean noReset =
        sign == null
            || !Objects.equals(sign.getResetEnabled(), 1)
            || !StringUtils.hasText(sign.getResetDays());
    // 如果没开启重置，当前周期就是整个活动周期；长期有效活动使用固定 forever 标识作为周期 key。
    if (noReset) {
      start = activityStart;
      end = activityEnd;
    } else if (Objects.equals(sign.getResetType(), 1)) {
      LocalDate[] range = weeklyPeriod(today, parseResetDays(sign.getResetDays()));
      start = range[0];
      end = range[1];
    } else if (Objects.equals(sign.getResetType(), 2)) {
      LocalDate[] range = monthlyPeriod(today, parseResetDays(sign.getResetDays()));
      start = range[0];
      end = range[1];
    } else {
      start = activityStart;
      end = activityEnd;
    }
    if (start.isBefore(activityStart)) start = activityStart;
    if (end.isAfter(activityEnd)) end = activityEnd;
    String key = noReset && forever ? start + "_forever" : start + "_" + end;
    return new PeriodWindow(start, end, key);
  }

  /** 解析重置日期配置：resetDays 为逗号分隔数字字符串。 */
  private List<Integer> parseResetDays(String resetDays) {
    if (!StringUtils.hasText(resetDays)) return Collections.emptyList();
    List<Integer> list = new ArrayList<>();
    for (String item : resetDays.split(",")) {
      try {
        if (StringUtils.hasText(item)) list.add(Integer.parseInt(item.trim()));
      } catch (NumberFormatException ignore) {
      }
    }
    return list.stream().distinct().sorted().toList();
  }

  /** 计算按周重置的当前周期起止日期。 */
  private LocalDate[] weeklyPeriod(LocalDate today, List<Integer> days) {
    if (CollectionUtils.isEmpty(days)) return new LocalDate[] {today, today};
    LocalDate start = null;
    LocalDate next = null;
    for (Integer day : days) {
      if (day == null || day < 1 || day > 7) continue;
      LocalDate candidate = today.with(java.time.DayOfWeek.of(day));
      if (!candidate.isAfter(today) && (start == null || candidate.isAfter(start)))
        start = candidate;
      if (candidate.isAfter(today) && (next == null || candidate.isBefore(next))) next = candidate;
    }
    if (start == null) {
      Integer maxDay =
          days.stream()
              .filter(d -> d != null && d >= 1 && d <= 7)
              .max(Integer::compareTo)
              .orElse(today.getDayOfWeek().getValue());
      start = today.with(java.time.DayOfWeek.of(maxDay)).minusWeeks(1);
    }
    if (next == null) {
      Integer minDay =
          days.stream()
              .filter(d -> d != null && d >= 1 && d <= 7)
              .min(Integer::compareTo)
              .orElse(today.getDayOfWeek().getValue());
      next = today.with(java.time.DayOfWeek.of(minDay)).plusWeeks(1);
    }
    return new LocalDate[] {start, next.minusDays(1)};
  }

  /** 计算按月重置的当前周期起止日期。 */
  private LocalDate[] monthlyPeriod(LocalDate today, List<Integer> days) {
    if (CollectionUtils.isEmpty(days)) return new LocalDate[] {today, today};
    LocalDate start = null;
    LocalDate next = null;
    for (Integer day : days) {
      LocalDate candidate = resetDateOfMonth(today.getYear(), today.getMonthValue(), day);
      if (!candidate.isAfter(today) && (start == null || candidate.isAfter(start)))
        start = candidate;
      if (candidate.isAfter(today) && (next == null || candidate.isBefore(next))) next = candidate;
    }
    if (start == null) {
      YearMonth prev = YearMonth.from(today).minusMonths(1);
      for (Integer day : days) {
        LocalDate candidate = resetDateOfMonth(prev.getYear(), prev.getMonthValue(), day);
        if (start == null || candidate.isAfter(start)) start = candidate;
      }
    }
    if (next == null) {
      YearMonth following = YearMonth.from(today).plusMonths(1);
      for (Integer day : days) {
        LocalDate candidate = resetDateOfMonth(following.getYear(), following.getMonthValue(), day);
        if (next == null || candidate.isBefore(next)) next = candidate;
      }
    }
    return new LocalDate[] {start, next.minusDays(1)};
  }

  /** 把配置日转换成某年某月实际重置日：超过当月天数按月末处理。 */
  private LocalDate resetDateOfMonth(int year, int month, Integer configuredDay) {
    YearMonth ym = YearMonth.of(year, month);
    int day = configuredDay == null ? 1 : configuredDay;
    if (day == 32 || day > ym.lengthOfMonth()) day = ym.lengthOfMonth();
    if (day < 1) day = 1;
    return ym.atDay(day);
  }

  /** 校验签到活动创建/修改参数：补足跨字段业务规则，避免只靠单字段注解时漏掉联动场景。 */
  private void validateSaveReq(ActivitySignSaveReqVO req) {
    // 如果活动日期类型是指定日期，开始时间和结束时间都必须传；长期有效不校验时间，允许前端不传。
    if (Objects.equals(req.getDateType(), 1)
        && (req.getStartTime() == null || req.getEndTime() == null)) {
      throw exception(SIGN_END_TIME_REQUIRED);
    }
    // 如果开始时间和结束时间都有值，结束时间不能早于开始时间。
    if (req.getStartTime() != null && req.getEndTime() != null && req.getEndTime().isBefore(req.getStartTime())) {
      throw exception(SIGN_TIME_RANGE_ERROR);
    }
    // 如果活动适用范围选择部分门店，必须传门店ID集合，否则活动无法命中具体门店。
    if (Objects.equals(req.getActivityStoreType(), 2) && CollectionUtils.isEmpty(req.getStoreIds())) {
      throw exception(SIGN_STORE_REQUIRED);
    }
    // 如果开启周期及奖励重置，必须选择按周或按月重置。
    if (Objects.equals(req.getResetEnabled(), 1) && req.getResetType() == null) {
      throw exception(SIGN_RESET_TYPE_REQUIRED);
    }
    // 如果开启周期及奖励重置，必须传具体重置日期配置，例如按月月末传32。
    if (Objects.equals(req.getResetEnabled(), 1) && !StringUtils.hasText(req.getResetDays())) {
      throw exception(SIGN_RESET_DAYS_REQUIRED);
    }
  }

  /** 已有用户参与时，不允许修改签到规则和奖励配置，避免影响已参与用户的周期和发奖口径。 */
  private void validateRuleRewardEditable(ActivitySignSaveReqVO req) {
    ActivityDO activity = activityService.selectById(req.getId());
    if (activity == null) {
      throw exception(BASE_ACTIVITY_NOT_FOUND);
    }
    if (!hasSignRecord(req.getId())) {
      return;
    }
    ActivitySignDO oldSign = selectSignByActivityId(req.getId());
    List<ActivitySignRewardDO> oldRewards = selectRewards(req.getId());
    if (signRuleChanged(oldSign, req) || rewardChanged(oldRewards, req.getPrizeList())) {
      throw exception(SIGN_RULE_REWARD_CANNOT_UPDATE);
    }
  }

  /** 判断活动是否已有用户签到记录：优先使用 ES 读模型，避免更新活动时扫描 10 张分表。 */
  private boolean hasSignRecord(Long activityId) {
    try {
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_RECORD_INDEX)
                      .query(
                          q ->
                              q.term(
                                  t ->
                                      t.field("activityId")
                                          .value(FieldValue.of(activityId))))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true)),
              Map.class);
      return totalHits(response) > 0;
    } catch (Exception e) {
      log.warn("查询签到参与记录 ES 失败，为避免误改规则默认按已有参与处理 activityId={}", activityId, e);
      return true;
    }
  }

  /** 判断签到规则是否变化：规则限定为重置规则和连签周期，活动文案、图片、分享配置仍允许修改。 */
  private boolean signRuleChanged(ActivitySignDO oldSign, ActivitySignSaveReqVO req) {
    if (oldSign == null) {
      return true;
    }
    return !Objects.equals(oldSign.getResetEnabled(), req.getResetEnabled())
        || !Objects.equals(oldSign.getResetType(), req.getResetType())
        || !Objects.equals(oldSign.getResetDays(), req.getResetDays())
        || !Objects.equals(oldSign.getContinuousCycleDays(), req.getContinuousCycleDays());
  }

  /** 判断奖励配置是否变化：逐条比较发奖规则、奖品、奖品内容、图片、面值和发放方式。 */
  private boolean rewardChanged(
      List<ActivitySignRewardDO> oldRewards, List<ActivitySignPrizeReqVO> newRewards) {
    List<String> oldKeys =
        (oldRewards == null ? Collections.<ActivitySignRewardDO>emptyList() : oldRewards).stream()
            .map(this::rewardCompareKey)
            .toList();
    List<String> newKeys =
        (newRewards == null ? Collections.<ActivitySignPrizeReqVO>emptyList() : newRewards).stream()
            .map(this::rewardCompareKey)
            .toList();
    return !Objects.equals(oldKeys, newKeys);
  }

  private String rewardCompareKey(ActivitySignRewardDO reward) {
    return String.join(
        "|",
        normalize(reward.getPrizeType()),
        normalize(reward.getPrizeId()),
        normalize(reward.getPrizeContent()),
        normalize(reward.getPrizeImgUrl()),
        normalize(reward.getPrizeValue()),
        normalize(reward.getSignRuleType()),
        normalize(reward.getSignDays()),
        normalize(reward.getGrantMode()));
  }

  private String rewardCompareKey(ActivitySignPrizeReqVO reward) {
    return String.join(
        "|",
        normalize(reward.getPrizeType()),
        normalize(reward.getPrizeId()),
        normalize(reward.getPrizeContent()),
        normalize(reward.getPrizeImgUrl()),
        normalize(reward.getPrizeValue()),
        normalize(reward.getSignRuleType()),
        normalize(reward.getSignDays()),
        normalize(reward.getGrantMode() == null ? 1 : reward.getGrantMode()));
  }

  private String normalize(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof BigDecimal decimal) {
      return decimal.stripTrailingZeros().toPlainString();
    }
    if (value instanceof String text) {
      return text.trim();
    }
    return String.valueOf(value);
  }

  /** 组装活动主表对象：把签到活动通用字段写入 activity 表。 */
  private ActivityDO buildActivity(ActivitySignSaveReqVO req, Long id) {
    ActivityDO a = new ActivityDO();
    a.setId(id);
    a.setActivityName(req.getActivityName());
    a.setActivityType(ActivityTypeEnum.SIGN.getCode());
    a.setActivityRemark(req.getActivityRemark());
    a.setIsEnabled(req.getEnabled());
    a.setActivityStore(Objects.equals(req.getActivityStoreType(), 1) ? 1 : 0);
    a.setStartDate(toDate(req.getStartTime()));
    a.setEndDate(req.getEndTime() == null ? null : toDate(req.getEndTime()));
    a.setActivityRules(req.getActivityRule());
    return a;
  }

  /** 组装签到扩展表对象：保存图片、分享、重置规则、活动规则等签到专属字段。 */
  private ActivitySignDO buildSign(ActivitySignSaveReqVO req, Long activityId) {
    ActivitySignDO s = new ActivitySignDO();
    s.setActivityId(activityId);
    s.setActivityCoverImage(req.getActivityCoverImage());
    s.setActivityBackgroundImage(req.getActivityBackgroundImage());
    s.setUnsignedImage(req.getUnsignedImage());
    s.setSignedImage(req.getSignedImage());
    s.setActivityDetailImage(req.getActivityDetailImage());
    s.setThemeColor(req.getThemeColor());
    s.setShareType(req.getShareType());
    s.setShareTitle(
        StringUtils.hasText(req.getShareTitle()) ? req.getShareTitle() : defaultShareTitle);
    s.setShareNote(
        StringUtils.hasText(req.getShareNote()) ? req.getShareNote() : defaultShareDescription);
    s.setShareImgUrl(
        StringUtils.hasText(req.getShareImgUrl()) ? req.getShareImgUrl() : defaultShareImageUrl);
    s.setResetEnabled(req.getResetEnabled());
    s.setResetType(req.getResetType());
    s.setResetDays(req.getResetDays());
    s.setContinuousCycleDays(req.getContinuousCycleDays());
    s.setCommunityOnly(req.getCommunityOnly());
    s.setStoreManagerQrCodeGuideImage(req.getStoreManagerQrCodeGuideImage());
    s.setStoreGroupQrCodeGuideImage(req.getStoreGroupQrCodeGuideImage());
    s.setActivityRule(req.getActivityRule());
    return s;
  }

  /** 保存奖励配置：新建/修改活动时直接覆盖保存，不单独提供保存奖励接口。 */
  private void saveRewards(Long activityId, List<ActivitySignPrizeReqVO> list) {
    if (list == null) return;
    for (ActivitySignPrizeReqVO p : list) {
      ActivitySignRewardDO d = new ActivitySignRewardDO();
      d.setActivityId(activityId);
      d.setPrizeType(p.getPrizeType());
      d.setPrizeId(p.getPrizeId());
      d.setPrizeContent(p.getPrizeContent());
      d.setPrizeImgUrl(p.getPrizeImgUrl());
      d.setPrizeValue(p.getPrizeValue());
      d.setSignRuleType(p.getSignRuleType());
      d.setSignDays(p.getSignDays());
      d.setGrantMode(p.getGrantMode() == null ? 1 : p.getGrantMode());
      activitySignRewardMapper.insert(d);
    }
  }

  /** 修改签到扩展配置：使用显式 set，确保关闭重置时 resetType、resetDays 可以被更新为 null。 */
  private void updateSignById(ActivitySignDO sign) {
    activitySignMapper.update(
        null,
        new LambdaUpdateWrapper<ActivitySignDO>()
            .eq(ActivitySignDO::getId, sign.getId())
            .set(ActivitySignDO::getActivityCoverImage, sign.getActivityCoverImage())
            .set(ActivitySignDO::getActivityBackgroundImage, sign.getActivityBackgroundImage())
            .set(ActivitySignDO::getUnsignedImage, sign.getUnsignedImage())
            .set(ActivitySignDO::getSignedImage, sign.getSignedImage())
            .set(ActivitySignDO::getActivityDetailImage, sign.getActivityDetailImage())
            .set(ActivitySignDO::getThemeColor, sign.getThemeColor())
            .set(ActivitySignDO::getShareType, sign.getShareType())
            .set(ActivitySignDO::getShareTitle, sign.getShareTitle())
            .set(ActivitySignDO::getShareNote, sign.getShareNote())
            .set(ActivitySignDO::getShareImgUrl, sign.getShareImgUrl())
            .set(ActivitySignDO::getResetEnabled, sign.getResetEnabled())
            .set(ActivitySignDO::getResetType, sign.getResetType())
            .set(ActivitySignDO::getResetDays, sign.getResetDays())
            .set(ActivitySignDO::getContinuousCycleDays, sign.getContinuousCycleDays())
            .set(ActivitySignDO::getCommunityOnly, sign.getCommunityOnly())
            .set(ActivitySignDO::getStoreManagerQrCodeGuideImage, sign.getStoreManagerQrCodeGuideImage())
            .set(ActivitySignDO::getStoreGroupQrCodeGuideImage, sign.getStoreGroupQrCodeGuideImage())
            .set(ActivitySignDO::getActivityRule, sign.getActivityRule()));
  }

  /** 按活动ID查询签到扩展配置。 */
  private ActivitySignDO selectSignByActivityId(Long activityId) {
    ActivitySignCache cache = getActivityCache(activityId);
    return cache == null ? null : cache.getSign();
  }

  /** 按活动ID从 MySQL 查询签到扩展配置：只允许缓存刷新/降级时调用。 */
  private ActivitySignDO selectSignByActivityIdFromDb(Long activityId) {
    return activitySignMapper.selectOne(
        new LambdaQueryWrapper<ActivitySignDO>()
            .eq(ActivitySignDO::getActivityId, activityId)
            .last("limit 1"));
  }

  /** 按活动ID查询奖励配置，按创建时间和ID升序用于进度计算。 */
  private List<ActivitySignRewardDO> selectRewards(Long activityId) {
    ActivitySignCache cache = getActivityCache(activityId);
    return cache == null || cache.getRewards() == null ? Collections.emptyList() : cache.getRewards();
  }

  /** 按活动ID从 MySQL 查询奖励配置：只允许缓存刷新/降级时调用。 */
  private List<ActivitySignRewardDO> selectRewardsFromDb(Long activityId) {
    return activitySignRewardMapper.selectList(
        new LambdaQueryWrapper<ActivitySignRewardDO>()
            .eq(ActivitySignRewardDO::getActivityId, activityId)
            .orderByAsc(ActivitySignRewardDO::getCreateTime, ActivitySignRewardDO::getId));
  }

  /** 奖励 DO 转 PC 回显 VO。 */
  private ActivitySignPrizeReqVO toPrizeReq(ActivitySignRewardDO r) {
    ActivitySignPrizeReqVO v = new ActivitySignPrizeReqVO();
    v.setId(r.getId());
    v.setPrizeType(r.getPrizeType());
    v.setPrizeId(r.getPrizeId());
    v.setPrizeContent(r.getPrizeContent());
    v.setCouponName(null);
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setPrizeValue(r.getPrizeValue());
    v.setSignRuleType(r.getSignRuleType());
    v.setSignDays(r.getSignDays());
    v.setGrantMode(r.getGrantMode());
    return v;
  }

  /** 奖励 DO 列表转 PC 回显 VO：详情回显时按奖品 ID 补齐优惠券名称和优惠券包名称，方便前端直接展示。 */
  private List<ActivitySignPrizeReqVO> toPrizeReqList(List<ActivitySignRewardDO> rewards) {
    // 如果活动还没配置奖励，直接返回空集合，避免后续批量查名称时处理空列表。
    if (CollectionUtils.isEmpty(rewards)) {
      return Collections.emptyList();
    }
    Map<Long, String> couponNameMap = couponNameMap(rewards);
    Map<Long, String> packageNameMap = couponPackageNameMap(rewards);
    return rewards.stream()
        .map(
            r -> {
              ActivitySignPrizeReqVO v = toPrizeReq(r);
              // 详情回显只补名称字段，不改前端原始 prizeContent 入库值。
              if (Objects.equals(r.getPrizeType(), PRIZE_TYPE_COUPON)
                  && couponNameMap.containsKey(r.getPrizeId())) {
                v.setCouponName(couponNameMap.get(r.getPrizeId()));
              }
              if (Objects.equals(r.getPrizeType(), PRIZE_TYPE_COUPON_PACKAGE)
                  && packageNameMap.containsKey(r.getPrizeId())) {
                v.setCouponName(packageNameMap.get(r.getPrizeId()));
              }
              return v;
            })
        .collect(Collectors.toList());
  }

  /** 批量查询优惠券名称：PC 活动详情只需要 prizeId -> couponName，避免奖励循环里逐条查库。 */
  private Map<Long, String> couponNameMap(List<ActivitySignRewardDO> rewards) {
    List<Long> couponIds =
        rewards.stream()
            .filter(r -> Objects.equals(r.getPrizeType(), PRIZE_TYPE_COUPON))
            .map(ActivitySignRewardDO::getPrizeId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    // 如果当前活动没有优惠券奖励，则不需要查询优惠券表。
    if (CollectionUtils.isEmpty(couponIds)) {
      return Collections.emptyMap();
    }
    List<GoodCouponDO> coupons =
        goodCouponMapper.selectList(
            new LambdaQueryWrapper<GoodCouponDO>().in(GoodCouponDO::getId, couponIds));
    // 如果按 prizeId 没查到券，保留原 prizeContent，避免详情接口因为历史脏数据报错。
    if (CollectionUtils.isEmpty(coupons)) {
      return Collections.emptyMap();
    }
    return coupons.stream()
        .filter(c -> c.getId() != null)
        .collect(Collectors.toMap(GoodCouponDO::getId, GoodCouponDO::getCouponName, (a, b) -> a));
  }

  /** 批量查询优惠券包名称：PC 活动详情只需要 prizeId -> packageName，避免奖励循环里逐条查库。 */
  private Map<Long, String> couponPackageNameMap(List<ActivitySignRewardDO> rewards) {
    List<Long> packageIds =
        rewards.stream()
            .filter(r -> Objects.equals(r.getPrizeType(), PRIZE_TYPE_COUPON_PACKAGE))
            .map(ActivitySignRewardDO::getPrizeId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    // 如果当前活动没有优惠券包奖励，则不需要查询优惠券包表。
    if (CollectionUtils.isEmpty(packageIds)) {
      return Collections.emptyMap();
    }
    return couponPackageService.getPackageNameMap(packageIds);
  }

  /** 签到扩展 DO 拷贝到 PC 活动详情 VO。 */
  private void copySignToResp(ActivitySignDO s, ActivitySignRespVO v) {
    v.setActivityCoverImage(s.getActivityCoverImage());
    v.setActivityBackgroundImage(s.getActivityBackgroundImage());
    v.setUnsignedImage(s.getUnsignedImage());
    v.setSignedImage(s.getSignedImage());
    v.setActivityDetailImage(s.getActivityDetailImage());
    v.setThemeColor(s.getThemeColor());
    v.setShareType(s.getShareType());
    v.setShareTitle(s.getShareTitle());
    v.setShareNote(s.getShareNote());
    v.setShareImgUrl(s.getShareImgUrl());
    v.setResetEnabled(s.getResetEnabled());
    v.setResetType(s.getResetType());
    v.setResetDays(s.getResetDays());
    v.setContinuousCycleDays(s.getContinuousCycleDays());
    v.setCommunityOnly(s.getCommunityOnly());
    v.setStoreManagerQrCodeGuideImage(s.getStoreManagerQrCodeGuideImage());
    v.setStoreGroupQrCodeGuideImage(s.getStoreGroupQrCodeGuideImage());
    v.setActivityRule(s.getActivityRule());
  }

  /** 通过 memberId 获取会员信息：优先使用登录 token 里的手机号/openId，避免压测时每次 Dubbo 查会员。 */
  private WxMemberDTO getMember(Long memberId) {
    WxMemberDTO tokenMember = memberFromToken(memberId);
    if (tokenMember != null && StringUtils.hasText(tokenMember.getMemberMobile())) {
      return tokenMember;
    }
    try {
      CommonResult<WxMemberDTO> r = wxMemberApi.getMemberById(memberId);
      WxMemberDTO apiMember = r == null ? null : r.getData();
      return apiMember == null ? tokenMember : apiMember;
    } catch (Exception e) {
      log.warn("get member fail", e);
      return tokenMember;
    }
  }

  /** 从登录 token 的 userInfo 里还原压测/小程序签到所需的会员基础字段。 */
  private WxMemberDTO memberFromToken(Long memberId) {
    LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
    Map<String, String> tokenInfo = loginUser == null ? null : loginUser.getInfo();
    if (tokenInfo == null || tokenInfo.isEmpty()) {
      return null;
    }
    String mobile = tokenInfo.get("mobile");
    if (!StringUtils.hasText(mobile)) {
      mobile = tokenInfo.get(LoginUser.INFO_KEY_MOBILE);
    }
    String openId = tokenInfo.get("openid");
    String unionId = tokenInfo.get("unionid");
    if (!StringUtils.hasText(mobile) && !StringUtils.hasText(openId)) {
      return null;
    }
    WxMemberDTO member = new WxMemberDTO();
    member.setMemberId(memberId);
    member.setMemberMobile(mobile);
    member.setOpenid(openId);
    member.setWxUnionid(unionId);
    member.setShardingValue(StringUtils.hasText(mobile) ? shard(mobile) : null);
    return member;
  }

  /** 获取签到权益维度手机号：优先会员服务手机号，其次取登录 token 附带手机号；拿不到手机号时直接阻断，不能用 memberId 代替手机号。 */
  private String mobileOf(WxMemberDTO m, Long memberId) {
    if (m != null && StringUtils.hasText(m.getMemberMobile())) {
      return m.getMemberMobile();
    }
    LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
    Map<String, String> tokenInfo = loginUser == null ? null : loginUser.getInfo();
    String tokenMobile = tokenInfo == null ? null : tokenInfo.get("mobile");
    // 兼容框架里可能定义的手机号 key，避免不同登录端写入的 userInfo 字段名不一致。
    if (!StringUtils.hasText(tokenMobile) && tokenInfo != null) {
      tokenMobile = tokenInfo.get(LoginUser.INFO_KEY_MOBILE);
    }
    if (StringUtils.hasText(tokenMobile)) {
      return tokenMobile;
    }
    throw exception(SIGN_MEMBER_MOBILE_REQUIRED);
  }

  /** 获取手机号但不抛异常：用于首页 hit 探测接口，未绑手机号时仍允许返回活动入口。 */
  private String mobileOrNull(WxMemberDTO m, Long memberId) {
    if (m != null && StringUtils.hasText(m.getMemberMobile())) {
      return m.getMemberMobile();
    }
    LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
    Map<String, String> tokenInfo = loginUser == null ? null : loginUser.getInfo();
    String tokenMobile = tokenInfo == null ? null : tokenInfo.get("mobile");
    if (!StringUtils.hasText(tokenMobile) && tokenInfo != null) {
      tokenMobile = tokenInfo.get(LoginUser.INFO_KEY_MOBILE);
    }
    return StringUtils.hasText(tokenMobile) ? tokenMobile : null;
  }

  /** 通过活动ID直接查主表，避免被外层数据权限影响。 */
  private ActivityDO getActivityById(Long activityId) {
    ActivitySignCache cache = getActivityCache(activityId);
    // 如果 Redis 活动配置缓存存在，直接用缓存中的活动主表数据。
    if (cache != null && cache.getActivity() != null) {
      return cache.getActivity();
    }
    ActivityDO activity = activityService.selectById(activityId);
    if (activity == null) {
      return null;
    }
    ActivitySignCache fallbackCache = new ActivitySignCache();
    fallbackCache.setActivity(activity);
    fallbackCache.setStartTimeMs(activity.getStartDate() == null ? null : activity.getStartDate().getTime());
    fallbackCache.setEndTimeMs(activity.getEndDate() == null ? null : activity.getEndDate().getTime());
    fallbackCache.setSign(selectSignByActivityIdFromDb(activityId));
    fallbackCache.setRewards(selectRewardsFromDb(activityId));
    restoreActivityTime(fallbackCache);
    refreshActivityCache(activityId);
    return fallbackCache.getActivity();
  }

  /** 获取会员展示名称：优先昵称，再取会员名。 */
  private String nameOf(WxMemberDTO m) {
    return m == null
        ? null
        : (StringUtils.hasText(m.getMemberNickName()) ? m.getMemberNickName() : m.getMemberName());
  }

  /** 根据手机号最后一位数字计算分表尾号。 */
  private int shard(String mobile) {
    // 如果手机号为空，无法按手机号尾号分表
    if (!StringUtils.hasText(mobile)) return 0;
    for (int i = mobile.length() - 1; i >= 0; i--)
      // 如果当前字符是数字，可以作为手机号尾号计算分表
      if (Character.isDigit(mobile.charAt(i))) return (mobile.charAt(i) - '0') % 10;
    return 0;
  }

  /** 生成签到记录物理分表名。 */
  private String recordTable(int s) {
    return "activity_sign_record_" + s;
  }

  /** 生成奖励发放记录物理分表名。 */
  private String rewardTable(int s) {
    return "activity_sign_reward_record_" + s;
  }

  /** LocalDateTime 转 Date，用于适配活动主表字段。 */
  private Date toDate(LocalDateTime t) {
    return t == null ? null : Date.from(t.atZone(ZoneId.systemDefault()).toInstant());
  }

  /** Date 转 LocalDateTime，用于返回接口和状态判断。 */
  private LocalDateTime toLdt(Date d) {
    return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
  }

  /** 结束时间转换入口：活动表结束时间按日期粒度存储时，需要覆盖结束日整天。 */
  private LocalDateTime toEndLdt(Date d) {
    LocalDateTime endTime = toLdt(d);
    if (endTime == null) {
      return null;
    }
    // 如果数据库只存了结束日期的 00:00:00，业务上按当天 23:59:59 结束处理。
    if (LocalTime.MIDNIGHT.equals(endTime.toLocalTime())) {
      return endTime.toLocalDate().atTime(23, 59, 59);
    }
    return endTime;
  }

  /** 生成用户签到日历 bitmap key：activityId + mobileHash。 */
  private String calendarKey(Long activityId, String mobile) {
    return "promotion:sign:calendar:" + activityId + ":" + Math.abs(Objects.hashCode(mobile));
  }

  /** 生成签到锁 key：activityId + mobileHash + date，防重复点击。 */
  private String signLockKey(Long activityId, String mobile, LocalDate date) {
    return "promotion:sign:lock:"
        + activityId
        + ":"
        + Math.abs(Objects.hashCode(mobile))
        + ":"
        + date;
  }

  /** 计算某天在活动开始日后的 bitmap 偏移量。 */
  private int signOffset(ActivityDO activity, LocalDate date) {
    LocalDate start = activityStartDate(activity, date);
    long offset = java.time.temporal.ChronoUnit.DAYS.between(start, date);
    return offset < 0 || offset > Integer.MAX_VALUE ? -1 : (int) offset;
  }

  /** 获取活动签到起点：指定日期取 startDate；长期有效没有 startDate 时取活动创建日，保证跨天 Redis bitmap 偏移稳定。 */
  private LocalDate activityStartDate(ActivityDO activity, LocalDate defaultDate) {
    // 如果活动主表没查到，只能使用调用方传入的默认日期兜底。
    if (activity == null) return defaultDate;
    // 如果活动配置了开始日期，说明是指定日期或历史数据有起止时间，直接以配置开始日期为准。
    if (activity.getStartDate() != null) return toLdt(activity.getStartDate()).toLocalDate();
    // 如果长期有效没有开始日期，使用活动创建日期作为签到起点，避免今天和明天算出不同 bitmap offset。
    if (activity.getCreateTime() != null) return activity.getCreateTime().toLocalDate();
    // 如果历史脏数据连创建时间都没有，使用调用方默认日期兜底，至少不影响当天签到。
    return defaultDate;
  }

  /** 判断手机号今天是否已签到：小程序高频读取优先 Redis bitmap，Redis 缺失时走 ES，ES 不可用时才降级 MySQL。 */
  private boolean todaySigned(Long aid, String m) {
    try {
      ActivityDO activity = getActivityById(aid);
      LocalDate today = signToday();
      int offset = signOffset(activity, today);
      String key = calendarKey(aid, m);
      // 如果用户签到 bitmap 已存在，今天这一位就是权威结果：true 是已签，false 是未签，不再打 MySQL。
      if (offset >= 0 && Boolean.TRUE.equals(redisCache.hasKey(key))) {
        return Boolean.TRUE.equals(redisCache.getBit(key, offset));
      }
    } catch (Exception e) {
      log.warn("读取今日签到 Redis 状态失败，降级查 MySQL activityId={}, mobile={}", aid, m, e);
    }
    return signDateList(aid, m).contains(signToday());
  }

  /** 填充首页 hit 活动基础信息；未绑手机号和已绑手机号都可以返回活动入口。 */
  private void fillHitActivityBase(AppActivitySignHitRespVO vo, ActivityDO a, ActivitySignDO s) {
    vo.setActivityId(a.getId());
    vo.setActivityName(a.getActivityName());
    vo.setActivityStatus(1);
    vo.setCoverImage(s == null ? null : s.getActivityCoverImage());
    vo.setActivityBackgroundImage(s == null ? null : s.getActivityBackgroundImage());
    vo.setTodaySigned(false);
    vo.setRewardFinished(false);
    vo.setButtonText("去签到");
  }

  /** 未绑手机号时不展示进度卡，但返回一个稳定的默认奖励进度，避免前端拿到 null/null。 */
  private void fillHitDefaultProgress(AppActivitySignHitRespVO vo, Long activityId) {
    List<ActivitySignRewardDO> rewards = selectRewards(activityId);
    Optional<ActivitySignRewardDO> firstNonDaily =
        rewards.stream()
            .filter(r -> !Objects.equals(r.getSignRuleType(), 1))
            .filter(r -> r.getSignDays() != null && r.getSignDays() > 0)
            .min(Comparator.comparing(ActivitySignRewardDO::getSignDays));
    int target = firstNonDaily.map(ActivitySignRewardDO::getSignDays).orElse(1);
    vo.setRewardProgressDays(0);
    vo.setNextRewardTargetDays(target);
    vo.setNextRewardNeedDays(target);
  }

  /** 根据已签到日期计算截至某天的连续签到天数。 */
  private int calcContinuous(List<LocalDate> dates, LocalDate end) {
    Set<LocalDate> set = new HashSet<>(dates);
    int c = 0;
    for (LocalDate d = end; set.contains(d); d = d.minusDays(1)) c++;
    return c;
  }

  /** 获取当前周期 key。 */
  private String periodKey(ActivitySignDO s) {
    PeriodWindow w = currentPeriodWindow(s);
    return w.key;
  }

  /** 计算小程序进度：当前周期内累计/连续天数，以及下一个可获得奖励的还需天数。 */
  private Progress calcProgress(Long aid, String mobile, ActivitySignDO s) {
    PeriodWindow window = currentPeriodWindow(s);
    List<LocalDate> dates = signedDates(aid, mobile, window);
    Progress p = new Progress();
    p.activityId = aid;
    p.mobile = mobile;
    p.periodKey = window == null ? null : window.key;
    p.today = signToday();
    p.total = dates.size();
    p.todaySigned = dates.contains(p.today);
    LocalDate continuousEndDate = p.todaySigned ? p.today : p.today.minusDays(1);
    // 如果今天还没签到，但昨天签到了，连续进度仍然可以接上，详情页要按昨天往前计算连续天数。
    p.continuous = calcContinuous(dates, continuousEndDate);
    List<ActivitySignRewardDO> rewards = selectRewards(aid);
    p.grantedRewardKeys = grantedRewardKeys(aid, mobile);
    boolean hasDailyReward = rewards.stream().anyMatch(r -> Objects.equals(r.getSignRuleType(), 1));
    // 如果配置了每日签到奖励，并且用户今天还没签到，下一个可获得奖励就是今天签到奖励，还需1天。
    if (hasDailyReward && !p.todaySigned) {
      p.progress = 0;
      p.target = 1;
      p.need = 1;
      p.finished = false;
      return p;
    }
    Optional<ActivitySignRewardDO> next =
        rewards.stream()
            .filter(r -> !Objects.equals(r.getSignRuleType(), 1))
            .filter(r -> !rewardObtained(r, p))
            .filter(r -> r.getSignDays() != null && r.getSignDays() > progressForReward(r, p))
            .min(
                Comparator.comparing(
                        (ActivitySignRewardDO r) -> r.getSignDays() - progressForReward(r, p))
                    .thenComparing(ActivitySignRewardDO::getSignDays));
    // 如果还有下一个未获得的连续/累计奖励，优先返回真实进度，不能被下面的 1/1 兜底覆盖。
    if (next.isPresent()) {
      p.target = next.get().getSignDays();
      p.progress = progressForReward(next.get(), p);
      p.need = Math.max(0, p.target - p.progress);
      p.finished = false;
      return p;
    }
    List<ActivitySignRewardDO> nonDailyRewards =
        rewards.stream().filter(r -> !Objects.equals(r.getSignRuleType(), 1)).toList();
    // 只有当前周期内所有非每日奖励都已获得，才算奖励全部完成；不能拿到任意一个奖励就显示 1/1。
    p.finished =
        !CollectionUtils.isEmpty(nonDailyRewards)
            && nonDailyRewards.stream().allMatch(r -> rewardObtained(r, p));
    // 如果没有下一个非每日奖励，但首页仍需展示进度卡，则至少回显一个稳定值，避免前端拿到 null/null。
    if (p.progress == null || p.target == null || p.need == null) {
      if (p.todaySigned) {
        p.progress = p.total > 0 ? p.total : 1;
        p.target = p.progress;
        p.need = 0;
        p.finished = true;
      } else {
        p.progress = 0;
        p.target = 1;
        p.need = 1;
      }
    }
    // 如果当前周期已经把所有非每日奖励都领完，首页进度卡固定回 1/1，避免前端拿到 null/null。
    if (p.finished) {
      p.progress = 1;
      p.target = 1;
      p.need = 0;
      return p;
    }
    return p;
  }

  /** 按奖励规则取对应进度：连续奖励看连续天数，累计奖励看累计天数，每日奖励看今日是否已签到。 */
  private int progressForReward(ActivitySignRewardDO reward, Progress progress) {
    // 如果是每日签到奖励，今天签过就是1，否则就是0。
    if (Objects.equals(reward.getSignRuleType(), 1)) return progress.todaySigned ? 1 : 0;
    // 如果是连续签到奖励，只能用 continuousDays 判断，不能和累计天数混用。
    if (Objects.equals(reward.getSignRuleType(), 2)) return progress.continuous;
    // 如果是累计签到奖励，只能用 totalDays 判断。
    if (Objects.equals(reward.getSignRuleType(), 3)) return progress.total;
    return 0;
  }

  /** 判断奖励是否已获得：每日奖励看今天是否签到，连续/累计奖励看当前周期 grantKey 是否已有发放记录。 */
  private boolean rewardObtained(ActivitySignRewardDO reward, Progress progress) {
    // 如果是每日签到奖励，每天都可以重新获得，详情只需要看今天是否已签到。
    if (Objects.equals(reward.getSignRuleType(), 1)) return progress.todaySigned;
    String grantKey = rewardGrantKey(progress.activityId, reward.getId(), progress.mobile, reward.getSignRuleType(), progress.today, progress.periodKey);
    return progress.grantedRewardKeys.contains(grantKey);
  }

  /** 查询用户当前活动已触发过的奖励幂等 key；详情回显优先 ES，ES 不可用时降级 MySQL。 */
  private Set<String> grantedRewardKeys(Long activityId, String mobile) {
    // 如果活动ID或手机号为空，无法查询奖励发放记录，按未获得处理。
    if (activityId == null || !StringUtils.hasText(mobile)) return Collections.emptySet();
    try {
      Set<String> keys = grantedRewardKeysFromEs(activityId, mobile);
      // 如果 ES 查询正常，直接使用 ES 读模型判断已获得奖品。
      if (keys != null) return keys;
    } catch (Exception e) {
      log.warn("查询签到奖励发放记录 ES 失败，降级查 MySQL activityId={}, mobile={}", activityId, mobile, e);
    }
    return grantedRewardKeysFromMysql(activityId, mobile);
  }

  /** 从 ES 查询用户奖励发放幂等 key；返回 null 表示 ES 查询异常，空集合表示 ES 正常但没有记录。 */
  private Set<String> grantedRewardKeysFromEs(Long activityId, String mobile) {
    try {
      BoolQuery.Builder b = new BoolQuery.Builder();
      b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(activityId))));
      b.filter(f -> f.term(t -> t.field("memberMobile").value(FieldValue.of(mobile))));
      // ES 历史文档可能没有 deleted 字段，不能用 deleted=false 精确过滤；这里只排除明确 deleted=true 的记录。
      b.mustNot(f -> f.term(t -> t.field("deleted").value(FieldValue.of(true))));
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(q -> q.bool(b.build()))
                      .source(src -> src.filter(f -> f.includes("grantKey")))
                      .size(200),
              Map.class);
      if (response.hits() == null || response.hits().hits() == null) return Collections.emptySet();
      return response.hits().hits().stream()
          .map(Hit::source)
          .filter(Objects::nonNull)
          .map(source -> source.get("grantKey"))
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .filter(StringUtils::hasText)
          .collect(Collectors.toSet());
    } catch (Exception e) {
      log.warn("ES 查询签到奖励 grantKey 失败 activityId={}, mobile={}", activityId, mobile, e);
      return null;
    }
  }

  /** MySQL 查询用户奖励发放幂等 key；只作为 ES 异常时的降级查询。 */
  private Set<String> grantedRewardKeysFromMysql(Long activityId, String mobile) {
    try {
      return activitySignRewardRecordMapper
          .selectByMobile(rewardTable(shard(mobile)), activityId, mobile)
          .stream()
          .map(ActivitySignRewardRecordDO::getGrantKey)
          .filter(StringUtils::hasText)
          .collect(Collectors.toSet());
    } catch (Exception e) {
      log.warn("查询签到奖励发放记录 MySQL 失败，按未获得处理 activityId={}, mobile={}", activityId, mobile, e);
      return Collections.emptySet();
    }
  }

  /** 查询小程序我的奖励记录：优先 ES 读模型，ES 不可用时才降级 MySQL。 */
  private List<ActivitySignRewardRecordDO> rewardRecords(Long activityId, String mobile) {
    if (activityId == null || !StringUtils.hasText(mobile)) return Collections.emptyList();
    List<ActivitySignRewardRecordDO> records = rewardRecordsFromEs(activityId, mobile);
    // 如果 ES 查询正常，直接使用 ES 读模型；空集合也代表当前中间件没有奖励记录。
    if (records != null) return records;
    return rewardRecordsFromMysql(activityId, mobile);
  }

  /** 从 ES 查询小程序我的奖励记录；返回 null 表示 ES 异常，空集合表示 ES 正常但没有记录。 */
  private List<ActivitySignRewardRecordDO> rewardRecordsFromEs(Long activityId, String mobile) {
    try {
      BoolQuery.Builder b = new BoolQuery.Builder();
      b.filter(f -> f.term(t -> t.field("activityId").value(FieldValue.of(activityId))));
      b.filter(f -> f.term(t -> t.field("memberMobile").value(FieldValue.of(mobile))));
      // ES 历史文档可能没有 deleted 字段，不能用 deleted=false 精确过滤；这里只排除明确 deleted=true 的记录。
      b.mustNot(f -> f.term(t -> t.field("deleted").value(FieldValue.of(true))));
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(q -> q.bool(b.build()))
                      .size(10000)
                      .sort(sort -> sort.field(f -> f.field("issueTime").order(SortOrder.Desc)))
                      .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc))),
              Map.class);
      if (response.hits() == null || response.hits().hits() == null) return Collections.emptyList();
      return response.hits().hits().stream()
          .map(Hit::source)
          .filter(Objects::nonNull)
          .map(this::toRewardRecordFromEs)
          .filter(Objects::nonNull)
          .toList();
    } catch (Exception e) {
      log.warn("查询小程序我的签到奖励 ES 失败，降级查 MySQL activityId={}, mobile={}", activityId, mobile, e);
      return null;
    }
  }

  /**
   * ES 签到文档转 DO：避免 Elasticsearch Java Client 直接反序列化 LocalDate/LocalDateTime 失败。
   * 测试服务器 ES 里时间按 yyyy-MM-dd HH:mm:ss 字符串写入，这里统一手工兼容转换。
   */
  private ActivitySignRecordDO toSignRecordFromEs(Map source) {
    if (source == null) return null;
    ActivitySignRecordDO r = new ActivitySignRecordDO();
    r.setId(esLong(source.get("id")));
    r.setActivityId(esLong(source.get("activityId")));
    r.setMemberMobile(esString(source.get("memberMobile")));
    r.setMobileShard(esInteger(source.get("mobileShard")));
    r.setTriggerMemberId(esLong(source.get("triggerMemberId")));
    r.setMemberName(esString(source.get("memberName")));
    r.setStoreId(esLong(source.get("storeId")));
    r.setStoreName(esString(source.get("storeName")));
    r.setPeriodKey(esString(source.get("periodKey")));
    r.setPeriodStartTime(esLocalDateTime(source.get("periodStartTime")));
    r.setPeriodEndTime(esLocalDateTime(source.get("periodEndTime")));
    r.setResetEnabledSnapshot(esInteger(source.get("resetEnabledSnapshot")));
    r.setResetTypeSnapshot(esInteger(source.get("resetTypeSnapshot")));
    r.setResetDaysSnapshot(esString(source.get("resetDaysSnapshot")));
    r.setSignDate(esLocalDate(source.get("signDate")));
    r.setSignTime(esLocalDateTime(source.get("signTime")));
    r.setSignType(esInteger(source.get("signType")));
    r.setContinuousDaysAfter(esInteger(source.get("continuousDaysAfter")));
    r.setTotalDaysAfter(esInteger(source.get("totalDaysAfter")));
    r.setCreateTime(esLocalDateTime(source.get("createTime")));
    r.setUpdateTime(esLocalDateTime(source.get("updateTime")));
    r.setDeleted(esBoolean(source.get("deleted")));
    r.setBusinessId(esLong(source.get("businessId")));
    return r;
  }

  /**
   * ES 奖励发放文档转 DO：避免 Elasticsearch Java Client 直接反序列化 LocalDateTime 失败。
   * ES 里时间是 yyyy-MM-dd HH:mm:ss 字符串，prizeValue 也可能是字符串或数字，这里统一做兼容转换。
   */
  private ActivitySignRewardRecordDO toRewardRecordFromEs(Map source) {
    if (source == null) return null;
    ActivitySignRewardRecordDO r = new ActivitySignRewardRecordDO();
    r.setId(esLong(source.get("id")));
    r.setActivityId(esLong(source.get("activityId")));
    r.setPrizeId(esLong(source.get("prizeId")));
    r.setMemberMobile(esString(source.get("memberMobile")));
    r.setMobileShard(esInteger(source.get("mobileShard")));
    r.setMemberId(esLong(source.get("memberId")));
    r.setMemberName(esString(source.get("memberName")));
    r.setGender(esInteger(source.get("gender")));
    r.setMemberCategory(esInteger(source.get("memberCategory")));
    r.setStoreId(esLong(source.get("storeId")));
    r.setStoreName(esString(source.get("storeName")));
    r.setPeriodKey(esString(source.get("periodKey")));
    r.setPeriodStartTime(esLocalDateTime(source.get("periodStartTime")));
    r.setPeriodEndTime(esLocalDateTime(source.get("periodEndTime")));
    r.setResetEnabledSnapshot(esInteger(source.get("resetEnabledSnapshot")));
    r.setResetTypeSnapshot(esInteger(source.get("resetTypeSnapshot")));
    r.setResetDaysSnapshot(esString(source.get("resetDaysSnapshot")));
    r.setRewardRuleSnapshot(esString(source.get("rewardRuleSnapshot")));
    r.setPrizeType(esInteger(source.get("prizeType")));
    r.setPrizeContent(esString(source.get("prizeContent")));
    r.setPrizeImgUrl(esString(source.get("prizeImgUrl")));
    r.setPrizeValue(esBigDecimal(source.get("prizeValue")));
    r.setSignRuleType(esInteger(source.get("signRuleType")));
    r.setTriggerDays(esInteger(source.get("triggerDays")));
    r.setSignDate(esLocalDate(source.get("signDate")));
    r.setSignRecordId(esLong(source.get("signRecordId")));
    r.setGrantKey(esString(source.get("grantKey")));
    r.setIssueStatus(esInteger(source.get("issueStatus")));
    r.setIssueTime(esLocalDateTime(source.get("issueTime")));
    r.setFailReason(esString(source.get("failReason")));
    r.setExternalRecordId(esString(source.get("externalRecordId")));
    r.setPrizeState(esInteger(source.get("prizeState")));
    r.setReceiveUser(esString(source.get("receiveUser")));
    r.setReceiveMobile(esString(source.get("receiveMobile")));
    r.setReceiveAddress(esString(source.get("receiveAddress")));
    r.setTrackingNumber(esString(source.get("trackingNumber")));
    r.setExpressCompany(esString(source.get("expressCompany")));
    r.setOutBillNo(esString(source.get("outBillNo")));
    r.setClaimStatus(esInteger(source.get("claimStatus")));
    r.setPackageInfo(esString(source.get("packageInfo")));
    r.setCreateTime(esLocalDateTime(source.get("createTime")));
    r.setUpdateTime(esLocalDateTime(source.get("updateTime")));
    r.setDeleted(esBoolean(source.get("deleted")));
    r.setBusinessId(esLong(source.get("businessId")));
    return r;
  }

  /** ES 字段转字符串：空值保持 null，避免把 null 转成 "null"。 */
  private String esString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  /** ES 字段转 Long：兼容数字和字符串两种写法。 */
  private Long esLong(Object value) {
    if (value == null) return null;
    if (value instanceof Number number) return number.longValue();
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    return Long.valueOf(text);
  }

  /** ES 字段转 Integer：兼容数字和字符串两种写法。 */
  private Integer esInteger(Object value) {
    if (value == null) return null;
    if (value instanceof Number number) return number.intValue();
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    return Integer.valueOf(text);
  }

  /** ES 字段转 BigDecimal：兼容 prizeValue 被写成字符串或数字。 */
  private BigDecimal esBigDecimal(Object value) {
    if (value == null) return null;
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    return new BigDecimal(text);
  }

  /** ES 字段转 Boolean：历史文档可能没有 deleted，新文档是 true/false。 */
  private Boolean esBoolean(Object value) {
    if (value == null) return null;
    if (value instanceof Boolean bool) return bool;
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    return Boolean.valueOf(text);
  }

  /** ES 字段转 LocalDate：兼容 yyyy-MM-dd 字符串。 */
  private LocalDate esLocalDate(Object value) {
    if (value == null) return null;
    if (value instanceof LocalDate date) return date;
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
  }

  /** ES 字段转 LocalDateTime：兼容 yyyy-MM-dd HH:mm:ss、ISO 字符串和 epoch millis。 */
  private LocalDateTime esLocalDateTime(Object value) {
    if (value == null) return null;
    if (value instanceof LocalDateTime time) return time;
    if (value instanceof Number number) {
      return Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    String text = String.valueOf(value).trim();
    if (!StringUtils.hasText(text)) return null;
    if (text.chars().allMatch(Character::isDigit)) {
      return Instant.ofEpochMilli(Long.parseLong(text)).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    try {
      return LocalDateTime.parse(text, ES_DATE_TIME_FORMATTER);
    } catch (DateTimeParseException ignored) {
      // 如果不是业务写入的 yyyy-MM-dd HH:mm:ss，就按 ES 常见 ISO 格式再解析一次。
      return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
  }

  /** MySQL 查询小程序我的奖励记录；只作为 ES 异常时的降级查询。 */
  private List<ActivitySignRewardRecordDO> rewardRecordsFromMysql(Long activityId, String mobile) {
    try {
      return activitySignRewardRecordMapper.selectByMobile(
          rewardTable(shard(mobile)), activityId, mobile);
    } catch (Exception e) {
      log.warn("查询小程序我的签到奖励 MySQL 失败，按空奖励处理 activityId={}, mobile={}", activityId, mobile, e);
      return Collections.emptyList();
    }
  }

  /** 生成奖励发放幂等 key：每日奖励按签到日期唯一，连续/累计奖励按当前周期唯一。 */
  private String rewardGrantKey(
      Long activityId, Long rewardId, String mobile, Integer signRuleType, LocalDate signDate, String periodKey) {
    return activityId
        + ":"
        + rewardId
        + ":"
        + mobile
        + ":"
        + (Objects.equals(signRuleType, 1) ? signDate : periodKey);
  }

  /** 获取某活动某手机号在当前周期内的签到日期：优先 Redis bitmap，失败降级 MySQL。 */
  private List<LocalDate> signedDates(Long activityId, String mobile, PeriodWindow window) {
    if (window == null) {
      return Collections.emptyList();
    }
    LocalDate today = signToday();
    LocalDate end = window.end == null || window.end.isAfter(today) ? today : window.end;
    return signDateList(activityId, mobile).stream()
        .filter(d -> d != null && !d.isBefore(window.start) && !d.isAfter(end))
        .toList();
  }

  /** 签到后发放奖励：每日/连续/累计规则命中后先插入奖励记录占位，插入成功后再调用积分/券/券包/红包发放能力。 */
  private List<ActivitySignRewardRecordDO> issueRewards(
      AppActivitySignReqVO req,
      WxMemberDTO m,
      String mobile,
      ActivitySignRecordDO sign,
      ActivitySignDO s) {
    // 用于返回给前端的本次实际触发奖励；未命中、重复命中奖励不会放入该列表。
    List<ActivitySignRewardRecordDO> out = new ArrayList<>();
    // 只做本次循环内的去重，不查询 MySQL/ES/Redis；真正幂等由下方 MySQL insert 唯一键占位负责。
    Set<String> handledKeys = new HashSet<>();
    // 查询活动当前配置的全部奖励规则，逐条判断本次签到后是否命中。
    for (ActivitySignRewardDO rw : selectRewards(req.getActivityId())) {
      // 判断是否命中奖励规则：
      // 1=每日签到，任意一次新增签到都命中；
      // 2=连续签到，签到后的连续天数等于配置天数才命中；
      // 3=累计签到，当前周期累计天数等于配置天数才命中。
      boolean hit =
          Objects.equals(rw.getSignRuleType(), 1)
              || (rw.getSignDays() != null
                  && (Objects.equals(rw.getSignRuleType(), 2)
                      ? Objects.equals(sign.getContinuousDaysAfter(), rw.getSignDays())
                      : Objects.equals(sign.getTotalDaysAfter(), rw.getSignDays())));
      // 如果本次签到后的天数没有命中奖励规则，直接跳过当前奖励。
      if (!hit) {
        continue;
      }
      // 生成奖励幂等 key：每日奖励按日期唯一，连续/累计奖励按当前周期唯一。
      String g =
          rewardGrantKey(
              req.getActivityId(),
              rw.getId(),
              mobile,
              rw.getSignRuleType(),
              sign.getSignDate(),
              sign.getPeriodKey());
      // 如果同一次签到循环里已经处理过这个 grantKey，直接跳过，避免重复配置导致重复外部发奖。
      if (handledKeys.contains(g)) {
        continue;
      }
      // 组装奖励发放主账本，先默认成功；后续外部发放失败时再把状态改为失败并写失败原因。
      ActivitySignRewardRecordDO rr = new ActivitySignRewardRecordDO();
      rr.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId());
      rr.setActivityId(req.getActivityId());
      // 奖励发放记录表 prize_id 目前是 NOT NULL：券/券包存外部奖品 ID；积分/实物/红包没有外部 ID 时用奖励配置 ID 占位。
      // 小程序返回时会按 prizeType 把非券类 prizeId 置空，填写实物地址必须使用 rewardRecordId。
      rr.setPrizeId(rw.getPrizeId() == null ? rw.getId() : rw.getPrizeId());
      rr.setMemberMobile(mobile);
      rr.setMobileShard(shard(mobile));
      rr.setMemberId(sign.getTriggerMemberId());
      rr.setMemberName(sign.getMemberName());
      rr.setGender(m == null ? null : m.getGender());
      rr.setMemberCategory(m == null ? null : m.getMemberCategory());
      rr.setStoreId(sign.getStoreId());
      rr.setPeriodKey(sign.getPeriodKey());
      rr.setPeriodStartTime(sign.getPeriodStartTime());
      rr.setPeriodEndTime(sign.getPeriodEndTime());
      rr.setResetEnabledSnapshot(sign.getResetEnabledSnapshot());
      rr.setResetTypeSnapshot(sign.getResetTypeSnapshot());
      rr.setResetDaysSnapshot(sign.getResetDaysSnapshot());
      rr.setRewardRuleSnapshot(rw.getSignRuleType() + ":" + rw.getSignDays());
      rr.setPrizeType(rw.getPrizeType());
      rr.setPrizeContent(rw.getPrizeContent());
      rr.setPrizeImgUrl(rw.getPrizeImgUrl());
      rr.setPrizeValue(rw.getPrizeValue());
      rr.setSignRuleType(rw.getSignRuleType());
      rr.setTriggerDays(rw.getSignDays());
      rr.setSignDate(sign.getSignDate());
      rr.setSignRecordId(sign.getId());
      rr.setGrantKey(g);
      rr.setIssueStatus(ISSUE_SUCCESS);
      rr.setIssueTime(signNow());
      rr.setPrizeState(
          Objects.equals(rw.getPrizeType(), PRIZE_TYPE_PHYSICAL)
              ? PRIZE_STATE_ADDRESS_EMPTY
              : PRIZE_STATE_ISSUED);
      rr.setClaimStatus(
          Objects.equals(rw.getPrizeType(), PRIZE_TYPE_RED_PACKET) ? CLAIM_PENDING : null);
      try {
        // 先插入奖励记录占位；只有占位成功，才允许调用积分/券/红包这些外部发奖能力。
        activitySignRewardRecordMapper.insertRecord(rewardTable(shard(mobile)), rr);
      } catch (DuplicateKeyException ignore) {
        // 如果 grantKey 唯一键冲突，说明这个奖励已经处理过，不能再调用外部发奖。
        handledKeys.add(g);
        continue;
      }
      // 占位成功后记录本次已处理 grantKey，避免同一次签到循环重复处理。
      handledKeys.add(g);
      // 如果奖品是积分，调用会员积分能力：更新会员积分并写积分流水。
      if (Objects.equals(rw.getPrizeType(), 2)) {
        issuePointReward(req.getActivityId(), rw, rr, sign);
      }
      // 如果奖品是优惠券，调用现有优惠券发放能力给当前会员发券。
      if (Objects.equals(rw.getPrizeType(), PRIZE_TYPE_COUPON)) {
        issueCouponReward(rw, rr, m, sign);
      }
      // 如果奖品是优惠券包，调用现有券包领取能力给当前会员发券包。
      if (Objects.equals(rw.getPrizeType(), PRIZE_TYPE_COUPON_PACKAGE)) {
        issueCouponPackageReward(rw, rr, m, sign);
      }
      // 如果奖品是现金红包，调用红包发放函数生成 outBillNo 和 packageInfo。
      if (Objects.equals(rw.getPrizeType(), PRIZE_TYPE_RED_PACKET)) {
        issueRedPacketReward(req.getActivityId(), rw, rr, m);
      }
      // 外部发奖结束后，把成功/失败状态、失败原因、红包 outBillNo/packageInfo 等结果更新回占位记录。
      activitySignRewardRecordMapper.updateIssueResult(rewardTable(shard(mobile)), rr);
      // 更新成功后加入返回列表，前端签到成功响应里只展示本次实际生成的奖励记录。
      out.add(rr);
    }
    return out;
  }

  /** 发放现金红包奖励：调用红包转账服务生成 outBillNo 和 packageInfo，失败只标记当前奖励记录。 */
  private void issueRedPacketReward(
      Long activityId,
      ActivitySignRewardDO reward,
      ActivitySignRewardRecordDO record,
      WxMemberDTO member) {
    try {
      LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
      // 红包发放必须传 openId；如果会员 openId 为空，外部红包服务会返回失败，本次只记失败记录。
      redPacketVo.setOpenId(member == null ? null : member.getOpenid());
      // 红包金额按元配置，微信接口按分传输，所以这里元转分：1 元 -> 100 分。
      redPacketVo.setTransferAmount(
          reward.getPrizeValue() == null
              ? 0
              : reward.getPrizeValue()
                  .movePointRight(2)
                  .setScale(0, RoundingMode.HALF_UP)
                  .intValue());
      redPacketVo.setTransferRemark("签到活动");
      redPacketVo.setActivityId(activityId);
      // 调用红包转账服务：服务内部不允许影响签到主事务，失败只返回错误信息。
      TransferToUser.TransferToUserResponse response =
          lotteryRedPacketService.transferUser(redPacketVo);
      // 如果红包服务无响应、无状态码或状态码不是 200，本条奖励记录标记为发放失败。
      if (response == null || response.getCode() == null || response.getCode() != 200) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason(response == null ? "红包发放失败" : response.getMessage());
        record.setClaimStatus(null);
        return;
      }
      // 红包发放成功后保存微信侧单号和领取凭证，前端签到成功当次可直接拿 packageInfo 领取。
      record.setOutBillNo(response.getOutBillNo());
      record.setPackageInfo(response.getPackageInfo());
    } catch (Exception e) {
      // 红包服务异常不能回滚签到，只把当前奖励记录置为失败，并截断失败原因。
      record.setIssueStatus(ISSUE_FAILED);
      record.setFailReason(limitFailReason(e));
      record.setClaimStatus(null);
    }
  }

  /** 构造奖励发放使用的会员对象：会员服务异常时，用签到记录里的 memberId、手机号和手机号分表值兜底，保证优惠券/券包服务能拿到必要会员字段。 */
  private WxMemberDTO rewardMember(WxMemberDTO member, ActivitySignRecordDO sign) {
    // 如果上游会员对象为空，创建一个空对象，后面用签到记录里的关键字段补齐。
    WxMemberDTO rewardMember = member == null ? new WxMemberDTO() : member;
    // 如果会员ID为空，用签到记录里的触发会员ID兜底，优惠券/券包发放必须依赖会员ID。
    if (rewardMember.getMemberId() == null) {
      rewardMember.setMemberId(sign.getTriggerMemberId());
    }
    // 如果手机号为空，用签到记录里的手机号兜底，保持奖励记录和发放会员身份一致。
    if (!StringUtils.hasText(rewardMember.getMemberMobile())) {
      rewardMember.setMemberMobile(sign.getMemberMobile());
    }
    // 如果分表值为空，用手机号尾号补齐，避免下游按会员分表写券/流水时缺少分表值。
    if (rewardMember.getShardingValue() == null && StringUtils.hasText(sign.getMemberMobile())) {
      rewardMember.setShardingValue(shard(sign.getMemberMobile()));
    }
    return rewardMember;
  }

  /** 发放积分奖励：参考抽签/集卡活动，先查会员当前积分，再更新为当前积分+奖励积分，并补一条积分流水。 */
  private void issuePointReward(
      Long activityId,
      ActivitySignRewardDO reward,
      ActivitySignRewardRecordDO record,
      ActivitySignRecordDO sign) {
    try {
      // 读取配置的积分值：积分按整数发放，小数部分向下取整。
      int pointAmount =
          reward.getPrizeValue() == null
              ? 0
              : reward.getPrizeValue().setScale(0, RoundingMode.DOWN).intValue();
      // 如果配置的积分数量小于等于 0，这条积分奖励无法正常发放。
      if (pointAmount <= 0) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("积分奖励数量必须大于0");
        return;
      }
      // 查询真实会员信息和当前积分：不能用 token 里的简化会员对象直接算积分。
      WxMemberDTO member = getSignPointMember(sign.getTriggerMemberId());
      // 如果当前积分为空，按 0 处理，再累加本次奖励积分。
      int currentIntegral = member.getMemberIntegral() == null ? 0 : member.getMemberIntegral();
      int newIntegral = currentIntegral + pointAmount;
      // 调用会员服务更新会员总积分。
      wxMemberApi.updateMemberById(member.getMemberId(), newIntegral);
      // 写积分流水，方便用户积分明细和后续对账。
      addSignPointsLog(activityId, pointAmount, member);
    } catch (Exception e) {
      // 积分发放异常不回滚签到，只把当前奖励记录置为失败。
      record.setIssueStatus(ISSUE_FAILED);
      record.setFailReason(limitFailReason(e));
    }
  }

  /** 查询签到发积分需要的真实会员信息：发积分必须读取当前积分，不能使用 token 里还原的简化会员对象。 */
  private WxMemberDTO getSignPointMember(Long memberId) {
    try {
      // 如果集卡同款接口能返回更完整的会员信息，优先使用它，避免积分查询和 token 会员对象脱节。
      CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
      WxMemberVO memberVO = memberResult == null ? null : memberResult.getData();
      if (memberResult != null && memberResult.isSuccess() && memberVO != null && memberVO.getMemberId() != null) {
        return BeanUtils.toBean(memberVO, WxMemberDTO.class);
      }
    } catch (Exception e) {
      // 如果老会员接口因为历史字段或数据权限问题查询异常，不中断积分发放，继续走 DTO 查询兜底。
      log.warn("签到积分发放查询会员 VO 失败，改用 DTO 查询兜底 memberId={}", memberId, e);
    }
    // 如果 VO 链路异常，再回退到 DTO 查询，避免整个积分发放直接失败。
    CommonResult<WxMemberDTO> fallbackResult = wxMemberApi.getMemberById(memberId);
    WxMemberDTO fallbackMember = fallbackResult == null ? null : fallbackResult.getData();
    if (fallbackResult == null || !fallbackResult.isSuccess() || fallbackMember == null || fallbackMember.getMemberId() == null) {
      throw new IllegalStateException("会员信息不存在");
    }
    return fallbackMember;
  }

  /** 写入签到积分流水：保持 points_log 口径和抽签/集卡一致，方便会员积分明细展示和后续对账。 */
  private void addSignPointsLog(Long activityId, int pointAmount, WxMemberDTO member) {
    // 组装积分流水基础字段，字段口径参考抽签/集卡，避免会员积分明细展示不一致。
    PointsLogDO pointsLogDO = new PointsLogDO();
    pointsLogDO.setMemberId(member.getMemberId());
    pointsLogDO.setPointsChange(Long.valueOf(pointAmount));
    pointsLogDO.setMemberName(member.getMemberName());
    pointsLogDO.setMemberNickName(member.getMemberNickName());
    pointsLogDO.setMemberMobile(member.getMemberMobile());
    pointsLogDO.setLogCode(generateSignPointNumber());
    long rawId = identifierGenerator.nextId(null).longValue();
    pointsLogDO.setPointsLogId(rawId);
    pointsLogDO.setPointsType(1);
    pointsLogDO.setIsDelete(0);
    pointsLogDO.setCreateTime(new Date());
    pointsLogDO.setPointsLogStatus(1);
    pointsLogDO.setIsPointsProduct(2);
    pointsLogDO.setProductType(3);
    pointsLogDO.setUpdateTime(new Date());
    pointsLogDO.setProductId(activityId);
    pointsLogDO.setBusinessId(BusinessContextHolder.getBusinessId());
    pointsLogDO.setShardingValue(member.getShardingValue());
    // 如果会员带分表值，积分流水ID拼上分表尾号后取 Long 范围内值，保持抽签/集卡现有分表写法。
    if (pointsLogDO.getShardingValue() != null) {
      String pointsLogId = rawId + "" + pointsLogDO.getShardingValue();
      BigInteger finalId = new BigInteger(pointsLogId).mod(BigInteger.valueOf(Long.MAX_VALUE));
      pointsLogDO.setPointsLogId(finalId.longValueExact());
    }
    // 调用积分流水服务落库。
    lotteryAddLogService.addPointsLog(pointsLogDO);
  }

  /** 生成签到积分流水编码：当前项目抽签/集卡使用毫秒时间戳，这里保持同一口径。 */
  private String generateSignPointNumber() {
    return String.valueOf(System.currentTimeMillis());
  }

  /** 发放优惠券奖励：复用现有优惠券服务，失败只标记发放失败原因。 */
  private void issueCouponReward(
      ActivitySignRewardDO reward,
      ActivitySignRewardRecordDO record,
      WxMemberDTO member,
      ActivitySignRecordDO sign) {
    try {
      // 优惠券奖品必须配置 prizeId，prizeId 对应优惠券ID。
      if (reward.getPrizeId() == null) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("优惠券ID为空");
        return;
      }
      // 构造下游发券需要的会员对象，缺失字段用签到记录兜底。
      WxMemberDTO rewardMember = rewardMember(member, sign);
      // 如果兜底后仍没有会员ID，无法调用发券接口。
      if (rewardMember.getMemberId() == null) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("会员ID为空，无法发放优惠券");
        return;
      }
      // 查询优惠券详情，发券服务需要完整优惠券对象。
      GoodCouponRespVO coupon = goodCouponService.getCouponById(reward.getPrizeId());
      // 如果优惠券不存在，记录发放失败，避免调用下游空对象异常。
      if (coupon == null) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("优惠券不存在");
        return;
      }
      // 调用现有优惠券发放服务，来源标记为签到活动，门店使用本次签到门店。
      Boolean success =
          userCouponService.insertUserCouponWithSeckill(
              rewardMember.getMemberId(),
              coupon,
              rewardMember,
              CouponSourceType.SIGN_ACTIVITY.getCode(),
              sign.getStoreId());
      // 如果下游返回非 true，认为本次优惠券发放失败。
      if (!Boolean.TRUE.equals(success)) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("优惠券发放失败");
      }
    } catch (Exception e) {
      // 发券异常不回滚签到，只把当前奖励记录置为失败。
      record.setIssueStatus(ISSUE_FAILED);
      record.setFailReason(limitFailReason(e));
    }
  }

  /** 发放优惠券包奖励：复用现有券包领取服务，失败只标记发放失败原因。 */
  private void issueCouponPackageReward(
      ActivitySignRewardDO reward,
      ActivitySignRewardRecordDO record,
      WxMemberDTO member,
      ActivitySignRecordDO sign) {
    try {
      // 优惠券包奖品必须配置 prizeId，prizeId 对应优惠券包ID。
      if (reward.getPrizeId() == null) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("优惠券包ID为空");
        return;
      }
      // 构造下游领券包需要的会员对象，缺失字段用签到记录兜底。
      WxMemberDTO rewardMember = rewardMember(member, sign);
      // 如果兜底后仍没有会员ID，无法调用领券包接口。
      if (rewardMember.getMemberId() == null) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("会员ID为空，无法发放优惠券包");
        return;
      }
      // 调用现有优惠券包领取服务，来源标记为签到活动，门店使用本次签到门店。
      Boolean success =
          couponPackageService.claimCouponPackage(
              rewardMember,
              reward.getPrizeId(),
              CouponSourceType.SIGN_ACTIVITY.getCode(),
              null,
              sign.getStoreId());
      // 如果下游返回非 true，认为本次优惠券包发放失败。
      if (!Boolean.TRUE.equals(success)) {
        record.setIssueStatus(ISSUE_FAILED);
        record.setFailReason("优惠券包发放失败");
      }
    } catch (Exception e) {
      // 发券包异常不回滚签到，只把当前奖励记录置为失败。
      record.setIssueStatus(ISSUE_FAILED);
      record.setFailReason(limitFailReason(e));
    }
  }

  /** 截断发奖失败原因：避免 Dubbo/SQL 异常堆栈过长，超过数据库 fail_reason 字段长度导致签到接口回滚。 */
  private String limitFailReason(Exception e) {
    String msg = e == null ? null : e.getMessage();
    if (!StringUtils.hasText(msg)) {
      return "发放失败";
    }
    return msg.length() > 500 ? msg.substring(0, 500) : msg;
  }

  /** 组装小程序签到结果：返回签到状态、当前天数和本次触发的奖励列表。 */
  private AppActivitySignResultRespVO signResult(
      AppActivitySignReqVO req, String mobile, List<ActivitySignRewardRecordDO> issued, boolean signSuccess) {
    return signResult(req, mobile, issued, signSuccess, null);
  }

  /** 组装小程序签到结果：todaySignedOverride 用于并发重复点击时直接告诉前端今天已签到，避免事务未提交造成状态误判。 */
  private AppActivitySignResultRespVO signResult(
      AppActivitySignReqVO req,
      String mobile,
      List<ActivitySignRewardRecordDO> issued,
      boolean signSuccess,
      Boolean todaySignedOverride) {
    Progress p =
        calcProgress(req.getActivityId(), mobile, selectSignByActivityId(req.getActivityId()));
    AppActivitySignResultRespVO vo = new AppActivitySignResultRespVO();
    vo.setActivityId(req.getActivityId());
    vo.setStoreId(req.getStoreId());
    vo.setSignSuccess(signSuccess);
    vo.setTodaySigned(
        todaySignedOverride == null ? todaySigned(req.getActivityId(), mobile) : todaySignedOverride);
    vo.setContinuousDays(p.continuous);
    vo.setTotalDays(p.total);
    vo.setRewardTriggered(!issued.isEmpty());
    vo.setPrizeList(issued.stream().map(this::toAppPrize).toList());
    return vo;
  }

  /** 奖励配置或发放记录转小程序奖励 VO。 */
  private AppActivitySignPrizeRespVO toAppPrize(ActivitySignRewardDO r, Progress p) {
    AppActivitySignPrizeRespVO v = toAppPrizeBase(r);
    boolean dailyReward = Objects.equals(r.getSignRuleType(), 1);
    boolean obtained = rewardObtained(r, p);
    v.setObtained(obtained);
    Integer needDays;
    // 如果奖励已获得，当前周期内不能再次获得，剩余天数固定返回 0。
    if (obtained) {
      needDays = 0;
    } else if (dailyReward) {
      // 如果是每日签到奖励，今天没签就是还差 1 天。
      needDays = 1;
    } else if (r.getSignDays() == null) {
      needDays = null;
    } else {
      // 如果是连续/累计奖励，只按该奖励自己的规则进度计算还差几天。
      needDays = Math.max(0, r.getSignDays() - progressForReward(r, p));
    }
    v.setNeedDays(needDays);
    return v;
  }

  /** 奖励配置或发放记录转小程序奖励 VO。 */
  private AppActivitySignPrizeRespVO toAppPrize(ActivitySignRewardRecordDO r) {
    AppActivitySignPrizeRespVO v = new AppActivitySignPrizeRespVO();
    v.setRewardRecordId(r.getId());
    v.setPrizeId(appPrizeId(r.getPrizeType(), r.getPrizeId()));
    v.setPrizeType(r.getPrizeType());
    v.setPrizeContent(r.getPrizeContent());
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setPrizeValue(r.getPrizeValue());
    v.setSignRuleType(r.getSignRuleType());
    v.setSignDays(r.getTriggerDays());
    v.setObtained(true);
    v.setNeedDays(0);
    // 如果本次签到获得的是红包，需要把微信领取凭证和领取状态一起返回给前端。
    if (Objects.equals(r.getPrizeType(), PRIZE_TYPE_RED_PACKET)) {
      v.setPackageInfo(r.getPackageInfo());
      v.setClaimStatus(r.getClaimStatus());
    }
    // 如果本次签到获得的是实物，需要返回实物状态，前端据此展示填写地址、待发货、待收货、超时未填写。
    if (Objects.equals(r.getPrizeType(), PRIZE_TYPE_PHYSICAL)) {
      v.setPrizeState(r.getPrizeState());
    }
    return v;
  }

  /** 奖励配置转小程序奖励基础字段。 */
  private AppActivitySignPrizeRespVO toAppPrizeBase(ActivitySignRewardDO r) {
    AppActivitySignPrizeRespVO v = new AppActivitySignPrizeRespVO();
    v.setPrizeId(r.getId());
    v.setPrizeType(r.getPrizeType());
    v.setPrizeContent(r.getPrizeContent());
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setPrizeValue(r.getPrizeValue());
    v.setSignRuleType(r.getSignRuleType());
    v.setSignDays(r.getSignDays());
    return v;
  }

  /** 小程序奖励发放记录对外暴露的 prizeId：只有优惠券/优惠券包有外部奖品 ID，积分/实物/红包不返回占位 ID。 */
  private Long appPrizeId(Integer prizeType, Long storedPrizeId) {
    if (Objects.equals(prizeType, PRIZE_TYPE_COUPON)
        || Objects.equals(prizeType, PRIZE_TYPE_COUPON_PACKAGE)) {
      return storedPrizeId;
    }
    return null;
  }

  /** 组装小程序详情页已签到日期：只返回当前活动中已经签到过的日期，未签到日期不返回，前端自行打标。 */
  private List<AppActivitySignWeekDateRespVO> weekDates(Long aid, String mobile) {
    LocalDate today = signToday();
    List<LocalDate> signed = signDateList(aid, mobile);
    List<AppActivitySignWeekDateRespVO> list = new ArrayList<>();
    for (LocalDate d : signed) {
      AppActivitySignWeekDateRespVO v = new AppActivitySignWeekDateRespVO();
      v.setDate(d);
      v.setToday(d.equals(today));
      v.setDisplayText(d.equals(today) ? "今天" : (d.getMonthValue() + "." + d.getDayOfMonth()));
      v.setSigned(true);
      list.add(v);
    }
    return list;
  }

  /** 构造签到记录导出查询条件；导出筛选项必须和 PC 列表保持一致。 */
  private ActivitySignRecordPageReqVO buildRecordPageReq(ActivitySignRecordExportReqVO reqVO) {
    ActivitySignRecordPageReqVO q = new ActivitySignRecordPageReqVO();
    q.setActivityId(reqVO.getActivityId());
    q.setMemberMobile(reqVO.getMemberMobile());
    q.setStoreId(reqVO.getStoreId());
    q.setContinuousCycleDays(reqVO.getContinuousCycleDays());
    q.setSignTimeStart(reqVO.getSignTimeStart());
    q.setSignTimeEnd(reqVO.getSignTimeEnd());
    return q;
  }

  /** 构造奖励发放记录导出查询条件；导出筛选项必须和 PC 列表保持一致。 */
  private ActivitySignRewardRecordPageReqVO buildRewardPageReq(
      ActivitySignRewardRecordExportReqVO reqVO) {
    ActivitySignRewardRecordPageReqVO q = new ActivitySignRewardRecordPageReqVO();
    q.setActivityId(reqVO.getActivityId());
    q.setMemberMobile(reqVO.getMemberMobile());
    q.setPrizeType(reqVO.getPrizeType());
    q.setClaimStatusList(reqVO.getClaimStatusList());
    q.setReceiveAddressStatus(reqVO.getReceiveAddressStatus());
    q.setTrackingNumberStatus(reqVO.getTrackingNumberStatus());
    q.setIssueTimeStart(reqVO.getIssueTimeStart());
    q.setIssueTimeEnd(reqVO.getIssueTimeEnd());
    return q;
  }

  /** 统计签到记录导出总数；用于 30W 导出限制。 */
  private long countRecordExport(ActivitySignRecordPageReqVO q) {
    long total = 0;
    for (int i = 0; i < 10; i++) {
      total += activitySignRecordMapper.countPage(recordTable(i), q);
    }
    return total;
  }

  /** 统计签到记录导出总数：优先 ES，返回 null 表示 ES 异常，需要降级 MySQL。 */
  private Long countRecordExportFromEs(ActivitySignRecordPageReqVO q) {
    try {
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_RECORD_INDEX)
                      .query(query -> query.bool(buildRecordPageBool(q)))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true)),
              Map.class);
      return totalHits(response);
    } catch (Exception e) {
      log.warn("签到记录导出 ES 统计失败，准备降级 MySQL, req={}", q, e);
      return null;
    }
  }

  /** 统计奖励发放记录导出总数；用于 30W 导出限制。 */
  private long countRewardExport(ActivitySignRewardRecordPageReqVO q) {
    long total = 0;
    for (int i = 0; i < 10; i++) {
      total += activitySignRewardRecordMapper.countPage(rewardTable(i), q);
    }
    return total;
  }

  /** 统计奖励发放记录导出总数：优先 ES，返回 null 表示 ES 异常，需要降级 MySQL。 */
  private Long countRewardExportFromEs(ActivitySignRewardRecordPageReqVO q) {
    try {
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s ->
                  s.index(SIGN_REWARD_INDEX)
                      .query(query -> query.bool(buildRewardPageBool(q)))
                      .size(0)
                      .trackTotalHits(t -> t.enabled(true)),
              Map.class);
      return totalHits(response);
    } catch (Exception e) {
      log.warn("签到奖励发放记录导出 ES 统计失败，准备降级 MySQL, req={}", q, e);
      return null;
    }
  }

  /**
   * 分页获取签到记录导出数据。
   *
   * <p>注意：记录表按手机号尾号拆成 10 张表，这里按全局 offset 跳过前面分表的数据，不在循环里重复查已跳过的数据。
   */
  private List<ActivitySignRecordExportRespVO> getRecordExportData(
      Page<ActivitySignRecordExportRespVO> page, ActivitySignRecordPageReqVO q) {
    int pageNo = Math.max((int) page.getCurrent(), 1);
    int pageSize = Math.max((int) page.getSize(), 10000);
    long globalOffset = (long) (pageNo - 1) * pageSize;
    int remain = pageSize;
    List<ActivitySignRecordExportRespVO> result = new ArrayList<>();
    for (int i = 0; i < 10 && remain > 0; i++) {
      String table = recordTable(i);
      long tableCount = activitySignRecordMapper.countPage(table, q);
      if (globalOffset >= tableCount) {
        globalOffset -= tableCount;
        continue;
      }
      List<ActivitySignRecordDO> rows =
          activitySignRecordMapper.selectPage(table, q, (int) globalOffset, remain);
      fillRecordDisplayNames(rows);
      result.addAll(
          rows.stream().map(r -> toRecordExportResp(r, q.getContinuousCycleDays())).toList());
      remain = pageSize - result.size();
      globalOffset = 0;
    }
    return result;
  }

  /** 从 ES 分页获取签到记录导出数据：使用 search_after，避免 from 深分页超过 ES 默认窗口。 */
  private SearchAfterPage<ActivitySignRecordExportRespVO> getRecordExportDataFromEs(
      ActivitySignRecordExportReqVO reqVO, Page<ActivitySignRecordExportRespVO> page) {
    int pageSize = Math.max((int) page.getSize(), 5000);
    try {
      ActivitySignRecordPageReqVO q = buildRecordPageReq(reqVO);
      normalizeRecordPageReq(q);
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s -> {
                s.index(SIGN_RECORD_INDEX)
                    .query(query -> query.bool(buildRecordPageBool(q)))
                    .size(pageSize)
                    .trackTotalHits(t -> t.enabled(true))
                    .sort(sort -> sort.field(f -> f.field("signTime").order(SortOrder.Desc)))
                    .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc)));
                // 如果 Excel 导出框架传入上一页游标，继续从游标之后读取下一批数据。
                if (!CollectionUtils.isEmpty(reqVO.getSearchAfter())) {
                  s.searchAfter(reqVO.getSearchAfter());
                }
                return s;
              },
              Map.class);
      List<Hit<Map>> hits =
          response.hits() == null || response.hits().hits() == null
              ? Collections.emptyList()
              : response.hits().hits();
      if (CollectionUtils.isEmpty(hits)) {
        return SearchAfterPage.empty(totalHits(response));
      }
      List<ActivitySignRecordDO> records =
          hits.stream()
              .map(Hit::source)
              .filter(Objects::nonNull)
              .map(this::toSignRecordFromEs)
              .filter(Objects::nonNull)
              .toList();
      fillRecordDisplayNames(records);
      SearchAfterPage<ActivitySignRecordExportRespVO> result =
          new SearchAfterPage<>(
              records.stream()
                  .map(r -> toRecordExportResp(r, q.getContinuousCycleDays()))
                  .toList(),
              totalHits(response));
      // 如果本页已经不足一批，说明没有下一页，不设置游标让导出框架停止。
      if (hits.size() >= pageSize && !CollectionUtils.isEmpty(hits.get(hits.size() - 1).sort())) {
        result.setSearchAfter(hits.get(hits.size() - 1).sort());
      }
      return result;
    } catch (Exception e) {
      log.warn("签到记录导出 ES 查询失败, req={}", reqVO, e);
      throw new RuntimeException("签到记录导出 ES 查询失败", e);
    }
  }

  /**
   * 分页获取奖励发放记录导出数据。
   *
   * <p>注意：奖励记录表按手机号尾号拆成 10 张表，这里按全局 offset 跳过前面分表的数据，不在循环里重复查已跳过的数据。
   */
  private List<ActivitySignRewardRecordExportRespVO> getRewardExportData(
      Page<ActivitySignRewardRecordExportRespVO> page, ActivitySignRewardRecordPageReqVO q) {
    int pageNo = Math.max((int) page.getCurrent(), 1);
    int pageSize = Math.max((int) page.getSize(), 10000);
    long globalOffset = (long) (pageNo - 1) * pageSize;
    int remain = pageSize;
    List<ActivitySignRewardRecordExportRespVO> result = new ArrayList<>();
    for (int i = 0; i < 10 && remain > 0; i++) {
      String table = rewardTable(i);
      long tableCount = activitySignRewardRecordMapper.countPage(table, q);
      if (globalOffset >= tableCount) {
        globalOffset -= tableCount;
        continue;
      }
      List<ActivitySignRewardRecordDO> rows =
          activitySignRewardRecordMapper.selectPage(table, q, (int) globalOffset, remain);
      fillRewardDisplayNames(rows);
      result.addAll(rows.stream().map(this::toRewardExportResp).toList());
      remain = pageSize - result.size();
      globalOffset = 0;
    }
    return result;
  }

  /** 从 ES 分页获取奖励发放记录导出数据：使用 search_after，避免 from 深分页超过 ES 默认窗口。 */
  private SearchAfterPage<ActivitySignRewardRecordExportRespVO> getRewardExportDataFromEs(
      ActivitySignRewardRecordExportReqVO reqVO, Page<ActivitySignRewardRecordExportRespVO> page) {
    int pageSize = Math.max((int) page.getSize(), 5000);
    try {
      ActivitySignRewardRecordPageReqVO q = buildRewardPageReq(reqVO);
      normalizeRewardPageReq(q);
      SearchResponse<Map> response =
          elasticsearchClient.search(
              s -> {
                s.index(SIGN_REWARD_INDEX)
                    .query(query -> query.bool(buildRewardPageBool(q)))
                    .size(pageSize)
                    .trackTotalHits(t -> t.enabled(true))
                    .sort(sort -> sort.field(f -> f.field("issueTime").order(SortOrder.Desc)))
                    .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc)));
                // 如果 Excel 导出框架传入上一页游标，继续从游标之后读取下一批数据。
                if (!CollectionUtils.isEmpty(reqVO.getSearchAfter())) {
                  s.searchAfter(reqVO.getSearchAfter());
                }
                return s;
              },
              Map.class);
      List<Hit<Map>> hits =
          response.hits() == null || response.hits().hits() == null
              ? Collections.emptyList()
              : response.hits().hits();
      if (CollectionUtils.isEmpty(hits)) {
        return SearchAfterPage.empty(totalHits(response));
      }
      List<ActivitySignRewardRecordDO> records =
          hits.stream()
              .map(Hit::source)
              .filter(Objects::nonNull)
              .map(this::toRewardRecordFromEs)
              .filter(Objects::nonNull)
              .toList();
      fillRewardDisplayNames(records);
      SearchAfterPage<ActivitySignRewardRecordExportRespVO> result =
          new SearchAfterPage<>(
              records.stream().map(this::toRewardExportResp).toList(), totalHits(response));
      // 如果本页已经不足一批，说明没有下一页，不设置游标让导出框架停止。
      if (hits.size() >= pageSize && !CollectionUtils.isEmpty(hits.get(hits.size() - 1).sort())) {
        result.setSearchAfter(hits.get(hits.size() - 1).sort());
      }
      return result;
    } catch (Exception e) {
      log.warn("签到奖励发放记录导出 ES 查询失败, req={}", reqVO, e);
      throw new RuntimeException("签到奖励发放记录导出 ES 查询失败", e);
    }
  }

  /** 构造签到记录导出文件名。 */
  private String buildSignRecordExportFileName(ActivitySignRecordExportReqVO reqVO) {
    return "签到记录_" + reqVO.getActivityId() + "_" + LocalDate.now() + ".xlsx";
  }

  /** 构造奖励发放记录导出文件名。 */
  private String buildSignRewardExportFileName(ActivitySignRewardRecordExportReqVO reqVO) {
    return "签到奖励发放记录_" + reqVO.getActivityId() + "_" + LocalDate.now() + ".xlsx";
  }


  /** 回填签到记录展示名称：历史 ES/MySQL 记录里可能没有会员昵称和门店名称，列表与导出展示前统一补齐。 */
  private void fillRecordDisplayNames(List<ActivitySignRecordDO> records) {
    if (CollectionUtils.isEmpty(records)) return;
    Map<Long, String> storeNameMap = loadStoreNameMap(records.stream().map(ActivitySignRecordDO::getStoreId).toList());
    Map<Long, String> memberNameMap =
        loadMemberNameMapByIds(records.stream().map(ActivitySignRecordDO::getTriggerMemberId).toList());
    for (ActivitySignRecordDO record : records) {
      // 如果签到记录自身没有会员昵称，则按 memberId 从批量会员结果中回填。
      if (!StringUtils.hasText(record.getMemberName()) && record.getTriggerMemberId() != null) {
        record.setMemberName(memberNameMap.get(record.getTriggerMemberId()));
      }
      // 如果签到记录自身没有门店名称，则按 storeId 从批量门店结果中回填。
      if (!StringUtils.hasText(record.getStoreName()) && record.getStoreId() != null) {
        record.setStoreName(storeNameMap.get(record.getStoreId()));
      }
    }
  }

  /** 回填发放记录展示名称：发放记录列表/导出也需要会员昵称和门店名称。 */
  private void fillRewardDisplayNames(List<ActivitySignRewardRecordDO> records) {
    if (CollectionUtils.isEmpty(records)) return;
    Map<Long, String> storeNameMap = loadStoreNameMap(records.stream().map(ActivitySignRewardRecordDO::getStoreId).toList());
    Map<Long, String> memberNameMap =
        loadMemberNameMapByIds(records.stream().map(ActivitySignRewardRecordDO::getMemberId).toList());
    for (ActivitySignRewardRecordDO record : records) {
      // 如果发放记录自身没有会员昵称，则按 memberId 从批量会员结果中回填，避免循环查会员表。
      if (!StringUtils.hasText(record.getMemberName()) && record.getMemberId() != null) {
        record.setMemberName(memberNameMap.get(record.getMemberId()));
      }
      // 如果发放记录自身没有门店名称，则按 storeId 从批量门店结果中回填。
      if (!StringUtils.hasText(record.getStoreName()) && record.getStoreId() != null) {
        record.setStoreName(storeNameMap.get(record.getStoreId()));
      }
    }
  }

  /** 批量查询门店名称：同一批次只调一次门店 RPC，避免列表和导出循环查门店。 */
  private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
    List<Long> ids =
        storeIds == null
            ? Collections.emptyList()
            : storeIds.stream().filter(Objects::nonNull).distinct().toList();
    if (CollectionUtils.isEmpty(ids)) return Collections.emptyMap();
    try {
      CommonResult<List<StoreInfoDTO>> result = storeApi.getStoresByStoreIds(ids);
      List<StoreInfoDTO> stores = result == null ? null : result.getData();
      if (result == null || !result.isSuccess() || CollectionUtils.isEmpty(stores)) return Collections.emptyMap();
      return stores.stream()
          .filter(s -> s.getStoreId() != null && StringUtils.hasText(s.getStoreName()))
          .collect(Collectors.toMap(StoreInfoDTO::getStoreId, StoreInfoDTO::getStoreName, (a, b) -> a));
    } catch (Exception e) {
      log.warn("签到记录查询门店名称失败 storeIds={}", ids, e);
      return Collections.emptyMap();
    }
  }

  /**
   * 批量查询会员展示名称：只用于 PC 列表/导出补昵称，不能复用发积分的 getWxMemberById 链路。
   * getWxMemberById 会查询历史 VO 字段 project_owner_ship，部分环境 wx_member 表没有该字段，展示补全失败不应影响列表查询。
   */
  private Map<Long, String> loadMemberNameMapByIds(List<Long> memberIds) {
    List<Long> idList =
        memberIds == null
            ? Collections.emptyList()
            : memberIds.stream().filter(Objects::nonNull).distinct().toList();
    if (CollectionUtils.isEmpty(idList)) return Collections.emptyMap();
    Map<Long, String> nameMap = new HashMap<>();
    for (int from = 0; from < idList.size(); from += MEMBER_NAME_QUERY_BATCH_SIZE) {
      List<Long> batch = idList.subList(from, Math.min(from + MEMBER_NAME_QUERY_BATCH_SIZE, idList.size()));
      try {
        Map<Long, String> batchMap = wxMemberApi.getMemberNameMapByIds(batch);
        if (!CollectionUtils.isEmpty(batchMap)) {
          nameMap.putAll(batchMap);
        }
      } catch (Exception e) {
        log.warn("签到记录批量查询会员昵称失败 memberIds={}", batch, e);
      }
    }
    return nameMap;
  }

  /** 签到记录 DO 转导出 VO。 */
  private ActivitySignRecordExportRespVO toRecordExportResp(ActivitySignRecordDO r, Integer cycle) {
    ActivitySignRecordExportRespVO v = new ActivitySignRecordExportRespVO();
    v.setMemberName(r.getMemberName());
    v.setMemberMobile(r.getMemberMobile());
    v.setMemberId(r.getTriggerMemberId() == null ? null : String.valueOf(r.getTriggerMemberId()));
    v.setStoreId(r.getStoreId() == null ? null : String.valueOf(r.getStoreId()));
    v.setStoreName(r.getStoreName());
    v.setSignDate(r.getSignDate());
    v.setSignTime(r.getSignTime());
    v.setSignTypeName(signTypeName(r.getSignType()));
    v.setContinuousCycleCount(cycle == null || cycle <= 0 ? 0 : r.getContinuousDaysAfter() / cycle);
    v.setMaxContinuousDays(r.getContinuousDaysAfter());
    v.setTotalSignDays(r.getTotalDaysAfter());
    return v;
  }

  /** 奖励发放记录 DO 转导出 VO。 */
  private ActivitySignRewardRecordExportRespVO toRewardExportResp(ActivitySignRewardRecordDO r) {
    ActivitySignRewardRecordExportRespVO v = new ActivitySignRewardRecordExportRespVO();
    v.setMemberName(r.getMemberName());
    v.setMemberMobile(r.getMemberMobile());
    v.setPrizeTypeName(prizeTypeName(r.getPrizeType()));
    v.setPrizeContent(r.getPrizeContent());
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setIssueTime(r.getIssueTime());
    v.setIssueStatusName(issueStatusName(r.getIssueStatus()));
    v.setClaimStatusName(claimStatusName(r.getClaimStatus()));
    v.setReceiveUser(r.getReceiveUser());
    v.setReceiveMobile(r.getReceiveMobile());
    v.setReceiveAddress(r.getReceiveAddress());
    v.setTrackingNumber(r.getTrackingNumber());
    v.setExpressCompany(r.getExpressCompany());
    v.setStoreName(r.getStoreName());
    return v;
  }

  /** 签到类型转换为导出展示文案。 */
  private String signTypeName(Integer signType) {
    if (Objects.equals(signType, 1)) return "正常签到";
    if (Objects.equals(signType, 2)) return "补签";
    if (Objects.equals(signType, 3)) return "后台补录";
    return "-";
  }

  /** 奖励类型转换为导出展示文案，枚举值与 ActivityCqPrizeTypeEnum 保持一致。 */
  private String prizeTypeName(Integer prizeType) {
    if (Objects.equals(prizeType, 1)) return "优惠券";
    if (Objects.equals(prizeType, 2)) return "积分";
    if (Objects.equals(prizeType, 3)) return "实物奖品";
    if (Objects.equals(prizeType, 5)) return "现金红包";
    if (Objects.equals(prizeType, 6)) return "优惠券包";
    return "-";
  }

  /** 发放状态转换为导出展示文案。 */
  private String issueStatusName(Integer issueStatus) {
    if (Objects.equals(issueStatus, ISSUE_SUCCESS)) return "已发放";
    if (Objects.equals(issueStatus, ISSUE_FAILED)) return "发放失败";
    return "-";
  }

  /** 红包领取状态转换为导出展示文案。 */
  private String claimStatusName(Integer claimStatus) {
    if (Objects.equals(claimStatus, 1)) return "未领取";
    if (Objects.equals(claimStatus, 2)) return "已领取";
    if (Objects.equals(claimStatus, 3)) return "已过期";
    return "-";
  }

  /** 签到记录 DO 转 PC 列表 VO，并计算连签周期次数。 */
  private ActivitySignRecordRespVO toRecordResp(ActivitySignRecordDO r, Integer cycle) {
    ActivitySignRecordRespVO v = new ActivitySignRecordRespVO();
    v.setId(r.getId());
    v.setActivityId(r.getActivityId());
    v.setMemberMobile(r.getMemberMobile());
    v.setMemberId(r.getTriggerMemberId());
    v.setMemberName(r.getMemberName());
    v.setStoreId(r.getStoreId());
    v.setStoreName(r.getStoreName());
    v.setSignDate(r.getSignDate());
    v.setSignTime(r.getSignTime());
    v.setSignType(r.getSignType());
    v.setContinuousCycleCount(cycle == null || cycle <= 0 ? 0 : r.getContinuousDaysAfter() / cycle);
    v.setMaxContinuousDays(r.getContinuousDaysAfter());
    v.setTotalSignDays(r.getTotalDaysAfter());
    return v;
  }

  /** 奖励发放记录 DO 转 PC 列表 VO。 */
  private ActivitySignRewardRecordRespVO toRewardResp(ActivitySignRewardRecordDO r) {
    ActivitySignRewardRecordRespVO v = new ActivitySignRewardRecordRespVO();
    v.setId(r.getId());
    v.setActivityId(r.getActivityId());
    v.setPrizeId(r.getPrizeId());
    v.setPrizeType(r.getPrizeType());
    v.setPrizeContent(r.getPrizeContent());
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setMemberName(r.getMemberName());
    v.setMemberMobile(r.getMemberMobile());
    v.setIssueTime(r.getIssueTime());
    v.setClaimStatus(r.getClaimStatus());
    v.setReceiveUser(r.getReceiveUser());
    v.setReceiveMobile(r.getReceiveMobile());
    v.setReceiveAddress(r.getReceiveAddress());
    v.setTrackingNumber(r.getTrackingNumber());
    v.setExpressCompany(r.getExpressCompany());
    return v;
  }

  /** 奖励发放记录 DO 转小程序我的奖励 VO。 */
  private AppActivitySignMyRewardItemRespVO toMyReward(ActivitySignRewardRecordDO r) {
    AppActivitySignMyRewardItemRespVO v = new AppActivitySignMyRewardItemRespVO();
    v.setRewardRecordId(r.getId());
    v.setPrizeId(appPrizeId(r.getPrizeType(), r.getPrizeId()));
    v.setPrizeType(r.getPrizeType());
    v.setPrizeContent(r.getPrizeContent());
    v.setPrizeImgUrl(r.getPrizeImgUrl());
    v.setPrizeValue(r.getPrizeValue());
    v.setIssueStatus(r.getIssueStatus());
    v.setIssueTime(r.getIssueTime());
    v.setClaimStatus(r.getClaimStatus());
    v.setOutBillNo(r.getOutBillNo());
    v.setPackageInfo(r.getPackageInfo());
    v.setPrizeState(r.getPrizeState());
    return v;
  }

  /** 查找用户参与过且当前仍可用的活动，用于首页签到进度模块。 */
  private ActivityDO findLatestParticipatedRunningActivity(Long storeId, String mobile) {
    List<Long> activityIds =
        activitySignRecordMapper.selectActivityIdsByMobile(recordTable(shard(mobile)), mobile);
    // 如果该手机号没有参与过任何签到活动，首页不展示签到进度
    if (CollectionUtils.isEmpty(activityIds)) return null;
    List<ActivityDO> activities = activityMapper.selectByIds(new LinkedHashSet<>(activityIds));
    // 如果参与记录里的活动都被删除了，首页不展示签到进度
    if (CollectionUtils.isEmpty(activities)) return null;
    List<ActivityDO> runningActivities =
        activities.stream()
            .filter(Objects::nonNull)
            .filter(activity -> statusOf(activity) == 1)
            .sorted(
                Comparator.comparing(
                        ActivityDO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(
                        ActivityDO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    // 如果用户参与过的活动当前都未开始、已结束或已停用，首页不展示签到进度
    if (CollectionUtils.isEmpty(runningActivities)) return null;
    // 如果当前门店为空，只能命中全部门店活动，不能命中部分门店活动
    if (storeId == null)
      return runningActivities.stream()
          .filter(activity -> Objects.equals(activity.getActivityStore(), 1))
          .findFirst()
          .orElse(null);
    return firstActivityMatchStore(runningActivities, storeId);
  }

  /** 从已排序的候选活动中找到第一个命中门店的活动；严格保持创建时间升序，保证同门店多个活动时最早创建的活动优先命中。 */
  private ActivityDO firstActivityMatchStore(List<ActivityDO> activities, Long storeId) {
    // 如果候选活动列表为空，无法命中当前门店。
    if (CollectionUtils.isEmpty(activities)) return null;
    Set<Long> matchedPartStoreActivityIds = Collections.emptySet();
    // 如果当前门店ID不为空，先一次性查出当前门店命中的部分门店活动，后面按候选活动顺序判断，避免循环查库。
    if (storeId != null) {
      List<Long> partStoreActivityIds =
          activities.stream()
              .filter(activity -> !Objects.equals(activity.getActivityStore(), 1))
              .map(ActivityDO::getId)
              .filter(Objects::nonNull)
              .toList();
      if (!CollectionUtils.isEmpty(partStoreActivityIds)) {
        List<ActivityStoreDO> storeRelations =
            activityStoreMapper.selectList(
                new LambdaQueryWrapper<ActivityStoreDO>()
                    .in(ActivityStoreDO::getActivityId, partStoreActivityIds)
                    .eq(ActivityStoreDO::getStoreId, storeId));
        if (!CollectionUtils.isEmpty(storeRelations)) {
          matchedPartStoreActivityIds =
              storeRelations.stream().map(ActivityStoreDO::getActivityId).collect(Collectors.toSet());
        }
      }
    }
    for (ActivityDO activity : activities) {
      // 如果活动适用全部门店，按照当前排序顺序直接命中。
      if (Objects.equals(activity.getActivityStore(), 1)) return activity;
      // 如果活动是部分门店，并且当前门店在活动门店关系里，按照当前排序顺序命中。
      if (storeId != null && matchedPartStoreActivityIds.contains(activity.getId())) return activity;
    }
    return null;
  }

  /** 判断活动是否适用于当前门店：全部门店直接命中，单个活动兜底判断时才查门店关系。 */
  private boolean activityMatchStore(ActivityDO activity, Long storeId) {
    if (activity == null) return false;
    // 如果活动适用全部门店，当前门店直接命中
    if (Objects.equals(activity.getActivityStore(), 1)) return true;
    // 如果当前门店ID为空，部分门店活动无法命中
    if (storeId == null) return false;
    return firstActivityMatchStore(List.of(activity), storeId) != null;
  }

  /** 查找当前门店可进入的签到活动：优先 Redis，失败查 MySQL，多个活动按创建时间升序，最早创建优先。 */
  private ActivityDO findLatestRunningActivity(Long storeId) {
    ActivityDO cacheActivity = findRunningActivityFromStoreHitCache(storeId);
    // 如果Redis 门店命中缓存里找到了可用活动，直接返回
    if (cacheActivity != null) return cacheActivity;
    LocalDate now = signToday();
    List<ActivityDO> list =
        activityMapper.selectList(
            new LambdaQueryWrapper<ActivityDO>()
                .eq(ActivityDO::getActivityType, ActivityTypeEnum.SIGN.getCode())
                .eq(ActivityDO::getIsEnabled, ENABLED)
                .and(
                    w ->
                        w.isNull(ActivityDO::getStartDate)
                            .or()
                            .le(ActivityDO::getStartDate, java.sql.Date.valueOf(now)))
                .and(
                    w ->
                        w.isNull(ActivityDO::getEndDate)
                            .or()
                            .ge(ActivityDO::getEndDate, java.sql.Date.valueOf(now)))
                .orderByAsc(ActivityDO::getCreateTime)
                .orderByAsc(ActivityDO::getId));
    if (CollectionUtils.isEmpty(list)) return null;
    return firstActivityMatchStore(list, storeId);
  }

  /** 计算活动状态：0未开始、1进行中、2已结束、3停用。 */
  private int statusOf(ActivityDO a) {
    // 如果活动未启用，返回停用状态
    if (!Objects.equals(a.getIsEnabled(), 1)) return 3;
    LocalDateTime now = signNow(),
        st = toLdt(a.getStartDate()),
        et = toEndLdt(a.getEndDate());
    // 如果当前时间早于活动开始时间，返回未开始状态
    if (st != null && now.isBefore(st)) return 0;
    // 如果当前时间晚于活动结束时间，返回已结束状态
    if (et != null && now.isAfter(et)) return 2;
    return 1;
  }

  /** 构造内部统计用签到记录查询条件。 */
  private ActivitySignRecordPageReqVO fakeRecordReq(Long aid) {
    ActivitySignRecordPageReqVO q = new ActivitySignRecordPageReqVO();
    q.setActivityId(aid);
    q.setPageNo(1);
    q.setPageSize(1000000);
    q.setContinuousCycleDays(1);
    return q;
  }

  private static class PeriodWindow {
    LocalDate start;
    LocalDate end;
    String key;

    PeriodWindow(LocalDate start, LocalDate end, String key) {
      this.start = start;
      this.end = end;
      this.key = key;
    }
  }

  private static class Progress {
    Long activityId;
    String mobile;
    LocalDate today;
    String periodKey;
    Set<String> grantedRewardKeys = Collections.emptySet();
    int continuous;
    int total;
    boolean todaySigned;
    Integer progress;
    Integer target;
    Integer need;
    boolean finished;
  }

  @Data
  private static class ActivitySignCache {
    private ActivityDO activity;
    private Long startTimeMs;
    private Long endTimeMs;
    private ActivitySignDO sign;
    private List<ActivitySignRewardDO> rewards;
  }
}
