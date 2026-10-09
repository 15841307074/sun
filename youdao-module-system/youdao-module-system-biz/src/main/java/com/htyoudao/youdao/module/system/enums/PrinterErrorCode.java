package com.htyoudao.youdao.module.system.enums;

/**
 * 鑫烨打印机错误码枚举
 */
public enum PrinterErrorCode {

    SN_USER_NOT_MATCH(1001, "打印机编号和用户不匹配", "打印机尚未添加至云平台，需要登录云平台添加或使用添加接口添加方可使用"),
    PRINTER_NOT_REGISTER(1002, "打印机未注册", "打印机尚未添加至云平台，需要登录云平台添加或使用添加接口添加方可使用"),
    PRINTER_OFFLINE(1003, "打印机不在线", "检查打印机是否正常联网或打印机所处网络环境是否可以正常访问互联网"),
    ADD_ORDER_FAILED(1004, "添加订单失败", "检查订单内容中是否有存在非法使用的标签，如空标签"),
    ORDER_NOT_FOUND(1005, "未找到订单信息", "检查订单号是否是云端接口返回的订单号"),
    ORDER_DATE_INVALID(1006, "订单日期格式或大小不正确", ""),
    PRINT_CONTENT_MORE_THAN_12K_BYTES(1007, "打印内容不能超过12K", "打印超出12K需要自己结合业务使用做数据拆分。"),
    PRINTER_RECORD_LOCK_FAIL(1008, "用户修改打印机记录失败", ""),
    SN_OR_NAME_EMPTY(1009, "用户添加打印机时，打印机编号或名称不能为空", "设备编号和名称为必填项，不可省略"),
    PRINTER_NUMBER_INVALID(1010, "打印机设备编号无效", "当前打印机不是云打印机或打印机编号输入错误，需输入打印机底部或背部带(sn/pid)字样后面的一串英文数字字符组合的编号"),
    PRINTER_EXIST(1011, "打印机已存在，若当前开放平台无法查询到打印机信息，请联系售后技术支持人员核实", "打印机已添加至云平台，若需再次添加，需在云平台删除，或调用删除(delPrinters)接口删除"),
    PRINTER_EXCEPTION(1012, "添加打印设备失败，请稍后再试或联系售后技术支持人员", ""),
    ORDER_IDEMPOTENT(1013, "打印订单时触发幂等性", "根据幂等因子，排查是否出现重复提交订单打印内容且幂等因子要保证全局唯一"),
    ORDER_IDEMPOTENT_LIMITLEN(1014, "幂等因子过长", "幂等因子有效长度为50个字符，可参考调整幂等因子长度"),
    LOGO_FORMAT_ERROR(1016, "LOGO文件格式错误", "(1)请检查上传的logo文件需符合图片格式(如：png，jpg)\n(2)上传的图片内容需使用base64加密获得\n(3)上传的图片数据需去掉'data:image/png;base64,'或'data:image/jpeg;base64,'等前缀"),
    LOGO_SIZE_LARGE(1017, "LOGO文件超出规定范围", "logo文件大小不能超出30kb"),
    LOGO_UPLOAD_TIMES(1018, "LOGO上传次数超限制", "logo文件上传每天限制最多可操作3次，若出现此提示，等待次日再上传即可，开发者不需要做其他操作"),
    LOGO_DELETE_NO_EXIST(1020, "LOGO删除失败", "logo文件已删除或尚未上传，若确认已上传但还是提示该错误，需联系技术人员排查"),
    LABEL_MODE_ERROR(1021, "LOGO上传模式错误", "针对标签打印机logo文件上传需在标签打印模式下上传，针对小票模式操作无效，可以通过打印自检页方式查看当前标签机的打印模式"),
    CUSTOM_DEVICE_NO_PERMISSION(1022, "该设备属于定制设备，您当前无权使用", "该打印机指定了所属账号，不能添加，有疑问请联系客服");

    private final int code;
    private final String message;
    private final String solution;

    PrinterErrorCode(int code, String message, String solution) {
        this.code = code;
        this.message = message;
        this.solution = solution;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getSolution() {
        return solution;
    }

    /**
     * 根据错误码获取对应的枚举
     */
    public static PrinterErrorCode getByCode(int code) {
        for (PrinterErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 根据错误码获取对应的枚举，如果找不到返回默认值
     */
    public static PrinterErrorCode getByCode(int code, PrinterErrorCode defaultValue) {
        PrinterErrorCode result = getByCode(code);
        return result != null ? result : defaultValue;
    }

    /**
     * 检查错误码是否存在
     */
    public static boolean contains(int code) {
        return getByCode(code) != null;
    }

    /**
     * 获取完整的错误信息
     */
    public String getFullErrorMessage() {
        return String.format("错误码: %d, 错误信息: %s, 解决方案: %s", code, message, solution);
    }

    @Override
    public String toString() {
        return String.format("%d: %s", code, message);
    }
}