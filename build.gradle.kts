import java.util.Properties

plugins {
    id("java")
    id("maven-publish")
    id("application")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"

    //shadow jar para iconly funcionar
    //id("com.github.johnrengelman.shadow") version "8.1.1" (NÃO FUNCIONA)
    id("com.gradleup.shadow") version "8.3.5"
}

val props = Properties()
file("gradle.properties").inputStream().use { props.load(it) }

group = "plicssw"
version = props.getProperty("appVersion")

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


// 🛑 2. CONFIGURA O PLUGIN DO JAVAFX
javafx {
    // Define a versão do JavaFX para ser usada em todos os módulos
    version = "17" // Mantida a versão 17.0.10.

    // Lista os módulos JavaFX que sua biblioteca PRECISA para compilar.
    // O plugin adiciona automaticamente a dependência para a sua plataforma de build.

    //esse meu projeto como é simples, só o modulo de controls e graphics foi o suficiente
    //modules("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.media", "javafx.web")
    modules("javafx.controls", "javafx.graphics")
}

dependencies {
    // Dependências de teste (mantidas)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("megalodonte:megalodonte-base:1.0.0-beta")
    implementation("megalodonte:megalodonte-components:1.0.0-beta")
    implementation("megalodonte:megalodonte-reactivity:1.0.0-beta")
    implementation("megalodonte:megalodonte-router:1.0.0-beta")
    implementation("megalodonte:megalodonte-theme:1.0.0-beta")


    implementation("org.kordamp.ikonli:ikonli-core:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-antdesignicons-pack:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-entypo-pack:12.4.0")

    //sqlite
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    //componentes gemfx
    //implementation("com.dlsc.gemsfx:gemsfx:2.16.0")

    //implementation("megalodonte:megalodonte-previewer-components:1.0.0")

    // Dependências JavaFX removidas (agora gerenciadas pelo bloco 'javafx { ... }')
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set(props.getProperty("appMainClass"))
}


tasks.shadowJar {
    archiveBaseName.set(props.getProperty("appName"))
    archiveClassifier.set("")
    mergeServiceFiles() // equivalente ao ServicesResourceTransformer

    manifest {
        attributes(
            "Main-Class" to props.getProperty("appMainClass")
        )
    }

    // Exclui JavaFX (como o pom.xml fazia)
    exclude("org/openjfx/**")

    // 🔥 remove assinaturas quebradas (igual no Maven)
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

tasks.jar {
    enabled = false
    archiveBaseName.set(props.getProperty("appName"))

    manifest {
        attributes(
            "Implementation-Title" to "JavaFX ${props.getProperty("appName")} app",
            "Implementation-Version" to project.version,
            "Main-Class" to props.getProperty("appMainClass")
        )
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


// Configuração de Publicação (mantida)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = props.getProperty("appName")
        }
    }
}
