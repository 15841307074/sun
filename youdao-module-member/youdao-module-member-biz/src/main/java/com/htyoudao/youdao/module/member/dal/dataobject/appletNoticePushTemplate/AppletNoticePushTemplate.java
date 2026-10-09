package com.htyoudao.youdao.module.member.dal.dataobject.appletNoticePushTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author
 */
@Getter
@Setter
@TableName("applet_notice_push_template")
@Schema(name = "AppletNoticePushTemplate", description = "")
public class AppletNoticePushTemplate extends BusinessBaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
	@Schema(name = "id", description = "主键")
    private Long id;

    /**
     * 推送模板id
     */
	@Schema(name = "templateId", description = "推送模板id")
    private String templateId;

    @Schema(name = "templateType", description = "模板类型(1 取餐通知, )")
    private Integer templateType;


    /**
     * 推送模板编号
     */
	@Schema(name = "templateCode", description = "推送模板编号")
    private Integer templateCode;

    /**
     * 模板标题
     */
	@Schema(name = "templateTitle", description = "模板标题")
    private String templateTitle;

    /**
     * 详细内容
     */
	@Schema(name = "content", description = "详细内容")
    private String content;

    /**
     * 场景说明
     */
	@Schema(name = "sceneExplain", description = "场景说明")
    private String sceneExplain;

    /**
     * 推送url
     */
	@Schema(name = "appletUrl", description = "推送url")
    private String appletUrl;

    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
	@Schema(name = "miniProgramState", description = "跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版")
    private String miniProgramState;

    /**
     * 模板来源(0-页面录入,1-脚本写入)
     */
	@Schema(name = "source", description = "模板来源(0-页面录入,1-脚本写入)")
    private Byte source;
}
