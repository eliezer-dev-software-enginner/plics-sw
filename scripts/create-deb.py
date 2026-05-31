#!/usr/bin/env python3
import os
import stat
import subprocess
from config import *

gradlew = ROOT / "gradlew"
if not os.access(gradlew, os.X_OK):
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR)

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

print("[5/6] 🎁 Gerando pacote .deb...")
run_jpackage(temp_dir, "deb", [
    "--linux-shortcut",
    "--linux-menu-group", "Office",
    "--linux-package-name", APP_NAME.lower().replace(" ", "-"),
])

print("[6/6] 📝 Renomeando pacote...")
final = rename_output("deb")
print(f"\n✅ Pacote .deb criado: {final}")
print(f"   Para instalar: sudo dpkg -i {final}")
