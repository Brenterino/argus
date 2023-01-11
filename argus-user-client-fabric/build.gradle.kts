plugins {
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

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappingsVersion}:v2")
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

    // Argus
    implementation(project(":argus-user-client"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
