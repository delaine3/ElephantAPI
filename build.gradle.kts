plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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

    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.pdfbox:pdfbox:2.0.1")
    implementation("org.apache.pdfbox:fontbox:2.0.0")
    implementation("org.apache.pdfbox:jempbox:1.8.11")
    implementation("org.apache.pdfbox:xmpbox:2.0.0")
    implementation("org.apache.pdfbox:preflight:2.0.0")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.0")
    implementation("software.amazon.awssdk:sesv2:2.26.20")
    implementation("software.amazon.awssdk:s3:2.20.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mssqlserver")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
