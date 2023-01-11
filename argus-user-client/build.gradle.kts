plugins {
    `java-library`
    id("io.freefair.lombok")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val okHttpVersion: String by project
val retrofitVersion: String by project

dependencies {
    // OkHttp / Retrofit
    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("com.squareup.retrofit2:retrofit:${retrofitVersion}")
    implementation("com.squareup.retrofit2:converter-jackson:${retrofitVersion}")

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
