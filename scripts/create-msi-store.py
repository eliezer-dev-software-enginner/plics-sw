#!/usr/bin/env python3
# Build específico para distribuição via Microsoft Store — a Store tem seu próprio
# mecanismo de atualização automática, então o runtime recebe
# -Dplics.microsoftStore=true (Main.isMicrosoftStore), que faz o app esconder o item
# "Buscar atualização" do menu Suporte. Pra build normal (site, GitHub Releases),
# use create-msi.py.
from config import *

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

print("[4/5] Gerando pacote MSI (Microsoft Store, sem updater)...")
run_jpackage(temp_dir, "msi", [
    "--win-menu",
    "--win-shortcut",
    "--win-per-user-install",
    "--win-upgrade-uuid", UPGRADE_UUID,
    "--java-options", "-Dplics.microsoftStore=true",
])

print("[5/5] Renomeando pacote...")
final = rename_output("msi")
shutil.rmtree(temp_dir, ignore_errors=True)
print(f"\nMSI criado: {final}")
open_dist_folder()
