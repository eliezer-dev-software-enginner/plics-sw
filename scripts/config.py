from pathlib import Path
import subprocess
import shutil
import os

ROOT = Path(__file__).resolve().parent.parent

APP_NAME = "Plics SW"
APP_VERSION = "1.0.3"
MAIN_CLASS = "my_app.Main"
ICON_PATH = "src/main/resources/assets/app_ico.png"
JAVAFX_VERSION = "25.0.1"


def get_platform():
    if os.name == "nt":
        return "windows"
    return "linux"


def javafx_dir():
    return ROOT / f"java_fx_modules/{get_platform()}-{JAVAFX_VERSION}"


def run_gradle(*tasks):
    gradlew = ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")
    subprocess.run([str(gradlew), *tasks], cwd=ROOT, check=True)


def find_jar():
    jars = list((ROOT / "build" / "libs").glob("*.jar"))
    if not jars:
        raise FileNotFoundError("Nenhum JAR encontrado em build/libs/")
    return jars[0]


def prepare_temp():
    temp_dir = ROOT / "temp"
    shutil.rmtree(temp_dir, ignore_errors=True)
    temp_dir.mkdir(exist_ok=True)
    return temp_dir


def copy_javafx(temp_dir: Path):
    jfx = javafx_dir()
    shutil.copytree(jfx / "lib", temp_dir / "lib")
    bin_dir = jfx / "bin"
    if bin_dir.exists():
        shutil.copytree(bin_dir, temp_dir / "bin")


def run_jlink(temp_dir: Path):
    jdk_jmods = Path(os.environ["JAVA_HOME"]) / "jmods"
    sep = ";" if os.name == "nt" else ":"
    module_path = f"{temp_dir / 'lib'}{sep}{jdk_jmods}"
    subprocess.run(
        [
            "jlink",
            "--module-path", module_path,
            "--add-modules", "javafx.controls,javafx.base,javafx.graphics,java.sql,jdk.zipfs",
            "--output", str(temp_dir / "runtime")
        ],
        cwd=ROOT, check=True
    )


def copy_natives(temp_dir: Path):
    ext = ".dll" if os.name == "nt" else ".so"
    target = "bin" if os.name == "nt" else "lib"
    for native in (temp_dir / "lib").glob(f"*{ext}"):
        dest = temp_dir / "runtime" / target / native.name
        dest.parent.mkdir(exist_ok=True)
        shutil.copy(native, dest)


def run_jpackage(temp_dir: Path, pkg_type: str, extra_args: list = None):
    shutil.rmtree(ROOT / "dist", ignore_errors=True)
    lib_path = "$APPDIR/bin" if os.name == "nt" else "$APPDIR/lib"
    cmd = [
        "jpackage",
        "--input", str(temp_dir),
        "--name", APP_NAME,
        "--app-version", APP_VERSION,
        "--main-jar", "app.jar",
        "--main-class", MAIN_CLASS,
        "--dest", "dist",
        "--type", pkg_type,
        "--runtime-image", str(temp_dir / "runtime"),
        "--java-options", f'"-Djava.library.path={lib_path}"',
        "--icon", str(ROOT / ICON_PATH),
    ]
    if extra_args:
        cmd.extend(extra_args)
    subprocess.run(cmd, cwd=ROOT, check=True)


def rename_output(pkg_type: str):
    dist_dir = ROOT / "dist"
    ext = f".{pkg_type}"
    files = list(dist_dir.glob(f"*{ext}"))
    if not files:
        raise FileNotFoundError(f"Nenhum pacote {ext} gerado em dist/")
    final_name = f"{APP_NAME}-{APP_VERSION}{ext}"
    files[0].rename(dist_dir / final_name)
    return dist_dir / final_name
