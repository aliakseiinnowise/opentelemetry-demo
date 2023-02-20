dependencies {
    api("io.opentelemetry:opentelemetry-context")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-reactor-3.1:1.23.0-alpha")
}
