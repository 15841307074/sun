package com.htyoudao.youdao.module.promotion.controller.app.activitySign;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo.*;
import com.htyoudao.youdao.module.promotion.service.activitySign.ActivitySignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小程序 - 签到活动")
@RestController
@RequestMapping("/promotion/app/activity-sign")
@DataPermission(enable = false)
public class AppActivitySignController {

  @Resource private ActivitySignService activitySignService;

  /** 小程序首页签到进度命中：只展示用户已参与且活动仍在期间内的签到活动。storeId 为当前门店。 */
  @GetMapping("/hit")
  @Operation(summary = "根据门店获取命中的签到活动")
  public CommonResult<AppActivitySignHitRespVO> hit(
      @RequestParam("storeId") Long storeId,
      @Parameter(hidden = true) @RequestParam(value = "forceMysql", required = false)
          Boolean forceMysql) {
    return success(activitySignService.hit(storeId, forceMysql));
  }

  /** 小程序签到活动详情：返回活动图片、规则、当日签到状态、周日历和奖励进度。 */
  @PostMapping("/detail")
  @Operation(summary = "获取签到活动详情")
  public CommonResult<AppActivitySignDetailRespVO> detail(
      @Valid @RequestBody AppActivitySignReqVO reqVO) {
    return success(activitySignService.detail(reqVO));
  }

  /** 小程序立即签到主流程：按手机号维度防重、落 MySQL、更新 Redis 日历、同步发奖，并在事务提交后投递 MQ 到 ES。 */
  @PostMapping("/sign")
  @Operation(summary = "立即签到")
  public CommonResult<AppActivitySignResultRespVO> sign(
      @Valid @RequestBody AppActivitySignReqVO reqVO) {
    return success(activitySignService.sign(reqVO));
  }

  /** 小程序签到记录：入参只有活动ID，返回该手机号在活动中的已签到日期列表；优先 Redis，失败降级 MySQL。 */
  @PostMapping("/sign-record")
  @Operation(summary = "获取我的签到记录")
  public CommonResult<AppActivitySignCalendarRespVO> signRecord(
      @Valid @RequestBody AppActivitySignRecordReqVO reqVO) {
    return success(activitySignService.signRecord(reqVO));
  }

  /** 小程序我的奖励：按当前登录手机号查询当前活动已获得的奖励记录。 */
  @GetMapping("/my-rewards")
  @Operation(summary = "获取我的奖励")
  public CommonResult<AppActivitySignMyRewardRespVO> myRewards(
      @RequestParam("activityId") Long activityId) {
    return success(activitySignService.myRewards(activityId));
  }

  /** 小程序填写实物奖品收货地址：按奖励记录ID扫描分表并更新地址。 */
  @PostMapping("/reward/physical/address")
  @Operation(summary = "实物奖励填写地址")
  public CommonResult<Boolean> savePhysicalAddress(
      @Valid @RequestBody AppActivitySignAddressReqVO reqVO) {
    return success(activitySignService.savePhysicalAddress(reqVO));
  }
}
