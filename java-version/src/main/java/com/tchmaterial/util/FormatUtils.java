package com.tchmaterial.util;

/**
 * 格式化工具类
 */
public class FormatUtils {
    
    /**
     * 格式化字节大小
     */
    public static String formatBytes(long size) {
        if (size < 0) {
            return "0 字节";
        }
        
        String[] units = {"字节", "KB", "MB", "GB", "TB"};
        double sizeDouble = size;
        int unitIndex = 0;
        
        while (sizeDouble >= 1024.0 && unitIndex < units.length - 1) {
            sizeDouble /= 1024.0;
            unitIndex++;
        }
        
        if (unitIndex == 0) {
            return String.format("%.0f %s", sizeDouble, units[unitIndex]);
        } else {
            return String.format("%.1f %s", sizeDouble, units[unitIndex]);
        }
    }
    
    /**
     * 格式化百分比
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.2f%%", percentage);
    }
}