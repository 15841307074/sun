package com.htyoudao.youdao.module.system.util.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * 导入数据处理工具类
 * @author dht
 */
public class ImportDataProcessor {
    
    /**
     * 处理部门字段
     */
    public static String processDepartmentField(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        String trimmedInput = input.trim();
        
        // 1. 优先判断是否包含"总经办"
        if (containsZongJingBan(trimmedInput)) {
            return "总经办";
        }
        
        // 2. 按照分号截取第一个部分
        String firstPart = getFirstPart(trimmedInput);
        if (firstPart == null) {
            return null;
        }
        
        // 3. 按照斜杠截取最后的部门名称
        return getLastPartAfterSlash(firstPart);
    }
    
    /**
     * 判断是否包含总经办（支持多种可能写法）
     */
    private static boolean containsZongJingBan(String input) {
        if (input == null) return false;
        
        // 支持的写法：总经办、总经办办公室、总经理办公室等
        String[] zongJingBanKeywords = {"总经办", "总经理办公室", "总裁办公室"};
        
        for (String keyword : zongJingBanKeywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 按分号截取第一个部分
     */
    private static String getFirstPart(String input) {
        if (input == null) return null;
        
        String[] parts = input.split(";");
        if (parts.length == 0) {
            return null;
        }
        
        String firstPart = parts[0].trim();
        return firstPart.isEmpty() ? null : firstPart;
    }
    
    /**
     * 按斜杠截取最后的部分
     */
    private static String getLastPartAfterSlash(String input) {
        if (input == null) return null;
        
        String[] parts = input.split("/");
        if (parts.length == 0) {
            return input;
        }
        
        String lastPart = parts[parts.length - 1].trim();
        return lastPart.isEmpty() ? input : lastPart;
    }
    
    /**
     * 批量处理部门字段
     */
    public static List<String> processDepartmentFields(List<String> inputs) {
        List<String> results = new ArrayList<>();
        
        if (inputs == null) {
            return results;
        }
        
        for (String input : inputs) {
            String result = processDepartmentField(input);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 获取所有部门列表（不处理，直接按分号分割）
     */
    public static List<String> getAllDepartments(String input) {
        List<String> departments = new ArrayList<>();
        
        if (input == null || input.trim().isEmpty()) {
            return departments;
        }
        
        String[] parts = input.split(";");
        for (String part : parts) {
            String department = getLastPartAfterSlash(part.trim());
            if (department != null && !department.isEmpty()) {
                departments.add(department);
            }
        }
        
        return departments;
    }

//    /**
//     * 测试方法
//     */
//    public static void main(String[] args) {
//        // 测试用例
//        String[] testCases = {
//            "零零玖零/财务部;零零玖零/总经办;零零玖零/浪大勺",
//            "零零玖零/技术部;零零玖零/市场部",
//            "零零玖零/总经办",
//            "财务部;总经办;人事部",
//            "零零玖零/财务部",
//            "单一部门",
//            "",
//            null
//        };
//
//        System.out.println("=== 部门字段处理测试 ===");
//        for (String testCase : testCases) {
//            String result = processDepartmentField(testCase);
//            System.out.printf("输入: %-40s -> 输出: %s%n",
//                testCase == null ? "null" : testCase,
//                result);
//        }
//
//        System.out.println("\n=== 获取所有部门测试 ===");
//        String multiDept = "零零玖零/财务部;零零玖零/总经办;零零玖零/浪大勺";
//        List<String> allDepts = getAllDepartments(multiDept);
//        System.out.printf("输入: %s%n", multiDept);
//        System.out.printf("所有部门: %s%n", allDepts);
//    }
}