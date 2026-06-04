from config import *

temp_dir = prepare_temp()

print("[1/5] 📦 Gerando fat JAR...")
run_gradle("clean", "shadowJar")
jar_file = find_jar()
shutil.copy(jar_file, temp_dir / "app.jar")

print("[2/5] 📚 Copiando JavaFX modules...")
copy_javafx(temp_dir)

print("[3/5] ⚙️  Gerando runtime com jlink...")
run_jlink(temp_dir)
copy_natives(temp_dir)

print("[4/5] 🎁 Gerando pacote MSI...")
run_jpackage(temp_dir, "msi", [
    "--win-menu",
    "--win-shortcut",
    "--win-per-user-install",
])

print("[5/5] 📝 Renomeando pacote...")
final = rename_output("msi")
print(f"\n✅ MSI criado: {final}")
