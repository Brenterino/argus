import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("io.freefair.lombok")
    id("com.github.johnrengelman.shadow")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val okHttpVersion: String by project
val retrofitVersion: String by project
val jacksonVersion: String by project
val javalinVersion: String by project

dependencies {
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

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val shadowJar = tasks.withType<ShadowJar> {
    archiveClassifier.convention("")
    archiveClassifier.set("")
    exclude("META-INF/LICENSE*")
}

tasks.getByName("build") {
    dependsOn(shadowJar)
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
