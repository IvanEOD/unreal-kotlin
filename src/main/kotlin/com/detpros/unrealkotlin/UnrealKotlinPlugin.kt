package com.detpros.unrealkotlin

import com.detpros.unrealkotlin.UnrealKotlinCache.cacheDukatGeneratedDirectory
import com.detpros.unrealkotlin.UnrealKotlinCache.cacheSourcesDirectory
import com.detpros.unrealkotlin.UnrealKotlinCache.cacheTypingsDirectory
import com.detpros.unrealkotlin.tasks.ProcessDukatOutput
import com.detpros.unrealkotlin.tasks.ProcessWithDukat
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import java.io.File


class UnrealKotlinPlugin : Plugin<Project> {

    lateinit var unrealProjectDirectory: File
        private set
    lateinit var project: Project
        private set

    override fun apply(target: Project) {
        this.project = target
        val extension = project.findOrCreateExtension<UnrealKotlinExtension>("unrealKotlin", project)
        extension.unrealProjectDir.convention(
            project.asDirectory(
                project.providers.environmentVariable("UNREAL_PROJECT_DIR").orNull?.let { File(it) }
                    ?: project.layout.projectDirectory.asFile.parentFile
            ))

        with(UnrealKotlinCache) { setCacheDirectory() }

        val unrealTypingsDirectory = extension.unrealProjectDir.dir("Content/Scripts/typings")
        val unrealSourceDestinationDirectory = extension.unrealProjectDir.dir("Content/Scripts")
        val cacheTypingsDirectory = cacheTypingsDirectory
        val cacheDukatGeneratedDirectory = cacheDukatGeneratedDirectory
        val cacheSourcesDirectory = cacheSourcesDirectory

        val sourceSets = project.extensions.getByType<KotlinJsProjectExtension>().sourceSets
        val mainSourceSet = sourceSets["main"]
        mainSourceSet.kotlin.srcDir(listOf(project.projectDir.resolve("src/main/kotlin"), cacheSourcesDirectory))

        val copyTypingsFromUnreal = project.tasks.register<Copy>("copyTypingsFromUnreal") {
            group = "unreal-kotlin"
            outputs.upToDateWhen { false }
            from(unrealTypingsDirectory)
            into(cacheTypingsDirectory)
        }

        val processUnrealTypingsToKotlin = project.tasks.register<ProcessWithDukat>("processUnrealTypingsToKotlin") {
            group = "unreal-kotlin"
            dependsOn(copyTypingsFromUnreal)
            source(cacheTypingsDirectory)
            outputs.upToDateWhen { outputDirectory.asFile.get().listFiles()?.isNotEmpty() ?: false }
            typescriptEntryPoint.set("ue.d.ts")
            outputDirectory.set(cacheDukatGeneratedDirectory)
        }

        val generateProjectSources = project.tasks.register<ProcessDukatOutput>("generateProjectApi") {
            group = "unreal-kotlin"
            dependsOn(processUnrealTypingsToKotlin)
            source(cacheDukatGeneratedDirectory)
            outputDirectory.set(cacheSourcesDirectory)
        }

        val build = project.tasks.getByName("build")

        val copySourcesToUnreal = project.tasks.register<Copy>("copySourcesToUnreal") {
            group = "unreal-kotlin"
            dependsOn(build)
            from(project.buildDir.resolve("classes/kotlin/main/")) {
                include("${project.name}.js")
            }
            into(unrealSourceDestinationDirectory)
        }


    }


    inline fun <reified T> Project.findOrCreateExtension(name: String, vararg constructionArgs: Any): T =
        extensions.findByName(name) as? T ?: extensions.create(name, T::class.java, *constructionArgs)
}