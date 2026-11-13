plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
    compileOnly("com.google.code.gson:gson:2.11.0")
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("PremiumOrCracked-Velocity-${project.version}.jar")
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
