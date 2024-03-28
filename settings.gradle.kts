pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My QuiizApp"
include(":app")
 