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
    }

}