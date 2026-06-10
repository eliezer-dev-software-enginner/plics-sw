#!/usr/bin/env python3
from config import *
from updater_config import *

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

print("[4/5] Gerando pacote MSI com updater...")

updater_props = temp_dir / "updater.properties"
updater_props.write_text(
    f"main-jar=app.jar\nmain-class={UPDATER_MAIN_CLASS}\n"
    f"app-args=\n"
)

run_jpackage(temp_dir, "msi", [
    "--win-menu",
    "--win-shortcut",
    "--win-per-user-install",
    "--win-upgrade-uuid", UPGRADE_UUID,
    "--add-launcher", f"{UPDATER_NAME}={updater_props}",
])

print("[5/5] Renomeando pacote...")
final = rename_output("msi")
shutil.rmtree(temp_dir, ignore_errors=True)
print(f"\nMSI criado: {final}")
