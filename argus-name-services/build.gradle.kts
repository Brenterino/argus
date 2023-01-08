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

dependencies {
    // Quarkus BoM
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}"))

    // Slf4j
    implementation("org.slf4j:slf4j-api")
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")

    // Test Dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
