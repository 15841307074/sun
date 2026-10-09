package com.htyoudao.youdao.module.bpm.service.storeInspection.template.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo.*;
import com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo.InspectionTemplateDropDownRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.StoreInspectionTemplateDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.TemplateChecklistDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.template.TemplateVisibleUserDO;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.template.StoreInspectionTemplateMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.template.TemplateChecklistMapper;
import com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.template.TemplateVisibleUserMapper;
import com.htyoudao.youdao.module.bpm.service.storeInspection.template.StoreInspectionTemplateService;
import com.htyoudao.youdao.module.system.api.dept.DeptOrgApi;
import jakarta.annotation.Resource;
import jakarta.json.Json;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 巡店模板 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class StoreInspectionTemplateServiceImpl implements StoreInspectionTemplateService {


    @DubboReference
    private DeptOrgApi deptOrgApi;

    @Resource
    private StoreInspectionTemplateMapper templateMapper;

    @Resource
    private TemplateChecklistMapper templateChecklistMapper;

    @Resource
    private TemplateVisibleUserMapper templateVisibleUserMapper;

    @Override
    public List<InspectionTemplateDropDownRespVO> getInspectionTemplate() {
        Long userId = WebFrameworkUtils.getLoginUserId();

        // 1. 查询当前用户被授权可见的模板ID
        List<Long> templateIds = templateVisibleUserMapper.selectList(
                        new LambdaQueryWrapper<TemplateVisibleUserDO>()
                                .select(TemplateVisibleUserDO::getTemplateId)
                                .eq(TemplateVisibleUserDO::getUserId, userId)
                ).stream()
                .map(TemplateVisibleUserDO::getTemplateId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2. 查询模板：全部可见 or 指定给当前用户可见
        LambdaQueryWrapper<StoreInspectionTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(StoreInspectionTemplateDO::getTemplateId, StoreInspectionTemplateDO::getTemplateName)
                .and(w -> w
                        .eq(StoreInspectionTemplateDO::getVisibilityType, false)
                        .or(CollectionUtil.isNotEmpty(templateIds))
                        .in(CollectionUtil.isNotEmpty(templateIds),
                                StoreInspectionTemplateDO::getTemplateId, templateIds)
                )
                .orderByDesc(StoreInspectionTemplateDO::getCreateTime)
                .orderByAsc(StoreInspectionTemplateDO::getTemplateName);

        List<StoreInspectionTemplateDO> templateList = templateMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(templateList)) {
            return List.of();
        }

        return templateList.stream()
                .map(item -> {
                    InspectionTemplateDropDownRespVO vo = new InspectionTemplateDropDownRespVO();
                    vo.setTemplateId(item.getTemplateId());
                    vo.setTemplateName(item.getTemplateName());
                    return vo;
                })
                .toList();
    }


    @Override
    public Integer create(TemplateReqVO templateReqVO) {

        validSaveOrUpdateTemplate(templateReqVO);

        // 1.创建模板主表
        StoreInspectionTemplateDO bean = BeanUtils.toBean(templateReqVO, StoreInspectionTemplateDO.class);
        int insert = templateMapper.insert(bean);
        // 2.获取主表主键
        Long templateId = bean.getTemplateId();
        // 3.判断主表可见性
        createSonTableForTemplate(templateReqVO,bean,templateId);

        return insert;
    }

    private void createSonTableForTemplate(TemplateReqVO templateReqVO, StoreInspectionTemplateDO bean, Long templateId) {
        Integer visibilityType = bean.getVisibilityType();
        if (visibilityType != null && visibilityType == 1){
            // 批量插入可见用户
            List<TemplateUserVO> userIds = templateReqVO.getUserIds();
            if (CollectionUtil.isNotEmpty(userIds)) {
                templateVisibleUserMapper.insertBatch(
                        userIds.stream().map(user ->
                                new TemplateVisibleUserDO()
                                        .setUserId(user.getUserId())
                                        .setUserName(user.getUserName())
                                        .setTemplateId(templateId)
                        ).toList()
                );
            }else {
                throw exception(VISIBILITY_TYPE_ERROR);
            }
        }

        // 4.批量插入模板点检项
        List<TemplateCheckListReqVO> templateChecklists = templateReqVO.getTemplateChecklists();
        // 批量设置模板ID
        Optional.ofNullable(templateChecklists).ifPresent(list ->{
            Map<String, Integer> typeSortMap = new HashMap<>();
            int[] sort = {1}; // 数组用于 lambda 内自增

            list.forEach(item -> {
                item.setTemplateId(templateId);
                item.setTypeSort(typeSortMap.computeIfAbsent(item.getTypeId(), id -> sort[0]++));
            });
        });

        if (CollectionUtil.isNotEmpty(templateChecklists)){
            // 批量插入
            List<TemplateChecklistDO> checklistDOList = BeanUtils.toBean(templateChecklists, TemplateChecklistDO.class);
            templateChecklistMapper.insertBatch(checklistDOList);
        }
    }

    private void validSaveOrUpdateTemplate(TemplateReqVO templateReqVO) {

        // 模板名称校验
        String templateName = templateReqVO.getTemplateName();
        if (StringUtils.isBlank(templateName)){
            throw exception(TEMPLATE_NAME_BLANK_ERROR);
        }else {
            if (templateName.length()>20){
                throw exception(TEMPLATE_NAME_TOO_LARGE_ERROR);
            }
            LambdaQueryWrapper<StoreInspectionTemplateDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StoreInspectionTemplateDO::getTemplateName, templateName);

            if (Objects.nonNull(templateReqVO.getTemplateId())){
                // 编辑
                wrapper.ne(StoreInspectionTemplateDO::getTemplateId, templateReqVO.getTemplateId());
            }

            List<StoreInspectionTemplateDO> storeInspectionTemplateDOS = templateMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(storeInspectionTemplateDOS)){
                throw exception(TEMPLATE_NAME_UNIQUE_ERROR);
            }
        }

        // 选择用户校验
        Integer visibilityType = templateReqVO.getVisibilityType();
        // 不能为空，且只能是 0 或 1
        if (visibilityType == null || (visibilityType != 0 && visibilityType != 1)) {
            throw exception(VISIBILITY_TYPE_NULL_ERROR);
        }

        // 如果是 1（指定人员可见），则 userIds 必须传值
        if (visibilityType == 1) {
            List<TemplateUserVO> userIds = templateReqVO.getUserIds();
            if (CollectionUtil.isEmpty(userIds)) {
                throw exception(VISIBILITY_TYPE_USERID_NULL_ERROR);
            }
        }

        // 点检项规则校验
        List<TemplateCheckListReqVO> templateChecklists = templateReqVO.getTemplateChecklists();
        if (CollectionUtil.isNotEmpty(templateChecklists)) {

            // 图片上传规则校验
            validImgRule(templateChecklists);

            // 描述规则校验
            validDescriptionRule(templateChecklists);
            
            // 不适用规则校验
            validInapplicabilityRule(templateChecklists);
            
            // 奖惩规则校验
            validRewardPunishmentRule(templateChecklists);

            // 大类点检项排序校验
            validSort(templateChecklists);
        }
    }

    private void validSort(List<TemplateCheckListReqVO> templateChecklists) {
        // key: typeId, value: 该分类下已存在的sort集合
        Map<String, Set<Integer>> typeSortMap = new HashMap<>();

        templateChecklists.forEach(item -> {
            String title = item.getTitle();
            Integer sort = item.getSort();
            String typeId = item.getTypeId();

            // 1. 排序不能为空
            if (sort == null) {
                ErrorCode errorCode = new ErrorCode(1_010_017_031, "点检项【" + title + "】排序不能为空");
                throw exception(errorCode);
            }

            // 2. 同一个 typeId 下 sort 不能重复
            Set<Integer> sortSet = typeSortMap.getOrDefault(typeId, new HashSet<>());
            if (sortSet.contains(sort)) {
                ErrorCode errorCode = new ErrorCode(1_010_017_032, "点检项【" + title + "】同一分类下排序值不能重复");
                throw exception(errorCode);
            }

            // 加入已使用排序
            sortSet.add(sort);
            typeSortMap.put(typeId, sortSet);
        });

    }

    private void validInapplicabilityRule(List<TemplateCheckListReqVO> templateChecklists) {
        templateChecklists.forEach(checkItem -> {
            String inapplicabilityRuleJson = checkItem.getInapplicabilityRule();

            // 1. 规则为空 走默认
            if (StringUtils.isBlank(inapplicabilityRuleJson)) {
                return;
            }

            // 2. JSON 转对象
            InapplicabilityRuleVO ruleVO;
            try {
                ruleVO = JSONUtil.toBean(inapplicabilityRuleJson, InapplicabilityRuleVO.class);
            } catch (Exception e) {
                ErrorCode errorCode = new ErrorCode(1_010_017_027, "点检项【" + checkItem.getTitle() + "】不适用规则格式错误");
                throw exception(errorCode);
            }

            // 3. 校验开关：0-不显示 1-显示
            Integer showFlag = ruleVO.getShowFlag();
            if (showFlag == null || (showFlag != 0 && showFlag != 1)) {
                ErrorCode errorCode = new ErrorCode(1_010_017_028, "点检项【" + checkItem.getTitle() + "】请选择是否显示不适用");
                throw exception(errorCode);
            }
        });
    }

    private void validDescriptionRule(List<TemplateCheckListReqVO> templateChecklists) {
        templateChecklists.forEach(checkItem -> {
            String descriptionRuleJson = checkItem.getDescriptionRule();

            // 1. 描述规则为空 走默认
            if (StringUtils.isBlank(descriptionRuleJson)) {
                return;
            }

            // 2. JSON 转对象
            DescriptionRuleVO descriptionRule;
            try {
                descriptionRule = JSONUtil.toBean(descriptionRuleJson, DescriptionRuleVO.class);
            } catch (Exception e) {
                ErrorCode errorCode = new ErrorCode(1_010_017_022, "点检项【" + checkItem.getTitle() + "】描述规则格式错误");
                throw exception(errorCode);
            }

            // 3. 校验描述开关 0-不允许 1-允许
            Integer descriptionFlag = descriptionRule.getDescriptionFlag();
            if (descriptionFlag == null || (descriptionFlag != 0 && descriptionFlag != 1)) {
                ErrorCode errorCode = new ErrorCode(1_010_017_023, "点检项【" + checkItem.getTitle() + "】请选择描述规则开关");
                throw exception(errorCode);
            }

            // 4. 开启描述（flag=1）时，校验三个状态规则
            if (descriptionFlag == 1) {
                Integer qualified = descriptionRule.getQualified();
                Integer unqualified = descriptionRule.getUnqualified();
                Integer inapplicability = descriptionRule.getInapplicability();

                // 校验合格规则
                if (qualified == null || (qualified != 0 && qualified != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_024, "点检项【" + checkItem.getTitle() + "】请选择合格时描述是否必填");
                    throw exception(errorCode);
                }

                // 校验不合格规则
                if (unqualified == null || (unqualified != 0 && unqualified != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_025, "点检项【" + checkItem.getTitle() + "】请选择不合格时描述是否必填");
                    throw exception(errorCode);
                }

                // 校验不适用规则
                if (inapplicability == null || (inapplicability != 0 && inapplicability != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_026, "点检项【" + checkItem.getTitle() + "】请选择不适用时描述是否必填");
                    throw exception(errorCode);
                }
            }
        });
    }

    private void validRewardPunishmentRule(List<TemplateCheckListReqVO> templateChecklists) {
        templateChecklists.forEach(checkItem -> {
            String rewardRuleJson = checkItem.getRewardPunishmentRule();

            // 1. 奖惩规则为空 走默认，不校验
            if (StringUtils.isBlank(rewardRuleJson)) {
                return;
            }

            // 2. JSON 转对象
            RewardPunishmentRuleVO ruleVO;
            try {
                ruleVO = JSONUtil.toBean(rewardRuleJson, RewardPunishmentRuleVO.class);
            } catch (Exception e) {
                ErrorCode errorCode = new ErrorCode(1_010_017_015, "点检项【" + checkItem.getTitle() + "】奖惩规则格式错误");
                throw exception(errorCode);
            }

            // 3. 校验奖惩开关 0-不允许 1-允许
            Integer rewardPunishmentFlag = ruleVO.getRewardPunishmentFlag();
            if (rewardPunishmentFlag == null || (rewardPunishmentFlag != 0 && rewardPunishmentFlag != 1)) {
                ErrorCode errorCode = new ErrorCode(1_010_017_016, "点检项【" + checkItem.getTitle() + "】请选择奖惩规则开关");
                throw exception(errorCode);
            }

            // 4. 开启奖惩（flag=1）时，必须校验合格、不合格配置
            if (rewardPunishmentFlag == 1) {
                Integer qualified = ruleVO.getQualified();
                Integer unqualified = ruleVO.getUnqualified();

                // 校验合格时规则 0-选填 1-必填
                if (qualified == null || (qualified != 0 && qualified != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_017, "点检项【" + checkItem.getTitle() + "】请选择合格时金额是否必填");
                    throw exception(errorCode);
                }

                // 校验不合格时规则 0-选填 1-必填
                if (unqualified == null || (unqualified != 0 && unqualified != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_018, "点检项【" + checkItem.getTitle() + "】请选择不合格时金额是否必填");
                    throw exception(errorCode);
                }
            }
        });
    }

    private void validImgRule(List<TemplateCheckListReqVO> templateChecklists) {
        templateChecklists.forEach(checkItem -> {
            String imgRuleJson = checkItem.getImgRule();

            // 1. 图片规则为空 走默认
            if (StringUtils.isBlank(imgRuleJson)) {
                return;
            }

            // 2. JSON 转对象
            ImageRuleVO imgRule;
            try {
                imgRule = JSONUtil.toBean(imgRuleJson, ImageRuleVO.class);
            } catch (Exception e) {
                ErrorCode errorCode = new ErrorCode(1_010_017_010, "点检项【" + checkItem.getTitle() + "】图片规则格式错误");
                throw exception(errorCode);
            }

            // 3. 校验允许上传开关
            Integer imgFlag = imgRule.getImgFlag();
            if (imgFlag == null || (imgFlag != 0 && imgFlag != 1)) {
                ErrorCode errorCode = new ErrorCode(1_010_017_011, "点检项【" + checkItem.getTitle() + "】请选择是否允许上传图片");
                throw exception(errorCode);
            }

            // 4. 允许上传时，校验后续配置
            if (imgFlag == 1) {
                String imgState = imgRule.getImgState();
                Integer imgCount = imgRule.getImgCount();
                Integer localImgFlag = imgRule.getLocalImgFlag();

                // 必传状态校验
                if (StringUtils.isBlank(imgState)) {
//                    throw exception("点检项【" + checkItem.getTitle() + "】请选择图片必传规则");
                    ErrorCode errorCode = new ErrorCode(1_010_017_012, "点检项【\" + checkItem.getTitle() + \"】请选择图片必传规则");
                    throw exception(errorCode);
                }
                List<String> list = Arrays.stream(imgState.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .toList();

                // 核心规则
                if (list.contains("0") && list.size() > 1) {
                    throw exception(CHECKLIST_IMG_STATE_ERROR);
                }

                // 图片张数 1~12
                if (imgCount == null || imgCount < 1 || imgCount > 12) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_013,"点检项【\" + checkItem.getTitle() + \"】图片张数必须在 1~12 之间");
                    throw exception(errorCode);
                }
                // 本地上传
                if (localImgFlag == null || (localImgFlag != 0 && localImgFlag != 1)) {
                    ErrorCode errorCode = new ErrorCode(1_010_017_013, "点检项【\" + checkItem.getTitle() + \"】请选择是否允许本地上传");
                    throw exception(errorCode);
                }
            }
        });
    }

    @Override
    public TemplateRespVO getTemplateById(Long templateId) {
        // 获取用户可见的模板ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        StoreInspectionTemplateDO storeInspectionTemplateDO = templateMapper.selectById(templateId);
        TemplateRespVO respVO = BeanUtils.toBean(storeInspectionTemplateDO, TemplateRespVO.class);
        if (Objects.isNull(storeInspectionTemplateDO)){
            return null;
        }
        Integer visibilityType = storeInspectionTemplateDO.getVisibilityType();
        if (visibilityType != null && visibilityType == 1){
            // 指定成员
            LambdaQueryWrapper<TemplateVisibleUserDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TemplateVisibleUserDO::getTemplateId, templateId);
            List<TemplateVisibleUserDO> templateVisibleUserDOS = templateVisibleUserMapper.selectList(wrapper);
            List<Long> userIdList = templateVisibleUserDOS.stream()
                    .map(TemplateVisibleUserDO::getUserId)
                    .toList();

//            if (!userIdList.contains(userId)){
//                throw exception(USER_VISIBILITY_ERROR);
//            }

            List<TemplateUserVO> user = BeanUtils.toBean(templateVisibleUserDOS, TemplateUserVO.class);
            List<TemplateUserRespVO> respList = templateVisibleUserDOS.stream()
                    .map(vo -> {
                        TemplateUserRespVO resp = new TemplateUserRespVO();
                        resp.setId(vo.getUserId());       // userId → id
                        resp.setNickName(vo.getUserName());// userName → nickName
                        return resp;
                    })
                    .toList();
            respVO.setUserIds(respList);
        }
        // 获取模板点检项
        LambdaQueryWrapper<TemplateChecklistDO> checklistDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        checklistDOLambdaQueryWrapper.eq(TemplateChecklistDO::getTemplateId, templateId);
        checklistDOLambdaQueryWrapper.orderByAsc(TemplateChecklistDO::getTypeSort);
        checklistDOLambdaQueryWrapper.orderByAsc(TemplateChecklistDO::getSort);
        List<TemplateChecklistDO> checklistDOList = templateChecklistMapper.selectList(checklistDOLambdaQueryWrapper);
        List<TemplateCheckListRespVO> checkListRespVOS = BeanUtils.toBean(checklistDOList, TemplateCheckListRespVO.class);
        respVO.setTemplateChecklists(checkListRespVOS);
        return respVO;
    }

    @Override
    public PageResult<TemplateRespVO> query(TemplateQueryReqVO templateQueryReqVO) {
        // 获取用户可见的模板ID
        /*Long userId = SecurityFrameworkUtils.getLoginUserId();

        CommonResult<List<Long>> commonResult = deptOrgApi.getUserIdsByDept();
        // 获取所有可见的userId
        List<Long> userIds = commonResult.getCheckedData();
        if (CollectionUtil.isEmpty(userIds)) {
            throw exception(STORE_INSPECTION_USER_DEPT_ERROR);
        }

        LambdaQueryWrapper<TemplateVisibleUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TemplateVisibleUserDO::getTemplateId);
        queryWrapper.in(TemplateVisibleUserDO::getUserId, userIds);
        List<TemplateVisibleUserDO> templateVisibleUsers = templateVisibleUserMapper.selectList(queryWrapper);

        // 提取出可见的模板ID列表
        List<Long> visibleTemplateIds = Optional.ofNullable(templateVisibleUsers)
                .orElse(Collections.emptyList())
                .stream()
                .map(TemplateVisibleUserDO::getTemplateId)
                .toList();
        */
        // 3. 构建模板查询条件（核心权限SQL）
        LambdaQueryWrapper<StoreInspectionTemplateDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotBlank(templateQueryReqVO.getTemplateName()),
                StoreInspectionTemplateDO::getTemplateName, templateQueryReqVO.getTemplateName());
        lambdaQueryWrapper.orderByDesc(StoreInspectionTemplateDO::getCreateTime);
        // ===================== 权限核心 =====================
        // (全部可见) OR (指定可见 + 用户在列表里)
        /*lambdaQueryWrapper.and(wrapper -> wrapper
                // 情况1：全部可见
                .eq(StoreInspectionTemplateDO::getVisibilityType, false)

                // 情况2：指定可见 + 模板ID在范围内（空时自动不拼接）
                .or(CollectionUtil.isNotEmpty(visibleTemplateIds),
                        subWrapper -> subWrapper
                                .eq(StoreInspectionTemplateDO::getVisibilityType, true)
                                .in(StoreInspectionTemplateDO::getTemplateId, visibleTemplateIds)
                )
        );*/

        PageResult<StoreInspectionTemplateDO> storeInspectionTemplateDOPageResult = templateMapper.selectPage(templateQueryReqVO, lambdaQueryWrapper);
        PageResult<TemplateRespVO> result = BeanUtils.toBean(storeInspectionTemplateDOPageResult, TemplateRespVO.class);

        List<TemplateRespVO> list = result.getList();


        // 批量填充点检项 → 【最优方案：一次性查询所有数据】
        if (CollectionUtil.isNotEmpty(list)) {
            // 1. 批量提取所有模板ID → 只查一次库
            List<Long> templateIds = list.stream()
                    .map(TemplateRespVO::getTemplateId)
                    .toList();

            // 2. 一次性查询所有模板的点检项（只执行1次SQL）
            List<TemplateChecklistDO> allCheckItems = templateChecklistMapper.selectList(
                    new LambdaQueryWrapper<TemplateChecklistDO>()
                            .in(TemplateChecklistDO::getTemplateId, templateIds)
            );

            // 3. 按 templateId 分组（内存处理）
            Map<Long, List<TemplateChecklistDO>> checkItemMap = allCheckItems.stream()
                    .collect(Collectors.groupingBy(TemplateChecklistDO::getTemplateId));

            // 4. 遍历赋值（内存操作，无DB）
            list.forEach(template -> {
                List<TemplateChecklistDO> checkList = checkItemMap.getOrDefault(template.getTemplateId(), Collections.emptyList());

                // 大类名称（去重）
                List<String> typeNames = checkList.stream()
                        .map(TemplateChecklistDO::getTypeName)
                        .distinct()
                        .toList();

                // 数量
                int count = checkList.size();

                // 总分
                int totalScore = checkList.stream()
                        .mapToInt(TemplateChecklistDO::getScore)
                        .sum();

                // 赋值
                template.setTemplateChecklistTypeNames(typeNames);
                template.setTemplateChecklistCount(count);
                template.setTemplateChecklistSumScore(totalScore);
            });
        }

        result.setList(list);

        return result;
    }

    @Override
    public Integer updateTemplate(TemplateReqVO templateReqVO) {

        validSaveOrUpdateTemplate(templateReqVO);

        StoreInspectionTemplateDO templateDO = BeanUtils.toBean(templateReqVO, StoreInspectionTemplateDO.class);
        int result = templateMapper.updateById(templateDO);

        Long templateId = templateReqVO.getTemplateId();
        // 删除指定成员
        deleteVisibleUser(templateId);

        // 删除点检项
        deleteTemplateChecklist(templateId);

        // 创建子表
        createSonTableForTemplate(templateReqVO,templateDO,templateId);
        return result;
    }

    @Override
    public Integer delTemplate(Long templateId) {
        int result = templateMapper.deleteById(templateId);
        deleteVisibleUser(templateId);
        deleteTemplateChecklist(templateId);
        return result;
    }

    private void deleteTemplateChecklist(Long templateId) {
        LambdaQueryWrapper<TemplateChecklistDO> checklistDelWrapper = new LambdaQueryWrapper<>();
        checklistDelWrapper.eq(TemplateChecklistDO::getTemplateId, templateId);
        templateChecklistMapper.delete(checklistDelWrapper);
    }

    private void deleteVisibleUser(Long templateId) {
        LambdaQueryWrapper<TemplateVisibleUserDO> visibleUserDelWrapper = new LambdaQueryWrapper<>();
        visibleUserDelWrapper.eq(TemplateVisibleUserDO::getTemplateId, templateId);
        templateVisibleUserMapper.delete(visibleUserDelWrapper);
    }
}