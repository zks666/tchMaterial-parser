package com.tchmaterial.model;

/**
 * 资源信息模型
 */
public class ResourceInfo {
    private String resourceUrl;
    private String contentId;
    private String title;
    
    public ResourceInfo(String resourceUrl, String contentId, String title) {
        this.resourceUrl = resourceUrl;
        this.contentId = contentId;
        this.title = title;
    }
    
    public String getResourceUrl() {
        return resourceUrl;
    }
    
    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
    
    public String getContentId() {
        return contentId;
    }
    
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isValid() {
        return resourceUrl != null && !resourceUrl.isEmpty();
    }
}