package com.htyoudao.youdao.module.order.util;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.controller.print.VO.InformationVO;
import com.htyoudao.youdao.module.order.controller.print.VO.PrintCommodityConfigTagVO;
import com.htyoudao.youdao.module.order.controller.print.VO.PrintConfigVO;
import com.htyoudao.youdao.module.order.controller.print.VO.ProductInfoVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import com.htyoudao.youdao.module.order.enums.OrderFromEnum;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.order.enums.PrinterTomplateTypeEnum;
import com.htyoudao.youdao.module.order.enums.PrinterTypeEnum;
import com.htyoudao.youdao.module.order.util.feie.Order;
import com.htyoudao.youdao.module.order.util.feie.PrintUtil4;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 小票模板工具类
 */
public class MultiReceiptTemplate {
    public static final String FE_TEXT_TAG_START = "<B>";
    public static final String FE_TEXT_TAG_END = "</B>";
    public static final String FE_TEXT_NORMAL_TAG_START = "<L>";
    public static final String FE_TEXT_NORMAL_TAG_END = "</L>";
    public static final String FE_TEXT_SMALL_TAG_START = "";
    public static final String FE_TEXT_SMALL_TAG_END = "";

    public static final String XP_TEXT_BIG_TAG_START = "<L><B>";
    public static final String XP_TEXT_BIG_TAG_END = "</L></B>";
    public static final String XP_TEXT_SMALL_TAG_START = "<L><N>";
    public static final String XP_TEXT_SMALL_TAG_END = "</L></N>";
    public static final String XP_TEXT_NORMAL_TAG_START = "<L><HB>";
    public static final String XP_TEXT_NORMAL_TAG_END = "</L></HB>";
    public static final String BOLD_TAG_START = "<BOLD>";
    public static final String BOLD_TAG_END = "</BOLD>";


    private static final String itemInfosAddStart = " ${itemInfoAddSizeStart}${itemInfosAddBlodStart}";
    private static final String itemInfosAddEnd = "${itemInfoAddSizeStart}${itemInfosAddBlodStart}";

    private static final String RECEIPT_SEPARATOR = "--------------------------------\n";
    private static final String ITEM_INFO_HEADER = "名称             数量   金额 <BR>";
    private static final String ITEM_INFO_HEADER_K = "名称                   数量 <BR>";
    private static final String ADD_ITEM_HEADER = "------------加购商品------------\n";

    // 商家联模板
    public static String generateReceipt(String receipt, int templateType, int printerType, String pickupCode
            , String itemInfos, String addItemInfos,
                                         PrintConfigVO printConfigVO
            , BzOrderDO bzOrder, StoreDTO storeDTO, String activityGoods, String totalItem) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createTimeStr = formatter.format(bzOrder.getCreateTime());
        String nowTimeStr = formatter.format(LocalDateTime.now());
        PrinterTypeEnum printerTypeEnum = PrinterTypeEnum.fromStatus(templateType);
        // 替换通用信息
        receipt = replaceInfo(receipt, "${pickupCode}", pickupCode);
        receipt = replaceInfo(receipt, "${diningMethod}", OrderTypeEnum.getMsgByCode(bzOrder.getOrderType()));
        receipt = replaceInfo(receipt, "${payType}", OrderFromEnum.getMessageByCode(bzOrder.getOrderFrom()));
        receipt = replaceInfo(receipt, "${orderRemark}", bzOrder.getOrderRemark() != null ? bzOrder.getOrderRemark() : "");
        receipt = replaceInfo(receipt, "${itemInfos}", itemInfos);

