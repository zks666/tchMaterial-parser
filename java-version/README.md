# 国家中小学智慧教育平台电子课本下载工具 - Java版

![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Made With Love❤️](https://img.shields.io/badge/Made_With-%E2%9D%A4-red.svg)

> 这是原Python版本的Java语言翻译版本，保持了相同的功能和用户体验。

本工具可以帮助您从[**国家中小学智慧教育平台**](https://basic.smartedu.cn/)获取电子课本的 PDF 文件网址并进行下载，让您更方便地获取课本内容。

## ✨工具特点

- 🔑**支持 Access Token 登录**：支持用户手动输入 Access Token 并自动保存，下次启动可自动加载。
- 📚**支持批量下载**：一次输入多个电子课本预览页面网址，即可批量下载 PDF 课本文件。
- 📂**自动文件命名**：程序会自动使用教材名称作为文件名，方便管理下载的课本文件。
- 🖥️**高 DPI 适配**：优化 UI 以适配高分辨率屏幕，避免界面模糊问题。
- 📊**下载进度可视化**：实时显示下载进度，支持多线程下载。
- 💻**跨平台支持**：支持 Windows、Linux、macOS 等操作系统（需要Java 8+）。

## 📋系统要求

- Java 8 或更高版本
- 图形界面环境（GUI）
- Maven 3.6.0+ （仅编译时需要）

## 🛠️编译和运行

### 使用Maven编译

```bash
# 克隆项目
git clone <repository-url>
cd java-version

# 编译项目
mvn clean compile

# 打包为可执行JAR
mvn clean package

# 运行程序
java -jar target/tchmaterial-parser-3.2.0.jar
```

### 直接运行

如果您有Java开发环境：

```bash
# 编译
javac -cp "lib/*" src/main/java/com/tchmaterial/*.java src/main/java/com/tchmaterial/*/*.java

# 运行
java -cp "target/classes:lib/*" com.tchmaterial.TchMaterialParser
```

## 🛠️使用方法

### 1. 输入教材链接⌨️

将电子课本的**预览页面网址**粘贴到程序文本框中，支持多个 URL（每行一个）。

**示例网址**：

```text
https://basic.smartedu.cn/tchMaterial/detail?contentType=assets_document&contentId=XXXXXX&catalogType=tchMaterial&subCatalog=tchMaterial
```

### 2. 设置 Access Token🔑

> [!TIP]
> 自 v3.1 版本起，这一步操作已经**不再必要**，当未设置 Access Token 时程序会使用其他方法下载资源。然而，这一方法**并不长期有效**，因此仍然建议您进行这一步操作。

1. **打开浏览器**，访问[国家中小学智慧教育平台](https://auth.smartedu.cn/uias/login)并**登录账号**。
2. 按下 **F12** 或 **Ctrl+Shift+I**，或右键——检查（审查元素）打开**开发者工具**，选择**控制台（Console）**。
3. 在控制台粘贴以下代码后回车（Enter）：

   ```js
   (function() {
     const authKey = Object.keys(localStorage).find(key => key.startsWith("ND_UC_AUTH"));
     if (!authKey) {
       console.error("未找到 Access Token，请确保已登录！");
       return;
     }
     const tokenData = JSON.parse(localStorage.getItem(authKey));
     const accessToken = JSON.parse(tokenData.value).access_token;
     console.log("%cAccess Token:", "color: green; font-weight: bold", accessToken);
   })();
   ```
  
4. 复制控制台输出的 **Access Token**，然后在本程序中点击 "**设置 Token**" 按钮，粘贴并保存 Token。

### 3. 开始下载🚀

点击 "**下载**" 按钮，程序将自动解析并下载 PDF 课本。

本工具支持**批量下载**，所有 PDF 文件会自动按课本名称命名并保存在选定目录中。

## 📁项目结构

```
java-version/
├── pom.xml                                 # Maven配置文件
├── README.md                              # 说明文档
└── src/main/java/com/tchmaterial/
    ├── TchMaterialParser.java             # 主程序入口
    ├── model/                             # 数据模型
    │   ├── ResourceInfo.java              # 资源信息模型
    │   └── DownloadState.java             # 下载状态模型
    ├── service/                           # 业务服务
    │   ├── TokenManager.java              # Token管理器
    │   ├── ResourceParser.java            # 资源解析器
    │   └── DownloadManager.java           # 下载管理器
    ├── util/                              # 工具类
    │   ├── SystemUtils.java               # 系统工具类
    │   └── FormatUtils.java               # 格式化工具类
    └── gui/                               # 图形界面
        ├── MainWindow.java                # 主窗口
        └── TokenDialog.java               # Token设置对话框
```

## 🔧技术栈

- **Java 8+**: 核心开发语言，兼容JDK8及以上版本
- **Swing**: 图形用户界面框架
- **OkHttp**: 现代化HTTP客户端库
- **Gson**: Google的JSON处理库
- **Maven**: 项目构建和依赖管理工具

## ❓常见问题

### 1. 为什么下载失败？⚠️

- 如果您没有设置 Access Token，可能是本程序使用的方法失效了，请[**设置 Access Token**](#2-设置-access-token)🔑。
- 如果您设置了 Access Token，由于其具有时效性（一般为 7 天），因此极有可能是 Access Token 过期了，请重新获取新的 Access Token。
- **确认网络连接是否正常**🌐，有时网络不稳定可能导致下载失败。
- **确保输入的网址有效**🔗，部分旧资源可能已被移除。

### 2. Access Token 保存在哪里？💾

- **Windows 操作系统**：Token 会存储在**注册表** `HKEY_CURRENT_USER\Software\tchMaterial-parser` 项中的 `AccessToken` 值。
- **Linux 操作系统**: Token 会存储在**文件** `~/.config/tchMaterial-parser/data.json` 中。
- **macOS 操作系统**：Token 会存储在**文件** `~/Library/Application Support/tchMaterial-parser/data.json` 中。

### 3. Token 会不会泄露？🔐

- 本程序**不会上传** Token，也不会存储在云端，仅用于本地请求授权。
- **请勿在公开场合分享 Token**，以免您的账号被他人使用，造成严重后果。

## 🤝贡献指南

如果您发现 Bug 或有改进建议，欢迎提交 **Issue** 或 **Pull Request**，让我们一起完善本工具！

## 📜许可证

本项目基于 [MIT 许可证](../LICENSE)，欢迎自由使用和二次开发。

## 💌致谢

- 感谢原Python版本的作者：[肥宅水水呀](https://space.bilibili.com/324042405)
- 原项目地址：https://github.com/happycola233/tchMaterial-parser