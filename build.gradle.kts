plugins {

    application

    id("org.openjfx.javafxplugin") version "0.1.0"
}
group = "megalodonte"
version = providers.gradleProperty("appVersion").get()
repositories { mavenLocal(); mavenCentral() }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }
javafx { version = "25.0.1"; modules("javafx.controls", "javafx.graphics") }
dependencies {

    implementation("megalodonte:megalodonte-components:1.0.0-beta")
    implementation("megalodonte:megalodonte-base:1.0.0-beta")
    implementation("megalodonte:megalodonte-theme:1.0.0-beta")

    //logs
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    //json
    implementation("com.google.code.gson:gson:2.11.0")
    //soup
    implementation("org.jsoup:jsoup:1.23.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))

    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
application {

    mainClass.set("plics_sw_new.Main")

    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}
tasks.test { useJUnitPlatform() }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
