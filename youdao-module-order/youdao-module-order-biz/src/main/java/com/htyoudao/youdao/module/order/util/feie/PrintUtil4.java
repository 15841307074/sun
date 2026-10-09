package com.htyoudao.youdao.module.order.util.feie;

import jodd.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("static-access")
public class PrintUtil4 {
    private static final String itemInfosStart = "${itemInfosSizeStart}${itemInfosBlodStart}";
    private static final String itemInfosEnd = "${itemInfosSizeEnd}${itemInfosBlodEnd}";
    private static final String itemInfosDetailStart = "${itemInfoDetailSizeStart}${itemInfosDetailBlodStart}";
    private static final String itemInfosDetailEnd = "${itemInfoDetailSizeEnd}${itemInfosDetailBlodEnd}";
    private static final String itemInfosActivityStart = "${otherSizeStart}${otherBoldStart}";
    private static final String itemInfosActivityEnd = "${otherBoldStart}${otherBoldEnd}";
    public static void main(String[] args) {
//    ======================1.多个打印机同时打印======================================
//    List<String> list = new ArrayList<>();
//    list.add(SN);
//    list.add(SN2);
//    for (String sn : list) {
//      String method1 = p.print(sn);
//      System.out.println(method1);
//    }

//    ======================2单个打印机打印======================================
//    String result = p.print(SN2);
//    System.out.println(result);

//    p.writeFile("E:/retlog.txt", result);
//    System.out.println("返回json数据已保存至 E:/retlog.txt 文件,有需要请查看");

    }

