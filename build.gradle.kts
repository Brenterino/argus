plugins {
    `maven-publish` apply false
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "TerraformersMC"
            url = uri("https://maven.terraformersmc.com/releases/")
        }
        maven {
            name = "Shedaniel"
            url = uri("https://maven.shedaniel.me/")
        }
    }
}

subprojects {
    group = "dev.zygon.argus"
    version = "1.0.0-SNAPSHOT"

    // Copy license into JAR META-INF folder
    tasks.withType<Jar> {
        into("META-INF") {
            from(project.rootDir) {
                include("LICENSE.md")
            }
        }
    }

    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/OWNER/REPOSITORY")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}
