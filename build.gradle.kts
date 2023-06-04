plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.2.0"
    `java-gradle-plugin`
    `maven-publish`
    signing
}

fun property(key: String) = providers.gradleProperty(key).get()
fun environment(key: String) = providers.environmentVariable(key).get()

val kotlinVersion = property("kotlin.version")

tasks {
    wrapper {
        gradleVersion = "8.1.1"
    }
}

group = property("group")
version = property("version")

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
    website.set("https://github.com/IvanEOD/unreal-kotlin")
    vcsUrl.set("https://github.com/IvanEOD/unreal-kotlin")
    plugins {
        create("unreal-kotlin") {
            id = "unreal-kotlin"
            group = "com.detpros.unrealkotlin"
            version = project.version
            implementationClass = "com.detpros.unrealkotlin.UnrealKotlinPlugin"
            displayName = "Unreal Kotlin"
            description = "Unreal Kotlin Gradle Plugin for using Kotlin with Unreal Engine 5"
        }
    }
}
