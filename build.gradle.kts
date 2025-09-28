plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // EclipseLink
    implementation("org.eclipse.persistence:eclipselink:4.0.2")
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.2")
    
    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.7")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Environment variables support
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    
    // WebSocket
    implementation("org.springframework:spring-websocket")
    implementation("org.springframework:spring-messaging")

    // Development tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    jar {
        enabled = false
    }

    // Настраиваем bootJar
    bootJar {
        enabled = true
        archiveFileName.set("app.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
