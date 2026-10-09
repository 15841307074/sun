package com.htyoudao.youdao.module.order.util.xingye;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author LylJavas
 * @create 2021/2/26 15:18
 * @description 打印示例
 */
@Slf4j
public class XpYunSendUtil {
    /**
     * 打印服务对象实例化
     */
    private static String BASE_URL = "https://open.xpyun.net/api/openapi";

    /**
     * 3.小票打印字体对齐样例，不支持金额播报
     * 注意：对齐标签L C R CB 请勿嵌套使用，嵌套使用内层标签有效，外层失效；
     * 同一行请勿使用多个对齐标签，否则只有最后一个对齐标签有效
     */
    public void printFontAlign() {
        /**
         * <BR>：换行符（同一行有闭合标签(如 </C>)则应放到闭合标签前面, 连续两个换行符<BR><BR>可以表示加一空行）
         *  <L></L>：左对齐
         *  <C></C>：居中对齐
         *  <R></R>：右对齐
         *  注意：同一行内容不能使用多种对齐方式，可通过补空格方式自定义对齐样式。
         *       58mm的机器，一行打印16个汉字，32个字母
         *       80mm的机器，一行打印24个汉字，48个字母
         *
         *  <N></N>：字体正常大小
         *  <HB></HB>：字体变高一倍
         *  <WB></WB>：字体变宽一倍
         *  <B></B>：字体放大一倍
         *  <CB></CB>：字体放大一倍居中
         *  <HB2></HB2>：字体变高二倍
         *  <WB2></WB2>：字体变宽二倍
         *  <B2></B2>：字体放大二倍
         *  <BOLD></BOLD>：字体加粗
         *  <IMG></IMG>：打印LOGO图片，需登录开放平台在【打印机管理=》设备管理】下通过设置LOGO功能进行上传。此处直接写入
         *             空标签, 如 <IMG></IMG> 即可, 具体可参考样例。
         *             图片宽度设置：可以通过 <IMG> 标签名称自定义，如 <IMG60> 表示宽度为60，相应的闭合标签 </IMG>
         *             不需要指定高度。<IMG> 标签不指定宽度默认为40，最小值为20，最大值为100
         *  <QR></QR>：二维码（标签内容是二维码值, 最大不能超过256个字符, 单个订单最多只能打印一个二维码）。
         *             二维码宽度设置：可以通过 <QR> 标签名称自定义，如 <QR180> 表示宽度为180，相应的闭合标签 </QR>
         *             不需要指定宽度。<QR> 标签不指定宽度默认为110，最小值为90，最大值为180
         *  <BARCODE></BARCODE>：条形码（标签内容是条形码值）
         */
        StringBuilder printContent = new StringBuilder();
        printContent.append("不加标签：").append("默认字体大小<BR>");

        printContent.append("<BR>");
        printContent.append("L标签：").append("<L>左对齐<BR></L>");
        printContent.append("<BR>");
        printContent.append("R标签：").append("<R>右对齐<BR></R>");
        printContent.append("<BR>");
        printContent.append("C标签：").append("<C>居中对齐<BR></C>");

        printContent.append("<BR>");
        printContent.append("N标签：").append("<N>字体正常大小<BR></N>");
        printContent.append("<BR>");
        printContent.append("HB标签：").append("<HB>字体变高一倍<BR></HB>");
        printContent.append("<BR>");
        printContent.append("WB标签：").append("<WB>字体变宽一倍<BR></WB>");
        printContent.append("<BR>");
        printContent.append("B标签：").append("<B>字体放大一倍<BR></B>");
        printContent.append("<BR>");
        printContent.append("HB2标签：").append("<HB2>字体变高二倍<BR></HB2>");
        printContent.append("<BR>");
        printContent.append("WB2标签：").append("<WB2>字体变宽二倍<BR></WB2>");
        printContent.append("<BR>");
        printContent.append("B2标签：").append("<B2>字体放大二倍<BR></B2>");
        printContent.append("<BR>");
        printContent.append("BOLD标签：").append("<BOLD>字体加粗<BR></BOLD>");

        // 嵌套使用对齐和字体
        printContent.append("<BR>");
        printContent.append("<C>嵌套使用：").append("<BOLD>居中加粗</BOLD>").append("<BR></C>");

        // 打印条形码和二维码
        printContent.append("<BR>");
        printContent.append("<C><BARCODE>9884822189</BARCODE></C>");
        printContent.append("<C><QR>https://www.xpyun.net</QR></C>");


        PrintRequest request = new PrintRequest();
        Config.createRequestHeader(request);
        //*必填*：打印机编号
        request.setSn(Config.OK_PRINTER_SN);
        //*必填*：打印内容,不能超过12K
        request.setContent(printContent.toString());
        //打印份数，默认为1
        request.setCopies(1);
        //声音播放模式，0 为取消订单模式，1 为静音模式，2 为来单播放模式，3为有用户申请退单了。默认为 2 来单播放模式
        request.setVoice(2);
        //打印模式：
        //值为 0 或不指定则会检查打印机是否在线，如果不在线 则不生成打印订单，直接返回设备不在线状态码；如果在线则生成打印订单，并返回打印订单号。
        //值为 1不检查打印机是否在线，直接生成打印订单，并返回打印订单号。如果打印机不在线，订单将缓存在打印队列中，打印机正常在线时会自动打印。
        request.setMode(0);
        // 打印接口回调地址对应标识(取值范围 [ 1 - 5 ] 的整数)，对于web管理后台 “功能设置” 菜单设置界面的打印接口回调标识。
        // request.setBackurlFlag(1);

//        ObjectRestResponse<String> resp = service.print(request);
//        //resp.data:正确返回订单编号
//        System.out.println(resp);
    }

