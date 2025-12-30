package com.tchmaterial.util;

import java.awt.*;
import java.io.File;

/**
 * 系统工具类
 */
public class SystemUtils {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    /**
     * 获取操作系统类型
     */
    public static String getOSName() {
        if (OS_NAME.contains("win")) {
            return "Windows";
        } else if (OS_NAME.contains("mac")) {
            return "Darwin";
        } else if (OS_NAME.contains("nix") || OS_NAME.contains("nux")) {
            return "Linux";
        }
        return "Unknown";
    }
    
    /**
     * 是否为Windows系统
     */
    public static boolean isWindows() {
        return "Windows".equals(getOSName());
    }
    
    /**
     * 是否为macOS系统
     */
    public static boolean isMacOS() {
        return "Darwin".equals(getOSName());
    }
    
    /**
     * 是否为Linux系统
     */
    public static boolean isLinux() {
        return "Linux".equals(getOSName());
    }
    
    /**
     * 获取缩放因子
     */
    public static double getScaleFactor() {
        try {
            if (isWindows()) {
                // Windows下获取DPI缩放
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                int dpi = toolkit.getScreenResolution();
                return Math.round((dpi / 96.0) * 100.0) / 100.0;
            } else {
                // 其他系统使用默认方法
                return 1.0; // Java 8兼容性，简化处理
            }
        } catch (Exception e) {
            return 1.0;
        }
    }
    
    /**
     * 获取用户主目录
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }
    
    /**
     * 获取配置文件目录
     */
    public static File getConfigDir() {
        String configPath;
        if (isWindows()) {
            // Windows使用注册表，这里返回临时目录
            configPath = System.getProperty("java.io.tmpdir") + File.separator + "tchMaterial-parser";
        } else if (isMacOS()) {
            configPath = getUserHome() + File.separator + "Library" + File.separator + 
                        "Application Support" + File.separator + "tchMaterial-parser";
        } else {
            // Linux
            configPath = getUserHome() + File.separator + ".config" + File.separator + "tchMaterial-parser";
        }
        
        File configDir = new File(configPath);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return configDir;
    }
}