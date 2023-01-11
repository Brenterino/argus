rootProject.name = "argus"

include(
    "argus-auth-services",
    "argus-commons",
    "argus-group-services",
    "argus-location-services",
    "argus-name-services",
    "argus-user-client",
    "argus-user-client-fabric"
)

pluginManagement {
    val quarkusVersion: String by settings
    val lombokPluginVersion: String by settings
    val jooqPluginVersion: String by settings
    val fabricLoomPluginVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    plugins {
        id("io.quarkus") version quarkusVersion
        id("io.freefair.lombok") version lombokPluginVersion
        id("nu.studer.jooq") version jooqPluginVersion
        id("fabric-loom") version fabricLoomPluginVersion
    }
}
