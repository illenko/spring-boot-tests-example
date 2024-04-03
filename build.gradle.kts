import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("info.solidsoft.pitest") version "1.15.0"
    id("se.solrike.sonarlint") version "2.0.0"
}

group = "com.illenko"
version = "0.0.1-SNAPSHOT"

val kotlinLoggingVersion: String by project
val mockkVersion: String by project
val springMockkVersion: String by project
val instancioVersion: String by project
val detektVersion: String by project
val testContainersVersion: String by project
val wiremockVersion: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework:spring-jdbc")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    testImplementation("org.instancio:instancio-junit:$instancioVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("com.h2database:h2")
    testImplementation("io.r2dbc:r2dbc-h2")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    sonarlintPlugins("org.sonarsource.kotlin:sonar-kotlin-plugin:2.20.0.4382")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

koverReport {
    filters {
        excludes {
            classes(
                "*ApplicationKt",
            )
        }
    }

    verify {
        rule {
            isEnabled = true
            bound { minValue = 95 }
        }
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    version.set("0.48.2")

    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
}

pitest {
    junit5PluginVersion = "1.2.1"
    threads.set(4)
    avoidCallsTo.set(setOf("kotlin.jvm.internal"))
}

detekt {
    config.from("gradle/config/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        txt.required.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeTags("UnitTest", "IntegrationTest", "FunctionalTest")
    }
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}
