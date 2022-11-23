plugins {
    java
    id("io.freefair.lombok")
    id("io.quarkus")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val quarkusVersion: String by project
val assertJVersion: String by project

dependencies {
    // Quarkus BoM
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}"))

    // Quarkus
    implementation("io.quarkus:quarkus-websockets")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

    // Argus
    implementation(project(":argus-commons"))

    // Slf4j
    implementation("org.slf4j:slf4j-api")
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")

    // Test Dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${assertJVersion}")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
