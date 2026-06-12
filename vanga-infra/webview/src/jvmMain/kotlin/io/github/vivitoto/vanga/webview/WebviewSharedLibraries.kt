package io.github.vivitoto.vanga.webview

import io.github.vivitoto.vanga.jni.DesktopPlatform
import io.github.vivitoto.vanga.jni.DesktopPlatform.Linux
import io.github.vivitoto.vanga.jni.DesktopPlatform.MacOS
import io.github.vivitoto.vanga.jni.DesktopPlatform.Unknown
import io.github.vivitoto.vanga.jni.DesktopPlatform.Windows
import io.github.vivitoto.vanga.jni.SharedLibrariesLoader
import io.github.vivitoto.vanga.jni.SharedLibrariesLoader.tempDir
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories

object WebviewSharedLibraries {
    private val loaded = AtomicBoolean(false)

    @Volatile
    var isAvailable = false
        private set

    fun load() {
        if (!loaded.compareAndSet(false, true)) return

        when (DesktopPlatform.Current) {
            Linux -> loadLinuxLibs()
            Windows -> SharedLibrariesLoader.loadLibrary("libvanga_webview")
            MacOS, Unknown -> error("Unsupported OS")
        }

        isAvailable = true
    }

    private fun loadLinuxLibs() {
        val extensionDir = tempDir.resolve("webkit").createDirectories()
        val classPathFile = SharedLibrariesLoader::class.java.getResource("/libvanga_webkit_extension.so")
            ?: throw UnsatisfiedLinkError("Failed to find libvanga_webkit_extension file")
        val fileBytes = classPathFile.readBytes()
        val libFile = Files.write(
            extensionDir.resolve("libvanga_webkit_extension.so"),
            fileBytes,
            StandardOpenOption.CREATE
        ).toFile()
        libFile.deleteOnExit()

        SharedLibrariesLoader.loadLibrary("vanga_webview")
    }
}