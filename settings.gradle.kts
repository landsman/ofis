pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/stable")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "ofis"
