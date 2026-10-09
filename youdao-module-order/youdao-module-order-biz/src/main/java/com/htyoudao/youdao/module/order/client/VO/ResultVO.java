package com.htyoudao.youdao.module.order.client.VO;


import cn.hutool.http.HttpStatus;

import java.util.HashMap;
import java.util.Objects;

/**
 * 操作消息提醒
 */
public class ResultVO extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    public static final String CODE_TAG = "code";

    public static final String STATUS_TAG = "status";

    public static final String RESULT_TAG = "result";


    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象
     */
    public static final String DATA_TAG = "data";

    /**
     * 初始化一个新创建的 ResultVO 对象，使其表示一个空消息。
     */
    public ResultVO() {
    }

    /**
     * 初始化一个新创建的 ResultVO 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public ResultVO(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }


    public Object getData() {
        return get(DATA_TAG);
    }

    /**
     * 初始化一个新创建的 ResultVO 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public ResultVO(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        super.put(DATA_TAG, data);

    }

    public ResultVO(int code, String msg, int status, Object data) {
        super.put(STATUS_TAG, status);
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        super.put(RESULT_TAG, data);
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static ResultVO success() {
        return ResultVO.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static ResultVO success(Object data) {
        return ResultVO.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static ResultVO success(String msg) {
        return ResultVO.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static ResultVO success(String msg, Object data) {
        return new ResultVO(HttpStatus.HTTP_OK, msg, data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
//    public static ResultVO warn(String msg)
//    {
//        return ResultVO.warn(msg, null);
//    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static ResultVO warn(String msg, Object data) {
        return new ResultVO(HttpStatus.HTTP_USE_PROXY, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static ResultVO error() {
        return ResultVO.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 错误消息
     */
    public static ResultVO error(String msg) {
        return ResultVO.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static ResultVO error(String msg, Object data) {
        return new ResultVO(HttpStatus.HTTP_INTERNAL_ERROR, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 错误消息
     */
    public static ResultVO error(int code, String msg) {
        return new ResultVO(code, msg, null);
    }

    public static ResultVO error(int code, String msg, String data) {
        return new ResultVO(code, msg, data);
    }

    /**
     * 是否为成功消息
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return Objects.equals(HttpStatus.HTTP_OK, this.get(CODE_TAG));
    }

    /**
     * 是否为警告消息
     *
     * @return 结果
     */
//    public boolean isWarn()
//    {
//        return Objects.equals(HttpStatus.WARN, this.get(CODE_TAG));
//    }

    /**
     * 是否为错误消息
     *
     * @return 结果
     */
    public boolean isError() {
        return Objects.equals(HttpStatus.HTTP_INTERNAL_ERROR, this.get(CODE_TAG));
    }

    /**
     * 方便链式调用
     *
     * @param key   键
     * @param value 值
     * @return 数据对象
     */
    @Override
    public ResultVO put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static ResultVO toAjax(int rows) {
        return rows > 0 ? ResultVO.success() : ResultVO.error();
    }
}