    // =====================打印订单排版Demo==========================
    public void writeFile(String path, String content) {
        content = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(new Date()) + ",保存的订单日志信息为: " + content;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path, true);
            fos.write(content.getBytes());
            fos.write("\r<BR>".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String signature(String USER, String UKEY, String STIME) {
        return DigestUtils.sha1Hex(USER + UKEY + STIME);
    }


    //飞鹅技术支持
    //#########################################################################################################

    //进行订单的多列排版demo，实现商品超出字数的自动换下一行对齐处理，同时保持各列进行对齐

    //排版原理是统计字符串字节数，补空格换行处理

    //58mm的机器,一行打印16个汉字,32个字母;80mm的机器,一行打印24个汉字,48个字母

    //#########################################################################################################

    //orderList为数组  b1代表名称列占用字节  b2单价列 b3数量列 b4金额列-->这里的字节数可按自己需求自由改写，详细往上看112行调用实际例子运用
    public static String getOrder(List<Order> orderList, int b1, int b4, int b3, int b2) {
        String orderInfo = "";
        double totals = 0.0;
        for (int i = 0; i < orderList.size(); i++) {
            String titleStart = "";
            String titleEnd = "";
            if (i > 0) {
                titleStart = orderList.get(i - 1).getTitle();
            }
            if (i < orderList.size() - 1) {
                titleEnd = orderList.get(i + 1).getTitle();
            }
            String title = orderList.get(i).getTitle();
            String num = orderList.get(i).getPrice();
            String price = orderList.get(i).getNum();
            Boolean isBig = orderList.get(i).getIsBig();
            String total = "";
            if (title.contains("@")) {
                String[] titleArr = title.split("\\@");
                title = titleArr[0];
                num = titleArr[1];
            }
            String otherStr = "";
            if (StringUtil.isNotBlank(total)) {
                total = addSpace(total, b4);
                otherStr += total;
            }

            if (StringUtil.isNotBlank(num)) {
                if (StringUtil.isNotBlank(price)) {
                    num = addSpace(num + "份", b3);
                    if (isBig) {
                        otherStr += "" + num+"" + price;
                    } else {
                        otherStr += "  " + num+"  " + price;
                    }
                } else {
                    num = addSpace(num + "份", b3);
                    if (isBig) {
                        otherStr += "       " + num;
                    } else {
                        otherStr += "         " + num;
                    }

                }
            }
            if ( StringUtil.isNotBlank(price)&&!StringUtil.isNotBlank(num)) {
                price = addSpace(price, b2);
                if (isBig) {
                    otherStr += "     " + price;
                } else {
                    otherStr += "       " + price;
                }

            }
            int tl = 0;
            try {
                tl = title.getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            int spaceNum = (tl / b1 + 1) * b1 - tl;
            if (spaceNum > 2) {
                spaceNum = spaceNum - 1;
            }
            if (tl < b1) {
                if (isBig) {
                    if (title.contains("-")||(title.contains("[")&&!title.contains("[满赠]"))){
                        title = title + "      " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = title + "<BR>      " + otherStr;// 添加 单价 数量 总额
                    }
                } else {
                    for (int k = 0; k < spaceNum; k++) {
                        title += " ";
                    }
                    title += otherStr;
                }
            } else if (tl == b1) {
                if (isBig) {
                    if (title.contains("-")||(title.contains("[")&&!title.contains("[满赠]"))){
                        title = title + "      " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = title + "<BR>       " + otherStr;// 添加 单价 数量 总额
                    }

                } else {
                    title += otherStr;
                }
            } else {
                List<String> list = getStrList(title, b1);
                String s0 = titleAddSpace(list.get(0));
                String s = s0;
                for (int k = 1; k < list.size(); k++) {
                    s += list.get(k);
                }
                try {
                    //    s = getStringByEnter(b1, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                title += s;
                if (isBig) {
                    title = s + "              " + otherStr;// 添加 单价 数量 总额
                } else {
                    if (title.contains("-")||(title.contains("[")&&!title.contains("[满赠]"))){
                        title = s + "              " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = s + "<BR>               " + otherStr;// 添加 单价 数量 总额
                    }

                }
            }
            if (title.contains("-")||(title.contains("[")&&!title.contains("[满赠]"))) {
                if (isBig) {
                    if (!titleStart.contains("-")) {
                        orderInfo += itemInfosDetailStart + " " + title.trim()+ "<BR>";
                    }
                    if (titleStart.contains("-")&&!titleEnd.contains("-")) {
                        orderInfo += " " + title.trim() + itemInfosDetailEnd+ "<BR>"+ "<BR>";
                    }
                    if (titleStart.contains("-") && titleEnd.contains("-")) {
                        orderInfo += " " + title.trim() + "<BR>";
                    }
                } else {
                    orderInfo += itemInfosDetailStart +" " + title + itemInfosDetailEnd+ "<BR>";
                }

            } else {
                if (i!=0){
                orderInfo +=  "<BR>"+ itemInfosStart + title + itemInfosEnd + "<BR>";
                }else{
                    orderInfo += itemInfosStart + title + itemInfosEnd + "<BR>";
                }
            }
        }
        return orderInfo;
    }
    public static String getActivityOrder(List<Order> orderList, int b1, int b4, int b3, int b2) {
        String orderInfo = "";
        double totals = 0.0;
        for (int i = 0; i < orderList.size(); i++) {
            String titleStart = "";
            String titleEnd = "";
            if (i > 0) {
                titleStart = orderList.get(i - 1).getTitle();
            }
            if (i < orderList.size() - 1) {
                titleEnd = orderList.get(i + 1).getTitle();
            }
            String title = orderList.get(i).getTitle();
            String num = orderList.get(i).getPrice();
            String price = orderList.get(i).getNum();
            Boolean isBig = orderList.get(i).getIsBig();
            String total = "";
            if (title.contains("@")) {
                String[] titleArr = title.split("\\@");
                title = titleArr[0];
                num = titleArr[1];
            }
            String otherStr = "";
            if (StringUtil.isNotBlank(total)) {
                total = addSpace(total, b4);
                otherStr += total;
            }

            if (StringUtil.isNotBlank(num)) {
                if (StringUtil.isNotBlank(price)) {
                    num = addSpace(num + "份", b3);
                    if (isBig) {
                        otherStr += "";
                    } else {
                        otherStr += " ";
                    }
                } else {
                    num = addSpace(num + "份", b3);
                    if (isBig) {
                        otherStr += "       " ;
                    } else {
                        otherStr += "         " ;
                    }

                }
            }
            if (StringUtil.isNotBlank(num) && StringUtil.isNotBlank(price)) {
                price = addSpace(price, b2);
                if (isBig) {
                    otherStr += "  " + price;
                } else {
                    otherStr += "      " + price;
                }

            }
            int tl = 0;
            try {
                tl = title.getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            int spaceNum = (tl / b1 + 1) * b1 - tl;
            if (spaceNum > 2) {
                spaceNum = spaceNum - 1;
            }
            if (tl < b1) {
                if (isBig) {
                    if (title.contains("-")){
                        title = title + "      " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = title + "<BR>      " + otherStr;// 添加 单价 数量 总额
                    }
                } else {
                    for (int k = 0; k < spaceNum; k++) {
                        title += " ";
                    }
                    title += otherStr;
                }
            } else if (tl == b1) {
                if (isBig) {
                    if (title.contains("-")){
                        title = title + "      " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = title + "<BR>       " + otherStr;// 添加 单价 数量 总额
                    }

                } else {
                    title += otherStr;
                }
            } else {
                List<String> list = getStrList(title, b1);
                String s0 = titleAddSpace(list.get(0));
                String s = s0;
                for (int k = 1; k < list.size(); k++) {
                    s += list.get(k);
                }
                try {
                    //    s = getStringByEnter(b1, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                title += s;
                if (isBig) {
                    title = s + "              " + otherStr;// 添加 单价 数量 总额
                } else {
                    if (title.contains("-")||title.contains("[")){
                        title = s + "              " + otherStr;// 添加 单价 数量 总额
                    }else{
                        title = s + "<BR>                " + otherStr;// 添加 单价 数量 总额
                    }

                }
            }
            if (title.contains("-")&&!price.contains("-")) {
                if (isBig) {
                    if (!titleStart.contains("-")) {
                        orderInfo += itemInfosDetailStart + " " + title.trim()+ "<BR>";
                    }
                    if (titleStart.contains("-")&&!titleEnd.contains("-")) {
                        orderInfo += " " + title.trim() + itemInfosDetailEnd+ "<BR>"+ "<BR>";
                    }
                    if (titleStart.contains("-") && titleEnd.contains("-")) {
                        orderInfo += " " + title.trim() + "<BR>";
                    }
                } else {
                    orderInfo += itemInfosDetailStart +" " + title + itemInfosDetailEnd+ "<BR>";
                }

            } else {
                orderInfo += itemInfosActivityStart + title + itemInfosActivityEnd + "<BR>";
            }
        }
        return orderInfo;
    }
    public static String titleAddSpace(String str) {
        int k = 0;
        int b = 14;
        try {
            k = str.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < b - k; i++) {
            str += " ";
        }
        return str;
    }

    public static String getStringByEnter(int length, String string) throws Exception {
        for (int i = 1; i <= string.length(); i++) {
            if (string.substring(0, i).getBytes("GBK").length > length) {
                return string.substring(0, i - 1) + "<BR>" + getStringByEnter(length, string.substring(i - 1));
            }
        }
        return string;
    }

    public static String addSpace(String str, int size) {
        int len = str.length();
        if (len < size) {
            for (int i = 0; i < size - len; i++) {
                str += " ";
            }
        }
        return str;
    }

    public static Boolean isEn(String str) {
        Boolean b = false;
        try {
            b = str.getBytes("GBK").length == str.length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    public static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length, (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static void close(CloseableHttpResponse response, HttpPost post, CloseableHttpClient httpClient) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            post.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}