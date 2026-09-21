#!/usr/bin/env python3
"""
Insere 20 mil produtos de teste (marcados com 'PERFTEST') no banco de PRODUÇÃO
do app pra medir a eficiência do app com muitos registros. Para apagar depois,
rode scripts/apagar_produtos_teste.py.

Uso:
  python scripts/criar_produtos_teste.py             # 20 mil produtos
  python scripts/criar_produtos_teste.py --total 1000

O banco é o mesmo do app real (Windows: %APPDATA%/plics-sw/erp.db; demais:
~/.plics-sw/erp.db). O script avisa (não bloqueia) se o banco estiver numa
migration mais antiga que o código.
"""
import argparse
import os
import sqlite3
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MIGRATIONS = ROOT / "src" / "main" / "resources" / "flyway_migrations"

PREFIXO = "PERFTEST"
TOTAL = 20_000

COLUNAS = (
    "id, codigo_barras, descricao, preco_compra, preco_venda, unidade, "
    "categoria_id, fornecedor_id, estoque, observacoes, imagem, marca, validade, "
    "garantia, dataCriacao, total_liquido, cor, tamanho, modelo, estoque_minimo, "
    "frete, aceita_devolucao"
)


def caminho_do_banco() -> Path:
    base = (Path(os.environ.get("APPDATA")) / "plics-sw") if os.name == "nt" else (Path.home() / ".plics-sw")
    base.mkdir(parents=True, exist_ok=True)
    return base / "erp.db"


def migration_esperada() -> int:
    return max(int(f.name.split("__")[0][1:]) for f in MIGRATIONS.glob("V*.sql"))


def migration_do_banco(con: sqlite3.Connection) -> int:
    try:
        return con.execute("SELECT COALESCE(MAX(CAST(version AS INTEGER)), 0) FROM flyway_schema_history").fetchone()[0]
    except sqlite3.Error:
        return 0


def contar_teste(con: sqlite3.Connection) -> int:
    return con.execute("SELECT COUNT(*) FROM produtos WHERE codigo_barras LIKE ?", (PREFIXO + "%",)).fetchone()[0]


def apagar_teste(con: sqlite3.Connection) -> int:
    return con.execute("DELETE FROM produtos WHERE codigo_barras LIKE ?", (PREFIXO + "%",)).rowcount


def produtos(numero: int, inicio_id: int, agora_ms: int) -> list:
    linhas = []
    for i in range(numero):
        linhas.append(
            (
                inicio_id + i,
                f"{PREFIXO}{i + 1:07d}",
                f"Produto Performance {i + 1:06d}",
                10.0,
                19.5,
                "UN",
                None,
                None,
                10.0,
                "DADO DE PERFORMANCE - REMOVER APOS TESTE",
                None,
                "MARCA PERFORMANCE TESTE",
                None,
                None,
                agora_ms,
                9.5,
                None,
                None,
                None,
                0.0,
                0.0,
                0,
            )
        )
    return linhas


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--total", type=int, default=TOTAL, help=f"quantidade de produtos (padrão: {TOTAL})")
    args = parser.parse_args()

    db = caminho_do_banco()
    print(f"Banco: {db}")

    con = sqlite3.connect(db)
    try:
        mig_bd = migration_do_banco(con)
        mig_esp = migration_esperada()
        if mig_esp > mig_bd:
            print(f"ATENÇÃO: banco está na migration {mig_bd}, código espera {mig_esp}. "
                  "Abra o app uma vez (ou rode as migrations) antes de confiar nos dados.")

        apagados = apagar_teste(con)
        proximo_id = con.execute("SELECT id FROM produtos ORDER BY id DESC LIMIT 1").fetchone()
        inicio_id = (proximo_id[0] + 1) if proximo_id else 1
        agora_ms = int(time.time() * 1000)

        inicio = time.perf_counter()
        with con:
            con.executemany(
                f"INSERT INTO produtos ({COLUNAS}) VALUES ({', '.join('?' * 22)})",
                produtos(args.total, inicio_id, agora_ms),
            )
        duracao_ms = (time.perf_counter() - inicio) * 1000

        if contar_teste(con) != args.total:
            raise RuntimeError(f"Inserção falhou: esperado {args.total}, encontrado {contar_teste(con)}")

        print(
            f"Apagados {apagados} produtos PERFTEST antigos; inseridos {args.total} produtos "
            f"(ids {inicio_id}..{inicio_id + args.total - 1}) em {duracao_ms:.1f} ms "
            f"({duracao_ms / args.total:.2f} ms/produto)"
        )
    finally:
        con.close()


if __name__ == "__main__":
    main()