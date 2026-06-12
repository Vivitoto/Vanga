package io.github.vivitoto.vanga

import dev.dirs.ProjectDirectories
import java.nio.file.Path
import kotlin.io.path.Path

object AppDirectories {
    val projectDirectories = ProjectDirectories.from("io.github.vivitoto.vanga", "", "Vanga")
    val fontDirectory: Path = Path(projectDirectories.dataDir).resolve("fonts")

    val defaultOfflineLibraryPath: Path = Path(projectDirectories.dataDir).resolve("offline_libraries")

    private val cachePath: Path = Path(System.getProperty("java.io.tmpdir")).resolve("vanga")
    val okHttpCachePath: Path = cachePath.resolve("okHttp")
    val coilCachePath: Path = cachePath.resolve("coil")
    val readerCachePath: Path = cachePath.resolve("reader")

    val databaseDirectory: Path = Path(projectDirectories.dataDir)
}
