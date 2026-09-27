from pathlib import Path
import subprocess
import shutil
import os

ROOT = Path(__file__).resolve().parent.parent


def _read_gradle_properties():
    props = {}

    with open(ROOT / "gradle.properties") as f:
        for line in f:
            line = line.strip()

            if line and not line.startswith("#"):
                k, _, v = line.partition("=")
                props[k.strip()] = v.strip()

    return props


_gradle_props = _read_gradle_properties()

APP_NAME = _gradle_props["appDisplayName"]

# appVersion (base, x.x.x) + appPatch (contador, 0 = sem patch) compostos numa
# única string — mesma lógica de build.gradle.kts. Ver gradle.properties.
_patch_number = int(_gradle_props.get("appPatch", "0") or 0)

APP_VERSION = (
    _gradle_props["appVersion"]
    if _patch_number == 0
    else f"{_gradle_props['appVersion']}.{_patch_number}"
)

MAIN_CLASS = _gradle_props["appMainClass"]
VENDOR = _gradle_props["appVendor"]
LINUX_MENU_GROUP = _gradle_props["appLinuxMenuGroup"]

ICON_PATH = (
    "src/main/resources/assets/app_ico.ico"
    if os.name == "nt"
    else "src/main/resources/assets/app_ico.png"
)

JAVAFX_VERSION = "25.0.1"

# Upgrade code fixo do MSI (Windows). Precisa ser o mesmo em toda geração de MSI
# (normal ou Store) pra o Windows tratar uma nova versão como upgrade da anterior
# em vez de instalar um produto "diferente" ao lado.
UPGRADE_UUID = "e3a2b1c4-7d5f-4a8e-9c6b-2f1d0a3e7b8c"


def get_platform():
    if os.name == "nt":
        return "windows"

    return "linux"


def javafx_dir():
    javafx_modules_home = os.environ.get("JAVAFX_MODULES_HOME")

    if not javafx_modules_home:
        raise EnvironmentError(
            "Variável de ambiente JAVAFX_MODULES_HOME não definida. "
            "Defina-a apontando para a pasta que contém "
            f"{get_platform()}-{JAVAFX_VERSION}/."
        )

    return (
        Path(javafx_modules_home)
        / f"{get_platform()}-{JAVAFX_VERSION}"
    )


def _java_home():
    jh = Path(os.environ.get("JAVA_HOME", ""))

    if jh.name == "bin":
        jh = jh.parent

    return str(jh)


def run_gradle(*tasks):
    gradlew = ROOT / (
        "gradlew.bat"
        if os.name == "nt"
        else "gradlew"
    )

    env = {
        **os.environ,
        "JAVA_HOME": _java_home()
    }

    subprocess.run(
        [str(gradlew), *tasks],
        cwd=ROOT,
        check=True,
        env=env
    )


def find_jar():
    jars = list(
        (ROOT / "build" / "libs").glob("*.jar")
    )

    if not jars:
        raise FileNotFoundError(
            "Nenhum JAR encontrado em build/libs/"
        )

    return jars[0]


def prepare_temp():
    temp_dir = ROOT / "temp"

    # Remove qualquer build temporário anterior.
    shutil.rmtree(
        temp_dir,
        ignore_errors=True
    )

    # A pasta temporária agora é separada por responsabilidade:
    #
    # temp/
    # ├── input/
    # │   └── app.jar
    # │
    # ├── javafx/
    # │   ├── lib/
    # │   └── bin/
    # │
    # └── runtime/
    #
    # input:
    #   contém apenas os arquivos da aplicação que o jpackage deve copiar
    #   para a pasta "app" do pacote final.
    #
    # javafx:
    #   é apenas uma área temporária contendo o JavaFX SDK utilizado
    #   pelo jlink e para obtenção das bibliotecas nativas.
    #
    # runtime:
    #   contém a imagem Java customizada criada pelo jlink.
    #
    # Essa separação evita que lib/, bin/ e runtime/ sejam copiados novamente
    # para dentro da pasta app/ pelo jpackage.

    (temp_dir / "input").mkdir(
        parents=True,
        exist_ok=True
    )

    (temp_dir / "javafx").mkdir(
        parents=True,
        exist_ok=True
    )

    return temp_dir


