#!/usr/bin/env python3
"""
Builda o Plics SW como app-image (jpackage, mesmo runtime jlink que o .deb usa) e
monta uma instalação Flatpak local via flatpak-builder --user --install, só pra
testar. Não publica nada no Flathub — isso ainda exige abrir um Pull Request manual
em github.com/flathub/flathub, revisão deles, etc.

Requer: flatpak e flatpak-builder instalados, e os runtimes
org.freedesktop.Platform//24.08 e org.freedesktop.Sdk//24.08 (o script não instala
esses dois pra você — se não tiver, `flatpak install flathub org.freedesktop.Platform//24.08
org.freedesktop.Sdk//24.08` primeiro).
"""
import os
import shutil
import stat
import subprocess
from config import *

APP_ID = "io.github.eliezerdevsoftwareenginner.PlicsSW"
FLATPAK_DIR = ROOT / "flatpak"

gradlew = ROOT / "gradlew"
if not os.access(gradlew, os.X_OK):
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR)

for tool in ("flatpak", "flatpak-builder"):
    if shutil.which(tool) is None:
        raise EnvironmentError(f"'{tool}' não encontrado no PATH. Instale antes de rodar este script.")

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

smoke_test(temp_dir)

print("[4/5] 🧩 Gerando app-image (jpackage)...")
run_jpackage(temp_dir, "app-image")

app_image_dir = ROOT / "dist" / APP_NAME
if not app_image_dir.exists():
    raise FileNotFoundError(f"app-image não encontrado em {app_image_dir}")

build_input_dir = FLATPAK_DIR / "app-image"
shutil.rmtree(build_input_dir, ignore_errors=True)
shutil.copytree(app_image_dir, build_input_dir)

print("[5/5] 🏗️  flatpak-builder (build + install --user)...")
subprocess.run(
    [
        "flatpak-builder", "--user", "--install", "--force-clean",
        str(FLATPAK_DIR / "_build"), str(FLATPAK_DIR / f"{APP_ID}.yml"),
    ],
    cwd=FLATPAK_DIR, check=True,
)

print(f"\n✅ Instalado localmente. Teste com: flatpak run {APP_ID}")
print(f"   Pra desinstalar: flatpak uninstall {APP_ID}")
