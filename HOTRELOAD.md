# Hot Reload (Python)

## Setup

```bash
python3 -m venv .venv
.venv/bin/pip install watchdog
```

## Uso

```bash
.venv/bin/python dev.py
```

Ou ative o venv:

```bash
source .venv/bin/activate
python dev.py
```

## Como funciona

1. Monitora alterações em `src/main/java/` via `watchdog`
2. Ao detectar mudança, compila os arquivos `.java` alterados com `javac`
3. Copia resources modificados para `build/classes/java/main/`
4. Recarrega a tela no JavaFX via `Reloader`
