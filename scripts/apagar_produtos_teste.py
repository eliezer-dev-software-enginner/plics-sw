#!/usr/bin/env python3
"""
Apaga os produtos de teste (marcados com 'PERFTEST') que foram inseridos por
scripts/criar_produtos_teste.py no banco de PRODUÇÃO do app.

Uso:
  python scripts/apagar_produtos_teste.py

O banco é o mesmo do app real (Windows: %APPDATA%/plics-sw/erp.db; demais:
~/.plics-sw/erp.db).
"""
import os
import sqlite3
import sys
from pathlib import Path

PREFIXO = "PERFTEST"


def caminho_do_banco() -> Path:
    base = (Path(os.environ.get("APPDATA")) / "plics-sw") if os.name == "nt" else (Path.home() / ".plics-sw")
    base.mkdir(parents=True, exist_ok=True)
    return base / "erp.db"


def main():
    db = caminho_do_banco()
    print(f"Banco: {db}")

    con = sqlite3.connect(db)
    try:
        apagados = con.execute("DELETE FROM produtos WHERE codigo_barras LIKE ?", (PREFIXO + "%",)).rowcount
        restantes = con.execute("SELECT COUNT(*) FROM produtos WHERE codigo_barras LIKE ?", (PREFIXO + "%",)).fetchone()[0]
        con.commit()
        print(f"Apagados {apagados} produtos de teste; restantes: {restantes}")
        if restantes:
            sys.exit(f"ERRO: ainda existem {restantes} produtos PERFTEST")
    finally:
        con.close()


if __name__ == "__main__":
    main()