package com.tchmaterial.gui;

import com.tchmaterial.model.ResourceInfo;
import com.tchmaterial.service.DownloadManager;
import com.tchmaterial.service.ResourceParser;
import com.tchmaterial.service.TokenManager;
import com.tchmaterial.util.FormatUtils;
import com.tchmaterial.util.SystemUtils;
import okhttp3.OkHttpClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 主窗口
 */
public class MainWindow extends JFrame {
    
    private final OkHttpClient httpClient;
    private final TokenManager tokenManager;
    private final ResourceParser resourceParser;
    private final DownloadManager downloadManager;
    
    private JTextArea urlTextArea;
    private JButton tokenButton;
    private JButton downloadButton;
    private JButton copyButton;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    
    private final double scaleFactor;
    
    public MainWindow() {
        this.scaleFactor = SystemUtils.getScaleFactor();
        
        // 初始化HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
        
        // 初始化服务
        this.tokenManager = new TokenManager();
        this.resourceParser = new ResourceParser(httpClient, tokenManager);
        this.downloadManager = new DownloadManager(httpClient, tokenManager);
        
        // 加载保存的token
        tokenManager.loadAccessToken();
        
        initializeGUI();
        setupEventHandlers();
    }
    
    /**
     * 初始化GUI
     */
    private void initializeGUI() {
        setTitle("国家中小学智慧教育平台 资源下载工具 v3.2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 设置窗口图标
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            // 忽略图标设置失败
        }
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(scale(20), scale(40), scale(20), scale(40)));
        
