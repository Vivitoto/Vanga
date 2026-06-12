# Vanga

Vanga 是面向 Komga 媒体库的跨平台漫画 / 图书客户端，支持 Android、桌面端和 Web 运行方式。项目重点是稳定的本地阅读体验、中文界面、常用媒体库管理，以及与 Komga / Komf 生态的连接能力。

> 当前版本：`v0.1.0`
>
> Android 包名：`io.github.vivitoto.vanga`

## 功能概览

- 连接 Komga 服务器，浏览媒体库、系列、单本、合集与阅读清单。
- 内置图片阅读器，支持分页 / 连续阅读、阅读方向、缩放、拉伸适配、裁边和采样设置。
- 支持 EPUB 阅读入口。
- 支持阅读进度、收藏、下载状态、批量操作等常用管理能力。
- 支持 Komf 相关连接、处理任务、元数据提供方与通知配置。
- 提供 Android、桌面端与 Web 构建目标。

## 截图

<details>
  <summary>手机端</summary>
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="Vanga" width="270">
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="Vanga" width="270">
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Vanga" width="270">
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" alt="Vanga" width="270">
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="Vanga" width="270">
   <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="Vanga" width="270">
</details>

<details>
  <summary>平板端</summary>
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/1.jpg" alt="Vanga" width="400" height="640">
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/2.jpg" alt="Vanga" width="400" height="640">
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/3.jpg" alt="Vanga" width="400" height="640">
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/4.jpg" alt="Vanga" width="400" height="640">
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/5.jpg" alt="Vanga" width="400" height="640">
   <img src="/fastlane/metadata/android/en-US/images/tenInchScreenshots/6.jpg" alt="Vanga" width="400" height="640">
</details>

<details>
  <summary>桌面端</summary>
   <img src="/screenshots/1.jpg" alt="Vanga" width="1280">
   <img src="/screenshots/2.jpg" alt="Vanga" width="1280">
   <img src="/screenshots/3.jpg" alt="Vanga" width="1280">
   <img src="/screenshots/4.jpg" alt="Vanga" width="1280">
   <img src="/screenshots/5.jpg" alt="Vanga" width="1280">
</details>

## 下载

当前推荐 APK 在 GitHub Release：

- 最新版本：https://github.com/Vivitoto/Vanga/releases/tag/latest
- 历史版本：https://github.com/Vivitoto/Vanga/releases

请优先下载 `vanga-v0_1_0.apk`。后续每次发布都会保留独立的 `vX.Y.Z` 历史版本，同时 `latest` 只保留最新 APK。

## 构建说明

Android 与桌面端目标需要 JDK 17 或更高版本。原生库构建需要 C / C++ 编译环境，EPUB 阅读器 WebUI 构建需要 Node.js。

推荐使用 `cmake/` 下提供的 Dockerfile 构建原生依赖；如果使用本机工具链，可在 Linux 上尝试：

```bash
./gradlew vangaBuildNonJvmDependencies
```

### 桌面端

可用平台包括：`linux-x86_64`、`windows-x86_64`。

```bash
docker build -t vanga-build-<platform> . -f ./cmake/<platform>.Dockerfile
docker run -v .:/build vanga-build-<platform>
./gradlew <platform>_copyJniLibs
./gradlew buildWebui
```

常用构建命令：

```bash
./gradlew :vanga-app:run
./gradlew :vanga-app:packageReleaseUberJarForCurrentOS
./gradlew :vanga-app:packageReleaseDeb
./gradlew :vanga-app:packageReleaseMsi
```

### Android

可用架构包括：`aarch64`、`armv7a`、`x86_64`、`x86`。

```bash
docker build -t vanga-build-android . -f ./cmake/android.Dockerfile
docker run -v .:/build vanga-build-android <arch>
./gradlew <arch>_copyJniLibs
./gradlew buildWebui
```

常用构建命令：

```bash
./gradlew :vanga-app:assembleDebug
./gradlew :vanga-app:assembleRelease
```
