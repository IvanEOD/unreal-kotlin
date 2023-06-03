package com.detpros.unrealkotlin

import java.io.File


/**
 *  Unreal Kotlin Cache
 *
 * @author IvanEOD ( 5/23/2023 at 12:16 PM EST )
 */
object UnrealKotlinCache {
    private lateinit var cacheDirectory: File

    private val cacheTypingsDirectory by lazy { cacheDirectory.safeResolveDir("typings") }
    private val cacheDukatGeneratedDirectory by lazy { cacheDirectory.safeResolveDir("generated/dukat") }
    private val cacheSourcesDirectory by lazy { cacheDirectory.safeResolveDir("generated/source") }

    val UnrealKotlinPlugin.cacheTypingsDirectory: File
        get() = UnrealKotlinCache.cacheTypingsDirectory

    val UnrealKotlinPlugin.cacheDukatGeneratedDirectory: File
        get() = UnrealKotlinCache.cacheDukatGeneratedDirectory

    val UnrealKotlinPlugin.cacheSourcesDirectory: File
        get() = UnrealKotlinCache.cacheSourcesDirectory

    fun UnrealKotlinPlugin.setCacheDirectory() {
        cacheDirectory = project.rootProject.file("build/.unreal-kotlin")
    }





}