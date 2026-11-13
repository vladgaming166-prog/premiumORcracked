rootProject.name = "premiumorcracked"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }
}

include("api", "common", "velocity", "paper")
