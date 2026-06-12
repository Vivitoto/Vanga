# Vanga 0.1.1

Android 启动崩溃修复版本。

## 修复内容

- 修复 Android APK 启动时找不到 `libsqlitejdbc.so` 导致的崩溃：
  - `java.lang.UnsatisfiedLinkError: dlopen failed: library "libsqlitejdbc.so" not found`
- 将 SQLite JDBC 的 Android native library 自动解包到 Gradle 生成目录，并接入 Android `jniLibs` 打包流程。
- CI 发布流程会构建并打包 Android 图片解码所需 native libraries，并校验 APK 中包含关键 `.so` 文件。
- 修正图片解码 native JNI 符号的包名，避免 Vanga 包名迁移后调用不到旧命名空间下的 JNI 方法。
- 调整 Android / 桌面 / metadata 图标留白比例，避免启动器裁切导致图标显示不全；背景保持深色，不引入白边。

## 说明

这是 `0.1.0` 首发后的紧急修复版本，建议所有 Android 用户升级。