def copy_javafx(temp_dir: Path):
    # Diretório original do JavaFX 25.0.1.
    jfx = javafx_dir()

    # O JavaFX não é mais copiado diretamente para temp/.
    # Ele fica isolado dentro de temp/javafx/.
    javafx_temp = temp_dir / "javafx"

    # JARs modulares do JavaFX utilizados pelo jlink.
    shutil.copytree(
        jfx / "lib",
        javafx_temp / "lib"
    )

    # DLLs/SOs nativos do JavaFX.
    bin_dir = jfx / "bin"

    if bin_dir.exists():
        shutil.copytree(
            bin_dir,
            javafx_temp / "bin"
        )


def run_jlink(temp_dir: Path):
    # Módulos do próprio JDK.
    jdk_jmods = (
        Path(_java_home())
        / "jmods"
    )

    sep = ";" if os.name == "nt" else ":"

    # Os módulos JavaFX agora ficam dentro de temp/javafx/lib.
    javafx_lib = (
        temp_dir
        / "javafx"
        / "lib"
    )

    # O module-path combina:
    #
    # - módulos JavaFX
    # - módulos do JDK
    module_path = (
        f"{javafx_lib}"
        f"{sep}"
        f"{jdk_jmods}"
    )

    # O JAR da aplicação agora fica isolado em temp/input.
    app_jar = (
        temp_dir
        / "input"
        / "app.jar"
    )

    java_bin = (
        Path(_java_home())
        / "bin"
    )

    jdeps_cmd = str(
        java_bin / "jdeps"
    )

    # Módulos que sabemos que o aplicativo precisa independentemente
    # do resultado obtido pelo jdeps.
    base_modules = {
        "javafx.controls",
        "java.sql",
        "jdk.zipfs",
        "java.logging",
        "java.xml",

        # Necessário para codepages como Cp860 e Cp437,
        # utilizadas principalmente na impressão ESC/POS.
        "jdk.charsets",
    }

    try:
        # Tenta detectar automaticamente módulos Java/JDK utilizados
        # pela aplicação.
        jdeps = subprocess.run(
            [
                jdeps_cmd,
                "--print-module-deps",
                "--ignore-missing-deps",
                "--module-path",
                str(javafx_lib),
                str(app_jar)
            ],
            capture_output=True,
            text=True,
            cwd=ROOT,
            check=True
        )

        lines = [
            line
            for line in jdeps.stdout.strip().splitlines()
            if not line.startswith("Warning:")
        ]

        found = set()

        if lines:
            found.update(
                lines[-1].split(",")
            )

        found.discard("")

        # Junta os módulos detectados automaticamente com os módulos
        # obrigatórios definidos acima.
        modules = ",".join(
            sorted(
                base_modules | found
            )
        )

    except subprocess.CalledProcessError:
        # Se o jdeps falhar, utiliza pelo menos os módulos obrigatórios.
        modules = ",".join(
            sorted(base_modules)
        )

    jlink_cmd = str(
        java_bin / "jlink"
    )

    # Gera um runtime Java reduzido específico para o aplicativo.
    subprocess.run(
        [
            jlink_cmd,

            "--module-path",
            module_path,

            "--add-modules",
            modules,

            "--no-header-files",
            "--no-man-pages",

            "--output",
            str(
                temp_dir
                / "runtime"
            )
        ],
        cwd=ROOT,
        check=True
    )


def copy_natives(temp_dir: Path):
    # O JavaFX possui bibliotecas nativas que não entram automaticamente
    # no runtime criado pelo jlink quando usamos os JARs do JavaFX SDK.
    #
    # Por enquanto mantemos a mesma estratégia original:
    # copiar as DLLs/SOs do JavaFX para a imagem runtime.
    #
    # Essa lógica poderá ser melhorada futuramente para copiar somente
    # natives pertencentes aos módulos efetivamente utilizados.

    ext = (
        ".dll"
        if os.name == "nt"
        else ".so"
    )

    # No Windows as DLLs ficam em runtime/bin.
    # No Linux as bibliotecas ficam em runtime/lib.
    target = (
        "bin"
        if os.name == "nt"
        else "lib"
    )

    javafx_temp = (
        temp_dir
        / "javafx"
    )

    source = (
        javafx_temp / "bin"
        if (javafx_temp / "bin").exists()
        else javafx_temp / "lib"
    )

    for native in source.glob(f"*{ext}"):
        dest = (
            temp_dir
            / "runtime"
            / target
            / native.name
        )

        dest.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        shutil.copy(
            native,
            dest
        )


