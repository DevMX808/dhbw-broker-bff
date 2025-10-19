plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.7.2"
}

group = "com.dhbw.broker"
version = "0.0.1-SNAPSHOT"
description = "dhbw-broker-bff"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories { mavenCentral() }

dependencyManagement {
    imports { mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:3.3.0") }
}
buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.7.2")
        classpath("org.postgresql:postgresql:42.7.4")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.flywaydb:flyway-core:11.7.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.7.2")
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

flyway {

    schemas = arrayOf("broker")
    defaultSchema = "broker"
    createSchemas = true

    url = System.getenv("SPRING_DATASOURCE_URL")
    user = System.getenv("SPRING_DATASOURCE_USERNAME")
    password = System.getenv("SPRING_DATASOURCE_PASSWORD")

    locations = arrayOf("classpath:db/migration")
    cleanDisabled = true
    baselineOnMigrate = true
}

tasks.withType<Test> { useJUnitPlatform() }