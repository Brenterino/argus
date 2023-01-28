import org.jooq.meta.jaxb.*

plugins {
    java
    id("io.freefair.lombok")
    id("io.quarkus")
    id("nu.studer.jooq")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val quarkusVersion: String by project
val jooqVersion: String by project
val assertJVersion: String by project

dependencies {
    // Quarkus BoM
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}"))

    // Quarkus
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-agroal")
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")

    // jOOQ
    implementation("org.jooq:jooq:${jooqVersion}")
    jooqGenerator("org.jooq:jooq-codegen:${jooqVersion}")
    jooqGenerator("org.jooq:jooq-meta-extensions:${jooqVersion}")

    // Argus
    implementation(project(":argus-commons"))

    // Slf4j
    implementation("org.slf4j:slf4j-api")
    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")

    // Test Dependencies
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core:${assertJVersion}")
    testImplementation("io.rest-assured:rest-assured")

    // Image/Deploy Dependencies
    implementation("io.quarkus:quarkus-container-image-docker-deployment")
}

jooq {
    version.set(jooqVersion)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties = listOf(
                            Property().apply {
                                key = "scripts"
                                value = "src/main/resources/db/migration/*DDL.sql"
                            },
                            Property().apply {
                                key = "defaultNameCase"
                                value = "lower"
                            })
                    }
                }
            }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
