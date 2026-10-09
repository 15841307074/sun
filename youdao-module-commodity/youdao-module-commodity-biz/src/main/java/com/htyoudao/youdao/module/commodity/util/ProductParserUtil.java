package com.htyoudao.youdao.module.commodity.util;

import com.htyoudao.youdao.module.commodity.dal.dto.otherorder.ProductQuantity;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ProductParserUtil {
    
    // 套餐模式匹配 - 用于识别套餐并忽略
    private static final Pattern COMBO_PATTERN = Pattern.compile(
        ".*(套餐|组合|件套|套装|套组|套餐[A-Z]|套餐[0-9]|店长推荐|Combo|Set).*",
        Pattern.CASE_INSENSITIVE
    );
    

    /**
     * 解析商品信息，处理套餐并聚合相同商品
     */
    public static List<ProductQuantity> parseProducts(String productInfo, ChannelType channelType) {
        if (StringUtils.isBlank(productInfo)) {
            return new ArrayList<>();
        }
        
        List<ProductQuantity> allProducts = new ArrayList<>();
        
        try {
            switch (channelType){
                case ELE_ME ->  allProducts = parseEleMeProducts(productInfo);
                case SAN_KUAI ->  allProducts = parseSanKuaiProducts(productInfo);
            }
            // 过滤掉套餐，只保留子品
            List<ProductQuantity> filteredProducts = filterOutCombos(allProducts);
            
            // 聚合相同商品
            return aggregateSameProducts(filteredProducts);
            
        } catch (Exception e) {
            log.warn("解析商品信息失败: {}", productInfo, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析饿了么商品信息
     */
    private static List<ProductQuantity> parseEleMeProducts(String productInfo) {
        List<ProductQuantity> products = new ArrayList<>();
        
        // 渠道A格式：智选套餐C[汉堡:板烧鸡肉卷,小吃2:薯条,小吃:霸王鸡架]_1*20.9
        // 可能有多个商品用换行分隔
        String[] productItems = productInfo.split("\\+");
        
        for (String item : productItems) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            
            // 提取商品名称和数量
            String[] parts = item.split("_");
            if (parts.length < 2) {
                continue;
            }
            
            String productName = parts[0].trim();
            
            // 提取数量：格式为 数量*单价
            String quantityPrice = parts[1];

            Integer quantity = null;
            try {
                quantity = Integer.valueOf(quantityPrice.split("\\*")[0]);
            } catch (Exception e) {
                quantity = 1;
                log.error("quantityPrice:{}", quantityPrice, e);
            }

            // 检查是否是套餐，如果是套餐则解析子品
            if (isComboProduct(productName)) {
                List<ProductQuantity> subProducts = parseComboSubProducts(productName, quantity);
                products.addAll(subProducts);
            } else {
                products.add(new ProductQuantity(productName, quantity));
            }
        }
        
        return products;
    }
    
    /**
     * 解析美团商品信息
     */
    private static List<ProductQuantity> parseSanKuaiProducts(String productInfo) {
        List<ProductQuantity> products = new ArrayList<>();
        
        // 渠道B格式：收藏门店享0.99大串1个(1个),单价0.99*数量1/香辣鸡腿堡(1人份),单价10.0*数量1
        // 商品用斜杠分隔
        String[] productItems = productInfo.split("[/+]");
        
        for (String item : productItems) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            
            // 提取商品名称
            String productName = extractChannelBProductName(item);
            
            // 提取数量
            Integer quantity = extractChannelBQuantity(item);
            
            if (quantity > 0 && StringUtils.isNotBlank(productName)) {
                // 检查是否是套餐，如果是套餐则解析子品
                if (isComboProduct(productName)) {
                    List<ProductQuantity> subProducts = parseComboSubProducts(item, quantity);
                    products.addAll(subProducts);
                } else {
                    products.add(new ProductQuantity(productName, quantity));
                }
            }
        }
        
        return products;
    }
    
    /**
     * 判断是否为套餐商品
     */
    private static boolean isComboProduct(String productName) {
        if (StringUtils.isBlank(productName)) {
            return false;
        }

        // 检查是否包含套餐关键词
        boolean isCombo = COMBO_PATTERN.matcher(productName).matches();

        return isCombo;
//
//        // 检查是否包含中括号（渠道A套餐格式）
//        boolean hasBrackets = productName.contains("[") && productName.contains("]");
//
//        // 检查是否包含冒号分隔（渠道A套餐子品格式）
//        boolean hasColonSeparator = productName.contains(":") && productName.contains(",");
        
//        return isCombo || hasBrackets || hasColonSeparator;
    }
    
    /**
     * 解析套餐中的子品
     */
    private static List<ProductQuantity> parseComboSubProducts(String comboName, Integer comboQuantity) {
        List<ProductQuantity> subProducts = new ArrayList<>();
        
        log.debug("解析套餐: {}，数量: {}", comboName, comboQuantity);
        
        // 尝试从套餐名称中提取子品信息
        // 格式1: 智选套餐C[汉堡:板烧鸡肉卷,小吃2:薯条,小吃:霸王鸡架]
        // 格式2: 店长推荐B(1人份,香辣鸡腿堡,大串,可乐)
        
        // 提取中括号内的内容
        String subProductsText = extractBracketContent(comboName);
        if (StringUtils.isBlank(subProductsText)) {
            // 如果没有中括号，尝试提取括号内的内容
            subProductsText = extractParenthesisContent(comboName);
        }
        
        if (StringUtils.isNotBlank(subProductsText)) {
            // 解析子品列表
            List<String> subProductNames = parseSubProductList(subProductsText);
            for (String subProductName : subProductNames) {
                if (StringUtils.isNotBlank(subProductName)) {
                    // 每个子品的数量等于套餐数量（通常一个套餐包含一个子品）
                    subProducts.add(new ProductQuantity(subProductName, comboQuantity));
                }
            }
        } else {
            // 如果无法解析子品，记录警告
            log.warn("无法解析套餐子品: {}", comboName);
        }
        
        return subProducts;
    }
    
    /**
     * 提取中括号内容
     */
    private static String extractBracketContent(String text) {
        Pattern pattern = Pattern.compile("\\[([^]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 提取括号内容
     */
    private static String extractParenthesisContent(String text) {
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 解析子品列表
     */
    private static List<String> parseSubProductList(String subProductsText) {
        List<String> subProducts = new ArrayList<>();
        
        // 多种分隔符：逗号、顿号、斜杠等
        String[] items = subProductsText.split("[,、/]");
        
        for (String item : items) {
            if (StringUtils.isBlank(item) || item.equals("1人份")) {
                continue;
            }
            
            // 清理子品名称
            String cleanedName = cleanSubProductName(item.trim());
            if (StringUtils.isNotBlank(cleanedName)) {
                subProducts.add(cleanedName);
            }
        }
        
        return subProducts;
    }
    
    /**
     * 清理子品名称
     */
    private static String cleanSubProductName(String rawName) {
        // 移除数量描述：如(1人份)、(1个)等
        String cleaned = rawName.replaceAll("\\([^)]*\\)", "");
        
        // 移除类别前缀：如"汉堡:"、"小吃:"等
        cleaned = cleaned.replaceAll("^[^:]+:", "");
        
        // 移除数字后缀：如"小吃2" -> "小吃"
//        cleaned = cleaned.replaceAll("\\d+$", "");
        
        // 移除多余空格
        cleaned = cleaned.trim();
        
        return cleaned;
    }
    
    /**
     * 过滤掉套餐，只保留子品
     */
    private static List<ProductQuantity> filterOutCombos(List<ProductQuantity> products) {
        return products.stream()
            .filter(pq -> !isComboProduct(pq.getProductName()))
            .collect(Collectors.toList());
    }
    
    /**
     * 聚合相同商品
     */
    public static List<ProductQuantity> aggregateSameProducts(List<ProductQuantity> products) {
        Map<String, Integer> productQuantityMap = new HashMap<>();
        
        for (ProductQuantity pq : products) {
            String normalizedName = normalizeProductName(pq.getProductName());
            productQuantityMap.merge(normalizedName, pq.getQuantity(), Integer::sum);
        }
        
        return productQuantityMap.entrySet().stream()
            .map(entry -> new ProductQuantity(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * 标准化商品名称（用于聚合）
     */
    private static String normalizeProductName(String productName) {
        if (StringUtils.isBlank(productName)) {
            return "";
        }
        
        // 移除可能的规格、单位等信息
        String normalized = productName
//            .replaceAll("\\[.*?\\]", "")      // 移除[]内容
//            .replaceAll("\\(.*?\\)", "")      // 移除()内容
//            .replaceAll("_.*", "")            // 移除_后面内容
//            .replaceAll("\\d+个", "")         // 移除数量描述
//            .replaceAll("\\d+份", "")
//            .replaceAll("\\d+人份", "")
//            .replaceAll("\\d+元", "")         // 移除价格
//            .replaceAll("[\\d.]+", "")        // 移除所有数字
            .trim();
        
        // 如果清理后为空，返回原始名称
        return StringUtils.isNotBlank(normalized) ? normalized : productName.trim();
    }
    
    // 以下是从原有代码迁移的辅助方法

    private static String extractChannelBProductName(String item) {
        // 格式：收藏门店享0.99大串1个(1个),单价0.99*数量1
        // 或者：香辣鸡腿堡(1人份),单价10.0*数量1

        int commaIndex = item.indexOf(',');
        int bracketIndex = item.indexOf('(');

        int endIndex = commaIndex;
        if (bracketIndex > 0 && (commaIndex == -1 || bracketIndex < commaIndex)) {
            endIndex = bracketIndex;
        }

        if (endIndex > 0) {
            return item.substring(0, endIndex).trim();
        }

        return item.trim();
    }

    private static Integer extractChannelBQuantity(String item) {
        // 查找"数量X"模式
        Pattern pattern = Pattern.compile("数量(\\d+)");
        Matcher matcher = pattern.matcher(item);
        
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("解析数量失败: {}", item);
            }
        }
        
        // 尝试从商品名称中提取数量
        Pattern namePattern = Pattern.compile("(\\d+)(?:个|份|人份)");
        Matcher nameMatcher = namePattern.matcher(item);
        if (nameMatcher.find()) {
            try {
                return Integer.parseInt(nameMatcher.group(1));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        return 1; // 默认数量为1
    }
    

}