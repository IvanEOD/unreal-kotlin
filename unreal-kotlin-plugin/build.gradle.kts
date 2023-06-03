plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

fun property(key: String) = providers.gradleProperty(key).get()
fun environment(key: String) = providers.environmentVariable(key).get()

val kotlinVersion = property("kotlin.version")

dependencies {
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("reflect"))
    implementation("com.squareup:kotlinpoet:1.13.2")

//    implementation(project(":declaration-generation"))
}

//
//gradlePlugin {
//    plugins {
//        create("unreal-kotlin") {
//            id = "unreal-kotlin"
//            group = "com.detonate-productions.unreal-kotlin"
//            version = "1.0.0"
//            implementationClass = "com.detpros.plugin.UnrealKotlinPlugin"
//        }
//    }
//}
