import hashlib
import subprocess
import sys
import time
from pathlib import Path

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

ROOT = Path(__file__).resolve().parent
WATCH_DIRS = ["src/main/java", "src/main/resources"]
GRADLE_RUN = ["./gradlew", "run"] if sys.platform != "win32" else ["gradlew.bat", "run"]
DEBOUNCE_SECONDS = 3.5

# Editores (IntelliJ com "safe write", vim swap, etc.) criam/tocam arquivos sem que
# o conteúdo do seu código tenha de fato mudado — não conta como mudança real.
IGNORED_NAME_MARKERS = ("___jb_tmp___", "___jb_old___", ".swp", ".swx", "~")

process = None


def is_noise(path_str: str) -> bool:
    name = Path(path_str).name
    if name.startswith("."):
        return True
    return any(marker in name for marker in IGNORED_NAME_MARKERS)


def content_hash(path_str: str):
    try:
        with open(path_str, "rb") as f:
            return hashlib.sha256(f.read()).hexdigest()
    except OSError:
        return None


class ChangeHandler(FileSystemEventHandler):
    """
    Só reinicia quando o CONTEÚDO de um arquivo realmente muda — não a qualquer
    evento de sistema de arquivos. Isso é o que faltava: o IntelliJ salva (ou
    "re-salva" sem mudança nenhuma) todos os arquivos abertos ao perder foco da
    janela ("Save on frame deactivation"), e faz isso via write-num-temp +
    rename-por-cima (aparece pro watchdog como um evento "moved"). Cada um desses
    disparava um restart mesmo sem nada ter mudado de verdade.
    """

    def __init__(self):
        self.last_change = 0
        self.known_hashes = {}

    def on_any_event(self, event):
        if event.is_directory:
            return

        target_path = getattr(event, "dest_path", None) or event.src_path
        if is_noise(target_path):
            return

        if event.event_type == "deleted":
            changed = self.known_hashes.pop(event.src_path, None) is not None
        else:
            if event.event_type == "moved":
                # o caminho antigo (temp file do safe-write) some — só o dest importa
                self.known_hashes.pop(event.src_path, None)

            new_hash = content_hash(target_path)
            if new_hash is None:
                return  # arquivo já não existe mais / não deu pra ler — ignora

            changed = self.known_hashes.get(target_path) != new_hash
            self.known_hashes[target_path] = new_hash

        if not changed:
            return

        now = time.time()
        if now - self.last_change > DEBOUNCE_SECONDS:
            self.last_change = now
            restart()


def start():
    global process
    print("[dev] Iniciando aplicação...")
    process = subprocess.Popen(GRADLE_RUN, cwd=ROOT)


def kill_process():
    global process
    if process is None:
        return

    if sys.platform == "win32":
        subprocess.run(
            ["taskkill", "/F", "/T", "/PID", str(process.pid)],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
    else:
        process.terminate()
        process.wait()

    process = None


def restart():
    print("[dev] Mudança detectada — reiniciando...")
    kill_process()
    start()


if __name__ == "__main__":
    # O .desktop de desenvolvimento (pro ícone aparecer na dock/taskbar no Linux) é
    # criado pelo próprio app agora — megalodonte.application.LinuxDesktopEntry,
    # chamado a partir de Main.java — e não mais daqui, pra valer também rodando
    # direto pela IDE, sem passar por este script.
    start()
    observer = Observer()
    handler = ChangeHandler()
    for d in WATCH_DIRS:
        observer.schedule(handler, d, recursive=True)
    observer.start()
    print(f"[dev] Monitorando: {WATCH_DIRS} (Ctrl+C para sair)")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("[dev] Encerrando...")
        if process:
            process.terminate()
        observer.stop()
    observer.join()
