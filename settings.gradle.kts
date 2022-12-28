rootProject.name = "argus"

include(
    "argus-auth-services",
    "argus-commons",
    "argus-group-services",
    "argus-location-services"
)

pluginManagement {
    val quarkusVersion: String by settings
    val lombokPluginVersion: String by settings
    val jooqPluginVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus") version quarkusVersion
        id("io.freefair.lombok") version lombokPluginVersion
        id("nu.studer.jooq") version jooqPluginVersion
    }
}
