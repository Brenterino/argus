allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "TerraformersMC"
            url = uri("https://maven.terraformersmc.com/releases/")
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
}
