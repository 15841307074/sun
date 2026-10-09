package com.htyoudao.youdao.module.system.util.printer;

import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.*;
import com.htyoudao.youdao.module.system.enums.PrinterTomplateTypeEnum;
import com.htyoudao.youdao.module.system.enums.PrinterTypeEnum;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static String generateReceipt(int templateType, int printerType, String pickupCode, String diningMethod, String pickupTime,
                                         String customerPhone, String orderRemark, List<String> itemInfos, List<String> addItemInfos,
                                         String itemTotal, String packagingFee, String coupon, String total, String deliveryFee,
                                         String orderTime, String printTime, String orderNumber,
                                         String storePhone, String storeName, String storeAddress,
                                         String deliveryTime, String customerNickname, String deliveryAddress, String payType, PrintConfigVO printConfigVO) {
        String receipt = getTemplate(templateType, false, false,printConfigVO.getStore().isMerchantQrCode());
        PrinterTypeEnum printerTypeEnum = PrinterTypeEnum.fromStatus(templateType);
        // 替换通用信息
        receipt = replaceInfo(receipt, "${pickupCode}", pickupCode);
        receipt = replaceInfo(receipt, "${diningMethod}", diningMethod);
        receipt = replaceInfo(receipt, "${payType}", payType);
        receipt = replaceInfo(receipt, "${orderRemark}", orderRemark);

        // 处理商品信息
        StringBuilder itemInfoStr = new StringBuilder();
        for (String itemInfo : itemInfos) {
            itemInfoStr.append(itemInfo).append("\n");
        }
        StringBuilder addItemTotal = new StringBuilder();
        for (String addItemInfo : addItemInfos) {
            addItemTotal.append(addItemInfo).append("\n");
        }
        receipt = replaceInfo(receipt, "${itemInfos}", itemInfoStr.toString());
        receipt = replaceInfo(receipt, "${addItemTotal}", addItemTotal.toString());

        // 根据打印机类型替换信息
        switch (printerTypeEnum) {
            case STORE:
                receipt = replaceStoreInfo(receipt, printerType, pickupTime, customerPhone, itemTotal, packagingFee, coupon, total, orderTime, printTime, orderNumber, printConfigVO);
                break;
            case MEMBER:
                receipt = replaceMemberInfo(receipt, printerType, pickupTime, storePhone, storeName, storeAddress, itemTotal, packagingFee, coupon, total, orderTime, orderNumber, printConfigVO);
                break;
            case KITCHEN:
                receipt = replaceKitchenInfo(receipt, printerType, pickupTime, printConfigVO);
                break;
            case DELIVERY:
                receipt = replaceDeliveryInfo(receipt, printerType, storePhone, storeName, storeAddress, customerPhone, deliveryTime, customerNickname, deliveryAddress, itemTotal, packagingFee, coupon, total, orderTime, orderNumber, deliveryFee, printConfigVO);
                break;
        }
        return receipt;
    }

    // 替换参数
    private static String replaceInfo(String receipt, String tag, String value) {
        return receipt.replace(tag, value);
    }

    private static boolean isDcDeliveryDisplayOrder(PrinterTomplateTypeEnum templateType) {
        return templateType == PrinterTomplateTypeEnum.DELIVERY || templateType == PrinterTomplateTypeEnum.STORE;
    }

    /**
     * 小程序模板只有配送联展示配送信息，商家联保持原堂食取餐模板。
     */
    private static boolean isAppDeliveryDisplayOrder(PrinterTomplateTypeEnum templateType) {
        return templateType == PrinterTomplateTypeEnum.DELIVERY;
    }

    private static String replaceStoreInfo(String receipt, int printerType, String pickupTime, String customerPhone, String itemTotal, String packagingFee, String coupon, String total, String orderTime, String printTime, String orderNumber, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${printTime}", printTime);
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        receipt = replaceInfo(receipt, "${customerPhone}", customerPhone);
        receipt = getString(receipt, itemTotal, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
        return receipt;
    }

    private static String getString(String receipt, String itemTotal, String packagingFee, String coupon, String total, String orderTime, String orderNumber) {
        receipt = replaceInfo(receipt, "${itemTotal}", itemTotal);
        receipt = replaceInfo(receipt, "${packagingFee}", packagingFee);
        receipt = replaceInfo(receipt, "${coupon}", coupon);
        receipt = replaceInfo(receipt, "${total}", total);
        receipt = replaceInfo(receipt, "${orderTime}", orderTime);
        receipt = replaceInfo(receipt, "${orderNumber}", orderNumber);
        return receipt;
    }

    private static String replaceMemberInfo(String receipt, int printerType, String pickupTime, String storePhone, String storeName, String storeAddress, String itemTotal, String packagingFee, String coupon, String total, String orderTime, String orderNumber, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${storePhone}", storePhone);
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        receipt = replaceInfo(receipt, "${storeName}", storeName);
        receipt = replaceInfo(receipt, "${storeAddress}", storeAddress);
        receipt = getString(receipt, itemTotal, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getMember().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
        return receipt;
    }

    private static String replaceKitchenInfo(String receipt, int printerType, String pickupTime, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${pickupTime}", pickupTime);
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
        receipt = replaceInfoTags(receipt, printerType, printConfigVO.getKitchen().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
        return receipt;
    }

    private static String replaceDeliveryInfo(String receipt, int printerType, String storePhone, String storeName, String storeAddress, String customerPhone, String deliveryTime, String customerNickname, String deliveryAddress, String itemTotal, String packagingFee, String coupon, String total, String orderTime, String orderNumber, String deliveryFee, PrintConfigVO printConfigVO) {
        receipt = replaceInfo(receipt, "${storePhone}", storePhone);
        receipt = replaceInfo(receipt, "${storeName}", storeName);
        receipt = replaceInfo(receipt, "${storeAddress}", storeAddress);
        receipt = replaceInfo(receipt, "${customerPhone}", customerPhone);
        receipt = replaceInfo(receipt, "${deliveryTime}", deliveryTime);
        receipt = replaceInfo(receipt, "${customerNickname}", customerNickname);
        receipt = replaceInfo(receipt, "${deliveryAddress}", deliveryAddress);
        receipt = getString(receipt, itemTotal, packagingFee, coupon, total, orderTime, orderNumber);
        receipt = replaceInfo(receipt, "${deliveryFee}", deliveryFee);
        receipt = getString(printerType, printConfigVO, receipt);
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
    public static String getTemplate(int templateType, Boolean isXP, Boolean isBig,Boolean isQrCode) {
        return getTemplate(templateType, isXP, isBig, isQrCode, null);
    }

    public static String getTemplate(int templateType, Boolean isXP, Boolean isBig, Boolean isQrCode, Integer orderType) {
        StringBuilder builder = new StringBuilder();
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        boolean deliveryDisplayOrder = isAppDeliveryDisplayOrder(printerTomplateTypeEnum);
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
                builder.append("\n      <CB>配送码:");
            } else {
                builder.append("\n      <CB>取餐码:");
            }
            builder.append("${pickupCode}")
                    .append("</CB>\n")
                    .append("<CB>")
                    .append("${diningMethod}")
                    .append("</CB>\n")
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
            builder.append("${diningMethod}\n")
                    .append("${payType}</CB>\n")
                    .append(RECEIPT_SEPARATOR);
        }
        if (deliveryDisplayOrder) {
            builder.append("${deliveryTimeSizeStart}${deliveryTimeBoldStart}预计送达时间:${deliveryTime}${deliveryTimeSizeEnd}${deliveryTimeBoldEnd}\n")
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

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE) {
            builder.append("${customerSizeStart}${customerBoldStart}")
                    .append("顾客电话:${customerPhone}\n")
                    .append("${customerSizeEnd}${customerBoldEnd}");
        }
        builder.append("${remarkSizeStart}${remarkBoldStart}")
                .append("${orderRemark}\n")
                .append("${remarkSizeEnd}${remarkBoldEnd}");
        builder.append(RECEIPT_SEPARATOR);
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(ITEM_INFO_HEADER_K);
        } else {
            builder.append(ITEM_INFO_HEADER);
        }

        builder.append(RECEIPT_SEPARATOR)
                .append("${itemInfos}\n");
        builder.append("${itemAddInfos}\n");
        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(RECEIPT_SEPARATOR);
            builder.append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("${totalItem}").append("${itemTotalSizeEnd}${itemTotalBoldEnd}");
            builder.append(RECEIPT_SEPARATOR);
            builder.append("${activityInfos}\n");
            if (isBig) {
                builder.append("${otherSizeStart}${otherBoldStart}").append("${packagingFee}").append("${otherSizeEnd}${otherBoldEnd}")
                        .append("${otherSizeStart}${otherBoldStart}").append("${coupon}");
            } else {
                builder.append("${otherSizeStart}${otherBoldStart}").append("${packagingFee}")
                        .append("${coupon}");
            }
            builder.append("${otherSizeEnd}${otherBoldEnd}");
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder
                    .append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("                    合计：${total}\n")
                    .append("${itemTotalSizeEnd}${itemTotalBoldEnd}")
                    .append(RECEIPT_SEPARATOR);
        }

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE) {
            builder
                    .append("${orderSizeStart}${orderBoldStart}")
                    .append("下单时间:${orderTime}\n")
                    .append("打印时间:${printTime}\n")
                    .append("订单号:${orderNumber}\n")
                    .append("${orderSizeEnd}${orderBoldEnd}");
        }

        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.MEMBER || printerTomplateTypeEnum == PrinterTomplateTypeEnum.DELIVERY) {
            builder
                    .append("${orderSizeStart}${orderBoldStart}")
                    .append("订单号:${orderNumber}\n")
                    .append("下单时间:${orderTime}\n")
                    .append("门店电话:${storePhone}\n")
                    .append("门店名称:${storeName}\n")
                    .append("门店地址:${storeAddress}\n")
                    .append("${orderSizeEnd}${orderBoldEnd}");
        }
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE&&isQrCode) {
            if (isXP) {
                builder.append("<QRCODE s=10 e=L l=center>${orderNumber}</QRCODE><BR>");
            } else {
                builder.append("<QR>${orderNumber}</QR>");
            }
        }
        return builder.toString();
    }


    public static String getTemplateDC(int templateType, Boolean isXP,Boolean isQrCode) {
        return getTemplateDC(templateType, isXP, isQrCode, null);
    }

    public static String getTemplateDC(int templateType, Boolean isXP, Boolean isQrCode, Integer orderType) {
        return getTemplateDC(templateType, isXP, isQrCode, orderType, true);
    }

    public static String getTemplateDC(int templateType, Boolean isXP, Boolean isQrCode, Integer orderType, boolean showCampusDeliveryAmount) {
        StringBuilder builder = new StringBuilder();
        PrinterTomplateTypeEnum printerTomplateTypeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        boolean deliveryDisplayOrder = isDcDeliveryDisplayOrder(printerTomplateTypeEnum);
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
                builder.append("\n      <CB>${pickupName}");
            } else {
                builder.append("\n      <CB>取餐码:");
            }
            builder.append("${pickupCode}")
                    .append("</CB>\n")
                    .append("<CB>")
                    .append("${diningMethod}")
                    .append("</CB>\n")
                    .append("      <CB>")
                    .append("${payType}")
                    .append("</CB>\n")
                    .append(RECEIPT_SEPARATOR);
        } else {
            builder.append(RECEIPT_SEPARATOR);
            if (deliveryDisplayOrder) {
                builder.append("<CB>${pickupName}${pickupCode}\n");
            } else {
                builder.append("<CB>取餐码:${pickupCode}\n");
            }
            builder.append("${diningMethod}\n")
                    .append("${payType}</CB>\n")
                    .append(RECEIPT_SEPARATOR);
        }
        if (deliveryDisplayOrder) {
            builder.append("${deliveryTimeSizeStart}${deliveryTimeBoldStart}${deliveryTime}${deliveryTimeSizeEnd}${deliveryTimeBoldEnd}\n")
                    .append("${customerSizeStart}${customerBoldStart}")
                    .append("${customerNickname}\n")
                    .append("${customerPhone}\n")
                    .append("${deliveryAddress}\n")
                    .append("${customerSizeEnd}${customerBoldEnd}");
        } else {
            builder.append("${pickupTimeSizeStart}${pickupTimeBoldStart}")
                    .append("${pickupTime}\n")
                    .append("${pickupTimeSizeEnd}${pickupTimeBoldEnd}");
        }

