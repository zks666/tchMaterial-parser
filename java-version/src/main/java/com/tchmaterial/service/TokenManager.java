package com.tchmaterial.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tchmaterial.util.SystemUtils;

import java.io.*;
import java.util.prefs.Preferences;

/**
 * Access Token管理器
 */
public class TokenManager {
    
    private static final String TOKEN_KEY = "AccessToken";
    private static final String CONFIG_FILE = "data.json";
    
    private String accessToken;
    
    /**
     * 加载本地存储的Access Token
     */
    public void loadAccessToken() {
        try {
            if (SystemUtils.isWindows()) {
                // Windows使用注册表
                Preferences prefs = Preferences.userRoot().node("Software/tchMaterial-parser");
                accessToken = prefs.get(TOKEN_KEY, null);
            } else {
                // Linux和macOS使用JSON文件
                File configFile = new File(SystemUtils.getConfigDir(), CONFIG_FILE);
                if (configFile.exists()) {
                    try (FileReader reader = new FileReader(configFile)) {
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                        if (jsonObject != null && jsonObject.has("access_token")) {
                            accessToken = jsonObject.get("access_token").getAsString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 读取失败则不做处理
            accessToken = null;
        }
    }
    
    /**
     * 设置并保存Access Token
     */
    public String setAccessToken(String token) {
        this.accessToken = token;
        
        try {
            if (SystemUtils.isWindows()) {
                // Windows保存到注册表
                Preferences prefs = Preferences.userRoot().node("Software/tchMaterial-parser");
                prefs.put(TOKEN_KEY, token);
                prefs.flush();
                return "Access Token 已保存！\n已写入注册表：HKEY_CURRENT_USER\\Software\\tchMaterial-parser\\AccessToken";
            } else {
                // Linux和macOS保存到JSON文件
                File configFile = new File(SystemUtils.getConfigDir(), CONFIG_FILE);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("access_token", token);
                
                try (FileWriter writer = new FileWriter(configFile)) {
                    Gson gson = new Gson();
                    gson.toJson(jsonObject, writer);
                }
                
                if (SystemUtils.isMacOS()) {
                    return "Access Token 已保存！\n已写入文件：~/Library/Application Support/tchMaterial-parser/data.json";
                } else {
                    return "Access Token 已保存！\n已写入文件：~/.config/tchMaterial-parser/data.json";
                }
            }
        } catch (Exception e) {
            return "Access Token 已保存！";
        }
    }
    
    /**
     * 获取Access Token
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * 检查是否有有效的Access Token
     */
    public boolean hasValidToken() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }
}