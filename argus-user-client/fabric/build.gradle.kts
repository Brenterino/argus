plugins {
    `maven-publish`
    id("io.freefair.lombok")
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    mixin {
        defaultRefmapName = "argus.refmap.json"
    }
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating

configurations {
    compileOnly.configure {
        extendsFrom(common)
    }
    runtimeOnly.configure {
        extendsFrom(common)
    }
}

val minecraftVersion: String by project
val yarnMappingsVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val modMenuVersion: String by project
val clothConfigVersion: String by project
val okHttpVersion: String by project
val retrofitVersion: String by project
val jacksonVersion: String by project
val javalinVersion: String by project

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappingsVersion}:v2")

    // Fabric
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    // Fabric API
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

    // ModMenu
    modCompileOnly("com.terraformersmc:modmenu:${modMenuVersion}") {
        isTransitive = false
    }
    modLocalRuntime("com.terraformersmc:modmenu:${modMenuVersion}") {
        isTransitive = false
    }

    // Cloth Config
    modApi("me.shedaniel.cloth:cloth-config-fabric:${clothConfigVersion}") {
        exclude("net.fabricmc.fabric-api")
    }

    common(project(":argus-user-client:common", "namedElements")) {
        isTransitive = false
    }

    // Dependencies to Shade
    // OkHttp / Retrofit
    shadowCommon("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    shadowCommon("com.squareup.retrofit2:retrofit:${retrofitVersion}")
    shadowCommon("com.squareup.retrofit2:converter-jackson:${retrofitVersion}")

    // Jackson
    shadowCommon(enforcedPlatform("com.fasterxml.jackson:jackson-bom:${jacksonVersion}"))
    shadowCommon("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Javalin (shading for downstream project)
    shadowCommon("io.javalin:javalin:${javalinVersion}") {
        exclude("org.ow2.asm") // do not include OW2 ASM
    }

    // Argus
    shadowCommon(project(":argus-commons"))

    shadowCommon(project(":argus-user-client:common", "transformProductionFabric")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("shadow")
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

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("orig")
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>(rootProject.name) {
            from(components["java"])
            artifactId = rootProject.name + "-" + minecraftVersion + "-" + project.name
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
