import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

fun property(key: String) = providers.gradleProperty(key).get()
fun environment(key: String) = providers.environmentVariable(key).get()

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("reflect"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup:kotlinpoet:1.13.2")
    implementation("net.pearx.kasechange:kasechange-jvm:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.1")
}

tasks.withType<KotlinCompile<*>> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers=true"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            groupId = "com.detpros.unrealkotlin"
            artifactId = "declaration-generation"
            version = "1.0.0"
        }
    }
}