plugins {
    java
    `maven-publish`
    id("fabric-loom")
    id("io.freefair.lombok")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val minecraftVersion: String by project
val yarnMappingsVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val modMenuVersion: String by project
val clothConfigVersion: String by project
val okHttpVersion: String by project
val retrofitVersion: String by project
val javalinVersion: String by project

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappingsVersion}:v2")
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

    // ModMenu
    modImplementation("com.terraformersmc:modmenu:${modMenuVersion}")

    // Cloth Config
    modApi("me.shedaniel.cloth:cloth-config-fabric:${clothConfigVersion}") {
        exclude("net.fabricmc.fabric-api")
    }

    // Implementation Dependencies
    implementation(project(":argus-commons"))
    implementation(project(":argus-user-client"))
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("com.squareup.retrofit2:retrofit:${retrofitVersion}")
    implementation("io.javalin:javalin:${javalinVersion}")

    // 'Shade' these Dependencies into our JAR
    include(project(":argus-user-client"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    // Warning: this is hacky AF :)
    dependsOn(":argus-web-ui:build")
    into("public") {
        val dist = project(":argus-web-ui")
            .layout
            .projectDirectory
            .dir("dist")
            .asFile
        println(dist.path)
        from(dist) {
            include()
        }
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>(rootProject.name) {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Brenterino/argus")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
