plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13" // For JavaFX
}

group = "com.cesarbahena"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")

    // Use JUnit Jupiter for testing.
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    // Define the main class for the application.
    mainClass.set("com.cesarbahena.bara.app.App")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}
