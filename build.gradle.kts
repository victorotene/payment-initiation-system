plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor") // ✅ Add this

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("org.reactivestreams:reactive-streams:1.0.4") // ✅ Add this

	implementation("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:postgresql")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("io.mockk:mockk:1.13.5")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
