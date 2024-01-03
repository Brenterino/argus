rootProject.name = "argus"

include(
    "argus-auth-services",
    "argus-commons",
    "argus-commons-services",
    "argus-group-services",
    "argus-location-services",
    "argus-status-services",
    "argus-user-client",
    "argus-user-client:common",
    "argus-user-client:fabric",
    "argus-user-client:forge",
    "argus-web-ui"
)

pluginManagement {
    val quarkusVersion: String by settings
    val lombokPluginVersion: String by settings
    val jooqPluginVersion: String by settings
    val architecturyPluginVersion: String by settings
    val architecturyLoomPluginVersion: String by settings
    val fabricLoomPluginVersion: String by settings
    val shadowPluginVersion: String by settings
    val nodePluginVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Forge"
            url = uri("https://files.minecraftforge.net/maven/")
        }
    }
    plugins {
        id("io.quarkus") version quarkusVersion
        id("io.freefair.lombok") version lombokPluginVersion
        id("nu.studer.jooq") version jooqPluginVersion
        id("architectury-plugin") version architecturyPluginVersion
        id("dev.architectury.loom") version architecturyLoomPluginVersion
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
        id("com.github.node-gradle.node") version nodePluginVersion
    }
}
