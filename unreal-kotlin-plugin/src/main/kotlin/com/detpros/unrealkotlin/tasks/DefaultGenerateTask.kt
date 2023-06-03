package com.detpros.unrealkotlin.tasks

import com.detpros.unrealkotlin.corrections.CorrectionEnvironment
import com.detpros.unrealkotlin.declaration.PackageDeclaration
import com.detpros.unrealkotlin.declaration.toDeclaration
import com.detpros.unrealkotlin.parsing.ParsingContext
import com.detpros.unrealkotlin.parsing.ParsingEnvironment
import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File


/**
 *  Abstract Generate Task
 *
 * @author IvanEOD ( 6/3/2023 at 3:13 PM EST )
 */
private lateinit var context: ParsingContext
class DefaultGenerateTask(
    private val source: FileTree,
    private val workerExecutor: WorkerExecutor,
    private val outputDirectoryProperty: DirectoryProperty,
) {
    val sourceFiles = source.asFileTree.files
    val outputDirectory = outputDirectoryProperty.get().asFile

    fun execute() {
        preGenerationClean()
        context = ParsingEnvironment.contextFor(sourceFiles)
        ParsingEnvironment.withSources(sourceFiles) {
            val queue = workerExecutor.noIsolation()
            val ktFiles = environment.getSourceFiles()
            ktFiles.forEach { file ->
                context.register(file)
                queue.submit(ProcessFileReformat::class.java) {
                    println("Processing ${file.name}")
                    it.fileName = file.name
                }
            }
            queue.await()
        }

        val packageContext = PackageDeclaration.withSources("ue", context.fileSpecs().map(FileSpec::toDeclaration).toSet())

        val environment = CorrectionEnvironment(outputDirectory, packageContext)
        environment.process()

    }



    private val fileNames = buildList {
        add("UE.kt")
        (0..5).forEach { add("UE$it.kt") }
    }
    private fun preGenerationClean() {
        val goalNames = source.files.map { it.name.replace(".d.ts", ".kt") } + fileNames
        val cleanedFiles = mutableListOf<String>()
        outputDirectory.listFiles()?.forEach {
            if (it.name !in goalNames) {
                cleanedFiles.add(it.name)
                it.deleteRecursively()
            }
        }
        if (cleanedFiles.isNotEmpty()) {
            println("Cleaned files: ")
            cleanedFiles.forEach { println("\t- $it") }
        }
    }


}

interface FileReformatParameters : WorkParameters {
    var fileName: String
}

abstract class ProcessFileReformat : WorkAction<FileReformatParameters> {
    private lateinit var source: KtFile
    private lateinit var destination: File

    override fun execute() {
        try {
            val name = parameters.fileName
            source = context.findKtFile { it.name == name } ?: return
            context.process(source)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}