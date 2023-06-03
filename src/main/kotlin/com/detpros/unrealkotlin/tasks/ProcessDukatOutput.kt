package com.detpros.unrealkotlin.tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class ProcessDukatOutput : SourceTask() {

    @get:Inject
    abstract val workerExecutor: WorkerExecutor


    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        val task = DefaultGenerateTask(
            source,
            workerExecutor,
            outputDirectory,
        )
        task.execute()
    }


}
