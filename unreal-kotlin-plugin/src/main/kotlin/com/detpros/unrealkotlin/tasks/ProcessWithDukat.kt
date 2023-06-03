package com.detpros.unrealkotlin.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import com.detpros.unrealkotlin.cmd
import java.io.File


/**
 *  Process With Dukat
 *
 * @author IvanEOD ( 5/23/2023 at 1:12 PM EST )
 */
abstract class ProcessWithDukat : SourceTask() {

    @get:Internal
    abstract val typescriptEntryPoint: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        val sourceFiles = source.files
        val entryPointName = typescriptEntryPoint.get()
        var typings: Collection<File> = sourceFiles.toList()
        if (typings.size == 1 && typings.first().name == "typings") typings = typings.first().listFiles()?.toList() ?: listOf()
        typings.filter { it.name.endsWith(".d.ts") }.map { it.name }.let {
            println("Typings Files: ")
            it.forEach { name -> println("\t-$name") }
        }
        val entryFile = typings.find { it.name == entryPointName }
            ?: throw GradleException("Entry point file not found: $entryPointName")

        val outputDirectory = outputDirectory.get().asFile
        cmd("dukat.cmd", "-d", outputDirectory.absolutePath, entryFile.absolutePath)

        val goalNames = typings.map { it.name.replace(".d.ts", ".kt") }
        outputDirectory.listFiles()?.forEach {
            if (it.name !in goalNames) it.deleteRecursively()
        }
    }


}


