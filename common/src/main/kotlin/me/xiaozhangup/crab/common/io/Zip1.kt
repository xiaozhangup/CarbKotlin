package me.xiaozhangup.crab.common.io

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 压缩文件
 *
 * @param target 压缩后的文件
 * @param skipParent 是否跳过该文件，从子文件开始压缩
 */
fun File.zip(target: File, skipParent: Boolean = false) {
    if (skipParent) {
        if (isDirectory) {
            FileOutputStream(target).use { fileOutputStream -> ZipOutputStream(fileOutputStream).use { listFiles()?.forEach { file -> it.putFile(file, "") } } }
        } else {
            error("is not directory")
        }
    } else {
        FileOutputStream(target).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { it.putFile(this, "") }
        }
    }
}

/**
 * 解压文件
 *
 * @param target 解压后的文件
 */
fun File.unzip(target: File) {
    unzip(target.path)
}

/**
 * 解压文件
 *
 * @param destDirPath 解压后的文件路径
 */
fun File.unzip(destDirPath: String) {
    val root = File(destDirPath).canonicalFile.toPath()
    ZipFile(this).use { zipFile ->
        zipFile.stream().forEach { entry ->
            val output = root.resolve(entry.name).toFile().canonicalFile
            require(output.toPath().startsWith(root)) { "ZIP entry escapes target directory: ${entry.name}" }
            if (entry.isDirectory) {
                newFolder(output.path)
            } else {
                zipFile.getInputStream(entry).use { input ->
                    newFile(output).outputStream().use(input::copyTo)
                }
            }
        }
    }
}

fun ZipOutputStream.putFile(file: File, path: String) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { putFile(it, path + file.name + "/") }
    } else {
        FileInputStream(file).use {
            putNextEntry(ZipEntry(path + file.name))
            write(it.readBytes())
            flush()
            closeEntry()
        }
    }
}
