package com.tchmaterial.service;

import com.tchmaterial.model.DownloadState;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 下载管理器
 */
public class DownloadManager {
    
    private final OkHttpClient httpClient;
    private final TokenManager tokenManager;
    private final ExecutorService executorService;
    private final List<DownloadState> downloadStates;
    
    public DownloadManager(OkHttpClient httpClient, TokenManager tokenManager) {
        this.httpClient = httpClient;
        this.tokenManager = tokenManager;
        this.executorService = Executors.newCachedThreadPool();
        this.downloadStates = new ArrayList<>();
    }
    
    /**
     * 下载文件
     */
    public CompletableFuture<Void> downloadFile(String url, String savePath, Consumer<DownloadProgress> progressCallback) {
        DownloadState state = new DownloadState(url, savePath);
        downloadStates.add(state);
        
        return CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Request.Builder requestBuilder = new Request.Builder().url(url);
                    
                    // 添加认证头
                    if (tokenManager.hasValidToken()) {
                        String authHeader = String.format("MAC id=\"%s\",nonce=\"0\",mac=\"0\"", 
                            tokenManager.getAccessToken());
                        requestBuilder.addHeader("X-ND-AUTH", authHeader);
                    }
                    
                    Request request = requestBuilder.build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            String errorMsg = "服务器返回 HTTP 状态码 " + response.code();
                            if (response.code() == 401 || response.code() == 403) {
                                errorMsg += "，Access Token 可能已过期或无效，请重新设置";
                            }
                            state.setFailedReason(errorMsg);
                            state.setFinished(true);
                            return;
                        }
                        
                        ResponseBody body = response.body();
                        if (body == null) {
                            state.setFailedReason("响应体为空");
                            state.setFinished(true);
                            return;
                        }
                        
                        long contentLength = body.contentLength();
                        state.setTotalSize(contentLength);
                        
                        File file = new File(savePath);
                        file.getParentFile().mkdirs();
                        
                        try (InputStream inputStream = body.byteStream();
                             FileOutputStream outputStream = new FileOutputStream(file)) {
                            
                            byte[] buffer = new byte[131072]; // 128KB buffer
                            int bytesRead;
                            
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                state.setDownloadedSize(state.getDownloadedSize() + bytesRead);
                                
                                // 更新进度
                                if (progressCallback != null) {
                                    DownloadProgress progress = calculateProgress();
                                    progressCallback.accept(progress);
                                }
                            }
                        }
                        
                        state.setDownloadedSize(state.getTotalSize());
                        state.setFinished(true);
                        
                    } catch (IOException e) {
                        state.setFailedReason(e.getMessage());
                        state.setFinished(true);
                    }
                    
                } catch (Exception e) {
                    state.setFailedReason(e.getMessage());
                    state.setFinished(true);
                }
                
                // 最终进度更新
                if (progressCallback != null) {
                    DownloadProgress progress = calculateProgress();
                    progressCallback.accept(progress);
                }
            }
        }, executorService);
    }
    
    /**
     * 计算总体下载进度
     */
    private DownloadProgress calculateProgress() {
        long allDownloadedSize = 0;
        long allTotalSize = 0;
        int downloadedNumber = 0;
        
        for (DownloadState state : downloadStates) {
            allDownloadedSize += state.getDownloadedSize();
            allTotalSize += state.getTotalSize();
            if (state.isFinished()) {
                downloadedNumber++;
            }
        }
        
        int totalNumber = downloadStates.size();
        double percentage = allTotalSize > 0 ? (double) allDownloadedSize / allTotalSize * 100 : 0;
        
        return new DownloadProgress(allDownloadedSize, allTotalSize, percentage, downloadedNumber, totalNumber);
    }
    
    /**
     * 获取失败的下载状态
     */
    public List<DownloadState> getFailedDownloads() {
        List<DownloadState> failedDownloads = new ArrayList<>();
        for (DownloadState state : downloadStates) {
            if (state.isFailed()) {
                failedDownloads.add(state);
            }
        }
        return failedDownloads;
    }
    
    /**
     * 检查是否所有下载都已完成
     */
    public boolean isAllDownloadsFinished() {
        for (DownloadState state : downloadStates) {
            if (!state.isFinished()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 清空下载状态
     */
    public void clearDownloadStates() {
        downloadStates.clear();
    }
    
    /**
     * 关闭下载管理器
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * 下载进度信息
     */
    public static class DownloadProgress {
        private final long downloadedSize;
        private final long totalSize;
        private final double percentage;
        private final int downloadedNumber;
        private final int totalNumber;
        
        public DownloadProgress(long downloadedSize, long totalSize, double percentage, 
                              int downloadedNumber, int totalNumber) {
            this.downloadedSize = downloadedSize;
            this.totalSize = totalSize;
            this.percentage = percentage;
            this.downloadedNumber = downloadedNumber;
            this.totalNumber = totalNumber;
        }
        
        public long getDownloadedSize() { return downloadedSize; }
        public long getTotalSize() { return totalSize; }
        public double getPercentage() { return percentage; }
        public int getDownloadedNumber() { return downloadedNumber; }
        public int getTotalNumber() { return totalNumber; }
    }
}