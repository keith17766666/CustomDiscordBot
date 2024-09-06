plugins {
    id("java")
}

group = "dev.keith"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.1.0") {
        // Optionally disable audio natives to reduce jar size by excluding `opus-java`
        // Kotlin DSL:
        // exclude(module="opus-java")
    }
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

tasks.test {
    useJUnitPlatform()
}