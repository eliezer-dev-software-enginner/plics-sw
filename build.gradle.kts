import java.util.Properties

plugins {
    id("java")
    id("maven-publish")
    id("application")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"
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

    // Mockito
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")

    implementation("megalodonte:megalodonte-base:1.0.0-beta")
    implementation("megalodonte:megalodonte-components:1.0.0-beta")
    implementation("megalodonte:megalodonte-reactivity:1.0.0-beta")
    implementation("megalodonte:megalodonte-router:1.0.0-beta")
    implementation("megalodonte:megalodonte-theme:1.0.0-beta")
    implementation("megalodonte:megalodonte-async:1.0.0-beta")


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

tasks.jar {
    enabled = true
    archiveBaseName.set(props.getProperty("appName"))

    manifest {
        attributes(
            "Implementation-Title" to "JavaFX ${props.getProperty("appName")} app",
            "Implementation-Version" to project.version
        )
    }
}
//no caso vai copiar os jar dinamicamente que a aplicação ta usando
// Crie uma tarefa para copiar todas as dependências de runtime
val copyDeps = tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("dependencies"))

    // Evita duplicar o que já vai estar no JRE via JLink
    exclude("org/openjfx/**")
}

tasks.register<Exec>("createInstallerLinux") {
    group = "distribution"
    description = "Gera o instalador .deb usando o script shell."

    dependsOn("jar", "copyDependencies")

    // Define o diretório de execução como a raiz do projeto
    workingDir = projectDir

    // Comando para rodar o script
    commandLine("./scripts/linux/create-installer-using-gradlew.sh")
}

tasks.register<Exec>("createInstallerLinuxOptimized") {
    group = "distribution"
    description = "Gera o instalador .deb otimizado usando o script shell."

    dependsOn("jar", "copyDependencies")

    workingDir = projectDir

    commandLine("./scripts/linux/create-installer-using-gradlew-optimized.sh")
}

tasks.register<Exec>("createInstallerWindows") {
    group = "distribution"
    description = "Gera o instalador .msi usando o script PowerShell."

    dependsOn("jar", "copyDependencies")

    // Define o diretório de execução como a raiz do projeto
    workingDir = projectDir

    // Comando para rodar o script
    commandLine("pwsh", "./scripts/windows/create-installer-using-gradlew.ps1")
}

tasks.register<Exec>("createFastExeWindows") {
    group = "distribution"
    description = "Gera o executável .exe rápido usando o script PowerShell."

    dependsOn("jar", "copyDependencies")

    workingDir = projectDir

    commandLine("pwsh", "./scripts/windows/create-fast-exe-using-gradlew.ps1")
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
