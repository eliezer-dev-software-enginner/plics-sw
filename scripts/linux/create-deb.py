#!/usr/bin/env python3
import subprocess
import shutil
import os
import stat
from pathlib import Path

# ============================================================================
# CONFIGURAÇÕES (AJUSTE CONFORME SEU PROJETO)
# ============================================================================
APP_NAME = "Plics SW"
APP_VERSION = "1.0.3"
MAIN_CLASS = "my_app.Main"                         # Classe principal do seu aplicativo
ICON_PATH = "src/main/resources/assets/app_ico.png"   # Ícone no formato PNG

# Versão do JavaFX (deve corresponder à pasta em java_fx_modules)
JAVAFX_VERSION = "25.0.1"
JAVAFX_PLATFORM = "linux"                          # linux, windows, mac
JAVAFX_DIR = f"java_fx_modules/{JAVAFX_PLATFORM}-{JAVAFX_VERSION}"

# Tipo de pacote fixo: .deb
PACKAGE_TYPE = "deb"

# ============================================================================
# PREPARAÇÃO INICIAL
# ============================================================================
# Diretório raiz do projeto (sobe dois níveis a partir deste script)
ROOT = Path(__file__).resolve().parents[2]
print(f"📁 Diretório raiz: {ROOT}")

# Verifica se o gradlew existe e tem permissão de execução
gradlew = ROOT / "gradlew"
if not gradlew.exists():
    raise FileNotFoundError(f"Arquivo {gradlew} não encontrado. Execute 'gradle wrapper' primeiro?")
if not os.access(gradlew, os.X_OK):
    print("🔧 Dando permissão de execução ao gradlew...")
    gradlew.chmod(gradlew.stat().st_mode | stat.S_IXUSR)

# Diretório temporário de trabalho
TEMP_DIR = ROOT / "temp"
shutil.rmtree(TEMP_DIR, ignore_errors=True)
TEMP_DIR.mkdir(exist_ok=True)

# ============================================================================
# 1. Limpeza e geração do fat JAR com ShadowJar
# ============================================================================
print("\n[1/6] 🧹 Limpando build anterior...")
subprocess.run([str(gradlew), "clean"], cwd=ROOT, check=True)

print("\n[2/6] 📦 Gerando fat JAR (shadowJar)...")
subprocess.run([str(gradlew), "shadowJar"], cwd=ROOT, check=True)

# Localiza o JAR gerado (deve haver apenas um)
jar_files = list((ROOT / "build" / "libs").glob("*.jar"))
if not jar_files:
    raise FileNotFoundError("Nenhum arquivo JAR encontrado em build/libs/")
jar_file = jar_files[0]
print(f"✅ JAR gerado: {jar_file.name}")

# Copia para temp/app.jar
shutil.copy(jar_file, TEMP_DIR / "app.jar")

# ============================================================================
# 2. Copiar módulos JavaFX (jars) e bibliotecas nativas (.so)
# ============================================================================
print("\n[3/6] 📚 Copiando JavaFX modules e nativas...")
javafx_base = ROOT / JAVAFX_DIR
javafx_lib = javafx_base / "lib"

if not javafx_lib.exists():
    raise FileNotFoundError(
        f"Diretórios do JavaFX não encontrados em {javafx_base}. "
        "Verifique se a versão e plataforma estão corretas."
    )

# Copia todos os jars e .so do JavaFX para temp/lib
shutil.copytree(javafx_lib, TEMP_DIR / "lib")

# ============================================================================
# 3. Gerar runtime customizado com jlink
# ============================================================================
print("\n[4/6] ⚙️  Gerando runtime mínimo com jlink...")
jdk_jmods = Path(os.environ["JAVA_HOME"]) / "jmods"
module_path = f"{TEMP_DIR / 'lib'}:{jdk_jmods}"  # Linux usa ':' como separador

subprocess.run(
    [
        "jlink",
        "--module-path", module_path,
        "--add-modules", "javafx.controls,javafx.base,javafx.graphics,java.sql,jdk.zipfs",
        "--output", str(TEMP_DIR / "runtime")
    ],
    cwd=ROOT,
    check=True
)

# Copia as bibliotecas nativas (.so) para dentro do runtime/lib
print("   Copiando nativas para runtime/lib...")
for so_file in (TEMP_DIR / "lib").glob("*.so"):
    dest = TEMP_DIR / "runtime" / "lib" / so_file.name
    dest.parent.mkdir(exist_ok=True)
    shutil.copy(so_file, dest)

# ============================================================================
# 4. (Opcional) Teste rápido do runtime - descomente se quiser validar
# ============================================================================
java_exe = TEMP_DIR / "runtime" / "bin" / "java"
print("\n[TESTE] Executando aplicação...")
subprocess.run(
    [
        str(java_exe),
        "-Djava.library.path={}".format(TEMP_DIR / "runtime" / "lib"),
        "-cp", str(TEMP_DIR / "app.jar"),
        MAIN_CLASS
    ],
    cwd=ROOT,
    check=True
)

# ============================================================================
# 5. Gerar pacote .deb com jpackage
# ============================================================================
print("\n[5/6] 🎁 Gerando pacote .deb com jpackage...")
shutil.rmtree(ROOT / "dist", ignore_errors=True)

jpackage_cmd = [
    "jpackage",
    "--input", str(TEMP_DIR),
    "--name", APP_NAME,
    "--app-version", APP_VERSION,
    "--main-jar", "app.jar",
    "--main-class", MAIN_CLASS,
    "--dest", "dist",
    "--type", "deb",
    "--runtime-image", str(TEMP_DIR / "runtime"),
    "--java-options", '"-Djava.library.path=$APPDIR/lib"',
    "--icon", str(ROOT / ICON_PATH),
    "--linux-shortcut",
    "--linux-menu-group", "Office",
    "--linux-package-name", APP_NAME.lower().replace(" ", "-"),
]

subprocess.run(jpackage_cmd, cwd=ROOT, check=True)

# ============================================================================
# 6. Renomear o pacote final com a versão
# ============================================================================
print("\n[6/6] 📝 Renomeando pacote final...")
dist_dir = ROOT / "dist"
deb_files = list(dist_dir.glob("*.deb"))
if not deb_files:
    raise FileNotFoundError("Nenhum pacote .deb gerado em dist/")
deb_gerado = deb_files[0]

final_name = f"{APP_NAME}-{APP_VERSION}.deb"
final_path = dist_dir / final_name
deb_gerado.rename(final_path)

print(f"\n✅ Pacote .deb criado com sucesso: {final_path}")
print("   Para instalar: sudo dpkg -i {}".format(final_path))