        if (StringUtils.isNotEmpty(addItemInfos)) {
            receipt = replaceInfo(receipt, "${itemAddInfos}", ADD_ITEM_HEADER + addItemInfos);
        }
        if (StringUtils.isNotEmpty(activityGoods)) {
            receipt = replaceInfo(receipt, "${activityInfos}", activityGoods);
        }
        receipt = replaceInfo(receipt, "${totalItem}", totalItem);
        String pickupTime = StringUtils.isNotEmpty(bzOrder.getAppointmentTime()) ? bzOrder.getAppointmentTime() : bzOrder.getOrderType() == 2 ? "尽快送达" : "立即取餐";
        String customerPhone = bzOrder.getTakeAwayTel() != null ? bzOrder.getTakeAwayTel() : "";
        String deliveryPhone = bzOrder.getReceiverMobile() != null ? bzOrder.getReceiverMobile() : "";
        String total = bzOrder.getPayAmount().toString();
        String packagingFee = bzOrder.getPackingCharge() != null ? bzOrder.getPackingCharge().toString() : "0";
        String coupon = bzOrder.getActivityDiscountAmount() != null ? bzOrder.getActivityDiscountAmount().toString() : "0";
        String orderTime = createTimeStr;
        String printTime = nowTimeStr;
        String orderNumber = bzOrder.getOrderSn();
        String storePhone = storeDTO.getStorePhone() != null ? storeDTO.getStorePhone() : "";
        String storeName = storeDTO.getStoreName() != null ? storeDTO.getStoreName() : "";
        String storeAddress = storeDTO.getStoreAddress() != null ? storeDTO.getStoreAddress() : "";
        String deliveryTime = StringUtils.isNotEmpty(bzOrder.getAppointmentTime()) ? bzOrder.getAppointmentTime() : bzOrder.getOrderType() == 2 ? "尽快送达" : "立即取餐";
        ;
        String customerNickname = bzOrder.getReceiverName() != null ? bzOrder.getReceiverName() : "";
        String deliveryAddress = bzOrder.getReceiverAddress() != null ? bzOrder.getReceiverAddress() : "";
        String deliveryFee = bzOrder.getExpressFee() != null ? bzOrder.getExpressFee().toString() : "0";
        if (isErrandOrder(bzOrder.getOrderType()) && StringUtils.isEmpty(bzOrder.getAppointmentTime())) {
            pickupTime = "立即送达";
            deliveryTime = "立即送达";
        }
        ErrandFeeDisplay errandFeeDisplay = calculateErrandFeeDisplay(bzOrder);
        receipt = replaceInfo(receipt, "${errandRewardAmount}", errandFeeDisplay.getRewardAmount());
        receipt = replaceInfo(receipt, "${errandStoreSubsidyAmount}", errandFeeDisplay.getStoreSubsidyAmount());
        // 根据打印机类型替换信息
        switch (printerTypeEnum) {
            case STORE:
                receipt = replaceStoreInfo(bzOrder, receipt, printerType, pickupTime, customerPhone, packagingFee, coupon, total, orderTime, printTime, orderNumber, deliveryFee, printConfigVO, deliveryTime, customerNickname, deliveryAddress, deliveryPhone);
                break;
            case MEMBER:
                receipt = replaceMemberInfo(receipt, printerType, pickupTime, storePhone, storeName, storeAddress, packagingFee, coupon, total, orderTime, orderNumber, deliveryFee, printConfigVO);
                break;
            case KITCHEN:
                receipt = replaceKitchenInfo(receipt, printerType, pickupTime, printConfigVO);
                break;
            case DELIVERY:
                customerPhone = bzOrder.getReceiverMobile();
                receipt = replaceDeliveryInfo(receipt, printerType, storePhone, storeName, storeAddress, customerPhone, deliveryTime, customerNickname, deliveryAddress, packagingFee, coupon, total, orderTime, orderNumber, deliveryFee, printConfigVO, deliveryPhone);
                break;
        }
        return receipt;
    }

    // 替换参数
    private static String replaceInfo(String receipt, String tag, String value) {
        return receipt.replace(tag, value);
    }

    private static boolean isErrandOrder(Integer orderType) {
        return orderType != null && orderType == OrderTypeEnum.ERRAND.getCode();
    }

    private static boolean isDeliveryDisplayOrder(PrinterTomplateTypeEnum templateType, int orderType) {
        return templateType == PrinterTomplateTypeEnum.DELIVERY
                || (templateType == PrinterTomplateTypeEnum.STORE
                && (orderType == OrderTypeEnum.TAKEAWAY.getCode() || orderType == OrderTypeEnum.ERRAND.getCode()));
    }

    private static ErrandFeeDisplay calculateErrandFeeDisplay(BzOrderDO bzOrder) {
        BigDecimal rewardAmount = defaultAmount(bzOrder == null ? null : bzOrder.getErrandRewardAmount());
        BigDecimal subsidyAmount = defaultAmount(bzOrder == null ? null : bzOrder.getErrandStoreSubsidyAmount());
        String subsidyDisplay = subsidyAmount.compareTo(BigDecimal.ZERO) > 0 ? "-" + formatAmount(subsidyAmount) : " " + formatAmount(BigDecimal.ZERO);
        return new ErrandFeeDisplay(formatAmount(rewardAmount), subsidyDisplay);
    }

    private static BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static String formatAmount(BigDecimal amount) {
        return defaultAmount(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static class ErrandFeeDisplay {
        private final String rewardAmount;
        private final String storeSubsidyAmount;

        private ErrandFeeDisplay(String rewardAmount, String storeSubsidyAmount) {
            this.rewardAmount = rewardAmount;
            this.storeSubsidyAmount = storeSubsidyAmount;
        }

        private String getRewardAmount() {
            return rewardAmount;
        }

        private String getStoreSubsidyAmount() {
            return storeSubsidyAmount;
        }
    }

    private static String replaceStoreInfo(BzOrderDO bzOrderDO, String receipt, int printerType, String pickupTime, String customerPhone, String packagingFee, String coupon, String total, String orderTime, String printTime, String orderNumber, String deliveryFee, PrintConfigVO printConfigVO, String deliveryTime, String customerNickname, String deliveryAddress, String deliveryPhone) {
        receipt = replaceInfo(receipt, "${printTime}", printTime);
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        if (bzOrderDO.getOrderType() == OrderTypeEnum.TAKEAWAY.getCode() || isErrandOrder(bzOrderDO.getOrderType())) {
            receipt = replaceInfo(receipt, "${customerPhone}", StringUtils.isNotEmpty(deliveryPhone) ? deliveryPhone : customerPhone);
        } else {

            receipt = replaceInfo(receipt, "${customerPhone}", customerPhone);
        }
        receipt = getString(receipt, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfo(receipt, "${deliveryFee}", deliveryFee);
        receipt = replaceInfo(receipt, "${deliveryTime}", deliveryTime);
        receipt = replaceInfo(receipt, "${customerNickname}", customerNickname);
        receipt = replaceInfo(receipt, "${deliveryAddress}", deliveryAddress);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getProductInformation(), "${itemInfosSizeStart}", "${itemInfosSizeEnd}", "${itemInfosBlodStart}", "${itemInfosBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getProductDetailedInformation(), "${itemInfoDetailSizeStart}", "${itemInfoDetailSizeEnd}", "${itemInfosDetailBlodStart}", "${itemInfosDetailBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getAdditionalPurchaseInformation(), "${itemInfoAddSizeStart}", "${itemInfoAddSizeEnd}", "${itemInfosAddBlodStart}", "${itemInfosAddBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${deliveryTimeSizeStart}", "${deliveryTimeSizeEnd}", "${deliveryTimeBoldStart}", "${deliveryTimeBoldEnd}");

        return receipt;
    }

    private static String getString(String receipt, String packagingFee, String coupon, String total, String orderTime, String orderNumber) {
        receipt = replaceInfo(receipt, "${packagingFee}", packagingFee);
        receipt = replaceInfo(receipt, "${coupon}", coupon);
        receipt = replaceInfo(receipt, "${total}", total);
        receipt = replaceInfo(receipt, "${orderTime}", orderTime);
        receipt = replaceInfo(receipt, "${orderNumber}", orderNumber);
        return receipt;
    }

    private static String replaceMemberInfo(String receipt, int printerType, String pickupTime, String storePhone, String storeName, String storeAddress, String packagingFee, String coupon, String total, String orderTime, String orderNumber, String deliveryFee, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${storePhone}", storePhone);
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        receipt = replaceInfo(receipt, "${storeName}", storeName);
        receipt = replaceInfo(receipt, "${storeAddress}", storeAddress);
        receipt = getString(receipt, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfo(receipt, "${deliveryFee}", deliveryFee);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getProductInformation(), "${itemInfosSizeStart}", "${itemInfosSizeEnd}", "${itemInfosBlodStart}", "${itemInfosBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getProductDetailedInformation(), "${itemInfoDetailSizeStart}", "${itemInfoDetailSizeEnd}", "${itemInfosDetailBlodStart}", "${itemInfosDetailBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getAdditionalPurchaseInformation(), "${itemInfoAddSizeStart}", "${itemInfoAddSizeEnd}", "${itemInfosAddBlodStart}", "${itemInfosAddBlodEnd}");
        return receipt;
    }

    private static String replaceKitchenInfo(String receipt, int printerType, String pickupTime, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getProductInformation(), "${itemInfosSizeStart}", "${itemInfosSizeEnd}", "${itemInfosBlodStart}", "${itemInfosBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getProductDetailedInformation(), "${itemInfoDetailSizeStart}", "${itemInfoDetailSizeEnd}", "${itemInfosDetailBlodStart}", "${itemInfosDetailBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getAdditionalPurchaseInformation(), "${itemInfoAddSizeStart}", "${itemInfoAddSizeEnd}", "${itemInfosAddBlodStart}", "${itemInfosAddBlodEnd}");

        return receipt;
    }

    private static String replaceDeliveryInfo(String receipt, int printerType, String storePhone, String storeName, String storeAddress, String customerPhone, String deliveryTime, String customerNickname, String deliveryAddress, String packagingFee, String coupon, String total, String orderTime, String orderNumber, String deliveryFee, PrintConfigVO printConfigVO, String deliveryPhone) {
        receipt = replaceInfo(receipt, "${storePhone}", storePhone);
        receipt = replaceInfo(receipt, "${storeName}", storeName);
        receipt = replaceInfo(receipt, "${storeAddress}", storeAddress);
        receipt = replaceInfo(receipt, "${customerPhone}", deliveryPhone);
        receipt = replaceInfo(receipt, "${deliveryTime}", deliveryTime);
        receipt = replaceInfo(receipt, "${customerNickname}", customerNickname);
        receipt = replaceInfo(receipt, "${deliveryAddress}", deliveryAddress);
        receipt = getString(receipt, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfo(receipt, "${deliveryFee}", deliveryFee);
        receipt = getString(printerType, printConfigVO, receipt);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getProductInformation(), "${itemInfosSizeStart}", "${itemInfosSizeEnd}", "${itemInfosBlodStart}", "${itemInfosBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getProductDetailedInformation(), "${itemInfoDetailSizeStart}", "${itemInfoDetailSizeEnd}", "${itemInfosDetailBlodStart}", "${itemInfosDetailBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getAdditionalPurchaseInformation(), "${itemInfoAddSizeStart}", "${itemInfoAddSizeEnd}", "${itemInfosAddBlodStart}", "${itemInfosAddBlodEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getDeliveryInformation(), "${deliveryTimeSizeStart}", "${deliveryTimeSizeEnd}", "${deliveryTimeBoldStart}", "${deliveryTimeBoldEnd}");


        return receipt;
    }

    private static String replaceInfoTags(String receipt, int printerType, InformationVO infoVO, String sizeStartTag, String sizeEndTag, String boldStartTag, String boldEndTag) {
        receipt = replaceInfo(receipt, sizeStartTag, getTextSizeStart(infoVO.getTextSize(), printerType));
        receipt = replaceInfo(receipt, sizeEndTag, getTextSizeEnd(infoVO.getTextSize(), printerType));
        receipt = replaceInfo(receipt, boldStartTag, getSortingMethodStrat(infoVO.getFontBold()));
        receipt = replaceInfo(receipt, boldEndTag, getSortingMethodEnd(infoVO.getFontBold()));
        return receipt;
    }

    // 拼接模板
    public static String getTemplate(int templateType, Boolean isXP, Boolean isTakeTel, Boolean isRemark, Boolean isAddItme, Boolean isBig, Boolean isActivity, int orderType, BzOrderDO bzOrder, Boolean isQrCode) {
        StringBuilder builder = new StringBuilder();
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        boolean deliveryDisplayOrder = isDeliveryDisplayOrder(printerTomplateTypeEnum, orderType);
        switch (printerTomplateTypeEnum) {
            case STORE:
                builder.append("商家联\n");
                break;
            case MEMBER:
                builder.append("顾客联\n");
                break;
            case KITCHEN:
                builder.append("后厨联\n");
                break;
            case DELIVERY:
                builder.append("配送联\n");
                break;
        }
        if (isXP) {
            builder.append(RECEIPT_SEPARATOR);
            if (deliveryDisplayOrder) {
                builder.append("\n     <CB>配送码:");
            } else {
                builder.append("\n     <CB>取餐码:");
            }
            builder.append("${pickupCode}")
                    .append("</CB>\n");
            if ( StringUtils.isNotEmpty(bzOrder.getAppointmentTime())) {
                builder.append(isErrandOrder(orderType) ? "<CB>预约单\n 【" : "   <CB>预约单【");
            }else{
                builder.append(isErrandOrder(orderType) ? "    <CB>【" : "        <CB>【");
            }
            builder.append("${diningMethod}")
                    .append("】</CB>\n")
                    .append("      <CB>")
                    .append("${payType}")
                    .append("</CB>\n")
                    .append(RECEIPT_SEPARATOR);
        } else {
            builder.append(RECEIPT_SEPARATOR);
            if (deliveryDisplayOrder) {
                builder.append("<CB>配送码:${pickupCode}\n");
            } else {
                builder.append("<CB>取餐码:${pickupCode}\n");
            }
            if ( StringUtils.isNotEmpty(bzOrder.getAppointmentTime())) {
                builder.append(isErrandOrder(orderType) ? "预约单\n【${diningMethod}】\n" : "预约单 【${diningMethod}】\n");
            }else{
                builder.append("【${diningMethod}】\n");
            }
            builder.append("${payType}</CB>\n")
                    .append(RECEIPT_SEPARATOR);
        }
        if (deliveryDisplayOrder) {
            builder.append("${deliveryTimeSizeStart}${deliveryTimeBoldStart}送达时间:${deliveryTime}${deliveryTimeSizeEnd}${deliveryTimeBoldEnd}\n")
                    .append("${customerSizeStart}${customerBoldStart}")
                    .append("顾客昵称:${customerNickname}\n")
                    .append("顾客电话:${customerPhone}\n")
                    .append("送货地址:${deliveryAddress}\n")
                    .append("${customerSizeEnd}${customerBoldEnd}");
        } else {
            builder.append("${pickupTimeSizeStart}${pickupTimeBoldStart}")
                    .append("取餐时间:${pickupTime}\n")
                    .append("${pickupTimeSizeEnd}${pickupTimeBoldEnd}");
        }

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE && isTakeTel && orderType != OrderTypeEnum.TAKEAWAY.getCode() && !isErrandOrder(orderType)) {
            builder.append("${customerSizeStart}${customerBoldStart}")
                    .append("顾客电话:${customerPhone}\n")
                    .append("${customerSizeEnd}${customerBoldEnd}");
        }
        if (isRemark) {
            builder.append("${remarkSizeStart}${remarkBoldStart}")
                    .append("订单备注:${orderRemark}\n")
                    .append("${remarkSizeEnd}${remarkBoldEnd}");
        }
        builder.append(RECEIPT_SEPARATOR);
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(ITEM_INFO_HEADER_K);
        } else {
            builder.append(ITEM_INFO_HEADER);
        }

        builder.append(RECEIPT_SEPARATOR)
                .append("${itemInfos}\n");
        if (isAddItme) {
            builder.append("${itemAddInfos}\n");
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(RECEIPT_SEPARATOR);
            builder.append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("${totalItem}").append("${itemTotalSizeEnd}${itemTotalBoldEnd}");
            builder.append(RECEIPT_SEPARATOR);
            if (isActivity) {
                builder.append("${activityInfos}");
            }
            if (isBig) {
                if (bzOrder.getPackingCharge() != null && bzOrder.getPackingCharge().compareTo(BigDecimal.ZERO) > 0) {
                    builder.append("${otherSizeStart}${otherBoldStart}").append("打包费\t  ${packagingFee}\n").append("${otherSizeEnd}${otherBoldEnd}");
                }
                if (bzOrder.getActivityDiscountAmount() != null && bzOrder.getActivityDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    builder.append("${otherSizeStart}${otherBoldStart}").append("优惠券\t  ${coupon}\n");
                    builder.append("${otherSizeEnd}${otherBoldEnd}");
                }
            } else {
                if (bzOrder.getPackingCharge() != null && bzOrder.getPackingCharge().compareTo(BigDecimal.ZERO) > 0) {
                    builder.append("${otherSizeStart}${otherBoldStart}").append("打包费\t                 ${packagingFee}\n");
                    builder.append("${otherSizeEnd}${otherBoldEnd}");
                }
                if (bzOrder.getActivityDiscountAmount() != null && bzOrder.getActivityDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    builder.append("${otherSizeStart}${otherBoldStart}").append("优惠券\t                 ${coupon}\n");
                    builder.append("${otherSizeEnd}${otherBoldEnd}");
                }
            }

        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN && isErrandOrder(orderType)) {
            builder.append("${otherSizeStart}${otherBoldStart}");
            if (isBig) {
                builder.append("赏金\t ${errandRewardAmount}\n");
                builder.append("配送补贴\t ${errandStoreSubsidyAmount}\n");
            } else {
                builder.append("赏金\t               ${errandRewardAmount}\n");
                builder.append("配送补贴\t      ${errandStoreSubsidyAmount}\n");
            }
            builder.append("${otherSizeEnd}${otherBoldEnd}");
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN && !isErrandOrder(orderType) && (printerTomplateTypeEnum == PrinterTomplateTypeEnum.DELIVERY || orderType == OrderTypeEnum.TAKEAWAY.getCode())) {
            if (bzOrder.getExpressFee() != null && bzOrder.getExpressFee().compareTo(BigDecimal.ZERO) > 0) {
                builder
                        .append("${otherSizeStart}${otherBoldStart}");
                if (isBig) {
                    builder.append(" 配送费\t  ${deliveryFee}\n");
                } else {
                    builder.append(" 配送费\t                 ${deliveryFee}\n");
                }
                builder.append("${otherSizeEnd}${otherBoldEnd}");
            }
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder
                    .append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("                     合计：${total}\n")
                    .append("${itemTotalSizeEnd}${itemTotalBoldEnd}")
                    .append(RECEIPT_SEPARATOR);
        }

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE) {
            builder
                    .append("${orderSizeStart}${orderBoldStart}")
                    .append("下单时间:${orderTime}\n")
                    .append("打印时间:${printTime}\n")
                    .append("订单号:${orderNumber}\n")
                    .append("${orderSizeEnd}${orderBoldEnd}")
            ;
        }

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.MEMBER || printerTomplateTypeEnum == PrinterTomplateTypeEnum.DELIVERY) {
            builder
                    .append("${orderSizeStart}${orderBoldStart}")
                    .append("订单号:${orderNumber}\n")
                    .append("下单时间:${orderTime}\n")
                    .append("门店电话:${storePhone}\n")
                    .append("门店名称:${storeName}\n")
                    .append("门店地址:${storeAddress}\n")
                    .append("${orderSizeEnd}${orderBoldEnd}")
            ;
        }
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE && orderType != OrderTypeEnum.TAKEAWAY.getCode() && isQrCode) {
            if (isXP) {
                builder.append("<QRCODE s=10 e=L l=center>${orderNumber}</QRCODE><BR>");
            } else {
                builder.append("<QR>${orderNumber}</QR>");
            }
        }
        return builder.toString();
    }

    private static final Map<Integer, Map<Integer, String>> TEXT_SIZE_START_MAP = new HashMap<>();

    static {
        Map<Integer, String> feMap = new HashMap<>();
        feMap.put(0, FE_TEXT_TAG_START);
        feMap.put(1, FE_TEXT_NORMAL_TAG_START);
        feMap.put(2, FE_TEXT_SMALL_TAG_START);
        TEXT_SIZE_START_MAP.put(1, feMap);

        Map<Integer, String> xpMap = new HashMap<>();
        xpMap.put(0, XP_TEXT_BIG_TAG_START);
        xpMap.put(1, XP_TEXT_NORMAL_TAG_START);
        xpMap.put(2, XP_TEXT_SMALL_TAG_START);
        TEXT_SIZE_START_MAP.put(2, xpMap);
    }

    private static String getTextSizeStart(int textSize, int printerType) {
        return TEXT_SIZE_START_MAP.getOrDefault(printerType, new HashMap<>()).getOrDefault(textSize, "");
    }

    private static String getSortingMethodStrat(Boolean sortingMethod) {
        return sortingMethod ? BOLD_TAG_START : "";
    }

    private static final Map<Integer, Map<Integer, String>> TEXT_SIZE_END_MAP = new HashMap<>();

    static {
        Map<Integer, String> feMap = new HashMap<>();
        feMap.put(0, FE_TEXT_TAG_END);
        feMap.put(1, FE_TEXT_NORMAL_TAG_END);
        feMap.put(2, FE_TEXT_SMALL_TAG_END);
        TEXT_SIZE_END_MAP.put(1, feMap);

        Map<Integer, String> xpMap = new HashMap<>();
        xpMap.put(0, XP_TEXT_BIG_TAG_END);
        xpMap.put(1, XP_TEXT_NORMAL_TAG_END);
        xpMap.put(2, XP_TEXT_SMALL_TAG_END);
        TEXT_SIZE_END_MAP.put(2, xpMap);
    }

    private static String getTextSizeEnd(int textSize, int printerType) {
        return TEXT_SIZE_END_MAP.getOrDefault(printerType, new HashMap<>()).getOrDefault(textSize, "");
    }

    private static String getSortingMethodEnd(Boolean sortingMethod) {
        return sortingMethod ? BOLD_TAG_END : "";
    }

    /**
     * 打印机列表获取对应模板信息
     */
    public static String getTemplateYu(Integer templateType, int printerType, PrintConfigVO printConfigVO
            , BzOrderDO bzOrder, List<BzOrderProductVO> orderProductList, String pickUpNum, List<BzOrderPurchaseDO> bzOrderPurchases, StoreDTO storeDTO, List<BzOrderProductVO> activityGoods, int orderType) {
        //获取模板
        Boolean isXp = false;
        if (printerType == 2) {
            isXp = true;
        }
        int sortItme = 0;
        int sortAddItme = 0;
        Boolean isBig = false;
        Boolean isActivity = false;
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        switch (printerTomplateTypeEnum) {
            case STORE:
                sortItme = printConfigVO.getStore().getProductInformation().getSortingMethod();
                sortAddItme = printConfigVO.getStore().getAdditionalPurchaseInformation().getSortingMethod();
                if (printConfigVO.getStore().getOtherInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case MEMBER:
                sortItme = printConfigVO.getMember().getProductInformation().getSortingMethod();
                sortAddItme = printConfigVO.getMember().getAdditionalPurchaseInformation().getSortingMethod();
                if (printConfigVO.getMember().getOtherInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case KITCHEN:
                sortItme = printConfigVO.getKitchen().getProductInformation().getSortingMethod();
                sortAddItme = printConfigVO.getKitchen().getAdditionalPurchaseInformation().getSortingMethod();
                break;
            case DELIVERY:
                sortItme = printConfigVO.getDelivery().getProductInformation().getSortingMethod();
                sortAddItme = printConfigVO.getDelivery().getAdditionalPurchaseInformation().getSortingMethod();
                if (printConfigVO.getDelivery().getOtherInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
        }
        String receipt = getTemplate(templateType, isXp, StringUtils.isNotEmpty(bzOrder.getTakeAwayTel()), StringUtils.isNotEmpty(bzOrder.getOrderRemark()), CollectionUtils.isNotEmpty(bzOrderPurchases), isBig, CollectionUtils.isNotEmpty(activityGoods), orderType, bzOrder, printConfigVO.getStore().isMerchantQrCode());
        //商品排序

        //拼接商品
        if (sortItme == 1) {
            orderProductList.sort(Comparator.comparing(BzOrderProductVO::getGoodsShowPrice).reversed());
        } else if (sortItme == 2) {
            orderProductList.sort(Comparator.comparing(BzOrderProductVO::getGoodsShowPrice));
        }
        String itme = buildProductListFe(templateType, orderProductList, templateType, printConfigVO);
        //拼接加购
        String itmeAdd = "";
        if (sortAddItme == 1) {
            bzOrderPurchases.sort(Comparator.comparing(BzOrderPurchaseDO::getPurchasePrice).reversed());
        } else if (sortAddItme == 2) {
            bzOrderPurchases.sort(Comparator.comparing(BzOrderPurchaseDO::getPurchasePrice));
        }
        if (CollectionUtils.isNotEmpty(bzOrderPurchases)) {
            itmeAdd = buildAddProductListFe(templateType, bzOrderPurchases, templateType, printConfigVO);
            itmeAdd = itmeAdd
                    .replace("${itemInfosSizeStart}", "${itemInfoAddSizeStart}")
                    .replace("${itemInfosBlodStart}", "${itemInfosAddBlodStart}")
                    .replace("${itemInfosSizeEnd}", "${itemInfoAddSizeEnd}")
                    .replace("${itemInfosBlodEnd}", "${itemInfosAddBlodEnd}");
        }
        //拼接优惠活动
        String itmeActivity = "";
        itmeActivity = buildActivityListFe(templateType, activityGoods, templateType, printConfigVO, bzOrder);
        //商品合计
        List<BzOrderProductVO> totalGoods = new ArrayList<>();
        BzOrderProductVO totalGoodsVO = new BzOrderProductVO();
        totalGoodsVO.setGoodsName("商品合计");
        totalGoodsVO.setGoodsShowPrice(bzOrder.getGoodsAmount());
        totalGoodsVO.setGoodsNum(orderProductList.stream().mapToInt(BzOrderProductVO::getGoodsNum).sum() + bzOrderPurchases.size());
        totalGoods.add(totalGoodsVO);
        String totalItem = buildProductListFe(templateType, totalGoods, templateType, printConfigVO);
        //商品标签
        receipt = generateReceipt(receipt, templateType, printerType, pickUpNum
                , itme, itmeAdd,
                printConfigVO
                , bzOrder, storeDTO, itmeActivity, totalItem);
        return receipt;
    }

    private static String getStringDC(int templateType, int printerType, PrintConfigVO printConfigVO, String receipt) {
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        switch (printerTomplateTypeEnum) {
            case DELIVERY:
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getDeliveryInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
                break;
            case STORE:
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getDeliveryInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                break;
            case MEMBER:
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
                break;
            case KITCHEN:
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
                break;
        }

        return receipt;
    }

    private static String getString(int printerType, PrintConfigVO printConfigVO, String receipt) {
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getDeliveryInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
        return receipt;
    }

    //拼接加购
    private static String generatePurchaseListFe(List<BzOrderPurchaseDO> bzOrderPurchases, Integer locationTYpe, int templateType, PrintConfigVO printConfigVO) {
        // 构建 b 字符串
        StringBuilder bBuilder = new StringBuilder();
        Boolean isBig = false;
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        switch (printerTomplateTypeEnum) {
            case STORE:
                if (printConfigVO.getStore().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case MEMBER:
                if (printConfigVO.getMember().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case KITCHEN:
                if (printConfigVO.getKitchen().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case DELIVERY:
                if (printConfigVO.getDelivery().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
        }
        if (!bzOrderPurchases.isEmpty()) {
            for (BzOrderPurchaseDO item : bzOrderPurchases) {
                if (locationTYpe != 2) {
                    if (!isBig) {
                        bBuilder.append(itemInfosAddStart + item.getPurchaseName()).append("          1     ");
                        bBuilder.append(item.getPurchasePrice());
                    } else {
                        bBuilder.append(itemInfosAddStart + item.getPurchaseName()).append("*1   ");
                        bBuilder.append(item.getPurchasePrice());
                    }

                } else {
                    if (!isBig) {
                        bBuilder.append(itemInfosAddStart + item.getPurchaseName()).append("                1     ");

                    } else {
                        bBuilder.append(itemInfosAddStart + item.getPurchaseName()).append("<BR>               1     ");
                    }

                }
                bBuilder.append("<BR>" + itemInfosAddEnd);
            }
        }
        return bBuilder.toString();
    }

    //拼接加购商品  新晔
    private static String generatePurchaseList(List<BzOrderPurchaseDO> bzOrderPurchases, Integer locationTYpe, int templateType, PrintConfigVO printConfigVO) {
        String b = null;
        Boolean isBig = false;
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        switch (printerTomplateTypeEnum) {
            case STORE:
                if (printConfigVO.getStore().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case MEMBER:
                if (printConfigVO.getMember().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case KITCHEN:
                if (printConfigVO.getKitchen().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
            case DELIVERY:
                if (printConfigVO.getDelivery().getAdditionalPurchaseInformation().getTextSize() == 0) {
                    isBig = true;
                }
                break;
        }
        if (!bzOrderPurchases.isEmpty()) {
            if (locationTYpe != 2) {
                if (!isBig) {
                    b = bzOrderPurchases.stream()
                            .map(item -> itemInfosAddStart + item.getPurchaseName() + "                1       <BR>" + itemInfosAddEnd)
                            .collect(Collectors.joining(""));
                } else {
                    b = bzOrderPurchases.stream()
                            .map(item -> itemInfosAddStart + item.getPurchaseName() + "<BR>" + "              1 " + item.getPurchasePrice() + "<BR>" + itemInfosAddEnd)
                            .collect(Collectors.joining(""));
                }

            } else {
                if (!isBig) {
                    b = bzOrderPurchases.stream()
                            .map(item -> itemInfosAddStart + item.getPurchaseName() + "                1       <BR>" + itemInfosAddEnd)
                            .collect(Collectors.joining(""));
                } else {
                    b = bzOrderPurchases.stream()
                            .map(item -> itemInfosAddStart + item.getPurchaseName() + "<BR>" + "             1  <BR>" + itemInfosAddEnd)
                            .collect(Collectors.joining(""));
                }
            }
        }
        return b;
    }

    /**
     * 点餐机 打印机列表获取对应商品信息标签
     */
    public static PrintCommodityConfigTagVO getTemplateCommodityTagDC(Integer templateType,
                                                                      int printerType, PrintConfigVO printConfigVO) {
        PrintCommodityConfigTagVO configVO = new PrintCommodityConfigTagVO();
        PrinterTomplateTypeEnum typeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        if (printConfigVO == null) {
            return configVO;
        }
// 统一提取三类配置
        ProductInfoVO infoVO = getProductInfoWithDefault(printConfigVO, typeEnum, "product");
        ProductInfoVO infoDetailVO = getProductInfoWithDefault(printConfigVO, typeEnum, "detail");
        ProductInfoVO infoAddVO = getProductInfoWithDefault(printConfigVO, typeEnum, "add");


        // 设置商品主信息
        setConfig(configVO::setProductSizeTagStart, configVO::setProductSizeTagEnd,
                configVO::setProductBoldTagStart, configVO::setProductBoldTagEnd,
                configVO::setProductSort, infoVO, printerType);

        // 设置商品详情信息
        setConfig(configVO::setProductDetailSizeTagStart, configVO::setProductDetailSizeTagEnd,
                configVO::setProductDetailBoldTagStart, configVO::setProductDetailBoldTagEnd,
                null, infoDetailVO, printerType);

        // 设置附加信息（如规格、属性等）
        setConfig(configVO::setProductAddSizeTagStart, configVO::setProductAddSizeTagEnd,
                configVO::setProductAddBoldTagStart, configVO::setProductAddBoldTagEnd,
                configVO::setProductAddSort, infoAddVO, printerType);
        return configVO;
    }

    @FunctionalInterface
    private interface ProductInfoExtractor {
        ProductInfoVO get(PrintConfigVO configVO);
    }

    private static final Map<PrinterTomplateTypeEnum, Map<String, ProductInfoExtractor>> EXTRACTORS = new EnumMap<>(PrinterTomplateTypeEnum.class);

    static {
        // 商家联模板字段提取器
        Map<String, ProductInfoExtractor> storeExtractors = new HashMap<>();
        storeExtractors.put("product", config ->
                config.getStore() != null ? config.getStore().getProductInformation() : null);
        storeExtractors.put("detail", config ->
                config.getStore() != null ? config.getStore().getProductDetailedInformation() : null);
        storeExtractors.put("add", config ->
                config.getStore() != null ? config.getStore().getAdditionalPurchaseInformation() : null);

        // 顾客联
        Map<String, ProductInfoExtractor> memberExtractors = new HashMap<>();
        memberExtractors.put("product", config ->
                config.getMember() != null ? config.getMember().getProductInformation() : null);
        memberExtractors.put("detail", config ->
                config.getMember() != null ? config.getMember().getProductDetailedInformation() : null);
        memberExtractors.put("add", config ->
                config.getMember() != null ? config.getMember().getAdditionalPurchaseInformation() : null);

        // 后厨联
        Map<String, ProductInfoExtractor> kitchenExtractors = new HashMap<>();
        kitchenExtractors.put("product", config ->
                config.getKitchen() != null ? config.getKitchen().getProductInformation() : null);
        kitchenExtractors.put("detail", config ->
                config.getKitchen() != null ? config.getKitchen().getProductDetailedInformation() : null);
        kitchenExtractors.put("add", config ->
                config.getKitchen() != null ? config.getKitchen().getAdditionalPurchaseInformation() : null);

        // 配送联
        Map<String, ProductInfoExtractor> deliveryExtractors = new HashMap<>();
        deliveryExtractors.put("product", config ->
                config.getDelivery() != null ? config.getDelivery().getProductInformation() : null);
        deliveryExtractors.put("detail", config ->
                config.getDelivery() != null ? config.getDelivery().getProductDetailedInformation() : null);
        deliveryExtractors.put("add", config ->
                config.getDelivery() != null ? config.getDelivery().getAdditionalPurchaseInformation() : null);

        // 注册到全局 map
        EXTRACTORS.put(PrinterTomplateTypeEnum.STORE, storeExtractors);
        EXTRACTORS.put(PrinterTomplateTypeEnum.MEMBER, memberExtractors);
        EXTRACTORS.put(PrinterTomplateTypeEnum.KITCHEN, kitchenExtractors);
        EXTRACTORS.put(PrinterTomplateTypeEnum.DELIVERY, deliveryExtractors);
    }

    private static ProductInfoVO getProductInfo(PrintConfigVO configVO, PrinterTomplateTypeEnum type, String key) {
        return EXTRACTORS.getOrDefault(type, Collections.emptyMap())
                .getOrDefault(key, c -> new ProductInfoVO())
                .get(configVO);
    }

    private static ProductInfoVO getProductInfoWithDefault(PrintConfigVO configVO, PrinterTomplateTypeEnum
            type, String key) {
        return Optional.ofNullable(getProductInfo(configVO, type, key))
                .orElse(new ProductInfoVO());
    }

    // 统一设置方法保持不变
    private static void setConfig(Consumer<String> sizeStartSetter, Consumer<String> sizeEndSetter,
                                  Consumer<String> boldStartSetter, Consumer<String> boldEndSetter,
                                  Consumer<Integer> sortSetter,
                                  ProductInfoVO infoVO, int printerType) {
        if (infoVO == null) {
            return;
        }

        sizeStartSetter.accept(getTextSizeStart(infoVO.getTextSize(), printerType));
        sizeEndSetter.accept(getTextSizeEnd(infoVO.getTextSize(), printerType));
        boldStartSetter.accept(getSortingMethodStrat(infoVO.getFontBold()));
        boldEndSetter.accept(getSortingMethodEnd(infoVO.getFontBold()));
        if (sortSetter != null) {
            sortSetter.accept(infoVO.getSortingMethod());
        }
    }

    public static String buildProductListFe(Integer locationTYpe, List<BzOrderProductVO> orderProductList,
                                            int templateType, PrintConfigVO printConfigVO) {
        // 处理 bzOrderProductList
        // 构建 a 字符串
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        Boolean infoBig = false;
        Boolean infoDetailBig = false;
        switch (printerTomplateTypeEnum) {
            case STORE:
                if (printConfigVO.getStore().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getStore().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case MEMBER:
                if (printConfigVO.getMember().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getMember().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case KITCHEN:
                if (printConfigVO.getKitchen().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getKitchen().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case DELIVERY:
                if (printConfigVO.getDelivery().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getDelivery().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
        }
        List<Order> orderList = new ArrayList<>();
        for (BzOrderProductVO item : orderProductList) {
            if (locationTYpe != 2) {
                Order order = new Order(item.getGoodsName().trim(), item.getGoodsNum().toString(), item.getGoodsShowPrice().toString(), infoBig);
                orderList.add(order);
                if (StringUtils.isNotBlank(item.getSpecValues())) {
                    Order orderSpecValues = new Order(((item.getIsSingle() == 1) ? "[规格]" : "-") + item.getSpecValues(), " ", " ", infoDetailBig);
                    orderList.add(orderSpecValues);
                }
                if (StringUtils.isNotBlank(item.getFlavorValue())) {
                    if (item.getFlavorValue().contains(",")) {
                        String[] splitName = item.getFlavorName().split(",");
                        String[] split = item.getFlavorValue().split(",");
                        for (int i = 0; i < split.length; i++) {
                            {
                                Order orderFlavor = new Order(((item.getIsSingle() == 1) ? "[属性]" : "-") + splitName[i] + ":" + split[i], " ", " ", infoDetailBig);
                                orderList.add(orderFlavor);
                            }
                        }
                    } else {
                        Order orderFlavor = new Order(((item.getIsSingle() == 1) ? "[属性]" : "-") + item.getFlavorName() + ":" + item.getFlavorValue(), " ", " ", infoDetailBig);
                        orderList.add(orderFlavor);
                    }
                }
                List<String> describeList = item.getDescribeList();
                if (describeList != null && !describeList.isEmpty()) {
                    List<String> downGoodsName = new ArrayList<>(describeList);
                    for (String goodsName : downGoodsName) {
                        String price = " ";
                        String num = " ";
                        if (goodsName.contains("##")) {
                            String[] split = goodsName.split("##");
                            goodsName = ((item.getIsSingle() == 1) ? "[加料]" : "-") + split[0] + "*" + split[1];
                        }
                        Order downOrder = new Order(goodsName.trim(), num, price, infoDetailBig);
                        orderList.add(downOrder);
                    }
                }
            } else {
                Order order = new Order(item.getGoodsName(), item.getGoodsNum().toString(), "", infoBig);
                orderList.add(order);
                if (StringUtils.isNotBlank(item.getSpecValues())) {
                    Order orderSpecValues = new Order(((item.getIsSingle() == 1) ? "[规格]" : "-规格：") + item.getSpecValues(), " ", " ", infoDetailBig);
                    orderList.add(orderSpecValues);
                }
                if (StringUtils.isNotBlank(item.getFlavorValue())) {
                    if (item.getFlavorValue().contains(",")) {
                        String[] splitName = item.getFlavorName().split(",");
                        String[] split = item.getFlavorValue().split(",");
                        for (int i = 0; i < split.length; i++) {
                            {
                                Order orderFlavor = new Order(((item.getIsSingle() == 1) ? "[属性]" : "-") + splitName[i] + ":" + split[i], " ", " ", infoDetailBig);
                                orderList.add(orderFlavor);
                            }
                        }
                    } else {
                        Order orderFlavor = new Order(((item.getIsSingle() == 1) ? "[属性]" : "-") + item.getFlavorName() + ":" + item.getFlavorValue(), " ", " ", infoDetailBig);
                        orderList.add(orderFlavor);
                    }
                }
                List<String> describeList = item.getDescribeList();
                if (describeList != null && !describeList.isEmpty()) {
                    List<String> downGoodsName = new ArrayList<>(describeList);
                    for (String goodsName : downGoodsName) {
                        String price = " ";
                        String num = " ";
                        if (goodsName.contains("##")) {
                            String[] split = goodsName.split("##");
                            goodsName = ((item.getIsSingle() == 1) ? "[加料]" : "-") + split[0] + "*" + split[1];
                            num = "";
                        }
                        Order downOrder = new Order(goodsName, num, price, infoDetailBig);
                        orderList.add(downOrder);
                    }
                }
            }

        }
        return PrintUtil4.getOrder(orderList, 16, 2, 3, 6);
    }

    public static String buildAddProductListFe(Integer locationTYpe, List<BzOrderPurchaseDO> orderProductList,
                                               int templateType, PrintConfigVO printConfigVO) {
        // 处理 bzOrderProductList
        // 构建 a 字符串
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        Boolean infoBig = false;
        Boolean infoDetailBig = false;
        switch (printerTomplateTypeEnum) {
            case STORE:
                if (printConfigVO.getStore().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getStore().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case MEMBER:
                if (printConfigVO.getMember().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getMember().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case KITCHEN:
                if (printConfigVO.getKitchen().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getKitchen().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
            case DELIVERY:
                if (printConfigVO.getDelivery().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                if (printConfigVO.getDelivery().getProductDetailedInformation().getTextSize() == 0) {
                    infoDetailBig = true;
                }
                break;
        }
        List<Order> orderList = new ArrayList<>();
        for (BzOrderPurchaseDO item : orderProductList) {
            if (locationTYpe != 2) {
                Order order = new Order(item.getPurchaseName().trim(), "1", item.getPurchasePrice().toString(), infoBig);
                orderList.add(order);
            } else {
                Order order = new Order(item.getPurchaseName(), "1", "", infoBig);
                orderList.add(order);
            }

        }
        return PrintUtil4.getOrder(orderList, 16, 2, 3, 6);
    }

    public static String buildActivityListFe(Integer locationTYpe, List<BzOrderProductVO> orderProductList,
                                             int templateType, PrintConfigVO printConfigVO, BzOrderDO bzOrder) {
        // 处理 bzOrderProductList
        // 构建 a 字符串
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        Boolean infoBig = false;
        Boolean infoDetailBig = false;
        switch (printerTomplateTypeEnum) {
            case STORE:
                if (printConfigVO.getStore().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                break;
            case MEMBER:
                if (printConfigVO.getMember().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                break;
            case DELIVERY:
                if (printConfigVO.getDelivery().getProductInformation().getTextSize() == 0) {
                    infoBig = true;
                }
                break;
        }
        List<Order> orderList = new ArrayList<>();
        for (BzOrderProductVO item : orderProductList) {
            if (item.getActivityType() != null) {
                if (item.getActivityType() == 1||item.getActivityType() == 5||item.getActivityType() == 11) {
                    Order order = new Order(item.getGoodsName().trim(), item.getGoodsNum() != null ? item.getGoodsNum().toString() : "", "-" + item.getGoodsShowPrice().toString(), infoBig);
                    orderList.add(order);
                }else {
                    Order order = new Order("秒杀活动", item.getGoodsNum() != null ? item.getGoodsNum().toString() : "", "    -" + bzOrder.getPromotionDiscountAmount().toString(), infoBig);
                    // 检查是否已经有秒杀活动订单
                    boolean hasSeckillOrder = orderList != null && orderList.stream()
                            .anyMatch(o -> "秒杀活动".equals(o.getTitle()));
                    // 是第一个秒杀活动订单，则添加
                    if (!hasSeckillOrder) {
                        orderList.add(order);
                    }
                }
            }
        }
        return PrintUtil4.getActivityOrder(orderList, 16, 2, 3, 6);
    }

    public static void main(String[] args) {
        String pickupCode = "A01";
        String diningMethod = "堂食";
        String pickupTime = "立即取餐";
        String payType = "微信小程序";
        String customerPhone = "17788889999";
        String orderRemark = "这是一段用户写的备注，字数应该不会很多，反正用户写了什么这块就展示什么就好了，知道吧？";
        List<String> itemInfos = new ArrayList<>();
        List<String> addItemInfos = new ArrayList<>();
        itemInfos.add("固定搭配套餐\t2份\t59.80");
        itemInfos.add("-xxx汉堡");
        itemInfos.add("-xxx小食");
        itemInfos.add("-饮品\n");
        itemInfos.add("分组可选套餐\t1份\t39.90");
        itemInfos.add("-汉堡\t3.00");
        itemInfos.add("-薯条*2");
        itemInfos.add("-饮品\n");
        itemInfos.add("这是一个单品\t1份\t9.90\n");
        itemInfos.add("这是一个单品名称如果名称很长的话就这么显示\t1份\t9.90\n");
        itemInfos.add("这是多属性单品\t1份\t39.90");
        itemInfos.add("[规格:大份, 500g]\t3.00");
        itemInfos.add("[属性]甘梅味");
        itemInfos.add("[加料:土豆片*3]\t9.00");
        itemInfos.add("[加料:牛肉饼*1]\t5.00\n");
        itemInfos.add("这是一个单品\t1份\t9.90\n");
        itemInfos.add("加购单品没有规格");
        itemInfos.add("没属性\t1份\t9.90");
        itemInfos.add("没加料\t1份\t9.90");

        String packagingFee = "1.00";
        String deliveryFee = "1.00";
        String coupon = "-10.00";
        String total = "999.80";
        String orderTime = "2021.01.01 14:25:31";
        String printTime = "2021.01.01 14:25:31";
        String orderNumber = "db2783b010a8h0e19m";
        String storePhone = "1234567890";
        String storeName = "美味餐厅";
        String storeAddress = "美食街1号";
        String deliveryTime = "预计15分钟送达";
        String customerNickname = "吃货小明";
        String deliveryAddress = "幸福小区1栋1单元101";
        PrintConfigVO printConfigVO = new PrintConfigVO();
        // 生成配送联小票
    }
}
