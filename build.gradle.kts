plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

fun property(key: String) = providers.gradleProperty(key).get()
fun environment(key: String) = providers.environmentVariable(key).get()

val kotlinVersion = property("kotlin.version")

tasks {
    wrapper {
        gradleVersion = "8.1.1"
    }
}

allprojects {

    fun property(key: String) = providers.gradleProperty(key).get()
    fun environment(key: String) = providers.environmentVariable(key).get()

    group = property("group")
    version = property("version")

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


gradlePlugin {
    plugins {
        create("unreal-kotlin") {
            id = "unreal-kotlin"
            group = "com.detpros.unrealkotlin"
            version = project.version
            implementationClass = "com.detpros.unrealkotlin.UnrealKotlinPlugin"
        }
    }
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


publishing {

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/IvanEOD/unreal-kotlin")
            credentials {
                username = environment("GITHUB_PACKAGES_USERID") ?: "IvanEOD"
                password = environment("GITHUB_PACKAGES_PUBLISH_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
        }
    }
}
