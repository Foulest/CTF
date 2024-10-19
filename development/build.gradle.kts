plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("xyz.jpenilla.run-paper") version "2.2.3" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.readutf.inari.development"
version = "1.0-SNAPSHOT"

// Set the language level to Java 17
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central") }

    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") }

    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }

    mavenLocal()
}

dependencies {
    implementation(project(":core"))

    compileOnly(paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("net.kyori:adventure-api:4.14.0")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Spark - for performance monitoring
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")

    // JetBrains Annotations - for code inspection and documentation
    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Lombok - for reducing boilerplate code
    // https://projectlombok.org
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.readutf.minigame"
            artifactId = "development"
            version = "1.1"

            from(components["java"])
        }
    }
}

tasks.assemble {
    dependsOn("reobfJar")
}

tasks.test {
    useJUnitPlatform()
}
