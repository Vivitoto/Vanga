# Vanga 0.1.0

首个 Vanga 发布版本。

## 更新内容

- 完成 Vanga 项目独立化：应用名、包名、命名空间、模块名和原生库名称均切换为 Vanga。
- Android 包名更新为 `io.github.vivitoto.vanga`，可作为全新应用安装。
- 保留 Android、桌面端和 Web 多平台构建目标。
- 保留 Komga 媒体库连接、漫画 / 图书阅读、下载、收藏、阅读进度和 Komf 集成能力。
- 移除浏览器扩展模块，Vanga 主项目不再包含 Komf Chrome extension。
- GitHub Actions 发布结构调整为：
  - `latest` release 始终放最新 APK。
  - `vX.Y.Z` release 保留对应历史版本 APK。

## 说明

这是 Vanga 的初始公开版本。由于包名已更换，它不会覆盖或升级其他应用，需要作为新应用安装。
