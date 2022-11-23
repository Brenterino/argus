plugins {
	`java-library`
	id("io.freefair.lombok")
	id("org.javamodularity.moduleplugin")
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

	// Test Dependencies
	testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
	test {
		useJUnitPlatform()
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}
