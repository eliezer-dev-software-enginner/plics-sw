import subprocess
from pathlib import Path
import os

import shutil

ROOT = Path(__file__).resolve().parents[2]

subprocess.run(
    ["gradlew.bat", "clean"],
    cwd=ROOT,
    check=True
)







#---------------------------

APP_NAME = "Plics SW"
APP_VERSION = "1.0.3"

#----------------------------

# 1. Gera o fat jar
subprocess.run(
    ["gradlew.bat", "shadowJar"],
    cwd=ROOT,
    check=True
)

# 2. Copia pra temp (substitui o anterior)
temp_dir = ROOT / "temp"
shutil.rmtree(temp_dir, ignore_errors=True)
temp_dir.mkdir(exist_ok=True)

# 3. Procura jar gerado
jar_file = next((ROOT / "build" / "libs").glob("*.jar"))

#copy build\libs\*.jar temp\app.jar
# 4. Copia o jar e renomeia
shutil.copy(
    jar_file,
    temp_dir / "app.jar"
)

# 5. Copia o os arquivos nativos do JavaFX para temp\lib
javafxlibs_dir = ROOT / "java_fx_modules" / "windows-25.0.1" / "lib"
javafxbin_dir = ROOT / "java_fx_modules" / "windows-25.0.1" / "bin"

shutil.copytree(javafxlibs_dir, temp_dir / "lib")
shutil.copytree(javafxbin_dir, temp_dir / "bin")

# 6. Copia o jar e renomeia
shutil.copy(
    jar_file,
    temp_dir / "app.jar"
)

#5 gera o runtime
# jlink --module-path "temp/lib;$env:JAVA_HOME\jmods" `
#       --add-modules "javafx.controls,javafx.base,javafx.graphics,java.sql" `
#       --output temp\runtime


jdk_jmods = Path(os.environ["JAVA_HOME"]) / "jmods"
module_path = f"temp\\lib;{jdk_jmods}"

subprocess.run(
    [
        "jlink",
        "--module-path",
        module_path,
        "--add-modules",
        "javafx.controls,javafx.base,javafx.graphics,java.sql,jdk.zipfs",
        "--output",
        "temp\\runtime"
    ],
    cwd=ROOT,
    check=True
)

#6 Copia as dlls do JavaFX para temp\runtime\bin
#copy temp\bin\*.dll temp\runtime\bin\

for dll in (temp_dir / "bin").glob("*.dll"):
    shutil.copy(
        dll,
        temp_dir / "runtime" / "bin" / dll.name
    )

# 7 Testa
#.\temp\runtime\bin\java.exe "-Djava.library.path=build\bin" -cp "temp\app.jar" my_app.Main

# java_exe = ROOT / "temp" / "runtime" / "bin" / "java.exe"
# subprocess.run(
#     [
#         str(java_exe),
#         "-Djava.library.path=build\\bin",
#         "-cp",
#         str(ROOT / "temp" / "app.jar"),
#         "my_app.Main"
#     ],
#     cwd=ROOT,
#     check=True
# )


## 8 Cria o MSI usando o WiX Toolset

#apaga pasta dist
shutil.rmtree(ROOT / "dist", ignore_errors=True)

#jpackage --input "build" --name "Meu App" --main-jar "app.jar" --main-class "my_app.Launch" --dest "dist" --type "msi" --runtime-image "build\runtime" --java-options '"-Djava.library.path=$APPDIR\bin"' --icon "src\main\resources\assets\app_ico.ico" --win-menu --win-shortcut
subprocess.run(
    [
        "jpackage",
        "--input",
        "temp",
        "--name",
        APP_NAME,
        "--app-version",
        APP_VERSION,
        "--main-jar",
        "app.jar",
        "--main-class",
        "my_app.Main",
        "--dest",
        "dist",
        "--type",
        "msi",
        "--runtime-image",
        "temp\\runtime",
        "--java-options",
        '"-Djava.library.path=$APPDIR\\bin"',
        "--icon",
        "src\\main\\resources\\assets\\app_ico.ico",
        "--win-menu",
        "--win-shortcut",
        "--win-per-user-install"#aparece aquela tela de "Instalar"
    ],
    cwd=ROOT,
    check=True
)

# Procura MSI gerado
generated_msi = next((ROOT / "dist").glob("*.msi"))

# Nome final
final_msi = ROOT / "dist" / f"{APP_NAME}-{APP_VERSION}.msi"

# Renomeia
generated_msi.rename(final_msi)

print(f"MSI criado com sucesso em: {final_msi}")