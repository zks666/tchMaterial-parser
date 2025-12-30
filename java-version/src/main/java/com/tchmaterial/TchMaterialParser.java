package com.tchmaterial;

import com.tchmaterial.gui.MainWindow;
import com.tchmaterial.util.SystemUtils;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * 国家中小学智慧教育平台资源下载工具 v3.2 Java版
 * 项目地址：https://github.com/happycola233/tchMaterial-parser
 * 作者：肥宅水水呀（https://space.bilibili.com/324042405）以及其他为本工具作出贡献的用户
 * Java版本翻译
 */
public class TchMaterialParser {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            // 在Windows上使用系统外观
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Exception e) {
            // 如果设置失败，使用默认外观
            System.out.println("无法设置系统外观，使用默认外观");
        }
        
        // 设置高DPI支持
        System.setProperty("sun.java2d.uiScale", String.valueOf(SystemUtils.getScaleFactor()));
        
        // 启动GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new MainWindow().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "程序启动失败：" + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}