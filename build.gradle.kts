plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "ch.nmeylan.plugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java"))
}

dependencies {

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.4"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")

    testCompileOnly("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation("org.springframework:spring-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        useJUnitPlatform()
    }
}