//        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE) {
//            builder.append("${customerSizeStart}${customerBoldStart}")
//                    .append("${customerPhone}\n")
//                    .append("${customerSizeEnd}${customerBoldEnd}");
//        }
        builder.append("${remarkSizeStart}${remarkBoldStart}")
                .append("${orderRemark}\n")
                .append("${remarkSizeEnd}${remarkBoldEnd}");
        builder.append(RECEIPT_SEPARATOR);
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(ITEM_INFO_HEADER_K);
        } else {
            builder.append(ITEM_INFO_HEADER);
        }
        builder.append(RECEIPT_SEPARATOR)
                .append("${itemInfos}\n");
        builder.append("${itemAddInfos}\n");
        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder.append(RECEIPT_SEPARATOR);
            builder.append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("${totalItem}").append("${itemTotalSizeEnd}${itemTotalBoldEnd}");
            builder.append(RECEIPT_SEPARATOR);
            builder.append("${activityInfos}\n")
                    .append("${otherSizeStart}${otherBoldStart}")
                    .append("${packagingFee}")
                    .append("${coupon}")
                    .append("${otherSizeEnd}${otherBoldEnd}");
        }


        if (showCampusDeliveryAmount && printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN&& printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE) {
            builder
                    .append("${otherSizeStart}${otherBoldStart}");
            builder.append("${errandRewardAmount}");
            builder.append("${errandStoreSubsidyAmount}");
            builder.append("${otherSizeEnd}${otherBoldEnd}");
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder
                    .append("${otherSizeStart}${otherBoldStart}");
            builder.append("${deliveryFee}\n");
            builder.append("${otherSizeEnd}${otherBoldEnd}");
        }

        if (printerTomplateTypeEnum != PrinterTomplateTypeEnum.KITCHEN) {
            builder
                    .append("${itemTotalSizeStart}${itemTotalBoldStart}")
                    .append("                    合计：${total}\n")
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
                    .append("${storePhone}\n")
                    .append("${storeName}\n")
                    .append("${storeAddress}\n")
                    .append("${orderSizeEnd}${orderBoldEnd}");
        }
        if (printerTomplateTypeEnum == PrinterTomplateTypeEnum.STORE&&isQrCode){
            builder.append("${qrCode}");
        }
        builder.append(RECEIPT_SEPARATOR);
        return builder.toString();
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


    private static String getTextSizeStart(int textSize, int printerType) {
        return TEXT_SIZE_START_MAP.getOrDefault(printerType, new HashMap<>()).getOrDefault(textSize, "");
    }

    private static String getSortingMethodStrat(Boolean sortingMethod) {
        return sortingMethod ? BOLD_TAG_START : "";
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

    private static String getTextSizeEnd(int textSize, int printerType) {
        return TEXT_SIZE_END_MAP.getOrDefault(printerType, new HashMap<>()).getOrDefault(textSize, "");
    }

    private static String getSortingMethodEnd(Boolean sortingMethod) {
        return sortingMethod ? BOLD_TAG_END : "";
    }

    /**
     * 点餐机 打印机列表获取对应模板信息
     */
    public static List<PrintCommodityTempVO> getTemplateDC(Set<Integer> templateTypes, int printerType, PrintConfigVO printConfigVO) {
        return getTemplateDC(templateTypes, printerType, printConfigVO, null);
    }

    public static List<PrintCommodityTempVO> getTemplateDC(Set<Integer> templateTypes, int printerType, PrintConfigVO printConfigVO, Integer orderType) {
        return getTemplateDC(templateTypes, printerType, printConfigVO, orderType, true);
    }

    public static List<PrintCommodityTempVO> getTemplateDC(Set<Integer> templateTypes, int printerType, PrintConfigVO printConfigVO, Integer orderType, boolean showCampusDeliveryAmount) {
        List<PrintCommodityTempVO> templateList = new ArrayList<>();
        Boolean isXP;
        if (printerType == 2) {
            isXP = true;
        } else {
            isXP = false;
        }
        templateTypes.forEach(templateType -> {
            PrintCommodityTempVO printCommodityTempVO = new PrintCommodityTempVO();
            String receipt = getTemplateDC(templateType, isXP, printConfigVO.getStore().isMerchantQrCode(), orderType, showCampusDeliveryAmount);
            receipt = getStringDC(templateType, printerType, printConfigVO, receipt);
            printCommodityTempVO.setPrintCommodityTem(receipt);
            printCommodityTempVO.setTemplateType(templateType);
            PrintCommodityConfigTagVO configVO = getTemplateCommodityTagDC(templateType, printerType, printConfigVO);
            PrinterTypeEnum printerTypeEnum = PrinterTypeEnum.fromStatus(templateType);
            switch (printerTypeEnum) {
                case STORE:
                    configVO.setOtherSizeTag(printConfigVO.getStore().getOtherInformation().getTextSize());
                    break;
                case MEMBER:
                    configVO.setOtherSizeTag(printConfigVO.getMember().getOtherInformation().getTextSize());
                    break;
                case DELIVERY:
                    configVO.setOtherSizeTag(printConfigVO.getDelivery().getOtherInformation().getTextSize());
                    break;
            }
            printCommodityTempVO.setProductInformation(configVO);
            templateList.add(printCommodityTempVO);
        });
        return templateList;
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
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getDelivery().getDeliveryInformation(), "${deliveryTimeSizeStart}", "${deliveryTimeSizeEnd}", "${deliveryTimeBoldStart}", "${deliveryTimeBoldEnd}");
                break;
            case STORE:
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${pickupTimeSizeStart}", "${pickupTimeSizeEnd}", "${pickupTimeBoldStart}", "${pickupTimeBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getCustomerInformation(), "${customerSizeStart}", "${customerSizeEnd}", "${customerBoldStart}", "${customerBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getRemarksInformation(), "${remarkSizeStart}", "${remarkSizeEnd}", "${remarkBoldStart}", "${remarkBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getTotalInformation(), "${itemTotalSizeStart}", "${itemTotalSizeEnd}", "${itemTotalBoldStart}", "${itemTotalBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOtherInformation(), "${otherSizeStart}", "${otherSizeEnd}", "${otherBoldStart}", "${otherBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getOrderInformation(), "${orderSizeStart}", "${orderSizeEnd}", "${orderBoldStart}", "${orderBoldEnd}");
                receipt = replaceInfoTags(receipt, printerType, printConfigVO.getStore().getPickUpInformation(), "${deliveryTimeSizeStart}", "${deliveryTimeSizeEnd}", "${deliveryTimeBoldStart}", "${deliveryTimeBoldEnd}");

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

    /**
     * 点餐机 打印机列表获取对应模板信息
     */
    public static List<PrintCommodityTempVO> getTemplateApp(Set<Integer> templateTypes, int printerType, PrintConfigVO printConfigVO) {
        return getTemplateApp(templateTypes, printerType, printConfigVO, null);
    }

    public static List<PrintCommodityTempVO> getTemplateApp(Set<Integer> templateTypes, int printerType, PrintConfigVO printConfigVO, Integer orderType) {
        List<PrintCommodityTempVO> templateList = new ArrayList<>();
        Boolean isXP;
        if (printerType == 2) {
            isXP = true;
        } else {
            isXP = false;
        }
        templateTypes.forEach(templateType -> {
            Boolean isBig = false;
            PrinterTomplateTypeEnum typeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
            switch (typeEnum) {
                case STORE:
                    if (printConfigVO.getStore().getOtherInformation().getTextSize() == 0) {
                        isBig = true;
                    }
                    break;
                case MEMBER:
                    if (printConfigVO.getMember().getOtherInformation().getTextSize() == 0) {
                        isBig = true;
                    }
                    break;
                case KITCHEN:
                    break;
                case DELIVERY:
                    if (printConfigVO.getDelivery().getOtherInformation().getTextSize() == 0) {
                        isBig = true;
                    }
                    break;
            }
            PrintCommodityTempVO printCommodityTempVO = new PrintCommodityTempVO();
            String receipt = getTemplate(templateType, isXP, isBig, printConfigVO.getStore().isMerchantQrCode(), orderType);
            receipt = getStringDC(templateType, printerType, printConfigVO, receipt);
            printCommodityTempVO.setPrintCommodityTem(receipt);
            printCommodityTempVO.setTemplateType(templateType);
            PrintCommodityConfigTagVO configVO = getTemplateCommodityTagDC(templateType, printerType, printConfigVO);
            printCommodityTempVO.setProductInformation(configVO);
            templateList.add(printCommodityTempVO);
        });
        return templateList;
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

    /**
     * 点餐机 打印机列表获取对应商品信息标签
     */
    public static PrintCommodityConfigTagVO getTemplateCommodityTagDC(Integer templateType, int printerType, PrintConfigVO printConfigVO) {
        PrintCommodityConfigTagVO configVO = new PrintCommodityConfigTagVO();
        PrinterTomplateTypeEnum typeEnum = PrinterTomplateTypeEnum.fromStatus(templateType);
        if (printConfigVO == null) {
            return configVO;
        }
// 统一提取三类配置
        ProductInfoVO infoVO = getProductInfoWithDefault(printConfigVO, typeEnum, "product");
        ProductInfoVO infoDetailVO = getProductInfoWithDefault(printConfigVO, typeEnum, "detail");
        ProductInfoVO infoAddVO = getProductInfoWithDefault(printConfigVO, typeEnum, "add");
        ProductInfoVO infoActivityVO = getProductInfoWithDefault(printConfigVO, typeEnum, "activity");


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
        // 设置活动
        setConfig(configVO::setProductActivitySizeTagStart, configVO::setProductActivitySizeTagEnd,
                configVO::setProductActivityBoldTagStart, configVO::setProductActivityBoldTagEnd,
                configVO::setProductAddSort, infoActivityVO, printerType);
        switch (typeEnum) {
            case STORE:
                configVO.setOtherSizeTag(printConfigVO.getStore().getOtherInformation().getTextSize());
                break;
            case MEMBER:
                configVO.setOtherSizeTag(printConfigVO.getMember().getOtherInformation().getTextSize());
                break;
            case DELIVERY:
                configVO.setOtherSizeTag(printConfigVO.getDelivery().getOtherInformation().getTextSize());
                break;
        }
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
        storeExtractors.put("activity", config ->
                config.getStore() != null ? config.getStore().getOtherInformation() : null);

        // 顾客联
        Map<String, ProductInfoExtractor> memberExtractors = new HashMap<>();
        memberExtractors.put("product", config ->
                config.getMember() != null ? config.getMember().getProductInformation() : null);
        memberExtractors.put("detail", config ->
                config.getMember() != null ? config.getMember().getProductDetailedInformation() : null);
        memberExtractors.put("add", config ->
                config.getMember() != null ? config.getMember().getAdditionalPurchaseInformation() : null);
        memberExtractors.put("activity", config ->
                config.getMember() != null ? config.getMember().getOtherInformation() : null);

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
        deliveryExtractors.put("activity", config ->
                config.getDelivery() != null ? config.getDelivery().getOtherInformation() : null);
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

    private static ProductInfoVO getProductInfoWithDefault(PrintConfigVO configVO, PrinterTomplateTypeEnum type, String key) {
        return Optional.ofNullable(getProductInfo(configVO, type, key))
                .orElse(new ProductInfoVO());
    }

    // 统一设置方法保持不变
    private static void setConfig(Consumer<String> sizeStartSetter, Consumer<String> sizeEndSetter,
                                  Consumer<String> boldStartSetter, Consumer<String> boldEndSetter,
                                  Consumer<Integer> sortSetter,
                                  ProductInfoVO infoVO, int printerType) {
        if (infoVO == null) return;

        sizeStartSetter.accept(getTextSizeStart(infoVO.getTextSize(), printerType));
        sizeEndSetter.accept(getTextSizeEnd(infoVO.getTextSize(), printerType));
        boldStartSetter.accept(getSortingMethodStrat(infoVO.getFontBold()));
        boldEndSetter.accept(getSortingMethodEnd(infoVO.getFontBold()));
        if (sortSetter != null) {
            sortSetter.accept(infoVO.getSortingMethod());
        }
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

        String itemTotal = "10份999.99";
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
        String deliveryReceipt = generateReceipt(1, 1, pickupCode, diningMethod, pickupTime, customerPhone, orderRemark, itemInfos, addItemInfos, itemTotal, packagingFee, deliveryFee, coupon, total, orderTime, printTime, orderNumber, storePhone, storeName, storeAddress, deliveryTime, customerNickname, deliveryAddress, payType, printConfigVO);
        System.out.println("\n配送联小票：\n" + deliveryReceipt);
    }
}
