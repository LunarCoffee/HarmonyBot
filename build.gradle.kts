plugins { kotlin("jvm") version "1.3.61" }

group = "dev.lunarcoffee"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("net.dv8tion:JDA:4.1.1_108")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("io.ktor:ktor-server-core:1.3.0")
    implementation("io.ktor:ktor-server-netty:1.3.0")
    implementation("io.ktor:ktor-gson:1.3.0")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
