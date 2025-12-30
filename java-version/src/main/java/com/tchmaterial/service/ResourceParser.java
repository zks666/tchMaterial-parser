package com.tchmaterial.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tchmaterial.model.ResourceInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源解析器
 */
public class ResourceParser {
    
    private final OkHttpClient httpClient;
    private final TokenManager tokenManager;
    private final Gson gson;
    
    public ResourceParser(OkHttpClient httpClient, TokenManager tokenManager) {
        this.httpClient = httpClient;
        this.tokenManager = tokenManager;
        this.gson = new Gson();
    }
    
    /**
     * 解析URL获取资源信息
     */
    public ResourceInfo parse(String url) {
        try {
            String contentId = extractContentId(url);
            if (contentId == null) {
                return new ResourceInfo(null, null, null);
            }
            
            String contentType = extractContentType(url);
            if (contentType == null) {
                contentType = "assets_document";
            }
            
            String apiUrl = buildApiUrl(url, contentId, contentType);
            JsonObject data = fetchResourceData(apiUrl);
            
            if (data == null) {
                return new ResourceInfo(null, null, null);
            }
            
            String resourceUrl = extractResourceUrl(data, contentType, contentId);
            String title = data.has("title") ? data.get("title").getAsString() : null;
            
            return new ResourceInfo(resourceUrl, contentId, title);
            
        } catch (Exception e) {
            return new ResourceInfo(null, null, null);
        }
    }
    
    /**
     * 从URL中提取contentId
     */
    private String extractContentId(String url) {
        Pattern pattern = Pattern.compile("[?&]contentId=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * 从URL中提取contentType
     */
    private String extractContentType(String url) {
        Pattern pattern = Pattern.compile("[?&]contentType=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * 构建API URL
     */
    private String buildApiUrl(String originalUrl, String contentId, String contentType) {
        if (originalUrl.contains("syncClassroom/basicWork/detail")) {
            return "https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/special_edu/resources/details/" + contentId + ".json";
        } else if ("thematic_course".equals(contentType)) {
            return "https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/special_edu/resources/details/" + contentId + ".json";
        } else {
            return "https://s-file-1.ykt.cbern.com.cn/zxx/ndrv2/resources/tch_material/details/" + contentId + ".json";
        }
    }
    
    /**
     * 获取资源数据
     */
    private JsonObject fetchResourceData(String apiUrl) {
        try {
            Request.Builder requestBuilder = new Request.Builder().url(apiUrl);
            
            // 添加认证头
            if (tokenManager.hasValidToken()) {
                String authHeader = String.format("MAC id=\"%s\",nonce=\"0\",mac=\"0\"", 
                    tokenManager.getAccessToken());
                requestBuilder.addHeader("X-ND-AUTH", authHeader);
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return gson.fromJson(responseBody, JsonObject.class);
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    /**
     * 从数据中提取资源URL
     */
    private String extractResourceUrl(JsonObject data, String contentType, String contentId) {
        String resourceUrl = null;
        
        // 查找PDF资源
        if (data.has("ti_items")) {
            JsonArray tiItems = data.getAsJsonArray("ti_items");
            for (JsonElement item : tiItems) {
                JsonObject itemObj = item.getAsJsonObject();
                if (itemObj.has("lc_ti_format") && "pdf".equals(itemObj.get("lc_ti_format").getAsString())) {
                    if (itemObj.has("ti_storages")) {
                        JsonArray storages = itemObj.getAsJsonArray("ti_storages");
                        if (storages.size() > 0) {
                            resourceUrl = storages.get(0).getAsString();
                            break;
                        }
                    }
                }
            }
        }
        
        // 如果没找到资源且是专题课程，尝试获取资源列表
        if (resourceUrl == null && "thematic_course".equals(contentType)) {
            resourceUrl = fetchThematicCourseResource(contentId);
        }
        
        // 如果没有token，尝试构造直接下载URL
        if (resourceUrl != null && !tokenManager.hasValidToken()) {
            resourceUrl = convertToDirectUrl(resourceUrl);
        }
        
        return resourceUrl;
    }
    
    /**
     * 获取专题课程资源
     */
    private String fetchThematicCourseResource(String contentId) {
        try {
            String apiUrl = "https://s-file-1.ykt.cbern.com.cn/zxx/ndrs/special_edu/thematic_course/" + 
                           contentId + "/resources/list.json";
            
            JsonObject resourcesData = fetchResourceData(apiUrl);
            if (resourcesData != null) {
                JsonArray resources = resourcesData.getAsJsonArray();
                for (JsonElement resource : resources) {
                    JsonObject resourceObj = resource.getAsJsonObject();
                    if (resourceObj.has("resource_type_code") && 
                        "assets_document".equals(resourceObj.get("resource_type_code").getAsString())) {
                        
                        if (resourceObj.has("ti_items")) {
                            JsonArray tiItems = resourceObj.getAsJsonArray("ti_items");
                            for (JsonElement item : tiItems) {
                                JsonObject itemObj = item.getAsJsonObject();
                                if (itemObj.has("lc_ti_format") && "pdf".equals(itemObj.get("lc_ti_format").getAsString())) {
                                    if (itemObj.has("ti_storages")) {
                                        JsonArray storages = itemObj.getAsJsonArray("ti_storages");
                                        if (storages.size() > 0) {
                                            return storages.get(0).getAsString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
    
    /**
     * 转换为直接下载URL（未登录时使用）
     */
    private String convertToDirectUrl(String originalUrl) {
        Pattern pattern = Pattern.compile("^https?://(?:.+)\\.ykt\\.cbern\\.com\\.cn/(.+)/([\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12})\\.pkg/(.+)\\.pdf$");
        Matcher matcher = pattern.matcher(originalUrl);
        
        if (matcher.find()) {
            return String.format("https://c1.ykt.cbern.com.cn/%s/%s.pkg/%s.pdf", 
                matcher.group(1), matcher.group(2), matcher.group(3));
        }
        
        return originalUrl;
    }
}