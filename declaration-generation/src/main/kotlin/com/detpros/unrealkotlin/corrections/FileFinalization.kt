package com.detpros.unrealkotlin.corrections

import java.io.File


/**
 *  File Finalization
 *
 * @author IvanEOD ( 6/1/2023 at 3:36 PM EST )
 */
class FileFinalization(
    private val file: File,
    private val text: String,
) {
    private val _lines = text.lines()
    private val _beforeImports = mutableListOf<String>()
    private val _afterImports = mutableListOf<String>()
    private val _imports = mutableSetOf<String>()

    init {
        process()
    }


    private fun process() {
        var importsStarted = false
        var importsFirstLine = 0
        var importsLastLine = 0


        for (line in _lines) {
            if (line.startsWith("import ")) {
                if (!importsStarted) {
                    importsFirstLine = _lines.indexOf(line)
                    importsStarted = true
                }
                _imports.add(line.substringAfter("import ").trim())
            } else {
                if (importsStarted) {
                    importsLastLine = _lines.indexOf(line)
                    importsStarted = false
                }
                val newLine = line.removeBackTicks().removeRemoves()
                if (importsLastLine > 0) _afterImports.add(newLine)
                else _beforeImports.add(newLine)
            }
        }
//        _imports.removeIf { it.startsWith("tsstdlib") }
//        for (import in ensureImports) {
//            if (import !in _imports) _imports.add(import)
//        }
        val _imports = imports[file.nameWithoutExtension] ?: setOf()
        if (file.nameWithoutExtension == "UE0") _afterImports.removeIf { it.trim() == "public external var Root: dynamic" }
        val before = _beforeImports.joinToString("\n")
        val imports = _imports.filter { !it.isNotImport() }.sorted().joinToString("\n") { "import $it" }
        val after = _afterImports.joinToString("\n")



        file.writeText("$before\n\n$imports\n\n$after")

    }

    private fun String.isNotImport(): Boolean = isNotEmpty() && !contains(".") && matches("^\\w+$".toRegex())

    private fun String.removeBackTicks(): String {
        val matches = backtickRegex.findAll(this)
        var result = this
        for (match in matches) {
            val replacement = match.groupValues[1]
            if (replacement in keywords) continue
            result = result.replace(match.value, replacement)
        }
        return result
    }

    private fun String.removeRemoves(): String {
        var result = this
        for (remove in removes) {
            result = result.replace(remove, "")
        }
        return result
    }


    companion object {
        private val backtickRegex = "`([^`]+)`".toRegex()

        private val removes = setOf(
            "tsstdlib."
        )

        private val keywords = setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )

        private val imports = mapOf(
            "UE" to setOf("org.w3c.dom.events.Event"),
            "UE0" to setOf("org.khronos.webgl.ArrayBuffer", "kotlin.js.Console"),
            "UE1" to setOf("org.w3c.dom.events.Event"),
            "UE2" to setOf("org.w3c.dom.AddEventListenerOptions", "org.w3c.dom.EventListenerOptions", "org.w3c.dom.events.EventListener"),
            "UE3" to setOf("org.w3c.dom.events.Event"),
            "UE4" to setOf(),
        )


        private val ensureImports = setOf(
            "kotlin.js.*",
            "org.khronos.webgl.*",
            "org.w3c.dom.*",
            "org.w3c.dom.events.*",
            "org.w3c.dom.parsing.*",
            "org.w3c.dom.svg.*",
            "org.w3c.dom.url.*",
            "org.w3c.fetch.*",
            "org.w3c.files.*",
            "org.w3c.notifications.*",
            "org.w3c.performance.*",
            "org.w3c.workers.*",
            "org.w3c.xhr.*",
        )


    }


}