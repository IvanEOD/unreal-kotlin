package com.detpros.unrealkotlin

import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.io.File


/**
 *  Unreal Kotlin Utilities
 *
 * @author IvanEOD ( 5/23/2023 at 12:23 PM EST )
 */


fun File.safeResolveDir(path: String): File {
    val directory = resolve(path)
    if (!directory.exists()) directory.mkdirs()
    return directory
}

fun File.safeResolveFile(path: String): File {
    val file = resolve(path)
    if (!file.exists()) file.createNewFile()
    return file
}

fun Project.asDirectory(file: File) : Directory = objects.directoryProperty().apply { set(file) }.get()