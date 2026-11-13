plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
    compileOnly("com.google.code.gson:gson:2.11.0")
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("PremiumOrCracked-Paper-${project.version}.jar")
        mergeServiceFiles()
        exclude("META-INF/maven/**")
        dependencies {
            exclude(dependency("net.kyori:.*"))
            exclude(dependency("com.google.code.gson:.*"))
            exclude(dependency("org.jetbrains:.*"))
        }
        relocate("org.yaml.snakeyaml", "com.premiumorcracked.libs.yaml")
    }

    assemble {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}
