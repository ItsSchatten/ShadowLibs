plugins {
    `java-library`
    id("maven-publish")

    id("io.freefair.lombok") version "8.6"
    // Shade libraries into one "UberJar"
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    api("org.jetbrains:annotations:24.1.0")
    api("org.apache.commons:commons-lang3:3.15.0")

    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

group = "com.itsschatten.libs"
version = "1.0.10"
description = "ShadowLibs"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ItsSchatten/ShadowLibs")
            credentials {
                username = (project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME"))
                password = (project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN"))
            }
        }
    }

    publications.register<MavenPublication>("gpr") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
