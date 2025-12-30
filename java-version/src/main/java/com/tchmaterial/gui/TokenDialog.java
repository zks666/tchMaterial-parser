package com.tchmaterial.gui;

import com.tchmaterial.service.TokenManager;
import com.tchmaterial.util.SystemUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Token设置对话框
 */
public class TokenDialog extends JDialog {
    
    private final TokenManager tokenManager;
    private JTextArea tokenTextArea;
    private final double scaleFactor;
    
    public TokenDialog(Frame parent, TokenManager tokenManager) {
        super(parent, "设置 Access Token", true);
        this.tokenManager = tokenManager;
        this.scaleFactor = SystemUtils.getScaleFactor();
        
        initializeGUI();
        setupEventHandlers();
    }
    
    /**
     * 初始化GUI
     */
    private void initializeGUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(scale(20), scale(20), scale(20), scale(20)));
        
        // 提示标签
        JLabel promptLabel = new JLabel("请粘贴从浏览器获取的 Access Token：");
        promptLabel.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        promptLabel.setBorder(new EmptyBorder(0, 0, scale(5), 0));
        
        // Token输入区域
        tokenTextArea = new JTextArea(4, 50);
        tokenTextArea.setFont(new Font("微软雅黑", Font.PLAIN, scale(9)));
        tokenTextArea.setLineWrap(true);
        tokenTextArea.setWrapStyleWord(true);
        
        // 如果已有token，填入
        if (tokenManager.hasValidToken()) {
            tokenTextArea.setText(tokenManager.getAccessToken());
        }
        
        JScrollPane scrollPane = new JScrollPane(tokenTextArea);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveToken();
            }
        });
        
        JButton helpButton = new JButton("如何获取？");
        helpButton.setFont(new Font("微软雅黑", Font.PLAIN, scale(10)));
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showHelpDialog();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(helpButton);
        
        // 组装界面
        mainPanel.add(promptLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 设置窗口
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // ESC键关闭窗口
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dispose();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Enter键保存
        tokenTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "save");
        tokenTextArea.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveToken();
            }
        });
        
        // Shift+Enter不换行
        tokenTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "none");
        tokenTextArea.getActionMap().put("none", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // 什么都不做
            }
        });
    }
    
    /**
     * 保存Token
     */
    private void saveToken() {
        String token = tokenTextArea.getText().trim();
        String message = tokenManager.setAccessToken(token);
        
        JOptionPane.showMessageDialog(this, message, "保存成功", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
    
    /**
     * 显示帮助对话框
     */
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "获取 Access Token 方法", true);
        helpDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBorder(new EmptyBorder(scale(20), scale(20), scale(20), scale(20)));
        
        String helpText = "国家中小学智慧教育平台需要登录后才可获取教材，因此要使用本程序下载教材，您需要在平台内登录账号（如没有需注册），然后获得登录凭据（Access Token）。本程序仅保存该凭据至本地。\n\n" +
            "获取方法如下：\n" +
            "1. 打开浏览器，访问国家中小学智慧教育平台（https://auth.smartedu.cn/uias/login）并登录账号。\n" +
            "2. 按下 F12 或 Ctrl+Shift+I，或右键——检查（审查元素）打开开发者工具，选择控制台（Console）。\n" +
            "3. 在控制台粘贴以下代码后回车（Enter）：\n" +
            "---------------------------------------------------------\n" +
            "(function() {\n" +
            "    const authKey = Object.keys(localStorage).find(key => key.startsWith(\"ND_UC_AUTH\"));\n" +
            "    if (!authKey) {\n" +
            "        console.error(\"未找到 Access Token，请确保已登录！\");\n" +
            "        return;\n" +
            "    }\n" +
            "    const tokenData = JSON.parse(localStorage.getItem(authKey));\n" +
            "    const accessToken = JSON.parse(tokenData.value).access_token;\n" +
            "    console.log(\"%cAccess Token:\", \"color: green; font-weight: bold\", accessToken);\n" +
            "})();\n" +
            "---------------------------------------------------------\n" +
            "然后在控制台输出中即可看到 Access Token。将其复制后粘贴到本程序中。";
        
        JTextArea helpTextArea = new JTextArea(helpText);
        helpTextArea.setFont(new Font("微软雅黑", Font.PLAIN, scale(9)));
        helpTextArea.setEditable(false);
        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        helpTextArea.setBackground(getBackground());
        
        JScrollPane helpScrollPane = new JScrollPane(helpTextArea);
        helpScrollPane.setPreferredSize(new Dimension(scale(600), scale(400)));
        
        helpPanel.add(helpScrollPane, BorderLayout.CENTER);
        helpDialog.add(helpPanel);
        
        helpDialog.pack();
        helpDialog.setLocationRelativeTo(this);
        
        // ESC键关闭帮助窗口
        helpDialog.getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    helpDialog.dispose();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        helpDialog.setVisible(true);
    }
    
    /**
     * 缩放尺寸
     */
    private int scale(int size) {
        return (int) (size * scaleFactor);
    }
}