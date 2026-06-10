#!/usr/bin/env python3
import os
import stat
import subprocess
from config import *
from updater_config import *

gradlew = ROOT / "gradlew"
if not os.access(gradlew, os.X_OK):
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR)

temp_dir = prepare_temp()

print("[1/5] Gerando fat JAR...")
run_gradle("clean", "shadowJar")
jar_file = find_jar()
shutil.copy(jar_file, temp_dir / "app.jar")

print("[2/5] Copiando JavaFX modules...")
copy_javafx(temp_dir)

print("[3/5] Gerando runtime com jlink...")
run_jlink(temp_dir)
copy_natives(temp_dir)

java_exe = temp_dir / "runtime" / "bin" / "java"
subprocess.run(
    [str(java_exe), "-Djava.library.path={}".format(temp_dir / "runtime" / "lib"),
     "-cp", str(temp_dir / "app.jar"), MAIN_CLASS],
    cwd=ROOT
)

print("[4/5] Gerando pacote .deb com updater...")

updater_props = temp_dir / "updater.properties"
updater_props.write_text(
    f"main-jar=app.jar\nmain-class={UPDATER_MAIN_CLASS}\n"
    f"app-args=\n"
)

run_jpackage(temp_dir, "deb", [
    "--linux-shortcut",
    "--linux-menu-group", "Office",
    "--linux-package-name", APP_NAME.lower().replace(" ", "-"),
    "--add-launcher", f"{UPDATER_NAME}={updater_props}",
])

print("[5/5] Renomeando pacote...")
final = rename_output("deb")
print(f"\nPacote .deb criado: {final}")
print(f"   Para instalar: sudo dpkg -i {final}")
