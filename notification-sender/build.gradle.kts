dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

    implementation("io.opentelemetry:opentelemetry-extension-kotlin")
    implementation("io.opentelemetry.instrumentation:opentelemetry-reactor-3.1:1.23.0-alpha")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:1.23.0-alpha") {
        exclude("io.opentelemetry", "opentelemetry-exporter-logging")
        exclude("io.opentelemetry", "opentelemetry-exporter-otlp")
    }
    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-aws-sdk-2.2-autoconfigure:1.23.0-alpha")

    implementation("software.amazon.awssdk:sqs")
}