def run_jpackage(
    temp_dir: Path,
    pkg_type: str,
    extra_args: list = None
):
    # Remove qualquer pacote gerado anteriormente.
    shutil.rmtree(
        ROOT / "dist",
        ignore_errors=True
    )

    jpackage_cmd = str(
        Path(_java_home())
        / "bin"
        / "jpackage"
    )

    cmd = [
        jpackage_cmd,

        # IMPORTANTE:
        #
        # --input aponta agora SOMENTE para temp/input.
        #
        # Antes apontava para temp inteiro, fazendo com que:
        #
        # temp/bin
        # temp/lib
        # temp/runtime
        #
        # fossem copiados para dentro da pasta app/ do pacote gerado.
        #
        # Agora apenas app.jar será tratado como arquivo da aplicação.
        "--input",
        str(
            temp_dir
            / "input"
        ),

        "--name",
        APP_NAME,

        "--app-version",
        APP_VERSION,

        "--vendor",
        VENDOR,

        # Main.APP_VERSION lê essa property em runtime
        # (mesmo padrão utilizado pelo isMicrosoftStore).
        "--java-options",
        f"-Dplics.appVersion={APP_VERSION}",

        "--main-jar",
        "app.jar",

        "--main-class",
        MAIN_CLASS,

        "--dest",
        "dist",

        "--type",
        pkg_type,

        # O runtime fica completamente separado dos arquivos da aplicação.
        # O jpackage irá utilizá-lo como runtime oficial do aplicativo.
        "--runtime-image",
        str(
            temp_dir
            / "runtime"
        ),

        "--icon",
        str(
            ROOT
            / ICON_PATH
        ),
    ]

    if extra_args:
        cmd.extend(
            extra_args
        )

    subprocess.run(
        cmd,
        cwd=ROOT,
        check=True
    )


def smoke_test(temp_dir):
    # Executável Java da imagem runtime criada pelo jlink.
    java_exe = (
        temp_dir
        / "runtime"
        / "bin"
        / "java"
    )

    # JAR da aplicação.
    app_jar = (
        temp_dir
        / "input"
        / "app.jar"
    )

    # Executa a aplicação diretamente usando o runtime recém-criado.
    # Se ela permanecer aberta por mais de 5 segundos, consideramos
    # que o runtime iniciou corretamente.
    proc = subprocess.Popen(
        [
            str(java_exe),

            "-Djava.library.path={}".format(
                temp_dir
                / "runtime"
                / "lib"
            ),

            "-cp",
            str(app_jar),

            MAIN_CLASS
        ],
        cwd=ROOT
    )

    try:
        proc.wait(
            timeout=5
        )

        if proc.returncode != 0:
            raise RuntimeError(
                "Smoke test falhou "
                f"(exit code {proc.returncode})"
            )

    except subprocess.TimeoutExpired:
        proc.terminate()
        proc.wait()


def open_dist_folder():
    # CI runners (GitHub Actions sets CI=true on all of them) não têm
    # sessão de desktop.
    #
    # xdg-open pode nem existir no runner Linux e os.startfile no Windows
    # não terá Explorer disponível.
    #
    # Sem essa guarda, o workflow poderia falhar no fim do processo,
    # depois de o pacote já ter sido criado corretamente.
    if os.environ.get("CI"):
        return

    dist_dir = (
        ROOT
        / "dist"
    )

    if os.name == "nt":
        os.startfile(
            dist_dir
        )

    else:
        subprocess.run(
            [
                "xdg-open",
                str(dist_dir)
            ]
        )


def rename_output(pkg_type: str):
    dist_dir = (
        ROOT
        / "dist"
    )

    ext = f".{pkg_type}"

    files = list(
        dist_dir.glob(
            f"*{ext}"
        )
    )

    if not files:
        raise FileNotFoundError(
            f"Nenhum pacote {ext} "
            "gerado em dist/"
        )

    final_name = (
        f"{APP_NAME}-"
        f"{APP_VERSION}"
        f"{ext}"
    )

    files[0].rename(
        dist_dir
        / final_name
    )

    return (
        dist_dir
        / final_name
    )