CREATE TABLE IF NOT EXISTS preferencias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    credenciais_habilitadas INTEGER NOT NULL,
    tema TEXT NOT NULL,
    login TEXT,
    senha TEXT,
    primeiro_acesso INTEGER NOT NULL DEFAULT 1,
    dataCriacao REAL NOT NULL
)
