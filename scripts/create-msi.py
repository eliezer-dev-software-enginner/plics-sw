from config import *

temp_dir = prepare_temp()

print("[1/6] 📦 Gerando fat JAR da aplicação...")
run_gradle("clean", "shadowJar")
jar_file = find_jar()
shutil.copy(jar_file, temp_dir / "app.jar")

print("[2/6] 📦 Gerando fat JAR do updater...")
build_updater(temp_dir)

print("[3/6] 📚 Copiando JavaFX modules...")
copy_javafx(temp_dir)

print("[4/6] ⚙️  Gerando runtime com jlink...")
run_jlink(temp_dir)
copy_natives(temp_dir)

java_exe = temp_dir / "runtime" / "bin" / "java"
subprocess.run(
    [str(java_exe), "-Djava.library.path={}".format(temp_dir / "runtime" / "lib"),
     "-cp", str(temp_dir / "app.jar"), MAIN_CLASS],
    cwd=ROOT
)

print("[5/6] 🎁 Gerando pacote MSI...")
run_jpackage(temp_dir, "msi", [
    "--win-menu",
    "--win-shortcut",
    "--win-per-user-install",
])

print("[6/6] 📝 Renomeando pacote...")
final = rename_output("msi")
print(f"\n✅ MSI criado: {final}")
