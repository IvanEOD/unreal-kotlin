package com.detpros.unrealkotlin

import java.io.File
import java.util.concurrent.TimeUnit


/**
 *  Unreal Kotlin Command Line
 *
 * @author IvanEOD ( 5/23/2023 at 1:18 PM EST )
 */
interface CommandLineScope {

    val args: MutableList<String>
    var workingDirectory: File?
    var waitFor: Int?
    var waitForUnit: TimeUnit


    operator fun plusAssign(arg: String) {
        args += arg
    }

    operator fun plusAssign(arg: File) {
        args += arg.absolutePath
    }

    operator fun plusAssign(arg: Number) {
        args += arg.toString()
    }

    operator fun plusAssign(arg: Boolean) {
        args += arg.toString()
    }

    operator fun plusAssign(arg: Pair<String, String>) {
        args += arg.first
        args += arg.second
    }

    operator fun String.unaryPlus() {
        args += this
    }

    operator fun Array<out String>.unaryPlus() {
        args += this
    }

    fun workingDirectory(path: String) {
        workingDirectory = File(path)
    }

    fun workingDirectory(path: File) {
        workingDirectory = path
    }

    fun waitFor(duration: Int, unit: TimeUnit = TimeUnit.SECONDS) {
        waitFor = duration
        waitForUnit = unit
    }

    fun arg(arg: String) {
        args += arg
    }

    fun args(vararg args: String) {
        this.args += args
    }

    fun argIf(condition: Boolean, arg: String) {
        if (condition) args += arg
    }

    fun argIf(condition: () -> Boolean, arg: String) {
        if (condition()) args += arg
    }

    fun argsIf(condition: Boolean, vararg args: String) {
        if (condition) this.args += args
    }

    fun argsIf(condition: () -> Boolean, vararg args: String) {
        if (condition()) this.args += args
    }

    infix fun String.withArg(value: String) {
        args += this
        args += value
    }


}

private class CommandLineScopeImpl : CommandLineScope {
    override val args: MutableList<String> = mutableListOf()
    override var workingDirectory: File? = null
    override var waitFor: Int? = null
    override var waitForUnit: TimeUnit = TimeUnit.SECONDS
}


private fun CommandLineScope.run() = ProcessBuilder(args)
    .directory(workingDirectory)
    .start()
    .also { waitFor?.let { duration -> it.waitFor(duration.toLong(), waitForUnit) } }
    .inputStream.bufferedReader().readText()

fun cmd(block: CommandLineScope.() -> Unit): String {
    val scope = CommandLineScopeImpl()
    scope.block()
    return scope.run()
}

fun cmd(vararg args: String): String = cmd { args(*args) }
fun cmd(timeout: Int, unit: TimeUnit = TimeUnit.SECONDS, vararg args: String): String = cmd {
    waitFor(timeout, unit)
    args(*args)
}

fun cmd(workingDir: File, vararg args: String): String = cmd {
    workingDirectory(workingDir)
    args(*args)
}

fun env(init: UnrealKotlinCommandLine.Builder.() -> Unit = {}): UnrealKotlinCommandLine = UnrealKotlinCommandLine(init)

class UnrealKotlinCommandLine private constructor(
    builder: Builder
) {

    private val workingDir: File? = builder.workingDir
    private val waitFor: Int? = builder.waitFor
    private val waitForUnit: TimeUnit = builder.waitForUnit
    private val defaultArgs: List<String> = builder.defaultArgs

    fun exec(vararg args: String, block: CommandLineScope.() -> Unit = {}): String = runCmd(*args, block = block)
    fun exec(block: CommandLineScope.() -> Unit): String = runCmd(block = block)

    private fun runCmd(vararg args: String, block: CommandLineScope.() -> Unit = {}): String = cmd {
        workingDir?.let { workingDirectory(it) }
        waitFor?.let { waitFor(it, waitForUnit) }
        if (defaultArgs.isNotEmpty()) args(*defaultArgs.toTypedArray())
        args(*args)
        block()
    }

    class Builder internal constructor() {
        var workingDir: File? = null
        var waitFor: Int? = null
        var waitForUnit: TimeUnit = TimeUnit.SECONDS
        val defaultArgs: MutableList<String> = mutableListOf()

        fun workingDirectory(path: String) {
            workingDir = File(path)
        }

        fun workingDirectory(path: File) {
            workingDir = path
        }

        fun defaultWait(duration: Int, unit: TimeUnit = TimeUnit.SECONDS) {
            waitFor = duration
            waitForUnit = unit
        }

        fun defaultArgs(vararg args: String) {
            defaultArgs += args
        }

        fun defaultArg(arg: String) {
            defaultArgs += arg
        }
    }

    companion object {
        operator fun invoke(init: Builder.() -> Unit = {}): UnrealKotlinCommandLine = Builder().apply(init).let(::UnrealKotlinCommandLine)
    }

}