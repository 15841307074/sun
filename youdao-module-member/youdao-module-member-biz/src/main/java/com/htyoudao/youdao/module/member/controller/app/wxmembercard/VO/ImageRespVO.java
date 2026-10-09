package com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO;

import io.swagger.v3.oas.annotations.media.Schema;

public class ImageRespVO {

    //图片地址
    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    String url;
    //图片类型
    @Schema(description = "图片类型", requiredMode = Schema.RequiredMode.REQUIRED)
    String type;
    //是否跳转
    @Schema(description = "是否跳转", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer isJump;
    //跳转链接
    @Schema(description = "跳转链接", requiredMode = Schema.RequiredMode.REQUIRED)
    String jumpUrl;
    @Schema(description = "来自", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer presentationForm;

    public Integer getPresentationForm() {
        return presentationForm;
    }

    public void setPresentationForm(Integer presentationForm) {
        this.presentationForm = presentationForm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsJump() {
        return isJump;
    }

    public void setIsJump(Integer isJump) {
        this.isJump = isJump;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    @Override
    public String toString() {
        return "ImageDTO{" +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", isJump=" + isJump +
                ", jumpUrl='" + jumpUrl + '\'' +
                '}';
    }
}
