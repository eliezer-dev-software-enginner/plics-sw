#!/usr/bin/env python3
"""
Gerencia appVersion/appPatch em gradle.properties (única fonte de verdade da
versão do app — ver Main.APP_VERSION e scripts/config.py).

Uso:
  python scripts/bump_version.py patch            # 1.1.1 (patch 5) -> patch 6
  python scripts/bump_version.py release 1.1.2    # define versão base nova, zera o patch

Depois de rodar, gere o pacote normalmente (create-msi.py, create-msi-with-updater.py
etc.) — eles leem gradle.properties e já embutem a versão composta no runtime.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GRADLE_PROPERTIES = ROOT / "gradle.properties"


def ler_propriedades(texto: str) -> dict:
    props = {}
    for linha in texto.splitlines():
        linha = linha.strip()
        if linha and not linha.startswith("#"):
            chave, _, valor = linha.partition("=")
            props[chave.strip()] = valor.strip()
    return props


def escrever_propriedade(texto: str, chave: str, valor: str) -> str:
    padrao = re.compile(rf"^{re.escape(chave)}=.*$", re.MULTILINE)
    nova_linha = f"{chave}={valor}"
    if padrao.search(texto):
        return padrao.sub(nova_linha, texto)
    return texto.rstrip("\n") + f"\n{nova_linha}\n"


def versao_composta(base: str, patch: int) -> str:
    return base if patch == 0 else f"{base}.{patch}"


def main():
    if len(sys.argv) < 2 or sys.argv[1] not in ("patch", "release"):
        print(__doc__)
        sys.exit(1)

    texto = GRADLE_PROPERTIES.read_text(encoding="utf-8")
    props = ler_propriedades(texto)
    base_atual = props["appVersion"]
    patch_atual = int(props.get("appPatch", "0") or 0)

    if sys.argv[1] == "patch":
        novo_patch = patch_atual + 1
        texto = escrever_propriedade(texto, "appPatch", str(novo_patch))
        GRADLE_PROPERTIES.write_text(texto, encoding="utf-8")
        print(f"Patch: {versao_composta(base_atual, patch_atual)} -> {versao_composta(base_atual, novo_patch)}")

    else:  # release
        if len(sys.argv) != 3 or not re.fullmatch(r"\d+\.\d+\.\d+", sys.argv[2]):
            print("Uso: python scripts/bump_version.py release X.Y.Z")
            sys.exit(1)
        nova_base = sys.argv[2]
        texto = escrever_propriedade(texto, "appVersion", nova_base)
        texto = escrever_propriedade(texto, "appPatch", "0")
        GRADLE_PROPERTIES.write_text(texto, encoding="utf-8")
        print(f"Release: {versao_composta(base_atual, patch_atual)} -> {nova_base}")


if __name__ == "__main__":
    main()
