plugins {
    id("java")
    id("maven-publish")
    id("application")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "megalodonte"
version = "1.0.0"

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

    implementation("megalodonte:simple-clipboard:1.0.0")
    implementation("megalodonte:components:1.0.0")
    implementation("megalodonte:reactivity:1.0.0")
    implementation("megalodonte:props:1.0.0")
    implementation("megalodonte:megalodonte-hot-preview:1.0.0")
    implementation("megalodonte:megalodonte-router:1.0.0-beta")
    implementation("megalodonte:megalodonte-styles:1.0.0")


    implementation("org.kordamp.ikonli:ikonli-core:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-antdesignicons-pack:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-entypo-pack:12.4.0")

    //sqlite
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    //implementation("megalodonte:megalodonte-previewer-components:1.0.0")

    // Dependências JavaFX removidas (agora gerenciadas pelo bloco 'javafx { ... }')
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("my_app.Main")
}

tasks.jar {
    enabled = true
    archiveBaseName.set("adb-file-pusher")

    manifest {
        attributes(
            "Implementation-Title" to "JavaFX adb-file-pusher app",
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

tasks.register<Exec>("createInstaller") {
    group = "distribution"
    description = "Gera o instalador .deb usando o script shell."

    // Garante que o JAR seja buildado antes de rodar o script
//    dependsOn("jar")
   // dependsOn("shadowJar")
    dependsOn("jar", "copyDependencies")

    // Define o diretório de execução como a raiz do projeto
    workingDir = projectDir

    // Comando para rodar o script
    commandLine("./scripts/linux/create-installer-using-gradlew.sh")
}

// Configuração de Publicação (mantida)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "adb-file-pusher"
        }
    }
}

