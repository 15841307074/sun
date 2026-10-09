package com.htyoudao.youdao.module.order.enums;

public interface OrderConstants {


    Integer YES = 1;

    Integer NO = 0;

    Integer IS_OLD = 0;

    Integer SUCCESS = 1;

    Integer FAIL = 0;

    Integer ZERO = 0;

    Integer ONE = 1;

    String ERROR = "ERROR";

    /**
     * 退款
     */
    public static final String E_O_LINK_REFUND = "/yyfsevr/order/refund";

    /**
     * 商户主扫(bToC)
     */
    public static final String B_TO_C_E_O_LINK_PAY = "/yyfsevr/order/scanByMerchant";

    /**
     * 统一下单接口(cToB)
     */
    public static final String C_TO_B_E_O_LINK_PAY = "/yyfsevr/order/pay";

    /**
     * 统下一单查询接口
     */
    public static final String C_TO_B_E_O_LINK_QUERY = "/yyfsevr/order/orderQuery";

    /**
     * 星驿付分账
     */
    public static final String XING_YI_SPLIT = "/yyfsevr/trans/order/account";

    /**
     * 星驿付订单分账撤销
     */
    public static final String XING_YI_SPLIT_REVOKE = "/yyfsevr/trans/order/accountRevoke";


    /**
     * 生成电子码
     */
    public static final String E_O_LINK_PAY_GET_CODE = "/yyfsevr/order/getCodeUrl";

    public static final String SPLICING_ORDER_CONFIG = "splicing.order.config";
}
