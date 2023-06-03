package com.detpros.unrealkotlin

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject


/**
 *  Unreal Kotlin Extension
 *
 * @author IvanEOD ( 5/23/2023 at 12:08 PM EST )
 */
abstract class UnrealKotlinExtension @Inject constructor(
    val project: Project
) {
    abstract val unrealProjectDir: DirectoryProperty

}