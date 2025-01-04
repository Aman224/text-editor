plugins {
    id("java")
}

group = "org.aman"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.java.dev.jna:jna:5.16.0")
}

tasks.test {
    useJUnitPlatform()
}