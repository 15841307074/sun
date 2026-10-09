package com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO;

public class ImageDTO {

    //图片地址
    String url;
    //附件类型：1视频，2图片
    Integer type;
    //是否跳转
    Integer isJump;
    //跳转链接
    String jumpUrl;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
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