    /**
     * 3.小票打印字体对齐样例，支持金额播报
     * 注意：对齐标签L C R CB 请勿嵌套使用，嵌套使用内层标签有效，外层失效；
     * 同一行请勿使用多个对齐标签，否则只有最后一个对齐标签有效
     */
    public void printFontAlignVoiceSupport() {
        /**
         * <BR>：换行符（同一行有闭合标签(如 </C>)则应放到闭合标签前面, 连续两个换行符<BR><BR>可以表示加一空行）
         *  <L></L>：左对齐
         *  <C></C>：居中对齐
         *  <R></R>：右对齐
         *  注意：同一行内容不能使用多种对齐方式，可通过补空格方式自定义对齐样式。
         *       58mm的机器，一行打印16个汉字，32个字母
         *       80mm的机器，一行打印24个汉字，48个字母
         *
         *  <N></N>：字体正常大小
         *  <HB></HB>：字体变高一倍
         *  <WB></WB>：字体变宽一倍
         *  <B></B>：字体放大一倍
         *  <CB></CB>：字体放大一倍居中
         *  <HB2></HB2>：字体变高二倍
         *  <WB2></WB2>：字体变宽二倍
         *  <B2></B2>：字体放大二倍
         *  <BOLD></BOLD>：字体加粗
         *  <IMG></IMG>：打印LOGO图片，需登录开放平台在【打印机管理=》设备管理】下通过设置LOGO功能进行上传。此处直接写入
         *             空标签, 如 <IMG></IMG> 即可, 具体可参考样例。
         *             图片宽度设置：可以通过 <IMG> 标签名称自定义，如 <IMG60> 表示宽度为60，相应的闭合标签 </IMG>
         *             不需要指定高度。<IMG> 标签不指定宽度默认为40，最小值为20，最大值为100
         *  <QR></QR>：二维码（标签内容是二维码值, 最大不能超过256个字符, 单个订单最多只能打印一个二维码）。
         *             二维码宽度设置：可以通过 <QR> 标签名称自定义，如 <QR180> 表示宽度为180，相应的闭合标签 </QR>
         *             不需要指定宽度。<QR> 标签不指定宽度默认为110，最小值为90，最大值为180
         *  <BARCODE></BARCODE>：条形码（标签内容是条形码值）
         */
        StringBuilder printContent = new StringBuilder();
        printContent.append("不加标签：").append("默认字体大小<BR>");

        printContent.append("<BR>");
        printContent.append("L标签：").append("<L>左对齐<BR></L>");
        printContent.append("<BR>");
        printContent.append("R标签：").append("<R>右对齐<BR></R>");
        printContent.append("<BR>");
        printContent.append("C标签：").append("<C>居中对齐<BR></C>");

        printContent.append("<BR>");
        printContent.append("N标签：").append("<N>字体正常大小<BR></N>");
        printContent.append("<BR>");
        printContent.append("HB标签：").append("<HB>字体变高一倍<BR></HB>");
        printContent.append("<BR>");
        printContent.append("WB标签：").append("<WB>字体变宽一倍<BR></WB>");
        printContent.append("<BR>");
        printContent.append("B标签：").append("<B>字体放大一倍<BR></B>");
        printContent.append("<BR>");
        printContent.append("HB2标签：").append("<HB2>字体变高二倍<BR></HB2>");
        printContent.append("<BR>");
        printContent.append("WB2标签：").append("<WB2>字体变宽二倍<BR></WB2>");
        printContent.append("<BR>");
        printContent.append("B2标签：").append("<B2>字体放大二倍<BR></B2>");
        printContent.append("<BR>");
        printContent.append("BOLD标签：").append("<BOLD>字体加粗<BR></BOLD>");

        // 嵌套使用对齐和字体
        printContent.append("<BR>");
        printContent.append("<C>嵌套使用：").append("<BOLD>居中加粗</BOLD>").append("<BR></C>");

        // 打印条形码和二维码
        printContent.append("<BR>");
        printContent.append("<C><BARCODE>9884822189</BARCODE></C>");
        printContent.append("<C><QR>https://www.xpyun.net</QR></C>");


        PrintRequest request = new PrintRequest();
        Config.createRequestHeader(request);
        //*必填*：打印机编号
        request.setSn(Config.OK_PRINTER_SN);
        //*必填*：打印内容,不能超过12K
        request.setContent(printContent.toString());
        //打印份数，默认为1
        request.setCopies(1);
        //声音播放模式，0 为取消订单模式，1 为静音模式，2 为来单播放模式，3为有用户申请退单了。默认为 2 来单播放模式
        request.setVoice(2);
        //打印模式：
        //值为 0 或不指定则会检查打印机是否在线，如果不在线 则不生成打印订单，直接返回设备不在线状态码；如果在线则生成打印订单，并返回打印订单号。
        //值为 1不检查打印机是否在线，直接生成打印订单，并返回打印订单号。如果打印机不在线，订单将缓存在打印队列中，打印机正常在线时会自动打印。
        request.setMode(0);

        //支付方式：
        //取值范围41~55：
        //支付宝 41、微信 42、云支付 43、银联刷卡 44、银联支付 45、会员卡消费 46、会员卡充值 47、翼支付 48、成功收款 49、嘉联支付 50、壹钱包 51、京东支付 52、快钱支付 53、威支付 54、享钱支付 55
        //仅用于支持金额播报的芯烨云打印机。
        request.setPayType(41);
        //支付与否：
        //取值范围59~61：
        //退款 59 到账 60 消费 61。
        //仅用于支持金额播报的芯烨云打印机。
        request.setPayMode(60);
        //支付金额：
        //最多允许保留2位小数。
        //仅用于支持金额播报的芯烨云打印机。
        request.setMoney(20.15);

        // 打印接口回调地址对应标识(取值范围 [ 1 - 5 ] 的整数)，对于web管理后台 “功能设置” 菜单设置界面的打印接口回调标识。
        // request.setBackurlFlag(1);

        String url = BASE_URL + "/xprinter/print";
        String jsonRequest = JSON.toJSONString(request);
        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
        ObjectRestResponse<String> resps = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<String>>() {
        });
        //resp.data:正确返回订单编号
        System.out.println(resps);
    }

    /**
     * 3.小票打印综合排版样例，不支持金额播报
     * 58mm打印机一行可打印机32个字符
     */
    public void printComplexReceipt() throws Exception {
        /**
         * <BR>：换行符（同一行有闭合标签(如 </C>)则应放到闭合标签前面, 连续两个换行符<BR><BR>可以表示加一空行）
         *  <L></L>：左对齐
         *  <C></C>：居中对齐
         *  <R></R>：右对齐
         *  注意：同一行内容不能使用多种对齐方式，可通过补空格方式自定义对齐样式。
         *       58mm的机器，一行打印16个汉字，32个字母
         *       80mm的机器，一行打印24个汉字，48个字母
         *
         *  <N></N>：字体正常大小
         *  <HB></HB>：字体变高一倍
         *  <WB></WB>：字体变宽一倍
         *  <B></B>：字体放大一倍
         *  <CB></CB>：字体放大一倍居中
         *  <HB2></HB2>：字体变高二倍
         *  <WB2></WB2>：字体变宽二倍
         *  <B2></B2>：字体放大二倍
         *  <BOLD></BOLD>：字体加粗
         *  <IMG></IMG>：打印LOGO图片，需登录开放平台在【打印机管理=》设备管理】下通过设置LOGO功能进行上传。此处直接写入
         *             空标签, 如 <IMG></IMG> 即可, 具体可参考样例。
         *             图片宽度设置：可以通过 <IMG> 标签名称自定义，如 <IMG60> 表示宽度为60，相应的闭合标签 </IMG>
         *             不需要指定高度。<IMG> 标签不指定宽度默认为40，最小值为20，最大值为100
         *  <QR></QR>：二维码（标签内容是二维码值, 最大不能超过256个字符, 单个订单最多只能打印一个二维码）。
         *             二维码宽度设置：可以通过 <QR> 标签名称自定义，如 <QR180> 表示宽度为180，相应的闭合标签 </QR>
         *             不需要指定宽度。<QR> 标签不指定宽度默认为110，最小值为90，最大值为180
         *  <BARCODE></BARCODE>：条形码（标签内容是条形码值）
         */
        StringBuilder printContent = new StringBuilder();

        printContent.append("<C>").append("<B>芯烨云小票</B>").append("<BR></C>");
        printContent.append("<BR>");

        printContent.append("菜名").append(StringUtils.repeat(" ", 32))
                .append("数量").append(StringUtils.repeat(" ", 2))
                .append("单价").append(StringUtils.repeat(" ", 2))
                .append("<BR>");
        printContent.append(StringUtils.repeat("-", 48)).append("<BR>");
        printContent.append(NoteFormatter.formatPrintOrderItemForNewLine80("可乐鸡翅", 2, 9.99));
        printContent.append(NoteFormatter.formatPrintOrderItemForNewLine80("水煮鱼特辣水煮鱼特辣水煮鱼特辣", 1, 108.0));
        printContent.append(NoteFormatter.formatPrintOrderItemForNewLine80("豪华版超级无敌龙虾炒饭豪华版超级无敌龙虾炒饭", 1, 99.9));
        printContent.append(NoteFormatter.formatPrintOrderItemForNewLine80("炭烤鳕鱼", 5, 19.99));
        printContent.append(StringUtils.repeat("-", 48)).append("<BR>");
        printContent.append("<R>").append("合计：").append("327.83").append("元").append("<BR></R>");

        printContent.append("<BR>");
        printContent.append("<L>")
                .append("客户地址：").append("珠海市香洲区xx路xx号").append("<BR>")
                .append("客户电话：").append("1363*****88").append("<BR>")
                .append("下单时间：").append("2020-9-9 15:07:57").append("<BR>")
                .append("备注：").append("少放辣 不吃香菜").append("<BR>");

        printContent.append("<C>")
                .append("<QR>https://www.xpyun.net</QR>")
                .append("</C>");

        PrintRequest request = new PrintRequest();
        Config.createRequestHeader(request);
        //*必填*：打印机编号
        request.setSn(Config.OK_PRINTER_SN);
        //*必填*：打印内容,不能超过12K
        request.setContent(printContent.toString());
        //打印份数，默认为1
        request.setCopies(1);
        //声音播放模式，0 为取消订单模式，1 为静音模式，2 为来单播放模式，3为有用户申请退单了。默认为 2 来单播放模式
        request.setVoice(2);
        //打印模式：
        //值为 0 或不指定则会检查打印机是否在线，如果不在线 则不生成打印订单，直接返回设备不在线状态码；如果在线则生成打印订单，并返回打印订单号。
        //值为 1不检查打印机是否在线，直接生成打印订单，并返回打印订单号。如果打印机不在线，订单将缓存在打印队列中，打印机正常在线时会自动打印。
        request.setMode(0);
        // 打印接口回调地址对应标识(取值范围 [ 1 - 5 ] 的整数)，对于web管理后台 “功能设置” 菜单设置界面的打印接口回调标识。
        // request.setBackurlFlag(1);

//        ObjectRestResponse<String> resp = service.print(request);
//        //resp.data:正确返回订单编号
//        System.out.println(resp);
    }

    /**
     * 3.小票打印综合排版样例，支持金额播报
     * 58mm打印机一行可打印机32个字符
     */
    public static String printComplexReceiptVoiceSupport(String content, String sn, Double money, int printCopies)  {
        /**
         * <BR>：换行符（同一行有闭合标签(如 </C>)则应放到闭合标签前面, 连续两个换行符<BR><BR>可以表示加一空行）
         *  <L></L>：左对齐
         *  <C></C>：居中对齐
         *  <R></R>：右对齐
         *  注意：同一行内容不能使用多种对齐方式，可通过补空格方式自定义对齐样式。
         *       58mm的机器，一行打印16个汉字，32个字母
         *       80mm的机器，一行打印24个汉字，48个字母
         *
         *  <N></N>：字体正常大小
         *  <HB></HB>：字体变高一倍
         *  <WB></WB>：字体变宽一倍
         *  <B></B>：字体放大一倍
         *  <CB></CB>：字体放大一倍居中
         *  <HB2></HB2>：字体变高二倍
         *  <WB2></WB2>：字体变宽二倍
         *  <B2></B2>：字体放大二倍
         *  <BOLD></BOLD>：字体加粗
         *  <IMG></IMG>：打印LOGO图片，需登录开放平台在【打印机管理=》设备管理】下通过设置LOGO功能进行上传。此处直接写入
         *             空标签, 如 <IMG></IMG> 即可, 具体可参考样例。
         *             图片宽度设置：可以通过 <IMG> 标签名称自定义，如 <IMG60> 表示宽度为60，相应的闭合标签 </IMG>
         *             不需要指定高度。<IMG> 标签不指定宽度默认为40，最小值为20，最大值为100
         *  <QR></QR>：二维码（标签内容是二维码值, 最大不能超过256个字符, 单个订单最多只能打印一个二维码）。
         *             二维码宽度设置：可以通过 <QR> 标签名称自定义，如 <QR180> 表示宽度为180，相应的闭合标签 </QR>
         *             不需要指定宽度。<QR> 标签不指定宽度默认为110，最小值为90，最大值为180
         *  <BARCODE></BARCODE>：条形码（标签内容是条形码值）
         */

        PrintRequest request = new PrintRequest();
        Config.createRequestHeader(request);
        //*必填*：打印机编号
        request.setSn(sn);
        //*必填*：打印内容,不能超过12K
        request.setContent(content);
        //打印份数，默认为1
        request.setCopies(printCopies);
        //声音播放模式，0 为取消订单模式，1 为静音模式，2 为来单播放模式，3为有用户申请退单了。默认为 2 来单播放模式
        request.setVoice(2);
        //打印模式：
        //值为 0 或不指定则会检查打印机是否在线，如果不在线 则不生成打印订单，直接返回设备不在线状态码；如果在线则生成打印订单，并返回打印订单号。
        //值为 1不检查打印机是否在线，直接生成打印订单，并返回打印订单号。如果打印机不在线，订单将缓存在打印队列中，打印机正常在线时会自动打印。
        request.setMode(1);

        //支付方式：
        //取值范围41~55：
        //支付宝 41、微信 42、云支付 43、银联刷卡 44、银联支付 45、会员卡消费 46、会员卡充值 47、翼支付 48、成功收款 49、嘉联支付 50、壹钱包 51、京东支付 52、快钱支付 53、威支付 54、享钱支付 55
        //仅用于支持金额播报的芯烨云打印机。
        //  request.setPayType(48);
        //支付与否：
        //取值范围59~61：
        //退款 59 到账 60 消费 61。
        //仅用于支持金额播报的芯烨云打印机。
        //  request.setPayMode(60);
        //支付金额：
        //最多允许保留2位小数。
        //仅用于支持金额播报的芯烨云打印机。
        // request.setMoney(money);
        // 打印接口回调地址对应标识(取值范围 [ 1 - 5 ] 的整数)，对于web管理后台 “功能设置” 菜单设置界面的打印接口回调标识。
        // request.setBackurlFlag(1);


        String url = BASE_URL + "/xprinter/print";
        String jsonRequest = JSON.toJSONString(request);
        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
        ObjectRestResponse<String> resps = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<String>>() {
        });
        //resp.data:正确返回订单编号
        log.info(">>> 调用打印机 url={}, 返回 = {}",url, resps);
        if (resps.getCode()==0 &&  resps.getData()!=null) {
            return resps.getData();
        }
        return resps.getMsg().toString()+"-----"+resps.getCode();
    }

    /**
     * 4.标签打印综合排版样例
     * 如何确定坐标：坐标原点位于左上角，x轴是从左往右，y轴是从上往下；
     * 根据测试，x轴最大值=标签纸宽度*8，y轴最大值=标签纸高度*8，
     * 如标签纸尺寸为40*30，x轴最大值=40*8=320，y轴最大值=30*8=240
     * 实际排版效果需要用户按实际纸张尺寸和需求自行排版
     * <p>
     * 打印内容内（标签除外）大于号和小于号需要经过转译才能正常打印。其中，“<”用“&lt”表示，“>”用“&gt”表示；1mm=8dots。
     */
    public static void printLabel() {
        /**
         * <PAGE></PAGE>：
         *  分页，用于支持打印多张不同的标签页面（最多10张），不使用该标签表示所有元素只打印在一个标签页
         *
         *  <SIZE>width,height</SIZE>：
         *  设置标签纸宽高，width 标签纸宽度(不含背纸)，height 标签纸高度(不含背纸)，单位mm，如<SIZE>40,30</SIZE>
         *
         *  <TEXT x="10" y="100" w="1" h="2" r="0">文本内容</TEXT>：
         *  打印文本，其中：
         *  属性 x 为水平方向起始点坐标（默认为0）
         *  属性 y 为垂直方向起始点坐标（默认为0）
         *  属性 w 为文字宽度放大倍率1-10（默认为1）
         *  属性 h 为文字高度放大倍率1-10（默认为1）
         *  属性 r 为文字旋转角度(顺时针方向，默认为0)：
         *  0     0度
         *  90   90度
         *  180 180度
         *  270 270度
         *
         *  <BC128 x="10" y="100" h="60" s="1" n="1" w="1" r="0">1234567</BC128>：
         *  打印code128一维码，其中：
         *  属性 x 为水平方向起始点坐标（默认为0）
         *  属性 y 为垂直方向起始点坐标（默认为0）
         *  属性 h 为条形码高度（默认为48）
         *  属性 s 是否人眼可识：0 不可识，1 可识（默认为1）
         *  属性 n 为窄 bar 宽度，以点(dot)表示（默认为1）
         *  属性 w 为宽 bar 宽度，以点(dot)表示（默认为1）
         *  属性 r 为文字旋转角度 (顺时针方向，默认为0)：
         *  0     0度
         *  90   90度
         *  180 180度
         *  270 270度
         *
         *  <BC39 x="10" y="100" h="60" s="1" n="1" w="1" r="0">1234567</BC39>：
         *  打印code39一维码，其中：
         *  属性 x 为水平方向起始点坐标（默认为0）
         *  属性 y 为垂直方向起始点坐标（默认为0）
         *          *  属性 h 为条形码高度（默认为48）
         *          *  属性 s 是否人眼可识：0 不可识，1 可识（默认为1）
         *          *  属性 n 为窄 bar 宽度，以点(dot)表示（默认为1）
         *          *  属性 w 为宽 bar 宽度，以点(dot)表示（默认为2）
         *          *  属性 r 为文字旋转角度(顺时针方向，默认为0)：
         *          *  0     0度
         *          *  90   90度
         *          *  180 180度
         *          *  270 270度
         *          *
         *          *  <QR x="20" y="20" w="160" e="H">二维码内容</QR>：
         *          *  打印二维码，其中：
         *          *  属性 x 为水平方向起始点坐标（默认为0）
         *          *  属性 y 为垂直方向起始点坐标（默认为0）
         *          *  属性 w 为二维码宽度（默认为160）
         *          *  属性 e 为纠错等级：L 7% M 15% Q 25% H 30%（默认为H）
         *          *  标签内容是二维码值, 最大不能超过256个字符
         *          *  注意：单个订单最多只能打印一个二维码
         * <IMG x="16" y="32" w="100">：
         * 打印LOGO图片，需登录开放平台在【打印机管理=》设备管理】下通过设置LOGO功能进行上传。此处直接
         * 写入空标签,若指定了<PAGE>标签，<IMG>标签应该放到<PAGE>标签里面， <IMG>, 如 <IMG>即可, 具
         * 体可参考样例。其中：
         *    * 属性 x 为水平方向起始点坐标（默认为0）
         *    * 属性 y 为垂直方向起始点坐标（默认为0）
         *    * 属性 w 为logo图片最大宽度（默认为50），最小值为20，最大值为100。logo图片的高度和宽度相等
         */
        StringBuilder printContent = new StringBuilder();

        //第一个标签
        printContent.append("<PAGE>")
                .append("<SIZE>40,30</SIZE>") //设定标签纸尺寸
                .append("<TEXT x=\"8\" y=\"8\" w=\"1\" h=\"1\" r=\"0\">")
                .append("#001").append(StringUtils.repeat(" ", 4))
                .append("一号桌").append(StringUtils.repeat(" ", 4))
                .append("1/3")
                .append("</TEXT>")
                .append("<TEX  " +
                        "+T x=\"8\" y=\"96\" w=\"2\" h=\"2\" r=\"0\">")
                .append("黄金炒饭")
                .append("</TEXT>")
                .append("<TEXT x=\"8\" y=\"200\" w=\"1\" h=\"1\" r=\"0\">")
                .append("王女士").append(StringUtils.repeat(" ", 4))
                .append("136****3388")
                .append("</TEXT>")
                .append("</PAGE>");
        //第二个标签
        printContent.append("<PAGE>")
                .append("<TEXT x=\"8\" y=\"8\" w=\"1\" h=\"1\" r=\"0\">")
                .append("#001").append(StringUtils.repeat(" ", 4))
                .append("一号桌").append(StringUtils.repeat(" ", 4))
                .append("2/3")
                .append("</TEXT>")
                .append("<TEXT x=\"8\" y=\"96\" w=\"2\" h=\"2\" r=\"0\">")
                .append("凉拌青瓜")
                .append("</TEXT>")
                .append("<TEXT x=\"8\" y=\"200\" w=\"1\" h=\"1\" r=\"0\">")
                .append("王女士").append(StringUtils.repeat(" ", 4))
                .append("136****3388")
                .append("</TEXT>")
                .append("</PAGE>");
        //第三个标签
        printContent.append("<PAGE>")
                .append("<TEXT x=\"8\" y=\"8\" w=\"1\" h=\"1\" r=\"0\">")
                .append("#001").append(StringUtils.repeat(" ", 4))
                .append("一号桌").append(StringUtils.repeat(" ", 4))
                .append("3/3")
                .append("</TEXT>")
                .append("<TEXT x=\"8\" y=\"96\" w=\"2\" h=\"2\" r=\"0\">")
                .append("老刘家肉夹馍")
                .append("</TEXT>")
                .append("<TEXT x=\"8\" y=\"200\" w=\"1\" h=\"1\" r=\"0\">")
                .append("王女士").append(StringUtils.repeat(" ", 4))
                .append("136****3388")
                .append("</TEXT>")
                .append("</PAGE>");
        //第四个标签 打印条形码
        printContent.append("<PAGE>")
                .append("<TEXT x=\"8\" y=\"8\" w=\"1\" h=\"1\" r=\"0\">")
                .append("打印条形码：")
                .append("</TEXT>")
                .append("<BC128 x=\"16\" y=\"32\" h=\"32\" s=\"1\" n=\"2\" w=\"2\" r=\"0\">")
                .append("12345678")
                .append("</BC128>")
                .append("</PAGE>");
        //第四个标签 打印二维码，宽度最小为128 低于128会无法扫描
        printContent.append("<PAGE>")
                .append("<TEXT x=\"8\" y=\"8\" w=\"1\" h=\"1\" r=\"0\">")
                .append("打印二维码宽度128：")
                .append("</TEXT>")
                .append("<QR x=\"16\" y=\"32\" w=\"128\">")
                .append("https://www.xpyun.net")
                .append("</QR>")
                .append("</PAGE>");

        PrintRequest request = new PrintRequest();
        Config.createRequestHeader(request);
        //*必填*：打印机编号
        request.setSn(Config.OK_PRINTER_SN);
        //*必填*：打印内容,不能超过12K
        request.setContent(printContent.toString());
        //打印份数，默认为1
        request.setCopies(1);
        //声音播放模式，0 为取消订单模式，1 为静音模式，2 为来单播放模式，3为有用户申请退单了。默认为 2 来单播放模式
        request.setVoice(2);
        //打印模式：
        //值为 0 或不指定则会检查打印机是否在线，如果不在线 则不生成打印订单，直接返回设备不在线状态码；如果在线则生成打印订单，并返回打印订单号。
        //值为 1不检查打印机是否在线，直接生成打印订单，并返回打印订单号。如果打印机不在线，订单将缓存在打印队列中，打印机正常在线时会自动打印。
        request.setMode(0);
        // 打印接口回调地址对应标识(取值范围 [ 1 - 5 ] 的整数)，对于web管理后台 “功能设置” 菜单设置界面的打印接口回调标识。
        // request.setBackurlFlag(1);

        String url = BASE_URL + "/xprinter/printLabel";
        String jsonRequest = JSON.toJSONString(request);
        String resp = HttpClientUtil.doPostJSON(url, jsonRequest);
        ObjectRestResponse<String> result = JSON.parseObject(resp, new TypeReference<ObjectRestResponse<String>>() {
        });
        //resp.data:正确返回订单编号
        System.out.println(resp);
    }

    public static void main(String[] args) throws Exception {
        printLabel();
    }
}
