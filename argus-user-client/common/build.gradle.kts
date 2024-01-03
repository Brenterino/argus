plugins {
    id("io.freefair.lombok")
    id("architectury-plugin")
    id("dev.architectury.loom")
}

val minecraftVersion: String by project
val yarnMappingsVersion: String by project
val fabricLoaderVersion: String by project
val clothConfigVersion: String by project
val okHttpVersion: String by project
val retrofitVersion: String by project
val jacksonVersion: String by project
val javalinVersion: String by project

architectury {
    injectInjectables = false
    common("forge", "fabric")
}

loom {
    mixin {
        defaultRefmapName = "argus.refmap.json"
    }
}

dependencies {
    // Fabric Loader Mixin Magic
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    // Minecraft
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappingsVersion}:v2")

    // Cloth Config
    modApi("me.shedaniel.cloth:cloth-config-fabric:${clothConfigVersion}") {
        exclude("net.fabricmc.fabric-api")
    }

    // OkHttp / Retrofit
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("com.squareup.retrofit2:retrofit:${retrofitVersion}")
    implementation("com.squareup.retrofit2:converter-jackson:${retrofitVersion}")

    // Jackson
    implementation(enforcedPlatform("com.fasterxml.jackson:jackson-bom:${jacksonVersion}"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Javalin (shading for downstream project)
    implementation("io.javalin:javalin:${javalinVersion}") {
        exclude("org.ow2.asm") // do not include OW2 ASM
    }

    // Argus
    implementation(project(":argus-commons"))
}
