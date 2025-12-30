package com.tchmaterial.model;

/**
 * 下载状态模型
 */
public class DownloadState {
    private String downloadUrl;
    private String savePath;
    private long downloadedSize;
    private long totalSize;
    private boolean finished;
    private String failedReason;
    
    public DownloadState(String downloadUrl, String savePath) {
        this.downloadUrl = downloadUrl;
        this.savePath = savePath;
        this.downloadedSize = 0;
        this.totalSize = 0;
        this.finished = false;
        this.failedReason = null;
    }
    
    // Getters and Setters
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getSavePath() {
        return savePath;
    }
    
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
    
    public long getDownloadedSize() {
        return downloadedSize;
    }
    
    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    
    public String getFailedReason() {
        return failedReason;
    }
    
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }
    
    public boolean isFailed() {
        return failedReason != null;
    }
}