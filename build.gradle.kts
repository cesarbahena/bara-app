plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13" // For JavaFX
    id("nu.studer.jooq") version "8.2" // For jOOQ code generation
    id("org.jetbrains.kotlin.jvm") version "1.9.23" // Retained for compatibility
}

group = "com.bara"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // SQLite JDBC Driver
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")

    // Flyway for database migrations
    implementation("org.flywaydb:flyway-core:9.22.3")

    // jOOQ Core
    implementation("org.jooq:jooq:3.18.9")

    // jOOQ Code Generator (for build-time use)
    jooqGenerator("org.jooq:jooq-codegen:3.18.9")
    jooqGenerator("org.jooq:jooq-meta-extensions:3.18.9")
    jooqGenerator("org.xerial:sqlite-jdbc:3.46.0.0") // SQLite driver for jOOQ generation
    jooqGenerator("org.slf4j:slf4j-simple:1.7.36") // Logging for jOOQ generation


    // Use JUnit Jupiter for testing.
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

jooq {
    version.set("3.18.9")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.sqlite.JDBC"
                    url = "jdbc:sqlite:bara.db"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        // SQLite doesn't support schemas, so we don't set inputSchema
                        excludes = "flyway_schema_history"
                    }
                    target.apply {
                        packageName = "com.bara.app.db.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isPojos = true
                        isImmutablePojos = false
                        isFluentSetters = true
                        isDaos = true
                    }
                }
            }
        }
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.bara.app.App")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceSets {
        main {
            java.srcDir("build/generated-src/jooq/main")
        }
    }
}


javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}
