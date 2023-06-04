plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.2.0"
    `java-gradle-plugin`
    `maven-publish`
    signing
}

fun properties(key: String) = providers.gradleProperty(key).orNull
fun environment(key: String) = providers.environmentVariable(key).orNull

val kotlinVersion = properties("kotlin.version")

tasks {
    wrapper {
        gradleVersion = "8.1.1"
    }
}

group = properties("group")!!
version = properties("version")!!

allprojects {

    fun properties(key: String) = providers.gradleProperty(key).get()
    fun environment(key: String) = providers.environmentVariable(key).get()

    group = properties("group")
    version = properties("version")

    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/IvanEOD/declaration-generation")
            credentials {
                username = environment("GITHUB_PACKAGES_USERID")
                password = environment("GITHUB_PACKAGES_IMPORT_TOKEN")
            }
        }
    }

}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("reflect"))
    implementation("com.squareup:kotlinpoet:1.13.2")
    implementation("com.detpros.unrealkotlin:declaration-generation:1.0.5")
}


tasks.register("pushNextVersion") {
    group = "git"
    doLast {
        val v = project.version as String
        val versionParts = v.split(".")
        val nextVersion = "${versionParts[0]}.${versionParts[1]}.${versionParts[2].toInt() + 1}"
        println("Next version: $nextVersion")
        project.version = nextVersion
        val gradleProperties = project.file("gradle.properties")
        val gradlePropertiesText = gradleProperties.readText()
        val newGradlePropertiesText = gradlePropertiesText.replace("version=${v}", "version=${nextVersion}")
        gradleProperties.writeText(newGradlePropertiesText)
        try {
            runCommands("git tag -d $nextVersion")
        } catch (_: Exception) { }
        runCommands(
            "git add .",
            "git commit -m \"Version bump to $nextVersion\"",
            "git status",
            "git tag $nextVersion",
            "git push --tags",
            "git push",
        )
    }
}

tasks.register("pushRelease") {
    group = "git"
    doLast {
        val v = project.version as String

        try {
            runCommands("git tag -d $v")
        } catch (_: Exception) { }

        runCommands(
            "git add .",
            "git commit -m \"Updating version $v\"",
            "git status",
            "git tag $v",
            "git push --tags",
            "git push",
        )
    }
}

fun runCommands(vararg commands: String) {
    commands.forEach {
        println("Running command: $it")
        val process = Runtime.getRuntime().exec(it)
        process.waitFor()
        val output = process.inputStream.bufferedReader().readText()
        if (output.isNotEmpty()) println("Command output: $output")
    }
}


gradlePlugin {
    website.set("https://github.com/IvanEOD/unreal-kotlin")
    vcsUrl.set("https://github.com/IvanEOD/unreal-kotlin")
    plugins {
        create("unreal-kotlin") {
            id = "com.detpros.unrealkotlin.unreal-kotlin"
            group = "com.detpros.unrealkotlin"
            version = project.version
            implementationClass = "com.detpros.unrealkotlin.UnrealKotlinPlugin"
            displayName = "Unreal Kotlin"
            description = "Unreal Kotlin Gradle Plugin for using Kotlin with Unreal Engine 5"
            tags.set(setOf("unreal", "engine", "unreal-engine", "kotlin"))
        }
    }
}


signing {
    val signingKeyPath = environment("GRADLE_SIGNING_KEY_PATH")
    val signingKey = environment("GRADLE_SIGNING_KEY") ?: file(signingKeyPath!!).readText()
    val signingPassword = environment("GRADLE_SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}