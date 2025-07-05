plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("io.ktor.plugin") version "2.3.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1") // For .env
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // For OpenAI API call
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.yusuf.insight.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}
