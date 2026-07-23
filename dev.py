import subprocess
import time
import sys
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

WATCH_DIRS = ["src/main/java", "src/main/resources"]
GRADLE_RUN = ["./gradlew", "run"] if sys.platform != "win32" else ["gradlew.bat", "run"]
DEBOUNCE_SECONDS = 3.5

process = None

class ChangeHandler(FileSystemEventHandler):
    def __init__(self):
        self.last_change = 0

    def on_any_event(self, event):
        if event.is_directory:
            return
        now = time.time()
        if now - self.last_change > DEBOUNCE_SECONDS:
            self.last_change = now
            restart()

def start():
    global process
    print("[dev] Iniciando aplicação...")
    process = subprocess.Popen(GRADLE_RUN)

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
    start()
    observer = Observer()
    for d in WATCH_DIRS:
        observer.schedule(ChangeHandler(), d, recursive=True)
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