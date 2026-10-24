rootProject.name = "plics-sw"

val frameworkDir = file("../../framework")
if (frameworkDir.exists()) {
    val libs = file("../../framework/libraries")
    if (libs.exists()) {
        libs.listFiles()?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }?.forEach { lib ->
            includeBuild(lib)
        }
    }
    val jsonc = file("../../framework/megalodonte-declarative/megalodonte-jsonc-ui")
    if (jsonc.exists()) {
        includeBuild(jsonc)
    }
    val scriptApp = file("../../framework/megalodonte-declarative/megalodonte-script-app")
    if (scriptApp.exists()) {
        includeBuild(scriptApp)
    }
    val packaging = file("../../framework/megalodonte-packaging")
    if (packaging.exists()) {
        includeBuild(packaging)
    }
    val ecossystem = file("../../framework/megalodonte-ecossystem")
    if (ecossystem.exists()) {
        includeBuild(ecossystem)
    }
}