        // 标题
        JLabel titleLabel = new JLabel("国家中小学智慧教育平台 资源下载工具", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, scale(16)));
        titleLabel.setBorder(new EmptyBorder(scale(5), 0, scale(5), 0));
        
        // 描述
        String description = "📌 请在下面的文本框中输入一个或多个资源页面的网址（每个网址一行）。\n" +
            "🔗 资源页面网址示例：\n" +
            "    https://basic.smartedu.cn/tchMaterial/detail?contentType=assets_document&contentId=...\n" +
            "📝 您也可以直接在下方的选项卡中选择教材。\n" +
            "� 点击 直\"下载\" 按钮后，程序会解析并下载资源。\n" +
            "⚠️ 注：为了更可靠地下载，建议点击 \"设置 Token\" 按钮，参照里面的说明完成设置。";
        
        JLabel descLabel = new JLabel("<html>" + description.replace("\n", "<br>") + "</html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, scale(9)));
        descLabel.setBorder(new EmptyBorder(scale(5), 0, scale(15), 0));
        
        // URL输入区域
        urlTextArea = new JTextArea(12, 70);
        urlTextArea.setFont(new Font("微软雅黑", Font.PLAIN, scale(9)));
        urlTextArea.setLineWrap(true);
        urlTextArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(urlTextArea);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        tokenButton = new JButton("设置 Token");
        tokenButton.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        
        copyButton = new JButton("解析并复制");
        copyButton.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        
        downloadButton = new JButton("下载");
        downloadButton.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        
        buttonPanel.add(tokenButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(downloadButton);
        
        // 进度面板
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(new EmptyBorder(scale(10), scale(40), 0, scale(40)));
        
        progressLabel = new JLabel("等待下载", JLabel.CENTER);
        progressLabel.setFont(new Font("微软雅黑", Font.PLAIN, scale(9)));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // 组装界面
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(descLabel, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(progressPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 设置窗口大小和位置
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(scale(900), scale(600)));
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        tokenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTokenDialog();
            }
        });
        
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseAndCopy();
            }
        });
        
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDownload();
            }
        });
        
        // 窗口关闭事件
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                downloadManager.shutdown();
                System.exit(0);
            }
        });
    }
    
    /**
     * 显示Token设置对话框
     */
    private void showTokenDialog() {
        TokenDialog dialog = new TokenDialog(this, tokenManager);
        dialog.setVisible(true);
    }
    
    /**
     * 解析并复制链接
     */
    private void parseAndCopy() {
        String[] urls = getUrlsFromTextArea();
        if (urls.length == 0) {
            JOptionPane.showMessageDialog(this, "请输入资源链接", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        List<String> resourceLinks = new ArrayList<>();
        List<String> failedLinks = new ArrayList<>();
        
        for (String url : urls) {
            ResourceInfo info = resourceParser.parse(url);
            if (info.isValid()) {
                resourceLinks.add(info.getResourceUrl());
            } else {
                failedLinks.add(url);
            }
        }
        
        if (!failedLinks.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "以下链接无法解析：\n" + String.join("\n", failedLinks), 
                "警告", JOptionPane.WARNING_MESSAGE);
        }
        
        if (!resourceLinks.isEmpty()) {
            String linksText = String.join("\n", resourceLinks);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(linksText), null);
            JOptionPane.showMessageDialog(this, "资源链接已复制到剪贴板", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 开始下载
     */
    private void startDownload() {
        String[] urls = getUrlsFromTextArea();
        if (urls.length == 0) {
            JOptionPane.showMessageDialog(this, "请输入资源链接", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        downloadButton.setEnabled(false);
        downloadManager.clearDownloadStates();
        
        File saveDir = null;
        if (urls.length > 1) {
            JOptionPane.showMessageDialog(this, 
                "您选择了多个链接，将在选定的文件夹中使用教材名称作为文件名进行下载。", 
                "提示", JOptionPane.INFORMATION_MESSAGE);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                downloadButton.setEnabled(true);
                return;
            }
            saveDir = fileChooser.getSelectedFile();
        }
        
        List<String> failedLinks = new ArrayList<>();
        List<CompletableFuture<Void>> downloadTasks = new ArrayList<>();
        
        for (String url : urls) {
            ResourceInfo info = resourceParser.parse(url);
            if (!info.isValid()) {
                failedLinks.add(url);
                continue;
            }
            
            String savePath;
            if (saveDir != null) {
                String filename = (info.getTitle() != null ? info.getTitle() : "download") + ".pdf";
                savePath = new File(saveDir, filename).getAbsolutePath();
            } else {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File((info.getTitle() != null ? info.getTitle() : "download") + ".pdf"));
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    downloadButton.setEnabled(true);
                    return;
                }
                savePath = fileChooser.getSelectedFile().getAbsolutePath();
            }
            
            CompletableFuture<Void> task = downloadManager.downloadFile(info.getResourceUrl(), savePath, 
                new java.util.function.Consumer<DownloadManager.DownloadProgress>() {
                    @Override
                    public void accept(DownloadManager.DownloadProgress progress) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateProgress(progress);
                            }
                        });
                    }
                });
            downloadTasks.add(task);
        }
        
        if (!failedLinks.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "以下链接无法解析：\n" + String.join("\n", failedLinks), 
                "警告", JOptionPane.WARNING_MESSAGE);
        }
        
        // 等待所有下载完成
        CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[0]))
            .thenRun(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            onDownloadComplete();
                        }
                    });
                }
            });
    }
    
    /**
     * 更新下载进度
     */
    private void updateProgress(DownloadManager.DownloadProgress progress) {
        if (progress.getTotalSize() > 0) {
            int percentage = (int) progress.getPercentage();
            progressBar.setValue(percentage);
            progressBar.setString(FormatUtils.formatPercentage(progress.getPercentage()));
            
            String progressText = String.format("%s/%s (%s) 已下载 %d/%d",
                FormatUtils.formatBytes(progress.getDownloadedSize()),
                FormatUtils.formatBytes(progress.getTotalSize()),
                FormatUtils.formatPercentage(progress.getPercentage()),
                progress.getDownloadedNumber(),
                progress.getTotalNumber());
            
            progressLabel.setText(progressText);
        }
    }
    
    /**
     * 下载完成处理
     */
    private void onDownloadComplete() {
        progressBar.setValue(0);
        progressBar.setString("");
        progressLabel.setText("等待下载");
        downloadButton.setEnabled(true);
        
        List<com.tchmaterial.model.DownloadState> failedDownloads = downloadManager.getFailedDownloads();
        if (!failedDownloads.isEmpty()) {
            StringBuilder message = new StringBuilder("文件已下载完成\n以下文件下载失败：\n");
            for (com.tchmaterial.model.DownloadState state : failedDownloads) {
                message.append(state.getDownloadUrl()).append("，原因：").append(state.getFailedReason()).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString(), "下载完成", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "所有文件下载完成！", "下载完成", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 从文本区域获取URL列表
     */
    private String[] getUrlsFromTextArea() {
        String text = urlTextArea.getText().trim();
        if (text.isEmpty()) {
            return new String[0];
        }
        
        return Arrays.stream(text.split("\n"))
            .map(new java.util.function.Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s.trim();
                }
            })
            .filter(new java.util.function.Predicate<String>() {
                @Override
                public boolean test(String line) {
                    return !line.isEmpty();
                }
            })
            .toArray(new java.util.function.IntFunction<String[]>() {
                @Override
                public String[] apply(int value) {
                    return new String[value];
                }
            });
    }
    
    /**
     * 缩放尺寸
     */
    private int scale(int size) {
        return (int) (size * scaleFactor);
    }
    
    /**
     * 创建窗口图标
     */
    private Image createIcon() {
        // 这里应该加载实际的图标，暂时返回null
        return null;
    }
}